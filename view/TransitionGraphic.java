package view;

import model.Transition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * Implementiert die graphische Repräsentation einer Transition im Petrinetz.
 */
public class TransitionGraphic extends NetElementGraphic implements NodeGraphic {

	//**** FIELDS ****
	/**
	 * die Farbe für eine nicht aktivierten Transition
	 */
	public static Color STD_COLOR = Color.ORANGE;
	/**
	 * die Farbe für den Rand einer unausgewählten Transition
	 */
	public static Color STROKE_COLOR = Color.GOLD;
	/**
	 * die Farbe für den Rand einer ausgewählten Transition
	 */
	public static Color SELECTION_COLOR = Color.AQUA;
	/**
	 * die Farbe für eine aktivierte Transition
	 */
	public static Color ACT_COLOR = Color.GREEN;
	/**
	 * die Farbe für eine aktivierte Transition mit Kontakt
	 */
	public static Color CONTACT_COLOR = Color.RED;
	/**
	 * globales Weiten-Attribut um globale Größenänderung zu vereinfachen
	 */
	public static DoubleProperty WIDTH = new SimpleDoubleProperty(25);
	/**
	 * globales Höhen-Attribut um globale Größenänderung zu vereinfachen
	 */
	public static DoubleProperty HEIGHT = new SimpleDoubleProperty(25);

	/**
	 * Referenz auf die Modellentsprechung der Transition
	 */
	private Transition modelNode;
	/**
	 * Das Viereck als dass die Transition dargestellt wird.
	 */
	private Rectangle rect;
	/**
	 * Das "Namensschild" der Transition.
	 */
	private Text nameTag;

	/**
	 * x-Koordinate des Mittelpunktes des Vierecks<br>
	 * Dient dazu den Mittelpunkt des Vierecks der graphischen Darstellung zu ermitteln, da Rectangle nur den
	 * oberen linken Eckpunkt anbietet.
	 */
	private DoubleProperty centerXProperty = new SimpleDoubleProperty();
	/**
	 * y-Koordinate des Mittelpunktes des Vierecks<br>
	 * Dient dazu den Mittelpunkt des Vierecks der graphischen Darstellung zu ermitteln, da Rectangle nur den
	 * oberen linken Eckpunkt anbietet.
	 */
	private DoubleProperty centerYProperty = new SimpleDoubleProperty();

	//**** CONSTRUCTOR ****
	/**
	 * Nimmt eine x- und y-Koordinate sowie eine Referenz auf die Modellentsprechung der neuen graphischen
	 * Transition. Initialisiert die centerProperties, sowie das Namensschild und assoziiert das
	 * Namensattribut der Modellentsprechung der Stelle damit. Richtet Listener auf die activated- und 
	 * contact-BooleanProperties der Modellentsprechung ein, um die Farbe entsprechend zu ändern.
	 * @param posX
	 * 		x-Koordinate der neuen Transition.
	 * @param posY
	 * 		y-Koordinate der neuen Transition.
	 * @param in
	 * 		Modellentsprechung der neuen Transition.
	 */
	public TransitionGraphic(double posX, double posY, Transition in) {
		rect = new Rectangle(WIDTH.get(), HEIGHT.get(), STD_COLOR);
		rect.heightProperty().bind(HEIGHT);
		rect.widthProperty().bind(WIDTH);
		rect.setStroke(STROKE_COLOR);
		rect.setStrokeWidth(3);
		modelNode = in;

		nameTag = new Text(in.getName());
		nameTag.xProperty().bind(centerXProperty());
		nameTag.yProperty().bind(centerYProperty().add(rect.heightProperty().divide(2).add(15)));
		nameTag.textProperty().addListener(e -> {
			modelNode.setName(nameTag.getText());
		});

		setCenterX(posX);
		setCenterY(posY);
		rect.xProperty().bind(centerXProperty.add((rect.widthProperty().divide(-2))));
		rect.yProperty().bind(centerYProperty.add((rect.heightProperty().divide(-2))));

		modelNode.activated().addListener((e, oldV, newV) -> {
			if(modelNode.contact().get() && modelNode.activated().get()) {
				this.rect.setFill(CONTACT_COLOR);
			}
			else if(modelNode.activated().get()) {		
				this.rect.setFill(ACT_COLOR);
			}
			else {
				this.rect.setFill(STD_COLOR);
			}
		});

		modelNode.contact().addListener((e, oldV, newV) -> {
			if(modelNode.contact().get() && modelNode.activated().get()) {
				this.rect.setFill(CONTACT_COLOR);
			}
			else if(modelNode.activated().get()) {		
				this.rect.setFill(ACT_COLOR);
			}
			else {
				this.rect.setFill(STD_COLOR);
			}
		});

		getChildren().add(rect);
		getChildren().add(nameTag);
	}

	//**** METHODS ****
	/**
	 * Gibt dem Element die Färbung die darauf hindeutet, dass es nicht durch den User zur Bearbeitung ausgewählt ist.
	 */
	@Override
	public void fillStandardColor() {
		rect.setStroke(STROKE_COLOR);		
	}
	/**
	 * Gibt dem Element die Färbung die darauf hindeutet, dass es zur Zeit durch den User zur Bearbeitung ausgewählt ist.
	 */
	@Override
	public void fillSelectionColor() {
		rect.setStroke(SELECTION_COLOR);
	}

	//**** MODEL CORRESPONDENCE ****
	/**
	 * Setzt die im Modell registrierten Koordinaten der Transition auf die Koordinaten der graphischen Darstellung.<br>
	 * Wird nach Drag-Bewegungen aufgerufen.
	 */
	@Override
	public void setModelCoordinates() {
		modelNode.setXpos(this.getCenterX());
		modelNode.setYpos(this.getCenterY());
	}
	/**
	 * @return
	 * 		gibt die Modellentsprechung der Transition.
	 */
	@Override
	public Transition getModel() {
		return modelNode;
	}
	/**
	 * Setzt die Modellentsprechung der Transition.
	 * @param p
	 * 		Modell-Stelle
	 */
	public void setModelNode(Transition p) {
		modelNode = p;
	}

	//**** GETTER AND SETTER ****
	/**
	 * @return
	 * 		Gibt das "Namens-Schild" als FX-'Text'-Objekt
	 */
	@Override
	public Text nameTag() {
		return nameTag;
	}

	@Override
	/**
	 * @return
	 * 		gibt die x-Koordinate des Mittelpunktes des Vierecks der graphischen Darstellung der Transition
	 */
	public double getCenterX() {
		return this.centerXProperty.get();
	}
	@Override
	/**
	 * @return
	 * 		gibt die y-Koordinate des Mittelpunktes des Vierecks der graphischen Darstellung der Transition
	 */
	public double getCenterY() {
		return this.centerYProperty.get();
	}
	@Override
	/**
	 * Setzt die x-Koordinate des Mittelpunktes des Vierecks der graphischen Darstellung der Transition.
	 * @param in
	 * 		numerischer Wert der x-Koordinate
	 */
	public void setCenterX(double posX) {
		this.centerXProperty.set(posX);
	}
	@Override
	/**
	 * Setzt die y-Koordinate des Mittelpunktes des Vierecks der graphischen Darstellung der Transition.
	 * @param in
	 * 		numerischer Wert der y-Koordinate
	 */
	public void setCenterY(double posY) {
		this.centerYProperty.set(posY);
	}
	@Override
	/**
	 * @return
	 * 		gibt die x-Koordinate des Mittelpunkts des Vierecks der graphischen Darstellung 
	 * 		der Transition als Property
	 */
	public DoubleProperty centerXProperty() {
		return centerXProperty;
	}
	@Override
	/**
	 * @return
	 * 		gibt die y-Koordinate des Mittelpunkts des Vierecks der graphischen Darstellung 
	 * 		der Transition als Property
	 */
	public DoubleProperty centerYProperty() {
		return centerYProperty;
	}
	/**
	 * @return
	 * 		Gibt die widthProperty des Vierecks der graphischen Darstellung der Transition
	 */
	public DoubleProperty widthProperty() {
		return rect.widthProperty();
	}
	/**
	 * @return
	 * 		Gibt die heightProperty des Vierecks der graphischen Darstellung der Transition
	 */
	public DoubleProperty heightProperty() {
		return rect.heightProperty();
	}

	@Override
	/**
	 * Setzt die Färbung des Randes des Vierecks der graphischen Darstellung der Transition
	 * @param c
	 * 		zu setzende Farbe
	 */
	public void setStroke(Color c) {
		rect.setStroke(c);
	}





}
