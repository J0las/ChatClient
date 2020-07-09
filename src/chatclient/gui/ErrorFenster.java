package chatclient.gui;

import javax.swing.JOptionPane;

public class ErrorFenster {
	
	public static void error(String error){  //Bei Fehlermeldungen wird diese Operation aufgerufen
                JOptionPane.showMessageDialog(null,
                                              "ERROR: "+error,   //angezeigter Text
                                              "ERROR",   //Titel des Fensters					      
					      JOptionPane.WARNING_MESSAGE);  //Bild einer Warnung    
	}
}