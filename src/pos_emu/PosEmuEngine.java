/*
 * PosEmuEngine
 * This is the engine of POS_EMU simulator
 * 
 */
package pos_emu;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

class PosEmuEngine {

    private final int CHAR_SIZE = 18;

    private final FXMLDocumentController internalIhmController;
    private final ParamConfigFile internalParamData;
    private PosEnums.State currentState = PosEnums.State.STATE_NOT_STARTED;
    private PosEnums.State nextState;

    /*
    * Constructor
     */
    PosEmuEngine(FXMLDocumentController ihmController, ParamConfigFile theParamData) {
        // Set controller to IHM
        internalIhmController = ihmController;
        internalParamData = theParamData;
    }

    /*
    * POS Engine (state machine)
     */
    public void StartEngine(PosEnums.State stateToFix) {

        nextState = stateToFix;

        do {
            currentState = nextState;

            switch (currentState) {
                case STATE_IDLE:
                    System.out.println("STATE IDLE");
                    if (Integer.parseInt(internalParamData.GetIdleType()) == 0) {
                        // 2 lines of 16 characters are displayed
                        ClearScreen();
                        DisplayLine(internalParamData.GetIdleMsg1(), Color.web("#404040"), 0, 120, new Font(CHAR_SIZE));
                        DisplayLine(internalParamData.GetIdleMsg2(), Color.web("#404040"), 0, 144, new Font(CHAR_SIZE));
                    } else {
                        // A logo is displayed
                        ImageView imageView = new ImageView(new Image(internalParamData.GetLogo()));
                        imageView.toFront();
                        Platform.runLater(() -> {
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
                    ClearScreen();
                    DisplayLine("    DEBIT", Color.web("#404040"), 0, 100, new Font(CHAR_SIZE));
                    DisplayLine("   1,00 EUR", Color.web("#404040"), 0, 150, new Font(CHAR_SIZE));
                    break;

                case STATE_CARD_WAITING:
                    System.out.println("STATE CARD WAITING");
                    break;

                case TRANSACTION:
                    System.out.println("STATE TRANSACTION");
                    ClearScreen();
                    DisplayLine("TRANSACTION", Color.web("#404040"), 0, 100, new Font(CHAR_SIZE));
                    DisplayLine(" EN COURS", Color.web("#404040"), 0, 150, new Font(CHAR_SIZE));
                    break;

                default:
                    System.out.println("STATE DEFAULT");
                    break;
            }

        } while (currentState != nextState);
    }

    public void EventReceived(PosEnums.PosEvent receivedEvent, PosEnums.PosKeyCode keyValue) {
        
        switch (currentState) {
            case STATE_IDLE:
                if (IsNumeric(keyValue) == true)
                    StartEngine(PosEnums.State.STATE_AMOUNT);
                break;

            case STATE_MENU_SCREEN:
                break;

            case STATE_AMOUNT:
                if (keyValue == PosEnums.PosKeyCode.NUM_CANCEL)
                    StartEngine(PosEnums.State.STATE_IDLE);
                break;

            case STATE_CARD_WAITING:
                break;

            case TRANSACTION:
                break;

            default:
                break;
        }
    }

    private int CenterMessage(String msg) {
        int msgLen = msg.length();
        return (210 - (CHAR_SIZE / 2) * msgLen) / 2;
    }

    private void ClearScreen() {
        Platform.runLater(() -> {
            internalIhmController.PosScreen.getChildren().clear();
        });
    }

    private void DisplayLine(String msg, Paint color, int posX, int posY, Font theFont) {
        // Set label values
        Label posScreenLabel = new Label(msg);
        posScreenLabel.setTextFill(color);
        posScreenLabel.setFont(theFont);
        posScreenLabel.setLayoutX(posX);
        posScreenLabel.setLayoutY(posY);

        // Set POS screen
        Platform.runLater(() -> {
            internalIhmController.PosScreen.getChildren().add((posScreenLabel));
        });
    }
    
    private boolean IsNumeric(PosEnums.PosKeyCode keyValue) {
        return (keyValue == PosEnums.PosKeyCode.NUM_0)
                || (keyValue == PosEnums.PosKeyCode.NUM_1)
                || (keyValue == PosEnums.PosKeyCode.NUM_2)
                || (keyValue == PosEnums.PosKeyCode.NUM_3)
                || (keyValue == PosEnums.PosKeyCode.NUM_4)
                || (keyValue == PosEnums.PosKeyCode.NUM_5)
                || (keyValue == PosEnums.PosKeyCode.NUM_6)
                || (keyValue == PosEnums.PosKeyCode.NUM_7)
                || (keyValue == PosEnums.PosKeyCode.NUM_8)
                || (keyValue == PosEnums.PosKeyCode.NUM_9);
    }
}
