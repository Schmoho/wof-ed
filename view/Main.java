package view;


import java.io.File;

import controller.ViewController;
import model.PetriNet;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Starterklasse für das Programm.<br>
 * Implementiert auch Lade- und Speicherfunktionen.
 */
public class Main extends Application {
	/**
	 * Die Stage wird als Attribut festgehalten, um ohne größere Umschweife über den ViewController
	 * Cursor-Wechsel vornehmen zu können.
	 */
	private static Stage stage;
	/**
	 * In jedem Tab kann ein Workflownetz bearbeitet werden.
	 */
	private final static TabPane tabBar = new TabPane();
	/**
	 * Der ViewController bekommt durch diese Klasse die Stage und den Haupt-Frame in dem diese gestaltet wird,
	 * sowie durch Lade-/Speicher- und Tabwechsel-Handler Informationen über das aktuell bearbeitete Netz geliefert.
	 */
	private static ViewController viewContr = new ViewController();
	/**
	 * Für Lade-/Speicherfunktionen.
	 */
	private static FileChooser fileChooser = new FileChooser();

	/**
	 * Die main ruft die FX-Methode Application.launch auf, die als Initalisierung
	 * und Aufhängepunkt für FX-Anwendungen fungiert.
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}
	/**
	 * Diese start-Methode ist der entry-point für FX-Anwendungen.<br>
	 * Sie konstruiert ein neues File-Menü, setzt einen Frame, füllt ihn mit den anfänglichen
	 * Elementen und verteilt ein paar Referenzen an die Stage und den ViewController.
	 */
	public void start(Stage primaryStage) {

		BorderPane mainFrame = new BorderPane();

		MenuBar menuBar = new MenuBar();
		Menu fileMenu = new Menu("File");
		MenuItem newFile = new MenuItem("New");
		newFile.setOnAction(newFileEventHandler);
		MenuItem loadFile = new MenuItem("Load");
		loadFile.setOnAction(loadFileEventHandler);
		MenuItem saveFile = new MenuItem("Save");
		saveFile.setOnAction(saveFileEventHandler);

		fileMenu.getItems().addAll(newFile, loadFile, saveFile);
		menuBar.getMenus().addAll(fileMenu);

		mainFrame.setTop(menuBar);
		mainFrame.setCenter(tabBar);

		stage = primaryStage;

		Scene scene = new Scene(mainFrame, 800, 600);
		viewContr.setSceneReference(scene);
		viewContr.setMainFrame(mainFrame);

		primaryStage.setScene(scene);
		primaryStage.setTitle("Workflow-Netz Editor");
		primaryStage.show();

	}

	//**** FILE MENU ****
	//**** new file ****
	/**
	 * Der EventHandler für den "new file"-Menüeintrag im Hauptfenster.<br>
	 * Öffnet ein neues Tab, weist ihm ein neues Netz zu, registriert den tabChangedHandler,
	 * initialisiert ein neues Pane und wählt das Tab aus.
	 */
	private static EventHandler<ActionEvent> newFileEventHandler = new EventHandler<ActionEvent>(){
		public void handle(ActionEvent t) {
			PNTab tab = new PNTab();
			PetriNet inNet = new PetriNet();
			tab.setOnSelectionChanged(tabChangedHandler);
			tab.setOnClosed(close -> {
				if (tabBar.getTabs().isEmpty())
					viewContr.setCurrentPane(null);
			});
			tab.setContent(viewContr.initializeNewPane());
			tab.setNet(inNet);
			tab.setText("new Workflow");
			viewContr.setCurrentNet(inNet);
			tabBar.getTabs().add(tab);
			tabBar.getSelectionModel().select(tab);
		}
	};
	//**** load file ****
	/**
	 * Der Eventhandler für den "load file"-Menüeintrag im Hauptfenster.<br>
	 * Öffnet ein neues Tab, erzeugt ein neues Netz und ruft die Parser-Routine auf dem Netz auf,
	 * setzt das Tab und veranlasst den ViewController das geladene Netz darzustellen.
	 */
	private static EventHandler<ActionEvent> loadFileEventHandler = new EventHandler<ActionEvent>() {
		public void handle(ActionEvent t) {
			PetriNet inNet = new PetriNet();

			fileChooser.setTitle("Open PNML File");
			File pnmlFile = fileChooser.showOpenDialog(stage);
			if (pnmlFile != null) {
				fileChooser.setInitialDirectory(pnmlFile.getParentFile());
				if(!pnmlFile.getName().endsWith(".pnml")) {	
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error loading.");
					alert.setHeaderText("Invalid file format.");
					alert.setContentText("Can only open files with a PNML-format.");

					alert.showAndWait();

					return;
				}
				try {
					inNet.loadNet(pnmlFile);
				} catch (Exception e) {
					Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error parsing.");
				alert.setHeaderText("Invalid PNML-File.");
				alert.setContentText("Error while parsing the PNML-File.");

				alert.showAndWait();
				return;
				}
				viewContr.setCurrentNet(inNet);

				PNTab tab = new PNTab();
				tab.setContent(viewContr.initializeNewPane());
				tab.setNet(inNet);
				tab.setText(pnmlFile.getName());
				tab.setOnSelectionChanged(tabChangedHandler);
				tab.setOnClosed(close -> {
					if (tabBar.getTabs().isEmpty())
						viewContr.setCurrentPane(null);
				});

				tabBar.getTabs().add(tab);
				tabBar.getSelectionModel().select(tab);

				viewContr.paintNet();
			}
		}
	};
	//**** save file ****
	/**
	 * Der EventHandler für den "save file"-Menüeintrag im Hauptfenster.<br>
	 * Ruft die Writer-Routine auf dem derzeit ausgewählten Netz auf.
	 */
	private static EventHandler<ActionEvent> saveFileEventHandler = new EventHandler<ActionEvent>() {
		public void handle(ActionEvent t) {
			fileChooser.setTitle("Save PNML File");
			File pnmlFile = fileChooser.showSaveDialog(stage);
			try {
				fileChooser.setInitialDirectory(pnmlFile.getParentFile());
			} catch (NullPointerException e) {}
			
			if (viewContr.getCurrentNet() != null) {
				viewContr.getCurrentNet().saveNet(pnmlFile);
				tabBar.getSelectionModel().getSelectedItem().setText(pnmlFile.getName());
			}
			else {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error saving.");
				alert.setHeaderText("No file open.");
				alert.setContentText("There is no file opened that could be saved.");

				alert.showAndWait();
			}
		}
	};
	
	//**** ASSOCIATE TABS, NETS AND PANES ****
	/**
	 * Der EventHandler für das Wechseln von Tabs.<br>
	 * Gibt dem ViewController die Referenzen auf das zu bearbeitende Netz und das damit assoziierte Pane.
	 */
	private static EventHandler<Event> tabChangedHandler = new EventHandler<Event>() {
		public void handle(Event t) {
			viewContr.setCurrentNet(((PNTab) t.getSource()).getNet());
			viewContr.setCurrentPane((PNPane) ((Tab) t.getSource()).getContent());
		}
	};

}
