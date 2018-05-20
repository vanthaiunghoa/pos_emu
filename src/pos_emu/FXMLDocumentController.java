/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pos_emu;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

/**
 *
 * @author balacahan
 */
public class FXMLDocumentController implements Initializable {
   
    @FXML
    public Label OutputLabel;
    public Pane PosScreen;    
    private PosEnums.PosEvent eventAvailable = PosEnums.PosEvent.NO_EVENT;
    private PosEnums.PosKeyCode eventKeyCode = PosEnums.PosKeyCode.NO_KEY;
    
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
        OutputLabel.setText("SMART CARD INSERTED");
    }    
    @FXML
    private void ButtonClessEvent(ActionEvent event) {
        OutputLabel.setText("CONTACTLESS CARD PASSED");
    }    
    @FXML
    private void ButtonMagstripeEvent(ActionEvent event) {
        OutputLabel.setText("MAGSTRIPE SWIPPED");        
    }    
    @FXML
    private void ButtonQuitEvent(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }    

    public PosEnums.PosEvent IsEventAvailable()
    {
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
        // TODO
        OutputLabel.setText("DESK/5000 Emulator");
    }    
    
}
