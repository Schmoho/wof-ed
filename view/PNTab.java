package view;

import model.PetriNet;
import javafx.scene.control.Tab;

/**
 * Implementiert ein Tab mit einer Referenz auf das mit ihm assoziierte Petrinetz.
 */
public class PNTab extends Tab {

	//**** FIELDS ****
	/**
	 * Das mit diesem Tab assoziierte Netz.<br>
	 * Das dient bei Tabwechseln die Aktualisierung der Referenzen im ViewController.
	 */
	private PetriNet net;

	//**** GETTER AND SETTER ****
	/**
	 * Gibt das mit diesem Tab assoziierte Netz zurück.
	 * @return
	 * 		assoziiertes Petrinetz
	 */
	public PetriNet getNet() {
		return net;
	}
	/**
	 * Setzt das mit diesem Tab assoziierte Netz.
	 * @param net
	 * 		Petrinetz mit dem dieses Tab assoziiert werden soll.
	 */
	public void setNet(PetriNet net) {
		this.net = net;
	}

}
