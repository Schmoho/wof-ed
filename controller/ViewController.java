package controller;

import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import model.Arc;
import model.PetriNet;
import model.Place;
import model.Transition;
import view.ArrowGraphic;
import view.NodeGraphic;
import view.PNPane;
import view.PlaceGraphic;
import view.TransitionGraphic;
public class ViewController {

	//**** FIELDS ****
	/**
	 * Referenz auf die Szene in der die Anwendung dargestellt wird.
	 */
	private Scene scene;
	/**
	 * Referenz auf den obersten Frame der die Szene strukturiert, außerdem root-Element der Szene.
	 */
	private BorderPane mainFrame;
	/**
	 * Referenz auf den aktuellen, also im ausgewählten Tab befindlichen, Pane.
	 */
	private PNPane currentPane;
	/**
	 * Referenz auf das aktuelle, also mit dem ausgewählten Tab assoziierten, Netz.
	 */
	private PetriNet currentNet;

	/**
	 * Referenz auf den Controller für die Events auf dem aktuellen Pane.
	 */
	private PaneEventsController paneController = new PaneEventsController(this);
	/**
	 * Referenz auf den Controller für die Events auf den Knoten.
	 */
	private NodeEventsController nodeEvents = new NodeEventsController(this, paneController);
	/**
	 * Referenz auf den EventHandler für Tastaturbefehle.
	 */
	private KeyEventHandler keyEvents = new KeyEventHandler(this);
	/**
	 * Referenz auf die Schaltfläche der Benutzeroberfläche.
	 */
	private ToolBoard tools = new ToolBoard(this, paneController);

	/**
	 * Faktor der beim Vergrößern und Verkleinern auf die Größen aller Netzelemente multipliziert/dividiert wird.
	 */
	private double sizingFactor = 1.15;

	/**
	 * flag das beim Zeichnen geladener Netze verwendet wird um sicherzustellen, dass wenn keine
	 * Anfangsmarkierung mitgeladen wurde ggf. der Startknoten markiert wird.
	 */
	private boolean initMarkFlag = false;

	//**** MODE TOGGLES ****
	/**
	 * Hebt die aktuelle Markierung des Netzes auf,
	 * setzt das editMode-flag in PaneController und Pane auf true, tauscht die EventHandler am Pane aus
	 * und richtet die Buttons der Benutzeroberfläche passend ein.
	 */
	public void toggleEditMode() {
		currentNet.voidMarking();
		paneController.setEditMode(true);
		currentPane.setInEditMode(true);
		currentPane.removeEventHandler(MouseEvent.ANY, paneController.getSimulationEventHandler());
		currentPane.addEventHandler(MouseEvent.ANY, paneController.getEditEventHandler());
		tools.editButtonToggle();
		tools.updateList();
	}
	/**
	 * Setzt die Anfangsmarkierung des Netzes,
	 * setzt das editMode-flag im PaneController und Pane auf false, tauscht die EventHandler am Pane aus,
	 * hebt die aktuelle Auswahl zur Bearbeitung auf
	 * und richtet die Buttons der Benutzeroberfläche passend ein.
	 */
	public void toggleSimulationMode() {
		paneController.setEditMode(false);
		currentPane.setInEditMode(false);
		currentPane.removeEventHandler(MouseEvent.ANY, paneController.getEditEventHandler());
		currentPane.addEventHandler(MouseEvent.ANY, paneController.getSimulationEventHandler());
		currentPane.clearCurrentlySelected();
		tools.simulationButtonToggle();
		currentNet.setInitialMarking();
		currentNet.testDeadlock();
	}

	//**** SIMULATION ROUTINES ****
	/**
	 * Ruft auf dem Netz die Funktionen auf, welche zusammen die Workflownetz-Eigenschaft konstituieren
	 * um dann ggf. die farbliche Markierung von Start- und Endknoten entsprechend einzurichten und 
	 * die Liste der Statusinformationen in der Benutzeroberfläche zu aktualisieren.
	 */
	public void isWorkflow() {
		try { currentNet.getStartNode().getGraphic().setStroke(PlaceGraphic.STROKE_COLOR); } catch(NullPointerException e) {}
		try { currentNet.getEndNode().getGraphic().setStroke(PlaceGraphic.STROKE_COLOR); } catch(NullPointerException e) {}
		if (currentNet.setStartAndEnd() && currentNet.testIfPathPropHolds()) {
			currentNet.getStartNode().getGraphic().setStroke(PlaceGraphic.START_COLOR);
			currentNet.getEndNode().getGraphic().setStroke(PlaceGraphic.END_COLOR);
		}

		tools.updateList();
	}

	//**** EDITING ROUTINES ****
	/**
	 * Veranlasst das aktuelle Netz eine neue Stelle hinzuzufügen, ihre Koordinaten auf die Werte der Parameter zu
	 * setzen, eine graphische Entsprechung der neuen Stelle zu erzeugen und die graphische Darstellung
	 * der und User-Feedback über die Workfloweigenschaften zu aktualisieren
	 * @param x
	 * 		x-Koordinate für die neue Stelle
	 * @param y
	 * 		y-Koordinate für die neue Stelle
	 */
	public void addNewPlace(double x, double y) {
		Place p = currentNet.newPlace();
		p.setXpos(x);
		p.setYpos(y);
		this.paintPlace(p);

		this.isWorkflow();
	}	
	/**
	 * Veranlasst das aktuelle Netz eine neue Transition hinzuzufügen, ihre Koordinaten auf die Werte der Parameter zu
	 * setzen, eine graphische Entsprechung der neuen Transition zu erzeugen und aktualisiert die Informationen 
	 * über die Workfloweigenschaft des Netzes sowie die Darstellung dieser.
	 * @param x
	 * 		x-Koordinate für die neue Transition
	 * @param y
	 * 		y-Koordinate für die neue Transition
	 */
	public void addNewTransition(double x, double y) {
		Transition t = currentNet.newTransition();
		t.setXpos(x);
		t.setYpos(y);
		this.paintTransition(t);

		this.isWorkflow();
	}
	/***
	 * Veranlasst das Netz einen neuen Bogen zwischen den durch die Parameter bezeichneten Knoten zu erzeugen -
	 * sofern ein solcher noch nicht existiert - erzeugt dann eine graphische Entsprechung des neuen Bogens
	 * und aktualisiert die Informationen über die Workfloweigenschaft des Netzes sowie die Darstellung dieser.
	 * @param from
	 * 		NodeGraphic von der aus der Bogen ausgehen soll
	 * @param to
	 * 		NodeGraphic zu der der Bogen führen soll
	 */
	public void addNewArc(NodeGraphic from, NodeGraphic to) {
		if (!currentNet.arcExists(from.getModel(),	to.getModel())) {
			currentNet.newArc(from.getModel(), to.getModel());
			ArrowGraphic temp = new ArrowGraphic(from, to, currentNet.findArc(from.getModel(), to.getModel()));
			temp.addEventHandler(MouseEvent.ANY, nodeEvents.getArrowHandler());
			currentPane.registerArrow(temp);

			this.isWorkflow();
		}
	}

	//**** PAINTING ROUTINES ****
	/**
	 * Wird aufgerufen wenn ein neues Netz geladen wird um alle Netzelemente zu zeichnen
	 * und ggf. eine mitgeladene Markierung anzuzeigen oder eine Startmarkierung zu setzen.
	 */
	public void paintNet() {
		currentNet.getTransitions().values().forEach(t -> paintTransition(t));

		initMarkFlag = false;
		currentNet.getPlaces().values().forEach(p -> {
			if (p.isMarked())
				initMarkFlag = true;
			paintPlace(p);
			((PlaceGraphic) p.getGraphic()).init();
		});
		currentNet.getArcs().values().forEach(a -> paintArc(a));

		this.isWorkflow();
		if (!initMarkFlag)
			currentNet.setInitialMarking();
	}

	/**
	 * Erzeugt zu einer Modell-Stelle eine graphische Repräsentation und registriert die relevanten EventHandler
	 * für sie und ihr Namensschild.
	 * @param inPlace
	 * 		Modell-Stelle für die eine graphische Repräsentation erzeugt und registriert werden soll
	 */
	public void paintPlace(Place inPlace) {
		PlaceGraphic c = new PlaceGraphic(inPlace.getXpos(), inPlace.getYpos(), inPlace);
		c.addEventHandler(MouseEvent.ANY, nodeEvents.getGeneralEventHandler());
		c.nameTag().addEventHandler(MouseEvent.ANY, nodeEvents.getNameTagHandler());
		currentPane.registerCircle(c);
		inPlace.setGraphic(c);
	}
	/**
	 * Erzeugt zu einer Modell-Transition eine graphische Repräsentation und registriert die relevanten EventHandler
	 * für sie und ihr Namensschild.
	 * @param inTrans
	 * 		Modell-Transition für die eine graphische Repräsentation erzeugt und registriert werden soll
	 */
	public void paintTransition(Transition inTrans) {
		TransitionGraphic r = new TransitionGraphic(inTrans.getXpos(), inTrans.getYpos(), inTrans);
		r.addEventHandler(MouseEvent.ANY, nodeEvents.getGeneralEventHandler());
		r.nameTag().addEventHandler(MouseEvent.ANY, nodeEvents.getNameTagHandler());
		currentPane.registerTransition(r);
		inTrans.setGraphic(r);
	}
	/**
	 * Erzeugt zu einem Modell-Bogen eine graphische Repräsentation und registriert den relevanten EventHandler.
	 * @param inArc
	 * 		Modell-Bogen für den ein graphische Repräsentation erzeugt und registriert werden soll
	 */
	public void paintArc(Arc inArc) {
		ArrowGraphic newArrow = new ArrowGraphic(inArc.from.getGraphic(), inArc.to.getGraphic(), inArc);
		newArrow.addEventHandler(MouseEvent.ANY, nodeEvents.getArrowHandler());
		currentPane.registerArrow(newArrow);
	}

	//**** METHODS ****
	/**
	 * Erzeugt ein neues PetriNetPane, registriert den SimulationsEventHandler darauf
	 * und öffnet gegebenfalls auf der Benutzeroberfläche die Werkzeugleiste.
	 * @return
	 * 		Gibt das neuerzeugt PetriNetPane zurück.
	 */
	public PNPane initializeNewPane() {
		PNPane p = new PNPane();
		p.addEventHandler(MouseEvent.ANY, paneController.getSimulationEventHandler());
		if(mainFrame.getRight() == null) {
			mainFrame.setRight(tools);
		}
		return p;
	}
	/**
	 * Setzt im ViewController die Referenz auf das aktuelle Pane, stellt den PaneController darauf ein
	 * und aktualisiert die Werkzeugleiste entsprechend.<br>
	 * Wenn eine null-Referenz übergeben wird (insb. beim Schließen des letzten Tabs) werden
	 * die Referenzen auf Pane und Netz im Controller genullt und die Werkzeugleiste ausgeblendet.
	 * @param p
	 * 		Referenz auf das als aktuelles zu setzende PetriNetPane.
	 */
	public void setCurrentPane(PNPane p) {
		if (p != null) {
			paneController.unregisterSelectRect();
			currentPane = p;
			paneController.registerSelectRect(p);
			if (currentPane.isInEditMode())
				tools.editButtonToggle();
			else
				tools.simulationButtonToggle();
			tools.updateList();
			activateKeyHandler();
		}
		else {
			currentPane = null;
			currentNet = null;
			mainFrame.setRight(null);
		}
	}
	
	/**
	 * Gibt die Anzahl der zur Zeit zur Bearbeitung ausgewählten Petrinetz-Knoten im aktuellen Pane zurück.
	 * @return
	 * 		Anzahl der ausgewählten Knoten als int
	 */
	public int currentlySelectedNodesCount() {
		return currentPane.getCurrentlySelectedNodes().size();
	}
	/**
	 * Gibt die Anzahl der zur Zeit zur Bearbeitung ausgewählten Petrinetz-Bögen im aktuellen Pane zurück.
	 * @return
	 * 		Anzahl der ausgewählten Bögen als int
	 */
	public int currentlySelectedArrowsCount() {
		return currentPane.getCurrentlySelectedArrows().size();
	}

	/**
	 * Löscht die im aktuellen Pane ausgewählten Netzelemente,
	 * sowohl graphisch als auch auf Modellebene. Dafür werden in Netz und Pane die entsprechenden
	 * Löschungsmethoden aufgerufen.<br>
	 * Anschließend wird die Information über die Workflownetzeigenschaften sowie ihre Darstellung aktualisiert.
	 */
	public void deleteCurrentlySelected() {
		if (!currentPane.getCurrentlySelectedArrows().isEmpty()) {
			currentPane.getCurrentlySelectedArrows().forEach(
					temp -> {
						currentNet.deleteArc(temp.getModel());
						currentPane.deleteArrow(temp);
					});
		}
		if (!currentPane.getCurrentlySelectedNodes().isEmpty()) {
			currentPane.getCurrentlySelectedNodes().forEach(
					temp -> {
						currentNet.deleteNode(temp.getModel());
						currentPane.deleteNode(temp);
					} );
		}
		currentPane.clearCurrentlySelected();
		isWorkflow();
	}

	/**
	 * Multipliziert global die Größe der Netzelemente mit einem konstanten Faktor.
	 */
	public void sizeUp() {
		ArrowGraphic.HEAD_SIZE = ArrowGraphic.HEAD_SIZE * sizingFactor;
		PlaceGraphic.RADIUS.set(PlaceGraphic.RADIUS.get() * sizingFactor);
		TransitionGraphic.HEIGHT.set(TransitionGraphic.HEIGHT.get() * sizingFactor);
		TransitionGraphic.WIDTH.set(TransitionGraphic.WIDTH.get() * sizingFactor);
	}	
	/**
	 * Dividiert global die Größe der Netzelemente durch einen konstanten Faktor.
	 */
	public void sizeDown() {
		ArrowGraphic.HEAD_SIZE = ArrowGraphic.HEAD_SIZE / sizingFactor;
		PlaceGraphic.RADIUS.set(PlaceGraphic.RADIUS.get() / sizingFactor);
		TransitionGraphic.HEIGHT.set(TransitionGraphic.HEIGHT.get() / sizingFactor);
		TransitionGraphic.WIDTH.set(TransitionGraphic.WIDTH.get() / sizingFactor);
	}

	/**
	 * Deaktiviert für die Szene in der die Anwendung dargestellt wird den KeyEventHandler.
	 */
	public void deactivateKeyHandler() {
		this.scene.removeEventHandler(KeyEvent.ANY, keyEvents);
	}
	/**
	 * Aktiviert für die Szene in der die Anwendung dargestellt wird den KeyEventHandler.
	 */
	public void activateKeyHandler() {
		this.scene.addEventHandler(KeyEvent.ANY, keyEvents);
	}

	
	//**** GETTER AND SETTER ****
	/**
	 * Gibt eine Referenz auf das aktuelle Pane, also den Inhalt des aktuellen Tabs.
	 * @return
	 * 		Referenz auf das PetriNetPane des aktuellen Tabs.
	 */
	public PNPane getCurrentPane() {
		return currentPane;
	}
	/**
	 * Setzt die Referenz auf die Szene in der der Workflownetz-Editor dargestellt wird.
	 * @param in
	 * 		Referenz auf die Szene in der die FX-Anwendung dargestellt wird.
	 */
	public void setSceneReference(Scene in) {
		this.scene = in;
	}
	/**
	 * @return
	 * Gibt die Referenz auf die Werkzeugleiste auf der rechten Seite der Szene.<br>
	 * Wird verwendet um beim Feuern der Transitionen die Liste zu updaten.
	 */
	public ToolBoard getToolBoard() {
		return tools;
	}
	
	/**
	 * Gibt den x-Anteil der Translation der im aktuellen Pane dargestellten Netzelemente.<br>
	 * Diese Translation wird für die Implementierung der Navigation verwendet (siehe
	 *  <i>controller.KeyEventsController</i> WASD
	 * und <i>controller.NodeEventsController</i> Drag-Funktionalität)
	 * @return
	 * 		numerischer Wert des x-Anteils der Translation der Netzelemente als content-<i>Group</i>
	 */
	public double getContentTranslateX() {
		return currentPane.getContent().getTranslateX();
	}
	/**
	 * Gibt den y-Anteil der Translation der im aktuellen Pane dargestellten Netzelemente.<br>
	 * Diese Translation wird für die Implementierung der Navigation verwendet (siehe
	 *  <i>controller.KeyEventsController</i> WASD
	 * und <i>controller.NodeEventsController</i> Drag-Funktionalität)
	 * @return
	 * 		numerischer Wert des y-Anteils der Translation der Netzelemente als content-<i>Group</i>
	 */
	public double getContentTranslateY() {
		return currentPane.getContent().getTranslateY();
	}

	/**
	 * Gibt eine Referenz auf das aktuelle Netz, welches der graphischen Simulation und Editierung zugrundeliegt.
	 * @return
	 * 		PetriNet welches aktuell in der Anwendung dargestellt wird, i.d.R. das mit dem aktuellen Tab assoziierte.
	 */
	public PetriNet getCurrentNet() {
		return currentNet;
	}
	/**
	 * Setzt die Referenz auf das aktuelle Netz, welches der graphischen Simulation und Editierung zugrundeliegt.
	 * @param inNet
	 * 		PetriNet welches in der Anwendung verhandelt werden soll.
	 */
	public void setCurrentNet(PetriNet inNet) {
		this.currentNet = inNet;
	}
	/**
	 * Setzt für die Szene in der die Anwendung dargestellt wird den Cursor.
	 * @param c
	 * 		Cursor der dargestellt werden soll.
	 */
	public void setCursor(Cursor c) {
		scene.setCursor(c);
	}
	/**
	 * Setzt die Referenz auf den Frame in dem die Anwendung dargestellt werden soll.
	 * @param mainFrame
	 * 		Referenz auf das BorderPane, welches als root-Element der Szene fungiert.
	 */
	public void setMainFrame(BorderPane mainFrame) {
		this.mainFrame = mainFrame;
	}
}
