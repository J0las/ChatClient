package chatclient.gui;

import javax.swing.JOptionPane;

public class ErrorFenster{
        public static void error(){
 
                // Aufruf der statischen Methode showMessageDialog()
                JOptionPane.showMessageDialog(null,
                                              "ERROR",
                                              "Eine Nachricht",					      
					      JOptionPane.WARNING_MESSAGE);
        }
}