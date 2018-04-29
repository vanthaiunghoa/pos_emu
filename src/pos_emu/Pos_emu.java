/*
 * POS_EMU
 * This is a Pos Of Sale emulator 
 */
package pos_emu;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author balacahan
 */
public class Pos_emu extends Application {
    FXMLDocumentController ihmController;
    private CommandInterpreter internalCommandInterpreter;
    
    public int TCP_IP_LISTENER_PORT = 8000;
 
    @Override
    public void start(Stage stage) throws Exception {
        //Set up instance instead of using static load() method
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
        Parent root = loader.load();

        //Now we have access to getController() through the instance... don't forget the type cast
        ihmController = (FXMLDocumentController)loader.getController();
        internalCommandInterpreter = new CommandInterpreter(ihmController);

        // Create the scene
        Scene scene = new Scene(root);
        // Window without Minimize, Maximize buttons
        stage.initStyle(StageStyle.DECORATED);
        // Window not resizable
        stage.setResizable(false);
     
        // Adding a icon 
        Image appliIcon = new Image(Pos_emu.class.getResourceAsStream( "resource/icon.png" ));
        stage.getIcons().add(appliIcon); 
        
        // Start the terminal boot
        StartPOSBoot();
        
        // Add the scene to the stage and launch the stage
        stage.setScene(scene);
        stage.setTitle("INGENICO POS EMULATOR");
        stage.show();
    }

    /**
     * Start the POS boot sequence
     */
    @SuppressWarnings("SleepWhileInLoop")
    public void StartPOSBoot()
    {
        // Set font to white
        ihmController.PosScreen.setStyle("-fx-background-color:white");
        // Display welcome message
        ihmController.PromptText.setAlignment(Pos.CENTER);
        ihmController.PromptText.setText("BIENVENUE");      
        
        // Start TCP/IP listener
        TCPServer tcpListener = new TCPServer(internalCommandInterpreter, TCP_IP_LISTENER_PORT);
        tcpListener.StartTCPServer();
        
        // Wait for a command
        new Thread(() -> {
            do {
                try {
                    // Infinite loop to be always listening 
                    System.out.println("Wait on TCP listener");
                    tcpListener.WaitTCPMessage();
                    
                    // Pause for 100 ms between each thread, i.e. each command
                    Thread.sleep(100);
                } catch (Exception e) {
                    System.out.println("Error: " + e);
                    
                    // Restart TCP/IP listener                   
                    tcpListener.RestartTCPServer();
                }                
            } while (true);
        }).start();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
