package model;

import java.util.ArrayList;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Diese Klasse implementiert die Modellebene einer Transition in einem Petrinetz.
 * Sie erbt von der abstrakten Klasse PetriNode.
 */
public class Transition extends PetriNode {

	//**** FIELDS ****

	/**
	 * Diese Liste enthält die Vorgänger der Transition im Netz. (qua Parametrisierung nur Stellen)
	 */
	public ArrayList<Place> pre = new ArrayList<Place>();
	/**
	 * Diese Liste enthält die Nachfolger der Transition im Netz. (qua Parametrisierung nur Stellen)
	 */
	public ArrayList<Place> post = new ArrayList<Place>();

	/**
	 * Dieses Property beschreibt ob die Transition aktiviert ist.<br>
	 * Es wurde ein BeanProperty verwendet um Datenbindung der visuellen Darstellung zu ermöglichen.
	 */
	private BooleanProperty activated = new SimpleBooleanProperty();
	/** Dieses Property beschreibt, ob die Transition einen Kontakt hat.<br>
	 * Es wurde ein BeanProperty verwendet um Datenbindung der visuellen Darstellung zu ermöglichen.
	 */
	private BooleanProperty contact = new SimpleBooleanProperty();

	//**** CONSTRUCTOR ****
	/**
	 * Der Konstruktor nimmt eine (String-)ID und erzeugt eine neue Instanz mit dieser ID.
	 * @param id
	 */
	public Transition(String id) {
		this.ID = id;
	}

	//**** METHODS ****
	/**
	 * Diese Methode nimmt eine (Modell-)Stelle und fügt sie der Liste der Vorgänger hinzu. (<i>pre</i>)
	 * @param inPlace
	 * 		Die Stelle die der Vorgängerliste hinzugefügt werden soll.
	 */
	public void addToPre(Place inPlace) {
		this.pre.add(inPlace);
	}
	/**
	 * Diese Methode nimmt eine (Modell-)Stelle und fügt sie der Liste der Nachfolger hinzu. (<i>post</i>)
	 * @param inPlace
	 * 		Die Stelle die der Nachfolgerliste hinzugefügt werden soll.
	 */
	public void addToPost(Place inPlace) {
		this.post.add(inPlace);
	}
	/**
	 * Diese Methode nimmt einen (Modell-)Bogen und entfernt ggf. den Knoten mit welchem ihn der Bogen verbindet
	 * aus der Liste der Vorgänger oder Nachfolger oder aus beiden.
	 * @param in
	 * 		Der Bogen für den ggf. Einträge aus den Vorgänger- und Nachfolgerlisten getilgt werden sollen.
	 */	
	public void removeArc(Arc in) {
		if (this.equals(in.from))
			this.post.remove(in.to);
		if (this.equals(in.to))
			this.pre.remove(in.from);
	}
	/**
	 * Diese rekursive Methode ruft alle Nachfolger der Instanz auf und bricht ab, wenn ein Nachfolger schon besucht wurde
	 * und markiert ihn ansonsten als besucht und wird auf allen Nachfolgern des Nachfolgers aufgerufen.<br>
	 * Sie dient dazu die Workflow-Eigenschaft zu überprüfen
	 * und berechnet effektiv eine Zusammenhangskomponente vom Startknoten aus.
	 */
	public void componentFromStart() {
		post.forEach(p -> {
			if (p.isOnPathFromStart())
				return;
			else {
				p.setOnPathFromStart(true);
				p.componentFromStart();
			}
		});
	}
	/**
	 * Diese rekursive Methode ruft alle Vorgänger der Instanz auf und bricht ab, wenn ein Vorgänger schon besucht wurde
	 * und markiert ihn ansonsten als besucht und wird auf allen Vorgängern des Vorgängers aufgerufen.<br>
	 * Sie dient dazu die Workflow-Eigenschaft zu überprüfen
	 * und berechnet effektiv eine Zusammenhangskomponente vom Endknoten aus entgegen der Bogenrichtungen.
	 */
	public void componentFromEnd() {
		pre.forEach(p -> {
			if (p.isOnPathFromEnd())
				return;
			else {
				p.setOnPathFromEnd(true);
				p.componentFromEnd();
			}
		});
	}

	/**
	 * Diese Methode berechnet anhand der Markierungen der Stellen
	 * in der Nachfolgerliste der Transition ob ein Kontakt vorliegt.<br>
	 * Wenn ein Bogen zurück von einer Stelle vorliegt ("Doppelpfeil"), kann diese markiert sein
	 * ohne als Kontakt zu gelten.
	 * @return
	 * 		<b>true</b>: wenn eine Nachfolgerstelle markiert ist, von der kein Bogen zurück vorliegt<br>
	 * 		<b>false</b>: sonst
	 */
	public boolean hasContact() {
		for (Place p: post) {
			if (p.isMarked() && !p.post.contains(this)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Löst eine aktivierte Transition aus, wenn sie keinen Kontakt hat.<br>
	 * Alle Markierungen ihrer Vorgänger werden entfernt und alle ihre Nachfolger erhalten eine Markierung.
	 */
	public void fireTransition() {

		if(activated.get() && !hasContact()) {
			pre.forEach(p -> {
				p.setMark("0");
			});
			post.forEach(p -> {
				p.setMark("1");
			});
		}
	}
	/**
	 * Diese Methode überprüft ob diese Transition als aktiviert gilt.<br>
	 * Wenn sie keine Vorgänger hat oder einer ihrer Vorgänger nicht markiert ist, wird die activated-Property
	 * auf 'false' gesetzt und die Methode abgebrochen.<br>
	 * Andernfalls wird die activated-Property auf 'true' gesetzt und die Transition auf eine Kontaktsituation getestet
	 * und die contact-Property entsprechend gesetzt.
	 */
	public void setActivationStatus() {
		if (pre.isEmpty())
			activated.set(false);
		else {
			for (Place temp: pre) {
				if (!temp.isMarked()) { 
					activated.set(false);
					return;
				}
			}
			activated.set(true);
			if(hasContact()) {
				contact.set(true);
			}
			else
				contact.set(false);
		}
	}

	/**
	 * @return
	 * 		Gibt BooleanProperty 'activated' zurück.
	 */
	public BooleanProperty activated() {
		return activated;
	}
	/**
	 * @return
	 * 		Gibt BooleanProperty 'contact' zurück.
	 */
	public BooleanProperty contact() {
		return contact;
	}

}
