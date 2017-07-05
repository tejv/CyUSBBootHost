package org.ykc.cyusbboothost;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import org.controlsfx.control.StatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MainWindowController implements Initializable{

	public static final Logger logger = LoggerFactory.getLogger(MainWindowController.class.getName());

    @FXML
    private BorderPane bPaneMainWindow;

    @FXML
    private Button bOpen;

    @FXML
    private Button bDownload;

    @FXML
    private Button bAbout;

    @FXML
    private StatusBar statusBar;

    @FXML // fx:id="cBoxDeviceList"
    private ComboBox<String> cBoxDeviceList; // Value injected by FXMLLoader

    @FXML
    private TextArea txtAreaLog;

    @FXML
    private TextField txtVid;

    @FXML
    private TextField txtPid;

    @FXML
    private TextField txtSkey;

    @FXML
    private Button bClear;

    @FXML
    private CheckBox chkSkey;

    private USBControl usbcontrol;
    private Stage myStage;
    private CyacdData cyData;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		Preferences.loadPreferences();
		bOpen.setGraphic(new ImageView(new Image("/open.png")));
		bOpen.setTooltip(new Tooltip("Open cyacd file"));
		bDownload.setGraphic(new ImageView(new Image("/download.png")));
		bDownload.setTooltip(new Tooltip("Download FW"));
		bAbout.setGraphic(new ImageView(new Image("/info.png")));
		bAbout.setTooltip(new Tooltip("About CyUSBBootHost"));
		bClear.setGraphic(new ImageView(new Image("/clear.png")));
		bClear.setTooltip(new Tooltip("Clear Log"));

	    txtAreaLog.appendText("Warning\n");
	    txtAreaLog.appendText("On Windows machine make sure libusb driver is bind for boot mode as well\n");

	    openCyacd(Preferences.getLastOpenedFile());
	    usbcontrol = new USBControl(cBoxDeviceList, statusBar, txtAreaLog);
	}

    private void openCyacd(File cyacdFile) {
		if(cyacdFile != null && cyacdFile.exists()){
			String msg = "File opened: " + cyacdFile.getAbsolutePath();
			statusBar.setText(msg);
			txtAreaLog.appendText(msg + "\n");
			cyData = CyacdUtils.parse(cyacdFile);
			//CyacdUtils.dumpCyacdData(cyData, txtAreaLog);
		}
	}

	@FXML
    void openOnDragOver(DragEvent event) {
    	 Dragboard db = event.getDragboard();
         if (db.hasFiles()) {
             event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
         } else {
             event.consume();
         }
    }

    @FXML
    void openOnDragDrop(DragEvent event) {
    	Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {

            String filePath = null;
            for (File file:db.getFiles()) {
            	if(Utils.getFileExtension(file).equals("cyacd")){
            		success = true;
            		Preferences.setLastOpenedFile(file);
            		openCyacd(file);
            		break;
            	}
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }



    @FXML
    void openCyacd(ActionEvent event) {
    	openCyacd(CyacdUtils.open(bPaneMainWindow.getScene().getWindow()));
    }

    @FXML
    void downloadFw(ActionEvent event) {
    	txtPid.setDisable(true);
    	txtVid.setDisable(true);
    	bOpen.setDisable(true);
    	usbcontrol.setPid(Utils.getShortFromLong(Utils.parseStringtoNumber(txtPid.getText())));
    	usbcontrol.setVid(Utils.getShortFromLong(Utils.parseStringtoNumber(txtVid.getText())));
    	if(chkSkey.isSelected()){
    		usbcontrol.setCyacdData(cyData, CyacdUtils.getSecurityKey(txtSkey.getText(), txtAreaLog));
    	}
    	else{
    		usbcontrol.setCyacdData(cyData);
    	}
    	usbcontrol.downloadFW();
    	txtPid.setDisable(false);
    	txtVid.setDisable(false);
    	bOpen.setDisable(false);
    }

    @FXML
    void clearLog(ActionEvent event) {
    	txtAreaLog.clear();
    }

    @FXML
    void pidChanged(ActionEvent event) {
    	usbcontrol.setPid(Utils.getShortFromLong(Utils.parseStringtoNumber(txtPid.getText())));
    }

    @FXML
    void vidChanged(ActionEvent event) {
    	usbcontrol.setVid(Utils.getShortFromLong(Utils.parseStringtoNumber(txtVid.getText())));
    }

    @FXML
    void showAboutMe(ActionEvent event) {
    	displayAboutMe();
    }

	private void displayAboutMe() {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = getClass().getResource("/version.properties").openStream();
			prop.load(input);
			String ver = prop.getProperty("MAJOR_VERSION") + "."+ prop.getProperty("MINOR_VERSION") + "." + prop.getProperty("BUILD_NO");
			MsgBox.display("About Me", "CyUSBBootHost -> Cypress USB Bootloader Host\nVersion: "+ ver +"\nAuthor: Tejender Sheoran\nEmail: tejendersheoran@gmail.com, teju@cypress.com\nCopyright(C) (2016-2018) Tejender Sheoran\nThis program is free software. You can redistribute it and/or modify it\nunder the terms of the GNU General Public License Ver 3.\n<http://www.gnu.org/licenses/>");

		} catch (IOException e) {

		}
		finally{
			if(input != null){
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private void appClosing(){
		Preferences.storePreferences();
	}

	public void setStage(Stage stage) {
	    myStage = stage;
		myStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		      public void handle(WindowEvent we) {
		    	  appClosing();
		    	  Platform.exit();
		      }
		  });
	}
}




