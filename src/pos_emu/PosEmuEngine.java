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

    private final long TIMER_1_SECONDS = 1000l;
    private final long TIMER_2_SECONDS = 2000l;
    private final long TIMER_5_SECONDS = 5000l;

    private final String BLIND_PIN = "****************";

    // Modules
    private final Pos_emu internalPosEmu;
    private final FXMLDocumentController internalIhmController;
    private final FXML_LogWindowController internalLogController;
    private final FXML_ReceiptWindowController internalReceiptController;
    private final ParamConfigFile internalParamData;
    private C_icc m_icc;
    private TransactionContext myTrxContext;
    
    private PosEnums.State currentState = PosEnums.State.STATE_NOT_STARTED;
    private PosEnums.State nextState;
    private String strAmount;
    private String strAmountInteger;
    private String strAmountDecimal;
    private int pressedNumKey;
    private String currentDateTimeDisplay = "";

    private C_err.Icc retIcc;
    private String module_name;
    private String PinCode;
    
    private int transactionStatus = 0;

    private PosEnums.PosTransactionTechno typeCurrentTrxTechno;

    /*
    * Constructor
     */
    PosEmuEngine(
            Pos_emu posEmuController, 
            FXMLDocumentController ihmController, 
            ParamConfigFile theParamData, 
            C_icc module_icc, 
            FXML_LogWindowController logController, 
            FXML_ReceiptWindowController receiptController) {
        
        // Set controller to IHM
        internalIhmController = ihmController;
        internalLogController = logController;
        internalReceiptController = receiptController;
        internalParamData = theParamData;
        internalPosEmu = posEmuController;
        m_icc = module_icc;
        myTrxContext = new TransactionContext();
    }

    /*
     * POS Engine (state machine)
     *
     */
    public void StartEngine(PosEnums.State stateToFix, boolean clearScreen, PosEnums.PosEvent theEvent) {
        C_err.Icc retIcc;
        String response;
        String str;

        nextState = stateToFix;
        currentState = nextState;
        ClearScreen(clearScreen);

        switch (currentState) {
            case STATE_IDLE:
                PosEmuUtils.DisplayLogInfo(internalLogController, "STATE IDLE");
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
                PosEmuUtils.DisplayLogInfo(internalLogController, "STATE MENU SCREEN");
                DisplayLine(CenterMessage("ADS APPLI"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                DisplayLine(CenterMessage("ADM"), POS_COLOR_GREY, 0, 130, FONT_CHAR_SIZE);
                DisplayLine(CenterMessage("BANCAIRE EMV"), POS_COLOR_GREY, 0, 160, FONT_CHAR_SIZE);
                DisplayLine(CenterMessage("SANS CONTACT"), POS_COLOR_GREY, 0, 190, FONT_CHAR_SIZE);
                break;

            case STATE_AMOUNT:
                PrintReceiptClear();
                str = strAmountInteger + "," + strAmountDecimal + " EUR";
                PosEmuUtils.DisplayLogInfo(internalLogController, "STATE AMOUNT = " + str);
                // 2 lines of 16 characters are displayed
                DisplayLine(CenterMessage("DEBIT"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                DisplayLine(str, POS_COLOR_GREY, 0, 150, FONT_CHAR_SIZE);
                break;

            case STATE_CARD_WAITING:
                // Wait for key entry
                PosEmuUtils.DisplayLogInfo(internalLogController, "STATE CARD WAITING");
                str = strAmountInteger + "," + strAmountDecimal + " EUR";
                myTrxContext.SetAmount(str);
                DisplayLine(CenterMessage("INSEREZ CARTE"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                DisplayLine(CenterMessage(str), POS_COLOR_RED, 0, 170, FONT_CHAR_SIZE);
                break;

            case STATE_TRANSACTION_ICC:
                PosEmuUtils.DisplayLogInfo(internalLogController, "STATE TRANSACTION");
                DisplayLine(CenterMessage("TRANSACTION"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                DisplayLine(CenterMessage("CARTE"), POS_COLOR_GREY, 0, 150, FONT_CHAR_SIZE);
                DisplayLine(CenterMessage("EN COURS"), POS_COLOR_GREY, 0, 170, FONT_CHAR_SIZE);

                typeCurrentTrxTechno = PosEnums.PosTransactionTechno.TRX_ICC;

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
                        myTrxContext.SetAid(response);

                        // Perform card reading
                        response = m_icc.IccReadCard();
                        if (response == null) {
                            C_logger_stdout.LogError(module_name, "Error reading card data");
                        } else {
                            C_logger_stdout.LogInfo(module_name, "CARD PAN is " + response);
                            myTrxContext.SetPan(response);
                        }

                    }
                } else {
                    if (C_err.Icc.ERR_ICC_NO_CARD == retIcc) {
                        C_logger_stdout.LogWarning(module_name, "No card present");
                    } else {
                        C_logger_stdout.LogError(module_name, "Error problem connecting card");
                    }
                }
                // Start a timer
                internalIhmController.StartTimerEvent(TIMER_1_SECONDS);
                break;

            case STATE_TRANSACTION_MAGSTRIPE:
                PosEmuUtils.DisplayLogInfo(internalLogController, "STATE TRANSACTION");
                DisplayLine(CenterMessage("TRANSACTION"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                DisplayLine(CenterMessage("PISTE"), POS_COLOR_GREY, 0, 150, FONT_CHAR_SIZE);
                DisplayLine(CenterMessage("EN COURS"), POS_COLOR_GREY, 0, 170, FONT_CHAR_SIZE);
                typeCurrentTrxTechno = PosEnums.PosTransactionTechno.TRX_MAG;
                // Start a timer
                internalIhmController.StartTimerEvent(TIMER_1_SECONDS);
                break;

            case STATE_TRANSACTION_CLESS:
                PosEmuUtils.DisplayLogInfo(internalLogController, "STATE TRANSACTION");
                DisplayLine(CenterMessage("TRANSACTION"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                DisplayLine(CenterMessage("CONTACTLESS"), POS_COLOR_GREY, 0, 150, FONT_CHAR_SIZE);
                DisplayLine(CenterMessage("EN COURS"), POS_COLOR_GREY, 0, 170, FONT_CHAR_SIZE);
                typeCurrentTrxTechno = PosEnums.PosTransactionTechno.TRX_CLESS;
                // Start a timer
                internalIhmController.StartTimerEvent(TIMER_1_SECONDS);
                break;

            case STATE_PIN_ENTRY:
                PosEmuUtils.DisplayLogInfo(internalLogController, "STATE PIN ENTRY: PIN=" + PinCode);
                str = strAmountInteger + "," + strAmountDecimal + " EUR";
                DisplayLine(CenterMessage("DEBIT"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                DisplayLine(str, POS_COLOR_GREY, 0, 130, FONT_CHAR_SIZE);
                DisplayLine(CenterMessage("SAISIR CODE:"), POS_COLOR_GREY, 0, 160, FONT_CHAR_SIZE);

                // Display PIN code (with stars *)
                String PinCodeBlind = BLIND_PIN.substring(0, PinCode.length());
                DisplayLine(CenterMessage(PinCodeBlind), POS_COLOR_GREY, 0, 190, FONT_CHAR_SIZE);

                break;

            case STATE_PIN_RESULT_OK:
                PosEmuUtils.DisplayLogInfo(internalLogController, "STATE PIN RESULT OK");
                DisplayLine(CenterMessage("CODE BON"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                break;

            case STATE_PIN_RESULT_NOK:
                PosEmuUtils.DisplayLogInfo(internalLogController, "STATE PIN RESULT KO");
                DisplayLine(CenterMessage("CODE FAUX"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                break;

            case STATE_AUTORISATION:
                PosEmuUtils.DisplayLogInfo(internalLogController, "STATE AUTORISATION");
                DisplayLine(CenterMessage("AUTORISATION"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                DisplayLine(CenterMessage("EN COURS"), POS_COLOR_GREY, 0, 150, FONT_CHAR_SIZE);
                break;

            case STATE_TRANSACTION_RESULT_OK:
                PosEmuUtils.DisplayLogInfo(internalLogController, "STATE TRX RESULT OK");
                DisplayLine(CenterMessage("PAIEMENT ACCEPTE"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                myTrxContext.SetTrxStatus(true);
                break;

            case STATE_TRANSACTION_RESULT_NOK:
                PosEmuUtils.DisplayLogInfo(internalLogController, "STATE TRX RESULT KO");
                DisplayLine(CenterMessage("PAIEMENT REFUSE"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                myTrxContext.SetTrxStatus(false);
                break;

            case STATE_PRINT_CUSTOMER_RECEIPT:
                PosEmuUtils.DisplayLogInfo(internalLogController, "STATE PRINT CUSTOMER RECEIPT");
                DisplayLine(CenterMessage("IMPRESSION EN COURS"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                DisplayLine(CenterMessage("VALIDEZ"), POS_COLOR_GREY, 0, 150, FONT_CHAR_SIZE);
                DisplayReceiptWindow(true);
                PrintReceipt(PosEnums.PosReceiptType.RECEIPT_CUSTOMER);
                PrintReceipt(PosEnums.PosReceiptType.RECEIPT_LINEFEED);
                break;

            case STATE_PRINT_MERCHANT_RECEIPT:
                PosEmuUtils.DisplayLogInfo(internalLogController, "STATE PRINT MERCHANT RECEIPT");
                DisplayLine(CenterMessage("IMPRESSION EN COURS"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                PrintReceipt(PosEnums.PosReceiptType.RECEIPT_MERCHANT);
                PrintReceipt(PosEnums.PosReceiptType.RECEIPT_LINEFEED);
                break;

            case STATE_CARD_REMOVED:
                PosEmuUtils.DisplayLogInfo(internalLogController, "STATE CARTE REMOVED");
                DisplayLine(CenterMessage("CARTE ARRACHEE"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                break;

            case STATE_WAIT_CARD_REMOVE:
                PosEmuUtils.DisplayLogInfo(internalLogController, "STATE WAIT CARD REMOVE");
                DisplayLine(CenterMessage("RETIREZ CARTE"), POS_COLOR_GREY, 0, 100, FONT_CHAR_SIZE);
                break;

            default:
                PosEmuUtils.DisplayLogInfo(internalLogController, "STATE DEFAULT");
                break;
        }
    }

    /*
     * POS Engine (state machine)
     */
    public void StartEngine(PosEnums.State stateToFix, boolean clearScreen) {
        StartEngine(stateToFix, clearScreen, PosEnums.PosEvent.NO_EVENT);
    }

    public void EventReceived(PosEnums.PosEvent receivedEvent, PosEnums.PosKeyCode keyValue) {

        // Update smart card status
        if (receivedEvent == PosEnums.PosEvent.ICC_INSERTED) {
            if (m_icc.IccIsCardPresent() == true) {
                m_icc.IccSetCardPresent(false);
            } else {
                m_icc.IccSetCardPresent(true);
            }
        }

        switch (currentState) {
            case STATE_IDLE:
                if (receivedEvent == PosEnums.PosEvent.KEY_PRESSED) {
                    if (IsNumeric(keyValue) == true) {
                        ClearAmount();
                        AddDigitToAmount(keyValue);
                        StartEngine(PosEnums.State.STATE_AMOUNT, true);
                    } else if (keyValue == PosEnums.PosKeyCode.NUM_MENU) {
                        StartEngine(PosEnums.State.STATE_MENU_SCREEN, true);
                    }
                } else if (receivedEvent == PosEnums.PosEvent.CHECKBOX_EVENT) {
                    m_icc = internalPosEmu.InitializeReaderModule();
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
                // Initialize transaction status
                transactionStatus = 0;
                
                // Select correct state according to the event
                if (receivedEvent == PosEnums.PosEvent.KEY_PRESSED) {
                    if (keyValue == PosEnums.PosKeyCode.NUM_CORR) {
                        RemoveDigitFromAmount();
                        StartEngine(PosEnums.State.STATE_AMOUNT, true);
                    }
                    if (keyValue == PosEnums.PosKeyCode.NUM_VAL) {
                        if (m_icc.IccIsCardPresent() == true) {
                            // Card already present
                            StartEngine(PosEnums.State.STATE_TRANSACTION_ICC, true, receivedEvent);
                        } else {
                            StartEngine(PosEnums.State.STATE_CARD_WAITING, true);
                        }
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
                StartEngine(PosEnums.State.STATE_AUTORISATION, true);
                // Start a timer
                internalIhmController.StartTimerEvent(TIMER_1_SECONDS);
                break;

            case STATE_AUTORISATION:
                if (strAmountDecimal.equals("00") == true) {
                    StartEngine(PosEnums.State.STATE_TRANSACTION_RESULT_OK, true);
                } else {
                    StartEngine(PosEnums.State.STATE_TRANSACTION_RESULT_NOK, true);
                }
                // Start a timer
                internalIhmController.StartTimerEvent(TIMER_2_SECONDS);
                break;

            case STATE_TRANSACTION_CLESS:
                StartEngine(PosEnums.State.STATE_AUTORISATION, true);
                // Start a timer
                internalIhmController.StartTimerEvent(TIMER_1_SECONDS);
                break;

            case STATE_PIN_ENTRY:

                if (m_icc.IccIsCardPresent() == false) {
                    // Card removed
                    StartEngine(PosEnums.State.STATE_CARD_REMOVED, true);

                    // Start a timer
                    internalIhmController.StartTimerEvent(TIMER_1_SECONDS);
                } else {

                    if (receivedEvent == PosEnums.PosEvent.KEY_PRESSED) {
                        if (keyValue == PosEnums.PosKeyCode.NUM_CANCEL) {
                            // Set transaction status to KO
                            transactionStatus = -1;
                            
                            // Cancel key pressed, check if card still present
                            if (m_icc.IccIsCardPresent() == false) {
                                // No card present, so move to idle
                                StartEngine(PosEnums.State.STATE_IDLE, true);
                            } else {
                                // Card still present, so move to remove card state
                                StartEngine(PosEnums.State.STATE_WAIT_CARD_REMOVE, true);
                            }
                        } else if (IsNumeric(keyValue)) {
                            // Numeric key pressed
                            PinCode += GetCharFromKeyValue(keyValue);
                            StartEngine(PosEnums.State.STATE_PIN_ENTRY, true);
                        } else if (keyValue == PosEnums.PosKeyCode.NUM_CORR) {
                            // Numeric key pressed
                            int position = PinCode.length() - 1;
                            if (position < 0) {
                                position = 0;
                            }
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

                            // Start a timer
                            internalIhmController.StartTimerEvent(TIMER_1_SECONDS);
                        }
                    }
                }
                break;

            case STATE_CARD_REMOVED:
                StartEngine(PosEnums.State.STATE_IDLE, true);
                break;

            case STATE_PIN_RESULT_OK:
                if (strAmountDecimal.equals("00") == true) {
                    StartEngine(PosEnums.State.STATE_TRANSACTION_RESULT_OK, true);
                } else {
                    StartEngine(PosEnums.State.STATE_TRANSACTION_RESULT_NOK, true);
                }

                // Disconnect card                
                m_icc.IccDisconnect();

                // Start a timer
                internalIhmController.StartTimerEvent(TIMER_1_SECONDS);
                break;

            case STATE_PIN_RESULT_NOK:
                PinCode = "";
                StartEngine(PosEnums.State.STATE_PIN_ENTRY, true);
                // Start a timer
                internalIhmController.StartTimerEvent(TIMER_1_SECONDS);
                break;

            case STATE_TRANSACTION_RESULT_OK:
            case STATE_TRANSACTION_RESULT_NOK:
                // First check card presence
                if ((PosEnums.PosTransactionTechno.TRX_ICC == typeCurrentTrxTechno) 
                        && (m_icc.IccIsCardPresent() == true)) {
                    // This is a smart card transaction, so ask for card removal
                    StartEngine(PosEnums.State.STATE_WAIT_CARD_REMOVE, true);
                } else {
                    StartEngine(PosEnums.State.STATE_PRINT_CUSTOMER_RECEIPT, true);
                    // Start a timer
                    internalIhmController.StartTimerEvent(TIMER_1_SECONDS);
                }
                break;

            case STATE_WAIT_CARD_REMOVE:                
                if (receivedEvent == PosEnums.PosEvent.ICC_INSERTED) {
                    if (transactionStatus == 0) {
                        StartEngine(PosEnums.State.STATE_PRINT_CUSTOMER_RECEIPT, true);
                        // Start a timer
                        internalIhmController.StartTimerEvent(TIMER_1_SECONDS); 
                    } else {
                        StartEngine(PosEnums.State.STATE_IDLE, true);
                    }
                }
                break;

            case STATE_PRINT_CUSTOMER_RECEIPT:
                if (receivedEvent == PosEnums.PosEvent.KEY_PRESSED) {
                    StartEngine(PosEnums.State.STATE_PRINT_MERCHANT_RECEIPT, true);
                    // Start a timer
                    internalIhmController.StartTimerEvent(TIMER_1_SECONDS);
                }
                break;

            case STATE_PRINT_MERCHANT_RECEIPT:
                StartEngine(PosEnums.State.STATE_IDLE, true);
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

    public void DisplayOutputLabel(String msg) {
        // Set POS screen
        Platform.runLater(() -> {
            internalIhmController.OutputLabel.setText(msg);
        });
    }

    public void SetCheckBox(int selected) {
        // Set POS screen
        Platform.runLater(() -> {
            internalIhmController.iccCardTypeChoiceBox.getSelectionModel().select(selected);
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

    private void DisplayReceiptWindow(boolean enable) {
        internalPosEmu.DisplayReceiptWindow(enable);
    }
    
    private void PrintReceiptClear() {
        DisplayReceiptWindow(false);
    }
    
    private void PrintReceipt(PosEnums.PosReceiptType typeReceipt) {
        if (typeReceipt == PosEnums.PosReceiptType.RECEIPT_LINEFEED) {
            internalReceiptController.ReceiptWindowAddLine("");            
        } else {
            String techno;
            internalReceiptController.ReceiptWindowAddLine("   CARTE BANCAIRE");
            if (typeCurrentTrxTechno == PosEnums.PosTransactionTechno.TRX_ICC) {
                internalReceiptController.ReceiptWindowAddLine(myTrxContext.GetAid());
                techno = "CB";
            } else if (typeCurrentTrxTechno == PosEnums.PosTransactionTechno.TRX_CLESS) {
                internalReceiptController.ReceiptWindowAddLine(myTrxContext.GetAid());
                techno ="SANS CONTACT";
            } else {
                techno ="CARTE A PISTE";                
            }
            internalReceiptController.ReceiptWindowAddLine(techno);
            internalReceiptController.ReceiptWindowAddLine(GetDateAndTime());
            internalReceiptController.ReceiptWindowAddLine("INGENICO PAYMENT");
            internalReceiptController.ReceiptWindowAddLine("75015 PARIS");
            internalReceiptController.ReceiptWindowAddLine("30004");
            internalReceiptController.ReceiptWindowAddLine("1234567");
            internalReceiptController.ReceiptWindowAddLine("95750393100413");
            if (typeReceipt == PosEnums.PosReceiptType.RECEIPT_CUSTOMER) {
                internalReceiptController.ReceiptWindowAddLine(myTrxContext.GetMaskedPan());
            } else {
                internalReceiptController.ReceiptWindowAddLine(myTrxContext.GetPan());            
            }
            internalReceiptController.ReceiptWindowAddLine("4c5f567fdc4c455c");
            internalReceiptController.ReceiptWindowAddLine("394 001 394024 001480");
            if ((typeCurrentTrxTechno == PosEnums.PosTransactionTechno.TRX_ICC) 
                || (typeCurrentTrxTechno == PosEnums.PosTransactionTechno.TRX_CLESS)) {
                internalReceiptController.ReceiptWindowAddLine("C @");
            } else {
                internalReceiptController.ReceiptWindowAddLine("S @");
            }
            internalReceiptController.ReceiptWindowAddLine("MONTANT");
            internalReceiptController.ReceiptWindowAddLine(myTrxContext.GetAmount());
            internalReceiptController.ReceiptWindowAddLine("DEBIT");
            if (myTrxContext.GetTrxStatus() == false) {
                internalReceiptController.ReceiptWindowAddLine("-- ABANDON --");
            } else {
                if (typeCurrentTrxTechno == PosEnums.PosTransactionTechno.TRX_MAG) {
                    // Add a section for signature
                    internalReceiptController.ReceiptWindowAddLine("   SIGNATURE");
                    internalReceiptController.ReceiptWindowAddLine("");
                    internalReceiptController.ReceiptWindowAddLine("");
                }                
            }
            if (typeReceipt == PosEnums.PosReceiptType.RECEIPT_CUSTOMER)
                internalReceiptController.ReceiptWindowAddLine(  "TICKET CLIENT");
            else
                internalReceiptController.ReceiptWindowAddLine(  "TICKET COMMERCANT");
            internalReceiptController.ReceiptWindowAddLine("A CONSERVER");
        }
    }

    private String GetDateAndTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String display = dtf.format(now);
        
        return display;
    }
}
