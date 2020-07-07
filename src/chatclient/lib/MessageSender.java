package chatclient.lib;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextField;

import chatclient.Launcher;

public class MessageSender implements ActionListener {
    private JTextField jTextField;
    public MessageSender(JTextField jTextField){
        this.jTextField = jTextField;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Launcher.selectedConnection.sendMessage(jTextField.getText());
        jTextField.setText("");
    }

}
