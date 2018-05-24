package model;


import view.NodeGraphic;

/**
 * Abstrakte Klasse der Knoten von der Stellen und Transitionen erben.
 */
public abstract class PetriNode extends NetElement{

	//**** FIELDS ****
	/**
	 * Name des Knotens als String.
	 */
	private String name;
	/**
	 * flag zeigt an ob der Knoten auf einem Pfad vom Anfang entlang der Bogenrichtungen liegt.
	 */
	private boolean onPathFromStart = false;
	/**
	 * flag zeigt an ob der Knoten auf einem Pfad vom Ende entgegen der Bogenrichtungen liegt.
	 */
	private boolean onPathFromEnd = false;
	/** 
	 * x-Koordinate des Knotens wie sie im korrespondierenden PNML-File auftauchen würde.
	 */
	private int xPos;
	/** 
	 * y-Koordinate des Knotens wie sie im korrespondierenden PNML-File auftauchen würde.
	 */
	private int yPos;
	/**
	 * Referenz auf die graphische Entsprechung dieses Knotens.
	 */
	private NodeGraphic graphic;

	//**** GETTER AND SETTER
	/**	
	 * @return
	 * Gibt die Referenz auf die graphische Entsprechung dieses Knotens.
	 */
	public NodeGraphic getGraphic() {
		return graphic;
	}
	/**
	 * Setze die Referenz auf die graphische Entsprechung dieses Knotens.
	 * @param graphic
	 * 		Der graphische Knoten auf den die Referenz gesetzt werden soll.
	 */
	public void setGraphic(NodeGraphic graphic) {
		this.graphic = graphic;
	}
	/**
	 * @return
	 * integer-Wert der x-Koordinate wie sie im korrespondierenden PNML-File auftauchen würde.
	 */
	public int getXpos() {
		return xPos;
	}
	/**
	 * Setzt den Wert der x-Koordinate als Integer und konvertiert dafür von Double.<br>
	 * [Anm.: das wurde so gehandhabt weil die Koordinaten-Ausgaben der korrespondierenden FX-Shapes double-Werte zurückgeben.]
	 * @param xpos
	 * 		der Wert auf den die x-Koordinate gesetzt werden soll.
	 */
	public void setXpos(Double xpos) {
		this.xPos = xpos.intValue();
	}
	/**
	 * @return
	 * integer-Wert der y-Koordinate wie sie im korrespondierenden PNML-File auftauchen würde.
	 */
	public int getYpos() {
		return yPos;
	}
	/**
	 * Setzt den Wert der y-Koordinate als Integer und konvertiert dafür von Double.<br>
	 * [Anm.: das wurde so gehandhabt weil die Koordinaten-Ausgaben der korrespondierenden FX-Shapes double-Werte zurückgeben.]
	 * @param ypos
	 * 		der Wert auf den die y-Koordinate gesetzt werden soll.
	 */
	public void setYpos(Double ypos) {
		this.yPos = ypos.intValue();
	}
	/**
	 * Setzt das Namens-Attribut des Knotens.
	 * @param name
	 * 		Name als String
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return
	 * Gibt den String-Wert des Namens-Attributs des Knotens.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Abstrakte Methode welche für einen gegebenen Bogen die relevanten Einträge aus den Nachfolger- und Vorgängerlisten
	 * entfernen soll.
	 * @param in
	 * 		Der Bogen für den die Einträge entfernt werden sollen.
	 */
	public abstract void removeArc(Arc in);
	
	/**
	 * Gibt an ob der Knoten sich auf einem Pfad vom Startpunkt des Netzes aus in Richtung der Bögen befindet.
	 * @return
	 * 	value of the flag
	 */
	public boolean isOnPathFromStart() {
		return onPathFromStart;
	}
	/**
	 * Teilt dem Knoten mit ob er sich auf einem Pfad vom Startpunkt des Netzes aus befindet.
	 */
	public void setOnPathFromStart(boolean onPath) {
		this.onPathFromStart = onPath;
	}
	/**
	 * Gibt an ob der Knoten sich auf einem Pfad vom Endpunkt des Netzes aus entgegen der Richtung der Bögen befindet. 
	 * @return
	 * 	value of the flag
	 */
	public boolean isOnPathFromEnd() {
		return onPathFromEnd;
	}
	/**
	 * Teilt dem Knoten mit ob er sich auf einem Pfad vom Endpunkt des Netzes aus befindet.
	 */
	public void setOnPathFromEnd(boolean onPath) {
		this.onPathFromEnd = onPath;
	}

}
