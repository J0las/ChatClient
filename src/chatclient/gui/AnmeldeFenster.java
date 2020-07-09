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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import chatclient.Launcher;
import chatclient.lib.Constants;
import chatclient.log.Log;

@SuppressWarnings("serial")
public class AnmeldeFenster extends JFrame {

  private JPanel contentPane;
  private JTextField username_feld;
  private JButton anmeldebutton;
  private JCheckBox logging_box;

  /**
   * Create the frame.
   */
  public AnmeldeFenster() {
    setTitle("Chat-Client Anmeldung");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 350, 150);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    contentPane.setLayout(null);
    setResizable(false);
    //erstellt das leere Fenster in einer unver�nderbarer Gr��e
    
    ImageIcon logo = new ImageIcon(".//res//Logo_ChatClient.jpg");
    setIconImage(logo.getImage());
   //erstellt das Logo des Fensters
    
    username_feld = new JTextField();
    username_feld.setBounds(Constants.username_feld);
    username_feld.setColumns(10);
    contentPane.add(username_feld);
    //erstellt das Feld, in den man dan Usernamen eingibt 
    
    anmeldebutton = new JButton("Anmelden");
    anmeldebutton.setBounds(Constants.anmeldebutton);
    contentPane.add(anmeldebutton);
    //erstellt den Button zum Anmelden    
    
    JLabel nametextfeld = new JLabel("Name:");
    nametextfeld.setBounds(Constants.nametextfeld);
    contentPane.add(nametextfeld);
    
    logging_box = new JCheckBox("Protokoll");
    logging_box.setBounds(Constants.logging_box);
    contentPane.add(logging_box);
    //erstellt die Checkbox, ob Logging aktiviert werden soll
    
    anmeldebutton.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {  //wird aufgerufen, wenn der Anmeldebutton gedr�ckt wird
            Launcher.name = username_feld.getText();
            Log.init(logging_box.isSelected());
            Launcher.loggedIn = true;   
        }
        
    });
  }
}