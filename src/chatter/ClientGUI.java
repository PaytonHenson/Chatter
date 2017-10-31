package chatter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


/*
 * The Client with its GUI
 */
public class ClientGUI extends JFrame implements ActionListener {

    private final JLabel label;
    // to hold  messages
    private final JTextField tf;
    // to get the list of the users
    private final JButton whoIsIn;
    // for the chat room
    private final JTextArea ta;
    // if it is for connection
    private boolean connected;
    // the Client object
    private Client client;
    //default username
    private String defaultUsername = "Anonymous";

    // Constructor connection receiving a socket number
    ClientGUI(String host, int port) {

        super("Chat Client");

        // The NorthPanel with:
        JPanel northPanel = new JPanel(new GridLayout(2,1));

        // the Label and the TextField
        label = new JLabel("Enter your message below", SwingConstants.CENTER);
        northPanel.add(label);
        tf = new JTextField("");
        tf.setBackground(Color.WHITE);
        northPanel.add(tf);
        add(northPanel, BorderLayout.NORTH);

        // The CenterPanel which is the chat room
        ta = new JTextArea("Welcome to the Chat room\n", 80, 80);
        JPanel centerPanel = new JPanel(new GridLayout(1,1));
        centerPanel.add(new JScrollPane(ta));
        ta.setEditable(false);
        add(centerPanel, BorderLayout.CENTER);

        whoIsIn = new JButton("Who is in");
        whoIsIn.addActionListener(this);

        JPanel southPanel = new JPanel();
        southPanel.add(whoIsIn);
        add(southPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 600);
        setVisible(true);
        tf.requestFocus();
        
        // try creating a new Client with GUI
        client = new Client(host, port, defaultUsername, this);
        // test if we can start the Client
        if(!client.start()) 
                return;
        connected = true;

        whoIsIn.setEnabled(true);
        // Action listener for when the user enter a message
        tf.addActionListener(this);

    }

    // called by the Client to append text in the TextArea 
    void append(String str) {
        ta.append(str);
        ta.setCaretPosition(ta.getText().length() - 1);
    }
    // REFACTOR
    void connectionFailed() {
        whoIsIn.setEnabled(false);
        label.setText("Enter your username below");
        tf.setText("Anonymous");
        // don't react to a <CR> after the username
        tf.removeActionListener(this);
        connected = false;
    }
		
    /*
    * Button or JTextField clicked
    */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        // if it the who is in button
        if(o == whoIsIn) {
            client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));				
            return;
        }

        // ok it is coming from the JTextField (REFACTOR)
        if(connected) {
            // just have to send the message
            String check = tf.getText().split(" ")[0];
            if (check.equals("\\nick")) {
                client.sendMessage(new ChatMessage(ChatMessage.CHANGENAME, tf.getText().split(" ")[1]));
                tf.setText("");
            }
            else {
                client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, tf.getText()));				
                tf.setText("");
            }
            return;
        }

    }

}
