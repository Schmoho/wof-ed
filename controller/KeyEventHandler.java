package controller;

import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.input.KeyEvent;

/**
 * Implementiert den EventHandler für die Keyboard-Bedienung des Workflownetz-Editors

 */
class KeyEventHandler implements EventHandler<KeyEvent> {
	/**
	 * Referenz auf den ViewController
	 */
	private static ViewController viewContr;

	/**
	 * flag um kontinuierliches Scrollen durch Gedrückthalten zu ermöglichen
	 */
	private static boolean wPressed = false;
	/**
	 * flag um kontinuierliches Scrollen durch Gedrückthalten zu ermöglichen
	 */
	private static boolean sPressed = false;
	/**
	 * flag um kontinuierliches Scrollen durch Gedrückthalten zu ermöglichen
	 */
	private static boolean aPressed = false;
	/**
	 * flag um kontinuierliches Scrollen durch Gedrückthalten zu ermöglichen
	 */
	private static boolean dPressed = false;

	/**
	 * Nimmt eine Referenz auf den ViewController.
	 * @param v
	 * 		Referenz auf den ViewController
	 */
	public KeyEventHandler(ViewController v) {
		viewContr = v;
	}
	
	@Override
	/**
	 * Setzt die Scrolling-Flags für Navigation mit WASD und startet ggf. den AnimationTimer,
	 * ruft für 'E' den Edit-Mode, für 'Q' den Simulations-Mode
	 * und für 'Entf' Löschen der ausgewählten Elemente auf dem ViewController auf.
	 */
	public void handle(KeyEvent e) {
		if (e.getEventType() == KeyEvent.KEY_RELEASED) {
			if (viewContr.getCurrentPane() != null) {
				switch (e.getCode()) {
				case W: wPressed = false; break;
				case A: aPressed = false; break;
				case S: sPressed = false; break;
				case D: dPressed = false; break;
				default:
					break;
				}
				if (!(wPressed || aPressed || sPressed || dPressed))
				{
					timer.stop();
				}
			}
		}

		else if (e.getEventType() == KeyEvent.KEY_PRESSED) {
			if (viewContr.getCurrentPane() != null) {
				switch (e.getCode()) {

				case W: wPressed = true; break;
				case A: aPressed = true; break;
				case S: sPressed = true; break;
				case D: dPressed = true; break;

				case E: viewContr.toggleEditMode();	break;
				case Q: viewContr.toggleSimulationMode();	break;
				case DELETE: viewContr.deleteCurrentlySelected(); break;

				default:
					break;
				}

				if (wPressed || aPressed || sPressed || dPressed)
					timer.start();
			}
		}
	}
	
	/**
	 * Wird gestartet wenn einer der scrolling-keys gedrückt wird und gestoppt wenn keiner
	 * der scrolling-keys mehr gedrückt ist.<br>
	 * Navigation funktioniert wie beim Pannen durch Translation der dargestellten Netzelemente.
	 */
	private static AnimationTimer timer = new AnimationTimer() {
		@Override
		public void handle(long time) {
			Group scrollingContent = viewContr.getCurrentPane().getContent();
			if (wPressed) {
				scrollingContent.setTranslateY(scrollingContent.getTranslateY() + 20);
			}
			if (aPressed) {
				scrollingContent.setTranslateX(scrollingContent.getTranslateX() + 20);
			}
			if (sPressed) {
				scrollingContent.setTranslateY(scrollingContent.getTranslateY() - 20);
			}
			if (dPressed) {
				scrollingContent.setTranslateX(scrollingContent.getTranslateX() - 20);
			}
		}

	};

}
