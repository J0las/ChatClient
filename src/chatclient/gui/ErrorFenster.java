package chatclient.gui;

import javax.swing.JOptionPane;

public class ErrorFenster {
	
	public static void error(){  //Bei Fehlermeldungen wird diese Operation aufgerufen
                JOptionPane.showMessageDialog(null,
                                              "ERROR",   //angezeigter Text
                                              "ERROR",   //Titel des Fensters					      
					      JOptionPane.WARNING_MESSAGE);  //Bild einer Warnung    
	}
}