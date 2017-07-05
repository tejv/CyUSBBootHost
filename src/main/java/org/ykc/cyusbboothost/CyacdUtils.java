package org.ykc.cyusbboothost;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.controlsfx.control.StatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.stage.FileChooser.ExtensionFilter;

public class CyacdUtils {
	public static final Logger logger = LoggerFactory.getLogger(CyacdUtils.class.getName());

	private static File openFile(Window win, ObservableList<ExtensionFilter> extensionFilterslist){
		FileChooser fileChooser = new FileChooser();
	    fileChooser.setTitle("Open .cyacd file");

	    File lastFile = Preferences.getLastOpenedFile();
	    if((lastFile != null) && (lastFile.exists()))
	    {
	    	fileChooser.setInitialDirectory(lastFile.getParentFile());
	    }
	    else{
	    	fileChooser.setInitialDirectory( new File(System.getProperty("user.home")));
	    }

	    fileChooser.getExtensionFilters().addAll(extensionFilterslist);
	    File file = fileChooser.showOpenDialog(win);

	    return file;
	}

	public static File open(Window win){

		ObservableList<ExtensionFilter> extensionFilters = FXCollections.observableArrayList();
		extensionFilters.add(new ExtensionFilter("cyacd Files(*.cyacd)", "*.cyacd"));
		File file =  openFile(win, extensionFilters);
	    if (file != null) {
	    	Preferences.setLastOpenedFile(file);
	    }
        return file;
	}

	public static CyacdData parse(File cyacdFile) {
		try {
			ObservableList<CyacdRow> rowList = FXCollections.observableArrayList();
			FileReader fr = new FileReader(cyacdFile);
		    BufferedReader br = new BufferedReader(fr);
		    String header = br.readLine();
		    String rowLine;
		    while ((rowLine = br.readLine()) != null) {
		       rowList.add(getRow(rowLine.trim()));
		    }
			return new CyacdData(getHeader(header), rowList);
		} catch (Exception e) {
			return null;
		}
	}

	private static CyacdHeader getHeader(String header) {
		String line = header.trim();
		String silId = "0x"+ line.substring(0, 8);
		String silRev = "0x" + line.substring(8,10);
		String chksumType = "0x" + line.substring(10,12);
		int siliconId = Utils.castLongtoUInt(Utils.parseStringtoNumber(silId));
		byte siliconRev = Utils.castLongtoByte(Utils.parseStringtoNumber(silRev));
		byte checksumType = Utils.castLongtoByte(Utils.parseStringtoNumber(chksumType));;

		return new CyacdHeader(siliconId, siliconRev, checksumType);
	}

	private static CyacdRow getRow(String rowLine) {
		ObservableList<Byte> flashBytes = FXCollections.observableArrayList();
		String line = rowLine.trim().substring(1);
		byte arrId = Utils.castLongtoByte(Utils.parseStringtoNumber("0x"+ line.substring(0, 2)));
		short rowNo = Utils.castLongtoShort(Utils.parseStringtoNumber("0x"+ line.substring(2, 6)));
		short len = Utils.castLongtoShort(Utils.parseStringtoNumber("0x"+ line.substring(6, 10)));
		String flash = line.substring(10, 10 + len* 2);
		byte chksum = Utils.castLongtoByte(Utils.parseStringtoNumber("0x"+ line.substring(10 + len*2)));
		for(int i = 0; i < len; i++){
			flashBytes.add(Utils.castLongtoByte(Utils.parseStringtoNumber("0x"+ flash.substring(i * 2, i * 2 + 2))));
		}
		return new CyacdRow(arrId, rowNo, len, flashBytes, chksum);
	}

	public static void dumpCyacdData(CyacdData cyData, TextArea txtAreaLog){
		txtAreaLog.clear();
		txtAreaLog.appendText(cyData.getCyHeader().getHeaderString());
		ObservableList<CyacdRow> rowList = cyData.getRowList();
		for(int i = 0; i < rowList.size(); i++){
			txtAreaLog.appendText(rowList.get(i).getRowString());
		}
	}
	
	public static byte[] getSecurityKey(String key, TextArea txtAreaLog){
		byte[] keyBytes = new byte[6];
		String line = key.substring(2);
		if(key.length() != 14){
			try {
				for(int i = 0; i< 6; i++){
					keyBytes[i] = Utils.castLongtoByte(Utils.parseStringtoNumber("0x"+ line.substring(i * 2, i * 2 + 2)));
				}
			} catch (Exception e) {
				txtAreaLog.appendText("Security key parsing failed.\n");
			}
		}
		return keyBytes;
	}
	
}
