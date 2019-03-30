/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pos_emu;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

/**
 *
 * @author balacahan
 */
public class FXML_ReceiptWindowController implements Initializable {
    
    @FXML
    public ListView ReceiptListView;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ReceiptListView.setEditable(false);
    }

    public void ReceiptWindowAddLine(String c) {
        Platform.runLater(() -> {
            ReceiptListView.getItems().add(ReceiptListView.getItems().size(), c);
            ReceiptListView.scrollTo(c);
        });
    }
    
    public void ReceiptWindowClear() {
        Platform.runLater(() -> {
            ReceiptListView.getItems().clear();
        });
    }
}
