package chatclient.gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;

public class AnmeldeFenster extends JFrame {

  private JPanel contentPane;
  private JTextField textField;

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
    setBounds(100, 100, 350, 200);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    contentPane.setLayout(null);
    setResizable(false);
    
    textField = new JTextField();
    textField.setBounds(78, 11, 200, 20);
    textField.setColumns(10);
    contentPane.add(textField);
    
    JButton anmeldebutton = new JButton("Anmelden");
    anmeldebutton.setBounds(118, 47, 120, 23);
    contentPane.add(anmeldebutton);
    
    JLabel nametextfeld = new JLabel("Name:");
    nametextfeld.setBounds(22, 5, 46, 33);
    contentPane.add(nametextfeld);
  }
}