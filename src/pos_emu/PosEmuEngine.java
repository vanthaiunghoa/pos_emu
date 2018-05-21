/*
 * PosEmuEngine
 * This is the engine of POS_EMU simulator
 * 
 */
package pos_emu;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final String BACKGROUND_IMAGE = "/pos_emu/resource/stone-background.png";
    private final Paint POS_COLOR_WHITE = Color.web("#FFFFFF");
    private final Paint POS_COLOR_GREY = Color.web("#DDDDDD");
    private final Paint POS_COLOR_RED = Color.web("#FFCCCC");
    
    private final Pos_emu internalPosEmu;
    private final FXMLDocumentController internalIhmController;
    private final ParamConfigFile internalParamData;
    private PosEnums.State currentState = PosEnums.State.STATE_NOT_STARTED;
    private PosEnums.State nextState;
    private String strAmount;
    private String strAmountInteger;
    private String strAmountDecimal;
    private int pressedNumKey;
    private String currentDateTimeDisplay = "";
            
    /*
    * Constructor
     */
    PosEmuEngine(Pos_emu posEmuController, FXMLDocumentController ihmController, ParamConfigFile theParamData) {
        // Set controller to IHM
        internalIhmController = ihmController;
        internalParamData = theParamData;
        internalPosEmu = posEmuController;
    }

    /*
    * POS Engine (state machine)
     */
    public void StartEngine(PosEnums.State stateToFix, boolean clearScreen) {

        nextState = stateToFix;

        do {
            currentState = nextState;
            
            switch (currentState) {
                case STATE_IDLE:
                    System.out.println("STATE IDLE");
                    ClearScreen(clearScreen);
                    if (Integer.parseInt(internalParamData.GetIdleType()) == 0) {
                        // 2 lines of 16 characters are displayed
                        DisplayLine(CenterMessage(internalParamData.GetIdleMsg1()), POS_COLOR_GREY, 0, 140, FONT_CHAR_SIZE);
                        DisplayLine(CenterMessage(internalParamData.GetIdleMsg2()), POS_COLOR_GREY, 0, 160, FONT_CHAR_SIZE);
                    } else {
                        // A logo is displayed
                        DisplayImage(internalParamData.GetLogo());
                    }
                    break;

                case STATE_MENU_SCREEN:
                    System.out.println("STATE MENU SCREEN");
                    break;

                case STATE_AMOUNT:
                    System.out.println("STATE AMOUNT");
                    // 2 lines of 16 characters are displayed
                    ClearScreen(clearScreen);
                    String str = strAmountInteger + "," + strAmountDecimal + " EUR";
                    DisplayLine(CenterMessage("DEBIT"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                    DisplayLine(str, POS_COLOR_GREY, 0, 150, FONT_CHAR_SIZE);
                    break;

                case STATE_CARD_WAITING:
                    System.out.println("STATE CARD WAITING");
                    str = strAmountInteger + "," + strAmountDecimal + " EUR";
                    ClearScreen(clearScreen);
                    DisplayLine(CenterMessage("INSERT CARD"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                    DisplayLine(CenterMessage(str), POS_COLOR_RED, 0, 170, FONT_CHAR_SIZE);
                    break;

                case TRANSACTION:
                    System.out.println("STATE TRANSACTION");
                    ClearScreen(clearScreen);
                    DisplayLine(CenterMessage("TRANSACTION"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                    DisplayLine(CenterMessage("EN COURS"), POS_COLOR_GREY, 0, 150, FONT_CHAR_SIZE);
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
                    StartEngine(PosEnums.State.STATE_AMOUNT, true);
                }
                break;

            case STATE_MENU_SCREEN:
                if (keyValue == PosEnums.PosKeyCode.NUM_CANCEL) {
                    StartEngine(PosEnums.State.STATE_IDLE, true);
                }
                break;

            case STATE_AMOUNT:
                if (keyValue == PosEnums.PosKeyCode.NUM_CORR) {
                    RemoveDigitFromAmount();
                    StartEngine(PosEnums.State.STATE_AMOUNT, true);
                }
                if (keyValue == PosEnums.PosKeyCode.NUM_VAL) {
                    StartEngine(PosEnums.State.STATE_CARD_WAITING, true);
                }
                if (keyValue == PosEnums.PosKeyCode.NUM_CANCEL) {
                    StartEngine(PosEnums.State.STATE_IDLE, true);
                }
                if (IsNumeric(keyValue) == true) {
                    AddDigitToAmount(keyValue);
                    StartEngine(PosEnums.State.STATE_AMOUNT, true);
                }
                break;

            case STATE_CARD_WAITING:
                if (keyValue == PosEnums.PosKeyCode.NUM_CANCEL) {
                    StartEngine(PosEnums.State.STATE_IDLE, true);
                }
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
        for (int i = 0; i < spaceNb; i++) {
            preSpaces += ' ';
        }

        return (preSpaces + msg);
    }

    private void ClearScreen(boolean clearScreen) {
        
        if (clearScreen == false)
            return;
        
        Platform.runLater(() -> {
            internalIhmController.PosScreen.getChildren().clear();
        });
        DisplayImage(BACKGROUND_IMAGE);
        UpdateTimeOnScreen(true);
    }

    private void DisplayImage(String imageToDisplay) {

        ImageView imageView = new ImageView(new Image(imageToDisplay));
        imageView.toFront();
        Platform.runLater(() -> {
            internalIhmController.PosScreen.getChildren().add((imageView));
        });
    }

    private void DisplayLine(Label myLabel, String msg, Paint color, int posX, int posY, int size) {
        // Set label values
        myLabel.setText(msg);
        myLabel.setTextFill(color);
        myLabel.setStyle("-fx-font-family: Monospace; -fx-font-size: " + size + "; -fx-font-weight: bold;");
        myLabel.setLayoutX(posX);
        myLabel.setLayoutY(posY);

        // Set POS screen
        Platform.runLater(() -> {
            internalIhmController.PosScreen.getChildren().add(myLabel);
        });
    }

    private void DisplayLine(String msg, Paint color, int posX, int posY, int size) {
        // Set label values
        Label posScreenLabel = new Label("");
        DisplayLine(posScreenLabel, msg, color, posX, posY, size);
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

    private void RemoveDigitFromAmount() {
        if (pressedNumKey > 0) {
            pressedNumKey -= 1;
            strAmount = shift(strAmount);
            UpdateAmountDisplay(1);
        }
    }

    private void AddDigitToAmount(PosEnums.PosKeyCode keyValue) {
        char val;

        switch (keyValue) {
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
                    bAmountInteger.setCharAt((AMOUNT_MAX_DIGIT - 3) - i, strAmount.charAt(pressedNumKey - 3 - i));
                }
            }
        }

        if (direction == 1) {
            if (pressedNumKey > 2) {
                bAmountInteger.setCharAt((AMOUNT_MAX_DIGIT - 3) - (pressedNumKey - 2), ' ');
            } else if (pressedNumKey == 2) {
                bAmountInteger.setCharAt((AMOUNT_MAX_DIGIT - 3) - (pressedNumKey - 2), '0');
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
        str = s.substring(0, s.length() - 1);
        return str;
    }
    
    public void UpdateTimeOnScreen(boolean force) {
      	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        LocalDateTime now = LocalDateTime.now();
        String display = dtf.format(now);
        if ((!currentDateTimeDisplay.equals(display)) || (true == force)) {
            System.out.println(dtf.format(now)); // 16:28 21/05/2018
            currentDateTimeDisplay = display;
            DisplayImage(BACKGROUND_IMAGE);
            DisplayLine(display.substring(0,5), POS_COLOR_WHITE, 164, 0, 12); // time
            DisplayLine(display.substring(6,16), POS_COLOR_GREY, 150, 12, 10); // date
            StartEngine(currentState, false);
        }
    }
}
