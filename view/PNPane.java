package view;

import java.util.ArrayList;
import java.util.function.Predicate;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

/**
 * Implementiert eine Leinwand mit den Referenzen auf die auf ihr darzustellenden Netzelementen
 * und Methoden zum Registrieren und Entfernen und bietet Listen der"zum Bearbeiten ausgewählten" Netzelementen an, sowie
 * Methoden zum Hinzufügen zu dieser Auswahl.
 */
public class PNPane extends Pane {

	//**** FIELDS ****
	/**
	 * Bewahrt die Information darüber in welchem Modus das Pane ist für Tabwechsel.
	 */
	private boolean inEditMode = false;
	/**
	 * Sammmelt alle graphisch dargestellten Objekte des Netzes.
	 */
	private Group content = new Group();
	/**
	 * Liste der zur Bearbeitung ausgewählten graphischen Knoten (d.h. Stellen und Transitionen)
	 */
	private ArrayList<NodeGraphic> currentlySelectedNodes = new ArrayList<NodeGraphic>();
	/**
	 * Liste der zur Bearbeitung ausgewählten graphischen Bögen.
	 */
	private ArrayList<ArrowGraphic> currentlySelectedArrows = new ArrayList<ArrowGraphic>();

	/**
	 * Dieses Prädikat wird verwendet in der Implementierung des Löschens eines Knotens um alle Bögen die diesen Knoten
	 * als Ausgangs- oder Endpunkt haben ebenfalls zu löschen.
	 * @param in
	 * 		Der Knoten für den überprüft werden soll, ob der Bogen ihn berührt.
	 * @return
	 * 		<b>true</b>: wenn der Knoten Start- oder Endpunkt des Bogens ist<br>
	 * 		<b>false</b>: sonst
	 */
	private static Predicate<Node> touchesNode(NodeGraphic in) {
		return p -> (((ArrowGraphic) p).from() == in || ((ArrowGraphic) p).to() == in);
	}
	/**
	 * Dieses Prädikat gibt für einen beliebigen Node an ob es sich um einen graphischen Pfeil handelt.<br>
	 * Es entspringt dem Experimentieren mit Java-8-Features und einer davon ausgehenden, 
	 * nicht weiter ausgeführten Überlegung Typsicherungscode in diesem Programm 
	 * durch Verwendung funktionaler Interfaces zu verschönern.
	 * @return
	 * 		<b>true</b>: wenn es sich bei dem Knoten um eine Instanz von ArrowGraphic handelt<br>
	 * 		<b>false</b>: sonst
	 */
	private static Predicate<Node> isArrow() {
		return p -> p.getClass() == ArrowGraphic.class;
	}

	//**** CONSTRUCTOR ****
	/**
	 * Setzt den Hintergrund des Panes auf weiß und registriert die content-Group.
	 */
	public PNPane() {
		this.setStyle("-fx-background-color: white;");
		this.getChildren().add(content);
	}

	//**** METHODS ****
	//** REGISTRATION **
	/**
	 * Registriert eine graphische Repräsentation einer Stelle.
	 * @param in
	 * 		Referenz auf die zuzufügende PlaceGraphic.
	 */
	public void registerCircle(PlaceGraphic in) {
		this.content.getChildren().add(in);
	}
	/**
	 * Registriert eine graphische Repräsentation eines Bogens.
	 * @param in
	 * 		Referenz auf die zuzufügende ArrowGraphic.
	 */
	public void registerArrow(ArrowGraphic in) {
		this.content.getChildren().add(in);
		in.toBack();
	}
	/**
	 * Registriert eine graphische Repräsentation einer Transition.
	 * @param in
	 * 		Referenz auf die zuzufügende TransitionGraphic.
	 */
	public void registerTransition(TransitionGraphic in) {
		this.content.getChildren().add(in);
	}

	/**
	 * Färbt ein zur Bearbeitung auszuwählendes Netzelement entsprechend ein und fügt es der entsprechenden Liste zu.<br>
	 * Wenn es sich um einen Pfeil handelt wird überprüft ob es sich um einen Doppelpfeil handelt
	 * und ggf. beide Pfeile ausgewählt.
	 * @param in
	 * 		Die zur Bearbeitung auszuwählende NetElementGraphic.
	 */
	public void addToCurrentlySelected(NetElementGraphic in) {
		in.fillSelectionColor();
		if (in.getClass() == PlaceGraphic.class || in.getClass() == TransitionGraphic.class)
			this.currentlySelectedNodes.add((NodeGraphic) in);
		if (in.getClass() == ArrowGraphic.class) {
			this.currentlySelectedArrows.add((ArrowGraphic) in);
			for (Node temp: this.content.getChildren()) {
				if (temp.getClass() == ArrowGraphic.class) {
					if (((ArrowGraphic) temp).getModel().from == ((ArrowGraphic) in).getModel().to
							&& ((ArrowGraphic) temp).getModel().to == ((ArrowGraphic) in).getModel().from) {
						this.currentlySelectedArrows.add((ArrowGraphic) temp);
						((ArrowGraphic) temp).fillSelectionColor();
					}
				}
			}
		}
	}
	/**
	 * Färbe alle bisher selektierten Knoten wieder so, dass sie als unausgewählt erkennbar sind,
	 * dann leere die entsprechenden Listen.
	 */
	public void clearCurrentlySelected() {
		this.currentlySelectedNodes.forEach(
				t -> t.fillStandardColor() );
		this.currentlySelectedNodes.clear();
		
		this.currentlySelectedArrows.forEach(
				t -> t.fillStandardColor() );
		this.currentlySelectedArrows.clear();
	}

	/**
	 * Entfernt einen graphischen Bogen von der Leinwand.
	 * @param in
	 * 		zu entferndender Bogen.
	 */
	public void deleteArrow(ArrowGraphic in) {
		this.content.getChildren().remove((Node) in);		
	}
	/**
	 * Entfernt einen graphischen Knoten und alle mit ihm verbundenen Bögen von der Leinwand.
	 * @param in
	 * 		zu entferndender Knoten.
	 */
	public void deleteNode(NodeGraphic in) {
		this.content.getChildren().removeIf(isArrow().and(touchesNode(in)));
		this.content.getChildren().remove(in.nameTag());
		this.content.getChildren().remove((Node) in);
	}

	//**** GETTER AND SETTER ****
	/**
	 * @return
	 * 		Group-Objekt das alle dargestellten Netzelemente enthält.
	 */
	public Group getContent() {
		return content;
	}	
	/**
	 * Wird im Wesentlichen zur Verschönerung des Codes und Verbesserung des Komforts
	 * in der Implementierung des NodeEventsController angeboten.
	 * @return
	 * 		ArrayList-Darstellung der dargestellten Knoten, ohne die assoziierten Text-Objekte ("Name-Tags")
	 */
	public ArrayList<NetElementGraphic> getContentArray() {
		ArrayList<NetElementGraphic> temp = new ArrayList<NetElementGraphic>();
		this.content.getChildren().forEach(node ->
		{ if (node.getClass() != Text.class)
			temp.add((NetElementGraphic) node);
		});
		return temp;
	}
	/**
	 * @return
	 * ArrayList der zur Bearbeitung ausgewählten graphischen Knoten (d.h. Stellen und Transitionen)
	 */
	public ArrayList<NodeGraphic> getCurrentlySelectedNodes() {
		return this.currentlySelectedNodes;
	}
	/**
	 * @return
	 * ArrayList der zur Bearbeitung ausgewählten graphischen Bögen
	 */
	public ArrayList<ArrowGraphic> getCurrentlySelectedArrows() {
		return this.currentlySelectedArrows;
	}
	/**
	 * Gibt an ob das Pane im Editier-Modus ist.<br>
	 * Wird nur für Tabwechsel verwendet.
	 * @return
	 * 		<b>true</b>: wenn das Tab im Editier-Modus betrachtet wird<br>
	 * 		<b>false</b>: sonst, insbesondere wenn es im Simulations-Modus betrachtet wird
	 */
	public boolean isInEditMode() {
		return inEditMode;
	}
	/**
	 * Setzt das inEditMode-flag.
	 * @param inEditMode
	 * 		boolean-Wert für das inEditModeFlag
	 */
	public void setInEditMode(boolean inEditMode) {
		this.inEditMode = inEditMode;
	}

}
