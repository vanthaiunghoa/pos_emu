/*
 * POS_EMU
 * This is a Pos Of Sale emulator 
 * TCPServer : TCP/IP listener
 */
package pos_emu;

import javafx.application.Platform;

// For GSON library (JSON parse)
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class CommandInterpreter {

    private final FXMLDocumentController internalIhmController;
    
    CommandInterpreter(FXMLDocumentController ihmController) {
        // Set controller to IHM
        internalIhmController = ihmController;
    }

    public void ExecuteCommand(String command) throws IOException {
        // Get and parse the JSON message
        // Example of JSON message = {cmd:"DISP",version:"1.0.0"}
        JsonCommandList msg;

        // Parse the JSON message and get the command
        Gson gson = new GsonBuilder().create();
        msg = gson.fromJson(command, JsonCommandList.class);
        String cmd = msg.GetCommand();
        String version = msg.GetVersion();
        if (cmd != null)
            System.out.println("json cmd=" + cmd);
        if (version != null)
            System.out.println("json version=" + version);
        
        // Interpret the message
        switch (cmd) {
            case "DISP":
                // Set POS screen
                Platform.runLater(() -> {
                    
                    // Message to display
                    String labelMsg = msg.GetMessageToDisplay();
                    if (labelMsg == null)
                        labelMsg = "NO MESSAGE";
                    
                    // Message color
                    String theMsgColor = msg.GetColor();
                    if (theMsgColor == null)
                        theMsgColor = "#808080";
                    
                    // Text position
                    int posx;
                    if (msg.GetXPos() == null) {
                        posx = 0;
                    } else {
                        posx = Integer.parseInt(msg.GetXPos());
                    }
                    int posy;
                    if (msg.GetYPos() == null) {
                        posy = 0;
                    } else {
                        posy = Integer.parseInt(msg.GetYPos());
                    }

                    // Text size
                    int textSize;
                    if (msg.GetTextSize() == null) {
                        textSize = 16;
                    } else {
                        textSize = Integer.parseInt(msg.GetTextSize());
                    }
                    
                    Label label1 = new Label(labelMsg);
                    label1.setLayoutX(posx);
                    label1.setLayoutY(posy);
                    label1.setFont(new Font(textSize)); // set to Label
                    label1.setTextFill(Color.web(theMsgColor));
                    internalIhmController.PosScreen.getChildren().add((label1));
                });
                break;
            case "VERS":
                // Set POS screen
                Platform.runLater(() -> {
                    internalIhmController.PromptText.setText("VERSION=" + version);
                });
                break;
            case "CLEAR":
                // Set POS screen
                Platform.runLater(() -> {
                    // internalIhmController.PromptText.setText("");
                    internalIhmController.PosScreen.getChildren().clear();
                });
                break;
            default:
                // Set POS screen
                Platform.runLater(() -> {
                    internalIhmController.PromptText.setText("UNKOWN COMMAND");
                });
                break;
        }
    }
}
