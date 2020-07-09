package chatclient.gui;

import javax.swing.JOptionPane;

public class ErrorFenster {
	
	public static void error(String message){  //Bei Fehlermeldungen wird diese Operation aufgerufen
                JOptionPane.showMessageDialog(null,
                                              message,   //angezeigter Text
                                              "ERROR",   //Titel des Fensters					      
					      JOptionPane.WARNING_MESSAGE);  //Bild einer Warnung    
	}
}