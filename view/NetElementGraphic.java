package view;

import javafx.scene.Group;

/**
 * Abstrakte Klasse, die Group beerbt und von der alle graphischen Netzelementdarstellungen erben.
 */
public abstract class NetElementGraphic extends Group {

	/**
	 * Gibt dem Element die Färbung die darauf hindeutet, dass es zur Zeit durch den User zur Bearbeitung ausgewählt ist.
	 */
	public abstract void fillSelectionColor();

	/**
	 * Gibt dem Element die Färbung die darauf hindeutet, dass es nicht durch den User zur Bearbeitung ausgewählt ist.
	 */
	public abstract void fillStandardColor();
}
