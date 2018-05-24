package view;

import javafx.beans.property.DoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import model.PetriNode;

/**
 * Interface für die graphische Darstellung von Petrinetz-Knoten. Im Wesentlichen für Typgarantien im FX-EventHandling.
 */
public interface NodeGraphic {

	//**** PROPERTIES / GETTER & SETTER ****
	/**
	 * @return
	 * Gibt die x-Koordinate des Mittelpunktes des graphischen Knoten.
	 */
	public double getCenterX();
	/**
	 * @return
	 * Gibt die y-Koordinate des Mittelpunktes des graphischen Knoten.
	 */
	public double getCenterY();
	/**
	 * Setzt die x-Koordinate des Mittelpunktes des graphischen Knoten.
	 * @param in
	 * 		der numerische Wert auf den die x-Koordinate gesetzt werden soll.
	 */
	public void setCenterX(double in);
	/**
	 * Setzt die y-Koordinate des Mittelpunktes des graphischen Knoten.
	 * @param in
	 * 		der numerische Wert auf den die y-Koordinate gesetzt werden soll.
	 */
	public void setCenterY(double in);

	/**
	 * @return
	 * Gibt die x-Koordinate des Mittelpunktes des Knoten als Property aus.
	 */
	public DoubleProperty centerXProperty();

	/**
	 * @return
	 * Gibt die y-Koordinate des Mittelpunktes des Knoten als Property aus.
	 */
	public DoubleProperty centerYProperty();
	

	
	//**** METHODS ****
	/**
	 * Setzt die Koordinaten der Modellentsprechung des graphischen Knoten gemäß seiner aktuellen Position in der Darstellung.
	 */
	public void setModelCoordinates();
	/**
	 * @return
	 * Gibt die Modellentsprechung des graphischen Knoten.
	 */
	public PetriNode getModel();
	
	/**
	 * Gibt dem Knoten die Färbung die darauf hindeutet, dass es zur Zeit durch den User zur Bearbeitung ausgewählt ist.
	 */
	public void fillStandardColor();
	/**
	 * Gibt dem Knoten die Färbung die darauf hindeutet, dass es nicht durch den User zur Bearbeitung ausgewählt ist.
	 */
	public void fillSelectionColor();
	/**
	 * Setzt die Färbung des Randes eines Knoten; notwendig bei Stellen um Anfang und Ende des Netzes zu markieren.
	 * @param c
	 */
	public void setStroke(Color c);
	/**
	 * @return
	 * Gibt das Text-Feld in dem der Name des Knoten angezeigt wird.
	 */
	public Text nameTag();



}
