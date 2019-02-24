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
import c_icc.C_icc;
import c_icc.C_icc_pcsc;
import c_icc.C_icc_virtual;
import c_common.C_logger_stdout;
import c_common.C_err;

class PosEmuEngine {

    private final int FONT_CHAR_SIZE = 18;
    private final int MAX_SCREEN_CHAR = 19;
    private final int AMOUNT_MAX_DIGIT = MAX_SCREEN_CHAR - 5;
    private final String BACKGROUND_IMAGE = "/pos_emu/resource/stone-background.png";
    private final Paint POS_COLOR_WHITE = Color.web("#FFFFFF");
    private final Paint POS_COLOR_GREY = Color.web("#DDDDDD");
    private final Paint POS_COLOR_RED = Color.web("#FFCCCC");

    private final String BLIND_PIN = "****************";

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

    private C_icc m_icc;
    private C_err.Icc retIcc;
    private String module_name;
    private String PinCode;

    /*
    * Constructor
     */
    PosEmuEngine(Pos_emu posEmuController, FXMLDocumentController ihmController, ParamConfigFile theParamData) {
        // Set controller to IHM
        internalIhmController = ihmController;
        internalParamData = theParamData;
        internalPosEmu = posEmuController;
    }

    public void initializePosEngine() {
        // Icc type
        C_icc.SmartCardManagementType smartCardType = C_icc.SmartCardManagementType.SMARTCARD_PCSC;

        // According to the parameter, use PC/SC or virtual Smart-Card
        if (smartCardType == C_icc.SmartCardManagementType.SMARTCARD_VIRTUAL) {
            // Create ICC smart card component based on PCSC
            m_icc = new C_icc_pcsc("m_icc");
        } else {
            // Create virtual card (no need for a real reader)
            m_icc = new C_icc_virtual("m_icc");
        }

        // Initialize module
        module_name = m_icc.getModuleName();
        C_logger_stdout.LogInfo(module_name, "Module Created");

        m_icc.initModule();
        C_logger_stdout.LogInfo(module_name, "Module Initialization Done");

        retIcc = m_icc.IccConnectReader(null);
        if (C_err.Icc.ERR_ICC_OK == retIcc) {
            C_logger_stdout.LogInfo(module_name, "Reader Connected : " + m_icc.IccGetReaderName());
        } else {
            C_logger_stdout.LogError(module_name, "Problem connecting to reader");
        }
    }

    /*
     * POS Engine (state machine)
     */
    public void StartEngine(PosEnums.State stateToFix, boolean clearScreen, PosEnums.PosEvent theEvent) {
        C_err.Icc retIcc;
        String response;
        String str;
        
        nextState = stateToFix;

//        do {            
            currentState = nextState;
            ClearScreen(clearScreen);

            switch (currentState) {
                case STATE_IDLE:
                    PosEmuUtils.DisplayLogInfo("STATE IDLE");
                    if (Integer.parseInt(internalParamData.GetIdleType()) != 0) {
                        // A logo is displayed
                        DisplayImage(internalParamData.GetLogo());
                    } else {
                        // 2 lines of 16 characters are displayed
                        DisplayLine(CenterMessage(internalParamData.GetIdleMsg1()), POS_COLOR_GREY, 0, 140, FONT_CHAR_SIZE);
                        DisplayLine(CenterMessage(internalParamData.GetIdleMsg2()), POS_COLOR_GREY, 0, 160, FONT_CHAR_SIZE);
                    }
                    break;

                case STATE_MENU_SCREEN:
                    PosEmuUtils.DisplayLogInfo("STATE MENU SCREEN");
                    break;

                case STATE_AMOUNT:
                    str = strAmountInteger + "," + strAmountDecimal + " EUR";
                    PosEmuUtils.DisplayLogInfo("STATE AMOUNT = " + str);
                    // 2 lines of 16 characters are displayed
                    DisplayLine(CenterMessage("DEBIT"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                    DisplayLine(str, POS_COLOR_GREY, 0, 150, FONT_CHAR_SIZE);
                    break;

                case STATE_CARD_WAITING:
                    PosEmuUtils.DisplayLogInfo("STATE CARD WAITING");
                    str = strAmountInteger + "," + strAmountDecimal + " EUR";
                    DisplayLine(CenterMessage("INSEREZ CARTE"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                    DisplayLine(CenterMessage(str), POS_COLOR_RED, 0, 170, FONT_CHAR_SIZE);
                    break;

                case STATE_TRANSACTION_ICC:
                    PosEmuUtils.DisplayLogInfo("STATE TRANSACTION");
                    DisplayLine(CenterMessage("TRANSACTION"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                    DisplayLine(CenterMessage("CARTE"), POS_COLOR_GREY, 0, 150, FONT_CHAR_SIZE);
                    DisplayLine(CenterMessage("EN COURS"), POS_COLOR_GREY, 0, 170, FONT_CHAR_SIZE);

                    // read the card
                    retIcc = m_icc.IccConnectSmartCard();
                    if (C_err.Icc.ERR_ICC_OK == retIcc) {
                        C_logger_stdout.LogInfo(module_name, "Card Connected - ATR=" + m_icc.IccGetATR());

                        // Perform selection
                        response = m_icc.IccPerformSelection();
                        if (response == null) {
                            C_logger_stdout.LogWarning(module_name, "No AID in common");
                        } else {
                            C_logger_stdout.LogInfo(module_name, "Selected AID is " + response);

                            // Perform card reading
                            response = m_icc.IccReadCard();
                            if (response == null) {
                                C_logger_stdout.LogError(module_name, "Error reading card data");
                            } else {
                                C_logger_stdout.LogInfo(module_name, "CARD PAN is " + response);
                            }

                            // Disconnect
                            m_icc.IccDisconnect();
                        }
                    } else {
                        if (C_err.Icc.ERR_ICC_NO_CARD == retIcc) {
                            C_logger_stdout.LogWarning(module_name, "No card present");
                        } else {
                            C_logger_stdout.LogError(module_name, "Error problem connecting card");
                        }
                    }

                    break;

                case STATE_TRANSACTION_MAGSTRIPE:
                    PosEmuUtils.DisplayLogInfo("STATE TRANSACTION");
                    DisplayLine(CenterMessage("TRANSACTION"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                    DisplayLine(CenterMessage("PISTE"), POS_COLOR_GREY, 0, 150, FONT_CHAR_SIZE);
                    DisplayLine(CenterMessage("EN COURS"), POS_COLOR_GREY, 0, 170, FONT_CHAR_SIZE);
                    break;

                case STATE_TRANSACTION_CLESS:
                    PosEmuUtils.DisplayLogInfo("STATE TRANSACTION");
                    DisplayLine(CenterMessage("TRANSACTION"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                    DisplayLine(CenterMessage("CONTACTLESS"), POS_COLOR_GREY, 0, 150, FONT_CHAR_SIZE);
                    DisplayLine(CenterMessage("EN COURS"), POS_COLOR_GREY, 0, 170, FONT_CHAR_SIZE);
                    break;
                    
                case STATE_PIN_ENTRY:
                    PosEmuUtils.DisplayLogInfo("STATE PIN ENTRY: PIN=" + PinCode);
                    str = strAmountInteger + "," + strAmountDecimal + " EUR";
                    DisplayLine(CenterMessage("DEBIT"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                    DisplayLine(str, POS_COLOR_GREY, 0, 130, FONT_CHAR_SIZE);
                    DisplayLine(CenterMessage("SAISIR CODE:"), POS_COLOR_GREY, 0, 160, FONT_CHAR_SIZE);
                    
                    // Display PIN code (with stars *)
                    String PinCodeBlind = BLIND_PIN.substring(0,PinCode.length());
                    DisplayLine(CenterMessage(PinCodeBlind), POS_COLOR_GREY, 0, 190, FONT_CHAR_SIZE);
                    
                    break;

                case STATE_PIN_RESULT_OK:
                    PosEmuUtils.DisplayLogInfo("STATE PIN RESULT OK");
                    DisplayLine(CenterMessage("CODE BON"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                    break;

                case STATE_PIN_RESULT_NOK:
                    PosEmuUtils.DisplayLogInfo("STATE PIN RESULT KO");
                    DisplayLine(CenterMessage("CODE FAUX"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                    break;

                case STATE_TRANSACTION_RESULT_OK:
                    PosEmuUtils.DisplayLogInfo("STATE TRX RESULT OK");
                    DisplayLine(CenterMessage("PAIEMENT ACCEPTE"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                    break;

                case STATE_TRANSACTION_RESULT_NOK:
                    PosEmuUtils.DisplayLogInfo("STATE TRX RESULT KO");
                    DisplayLine(CenterMessage("PAIEMENT REFUSE"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                    break;

                case STATE_PRINT_CUSTOMER_RECEIPT:
                    PosEmuUtils.DisplayLogInfo("STATE PRINT CUSTOMER RECEIPT");
                    DisplayLine(CenterMessage("IMPRESSION EN COURS"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                    DisplayLine(CenterMessage("VALIDEZ"), POS_COLOR_GREY, 0, 150, FONT_CHAR_SIZE);
                    break;

                case STATE_PRINT_MERCHANT_RECEIPT:
                    PosEmuUtils.DisplayLogInfo("STATE PRINT MERCHANT RECEIPT");
                    DisplayLine(CenterMessage("IMPRESSION EN COURS"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                    break;

                default:
                    PosEmuUtils.DisplayLogInfo("STATE DEFAULT");
                    break;
            }

 //       } while (currentState != nextState);
    }

    /*
     * POS Engine (state machine)
     */
    public void StartEngine(PosEnums.State stateToFix, boolean clearScreen) {
        StartEngine(stateToFix, clearScreen, PosEnums.PosEvent.NO_EVENT);
    }

    public void EventReceived(PosEnums.PosEvent receivedEvent, PosEnums.PosKeyCode keyValue) {

        switch (currentState) {
            case STATE_IDLE:
                if (receivedEvent == PosEnums.PosEvent.KEY_PRESSED) {
                    if (IsNumeric(keyValue) == true) {
                        ClearAmount();
                        AddDigitToAmount(keyValue);
                        StartEngine(PosEnums.State.STATE_AMOUNT, true);
                    }
                }
                break;

            case STATE_MENU_SCREEN:
                if (receivedEvent == PosEnums.PosEvent.KEY_PRESSED) {
                    if (keyValue == PosEnums.PosKeyCode.NUM_CANCEL) {
                        StartEngine(PosEnums.State.STATE_IDLE, true);
                    }
                }
                break;

            case STATE_AMOUNT:
                if (receivedEvent == PosEnums.PosEvent.KEY_PRESSED) {
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
                }
                break;

            case STATE_CARD_WAITING:
                if (receivedEvent == PosEnums.PosEvent.KEY_PRESSED) {
                    if (keyValue == PosEnums.PosKeyCode.NUM_CANCEL) {
                        StartEngine(PosEnums.State.STATE_IDLE, true);
                    }
                } else if (receivedEvent == PosEnums.PosEvent.ICC_INSERTED) {
                    StartEngine(PosEnums.State.STATE_TRANSACTION_ICC, true, receivedEvent);
                } else if (receivedEvent == PosEnums.PosEvent.CARD_SWIPED) {
                    StartEngine(PosEnums.State.STATE_TRANSACTION_MAGSTRIPE, true, receivedEvent);
                } else if (receivedEvent == PosEnums.PosEvent.CLESS_CARD) {
                    StartEngine(PosEnums.State.STATE_TRANSACTION_CLESS, true, receivedEvent);
                }

                break;

            case STATE_TRANSACTION_ICC:
                PinCode = "";
                StartEngine(PosEnums.State.STATE_PIN_ENTRY, true);
                break;
                
            case STATE_TRANSACTION_MAGSTRIPE:
            case STATE_TRANSACTION_CLESS:
                if (receivedEvent == PosEnums.PosEvent.KEY_PRESSED) {
                    if (keyValue == PosEnums.PosKeyCode.NUM_CANCEL) {
                        StartEngine(PosEnums.State.STATE_IDLE, true);
                    }
                }
                break;

            case STATE_PIN_ENTRY:
                if (receivedEvent == PosEnums.PosEvent.KEY_PRESSED) {
                    if (keyValue == PosEnums.PosKeyCode.NUM_CANCEL) {
                        // Cancel key pressed
                        StartEngine(PosEnums.State.STATE_IDLE, true);
                    } else if (IsNumeric(keyValue)) {
                        // Numeric key pressed
                        PinCode += GetCharFromKeyValue(keyValue);
                        StartEngine(PosEnums.State.STATE_PIN_ENTRY, true);
                    } else if (keyValue == PosEnums.PosKeyCode.NUM_CORR) {
                        // Numeric key pressed
                        int position = PinCode.length() - 1;
                        if (position < 0) position = 0;
                        PinCode = PinCode.substring(0, position);
                        StartEngine(PosEnums.State.STATE_PIN_ENTRY, true);
                    } else if (keyValue == PosEnums.PosKeyCode.NUM_VAL) {
                        // PIN is Entered, Check the PIN code
                        retIcc = m_icc.IccPinVerify(PinCode);
                        if (C_err.Icc.ERR_ICC_OK == retIcc) {
                            C_logger_stdout.LogInfo(module_name, "PIN OK");
                            StartEngine(PosEnums.State.STATE_PIN_RESULT_OK, true);
                        } else {
                            C_logger_stdout.LogError(module_name, "WRONG PIN");
                            StartEngine(PosEnums.State.STATE_PIN_RESULT_NOK, true);
                        }
                    }
                }
                break;
                
            case STATE_PIN_RESULT_OK:
                if (receivedEvent == PosEnums.PosEvent.KEY_PRESSED) {
                    if (strAmountDecimal.equals("00") == true) {
                        StartEngine(PosEnums.State.STATE_TRANSACTION_RESULT_OK, true);
                    } else {
                        StartEngine(PosEnums.State.STATE_TRANSACTION_RESULT_NOK, true);
                    }
                }
                break;

            case STATE_PIN_RESULT_NOK:
                if (receivedEvent == PosEnums.PosEvent.KEY_PRESSED) {
                    PinCode = "";
                    StartEngine(PosEnums.State.STATE_PIN_ENTRY, true);
                }
                break;

            case STATE_TRANSACTION_RESULT_OK:
            case STATE_TRANSACTION_RESULT_NOK:
                if (receivedEvent == PosEnums.PosEvent.KEY_PRESSED) {
                    StartEngine(PosEnums.State.STATE_PRINT_CUSTOMER_RECEIPT, true);
                }
                break;

            case STATE_PRINT_CUSTOMER_RECEIPT:
                if (receivedEvent == PosEnums.PosEvent.KEY_PRESSED) {
                    StartEngine(PosEnums.State.STATE_PRINT_MERCHANT_RECEIPT, true);
                }
                break;

            case STATE_PRINT_MERCHANT_RECEIPT:
                if (receivedEvent == PosEnums.PosEvent.KEY_PRESSED) {
                    StartEngine(PosEnums.State.STATE_IDLE, true);
                }
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

        if (clearScreen == false) {
            return;
        }

        Platform.runLater(() -> {
            internalIhmController.PosScreen.getChildren().clear();
        });
        DisplayImage(BACKGROUND_IMAGE);
        UpdateTimeOnScreen(false);
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

        val = GetCharFromKeyValue(keyValue);

        if (pressedNumKey < AMOUNT_MAX_DIGIT) {
            strAmount = strAmount + val;
            pressedNumKey++;
            UpdateAmountDisplay(0);
        }
    }

    private char GetCharFromKeyValue(PosEnums.PosKeyCode keyval) {
        char val;
        
        switch (keyval) {
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
        
        return val;
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
            currentDateTimeDisplay = display;
            DisplayImage(BACKGROUND_IMAGE);
            DisplayLine(display.substring(0, 5), POS_COLOR_WHITE, 164, 0, 12); // time
            DisplayLine(display.substring(6, 16), POS_COLOR_GREY, 150, 12, 10); // date
            StartEngine(currentState, false);
        }
    }
}
