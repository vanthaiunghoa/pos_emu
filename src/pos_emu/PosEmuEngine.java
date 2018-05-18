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
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

class PosEmuEngine {

    private final int CHAR_SIZE = 18;
    private final String THE_FONT = "Arial monospaced for SAP";
    private final int AMOUNT_MAX_DIGIT = 14;

    private final FXMLDocumentController internalIhmController;
    private final ParamConfigFile internalParamData;
    private PosEnums.State currentState = PosEnums.State.STATE_NOT_STARTED;
    private PosEnums.State nextState;
    private String strAmount;
    private String strAmountInteger;
    private String strAmountDecimal;
    private int pressedNumKey;
    private int currencyExponent;
    
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
                    String str = strAmountInteger + "," + strAmountDecimal + " EUR";
                    ClearScreen();
                    DisplayLine("       DEBIT", Color.web("#404040"), 0, 100, new Font(THE_FONT, CHAR_SIZE));
                    DisplayLine(str, Color.web("#404040"), 0, 150, new Font(THE_FONT, CHAR_SIZE));
                    break;

                case STATE_CARD_WAITING:
                    System.out.println("STATE CARD WAITING");
                    str = strAmountInteger + "," + strAmountDecimal + " EUR";
                    ClearScreen();
                    DisplayLine("        INSERT CARD", Color.web("#404040"), 0, 100, new Font(THE_FONT, CHAR_SIZE));
                    DisplayLine(str, Color.web("#802020"), 0, 170, new Font(THE_FONT, CHAR_SIZE));
                    break;

                case TRANSACTION:
                    System.out.println("STATE TRANSACTION");
                    ClearScreen();
                    DisplayLine("TRANSACTION", Color.web("#404040"), 0, 100, new Font(THE_FONT, CHAR_SIZE));
                    DisplayLine(" EN COURS", Color.web("#404040"), 0, 150, new Font(THE_FONT, CHAR_SIZE));
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
                ClearAmount();
                if (IsNumeric(keyValue) == true) {
                    AddDigitToAmount(keyValue);
                }
                StartEngine(PosEnums.State.STATE_AMOUNT);
                break;

            case STATE_MENU_SCREEN:
                if (keyValue == PosEnums.PosKeyCode.NUM_CANCEL)
                    StartEngine(PosEnums.State.STATE_IDLE);
                break;

            case STATE_AMOUNT:
                if (keyValue == PosEnums.PosKeyCode.NUM_CORR) {
                    RemoveDigitFromAmount(keyValue);
                    StartEngine(PosEnums.State.STATE_AMOUNT);
                }
                if (keyValue == PosEnums.PosKeyCode.NUM_VAL)
                    StartEngine(PosEnums.State.STATE_CARD_WAITING);
                if (keyValue == PosEnums.PosKeyCode.NUM_CANCEL)
                    StartEngine(PosEnums.State.STATE_IDLE);
                if (IsNumeric(keyValue) == true) {
                    AddDigitToAmount(keyValue);
                    StartEngine(PosEnums.State.STATE_AMOUNT);
                }
                break;

            case STATE_CARD_WAITING:
                if (keyValue == PosEnums.PosKeyCode.NUM_CANCEL)
                    StartEngine(PosEnums.State.STATE_IDLE);
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
    
    private void ClearAmount() {
        strAmount = "";
        strAmountInteger = "           0";
        strAmountDecimal = "00";
        pressedNumKey = 0;
    }
    
    private void RemoveDigitFromAmount(PosEnums.PosKeyCode keyValue) {
        if (pressedNumKey > 0)
        {
            pressedNumKey -= 1;
            strAmount = shift(strAmount);
            UpdateAmountDisplay();            
        }
    }
            
    private void AddDigitToAmount(PosEnums.PosKeyCode keyValue) {
        char val;
        
        switch(keyValue)
        {
            case NUM_0:
                val = '0';
                break;
            case NUM_1:
                val = '1';
                break;
            case NUM_2:
                val = '2';
                break;
            case NUM_3:
                val = '3';
                break;
            case NUM_4:
                val = '4';
                break;
            case NUM_5:
                val = '5';
                break;
            case NUM_6:
                val = '6';
                break;
            case NUM_7:
                val = '7';
                break;
            case NUM_8:
                val = '8';
                break;
            case NUM_9:
                val = '9';
                break;
            default:
                val = '0';
                break;
        }
        
        strAmount = strAmount + val;
        UpdateAmountDisplay();
        pressedNumKey++;
    }
    
    private void UpdateAmountDisplay() {
        // Update display of amount
        StringBuilder bAmountDecimal = new StringBuilder(strAmountDecimal);
        StringBuilder bAmountInteger = new StringBuilder(strAmountInteger);

        if (pressedNumKey < AMOUNT_MAX_DIGIT) {
            bAmountDecimal.setCharAt(1, strAmount.charAt(pressedNumKey));
            if (pressedNumKey > 0) {
                bAmountDecimal.setCharAt(0, strAmount.charAt(pressedNumKey - 1));
            }
            if (pressedNumKey > 1) {
                for (int i = 0; i < (pressedNumKey - 1); i++) {
                    bAmountInteger.setCharAt((AMOUNT_MAX_DIGIT-3) - i, strAmount.charAt(pressedNumKey - 2 - i));
                }
            }
        }

        strAmountDecimal = bAmountDecimal.toString();
        strAmountInteger = bAmountInteger.toString();
    }
    
    private static String shift(String s) {
        String str;
        str = s.substring(0,s.length()-1);
        return str;
    }    
}
