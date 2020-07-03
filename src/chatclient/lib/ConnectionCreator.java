package chatclient.lib;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JFormattedTextField;

import chatclient.Connection;
import chatclient.Connections;
import chatclient.Launcher;

public class ConnectionCreator implements ActionListener {

    private JFormattedTextField field;
    
	public ConnectionCreator(JFormattedTextField field) {
	    this.field = field;
	}

    @Override
    public void actionPerformed(ActionEvent event) {
        String ipString = field.getText();
        System.out.println(ipString);
        String[] subIP = ipString.split("\\.");
        byte[] ipBytes = new byte[4];
        try {
        for(int i=0;i<4;i++) {
            ipBytes[i] = (byte)(Integer.parseInt(subIP[i]));
        }
            InetAddress ip = InetAddress.getByAddress(ipBytes);
            if(!ip.isReachable(1000)) throw new UnreachableIPException(ip);
            Connection con = new Connection(
                                new Socket(ip,
                                        Constants.STANDARD_PORT),
                                Launcher.name, true);
            con.start();
            Connections.add(con);
        } catch (IOException | NumberFormatException | UnreachableIPException e) {
            return;
        }
    }
}
