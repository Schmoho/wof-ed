package controller;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import view.NetElementGraphic;
import view.PNPane;

/**
 * Implementiert einen EventHandler der am PetriNetPane registiert wird
 * und für den Simulations-Modus die Navigation durch Panning ermöglicht, sowie für den
 * Editier-Modus die "Pinsel"-Funktionalität, das Erzeugen neuer Knoten und 
 * die Auswahl durch ein ziehbares Auswahlviereck.
 */
class PaneEventsController {

	//**** FIELDS ****
	/**
	 * Referenz auf den ViewController
	 */
	private ViewController viewContr;

	/**
	 * "Pinsel" als visuelles Feedback, dass der User per Linksklick Transitionen setzen kann.
	 */
	private Rectangle transBrush = new Rectangle(10, 10, Color.ORANGE);
	/**
	 * "Pinsel" als visuelles Feedback, dass der User per Linksklick Stellen setzen kann.
	 */
	private Circle placeBrush = new Circle(10, Color.BLUE);
	/**
	 * "Behälter"variable, welche die Implementierung der (un-)register-Methoden für die Pinsel erleichtert.
	 */
	private Shape brush;
	/**
	 * flag, welches dem gegenseitigen Ausschluß von Stellen- und Transitions-Pinsel dient.
	 */
	private boolean placeBrushSelected = false;
	/**
	 * flag, welches dem gegenseitigen Ausschluß von Stellen- und Transitions-Pinsel dient.
	 */
	private boolean transBrushSelected = false;
	/**
	 * flag, welches den Editier-Modus vom Simulations-Modus unterscheidet.
	 */
	private boolean editMode = false;
	/**
	 * Das Viereck, welches für die Implementierung der Mehrfachauswahl verwendet wird.
	 */
	private Rectangle selectRect = new Rectangle(0, 0, new Color(0, 0, 0, 0.07));

	//**** CONSTRUCTORS ****
	/**
	 * Initialisiert die Referenz auf den ViewController.
	 * @param v
	 * 		Referenz auf den ViewController
	 */
	public PaneEventsController(ViewController v) {
		this.viewContr = v;
	}

	//**** METHODS ****
	/**
	 * Setzt das Editier-Modus flag entsprechend, falsifiziert beide Pinsel-Flags und
	 * versucht den Übergang zu glätten, falls der Editier-Modus während eines Drags per Hotkey ausgeschaltet wurde.
	 * @param b
	 * 		Parameter sei true für Editier-Modus, false für Simulations-Modus
	 */
	public void setEditMode(boolean b) {
		editMode = b;
		transBrushSelected = false;
		placeBrushSelected = false;
		if (b == false) {
			unregisterBrush();
			selectRect.setVisible(false);
			dragStartX = dragStartX + offsetX;
			dragStartY = dragStartY + offsetY;
		}
	}

	/**
	 * Sorgt für die graphische Darstellung des Stellen-Pinsels im Editier-Modus.<br>
	 * Wenn im Editier-Modus, wählt die Methode entweder den Stellen-Pinsel aus,
	 * indem die die flags entsprechend gesetzt werden und setzt die brush Referenz oder 
	 * - wenn der Stellen-Pinsel ausgewählt ist - falsifiziert das flag und nullt die brush-Referenz.
	 */
	public void togglePlaceBrush() {
		if (editMode && (!placeBrushSelected)) {
			transBrushSelected = false;
			placeBrushSelected = true;
			brush = placeBrush;
		}
		else if (editMode && placeBrushSelected) {
			placeBrushSelected = false;
			brush = null;
		}
	}
	/**
	 * Sorgt für die graphische Darstellung des Transitions-Pinsels im Editier-Modus.<br>
	 * Wenn im Editier-Modus, wählt die Methode entweder den Transitions-Pinsel aus,
	 * indem die die flags entsprechend gesetzt werden und setzt die brush Referenz oder 
	 * - wenn der Transitions-Pinsel ausgewählt ist - falsifiziert das flag und nullt die brush-Referenz.
	 */
	public void toggleTransitionBrush() {
		if (editMode && (!transBrushSelected)) {
			placeBrushSelected = false;
			transBrushSelected = true;
			brush = transBrush;		
		}
		else if (editMode && transBrushSelected) {
			transBrushSelected = false;
			brush = null;
		}
	}

	/**
	 * Meldet das Auswahlviereck beim Pane ab.<br>
	 * Wird beim Wechseln der Panes beim Tabwechsel verwendet, um das Auswahlviereck
	 * konzeptionell als Teil des PaneEventController zu belassen.
	 */
	public void unregisterSelectRect() {
		if(viewContr.getCurrentPane() != null && viewContr.getCurrentPane().getChildren().contains(selectRect))
			viewContr.getCurrentPane().getChildren().remove(selectRect);
	}
	/**
	 * Meldet das Auswahlviereck beim Pane an.<br>
	 * Wird beim Wechseln der Panes beim Tabwechsel verwendet, um das Auswahlviereck
	 * konzeptionell als Teil des PaneEventController zu belassen.
	 * @param p
	 * 		Pane an dem das Auswahlviereck angemeldet werden soll.
	 */
	public void registerSelectRect(PNPane p) {
		viewContr.getCurrentPane().getChildren().add(selectRect);
	}

	/**
	 * Meldet die Pinsel-Behältervariable beim aktuellen Pane ab.
	 */
	public void unregisterBrush() {
		if (viewContr.getCurrentPane().getChildren().contains(brush))
			viewContr.getCurrentPane().getChildren().remove(brush);
	}
	/**
	 * Meldet die Pinsel-Behältervariable beim aktuellen Pane an
	 * und setzt den Pinsel auf die Position des Cursors. Dafür wird ein MouseEvent entgegengenommen.
	 * @param e
	 * 		MouseEvent anhanddessen die Position des Pinsels festgelegt wird
	 */
	public void registerBrush(MouseEvent e) {
		if (transBrushSelected || placeBrushSelected) {
			if (!viewContr.getCurrentPane().getChildren().contains(brush))
				viewContr.getCurrentPane().getChildren().add(brush);
			brush.toBack();
		}
		if (transBrushSelected) {
			transBrush.setX(e.getX() - 5);
			transBrush.setY(e.getY() - 5);
		}
		if (placeBrushSelected) {
			placeBrush.setCenterX(e.getX());
			placeBrush.setCenterY(e.getY());
		}
	}

	//**** EVENT HANDLERS ****
	/**
	 * Registriert die x-Koordinate der Entfernung des Cursors vom Ausgangspunkt des Drags
	 * bei der Implementierung des Auswahlvierecks.
	 */
	private double offsetX;
	/**
	 * Registriert die y-Koordinate der Entfernung des Cursors vom Ausgangspunkt des Drags
	 * bei der Implementierung des Auswahlvierecks.
	 */
	private double offsetY;
	/**
	 * Registriert die x-Koordinate des Ausgangspunktes des Drags
	 * bei der Implementierung des Auswahlvierecks.
	 */
	private double dragStartX;
	/**
	 * Registriert die y-Koordinate des Ausgangspunktes des Drags
	 * bei der Implementierung des Auswahlvierecks.
	 */
	private double dragStartY;

	/**
	 * Sorgt bei Linksklick und ausgewähltem Pinsel im Editier-Modus für das Neueinfügen von
	 * Stellen oder Transitionen.<br>
	 * Implementiert Zeichnen und Auswahl durch ein Viereck durch Drag-Bewegungen.
	 */
	private EventHandler<MouseEvent> editEventHandler = new EventHandler<MouseEvent>() {
		// Unterscheidet Klicks von Drags
		private boolean dragging;

		public void handle(MouseEvent e) {
			EventType<? extends MouseEvent> typeTemp = e.getEventType();
			if (typeTemp == MouseEvent.MOUSE_PRESSED) {
				// initialisiert die für den Drag-Vorgang relevanten Variablen
				dragging = false;			
				dragStartX = (int) e.getX();
				dragStartY = (int) e.getY();
				selectRect.setX(e.getX());
				selectRect.setY(e.getY());
				selectRect.setWidth(0);
				selectRect.setHeight(0);
				selectRect.setTranslateX(0);
				selectRect.setTranslateY(0);
				selectRect.setVisible(true);
				e.consume();
			}
			else if (typeTemp == MouseEvent.DRAG_DETECTED) {
				dragging = true;
				if (viewContr.getCurrentPane().getChildren().contains(brush))
					viewContr.getCurrentPane().getChildren().remove(brush);
				viewContr.setCursor(Cursor.CROSSHAIR);
			}
			else if (typeTemp == MouseEvent.MOUSE_DRAGGED) {
				// aktualisiere die offset-Variablen
				// Fallunterscheidung um Ziehen des Vierecks in alle Richtungen zu ermöglichen
				offsetX = e.getX() - dragStartX;
				offsetY = e.getY() - dragStartY;
				if (offsetX >= 0) {
					selectRect.setWidth(offsetX);
					selectRect.setTranslateX(0);
				}
				else {
					selectRect.setWidth(-offsetX);
					selectRect.setTranslateX(offsetX);
				}
				if (offsetY >= 0) {
					selectRect.setHeight(offsetY);
					selectRect.setTranslateY(0);
				}
				else {
					selectRect.setHeight(-offsetY);
					selectRect.setTranslateY(offsetY);
				}
				e.consume();
			}
			else if (typeTemp == MouseEvent.MOUSE_RELEASED) {
				// am Ende eines Drags, richte die Cursor wieder her, besorge die Auswahl
				if(dragging) {
					selectRect.setVisible(false);
					viewContr.setCursor(Cursor.DEFAULT);
					registerBrush(e);

					viewContr.getCurrentPane().clearCurrentlySelected();
					selectRect.setX(selectRect.getX() + selectRect.getTranslateX() - viewContr.getContentTranslateX());
					selectRect.setY(selectRect.getY() + selectRect.getTranslateY() - viewContr.getContentTranslateY());
					for (NetElementGraphic temp: viewContr.getCurrentPane().getContentArray()) {
						// if (Element ist im sichtbaren, also translatierten, Auswahlviereck)
						if (selectRect.intersects(temp.getBoundsInLocal()))
						{
							viewContr.getCurrentPane().addToCurrentlySelected((NetElementGraphic) temp);
						}
					}
				}
			}
			else if (typeTemp == MouseEvent.MOUSE_CLICKED) {
				if (!dragging) {
					if (viewContr.currentlySelectedArrowsCount() + viewContr.currentlySelectedNodesCount() != 0) {
						viewContr.getCurrentPane().clearCurrentlySelected();
					}
					else {
						if(placeBrushSelected) {
							viewContr.addNewPlace(
									e.getX() - viewContr.getContentTranslateX(),
									e.getY() - viewContr.getContentTranslateY());
						}
						if(transBrushSelected) {
							viewContr.addNewTransition(
									e.getX() - viewContr.getContentTranslateX(),
									e.getY() - viewContr.getContentTranslateY());
						}
					}
				}
				e.consume();
			}
			else if (typeTemp == MouseEvent.MOUSE_ENTERED) {
				if (!dragging)
					registerBrush(e);
			}
			else if (typeTemp == MouseEvent.MOUSE_MOVED) {
				if (transBrushSelected) {
					transBrush.setX(e.getX() - 5);
					transBrush.setY(e.getY() - 5);
				}
				if (placeBrushSelected) {
					placeBrush.setCenterX(e.getX());
					placeBrush.setCenterY(e.getY());
				}
			}
			else if (typeTemp == MouseEvent.MOUSE_EXITED) {
				if (!dragging) {
					if (placeBrushSelected || transBrushSelected) {
						viewContr.setCursor(Cursor.DEFAULT);
						unregisterBrush();
					}
				}
			}
		}
	};
	/**
	 * Erlaubt im Simulations-Modus Navigation durch Panning, indem alle sichtbaren Netzelemente relativ zum Pane
	 * translatiert werden.
	 */
	private EventHandler<MouseEvent> simulationEventHandler = new EventHandler<MouseEvent>() {
		private int groupTranslateX, groupTranslateY;

		public void handle(MouseEvent e) {
			EventType<? extends MouseEvent> typeTemp = e.getEventType();
			if (typeTemp == MouseEvent.MOUSE_PRESSED) {
				dragStartX = (int) e.getX();
				dragStartY = (int) e.getY();
				groupTranslateX = (int) viewContr.getContentTranslateX();
				groupTranslateY = (int) viewContr.getContentTranslateY();
				viewContr.setCursor(Cursor.CLOSED_HAND);
				e.consume();
			}
			if (typeTemp == MouseEvent.MOUSE_DRAGGED) {
				offsetX = e.getX() - dragStartX;
				offsetY = e.getY() - dragStartY;
				viewContr.getCurrentPane().getContent().setTranslateX(groupTranslateX + offsetX);
				viewContr.getCurrentPane().getContent().setTranslateY(groupTranslateY + offsetY);
				e.consume();
			}
			if (typeTemp == MouseEvent.MOUSE_RELEASED) {
				viewContr.setCursor(Cursor.DEFAULT);
			}
		}
	};

	//**** GETTER AND SETTER ****
	/**
	 * Gibt den EventHandler für das Pane für den Editier-Modus
	 * @return
	 * 		gibt editEventHandler
	 */
	public EventHandler<MouseEvent> getEditEventHandler() {
		return editEventHandler;
	}
	/**
	 * Gibt den EventHandler für das Pane für den Simulations-Modus
	 * @return
	 * 		gibt simulationEventHandler
	 */
	public EventHandler<MouseEvent> getSimulationEventHandler() {
		return simulationEventHandler;
	}
	/**
	 * Teilt mit, ob Editier- oder Simulations-Modus beim PaneEventController aktiviert ist
	 * @return
	 * 		<b>true</b>: wenn Editier-Modus aktiv ist<br>
	 * 		<b>false</b>: sonst, d.h. insbesondere wenn der Simulations-Modus aktiviert ist
	 */
	public boolean isEditMode() {
		return editMode;
	}
}
