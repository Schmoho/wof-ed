package view;

import model.PetriNode;
import model.Place;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

/**
 * Implementiert die graphische Repräsenation einer Stelle in einem Petrinetz.
 */
public class PlaceGraphic extends NetElementGraphic implements NodeGraphic {

	//**** FIELDS ****
	/**
	 * die Farbe für eine unausgewählte Stelle<br>
	 * (desaturiertes CORNFLOWERBLUE)
	 */
	public static Color STD_COLOR = Color.hsb(219, 0.38, 0.93);
	/**
	 * die Farbe für den Rand einer Stelle die weder Start- noch Endstelle ist<br>
	 * (etwas saturierteres und dunkleres CORNFLOWERBLUE)
	 */
	public static Color STROKE_COLOR = Color.hsb(219, 0.60, 0.7);
	/**
	 * die Farbe für den Rand einer Start-Stelle
	 */
	public static Color START_COLOR = Color.LAWNGREEN;
	/**
	 * die Farbe für den Rand einer End-Stelle
	 */
	public static Color END_COLOR = Color.ORANGERED;
	/**
	 * die Farbe für eine zur Bearbeitung ausgewählte Stelle
	 */
	public static Color SELECTION_COLOR = Color.AQUA;
	/**
	 * die Farbe für eine Marke
	 */
	public static Color MARK_COLOR = Color.BLACK;
	/**
	 * globales Radius-Attribut um globale Größenveränderungen zu vereinfachen
	 */
	public static DoubleProperty RADIUS = new SimpleDoubleProperty(20);

	/**
	 * Referenz auf die Modellentsprechung dieser Stelle.
	 */
	private Place modelNode;
	/**
	 * Der Kreis als der die Stelle dargestellt wird.
	 */
	private Circle circle;
	/**
	 * Das "Namensschild" der Stelle.
	 */
	private Text nameTag;
	/**
	 * Der Kreis als der eine eventuelle Markierung dargestellt wird.
	 */
	private Circle markGraphic;

	//**** CONSTRUCTORS ****
	/**
	 * Nimmt eine x- und eine y-Koordinate für die neue Stelle sowie die Modellentsprechung der neuen graphischen 
	 * Stelle. Initialisiert das Namensschild und assoziiert das Namensattribut der Modellentsprechung
	 * der Stelle damit, sowie die Markierungsgraphik mit dem mark-Attribut der Modellentsprechung der Stelle.
	 * @param x
	 * 		x-Koordinate der neuen Stelle.
	 * @param y
	 * 		y-Koordinate der neuen Stelle.
	 * @param in
	 * 		Modellentsprechung der neuen Stelle.
	 */
	public PlaceGraphic(int x, int y, Place in) {
		circle = new Circle(x, y, RADIUS.get(), STD_COLOR);
		circle.radiusProperty().bind(RADIUS);
		circle.setStroke(STROKE_COLOR);
		circle.setStrokeWidth(3);
		modelNode = in;

		nameTag = new Text(in.getName());
		nameTag.xProperty().bind(circle.centerXProperty().add(circle.radiusProperty()));
		nameTag.yProperty().bind(circle.centerYProperty().add(circle.radiusProperty()));
		nameTag.textProperty().addListener(e -> {
			modelNode.setName(nameTag.getText());
		});

		markGraphic = new Circle();
		markGraphic.setFill(MARK_COLOR);
		markGraphic.radiusProperty().bind(circle.radiusProperty().divide(2));
		markGraphic.centerXProperty().bind(circle.centerXProperty());
		markGraphic.centerYProperty().bind(circle.centerYProperty());
		getChildren().add(circle);
		getChildren().add(nameTag);
		modelNode.markProperty().addListener(e ->
		{
			if (modelNode.isMarked()) {
				this.getChildren().add(markGraphic);
			}
			if (!modelNode.isMarked()) {
				this.getChildren().remove(markGraphic);
			}
		});

	}

	//**** METHODS ****
	/**
	 * Gibt dem Element die Färbung die darauf hindeutet, dass es nicht durch den User zur Bearbeitung ausgewählt ist.
	 */
	public void fillStandardColor() {
		circle.setFill(STD_COLOR);
	}
	/**
	 * Gibt dem Element die Färbung die darauf hindeutet, dass es zur Zeit durch den User zur Bearbeitung ausgewählt ist.
	 */
	public void fillSelectionColor() {
		circle.setFill(SELECTION_COLOR);
	}

	//**** MODEL CORRESPONDENCE ****
	/**
	 * Diese Methode stellt sicher, dass die Darstellung von innerhalb einer Simulation gespeicherten, 
	 * neu geladenen Netzen korrekt verläuft, d.h. Markierung und Aktivierungsstatus berechnet und dargestellt werden.<br>
	 * Sie wurde eingerichtet, weil die entsprechenden Listener beim Laden nicht getriggert werden, da die graphische Darstellung erst nach dem Laden
	 * des Netzes stattfindet.
	 */
	public void init() {
		if (modelNode.isMarked()) {
			this.getChildren().add(markGraphic);
			modelNode.post.forEach(t -> t.setActivationStatus());
		}
		if (!modelNode.isMarked()) {
			this.getChildren().remove(markGraphic);
		}
	}
	/**
	 * Setzt die im Modell registrierten Koordinaten der Stelle auf die Koordinaten der graphischen Darstellung.<br>
	 * Wird nach Drag-Bewegungen aufgerufen.
	 */
	public void setModelCoordinates() {
		modelNode.setXpos(circle.getCenterX());
		modelNode.setYpos(circle.getCenterY());
	}
	/**
	 * @return
	 * 		gibt die Modellentsprechung der Stelle.
	 */
	public PetriNode getModel() {
		return modelNode;
	}
	/**
	 * Setzt die Modellentsprechung der Stelle.
	 * @param p
	 * 		Modell-Stelle
	 */
	public void setModelNode(Place p) {
		modelNode = p;
	}

	//**** GETTER & SETTER
	/**
	 * @return
	 * 		Gibt das "Namens-Schild" als FX-'Text'-Objekt
	 */
	public Text nameTag() {
		return nameTag;
	}
	@Override
	/**
	 * @return
	 * 		gibt die x-Koordinate des Mittelpunktes des Kreises der graphischen Darstellung der Stelle
	 */
	public double getCenterX() {
		return circle.getCenterX();
	}
	@Override
	/**
	 * @return
	 * 		gibt die y-Koordinate des Mittelpunktes des Kreises der graphischen Darstellung der Stelle
	 */
	public double getCenterY() {
		return circle.getCenterY();
	}
	@Override
	/**
	 * Setzt die x-Koordinate des Mittelpunktes des Kreises der graphischen Darstellung der Stelle.
	 * @param in
	 * 		numerischer Wert der x-Koordinate
	 */
	public void setCenterX(double in) {
		circle.setCenterX(in);
	}
	@Override
	/**
	 * Setzt die y-Koordinate des Mittelpunktes des Kreises der graphischen Darstellung der Stelle.
	 * @param in
	 * 		numerischer Wert der y-Koordinate
	 */
	public void setCenterY(double in) {
		circle.setCenterY(in);		
	}
	@Override
	/**
	 * @return
	 * 		gibt die x-Koordinate des Mittelpunkts des Kreises der graphischen Darstellung der Stelle als Property
	 */
	public DoubleProperty centerXProperty() {
		return circle.centerXProperty();
	}
	@Override
	/**
	 * @return
	 * 		gibt die y-Koordinate des Mittelpunkts des Kreises der graphischen Darstellung der Stelle als Property
	 */
	public DoubleProperty centerYProperty() {
		return circle.centerYProperty();
	}
	/**
	 * @return
	 * 		Gibt die radiusProperty des Kreises der graphischen Darstellung der Stelle
	 */
	public DoubleProperty radiusProperty() {
		return circle.radiusProperty();
	}
	/**
	 * Setzt die Färbung des Randes des Kreises der graphischen Darstellung der Stelle
	 * @param c
	 * 		zu setzende Farbe
	 */
	public void setStroke(Color c) {
		circle.setStroke(c);
	}
}
