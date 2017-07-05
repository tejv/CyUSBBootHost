package org.ykc.cyusbboothost;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.ZonedDateTime;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.control.Tab;

public class Preferences {
	public static final Logger logger = LoggerFactory.getLogger(Preferences.class.getName());
	private static File lastOpenedFile = null;


	public static File getLastOpenedFile() {
		return lastOpenedFile;
	}

	public static void setLastOpenedFile(File lastOpenedFile) {
		Preferences.lastOpenedFile = lastOpenedFile;
	}

	public static boolean storePreferences()
	{
	    try {
			File prefDir = new File(System.getProperty("user.home"), "CyUSBBootHost/preferences");
			if (! prefDir.exists()) {
				prefDir.mkdirs();
			}
			File prefFile = new File(System.getProperty("user.home"), "CyUSBBootHost/preferences/app.xml");
			if (! prefFile.exists()) {
				prefFile.createNewFile();
			}
			return createPreferences(prefFile);
		} catch (Exception e) {

		}
		return false;
	}

	public static org.jdom2.Document getJDOM2Doc(File input) {
		try {
			SAXBuilder saxBuilder = new SAXBuilder();
			org.jdom2.Document document = saxBuilder.build(input);
			return document;
		}
		catch(Exception ex){

		}
		return null;
	}

	public static boolean loadPreferences(){

	    try {
			File prefFile = new File(System.getProperty("user.home"), "CyUSBBootHost/preferences/app.xml");
			if (! prefFile.exists()) {
				return false;
			}
			Document prefDoc = getJDOM2Doc(prefFile);
			if(prefDoc == null)
			{
				return false;
			}
			Element prefElement = prefDoc.getRootElement();
			String lastOpenFile = prefElement.getChildText("lastOpenedFile");
			if((!lastOpenFile.equals("null")) && (new File(lastOpenFile).exists()))
			{
				lastOpenedFile = new File(lastOpenFile);
			}

			return true;
		} catch (Exception e) {

		}
		return false;
	}

	private static boolean createPreferences(File preFile){
		Document doc = new Document();
		Element theRoot = new Element("preferences");
		doc.setRootElement(theRoot);

		Element lastOpenFile = new Element("lastOpenedFile");
		if(lastOpenedFile != null){
			lastOpenFile.setText(lastOpenedFile.getAbsolutePath());
		}
		else{
			lastOpenFile.setText("null");
		}
		theRoot.addContent(lastOpenFile);

		XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
		try {
			FileOutputStream x = new FileOutputStream(preFile);
			xmlOutput.output(doc, x);
			x.close();
			return true;
		} catch (IOException e) {
		}
		return false;
	}
}

