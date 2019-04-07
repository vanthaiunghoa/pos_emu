/*
 * POS_EMU
 * This is a Pos Of Sale emulator 
 *
 * This is the command interpreter 
 */
package pos_emu;

import c_common.C_err;
import c_common.C_logger_stdout;
import c_icc.C_icc;
import c_icc.C_icc_pcsc;
import c_icc.C_icc_virtual;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
// For GSON library (JSON parse)
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.text.Font;
import javafx.stage.WindowEvent;

class pixel_position {
    double x;
    double y;
    
    double width;
    double height;
    
    pixel_position() {
        x = 0;
        y = 0;
        height = 0;
        width = 0;
    }
    pixel_position(double xx, double yy, double ww, double hh) {
        x = xx;
        y = yy;
        width = ww;
        height = hh;
    }
}

/**
 *
 * @author balacahan
 */
public class Pos_emu extends Application {
    // Constant for port to listen
    public int TCP_IP_LISTENER_PORT = 8000;
    // Configuration file name
    final String param_json_path = "src/pos_emu/config/param_pos_emu.json";
    // FXML Document and controller
    FXMLDocumentController ihmController;
    FXML_LogWindowController logWindowController;
    FXML_ReceiptWindowController internalReceiptWindowController;
    private CommandInterpreter internalCommandInterpreter;
    private PosEmuEngine internalPosEmuEngine;
    private C_icc internal_m_icc;

    private C_err.Icc retIcc;
    private String module_name;

    // For log window
    Stage log_stage;
    Scene log_scene;

    // For receipt window
    Stage receipt_stage;
    Scene receipt_scene;

    // Parameters from configuration file
    private ParamConfigFile config_param_data;
    
    // Smart card card presence/absence detection
    private boolean bSmartCardPresent = false;

    // To manage sound of click
    String clickSoundPath = "/pos_emu/resource/UI_Blip_01.wav";
    AudioClip mClickSound;
    
    @Override
    public void start(Stage stage) throws Exception {
        // Initialize click sound
        mClickSound = new AudioClip(this.getClass().getResource(clickSoundPath).toExternalForm());

        // Display the log window, first
        pixel_position logWindowPos = DisplayLogWindow();        
        double mainWindowsPos_X = logWindowPos.x - 500; // logWindowPos.width + 5;
        double mainWindowsPos_Y = logWindowPos.y - 10;
        
        //Set up instance instead of using static load() method
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
        Parent root = loader.load();

        // Start the logger
        PosEmuUtils.DisplayLogInfo(logWindowController, "Starting Logging ...");

        // Creation of the Stage (Window) for the receipts
        CreateReceiptWindow();        
        
        // First display working directory
        PosEmuUtils.DisplayLogInfo(logWindowController, "Working Directory = " + System.getProperty("user.dir"));
               
        //Now we have access to getController() through the instance... don't forget the type cast
        ihmController = (FXMLDocumentController)loader.getController();
        
        // Create instance of CommandInterpreter
        internalCommandInterpreter = new CommandInterpreter(ihmController, logWindowController);
        
        // Load the custom font
        Font.loadFont(Pos_emu.class.getResource("/pos_emu/font/Monospace.ttf").toExternalForm(),16);
        
        // Create the scene
        Scene scene = new Scene(root);
        // Window without Minimize, Maximize buttons
        stage.initStyle(StageStyle.DECORATED);
        // Window not resizable
        stage.setResizable(false);
      
        // Adding an icon 
        Image appliIcon = new Image(Pos_emu.class.getResourceAsStream( "resource/icon.png" ));
        stage.getIcons().add(appliIcon); 
        
        // Read Configuration file 
        config_param_data = ReadJsonConfigFile();

        // Initialize ICC Reader Module
        InitializeReaderModule();
        // Create instante of Engine
        internalPosEmuEngine = new PosEmuEngine(this, ihmController, config_param_data, internal_m_icc, logWindowController, internalReceiptWindowController);

        // Set operations when window is closed
        stage.setOnCloseRequest((WindowEvent we) -> {
            Platform.exit();
            System.exit(0);
        });
                
        // The event listener is the thread which waits for events like key press, card insertion, etc.
        StartEventListener(ihmController, internalPosEmuEngine);
        
        // Start the terminal boot
        StartPOSBoot();        
        
        // Start the IDLE Screen
        internalPosEmuEngine.StartEngine(PosEnums.State.STATE_IDLE, true);               

        // Add the scene to the stage and launch the stage
        stage.setScene(scene);
        stage.setTitle("INGENICO POS EMULATOR");
        stage.setX(mainWindowsPos_X);
        stage.setY(mainWindowsPos_Y);
        stage.show();
    }

    /**
     * Display the window for the log (different stage)
     * 
     * @return position of the created stage
     */
    public pixel_position DisplayLogWindow() {
        pixel_position stage_pos = new pixel_position();
        try {
            log_stage = new Stage();
            FXMLLoader fxmlLoader=new FXMLLoader(getClass().getResource("FXML_LogWindow.fxml"));
            Pane root = fxmlLoader.load();
            log_scene = new Scene(root);
            log_stage.setScene(log_scene);
            log_stage.setTitle("Log Window");
            log_stage.initStyle(StageStyle.DECORATED);

            // Now we have access to getController() through the instance... don't forget the type cast
            logWindowController = (FXML_LogWindowController)fxmlLoader.getController();

            // Window not resizable
            log_stage.setResizable(false);

            // Show the window
            log_stage.show();
        } catch (IOException e) {
            PosEmuUtils.DisplayLogInfo(logWindowController, "Error opening window log FXML file !");
        }

        stage_pos.x = log_stage.getX();
        stage_pos.y = log_stage.getY();

        stage_pos.width = log_stage.getWidth();
        stage_pos.height = log_stage.getHeight();

        return stage_pos;
    }

    /**
     * Create the window for the receipts (different stage)
     * 
     * @return position of the created stage
     */
    public pixel_position CreateReceiptWindow() {
        pixel_position stage_pos = new pixel_position();
        try {
            receipt_stage = new Stage();
            FXMLLoader fxmlLoader=new FXMLLoader(getClass().getResource("FXML_ReceiptWindow.fxml"));
            Pane root = fxmlLoader.load();
            receipt_scene = new Scene(root);
            receipt_stage.setScene(receipt_scene);
            receipt_stage.setTitle("Receipt Window");
            receipt_stage.initStyle(StageStyle.DECORATED);

            // Now we have access to getController() through the instance... don't forget the type cast
            internalReceiptWindowController = (FXML_ReceiptWindowController)fxmlLoader.getController();

            // Window not resizable
            receipt_stage.setResizable(false);

        } catch (IOException e) {
            PosEmuUtils.DisplayLogError(logWindowController, "Error opening window Receipt FXML file !");
        }

        stage_pos.x = receipt_stage.getX();
        stage_pos.y = receipt_stage.getY();

        stage_pos.width = receipt_stage.getWidth();
        stage_pos.height = receipt_stage.getHeight();

        return stage_pos;
    }

    /**
     * Display or hide the receipt window (stage)
     * @param display =true to display the window, =false to hide it
     */
    public void DisplayReceiptWindow(boolean display) {
        if (display == true) {
            if (!receipt_stage.isShowing()) {
                Platform.runLater(() -> {
                    receipt_stage.show();
                });
                internalReceiptWindowController.ReceiptWindowClear();
            }
        } else {
            if (receipt_stage.isShowing())
                Platform.runLater(() -> {
                    receipt_stage.hide();
                });
        }
    }   
    
    /**
     * Initialize the Reader module according to the checkbox
     * 
     * @return Icc class to be used
     */
    public C_icc InitializeReaderModule() {
        if (ihmController.iccCardTypeChoiceBox.getValue() == "VIRTUAL") {
            PosEmuUtils.DisplayLogInfo(logWindowController, "Virtual Reader Selected");
            initializeIccModule(C_icc.SmartCardManagementType.SMARTCARD_VIRTUAL);
        } else {
            PosEmuUtils.DisplayLogInfo(logWindowController, "PC/SC Reader Selected");
            retIcc = initializeIccModule(C_icc.SmartCardManagementType.SMARTCARD_PCSC);
            if (retIcc != C_err.Icc.ERR_ICC_OK) {
                internalPosEmuEngine.DisplayOutputLabel("ERROR ACCESSING PCSC");
                internalPosEmuEngine.SetCheckBox(0);
                PosEmuUtils.DisplayLogError(logWindowController, "Error accessing PCSC reader");
                initializeIccModule(C_icc.SmartCardManagementType.SMARTCARD_VIRTUAL);
            }
        }
        return internal_m_icc;
    }    
    
    private C_err.Icc initializeIccModule(C_icc.SmartCardManagementType smartCardType) {
        // According to the parameter, use PC/SC or virtual Smart-Card
        if (smartCardType == C_icc.SmartCardManagementType.SMARTCARD_PCSC) {
            // Create ICC smart card component based on PCSC
            internal_m_icc = new C_icc_pcsc("m_icc");
        } else {
            // Create virtual card (no need for a real reader)
            internal_m_icc = new C_icc_virtual("m_icc");
        }

        // Initialize module
        module_name = internal_m_icc.getModuleName();
        C_logger_stdout.LogInfo(module_name, "Module Created");

        internal_m_icc.initModule();
        C_logger_stdout.LogInfo(module_name, "Module Initialization Done");

        retIcc = internal_m_icc.IccConnectReader(null);
        if (C_err.Icc.ERR_ICC_OK == retIcc) {
            C_logger_stdout.LogInfo(module_name, "Reader Connected : " + internal_m_icc.IccGetReaderName());
        } else {
            C_logger_stdout.LogError(module_name, "Problem connecting to reader");
        }

        return retIcc;
    }
    
    /**
     * Read the configuration file : param.json
     * 
     * @return ParamConfigFile parameters coming from the file
     * 
     * @throws java.io.FileNotFoundException
     */
    private ParamConfigFile ReadJsonConfigFile() throws FileNotFoundException
    {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(param_json_path));        
        } catch(FileNotFoundException e) 
        {
            PosEmuUtils.DisplayLogError(logWindowController, "Configuration File not found ! - error " + e);
        }
        
        // Parse the JSON file
        Gson gson = new Gson();
        return gson.fromJson(bufferedReader, ParamConfigFile.class);        
    }
            
    /**
     * Start the POS boot sequence
     */
    @SuppressWarnings("SleepWhileInLoop")
    private void StartPOSBoot()
    {
        // Set font to white
        ihmController.PosScreen.setStyle("-fx-background-color:white");
        
        // Start TCP/IP listener
        int port = Integer.parseInt(config_param_data.GetPosPort());
        TCPServer tcpListener = new TCPServer(internalCommandInterpreter, port, logWindowController);
        tcpListener.StartTCPServer();
        
        // Wait for a command
        new Thread(() -> {
            do {
                try {
                    // Infinite loop to be always listening 
                    PosEmuUtils.DisplayLogInfo(logWindowController, "Wait on TCP listener (" + port + ")");
                    tcpListener.WaitTCPMessage();
                    
                    // Pause for 100 ms between each thread, i.e. each command
                    Thread.sleep(1);
                } catch (Exception e) {
                    PosEmuUtils.DisplayLogError(logWindowController, "Network Error: " + e);
                    
                    // Restart TCP/IP listener                   
                    tcpListener.RestartTCPServer();
                }                
            } while (true);
        }).start();
    }

    /**
     * Waits for an event
     */
    private void StartEventListener(FXMLDocumentController ihmController, PosEmuEngine internalPosEmuEngine) {
       // Wait for a command
        new Thread(() -> {
            do {
                try {
                    // Check if an event is available
                    PosEnums.PosEvent theEvent = ihmController.IsEventAvailable();

                    // Check smart card event
                    if (theEvent == PosEnums.PosEvent.NO_EVENT) {
                        theEvent = CheckSmartCardEvent();
                    }
                    
                    // Manage the event
                    if (theEvent != PosEnums.PosEvent.NO_EVENT)
                    {
                        // there is an event
                        switch(theEvent)
                        {
                            case KEY_PRESSED:
                                mClickSound.play();
                                PosEnums.PosKeyCode theKey = ihmController.GetKeyCode();
                                internalPosEmuEngine.EventReceived(theEvent, theKey);
                                break;
                            case ICC_INSERTED:
                            case CARD_SWIPED:
                            case CLESS_CARD:
                                internalPosEmuEngine.EventReceived(theEvent, PosEnums.PosKeyCode.NO_KEY);
                                break;
                            case TIMER_EVENT:
                                internalPosEmuEngine.EventReceived(theEvent, PosEnums.PosKeyCode.NO_KEY);
                                break;
                            case CHECKBOX_EVENT:
                                internalPosEmuEngine.EventReceived(theEvent, PosEnums.PosKeyCode.NO_KEY);
                                break;
                            default:
                                break;
                        }
                        // Update Smart card status
                        bSmartCardPresent = internal_m_icc.IccIsCardPresent();
                    }
                    // Wait 10 ms, not to use all CPU resource
                    Thread.sleep(10);
                    // Update the time
                    internalPosEmuEngine.UpdateTimeOnScreen(false);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Pos_emu.class.getName()).log(Level.SEVERE, null, ex);
                }
            } while (true);
        }).start();
   }  

    private PosEnums.PosEvent CheckSmartCardEvent() {
        PosEnums.PosEvent ev = PosEnums.PosEvent.NO_EVENT;
        if (bSmartCardPresent != internal_m_icc.IccIsCardPresent()) {
            PosEmuUtils.DisplayLogInfo(logWindowController, "difference");
            ev = PosEnums.PosEvent.ICC_INSERTED;
        }
        
        return ev;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }    
    
}
