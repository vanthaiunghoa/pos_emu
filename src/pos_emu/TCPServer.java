/*
 * POS_EMU
 * This is a Pos Of Sale emulator 
 * TCPServer : TCP/IP listener
 */
package pos_emu;

import java.io.*;
import java.net.*;

public class TCPServer 
{    
    private final int internalTcpPort;
    private ServerSocket welcomeSocket;
    private CommandInterpreter internalCmdInterpreter;
    
    TCPServer(CommandInterpreter cmdInter, int TcpPort)
    {        
        // Constructor
        internalTcpPort = TcpPort;     
        internalCmdInterpreter = cmdInter;
    }
    
    public void StartTCPServer()
    {
        // Start the server
        System.out.println("Starting TCP-IP server : " + internalTcpPort);
        
        // Create a socket on the predefined port
        try {
            welcomeSocket = new ServerSocket(internalTcpPort);
        } catch (Exception e)
        {
            System.out.println("Error Starting TCP server:" + e);
        }         
    }
    
    public void WaitTCPMessage() throws Exception
    {
        String receivedMessage;
        String messageToSend;
        
        // For a frame reception
        Socket connectionSocket = welcomeSocket.accept();
        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        receivedMessage = inFromClient.readLine();
        System.out.println("Received: " + receivedMessage);
        
        // Execute the command
        internalCmdInterpreter.ExecuteCommand(receivedMessage);
        
        // Send OK response
        messageToSend = "OK\n";
        outToClient.writeBytes(messageToSend);
    }
}
