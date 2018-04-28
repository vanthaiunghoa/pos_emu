/*
 * POS_EMU
 * This is a Pos Of Sale emulator 
 * TCPServer : TCP/IP listener
 */
package pos_emu;

import javafx.application.Platform;

public class CommandInterpreter 
{    
    private final FXMLDocumentController internalIhmController;
    
    CommandInterpreter(FXMLDocumentController ihmController)
    {        
        // Set controller to IHM
        internalIhmController = ihmController;
    }
    
    public void ExecuteCommand(String command)
    {
        // Set POS screen
        Platform.runLater(() -> {
            internalIhmController.PromptText.setText(command);
        });
    }
}
