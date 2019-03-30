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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;

/**
 *
 * @author balacahan
 */
public class FXML_LogWindowController implements Initializable {
    
    @FXML
    public ListView LogListView;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        LogListView.setEditable(false);
    }

    public void LogWindowAddMsg(String c) {
        Platform.runLater(() -> {
            LogListView.getItems().add(LogListView.getItems().size(), c);
            LogListView.scrollTo(c);
        });
    }
}
