package chatclient.gui;

import javax.swing.JOptionPane;

import chatclient.Launcher;

public class ErrorFenster{
        public static synchronized void error(String message){  //Bei Fehlermeldungen wird diese Operation aufgerufen
                JOptionPane.showMessageDialog(Launcher.chatFenster,
                                              message,   //angezeigter Text
                                              "ERROR",   //Titel des Fensters					      
					      JOptionPane.WARNING_MESSAGE);  //Bild einer Warnung
        }
}