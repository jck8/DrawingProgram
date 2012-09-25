import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;


public class MainKeyDispatcher implements KeyEventDispatcher {

	JPanel keyPanel;
	KeyboardFocusManager kfm;
	public MainKeyDispatcher(JPanel kp, KeyboardFocusManager k) {
		keyPanel = kp;
		kfm = k;
	}
	public boolean dispatchKeyEvent (KeyEvent e) {
		int mods = e.getModifiersEx();
		if ((mods & KeyEvent.META_DOWN_MASK) == KeyEvent.META_DOWN_MASK) {
			kfm.redispatchEvent(keyPanel, e);
			return true;
		}
		return false;
	}

}
