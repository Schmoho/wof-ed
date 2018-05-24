package controller;

import java.util.HashMap;
import java.util.Map;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import view.NetElementGraphic;
import view.NodeGraphic;
import view.TransitionGraphic;

/**
 * Implementiert EventHandler, die jeweils an allen Knoten, Pfeilen und Namensschildern registriert werden,
 * um Drag'n'Drop, das Zeichnen von Kanten, Umbenennung und Auswahl zum Löschen zu ermöglichen.
 */
class NodeEventsController {

	//**** FIELDS ****
	/**
	 * registriert die x-Koordinate an der das MouseEvent was zum Drag führt registriert wurde vermittels e.getX()
	 */
	private double eventStartX;
	/**
	 * registriert die y-Koordinate an der das MouseEvent was zum Drag führt registriert wurde vermittels e.getY()
	 */
	private double eventStartY;
	/**
	 * hält während des Drags die Entfernung des MouseDragged-Events von eventStartX fest,<br>
	 * d.h. die Entfernung um die das Element in x-Richtung verschoben wurde
	 */
	private double offsetX;

	/**
	 * hält während des Drags die Entfernung des MouseDragged-Events von eventStartY fest,<br>
	 * d.h. die Entfernung um die das Element in y-Richtung verschoben wurde
	 */
	private double offsetY;
	/**
	 * hält für einen einzelnen zu verschiebenden Knoten seine anfängliche x-Koordinate fest, um
	 * mit dem Offset die neue Position des Knoten zu bestimmten.
	 */
	private double nodeStartX;
	/**
	 * hält für einen einzelnen zu verschiebenden Knoten seine anfängliche y-Koordinate fest, um
	 * mit dem Offset die neue Position des Knoten zu bestimmten.
	 */
	private double nodeStartY;
	/**
	 * hält für eine Mehrzahl zu verschiebender Knoten ihre anfänglichen x-Koordinaten fest, um
	 * mit dem Offset die neuen Positionen der Knoten zu bestimmten.
	 */
	private Map<NodeGraphic, Double> nodeStartXMap = new HashMap<NodeGraphic, Double>();
	/**
	 * hält für eine Mehrzahl zu verschiebender Knoten ihre anfänglichen y-Koordinaten fest, um
	 * mit dem Offset die neuen Positionen der Knoten zu bestimmten.
	 */
	private Map<NodeGraphic, Double> nodeStartYMap = new HashMap<NodeGraphic, Double>();
	/**
	 * flag um Drags von Clicks zu unterscheiden
	 */
	private static boolean dragging;
	/**
	 * Eingabefeld was für die Umbenennung von Knoten eingeblendet wird
	 */
	private TextField inputField = new TextField();
	/**
	 * Referenz auf den ViewController
	 */
	private static ViewController viewContr;
	/**
	 * Referenz auf den Controller für Events auf dem PetriNetPane
	 */
	private static PaneEventsController paneContr;

	/**
	 * Kontextmenü welches das Umbenennn namenloser Knoten erlaubt.
	 */
	private ContextMenu rightClickMenu = new ContextMenu();
	/***
	 * Menüeintrag für das Umbenennen
	 */
	private MenuItem renameNode = new MenuItem("rename node");

	//**** CONSTRUCTOR ****
	/**
	 * Nimmt Referenzen auf den ViewController, den PaneEventsController und initialisiert das Kontextmenü.
	 * @param v
	 * 		Referenz auf den ViewController
	 * @param p
	 * 		Referenz auf den PaneEventsController
	 */
	public NodeEventsController(ViewController v, PaneEventsController p) {
		viewContr = v;
		paneContr = p;
		rightClickMenu.getItems().add(renameNode);

	}

	//**** EVENT HANDLERS ****
	//handler distinguishing between events relevant in editing
	/**
	 * Allgemeiner EventHandler, der an Knoten registriert wird und die entsprechenden anderen EventHandler auslöst.
	 */
	private final EventHandler<MouseEvent> generalEventHandler = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent e) {

			if (e.getEventType() == MouseEvent.MOUSE_PRESSED) {
				dragging = false;
				MousePressedHandler.handle(e);
			}
			else if (e.getEventType() == MouseEvent.DRAG_DETECTED) {
				dragging = true;
				paneContr.unregisterBrush();
			}
			else if (e.getEventType() == MouseEvent.MOUSE_DRAGGED) {
				MouseDraggedHandler.handle(e);
			}
			else if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {
				if (!dragging) {
					OnClickHandler.handle(e);
				}
				paneContr.registerBrush(e);
			}
			else if (e.getEventType() == MouseEvent.MOUSE_ENTERED) {
				MouseEnteredHandler.handle(e);
			}
			else if (e.getEventType() == MouseEvent.MOUSE_EXITED) {
				MouseExitedHandler.handle(e);
			}
			else if (e.getEventType() == MouseEvent.MOUSE_RELEASED) {
				DragReleasedHandler.handle(e);
			}
		}
	};

	/**
	 * Löst bei Linksklick<br>
	 * - im Simulationsmodus auf eine aktivierte Transition ohne Kontakt das Feuern der Transition aus;<br>
	 * - im Editmodus ggf. das Zeichnen einer Kante zwischen zwei Knoten aus;<br>
	 * - oder wählt im Editmodus einen neuen Knoten aus, wenn keiner aktiviert ist, oder zwischen dem aktivierten
	 * 		und diesem keine Kante gezeichnet werden kann, oder mehrere Knoten oder nur Pfeile ausgewählt sind
	 * <br><br>
	 * Löst bei Rechtsklick<br>
	 * - im Editmodus das Anzeigen eines Kontextmenüs aus und registriert am "rename"-Menüeintrag
	 * 		die handle-Methode des nameTag-handlers und reicht dafür das MouseEvent durch
	 * <br><br>
	 * Konsumiert das Event.
	 */
	private EventHandler<MouseEvent> OnClickHandler = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent e) {
			if (e.getButton() == MouseButton.PRIMARY) {
				//Feuere eine aktivierte Transition, dann prüfe auf Deadlock
				if (!paneContr.isEditMode()
						&& e.getSource().getClass() == TransitionGraphic.class 
						&& ((TransitionGraphic) e.getSource()).getModel().activated().get()
						&& !((TransitionGraphic) e.getSource()).getModel().hasContact()) {
					((TransitionGraphic) e.getSource()).getModel().fireTransition();
					viewContr.getCurrentNet().testDeadlock();
					viewContr.getCurrentNet().testFinished();
					viewContr.getToolBoard().updateList();
					e.consume();
				}

				else if (paneContr.isEditMode()) {
					// Wenn genau ein Knoten zur Zeit ausgewählt ist ...
					if (viewContr.currentlySelectedNodesCount() == 1 && viewContr.currentlySelectedArrowsCount() == 0) 
					{
						NodeGraphic targetNode = viewContr.getCurrentPane().getCurrentlySelectedNodes().get(0);
						// ... füge einen neuen Bogen zum angeklickten Knoten ein ...
						if (!(targetNode.getClass() == e.getSource().getClass())) {
							viewContr.addNewArc(targetNode, (NodeGraphic) e.getSource());
							viewContr.getCurrentPane().clearCurrentlySelected();
							viewContr.getCurrentPane().addToCurrentlySelected((NetElementGraphic) e.getSource());
						}
						// ... solange er nicht die gleiche Klasse hat
						else {
							viewContr.getCurrentPane().clearCurrentlySelected();
						}
					}

					// Wenn nicht genau ein Knoten ausgewählt ist
					else {
						viewContr.getCurrentPane().clearCurrentlySelected();
						viewContr.getCurrentPane().addToCurrentlySelected((NetElementGraphic) e.getSource());
					}
				}
			}
			if (e.getButton() == MouseButton.SECONDARY && paneContr.isEditMode()) {
				rightClickMenu.show((Node) e.getSource(), e.getScreenX(), e.getScreenY());
				renameNode.setOnAction(f ->
				{
					nameTagHandler.handle(e);
				});
			}
			e.consume();

		}
	};	
	/**
	 * Setzt die eventStart- und nodeStart-Attribute.<br>
	 * Wenn die Maus auf einem Knoten gedrückt wird, der zur Zeit ausgewählt ist, werden die Positionen
	 * aller ausgewählten Knoten in nodeStart-Maps registriert.<br>
	 * Setzt den Cursor auf Cursor.MOVE und konsumiert das Event.
	 */
	private EventHandler<MouseEvent> MousePressedHandler = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent e) {
			eventStartX = e.getX();
			eventStartY = e.getY();
			if ((viewContr.getCurrentPane().getCurrentlySelectedNodes().contains(e.getSource()))) {
				for (NodeGraphic temp: viewContr.getCurrentPane().getCurrentlySelectedNodes()) {
					nodeStartXMap.put(temp, temp.getCenterX());
					nodeStartYMap.put(temp, temp.getCenterY());
				}
			}
			else {		
				nodeStartX = ((NodeGraphic) e.getSource()).getCenterX();
				nodeStartY = ((NodeGraphic) e.getSource()).getCenterY();
			}
			viewContr.setCursor(Cursor.MOVE);
			e.consume();
		}
	};
	/**
	 * Aktualisiert die offset-Attribute.<br>
	 * Wenn eine Gruppe von Knoten bewegt wird, werden für alle Knoten in der nodeStart-Map die
	 * center-Attribute aktualisiert (d.h. Anfangswert + offset).<br>
	 * Wird ein einzelner Knoten bewegt, werden seine center-Attribute entsprechend aktualisiert.<br>
	 * Konsumiert das Event.
	 */
	private EventHandler<MouseEvent> MouseDraggedHandler = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent e) {
			offsetX = e.getX() - eventStartX;
			offsetY = e.getY() - eventStartY;

			if (viewContr.getCurrentPane().getCurrentlySelectedNodes().contains(e.getSource())) {
				for (NodeGraphic temp: viewContr.getCurrentPane().getCurrentlySelectedNodes()) {
					temp.setCenterX(nodeStartXMap.get(temp) + offsetX);
					temp.setCenterY(nodeStartYMap.get(temp) + offsetY);
				}
			}
			else {

				((NodeGraphic) e.getSource()).setCenterX(nodeStartX + offsetX);
				((NodeGraphic) e.getSource()).setCenterY(nodeStartY + offsetY);
			}
			e.consume();
		}
	};
	/**
	 * Veranlasst für alle bewegten Knoten das Setzen der in der Modellentsprechung gespeicherten Koordinaten.<br>
	 * Setzt den Cursor auf Cursor.HAND und konsumiert das Event.
	 */
	private EventHandler<MouseEvent> DragReleasedHandler = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent e) {
			viewContr.setCursor(Cursor.HAND);
			if (viewContr.getCurrentPane().getCurrentlySelectedNodes().contains(e.getSource())) {
				for (NodeGraphic temp: viewContr.getCurrentPane().getCurrentlySelectedNodes()) {
					temp.setModelCoordinates();
				}
				nodeStartXMap.clear();
				nodeStartYMap.clear();
			}
			else
				((NodeGraphic) e.getSource()).setModelCoordinates();
			e.consume();
		}
	};
	/**
	 * Wenn die linke Maustaste nicht gedrückt ist, setze den Cursor auf Cursor.HAND und
	 * unregistriere ggf. den Knoten-Pinsel.
	 */
	private EventHandler<MouseEvent> MouseEnteredHandler = new EventHandler<MouseEvent>() {
		@Override
		public void handle (MouseEvent e) {
			if (!e.isPrimaryButtonDown()) {
				viewContr.setCursor(Cursor.HAND);
				paneContr.unregisterBrush();
			}
		}
	};
	/**
	 * Wenn die linke Maustauste nicht gedrückt ist, setze den Cursor auf Cursor.DEFAULT und registriere ggf.
	 * den Knoten-Pinsel wieder.
	 */
	private EventHandler<MouseEvent> MouseExitedHandler = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent e) {
			if (!e.isPrimaryButtonDown()) {
				viewContr.setCursor(Cursor.DEFAULT);
				paneContr.registerBrush(e);
			}
		}
	};
	/**
	 * Zeigt im Edit-Modus bei Klick ein Eingabefeld an der Position des Events ein (i.d.R. am nameTag),<br>
	 * registriert auf diesem und auf dem Pane EventHandler die dafür sorgen, dass:<br>
	 * - ENTER die Eingabe erfolgreich abschließt, ESCAPE sie abbricht<br>
	 * - das Feld wieder ausgeblendet wird, wenn ein weiteres aufgemacht oder irgendwoanders hingeklickt wird<br>
	 * - der KeyEventHandler inaktiv ist, solange ein Eingabefeld offen ist<br>
	 * Bei MOUSE_ENTERED und MOUSE_EXITED wird ggf. der Knoten-Pinsel
	 * registriert oder abgemeldet und der Cursor gesetzt.<br>
	 * Die handle-Methode ist so eingerichtet, dass sie für den "rename"-Eintrag des Kontextmenüs im OnClickHandler
	 * wiederverwendet werden kann und konsumiert das Event.
	 */
	private final EventHandler<MouseEvent> nameTagHandler = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent e) {
			if (paneContr.isEditMode()) {
				if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {
					//if-else um die handle-Methode im OnClickHandler recyclen zu können
					if (e.getSource().getClass() == Text.class) {
						inputField.setText(((Text) e.getSource()).getText());
						inputField.setTranslateX(((Text) e.getSource()).getX());
						inputField.setTranslateY(((Text) e.getSource()).getY() - ((Text) e.getSource()).getBaselineOffset());
					}
					else {
						inputField.setTranslateX(e.getX());
						inputField.setTranslateY(e.getY());
					}
					//deaktiviere Keyboard-Navigation solange Eingabefeld geöffnet ist
					viewContr.deactivateKeyHandler();

					//schließe das Eingabefeld bei Enter (dann aktualisiere den Namen) oder Escape,
					//aktiviere Keyboard-Navigation
					inputField.setOnKeyPressed(key -> {
						if(key.getCode() == KeyCode.ENTER) {
							if (e.getSource().getClass() == Text.class) {
								((Text) e.getSource()).setText(inputField.getText());
							}
							else
								((NodeGraphic) e.getSource()).nameTag().setText(inputField.getText());
							viewContr.getCurrentPane().getContent().getChildren().remove(inputField);
							viewContr.activateKeyHandler();
						}
						if(key.getCode() == KeyCode.ESCAPE) {
							viewContr.getCurrentPane().getContent().getChildren().remove(inputField);
							viewContr.activateKeyHandler();
						}
					});
					//wird ein zweites Feld aufgemacht wird der Fokus übertragen.
					//dann wird das alte geschlossen
					inputField.focusedProperty().addListener(focus -> {
						if(!inputField.isFocused()) {
							viewContr.getCurrentPane().getContent().getChildren().remove(inputField);
							viewContr.activateKeyHandler();
						}
					});

					//wird irgendwoanders auf dem Pane geklickt, schließe das Eingabefeld
					viewContr.getCurrentPane().setOnMousePressed(new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent mouseEvent) {
							viewContr.getCurrentPane().getContent().getChildren().remove(inputField);
							viewContr.activateKeyHandler();
							viewContr.getCurrentPane().removeEventHandler(MouseEvent.MOUSE_PRESSED, this);
						}
					});
					try {
						viewContr.getCurrentPane().getContent().getChildren().add(inputField);
					}
					catch (IllegalArgumentException excep) {
					}
					inputField.requestFocus();
				}
				else if (e.getEventType() == MouseEvent.MOUSE_ENTERED) {
					paneContr.unregisterBrush();
					viewContr.setCursor(Cursor.TEXT);
				}
				else if (e.getEventType() == MouseEvent.MOUSE_EXITED) {
					paneContr.registerBrush(e);
				}
			}
			e.consume();
		}
	};

	/**
	 * Wählt bei Klick genau den angeklickten graphischen Bogen aus, wenn er nicht ausgewählt ist - in dem Fall hebt er die Auswahl auf.<br>
	 * Setzt bei MOUSE_ENTERED und MOUSE_EXITED den Cursor und registriert oder meldet ggf. den Knoten-Pinsel ab.<br>
	 * Konsumiert das Event.
	 */
	private final EventHandler<MouseEvent> arrowHandler = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent e) {
			if (paneContr.isEditMode()) {
				if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {
					if (viewContr.getCurrentPane().getCurrentlySelectedArrows().contains(e.getSource())) {
						viewContr.getCurrentPane().clearCurrentlySelected();
					}
					else {
						viewContr.getCurrentPane().clearCurrentlySelected();
						viewContr.getCurrentPane().addToCurrentlySelected((NetElementGraphic) e.getSource());
					}
				}
				if (e.getEventType() == MouseEvent.MOUSE_ENTERED) {
					viewContr.setCursor(Cursor.HAND);
					paneContr.unregisterBrush();
				}
				if (e.getEventType() == MouseEvent.MOUSE_EXITED) {
					viewContr.setCursor(Cursor.DEFAULT);
					paneContr.registerBrush(e);
				}
			}
			e.consume();
		}
	};

	/**
	 * Gibt eine Referenz auf den allgemeinen EventHandler für Knoten.
	 * @return
	 * 		Referenz auf generalEventHandler
	 */
	public EventHandler<MouseEvent> getGeneralEventHandler() {
		return generalEventHandler;
	}
	/**
	 * Gibt eine Referenz auf den EventHandler für "Namensschilder".
	 * @return
	 * 		Referenz auf nameTagHandler
	 */
	public EventHandler<MouseEvent> getNameTagHandler() {
		return nameTagHandler;
	}
	/**
	 * Gibt eine Referenz auf den EventHandler für graphische Bögen.
	 * @return
	 * 		Referenz auf arrowHandler
	 */
	public EventHandler<MouseEvent> getArrowHandler() {
		return arrowHandler;
	}
}