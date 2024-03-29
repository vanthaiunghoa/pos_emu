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
    private ServerSocket inSocket;
    private final CommandInterpreter internalCmdInterpreter;
    private final FXML_LogWindowController internalLogController;
    
    TCPServer(CommandInterpreter cmdInter, int TcpPort, FXML_LogWindowController logger)
    {        
        // Constructor
        internalTcpPort = TcpPort;     
        internalCmdInterpreter = cmdInter;
        internalLogController = logger;
    }
    
    public void StartTCPServer()
    {
        // Start the server
        PosEmuUtils.DisplayLogInfo(internalLogController, "Starting TCP-IP server : " + internalTcpPort);
        
        // Create a socket on the predefined port
        try {
            inSocket = new ServerSocket(internalTcpPort);
        } catch (IOException e)
        {
            PosEmuUtils.DisplayLogError(internalLogController, "Error Starting TCP server:" + e);
        }         
    }
    
    public void RestartTCPServer()
    {
        try {
            inSocket.close();
            StartTCPServer();
        } catch (IOException e)
        {
            PosEmuUtils.DisplayLogError(internalLogController, "Error ReStarting TCP server:" + e);
        }          
    }
    
    public void WaitTCPMessage() throws Exception
    {
        // Start server
        String clientSentence;
        FileInputStream in = null;
        
        try {
            while (true) {
                try (Socket connectionSocket = inSocket.accept()) {
                    BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                    DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                    clientSentence = inFromClient.readLine();
                    if (clientSentence == null) {
                        clientSentence = "WARNING empty received\n";
                    }
                    PosEmuUtils.DisplayLogInfo(internalLogController, "Received: " + clientSentence);
                    
                    // Execute the command
                    internalCmdInterpreter.ExecuteCommand(clientSentence);
                    
                    // Send the response
                    try {
                        in = new FileInputStream("e:\\rsp.json");
                    } catch(FileNotFoundException ex) {
                        PosEmuUtils.DisplayLogError(internalLogController, "File not found - " + ex);
                    }
                    
                    byte[] bytes = new byte[16 * 1024];
                    
                    int count;
                    while ((count = in.read(bytes)) > 0) {
                        PosEmuUtils.DisplayLogInfo(internalLogController, "bytes: " + new String(bytes, "UTF-8"));
                        outToClient.write(bytes, 0, count);
                    }
                    
                    in.close();
                    outToClient.close();
                }
                catch(Exception e) {
                    PosEmuUtils.DisplayLogError(internalLogController, "Network Error: " + e);    
                }
                Thread.sleep(1);
            }
        } catch (InterruptedException e) {
            PosEmuUtils.DisplayLogError(internalLogController, "Network Error: " + e);
        }
    }
}
