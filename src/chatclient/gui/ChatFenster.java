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

import java.awt.EventQueue;
import java.text.ParseException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import chatclient.Launcher;
import chatclient.lib.ConnectionCreator;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JFormattedTextField;

import javax.swing.text.MaskFormatter;


@SuppressWarnings("serial")
public class ChatFenster extends JFrame {

  private JPanel contentPane;
  public JFormattedTextField ip_feld;
  public JButton connectButton;

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          ChatFenster frame = new ChatFenster();
          frame.setVisible(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Create the frame.
   */
  public ChatFenster() {
    setTitle("Chat-Client von " + Launcher.name);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 450, 300);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    contentPane.setLayout(null);
    setResizable(false);
    
    connectButton = new JButton("Connect");
    connectButton.setBounds(293, 184, 141, 77);
    contentPane.add(connectButton);
    
    JLabel ip_anweisung = new JLabel("IP-Adresse des Empf\u00E4ngers:");
    ip_anweisung.setBounds(10, 184, 273, 38);
    contentPane.add(ip_anweisung);
    try {
    	MaskFormatter mf = new MaskFormatter("###.###.###.###");
    	ip_feld = new JFormattedTextField(mf);
        ip_feld.setBounds(20, 224, 263, 29);
    	contentPane.add(ip_feld);
    	connectButton.addActionListener(new ConnectionCreator(ip_feld));
    } catch(ParseException e) {
    	e.printStackTrace();
    }
    
  }
}
