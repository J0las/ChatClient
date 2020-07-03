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

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JCheckBox;

public class AnmeldeFenster extends JFrame {

  private JPanel contentPane;
  public JTextField username_feld;
  public JButton anmeldebutton;
  public JCheckBox logging_box;

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          AnmeldeFenster frame = new AnmeldeFenster();
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
  public AnmeldeFenster() {
    setTitle("Chat-Client Anmeldung");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 350, 150);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    contentPane.setLayout(null);
    setResizable(false);
    
    username_feld = new JTextField();
    username_feld.setBounds(78, 11, 200, 20);
    username_feld.setColumns(10);
    contentPane.add(username_feld);
    
    anmeldebutton = new JButton("Anmelden");
    anmeldebutton.setBounds(118, 47, 120, 23);
    contentPane.add(anmeldebutton);
    
    JLabel nametextfeld = new JLabel("Name:");
    nametextfeld.setBounds(22, 5, 46, 33);
    contentPane.add(nametextfeld);
    
    logging_box = new JCheckBox("Protokoll");
    logging_box.setBounds(141, 77, 97, 23);
    contentPane.add(logging_box);
  }
}