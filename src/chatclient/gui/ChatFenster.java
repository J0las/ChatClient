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

package chatclient.gui;

import java.awt.Color;
import java.text.ParseException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.MaskFormatter;

import chatclient.Launcher;
import chatclient.lib.ConnectionCreator;
import chatclient.lib.Constants;
import chatclient.lib.MessageSender;


@SuppressWarnings("serial")
public class ChatFenster extends JFrame {

  public JPanel contentPane;
  public JButton[] connectionButtons = new JButton[5];  
  private JFormattedTextField ip_feld;
  private JButton connectButton;
  private JTextPane chattext;
  private JTextField send_feld;
  private JLabel connectionName;
  private JTextPane lastPane;

  /**
   * Create the frame.
   */
  public ChatFenster() {
    setTitle("Chat-Client von " + Launcher.name);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(Constants.chatfenster);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    contentPane.setLayout(null);
    setResizable(false);
    //erstellt das leere Fenster in einer unver�nderbarer Gr��e
    
    ImageIcon logo = new ImageIcon(".//res//Logo_ChatClient.jpg");
    setIconImage(logo.getImage());
    //erstellt das Logo des Fensters
    
    connectButton = new JButton("Connect"); 
    connectButton.setBounds(Constants.connectButton);
    contentPane.add(connectButton);
    //erstellt den Connectbutton
    
    JLabel ip_anweisung = new JLabel("IP-Adresse des Empf\u00E4ngers:");
    ip_anweisung.setBounds(Constants.ip_anweisung);
    contentPane.add(ip_anweisung);
    try {
    	MaskFormatter mf = new MaskFormatter("###.###.###.###"); //setzt das Layout der IP
    	ip_feld = new JFormattedTextField(mf);
        ip_feld.setBounds(10, 382, 141, 29);
    	contentPane.add(ip_feld);
    	connectButton.addActionListener(new ConnectionCreator(ip_feld));
        //erstellt das Textfeld, in der die IP-Adresse mit der Maske eingegeben wird
    	
    	chattext = new JTextPane();
    	chattext.setEditable(false);
    	chattext.setBounds(Constants.chattext);
    	chattext.setBackground(new Color(255, 255, 255));
    	contentPane.add(chattext);
    	lastPane = chattext;
    	//hier wird der Text der Chatverlauf eingef�gt
    	
    	JButton sendeButton = new JButton("Senden");
    	sendeButton.setBounds(600, 333, 89, 77);
    	contentPane.add(sendeButton);
    	//erstellt den Sende-Button
    	
    	connectionName = new JLabel();
    	connectionName.setBounds(310, 11, 379, 38);
    	connectionName.enableInputMethods(true);
    	contentPane.add(connectionName);
    	//hier steht der Name dessen, dem man Nachrichten schreibt
    	
    	connectionButtons[0] = new JButton("Keine Verbindung");
    	connectionButtons[0].setBounds(28, 60, 262, 38);
    	contentPane.add(connectionButtons[0]);
    	
    	connectionButtons[1] = new JButton("Keine Verbindung");
    	connectionButtons[1].setBounds(28, 109, 262, 38);
    	contentPane.add(connectionButtons[1]);
    	
    	connectionButtons[2] = new JButton("Keine Verbindung");
    	connectionButtons[2].setBounds(28, 158, 262, 38);
    	contentPane.add(connectionButtons[2]);
    	
    	connectionButtons[3] = new JButton("Keine Verbindung");
    	connectionButtons[3].setBounds(28, 207, 262, 38);
    	contentPane.add(connectionButtons[3]);
    	
    	connectionButtons[4] = new JButton("Keine Verbindung");
    	connectionButtons[4].setBounds(28, 254, 262, 38);
    	contentPane.add(connectionButtons[4]);
    	
    	//erstellt die f�nf Verbindungsbuttons
    	
    	send_feld = new JTextField();
    	send_feld.setBounds(320, 333, 270, 78);
    	contentPane.add(send_feld);
    	send_feld.setColumns(10);
    	sendeButton.addActionListener(new MessageSender(send_feld));
    	//hier werden die zu verschickenen Nachrichten eingegeben
    	
    } catch(ParseException e) {
    	throw new AssertionError();
    }
  }
  public void switchJTextPane(JTextPane pane) {
      lastPane.setVisible(false);
      pane.setVisible(true);
      lastPane = pane;      
  }
  //ersetzt das Chatfenster der alten Verbindung surch das der neuen
  
  public JButton getButton(int index) {
      return connectionButtons[index%5];
  }
  //gibt den Button zur�ck, der die Referenz zu der neuen Verbindung bekommt 
  
  public void setConnectionName(String name) {
      connectionName.setText(name);
  }
  //setzt den Usernamen der Verbindung �ber den Chat
public void resetJTextPane(JTextPane pane) {
    if(lastPane == pane) {
        pane.setVisible(false);
        lastPane = chattext;
        contentPane.remove(pane);
        lastPane.setVisible(true);
        }  
    }
}
