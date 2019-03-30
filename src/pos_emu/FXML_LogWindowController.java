/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pos_emu;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

/**
 *
 * @author balacahan
 */
public class FXML_LogWindowController implements Initializable {
    
    @FXML
    public Label LabelStart;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        LabelStart.setText("BUTTON 0 PRESSED");
    }    

}
