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
    public Label PromptText;
    public Pane PosScreen;
    
    @FXML
    private void Button0Event(ActionEvent event) {
        OutputLabel.setText("BUTTON 0 PRESSED");
    }
    @FXML
    private void Button1Event(ActionEvent event) {
        OutputLabel.setText("BUTTON 1 PRESSED");
    }
    @FXML
    private void Button2Event(ActionEvent event) {
        OutputLabel.setText("BUTTON 2 PRESSED");
    }
    @FXML
    private void Button3Event(ActionEvent event) {
        OutputLabel.setText("BUTTON 3 PRESSED");
    }
    @FXML
    private void Button4Event(ActionEvent event) {
        OutputLabel.setText("BUTTON 4 PRESSED");
    }
    @FXML
    private void Button5Event(ActionEvent event) {
        OutputLabel.setText("BUTTON 5 PRESSED");
    }
    @FXML
    private void Button6Event(ActionEvent event) {
        OutputLabel.setText("BUTTON 6 PRESSED");
    }
    @FXML
    private void Button7Event(ActionEvent event) {
        OutputLabel.setText("BUTTON 7 PRESSED");
    }
    @FXML
    private void Button8Event(ActionEvent event) {
        OutputLabel.setText("BUTTON 8 PRESSED");
    }
    @FXML
    private void Button9Event(ActionEvent event) {
        OutputLabel.setText("BUTTON 9 PRESSED");
    }
    @FXML
    private void ButtonLEvent(ActionEvent event) {
        OutputLabel.setText("BUTTON L PRESSED");
    }
    @FXML
    private void ButtonPointEvent(ActionEvent event) {
        OutputLabel.setText("BUTTON Point PRESSED");
    }
    @FXML
    private void ButtonFEvent(ActionEvent event) {
        OutputLabel.setText("BUTTON F PRESSED");
    }
    @FXML
    private void ButtonCancelEvent(ActionEvent event) {
        OutputLabel.setText("BUTTON CANCEL PRESSED");
    }
    @FXML
    private void ButtonCorrEvent(ActionEvent event) {
        OutputLabel.setText("BUTTON CORR PRESSED");
    }
    @FXML
    private void ButtonValidEvent(ActionEvent event) {
        OutputLabel.setText("BUTTON VALID PRESSED");
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        OutputLabel.setText("DESK/5000 Emulator");
    }    
    
}
