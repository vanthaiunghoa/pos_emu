/*
 * POS_EMU
 * This is a Pos Of Sale emulator 
 *
 * This is the command interpreter 
 */
package pos_emu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
// For GSON library (JSON parse)
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.text.Font;
import javafx.stage.WindowEvent;

/**
 *
 * @author balacahan
 */
public class Pos_emu extends Application {
    // Constant for port to listen
    public int TCP_IP_LISTENER_PORT = 8000;
    // Configuration file name
    final String param_json_path = "src/pos_emu/config/param.json";
    // FXML Document and controller
    FXMLDocumentController ihmController;
    private CommandInterpreter internalCommandInterpreter;
    private PosEmuEngine internalPosEmuEngine;
    
    // Parameters from configuration file
    private ParamConfigFile config_param_data;
    
    @Override
    public void start(Stage stage) throws Exception {
        //Set up instance instead of using static load() method
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
        Parent root = loader.load();

        // First display workping directory
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
               
        //Now we have access to getController() through the instance... don't forget the type cast
        ihmController = (FXMLDocumentController)loader.getController();
        
        // Create instance of CommandInterpreter
        internalCommandInterpreter = new CommandInterpreter(ihmController);
        
        // Load the custom font
        Font.loadFont(Pos_emu.class.getResource("/pos_emu/font/Monospace.ttf").toExternalForm(),16);
        
        // Create the scene
        Scene scene = new Scene(root);
        // Window without Minimize, Maximize buttons
        stage.initStyle(StageStyle.DECORATED);
        // Window not resizable
        stage.setResizable(false);
      
        // Adding a icon 
        Image appliIcon = new Image(Pos_emu.class.getResourceAsStream( "resource/icon.png" ));
        stage.getIcons().add(appliIcon); 
        
        // Read Configuration file 
        config_param_data = ReadJsonConfigFile();

        // Create instante of Engine
        internalPosEmuEngine = new PosEmuEngine(ihmController, config_param_data);

        // Set operations when window is closed
        stage.setOnCloseRequest((WindowEvent we) -> {
            System.out.println("Stage is closing: " + we);
            Platform.exit();
            System.exit(0);
        });
                
        // The event listener is the thread which waits for events like key press, card insertion, etc.
        StartEventListener(ihmController, internalPosEmuEngine);
        
        // Start the terminal boot
        StartPOSBoot();        
        
        // Start the IDLE Screen
        internalPosEmuEngine.StartEngine(PosEnums.State.STATE_IDLE);
        
        // Add the scene to the stage and launch the stage
        stage.setScene(scene);
        stage.setTitle("INGENICO POS EMULATOR");
        stage.show();
    }

    /**
     * Read the configuration file : param.json
     * 
     * @return ParamConfigFile parameters coming from the file
     * 
     * @throws java.io.FileNotFoundException
     */
    private ParamConfigFile ReadJsonConfigFile() throws FileNotFoundException
    {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(param_json_path));        
        } catch(FileNotFoundException e) 
        {
            System.out.println("Configuration File not found ! - error " + e);
        }
        
        // Parse the JSON file
        Gson gson = new Gson();
        return gson.fromJson(bufferedReader, ParamConfigFile.class);        
    }
            
    /**
     * Start the POS boot sequence
     */
    @SuppressWarnings("SleepWhileInLoop")
    private void StartPOSBoot()
    {
        // Set font to white
        ihmController.PosScreen.setStyle("-fx-background-color:white");
        
        // Start TCP/IP listener
        int port = Integer.parseInt(config_param_data.GetPosPort());
        TCPServer tcpListener = new TCPServer(internalCommandInterpreter, port);
        tcpListener.StartTCPServer();
        
        // Wait for a command
        new Thread(() -> {
            do {
                try {
                    // Infinite loop to be always listening 
                    System.out.println("Wait on TCP listener (" + port + ")");
                    tcpListener.WaitTCPMessage();
                    
                    // Pause for 100 ms between each thread, i.e. each command
                    Thread.sleep(1);
                } catch (Exception e) {
                    System.out.println("Network Error: " + e);
                    
                    // Restart TCP/IP listener                   
                    tcpListener.RestartTCPServer();
                }                
            } while (true);
        }).start();
    }

    /**
     * Waits for an event
     */
    private void StartEventListener(FXMLDocumentController ihmController, PosEmuEngine internalPosEmuEngine) {
       // Wait for a command
        new Thread(new Runnable() {
            @Override
            public void run() {
                do {
                    try {
                        // Check if an event is available
                        PosEnums.PosEvent theEvent = ihmController.IsEventAvailable();
                        if (theEvent != PosEnums.PosEvent.NO_EVENT)
                        {
                            // there is an event
                            switch(theEvent)
                            {
                                case KEY_PRESSED:
                                    PosEnums.PosKeyCode theKey = ihmController.GetKeyCode();
                                    internalPosEmuEngine.EventReceived(theEvent, theKey);
                                    break;
                                default:
                                    break;
                            }
                        }
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Pos_emu.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } while (true);
            }
        }).start();         
   }  
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }    
    
}
