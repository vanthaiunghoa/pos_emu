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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class CommandInterpreter {

    private final FXMLDocumentController internalIhmController;
    
    CommandInterpreter(FXMLDocumentController ihmController) {
        // Set controller to IHM
        internalIhmController = ihmController;
    }
    
    /**
    * Execute the command received from the TCP/IP (in JSON format)
    * 
    *  <P> -- </P>
    *  <P>DISP : For a message display command</P>
    *  <P>cmd          ="DISP", mandatory</P>
    *  <P>version      Version of the caller (format is "X.Y.Z"), mandatory</P>
    *  <P>color        Color of the message to display in RGB format : "#RRGGBB", default is "#808080"</P>
    *  <P>x_pos        Position in X of the message in pixel, default is "0", ex: "100"</P>
    *  <P>y_pos        Position in Y of the message in pixel, default is "0", ex: "100"</P>
    *  <P>size         Size of the font, default is "16", ex: "10"</P>
    * 
    *  <P> -- </P>
    *  <P>VERS : Provide the module version</P>
    *  <P>cmd          ="VERS", mandatory</P>
    *  <P>version      Version of the caller (format is "X.Y.Z"), mandatory</P>
    *  <P>color        Color of the message to display in RGB format : "#RRGGBB", default is "#808080"</P>
    *  <P>x_pos        Position in X of the message in pixel, default is "0", ex: "100"</P>
    *  <P>y_pos        Position in Y of the message in pixel, default is "0", ex: "100"</P>
    *  <P>size         Size of the font, default is "16", ex: "10"</P>
    * 
    *  <P> -- </P>
    *  <P>CLEAR : Clear the POS screen</P>
    *  <P>cmd          ="CLEAR", mandatory</P>
    *  <P>version      Version of the caller (format is "X.Y.Z"), mandatory</P>
    * 
    *  <P> -- </P>
    *  <P>QUIT : Close the emulator</P>
    *  <P>cmd          ="QUIT", mandatory</P>
    *  <P>version      Version of the caller (format is "X.Y.Z"), mandatory</P>
    * 
    *  <P> -- </P>
    *  <P>LOGO : Display a logo (image) on the POS screen : png, bmp or jpg. Can be from file or URL</P>
    *  <P>cmd          ="LOGO", mandatory</P>
    *  <P>version      Version of the caller (format is "X.Y.Z"), mandatory</P>
    *  <P>image        Image file to display : 
    * "file" has to be in repository "resource" (and file name is "logo.png") : "imgName" will be "/pos_emu/resource/logo.png"
    * it is also possible to provide an URL : "http://www.balacsoft.com/images/balactris.png"</P>
    * 
    *  <P> -- </P>
    * @param receivedJsonMessage Message in JSON containing at least the command and the version
    * 
    * @throws IOException
    */
    public void ExecuteCommand(String receivedJsonMessage) throws IOException {
        // Get and parse the JSON message
        // Example of JSON message = {cmd:"DISP",version:"1.0.0"}
        JsonCommandList msg;

        // Parse the JSON message and get the command
        Gson gson = new GsonBuilder().create();
        msg = gson.fromJson(receivedJsonMessage, JsonCommandList.class);
        
        // Get the command and the version
        String cmd = msg.GetCommand();
        String version = msg.GetVersion();
        System.out.println("json cmd=" + cmd);
        System.out.println("json version=" + version);
        
        // Configure label to display
        String labelMsg = msg.GetMessageToDisplay();
        // Message color
        String theMsgColor = msg.GetColor();
        // Text position
        int posx = Integer.parseInt(msg.GetXPos());
        int posy = Integer.parseInt(msg.GetYPos());
        // Text size
        int textSize = Integer.parseInt(msg.GetTextSize());
        // Image name to display
        String imgName = msg.GetImageName();

        // Set label values
        Label posScreenLabel = new Label(labelMsg);
        posScreenLabel.setLayoutX(posx);
        posScreenLabel.setLayoutY(posy);
        posScreenLabel.setFont(new Font(textSize)); // set to Label
        posScreenLabel.setTextFill(Color.web(theMsgColor));

        // Interpret the message
        switch (cmd) {
            case "DISP":
                // Set POS screen
                Platform.runLater(() -> {
                    internalIhmController.PosScreen.getChildren().add((posScreenLabel));
                });
                break;
            case "VERS":
                // Set POS screen
                Platform.runLater(() -> {
                    posScreenLabel.setText("VERSION=" + version);
                    internalIhmController.PosScreen.getChildren().add((posScreenLabel));                    
                });
                break;
            case "CLEAR":
                // Set POS screen
                Platform.runLater(() -> {
                    internalIhmController.PosScreen.getChildren().clear();
                });
                break;
            case "LOGO":     
                // Image size : width=210, height=300
                // "file" has to be in repository "resource" (and file name is "logo.png") => "imgName" will be "/pos_emu/resource/logo.png"
                // it is also possible to provide an URL : "http://www.balacsoft.com/images/balactris.png"
                ImageView imageView = new ImageView(new Image (imgName));
                imageView.toFront();
                Platform.runLater(() -> {
                    internalIhmController.PosScreen.getChildren().clear();
                    internalIhmController.PosScreen.getChildren().add((imageView));
                });
                break;
            case "QUIT":
                Platform.runLater(() -> {
                    Platform.exit();
                    System.exit(0);
                });
                break;
            default:
                // Set POS screen
                Platform.runLater(() -> {
                    posScreenLabel.setText("UNKOWN COMMAND");
                    internalIhmController.PosScreen.getChildren().add((posScreenLabel));                    
                });
                break;
        }
    }
}
