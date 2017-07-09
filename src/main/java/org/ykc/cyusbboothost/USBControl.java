package org.ykc.cyusbboothost;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.usb.UsbDevice;
import javax.usb.UsbException;

import org.controlsfx.control.StatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ykc.usbmanager.USBManEvent;
import org.ykc.usbmanager.USBManListener;
import org.ykc.usbmanager.USBManager;

import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class USBControl implements USBManListener{
	public static final Logger logger = LoggerFactory.getLogger(USBControl.class.getName());
	private boolean isHwAttached = false;
	private boolean isBootloading = false;
	private ArrayList<UsbDevice> devList;
	private USBManager usbm;
	private UsbDevice dev;
	private int comboBoxDeviceSelIdx = -1;
	static private USBTransfer usbTransferTask;
	private ComboBox<String> cBoxDeviceList;
	private StatusBar statusBar;
	private int vid = 0x04B4;
	private int pid = 0xB71D;
	private CyacdData cyData;
	private TextArea txtAreaLog;
	private byte[] skey;
	private boolean isSkeyEnabled;


	public void setCyacdData(CyacdData cyData, byte[] skey) {
		this.cyData = cyData;
		this.skey = skey;
		isSkeyEnabled = true;
	}

	public void setCyacdData(CyacdData cyData) {
		this.cyData = cyData;
		isSkeyEnabled = false;
	}

	public int getVid() {
		return vid;
	}

	public void setVid(int vid) {
		this.vid = vid;
		updateDeviceList();
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
		updateDeviceList();

	}

	public boolean isBootloading() {
		return isBootloading;
	}

	public static USBTransfer getUsbTransferTask() {
		return usbTransferTask;
	}

	public USBControl(ComboBox<String> cBoxDeviceList, StatusBar statusBar, TextArea txtAreaLog) {
		try {
			usbm = USBManager.getInstance();
			usbTransferTask = new USBTransfer(statusBar, txtAreaLog);

			this.statusBar = statusBar;
			this.cBoxDeviceList = cBoxDeviceList;

			updateDeviceList();

			cBoxDeviceList.setOnAction((event) -> {
			    deviceSelectionChanged();
			});

			if(!devList.isEmpty()){
				cBoxDeviceList.getSelectionModel().clearAndSelect(0);
				deviceSelectionChanged();
			}

			usbm.addUSBManListener(this);

			Thread usbTransferThread = new Thread(usbTransferTask);
			usbTransferThread.start();
			this.txtAreaLog = txtAreaLog;

		} catch (SecurityException e) {
			logger.error("Error in creating USB manager");
		} catch (UsbException e) {
			logger.error("Error in creating USB manager");
		}
	}

	private void deviceSelectionChanged() {
		int selIdx = cBoxDeviceList.getSelectionModel().getSelectedIndex();

		if((selIdx >= 0) &&(selIdx != comboBoxDeviceSelIdx))
		{
			comboBoxDeviceSelIdx = selIdx;
			dev = devList.get(comboBoxDeviceSelIdx);
			usbTransferTask.setDevice(dev);
			statusBar.setText("Device Selected : " + dev.toString() + " -> " + "Boot Device Attached");
			isHwAttached = true;
			logger.info("Boot Device attached: " + dev.toString());
		}

	}

	private void updateDeviceList()
	{
		cBoxDeviceList.getItems().clear();
		devList = usbm.getDeviceList((short)vid, (short)pid);
		for(UsbDevice device : devList)
		{
			cBoxDeviceList.getItems().add(device.toString());
			if(!isHwAttached){
				cBoxDeviceList.getSelectionModel().select(0);
			}
		}
	}

	@Override
	public void deviceAttached(USBManEvent arg0) {
		Platform.runLater(() -> {
			updateDeviceList();
        });
	}

	@Override
	public void deviceDetached(USBManEvent e) {
		Platform.runLater(() -> {
			if(isHwAttached == true)
			{
				if(USBManager.isDevicePresent(USBManager.filterDeviceList(e.getDeviceList(), (short)vid, (short)pid), dev) == false)
				{
					usbTransferTask.stop();
					isHwAttached = false;
					isBootloading = false;
					statusBar.setText("Boot Device Removed \n");
					comboBoxDeviceSelIdx = -1;
				}
			}
			updateDeviceList();
        });
	}

	public void downloadFW() {
		if(isHwAttached == false){
			statusBar.setText("Boot Device not attached: Command failed.");
			return;
		}
		if(cyData == null){
			statusBar.setText("Error: cyacd file not opened.");
			return;
		}
		boolean result;
		usbTransferTask.setVidPid((short)vid, (short)pid);
		if(isSkeyEnabled){
			usbTransferTask.start(cyData, skey);
		}
		else {
			usbTransferTask.start(cyData);
		}
	}

	public void terminate() {
		usbTransferTask.stop();
		usbTransferTask.terminate();
	}
}
