package ca.bigcattech.MediaDB.gui.interfaces;

import java.awt.event.KeyEvent;

public interface IKeyListener {
	
	void keyTyped(KeyEvent e, boolean control);
	void keyPressed(KeyEvent e, boolean control);
	void keyReleased(KeyEvent e, boolean control);
	
}
