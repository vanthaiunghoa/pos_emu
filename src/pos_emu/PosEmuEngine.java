/*
 * PosEmuEngine
 * This is the engine of POS_EMU simulator
 * 
 */
package pos_emu;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

class PosEmuEngine
{
    private final int CHAR_SIZE = 18;

    public enum PosKeyCode {
        NO_KEY,
        NUM_0,
        NUM_1,
        NUM_2,
        NUM_3,
        NUM_4,
        NUM_5,
        NUM_6,
        NUM_7,
        NUM_8,
        NUM_9,
        NUM_VAL,
        NUM_CORR,
        NUM_CANCEL,
        NUM_MENU,
        NUM_POINT,
        NUM_F
    }
    
    public enum State
    {
        STATE_IDLE,
        STATE_MENU_SCREEN,
        STATE_AMOUNT,
        STATE_CARD_WAITING,
        TRANSACTION
    }
    
    public enum PosEvent
    {
        NO_EVENT,
        KEY_PRESSED,
        TOUCHSCREEN,
        COMM_MESSAGE,
        ICC_INSERTED,
        CARD_SWIPED,
        CLESS_CARD
    }
        
    private final FXMLDocumentController internalIhmController;
    private final ParamConfigFile internalParamData;
    
    /*
    * Constructor
    */
    PosEmuEngine(FXMLDocumentController ihmController, ParamConfigFile theParamData) {
        // Set controller to IHM
        internalIhmController = ihmController;
        internalParamData = theParamData;
    }    
    
    public void StartEngine(State stateToFix)
    {
        State currentState = State.STATE_IDLE;
        
        switch(currentState)
        {
            case STATE_IDLE:
                System.out.println("STATE IDLE");
                if (Integer.parseInt(internalParamData.GetIdleType()) == 0) {
                    // 2 lines of 16 characters are displayed
                    String labelMsg1 = internalParamData.GetIdleMsg1();
                    String labelMsg2 = internalParamData.GetIdleMsg2();
                    
                    // Set label values
                    Label posScreenLabel1 = new Label(labelMsg1);
                    Label posScreenLabel2 = new Label(labelMsg2);
                    posScreenLabel1.setTextFill(Color.web("#404040"));
                    posScreenLabel2.setTextFill(Color.web("#404040"));
                    posScreenLabel2.setFont(new Font(CHAR_SIZE)); // set to Label
                    posScreenLabel1.setFont(new Font(CHAR_SIZE)); // set to Label
                    posScreenLabel1.setLayoutY(120);
                    posScreenLabel2.setLayoutY(144);
                    posScreenLabel1.setLayoutX(0);
                    posScreenLabel2.setLayoutX(0);
                
                    // Set POS screen
                    Platform.runLater(() -> {
                        internalIhmController.PosScreen.getChildren().clear();
                        internalIhmController.PosScreen.getChildren().add((posScreenLabel1));
                        internalIhmController.PosScreen.getChildren().add((posScreenLabel2));
                    });                    
                } else {
                    // A logo is displayed
                    ImageView imageView = new ImageView(new Image(internalParamData.GetLogo()));
                    imageView.toFront();
                    Platform.runLater(() -> {
                        internalIhmController.PosScreen.getChildren().clear();
                        internalIhmController.PosScreen.getChildren().clear();
                        internalIhmController.PosScreen.getChildren().add((imageView));
                    });
                }
                break;
            case STATE_MENU_SCREEN:
                System.out.println("STATE MENU SCREEN");
                break;
            case STATE_AMOUNT:
                System.out.println("STATE AMOUNT");
                // 2 lines of 16 characters are displayed
                String labelMsg1 = "DEBIT";
                String labelMsg2 = "1,00 EUR";
                Label posScreenLabel1 = new Label(labelMsg1);
                Label posScreenLabel2 = new Label(labelMsg2);
                
                // Set POS screen
                Platform.runLater(() -> {
                    internalIhmController.PosScreen.getChildren().clear();
                    internalIhmController.PosScreen.getChildren().add((posScreenLabel1));
                    internalIhmController.PosScreen.getChildren().add((posScreenLabel2));
                });
                break;
            case STATE_CARD_WAITING:
                System.out.println("STATE CARD WAITING");
                break;
            case TRANSACTION:
                System.out.println("STATE TRANSACTION");
                break;
            default:
                System.out.println("STATE DEFAULT");
                break;
        }
    }
    
    public void EventReceived(PosEvent receivedEvent, PosKeyCode keyValue)
    {
        StartEngine(State.STATE_AMOUNT);
    }
    
    private int CenterMessage(String msg)
    {
        int msgLen = msg.length();        
        return (210 - (CHAR_SIZE/2) * msgLen)/2;
    }
}
