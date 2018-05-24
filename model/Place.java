package model;

import java.util.ArrayList;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
/**
 * Diese Klasse implementiert die Modellebene für eine Stelle in einem Petrinetz.
 * Sie erbt von der abstrakten Klasse PetriNode.
 */
public class Place extends PetriNode {

	//**** FIELDS ****
	/**
	 * Diese Liste enthält die Vorgänger der Stelle im Netz. (qua Parametrisierung nur Transitionen)
	 */
	public ArrayList<Transition> pre = new ArrayList<Transition>();
	/**
	 * Diese Liste enthält die Nachfolger der Stelle im Netz. (qua Parametrisierung nur Transitionen)
	 */
	public ArrayList<Transition> post = new ArrayList<Transition>();
	/**
	 * Dieses StringProperty zeigt an ob die Stelle markiert ist.<br>
	 * Es wurde ein BeanProperty verwendet um Datenbindung der visuellen Darstellung zu ermöglichen.
	 */
	private StringProperty mark = new SimpleStringProperty();


	//**** CONSTRUCTOR ****
	/**
	 * Der Konstruktor nimmt eine (String-)ID und erzeugt eine neue unmarkierte Instanz mit dieser ID.
	 */
	public Place(String id) {
		this.ID = id;
		this.mark.set("0");
	}

	//**** METHODS ****
	/**
	 * Diese Methode nimmt eine (Modell-)Transition und fügt sie der Liste der Vorgänger hinzu. (<i>pre</i>)
	 * @param inTrans
	 * 		Die Transition die der Vorgängerliste hinzugefügt werden soll.
	 */
	public void addToPre(Transition inTrans) {
		this.pre.add(inTrans);
	}
	/**
	 * Diese Methode nimmt eine (Modell-)Transition und fügt sie der Liste der Nachfolger hinzu. (<i>post</i>)
	 * @param inTrans
	 * 		Die Transition die der Nachfolgerliste hinzugefügt werden soll.
	 */
	public void addToPost(Transition inTrans) {
		this.post.add(inTrans);
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
			if (p.isOnPathFromStart()) {
				return; }
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
	 * und berechnet effektiv eine Zusammenhangskomponente vom Endknoten aus entgegegen der Bogenrichtungen.
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
	 * Diese Methode gibt aus, ob die Stelle markiert ist.
	 * @return
	 * 		<b>true</b>: wenn die Stelle markiert ist (!= "0")<br>
	 * 		<b>false</b>: sonst
	 */
	public boolean isMarked() {
		if (mark.get().contentEquals("0"))
			return false;

		return true;
	}


	//**** GETTER AND SETTER ****

	/**
	 * Die Methode nimmt einen String und weist ihn der mark-Property zu,
	 * danach ruft die für alle Vorgänger- und Nachfolger-Transitionen
	 * die Methode setActivationStatus auf.<br>
	 * Sie wird innerhalb des Simulationsmodus verwendet und insbesondere
	 * beim Feuern einer Transition (siehe model.Transition) aufgerufen.
	 * @param m
	 * 		Der String mit dem die Stelle markiert werden soll.<br>("0" beschreibt eine nicht aktivierte Transition,
	 * 		alles andere eine aktivierte (zur Zeit wird nur "1" verwendet))
	 */
	public void setMark(String m) {
		this.mark.set(m);

		this.pre.forEach(t -> {
			t.setActivationStatus();
		});
		this.post.forEach(t -> {
			t.setActivationStatus();
		});
	}

	/**
	 * @return
	 * 		Gibt StringProperty 'mark' zurück.
	 */
	public StringProperty markProperty() {
		return mark;
	}	

	/**
	 * @return
	 * Gibt den String-Wert der Markierung aus.<br>
	 * Wird ausschließlich beim Speichern des Netzes für den PNML-Writer benötigt.
	 */
	public String getMark() {
		return this.mark.get();
	}

}
