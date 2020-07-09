package chatclient.gui;

import javax.swing.JOptionPane;

import chatclient.Launcher;

public class ErrorFenster {
    
	public static void error(String error){  //Bei Fehlermeldungen wird diese Operation aufgerufen
                JOptionPane.showMessageDialog(Launcher.chatFenster,
                                              "ERROR: "+error,   //angezeigter Text
                                              "ERROR",   //Titel des Fensters					      
					      JOptionPane.WARNING_MESSAGE);  //Bild einer Warnung    
	}
}