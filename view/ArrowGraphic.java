package view;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;
import model.Arc;

/**
 * Implementiert die graphische Entsprechung eines Bogens im Modellnetz als Pfeil.
 */
public class ArrowGraphic extends NetElementGraphic {
	/**
	 * Instanz der inneren Klasse BoundLine, die eine Linie zwischen den Rändern von Vierecken und Kreisen beschreibt.
	 */
	private BoundLine line;
	/**
	 * Instanz der inneren Klasse Triangle, welche ein Dreieck als Pfeilspitze beschreibt.
	 */
	private Triangle arrowHead;

	/**
	 * Statisches Attribut welches einen die Größe bestimmenden Faktor für alle Pfeilspitzen beschreibt.<br>
	 * [Anm.: damit auch über alle Tabs hinweg]
	 */
	public static double HEAD_SIZE = 8;
	/**
	 * Referenz auf die Modellentsprechung dieses Bogens
	 */
	private Arc modelElement;

	/**
	 * Dieser Konstruktor nimmt Referenzen auf den graphischen Anfangs- und Endknoten des Bogens
	 * und auf die Modellentsprechung.<br>
	 * Dann erzeugt er eine gebundene Linie zwischen Anfang und Ende,
	 * ein Dreieck als Pfeilspitze und richtet Listener ein, die sicherstellen, 
	 * dass die Pfeilspitze immer aktualisiert wird, wenn sich der Winkel der Linie des Pfeils geändert haben könnte.
	 * @param start
	 * 		graphischer Knoten von dem der Pfeil ausgehen soll.
	 * @param end
	 * 		graphischer Knoten zu dem der Pfeil führen soll.
	 * @param inArc
	 * 		Modellentsprechung des Bogens
	 */
	public ArrowGraphic (NodeGraphic start, NodeGraphic end, Arc inArc) {
		this.modelElement = inArc;


		line = new BoundLine(start, end);
		arrowHead = new Triangle(line.endXProperty().get(), line.endYProperty().get(),
				line.startXProperty().get(), line.startYProperty().get());

		line.endXProperty().addListener((observable, oldV, newV) -> {
			arrowHead.move(line.startXProperty().get(), newV.doubleValue(),
					line.startYProperty().get(), line.endYProperty().get());
		});
		line.endYProperty().addListener((observable, oldV, newV) -> {
			arrowHead.move(line.startXProperty().get(), line.endXProperty().get(),
					line.startYProperty().get(), newV.doubleValue());
		});

		getChildren().add(line);
		getChildren().add(arrowHead);
	}

	/**
	 * @return
	 * Gibt die Referenz auf den graphischen Ausgangsknoten des Pfeils.
	 */
	public NodeGraphic from() {
		return line.from;
	}
	/**
	 * @return
	 * Gibt die Referenz auf den graphischen Endknoten des Pfeils.
	 */
	public NodeGraphic to() {
		return line.to;
	}

	/**
	 * Implementiert eine Linie zwischen einem Ausgangs- und einem Endknoten.
	 */
	private class BoundLine extends Line {
		/**
		 * Referenz auf den Ausgangsknoten des Pfeils.<br>
		 * Befindet sich hier, weil konzeptionell enger an die Linie als an die Pfeilgruppe gekoppelt.
		 */
		private NodeGraphic from;
		/**
		 * Referenz auf den Endknoten des Pfeils.<br>
		 * Befindet sich hier, weil konzeptionell enger an die Linie als an die Pfeilgruppe gekoppelt.
		 */
		private NodeGraphic to;
		/**
		 * Winkel-Property welches für die Datenbindungen für die Bestimmung der Deltas benötigt wird, 
		 * welche die Start- und Endpunkt der Linie auf die Ränder der Vierecke und Kreise verlegen.
		 */
		private DoubleProperty angleProp = new SimpleDoubleProperty();
		/**
		 * Delta-X-Property für Kreise, um die Linie auf dem Rand enden zu lassen.
		 */
		private DoubleProperty delXCircProperty = new SimpleDoubleProperty();
		/**
		 * Delta-Y-Property für Kreise, um die Linie auf dem Rand enden zu lassen.
		 */
		private DoubleProperty delYCircProperty = new SimpleDoubleProperty();
		/**
		 * Delta-X-Property für Vierecke, um die Linie auf dem Rand enden zu lassen.
		 */
		private DoubleProperty delXRectProperty = new SimpleDoubleProperty();

		/**
		 * Delta-Y-Property für Vierecke, um die Linie auf dem Rand enden zu lassen.
		 */
		private DoubleProperty delYRectProperty = new SimpleDoubleProperty();

		/**
		 * Bestimmt den Nenner der Steigung der Linie.
		 * @return
		 * 		Numerischen Wert des Nenners.
		 */
		private double slopeDenominator() {
			return		to.centerXProperty().get() - from.centerXProperty().get();
		}
		/**
		 * Bestimmt den Zähler der Steigung der Linie.
		 * @return
		 * 		Numerischen Wert des Zählers.
		 */
		private double slopeNumerator() {
			return		to.centerYProperty().get() - from.centerYProperty().get();
		}

		//**** KONSTRUKTOREN ****
		/**
		 * Dieser Konstruktor nimmt die Referenz auf den Start- und Endknoten und initialisiert oben deklarierten
		 * Property-Bindings.
		 * @param start
		 * 		Ausgangsknoten des Pfeils.
		 * @param end
		 * 		Endknoten des Pfeils.
		 */
		protected BoundLine(NodeGraphic start, NodeGraphic end) {
			from = start;
			to = end;

			angleProp.bind(Bindings.createDoubleBinding(
					//				Funktion die ausgeführt wird ...
					() -> {
						return Math.atan2(slopeNumerator(), slopeDenominator());
					}
					//				... wenn diese Abhängigkeiten sich ändern.
					, start.centerXProperty(), start.centerYProperty()
					, end.centerXProperty(), end.centerYProperty()
					));
			delXCircProperty.bind(Bindings.createDoubleBinding(
					() -> {
						return Math.cos(angleProp.get()) * PlaceGraphic.RADIUS.get();
					}
					, angleProp, PlaceGraphic.RADIUS));

			delYCircProperty.bind(Bindings.createDoubleBinding(
					() -> {
						return Math.sin(angleProp.get()) * PlaceGraphic.RADIUS.get();
					}
					, angleProp, PlaceGraphic.RADIUS));

			delXRectProperty.bind(Bindings.createDoubleBinding(
					() -> {
						//m sei die Steigung in y = mx, h sei Viereck-Höhe und w Weite:
						//if (-h/2 <= m * w/2 <= h/2)
						if ( 
								((TransitionGraphic.HEIGHT.get() / -2) <= ( (slopeNumerator() / slopeDenominator()) * TransitionGraphic.WIDTH.get() / 2 ))
								&&
								((TransitionGraphic.HEIGHT.get() / 2) >= ( (slopeNumerator() / slopeDenominator()) * TransitionGraphic.WIDTH.get() / 2 )) 
								) {
							//if ('start' ist links von 'end') then (delX ist positiv)
							if (start.centerXProperty().get() < end.centerXProperty().get())
								return TransitionGraphic.WIDTH.get() / 2;
							else if (start.centerXProperty().get() > end.centerXProperty().get())
								return TransitionGraphic.WIDTH.get() / -2;
						}
						else {
							//anpassen je nach Steigungsvorzeichen
							if (start.centerYProperty().get() < end.centerYProperty().get())
								return (slopeDenominator() / slopeNumerator()) * TransitionGraphic.HEIGHT.get() / 2 ;
							else
								return (slopeDenominator() / slopeNumerator()) * TransitionGraphic.HEIGHT.get() / -2 ;
						}
						//eigentlich sollte dieses return nicht erreicht werden können, es unterdrückt effektiv nur eine Warnung
						return 0.0;
					}
					, start.centerXProperty(), start.centerYProperty()
					, end.centerXProperty(), end.centerYProperty()
					, TransitionGraphic.HEIGHT, TransitionGraphic.WIDTH
					));

			delYRectProperty.bind(Bindings.createDoubleBinding(
					() ->
					{
						//m sei die Steigung in y = mx, h sei Viereck-Höhe und w Weite:
						//if (-w/2 <= h/2m <= w/2)
						if ( 
								((TransitionGraphic.WIDTH.get() / -2) <= ( (slopeDenominator() / slopeNumerator()) * TransitionGraphic.HEIGHT.get() / 2 ))
								&&
								((TransitionGraphic.WIDTH.get() / 2) >= ( (slopeDenominator() / slopeNumerator()) * TransitionGraphic.HEIGHT.get() / 2 )) ) {
							//if ('start' ist über 'end') then (delY ist positiv (also nach unten))
							if (start.centerYProperty().get() < end.centerYProperty().get())
								return TransitionGraphic.HEIGHT.get() / 2;
							else if (start.centerYProperty().get() > end.centerYProperty().get())
								return TransitionGraphic.HEIGHT.get() / -2;
						}
						else {
							//anpassen je nach Steigungsvorzeichen
							if (start.centerXProperty().get() < end.centerXProperty().get())
								return TransitionGraphic.WIDTH.get() / 2 * (slopeNumerator() / slopeDenominator());
							else
								return TransitionGraphic.WIDTH.get() / -2 * (slopeNumerator() / slopeDenominator());
						}
						//eigentlich sollte dieses return nicht erreicht werden können, es unterdrückt effektiv nur eine Warnung
						return 0.0;
					}
					, start.centerXProperty(), start.centerYProperty()
					, end.centerXProperty(), end.centerYProperty()
					, TransitionGraphic.WIDTH, TransitionGraphic.HEIGHT
					)); 

			//REGISTER DELTA-PROPERTIES
			if (start.getClass() == PlaceGraphic.class) {
				startXProperty().bind(start.centerXProperty().add(delXCircProperty));
				startYProperty().bind(start.centerYProperty().add(delYCircProperty));
			}
			else if (start.getClass() == TransitionGraphic.class) {
				startXProperty().bind(start.centerXProperty().add(delXRectProperty));
				startYProperty().bind(start.centerYProperty().add(delYRectProperty));
			}	
			if (end.getClass() == PlaceGraphic.class) {
				endXProperty().bind(end.centerXProperty().subtract(delXCircProperty));
				endYProperty().bind(end.centerYProperty().subtract(delYCircProperty));
			}
			else if (end.getClass() == TransitionGraphic.class) {
				endXProperty().bind(end.centerXProperty().subtract(delXRectProperty));
				endYProperty().bind(end.centerYProperty().subtract(delYRectProperty));
			}

			setStrokeWidth(2.5);
			setStroke(Color.BLACK.deriveColor(0, 1, 1, 0.5));
			setStrokeLineCap(StrokeLineCap.BUTT);
		}
	}
	/**
	 * Implementiert ein Dreieck als Polygon
	 */
	private class Triangle extends Polygon {
		/**
		 * Enthält zu jedem Zeitpunkt einen Faktor aus dem Kehrtwert der euklidischen Distanz
		 * von Start- und Endpunkt des Pfeils, dem Attribut HEAD_SIZE und 1/2.
		 */
		private double proportion;
		
		protected Triangle (double endX, double endY, double startX, double startY) {
			super(0, 0, 0, 0, 0, 0);
			proportion = HEAD_SIZE / Math.hypot(startX - endX, startY - endY) / 2;
			getPoints().setAll(
					endX,
					endY,
					endX + ((startX - endX) - (startY - endY)) * proportion,
					endY + ((startY - endY) + (startX - endX)) * proportion,
					endX + ((startX - endX) + (startY - endY)) * proportion,
					endY + ((startY - endY) - (startX - endX)) * proportion
					);
		}

		public void move(double startX, double endX, double startY, double endY) {
			proportion = HEAD_SIZE / Math.hypot(startX - endX, startY - endY) / 2;
			getPoints().setAll(
					endX,
					endY,
					endX + ((startX - endX) - (startY - endY)) * proportion,
					endY + ((startY - endY) + (startX - endX)) * proportion,
					endX + ((startX - endX) + (startY - endY)) * proportion,
					endY + ((startY - endY) - (startX - endX)) * proportion
					);
		}
	}

	@Override
	/**
	 * Implementiert die abstrakte Methode von NetElementGraphic.<br>
	 * Gibt dem Pfeil die Farbe mit der angezeigt werden soll, dass er derzeit zur Bearbeitung ausgewählt ist.
	 */
	public void fillSelectionColor() {
		this.line.setStroke(Color.AQUA.deriveColor(0, 1, 1, 0.5));
		this.arrowHead.setFill(Color.AQUA.deriveColor(0, 1, 1, 0.5));
	}

	@Override
	/**
	 * Implementiert die abstrakte Methode von NetElementGraphic.<br>
	 * Gibt dem Pfeil die Farbe mit der angezeigt werden soll, dass er nicht zur Bearbeitung ausgewählt ist.
	 */
	public void fillStandardColor() {
		this.line.setStroke(Color.BLACK.deriveColor(0, 1, 1, 0.5));
		this.arrowHead.setFill(Color.BLACK);
	}

	/**
	 * @return
	 * Gibt den zugehörigen Modellbogen.
	 */
	public Arc getModel() {
		return modelElement;
	}
	
	public Line getLine() {
		return line;
	}
}
