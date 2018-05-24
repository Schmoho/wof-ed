package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/**
 * Implementiert die Werkzeugleiste für die Benutzeroberfläche des Workflownetzeditor.
 */
class ToolBoard extends GridPane {

	//**** FIELDS ****
	/**
	 * Referenz auf den ViewController.
	 */
	private static ViewController viewContr;
	/**
	 * Referenz auf den EventController für das PetriNetPane.
	 */
	private static PaneEventsController paneEventsContr;

	/**
	 * ListView zur Darstellung von Status-Informationen über das aktuell zu bearbeitende Netz.
	 */
	private ListView<String> list = new ListView<String>();
	/**
	 * Liste der Einträge in die Liste der Status-Informationen über das aktuell zu bearbeitende Netz.
	 */
	private ObservableList<String> items = FXCollections.observableArrayList();
	/**
	 * Button der den Simulations-Modus auslöst.
	 */
	private ToggleButton simulation = new ToggleButton("Simulation");
	/**
	 * Button der den Edit-Modus auslöst.
	 */
	private ToggleButton edit = new ToggleButton("Edit");
	/**
	 * Button der einen "Pinsel" auslöst mit dem sich im Edit-Modus neue Stellen platzieren lassen.
	 */
	private ToggleButton placeBrush = new ToggleButton("Place", new Circle(8, Color.BLUE));
	/**
	 * Button der einen "Pinsel" auslöst mit dem sich im Edit-Modus neue Transitionen platzieren lassen.
	 */
	private ToggleButton transBrush = new ToggleButton("Transition", new Rectangle(10, 10, Color.ORANGE));
	/**
	 * Button mit dem sich ein Netz im Simulationsmodus auf die Anfangsmarkierung zurücksetzen lässt.
	 */
	private Button rewind = new Button("rewind");
	/**
	 * Toggle-Gruppe die sicherstellt, dass erkennbar immer entweder Simulations- oder Edit-Modus ausgewählt sein muss.
	 */
	private final ToggleGroup simEd = new ToggleGroup();
	/**
	 * Toggle-Gruppe die sicherstellt, dass erkennbar immer nur entweder der Stellen- oder der Transitions-Pinsel
	 * ausgewählt sein kann.
	 */
	private final ToggleGroup plaTra = new ToggleGroup();
	
	/*
	 * Diese Strings werden verwendet als Objekte der Liste der Statusinformationen.
	 */
	private static String noStart = "no start node";
	private static String noEnd = "no end node";
	private static String moreStart = "more than one start node";
	private static String moreEnd = "more than one end node";
	private static String pathProp = "not all nodes are on a path from start to end";
	private static String deadlock = "a deadlock has occurred";
	private static String simFinish = "Simulation completed successfully";
	
	//**** CONSTRUCTORS ****
	/**
	 * Der Konstruktor baut die graphische Oberfläche des ToolBoards zusammen
	 * und registriert die mit den Buttons assoziierten Aktionen als EventHandler.
	 * @param v
	 * 		Referenz auf den ViewController.
	 */
		public ToolBoard(ViewController v, PaneEventsController p) {
			viewContr = v;
			paneEventsContr = p;

			simulation.setToggleGroup(simEd);
			edit.setToggleGroup(simEd);
			placeBrush.setToggleGroup(plaTra);
			transBrush.setToggleGroup(plaTra);

			simEd.selectedToggleProperty().addListener((obs, oldV, newV) -> {
				if (newV == null) {
					simEd.selectToggle(simulation);
					if (viewContr.getCurrentPane() != null) {
						viewContr.toggleSimulationMode();
					}
				}
			});

			//Alle EventHandler für die Buttons.
			edit.setOnAction(e -> {
				if (viewContr.getCurrentPane() != null) {
					viewContr.toggleEditMode();
				}
			});
			simulation.setOnAction(e -> {
				if (viewContr.getCurrentPane() != null) {
					viewContr.toggleSimulationMode();
				}
			});
			placeBrush.setOnAction(e -> { 
				if (viewContr.getCurrentPane() != null)
					paneEventsContr.togglePlaceBrush();
			});
			transBrush.setOnAction(e -> { 
				if (viewContr.getCurrentPane() != null)
					paneEventsContr.toggleTransitionBrush();
			});

			Button sizeUp = new Button("+");
			sizeUp.setOnAction(e -> {
				if (viewContr.getCurrentPane() != null)
					viewContr.sizeUp();
			});
			Button sizeDown = new Button("-");
			sizeDown.setOnAction(e -> {
				if (viewContr.getCurrentPane() != null)
					viewContr.sizeDown();
			});
			rewind.setOnAction(e -> {
				if (viewContr.getCurrentNet() != null)
				viewContr.getCurrentNet().setInitialMarking();
			});

			//Stellt sicher, dass die Elemente den zur Verfügung stehenden Platz ausnutzen.
			simulation.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
			rewind.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
			edit.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
			placeBrush.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
			transBrush.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
			sizeUp.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
			sizeDown.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
			list.setMaxSize(Double.MAX_VALUE, 200);

			//Stellt sicher, dass die Spalten gleichmäßig viel Platz bekommen.
			int numColumns = 2 ;
			for (int i = 0 ; i < numColumns; i++ ) {
				ColumnConstraints constraint = new ColumnConstraints();
				constraint.setFillWidth(true);
				constraint.setHgrow(Priority.ALWAYS);
				this.getColumnConstraints().add(constraint);
			}
			
			this.add(simulation, 0, 0, 2, 1);
			this.add(rewind, 0, 1, 2, 1);
			this.add(list, 0, 2, 2, 4);
			this.add(edit, 0, 7, 2, 1);
			this.add(placeBrush, 0, 8, 1, 1);
			this.add(transBrush, 1, 8, 1, 1);
			this.add(sizeUp, 0, 9, 1, 1);
			this.add(sizeDown, 1, 9, 1, 1);
			
			list.setItems(items);

		}
	
	/**
	 * Wenn der Simulations-Modus ausgewählt wird, werden die Pinsel deaktiviert und der Rewind-Button aktiviert.
	 */
	public void simulationButtonToggle() {
		simEd.selectToggle(simulation);
		if (viewContr.getCurrentPane() != null) {
			plaTra.selectToggle(null);
			rewind.setDisable(false);
			placeBrush.setDisable(true);
			transBrush.setDisable(true);
		}
	}
	/**
	 * Wenn der Edit-Modus ausgewählt wird, werden die Pinsel aktiviert und der Rewind-Button deaktiviert.
	 */
	public void editButtonToggle() {
		simEd.selectToggle(edit);
		if (viewContr.getCurrentPane() != null) {
			plaTra.selectToggle(null);
			rewind.setDisable(true);
			placeBrush.setDisable(false);
			transBrush.setDisable(false);
		}
	}

	/**
	 * Fügt genau die dem Zustand des Modellnetzes entsprechenden Statusinformationen in die Liste
	 * über die Statusinformationen ein.
	 */
	public void updateList() {
		items.clear();
		if (!viewContr.getCurrentNet().getStartExists().get()) {
			items.add(noStart);
		}
		if (!viewContr.getCurrentNet().getEndExists().get()) {
			items.add(noEnd);
		}
		if (viewContr.getCurrentNet().getStartMoreThanOne().get()) {
			items.add(moreStart);
		}
		if (viewContr.getCurrentNet().getEndMoreThanOne().get()) {
			items.add(moreEnd);
		}
		if (!viewContr.getCurrentNet().getPathProperty().get()) {
			items.add(pathProp);
		}
		if (viewContr.getCurrentNet().getDeadlockProperty().get() && !paneEventsContr.isEditMode()) {
			items.add(deadlock);
		}
		if (viewContr.getCurrentNet().getSimFinProp().get() && !paneEventsContr.isEditMode()) {
			items.add(simFinish);
		}
	}
}