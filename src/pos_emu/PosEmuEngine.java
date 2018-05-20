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

class PosEmuEngine {

    private final int FONT_CHAR_SIZE = 18;
    private final int MAX_SCREEN_CHAR = 19;
    private final int AMOUNT_MAX_DIGIT = MAX_SCREEN_CHAR - 5;

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
                        DisplayLine(CenterMessage(internalParamData.GetIdleMsg1()), Color.web("#404040"), 0, 120, FONT_CHAR_SIZE);
                        DisplayLine(CenterMessage(internalParamData.GetIdleMsg2()), Color.web("#404040"), 0, 144, FONT_CHAR_SIZE);
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
                    DisplayLine(CenterMessage("DEBIT"), Color.web("#404040"), 0, 100, FONT_CHAR_SIZE);
                    DisplayLine(str, Color.web("#404040"), 0, 150, FONT_CHAR_SIZE);
                    break;

                case STATE_CARD_WAITING:
                    System.out.println("STATE CARD WAITING");
                    str = strAmountInteger + "," + strAmountDecimal + " EUR";
                    ClearScreen();
                    DisplayLine(CenterMessage("INSERT CARD"), Color.web("#404040"), 0, 100, FONT_CHAR_SIZE);
                    DisplayLine(CenterMessage(str), Color.web("#802020"), 0, 170, FONT_CHAR_SIZE);
                    break;

                case TRANSACTION:
                    System.out.println("STATE TRANSACTION");
                    ClearScreen();
                    DisplayLine(CenterMessage("TRANSACTION"), Color.web("#404040"), 0, 100, FONT_CHAR_SIZE);
                    DisplayLine(CenterMessage("EN COURS"), Color.web("#404040"), 0, 150, FONT_CHAR_SIZE);
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
                if (IsNumeric(keyValue) == true) {
                    ClearAmount();
                    AddDigitToAmount(keyValue);
                    StartEngine(PosEnums.State.STATE_AMOUNT);
                }
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

    private String CenterMessage(String msg) {
        int spaceNb = (MAX_SCREEN_CHAR - msg.length()) / 2;
        String preSpaces = "";
        for (int i=0; i<spaceNb; i++)
            preSpaces += ' ';
        
        return (preSpaces + msg);
    }

    private void ClearScreen() {
        Platform.runLater(() -> {
            internalIhmController.PosScreen.getChildren().clear();
        });
    }

    private void DisplayLine(String msg, Paint color, int posX, int posY, int size) {
        // Set label values
        Label posScreenLabel = new Label(msg);
        posScreenLabel.setTextFill(color);
        posScreenLabel.setStyle("-fx-font-family: Monospace; -fx-font-size: " + size + "; -fx-font-weight: bold;");
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
            UpdateAmountDisplay(1);
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
        
        if (pressedNumKey < AMOUNT_MAX_DIGIT) {
            strAmount = strAmount + val;
            pressedNumKey++;
            UpdateAmountDisplay(0);
        }
    }
    
    private void UpdateAmountDisplay(int direction) {
        // Update display of amount
        StringBuilder bAmountDecimal = new StringBuilder(strAmountDecimal);
        StringBuilder bAmountInteger = new StringBuilder(strAmountInteger);
       
        if ((pressedNumKey > 0) && (pressedNumKey <= AMOUNT_MAX_DIGIT)) {
            bAmountDecimal.setCharAt(1, strAmount.charAt(pressedNumKey - 1));
            if (pressedNumKey > 1) {
                bAmountDecimal.setCharAt(0, strAmount.charAt(pressedNumKey - 2));
            }
            if (pressedNumKey > 2) {
                for (int i = 0; i < (pressedNumKey - 2); i++) {
                    bAmountInteger.setCharAt((AMOUNT_MAX_DIGIT-3) - i, strAmount.charAt(pressedNumKey - 3 - i));
                }
            }
        }

        if (direction == 1) {
            if (pressedNumKey > 2) {
                bAmountInteger.setCharAt((AMOUNT_MAX_DIGIT-3) - (pressedNumKey-2), ' ');
            } else if (pressedNumKey == 2) {
                bAmountInteger.setCharAt((AMOUNT_MAX_DIGIT-3) - (pressedNumKey-2), '0');
            } else if (pressedNumKey == 1) {
                bAmountDecimal.setCharAt(0, '0');
            } else {
                bAmountDecimal.setCharAt(1, '0');
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
