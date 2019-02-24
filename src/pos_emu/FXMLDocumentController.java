/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pos_emu;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;

/**
 *
 * @author balacahan
 */
public class FXMLDocumentController implements Initializable {

    private final String MSG_DISPLAY_VIRTUAL = "VIRTUAL";
    private final String MSG_DISPLAY_PCSC = "PC/SC";
    
    @FXML
    public Label OutputLabel;
    public Pane PosScreen;
    public ChoiceBox iccCardTypeChoiceBox;
    public Button ButtonMagstripe;
    public Button ButtonCless;
    public Button ButtonSmartCard;

    private PosEnums.PosEvent eventAvailable = PosEnums.PosEvent.NO_EVENT;
    private PosEnums.PosKeyCode eventKeyCode = PosEnums.PosKeyCode.NO_KEY;
    private long timerEndTime;
    private boolean timerRun = false;
    
    @FXML
    private void Button0Event(ActionEvent event) {
        OutputLabel.setText("BUTTON 0 PRESSED");
        eventAvailable = PosEnums.PosEvent.KEY_PRESSED;
        eventKeyCode = PosEnums.PosKeyCode.NUM_0;
    }
    @FXML
    private void Button1Event(ActionEvent event) {
        OutputLabel.setText("BUTTON 1 PRESSED");
        eventAvailable = PosEnums.PosEvent.KEY_PRESSED;
        eventKeyCode = PosEnums.PosKeyCode.NUM_1;
    }
    @FXML
    private void Button2Event(ActionEvent event) {
        OutputLabel.setText("BUTTON 2 PRESSED");
        eventAvailable = PosEnums.PosEvent.KEY_PRESSED;
        eventKeyCode = PosEnums.PosKeyCode.NUM_2;
    }
    @FXML
    private void Button3Event(ActionEvent event) {
        OutputLabel.setText("BUTTON 3 PRESSED");
        eventAvailable = PosEnums.PosEvent.KEY_PRESSED;
        eventKeyCode = PosEnums.PosKeyCode.NUM_3;
    }
    @FXML
    private void Button4Event(ActionEvent event) {
        OutputLabel.setText("BUTTON 4 PRESSED");
        eventAvailable = PosEnums.PosEvent.KEY_PRESSED;
        eventKeyCode = PosEnums.PosKeyCode.NUM_4;
    }
    @FXML
    private void Button5Event(ActionEvent event) {
        OutputLabel.setText("BUTTON 5 PRESSED");
        eventAvailable = PosEnums.PosEvent.KEY_PRESSED;
        eventKeyCode = PosEnums.PosKeyCode.NUM_5;
    }
    @FXML
    private void Button6Event(ActionEvent event) {
        OutputLabel.setText("BUTTON 6 PRESSED");
        eventAvailable = PosEnums.PosEvent.KEY_PRESSED;
        eventKeyCode = PosEnums.PosKeyCode.NUM_6;
    }
    @FXML
    private void Button7Event(ActionEvent event) {
        OutputLabel.setText("BUTTON 7 PRESSED");
        eventAvailable = PosEnums.PosEvent.KEY_PRESSED;
        eventKeyCode = PosEnums.PosKeyCode.NUM_7;
    }
    @FXML
    private void Button8Event(ActionEvent event) {
        OutputLabel.setText("BUTTON 8 PRESSED");
        eventAvailable = PosEnums.PosEvent.KEY_PRESSED;
        eventKeyCode = PosEnums.PosKeyCode.NUM_8;
    }
    @FXML
    private void Button9Event(ActionEvent event) {
        OutputLabel.setText("BUTTON 9 PRESSED");
        eventAvailable = PosEnums.PosEvent.KEY_PRESSED;
        eventKeyCode = PosEnums.PosKeyCode.NUM_9;
    }
    @FXML
    private void ButtonLEvent(ActionEvent event) {
        OutputLabel.setText("BUTTON L PRESSED");
        eventAvailable = PosEnums.PosEvent.KEY_PRESSED;
        eventKeyCode = PosEnums.PosKeyCode.NUM_F;
    }
    @FXML
    private void ButtonPointEvent(ActionEvent event) {
        OutputLabel.setText("BUTTON Point PRESSED");
        eventAvailable = PosEnums.PosEvent.KEY_PRESSED;
        eventKeyCode = PosEnums.PosKeyCode.NUM_POINT;
    }
    @FXML
    private void ButtonFEvent(ActionEvent event) {
        OutputLabel.setText("BUTTON F PRESSED");
        eventAvailable = PosEnums.PosEvent.KEY_PRESSED;
        eventKeyCode = PosEnums.PosKeyCode.NUM_MENU;
    }
    @FXML
    private void ButtonCancelEvent(ActionEvent event) {
        OutputLabel.setText("BUTTON CANCEL PRESSED");
        eventAvailable = PosEnums.PosEvent.KEY_PRESSED;
        eventKeyCode = PosEnums.PosKeyCode.NUM_CANCEL;
    }
    @FXML
    private void ButtonCorrEvent(ActionEvent event) {
        OutputLabel.setText("BUTTON CORR PRESSED");
        eventAvailable = PosEnums.PosEvent.KEY_PRESSED;
        eventKeyCode = PosEnums.PosKeyCode.NUM_CORR;
    }
    @FXML
    private void ButtonValidEvent(ActionEvent event) {
        OutputLabel.setText("BUTTON VALID PRESSED");
        eventAvailable = PosEnums.PosEvent.KEY_PRESSED;
        eventKeyCode = PosEnums.PosKeyCode.NUM_VAL;
    }    
    @FXML
    private void ButtonSmartCardEvent(ActionEvent event) {
        if (iccCardTypeChoiceBox.getValue() == MSG_DISPLAY_VIRTUAL) {
            OutputLabel.setText("SMART CARD INSERTED/REMOVED");
            eventAvailable = PosEnums.PosEvent.ICC_INSERTED;
        }
    }    
    @FXML
    private void ButtonClessEvent(ActionEvent event) {
        OutputLabel.setText("CONTACTLESS CARD PASSED");
        eventAvailable = PosEnums.PosEvent.CLESS_CARD;
    }    
    @FXML
    private void ButtonMagstripeEvent(ActionEvent event) {
        OutputLabel.setText("MAGSTRIPE SWIPPED");        
        eventAvailable = PosEnums.PosEvent.CARD_SWIPED;
    }    
    @FXML
    private void ButtonQuitEvent(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }    

    private void CheckTimerEvent() {
        if (timerRun == true) {
            // Check if end time is reached
            if (System.currentTimeMillis() >= timerEndTime) {            
                eventAvailable = PosEnums.PosEvent.TIMER_EVENT;
                timerRun = false;
                PosEmuUtils.DisplayLogInfo("Timer Event Occured");
            }
        } 
    }
    
    public void StartTimerEvent(long myTimerValue) {
        // Set timer end time
        timerEndTime = System.currentTimeMillis() + myTimerValue;
        timerRun = true;
        PosEmuUtils.DisplayLogInfo("Timer Event Start = " + timerEndTime);
   }
    
    public PosEnums.PosEvent IsEventAvailable()
    {
        // First check if a timer event has occured
        CheckTimerEvent();
    
        // Check if there is another event (keyboard, card, ...)
        PosEnums.PosEvent retEvent = eventAvailable;
        eventAvailable = PosEnums.PosEvent.NO_EVENT;
        return retEvent;
    }

    public PosEnums.PosKeyCode GetKeyCode()
    {        
        PosEnums.PosKeyCode retKeyCode = eventKeyCode;
        eventKeyCode = PosEnums.PosKeyCode.NO_KEY;
        return retKeyCode;
    }
        
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // output display
        OutputLabel.setText("DESK/5000 Emulator");
        
        // Buttons tooltype
        ButtonMagstripe.setTooltip(new Tooltip("Swipe a magstripe"));
        ButtonSmartCard.setTooltip(new Tooltip("Insert or Remove Smart Card"));
        ButtonCless.setTooltip(new Tooltip("Present a contactless card"));
        
        // Checkbox for Reader type
        iccCardTypeChoiceBox.setTooltip(new Tooltip("Select ICC Reader type"));
        iccCardTypeChoiceBox.setItems(FXCollections.observableArrayList(MSG_DISPLAY_VIRTUAL, MSG_DISPLAY_PCSC));
        iccCardTypeChoiceBox.getSelectionModel().select(0);

        // add a listener 
        iccCardTypeChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() { 
            // if the item of the list is changed 
            @Override
            public void changed(ObservableValue ov, Number value, Number new_value) 
            { 
                eventAvailable = PosEnums.PosEvent.CHECKBOX_EVENT;
            } 
        });
    }    
}
