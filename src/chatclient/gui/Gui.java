package chatclient.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import chatclient.Launcher;
import chatclient.log.Log;

public class Gui {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		AnmeldeFenster af = new AnmeldeFenster();
		af.setVisible(true);
		af.anmeldebutton.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    			Launcher.name = af.username_feld.getText();
		    		ChatFenster cf = new ChatFenster();
		    		cf.setVisible(true);
		    		af.setVisible(false);
		    		if(af.logging_box.isEnabled() == true) {
		    			Log.init(true);
		    		}
		    }
	    });
	}

}
