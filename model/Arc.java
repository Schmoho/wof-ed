package model;

/**
 * Diese Klasse implementiert die Modellebene für einen Bogen in einem Petrinetz.
 * Sie erweitert die abstrakte Klasse NetElement.
 */
public class Arc extends NetElement{
	
	//**** FIELDS ****
	/**
	 * Dieses Attribut beschreibt von welchem Knoten der Bogen ausgeht.
	 */
	public PetriNode from;
	/**
	 * Dieses Attribut beschreibt zu welchem Knoten der Bogen führt.
	 */
	public PetriNode to;
	
}
