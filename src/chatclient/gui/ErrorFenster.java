package chatclient.gui;

import javax.swing.JOptionPane;

public class ErrorFenster{
        public static void error(){  //Bei Fehlermeldungen wird diese Operation aufgerufen
                 // Aufruf der statischen Methode showMessageDialog()
                JOptionPane.showMessageDialog(null,
                                              "ERROR",
                                              "ERROR",					      
					      JOptionPane.WARNING_MESSAGE);
        }
}