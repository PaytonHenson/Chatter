package chatter;

import java.net.*;
import java.io.*;

public class Client  {

    // for I/O
    private ObjectInputStream sInput;		// to read from the socket
    private ObjectOutputStream sOutput;		// to write on the socket
    private Socket socket;

    //GUI
    private ClientGUI cg;

    // the server, the port and the username
    private String server, username;
    private int port;
    
//  

    Client(String server, int port, String username, ClientGUI cg) {
        this.server = server;
        this.port = port;
        this.username = username;
        this.cg = cg;
    }
	
    /*
     * To start the dialog
     */
    public boolean start() {
        // try to connect to the server
        try {
            socket = new Socket(server, port);
        } 
        // if it failed not much I can do
        catch(IOException ec) {
            display("Error connectiong to server:" + ec);
            return false;
        }

        String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
        display(msg);

        /* Creating both Data Stream */
        try
        {
            sInput  = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (IOException eIO) {
            display("Exception creating new Input/output Streams: " + eIO);
            return false;
        }

        // creates the Thread to listen from the server 
        new ListenFromServer().start();
        // Send our username to the server this is the only message that we
        // will send as a String. All other messages will be ChatMessage objects
        try
        {
            sOutput.writeObject(username);
        }
        catch (IOException eIO) {
            display("Exception doing login : " + eIO);
            disconnect();
            return false;
        }
        // success we inform the caller that it worked
        return true;
    }

    
    private void display(String msg) {
        cg.append(msg + "\n"); // append to the ClientGUI JTextArea (or whatever)
    }
	
    /*
     * To send a message to the server
     */
    void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        }
        catch(IOException e) {
            display("Exception writing to server: " + e);
        }
    }

    /*
     * When something goes wrong
     * Close the Input/Output streams and disconnect not much to do in the catch clause
     */
    private void disconnect() {
        try { 
            if(sInput != null) sInput.close();
        }
        catch(IOException e) {} // not much else I can do
        try {
            if(sOutput != null) sOutput.close();
        }
        catch(IOException e) {} // not much else I can do
        try{
            if(socket != null) socket.close();
        }
        catch(IOException e) {} // not much else I can do

        // inform the GUI
        if(cg != null)
            cg.connectionFailed();

    }
    

    /*
     * a class that waits for the message from the server and append them to the JTextArea
     */
    class ListenFromServer extends Thread {

        @Override
        public void run() {
            while(true) {
                try {
                    String msg = (String) sInput.readObject();
                    cg.append(msg);
                }
                catch(IOException e) {
                    display("Server has closed the connection: " + e);
                    if(cg != null) 
                        cg.connectionFailed();
                    break;
                }
                catch(ClassNotFoundException e2) {}
            }
        }
    }
    
    // to start the whole thing the server
    public static void main(String[] args) {
        
        String hostname;
        int portNumber;
        
        switch(args.length) {
            case 2: 
                try {
                    portNumber = Integer.parseInt(args[1]);
                    hostname = args[0];
                }
                catch (NumberFormatException e) {
                    System.out.println("Invalid port number.");
                    System.out.println("Usage is: > java Client [hostname] [portNumber]");
                    return;
                }
                break;
            default:
                System.out.println("Usage is: > java Client [hostname] [portNumber]");
                return;
        }
        
        new ClientGUI(hostname, portNumber);
    }
}

