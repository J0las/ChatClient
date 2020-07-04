/*
 *  Copyright (C) 2020  Felix Johannsmann, Johan Bücker
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, version 3, as published by
 *  the Free Software Foundation.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package chatclient.lib;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JFormattedTextField;

import chatclient.Connection;
import chatclient.Connections;

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
                                        Constants.STANDARD_PORT), true);
            con.start();
            Connections.add(con);
        } catch (IOException | NumberFormatException | UnreachableIPException e) {
            return;
        }
    }
}
