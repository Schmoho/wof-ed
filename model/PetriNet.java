package model;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.BooleanProperty;
import parser.*;


/**
 *	Diese Klasse implementiert ein Workflownetz auf Modellebene. 
 */
public class PetriNet {

	//**** FIELDS ****
	/**
	 * Enthält alle Stellen des Netzes und lässt sie durch IDs abbilden.
	 */
	private Map<String, Place> places = new HashMap<String, Place>();
	/**
	 * Enthält alle Transitionen des Netzes und lässt sie durch IDs abbilden.
	 */
	private Map<String, Transition> transitions = new HashMap<String, Transition>();
	/**
	 * Enthält alle Bögen des Netzes und lässt sie durch IDs abbilden.
	 */
	private Map<String, Arc> arcs = new HashMap<String, Arc>();

	/**
	 * Beschreibt den Startknoten des Workflownetzes.
	 */
	private Place startNode = null;
	/**
	 * Beschreibt den Endknoten des Workflownetzes.
	 */
	private Place endNode = null;
	/**
	 * flag-Property beschreibt ob das Netz einen Startknoten hat.<br>
	 * Es dient der Datenbindung für die visuelle Darstellung.
	 */
	private BooleanProperty startExists = new SimpleBooleanProperty();
	/**
	 * flag-Property beschreibt ob das Netz einen Endknoten hat.<br>
	 * Es dient der Datenbindung für die visuelle Darstellung.
	 */
	private BooleanProperty endExists = new SimpleBooleanProperty();
	/**
	 * flag-Property beschreibt ob das Netz mehr als einen Startknoten hat.<br>
	 * Es dient der Datenbindung für die visuelle Darstellung.
	 */
	private BooleanProperty startMoreThanOne = new SimpleBooleanProperty();
	/**
	 * flag-Property beschreibt ob das Netz mehr als einen Endknoten hat.<br>
	 * Es dient der Datenbindung für die visuelle Darstellung.
	 */
	private BooleanProperty endMoreThanOne = new SimpleBooleanProperty();
	/**
	 * flag-Property beschreibt ob in dem Netz alle Knoten auf einem Pfad vom Anfang zum Ende liegen.<br>
	 * Es dient der Datenbindung für die visuelle Darstellung.
	 */
	private BooleanProperty pathProp = new SimpleBooleanProperty();
	/**
	 * flag-Property beschreibt ob ein Deadlock in der Simulation vorliegt
	 */
	private BooleanProperty deadlockProp = new SimpleBooleanProperty();
	/**
	 * flag-Property beschreibt ob die Endmarkierung erreicht ist
	 */
	private BooleanProperty simFinProp = new SimpleBooleanProperty();

	/**
	 * Wird verwendet in der Implementierung des Löschens eines Knotens
	 * um alle Bögen die diesen Knoten als Ausgangs- oder Endpunkt haben ebenfalls zu löschen.
	 * @param in
	 * 		Der Knoten für den überprüft werden soll, ob der Bogen ihn berührt.
	 * @return
	 * 		<b>true</b>: wenn der Knoten Start- oder Endpunkt des Bogens ist,<br>
	 * 		<b>false</b>: sonst.
	 */
	private static Predicate<Arc> touchesNode(PetriNode in) {
		return p -> (p.from == in || p.to == in);
	}

	//**** INSERT NEW ELEMENTS / DELETION ****
	// die folgenden drei Methoden sind Parsing-Routinen
	/**
	 * Nimmt eine ID fügt für diese eine neue Stelle in die Map places ein,
	 * sofern die ID noch nicht enthalten ist.
	 * @param id
	 * 		Der String der die ID für die neue Stelle beschreibt.
	 */
	public void newPlace(String id) {
		if (isUniqueId(id)) {
			places.put(id, new Place(id));
		}
		else
			System.out.println("non-unique ID, Place has been skipped");
	}
	/**
	 * Nimmt eine ID fügt für diese eine neue Transition in die Map transitions ein,
	 * sofern die ID noch nicht enthalten ist.
	 * @param id
	 * 		Der String der die ID für die neue Transition beschreibt.
	 */
	public void newTransition(String id) {
		if (isUniqueId(id)) {
			transitions.put(id, new Transition(id));
		}
		else
			System.out.println("non-unique ID, Transition has been skipped");
	}
	/**
	 * Nimmt eine ID, die ID des Ausgangs- und die ID des Endknotens und fügt einen neuen Bogen in die Map arcs ein,
	 * sofern die ID noch nicht enthalten ist.<br>
	 * Für die Knoten die durch den Bogen verbunden werden, aktualisiert sie die Vorgänger- und Nachfolgerlisten.<br>
	 * Sie stellt außerdem sicher, dass ein Bogen immer von einer Stelle zu einer Transition führt oder umgekehrt,
	 * indem andere Fälle zu einer Fehlermeldung führen.
	 * @param id
	 * 		Der String der die ID des neuen Bogens enthält.
	 * @param source
	 * 		Der String der die ID des Ausgangsknotens des Bogens enthält.
	 * @param target
	 * 		Der String der die ID des Endknotens des Bogens enthält.
	 */
	public void newArc(String id, String source, String target) {
		if (!isUniqueId(id)) {
			System.out.println("non-unique ID, Arc has been skipped");
			return;
		}
		Arc arcToBeAdded = new Arc();
		arcToBeAdded.ID = id;
		if (places.containsKey(source) && transitions.containsKey(target)) {
			arcToBeAdded.from = places.get(source);
			arcToBeAdded.to = transitions.get(target);
			transitions.get(target).addToPre(places.get(source));
			places.get(source).addToPost(transitions.get(target));
			arcs.put(id, arcToBeAdded);
		}
		else if (transitions.containsKey(source) && places.containsKey(target)) {
			arcToBeAdded.from = transitions.get(source);
			arcToBeAdded.to = places.get(target);
			places.get(target).addToPre(transitions.get(source));
			transitions.get(source).addToPost(places.get(target));
			arcs.put(id, arcToBeAdded);
		}
		else
			System.out.println("Error: invalid arc");
	}
	//die folgenden drei Methoden werden ausgelöst während der Benutzer editiert
	/**
	 * Erzeugt zuerst eine möglichst kleine noch nicht vergebene ID und fügt mit dieser eine neue Stelle
	 * in die Map places ein.
	 * @return
	 * 		Gibt die neu eingefügte Stelle zurück, für weitere Verwendung im ViewController.
	 */
	public Place newPlace() {
		int i = 0;
		String newID = "P0";
		while (places.containsKey(newID)) {
			newID = "P" + Integer.toString(i);
			i++;
		}
		Place p = new Place(newID);
		places.put(newID, p);
		return p;
	}
	/**
	 * Erzeugt zuerst eine möglichst kleine noch nicht vergebene ID und fügt mit dieser eine neue Transition
	 * in die Map transitions ein.
	 * @return
	 *		Gibt die neu eingefügt Transition zurück, für weitere Verwendung im ViewController.
	 */
	public Transition newTransition() {
		int i = 0;
		String newID = "T0";
		while (transitions.containsKey(newID)) {
			newID = "T" + Integer.toString(i);
			i++;
		}
		Transition t = new Transition(newID);
		transitions.put(newID, t);
		return t;
	}

	/**
	 * Nimmt einen Ausgangs- und Eingangsknoten, erzeugt eine möglichst kleine noch nicht vergebene ID
	 * und ruft mit dieser und den IDs der Knoten die Funktion newArc(String id, String source, String target) auf.
	 * @param source
	 * 		Der Modellknoten von dem aus der Bogen gehen soll.
	 * @param target
	 * 		Der Modellknoten zu dem der Bogen führen soll.
	 */
	public void newArc(PetriNode source, PetriNode target) {
		int i = 0;
		String newID = "K0";
		while (arcs.containsKey(newID)) {
			newID = "K" + Integer.toString(i);
			i++;
		}
		newArc(newID, source.ID, target.ID);
	}
	//die folgenden vier Methoden sind Löschmethoden
	/**
	 * Nimmt einen Knoten und entfernt alle Bögen die von ihm aus- oder bei ihm eingehen.<br>
	 * Je nachdem ob es sich um eine Stelle oder Transition handelt, ruft sie die entsprechende Löschmethode auf
	 * und testet anschließend, ob die Pfadeigenschaft eines Workflownetzes noch erhalten ist.
	 * @param inNode
	 * 		Der Knoten der aus dem Netz gelöscht werden soll.
	 */
	public void deleteNode(PetriNode inNode) {

		arcs.values().removeIf(touchesNode(inNode));
		if (inNode.getClass() == Place.class)
			deletePlace((Place) inNode);
		if (inNode.getClass() == Transition.class)
			deleteTransition((Transition) inNode);
		this.testIfPathPropHolds();
	}
	/**
	 * Entfernt die zu löschende Stelle zuerst aus den Nachfolgerlisten all seiner Vorgänger
	 * und aus den Vorgängerlisten all seiner Nachfolger und entfernt sie schließlich aus der Map <i>places</i>.<br>
	 * Wenn es sich um den Start- oder Endknoten gehandelt hat, wird das Attribut des Netzes entsprechend genullt.
	 * @param inPlace
	 * 		Die zu löschende Stelle.
	 */
	private void deletePlace(Place inPlace) {
		inPlace.post.forEach(e -> {
			e.pre.remove(inPlace);
		});
		inPlace.pre.forEach(e -> {
			e.post.remove(inPlace);
		});
		places.remove(inPlace.ID);
		if (inPlace == startNode)
			startNode = null;
		if (inPlace == endNode)
			endNode = null;
	}
	/**
	 * Entfernt die zu löschende Transition zuerst aus den Nachfolgerlisten all ihrer Vorgänger
	 * und aus den Vorgängerlisten all ihrer Nachfolger und entfernt sie schließlich aus der Map <i>transitions</i>.
	 * @param inTrans
	 * 		Die zu löschende Transition.
	 */
	private void deleteTransition(Transition inTrans) {
		inTrans.post.forEach(e -> {
			e.pre.remove(inTrans);
		});
		inTrans.pre.forEach(e -> {
			e.post.remove(inTrans);
		});
		transitions.remove(inTrans.ID);
	}
	/**
	 * Entfernt den Endpunkt des zu löschenden Bogens aus der Nachfolgerliste seines Ausgangspunktes
	 * und seinen Startpunkt aus der Vorgängerliste seines Eingangspunktes und entfernt den Bogen schließlich aus der Map <i>arcs</i>.
	 * Schließlich testet sie, ob die Pfadeigenschaft eines Workflownetzes noch erhalten ist.
	 * @param inArc
	 */
	public void deleteArc(Arc inArc) {
		inArc.from.removeArc(inArc);
		inArc.to.removeArc(inArc);
		this.arcs.remove(inArc.ID);
		this.testIfPathPropHolds();
	}

	//**** MODIFY ELEMENT PROPERTIES ****
	//die folgenden drei Methoden sind Parsing-Routinen
	/**
	 * Nimmt eine ID und zwei Zahlenwerte und setzt, falls die ID enthalten ist,
	 * für die korrespondierende Stelle oder Transitionen die X und Y-Werte entsprechend.
	 * @param id
	 * 		Die ID für die die X- und Y-Werte gesetzt werden sollen.
	 * @param x
	 * 		Der Wert auf den das X-Attribut des Knotens gesetzt werden soll.<br>
	 * 		Es findet hier ein implizierter Typecast auf double statt, der die Korrespondenz von Darstellung
	 * 		und Modell im Folgenden erleichtert.
	 * @param y
	 * 		Der Wert auf den das Y-Attribut des Knotens gesetzt werden soll.<br>
	 * 		Es findet hier ein implizierter Typecast auf double statt, der die Korrespondenz von Darstellung
	 * 		und Modell im Folgenden erleichtert.
	 */
	public void setPosition(String id, double x, double y) {
		if (places.containsKey(id))
		{
			places.get(id).setXpos(x);
			places.get(id).setYpos(y);
		}
		else if (transitions.containsKey(id)) {
			transitions.get(id).setXpos(x);
			transitions.get(id).setYpos(y);
		}
		else
			System.out.println("setPosition called with bad values");
	}
	/**
	 * Nimmt eine ID und eine String auf den das Namens-Attribut der zur ID korrespondieren
	 * Stelle oder Transition gesetzt werden soll.
	 * @param id
	 * 		Die ID für die das Namens-Attribut gesetzt werden soll.
	 * @param name
	 * 		Der String auf den das Namens-Attribut gesetzt werden soll.
	 */
	public void setName(String id, String name) {
		if (places.containsKey(id))
		{
			places.get(id).setName(name);
		}
		else if (transitions.containsKey(id)) {
			transitions.get(id).setName(name);
		}
		else
			System.out.println("setName called with bad values");
	}
	/**
	 * Nimmt eine ID und einen String der die Markierung der zur ID korrespondierenden Stelle beschreibt.
	 * @param id
	 * 		Die ID der Stelle deren Markierung gesetzt werden soll.
	 * @param marking
	 * 		Der String der die Markierung des Knotens beschreibt (i.d.R. "0" oder "1").
	 */
	public void setMarking(String id, String marking) {
		if (places.containsKey(id)) {
			places.get(id).setMark(marking);
		}
		else
			System.out.println("setMarking called with bad values");
	}

	//**** SIMULATION ****
	/**
	 * Überprüft ob es Start- und Endknoten gibt und ob die Pfadeigenschaft eines Workflownetzes erfüllt ist.<br>
	 * Wenn nicht, sorgt sie dafür, dass keine Markierungen im Netz bleiben.<br>
	 * Andernfalls setzt sie alle Markierungen auf "0" und die Markierung des Startknotens auf "1".
	 */
	public void setInitialMarking() {
		if (startNode == null || endNode == null || !pathProp.get())
			voidMarking();
		else if (!places.isEmpty()) {
			places.values().forEach(p ->
			p.setMark("0"));
			if (startNode != null)
				startNode.setMark("1");
		}
	}
	/**
	 * Setzt für alle Stellen die Markierung auf "0".
	 */
	public void voidMarking() {
		if (!places.isEmpty()) {
			places.values().forEach(p ->
			p.setMark("0"));
		}
	}
	/**
	 * Durchläuft alle Stellen und überprüft ob irgendeine davon als Start-
	 * bzw Endknoten qualifiziert. Wenn mehr als eine qualifiziert, Start und Ende gleich sind,
	 * oder eines von beidem nicht gefunden werden konnte, werden die entsprechenden flags-Properties gesetzt und
	 * die Netzattribute für Start- und Endknoten genullt.<br>
	 * @return
	 * 		<b>true</b>: falls genau ein Start- und genau ein Endknoten gefunden wurde<br>
	 * 		<b>false</b>: sonst
	 */
	public boolean setStartAndEnd() {
		startExists.set(false);
		endExists.set(false);
		startMoreThanOne.set(false);
		endMoreThanOne.set(false);
		startNode = null;
		endNode = null;
		for (Place p : places.values()) {
			if (p.pre.isEmpty()) {
				if (this.startNode == null) {
					startNode = p;
					startExists.set(true);
				}
				else {
					startMoreThanOne.set(true);
				}
			}
			if (p.post.isEmpty()) {
				if (this.endNode == null) {
					endNode = p;
					endExists.set(true);
				}
				else {
					endMoreThanOne.set(true);
				}
			}
		}
		if (startNode == null || endNode == null || startNode == endNode
				|| startMoreThanOne.get() || endMoreThanOne.get()) {
			startNode = null;
			endNode = null;
			return false;
		}
		return true;
	}
	/**
	 * Veranlasst, dass (falls diese existieren) vom Start- und vom Endknoten Zusammenhangskomponenten bestimmt
	 * werden. Sie geht dann alle Knoten des Netzes durch und stellt fest, ob jeder ein Teil beider Komponenten ist.<br>
	 * Dadurch bestimmt sie ob die Pfadeigenschaft eines Workflownetzes (jeder Knoten ist auf einem Pfad vom Anfangs-
	 * zum Endknoten) gegeben ist.<br>
	 * Sie setzt dann den Wert des Property 'pathProperty' entsprechend und gibt den boolean-Wert zurück.
	 * @return
	 * 		<b>true</b>: wenn alle Knoten des Netzes Teil einer gerichteten Zusammenhangskomponente vom Anfangs-
	 * 				sowie gegengerichtet vom Endknoten aus sind<br>
	 * 		<b>false</b>: sonst
	 */
	public boolean testIfPathPropHolds() {
		if (startNode == null || endNode == null) {
			pathProp.set(false);
			return false;
		}

		pathProp.set(true);
		startNode.setOnPathFromStart(true);
		startNode.componentFromStart();
		endNode.setOnPathFromEnd(true);
		endNode.componentFromEnd();

		for (Place p: places.values()) {
			if (!(p.isOnPathFromStart() && p.isOnPathFromEnd())) {
				pathProp.set(false);}
			p.setOnPathFromStart(false);
			p.setOnPathFromEnd(false);
		}
		for (Transition t: transitions.values()) {
			if (!(t.isOnPathFromStart() && t.isOnPathFromEnd()))
				pathProp.set(false);;
				t.setOnPathFromStart(false);
				t.setOnPathFromEnd(false);
		}
		return pathProp.get();
	}
	/**
	 * Setzt das BooleanProperty deadlockProp auf false wenn eine Transition des Netzes ohne Kontakt aktiviert ist,<br>
	 * auf true sonst.
	 */
	public void testDeadlock() {
		for (Transition temp: transitions.values()) {
			if (temp.activated().get() && !temp.hasContact()) {
				deadlockProp.set(false);
				return;
			}
		}
		deadlockProp.set(true);
	}

	/**
	 * Setzt das BooleanProperty simFinProp auf true wenn die Endstelle existiert und markiert ist,<br>
	 * auf false sonst.
	 */
	public void testFinished() {
		if (endNode != null && endNode.isMarked()) {
			simFinProp.set(true);
			deadlockProp.set(false);
		}
		else
			simFinProp.set(false);
	}

	//**** VALIDITY CHECKS ****
	/**
	 * Nimmt eine ID und überprüft ob sie in einer der das Netz beschreibenden Maps auftaucht (d.h. in 'places', 'transitions', oder 'arcs').
	 * @param id
	 * 		Die ID deren Einzigartigkeit getestet werden soll.
	 * @return
	 * 		<b>true</b>: falls die ID nicht auftaucht<br>
	 * 		<b>false</b> sonst
	 */
	private boolean isUniqueId (String id) {
		if (places.containsKey(id) || transitions.containsKey(id) || arcs.containsKey(id)) {
			return false;
		}
		return true;
	}
	/**
	 * Nimmt einen Ausgangs- und einen Endknoten und überprüft ob es bereits einen Bogen zwischen diesen
	 * im Netz gibt (d.h. in der Map 'arcs').
	 * @param source
	 * 		Der Modellknoten für den getestet werden soll ob es eine Kante gibt, die ihn mit dem Endknoten verbindet.
	 * @param target
	 * 		Der Modellknoten für den getestet werden soll, ob es eine Kante gibt, die ihn mit dem Ausgangsknoten verbindet.
	 * @return
	 * 		<b>true</b>: falls es eine Kante gibt, die vom Ausgangs- zum Endknoten führt<br>
	 * 		<b>false</b> sonst
	 */
	public boolean arcExists(PetriNode source, PetriNode target) {
		for (Arc temp: arcs.values()) {
			if (temp.from == source && temp.to == target) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Nimmt einen Ausgangs- und einen Endknoten und gibt ggf. den Bogen zwischen diesen zurück.
	 * @param source
	 * 		Der Modellknoten der den Ausgangsknoten des gesuchten Bogens beschreibt.
	 * @param target
	 * 		Der Modellknoten der den Endknoten des gesuchten Bogens beschreibt.
	 * @return
	 * 		Den Bogen der den Ausgangsknoten und den Endknoten verbindet,
	 * 		null wenn keiner gefunden wurde.
	 */
	public Arc findArc(PetriNode source, PetriNode target) {
		for (Arc temp: arcs.values()) {
			if (temp.from == source && temp.to == target) {
				return temp;
			}
		}
		return null;
	}

	//**** LOAD AND SAVE ****
	/**
	 * Nimmt ein File-Objekt aus dem ein Netz geladen werden soll, erzeugt eine neue Instanz 
	 * von PNML-Parser und übergibt die File und das Netz als Parameter,
	 * initialisiert und initiiert den Parser.
	 * @param pnmlFile
	 * 		Das File welches die Informationen enthält, die in das Netz geladen werden sollen.
	 * @throws Exception 
	 */
	public void loadNet(final File pnmlFile) throws Exception {
		PNMLParser pnmlParser = new PNMLParser(pnmlFile, this);
		pnmlParser.initParser();
		pnmlParser.parse();
	}
	/**
	 * Nimmt ein File-Objekt in das ein Netz gespeichert werden soll, erzeugt eine neue Instanz von PNML-Parser
	 * und ruft für alle Stellen, Transitionen und Bögen die entsprechenden Schreibefunktionen auf.
	 * @param pnmlFile
	 */
	public void saveNet(final File pnmlFile) {
		PNMLWriter pnmlWriter = new PNMLWriter(pnmlFile);
		pnmlWriter.startXMLDocument();

		for (Place place: places.values()) {
			pnmlWriter.addPlace(place.ID, place.getName(), 
					Integer.toString(place.getXpos()), Integer.toString(place.getYpos()),
					place.getMark());
		}

		for (Transition transition: transitions.values()) {
			pnmlWriter.addTransition(transition.ID, transition.getName(), 
					Integer.toString(transition.getXpos()), Integer.toString(transition.getYpos()));
		}

		for (Arc arc: arcs.values()) {
			pnmlWriter.addArc(arc.ID, arc.from.ID, arc.to.ID);
		}

		pnmlWriter.finishXMLDocument();
	}

	//**** GETTER & SETTER
	/**
	 * @return
	 * Gibt die Menge der Stellen als Map zurück.
	 */
	public Map<String, Place> getPlaces() {
		return places;
	}
	/**
	 * @return
	 * Gibt die Menge der Transitionen als Map zurück.
	 */
	public Map<String, Transition> getTransitions() {
		return transitions;
	}
	/**
	 * @return
	 * Gibt die Menge der Bögen als Map zurück.
	 */
	public Map<String, Arc> getArcs() {
		return arcs;
	}
	/**
	 * @return
	 * Gibt den Startknoten des Workflownetzes zurück.
	 */
	public Place getStartNode() {
		return startNode;
	}
	/**
	 * @return
	 * Gibt den Endknoten des Workflownetzes zurück.
	 */
	public Place getEndNode() {
		return endNode;
	}
	/**
	 * @return
	 * Gibt das Property zurück, welches anzeigt ob ein Startknoten existiert.
	 */
	public BooleanProperty getStartExists() {
		return startExists;
	}
	/**
	 * @return
	 * Gibt das Property zurück, welches anzeigt ob ein Endknoten existiert.
	 */
	public BooleanProperty getEndExists() {
		return endExists;
	}
	/**
	 * @return
	 * Gibt das Property zurück, welches anzeigt ob mehr als ein Startknoten existiert.
	 */
	public BooleanProperty getStartMoreThanOne() {
		return startMoreThanOne;
	}
	/**
	 * @return
	 * Gibt das Property zurück, welches anzeigt ob mehr als ein Endknoten existiert.
	 */
	public BooleanProperty getEndMoreThanOne() {
		return endMoreThanOne;
	}
	/**
	 * @return
	 * Gibt das Property zurück, welches anzeigt ob die Pfadeigenschaft eines Workflownetzes erfüllt ist.
	 */
	public BooleanProperty getPathProperty() {
		return pathProp;
	}

	/**
	 * @return
	 * Gibt das Property zurück, welches anzeigt ob ein Deadlock vorliegt
	 */
	public BooleanProperty getDeadlockProperty() {
		return deadlockProp;
	}
	/**
	 * @return
	 * Gibt das Property zurück, welches anzeigt ob die Endmarkierung erreicht ist
	 */
	public BooleanProperty getSimFinProp() {
		return simFinProp;
	}

}
