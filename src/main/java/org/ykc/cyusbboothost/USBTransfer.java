package org.ykc.cyusbboothost;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.print.attribute.Size2DSyntax;
import javax.usb.UsbDevice;
import javax.usb.UsbException;

import org.controlsfx.control.StatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ykc.cyusbboothost.BootloaderUtils.BootStatus;
import org.ykc.usbmanager.*;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextArea;


public class USBTransfer implements Runnable{
	public static final Logger logger = LoggerFactory.getLogger(USBTransfer.class.getName());
	private static final int FLASH_ROW_PART_LEN = 53;
	public short vid;
	public short pid;
	static  final byte IN_EP = (byte)0x82;
	static  final byte OUT_EP = (byte)0x1;
	static  final int MAX_READ_SIZE = 64;
	private byte[] tmpInArray = new byte[MAX_READ_SIZE];
	private boolean isBootloading = false;
	private UsbDevice dev = null;
	private StatusBar statusBar;
	private CyacdData cyData;
	private TextArea txtAreaLog;
	private byte[] skey;
	private boolean isSkeyEnabled;

	USBTransfer(StatusBar statusBar, TextArea txtAreaLog)
	{
		this.statusBar = statusBar;
		this.txtAreaLog = txtAreaLog;
	}

	public void setDevice(UsbDevice newDev)
	{
		dev = newDev;
	}

	public UsbDevice getDevice()
	{
		return dev;
	}

	public void setVidPid(short vid, short pid) {
		this.vid = vid;
		this.pid = pid;
	}


	public boolean start(CyacdData cyData, byte[] skey)
	{
		this.cyData = cyData;
		this.skey = skey;
		isSkeyEnabled = true;
		isBootloading = true;
		return true;
	}

	public void start(CyacdData cyData) {
		this.cyData = cyData;
		isSkeyEnabled = false;
		isBootloading = true;
	}


	public void stop()
	{
		isBootloading = false;
	}

	@Override
	public void run() {

		try {
			Thread.sleep(10);
		} catch (InterruptedException e1) {
		}

		while(true)
		{
			if(isBootloading)
			{
				try {

					if(!enterBoot())
						throw new RuntimeException();
					if(!programAndVerifyRows())
						throw new RuntimeException();
					if(!exitBootloader())
						throw new RuntimeException();
					isBootloading = false;
				} catch (Exception e) {
					isBootloading = false;
				    logDetails("Error in bootloading.\n");

				}

			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {

			}

		}
	}

	public boolean isBootloading()
	{
		return isBootloading;
	}

	private byte[] formEnterBootCmd() {
		if(isSkeyEnabled){
			return BootloaderUtils.formpacket((byte)0x38, skey);
		}
		else{
			return BootloaderUtils.formpacket((byte)0x38, null);
		}
	}

	private boolean enterBoot(){
		updateProgressBar(0);
		byte[] tmp = formEnterBootCmd();

		// Debug
//		for(int i = 0 ; i < tmp.length; i++){
//			logDetails( Utils.byteToHex(tmp[i]) + " ");
//		}
//		logDetails("\n");
		//Debug

		int outSize = USBManager.epXfer(dev, OUT_EP, tmp);
		if( outSize != tmp.length)
		{
			logDetails("Enter Boot command fail. Please make sure libusb driver is binded.\n");
			return false;
		}
		int size = USBManager.epXfer(dev, IN_EP, tmpInArray);
		BootStatus status = BootloaderUtils.getStatus(tmpInArray);
		if(status.equals(BootStatus.SUCCESS)){
			logDetails("Starting boot at "+ Utils.getCurrentTime() +"\n");
			if(BootloaderUtils.getSiID(tmpInArray) != cyData.getCyHeader().getSiliconId()){
				logDetails("Silicon ID mismatch.\n");
				return false;
			}
			return true;
		}

		logDetails("Enter Boot response fail. " + status.name() +"\n");
		return false;
	}


	private boolean programAndVerifyRows() {
		int lines = cyData.getRowList().size();
		for(int i = 0 ; i < lines; i++){
//			logDetails("Line: "+i +"\n");
			if(!programAndVerifyRow(i)){
				return false;
			}
			updateProgressBar((double)i/(double)lines);
		}
		return true;

	}

	private boolean programAndVerifyRow(int idx){
		CyacdRow row = cyData.getRowList().get(idx);
		int dataLen = row.getDataLen();
		int partCount = Utils.roundUp(dataLen, FLASH_ROW_PART_LEN);
		int lastchunkBytes = dataLen % FLASH_ROW_PART_LEN;
		for(int i = 0 ; i < partCount; i++){
			if(i == partCount -1){
				if(!programRow(row.getArrayId(), row.getRowNumber(), row.getData(), i * FLASH_ROW_PART_LEN, lastchunkBytes))
				   return false;
			}
			else{
				if(!sendData(row.getData(), i * FLASH_ROW_PART_LEN, FLASH_ROW_PART_LEN))
				   return false;
			}
		}
		return verifyChecksum(row.getArrayId(), row.getRowNumber(), idx);
	}

	private boolean sendData(ObservableList<Byte> data, int startIdx, int len){
		byte[] payload = new byte[len];
		for(int i = 0; i < len; i++){
			payload[i] = data.get(i + startIdx);
		}

		byte[] tmp = BootloaderUtils.formpacket((byte)0x37, payload);
		// Debug
//		for(int i = 0 ; i < tmp.length; i++){
//			logDetails( Utils.byteToHex(tmp[i]) + " ");
//		}
//		logDetails("\n");
		// Debug

		int outSize = USBManager.epXfer(dev, OUT_EP, tmp);
		if( outSize != tmp.length)
		{
			logDetails("Send Data command fail.\n");
			return false;
		}
		int size = USBManager.epXfer(dev, IN_EP, tmpInArray);
		BootStatus status = BootloaderUtils.getStatus(tmpInArray);
		if(status.equals(BootStatus.SUCCESS)){
			return true;
		}

		logDetails("Send Data response fail. " + status.name() +"\n");
		return false;
	}

	private boolean programRow(byte arrayId, short rowNumber, ObservableList<Byte> data, int startIdx, int len){
		byte[] payload = new byte[len + 3];
		payload[0] = arrayId;
		payload[1] = Utils.uint16_get_lsb(rowNumber);
		payload[2] = Utils.uint16_get_msb(rowNumber);

		for(int i = 0; i < len; i++){
			payload[i + 3] = data.get(i + startIdx);
		}

		byte[] tmp = BootloaderUtils.formpacket((byte)0x39, payload);
		// Debug
//		for(int i = 0 ; i < tmp.length; i++){
//			logDetails( Utils.byteToHex(tmp[i]) + " ");
//		}
//		logDetails("\n");
		// Debug

		int outSize = USBManager.epXfer(dev, OUT_EP, tmp);
		if( outSize != tmp.length)
		{
			logDetails("Program row command fail.\n");
			return false;
		}
		int size = USBManager.epXfer(dev, IN_EP, tmpInArray);
		BootStatus status = BootloaderUtils.getStatus(tmpInArray);
		if(status.equals(BootStatus.SUCCESS)){
			return true;
		}

		logDetails("Program row response fail. " + status.name() +"\n");
		return false;
	}

	private boolean verifyChecksum(byte arrayId, short rowNumber, int idx) {
		byte[] payload = new byte[3];
		payload[0] = arrayId;
		payload[1] = Utils.uint16_get_lsb(rowNumber);
		payload[2] = Utils.uint16_get_msb(rowNumber);

		byte[] tmp = BootloaderUtils.formpacket((byte)0x3A, payload);
		// Debug
//		for(int i = 0 ; i < tmp.length; i++){
//			logDetails( Utils.byteToHex(tmp[i]) + " ");
//		}
//		logDetails("\n");
		// Debug

		int outSize = USBManager.epXfer(dev, OUT_EP, tmp);
		if( outSize != tmp.length)
		{
			logDetails("Get row checksum command fail.\n");
			return false;
		}
		int size = USBManager.epXfer(dev, IN_EP, tmpInArray);
		BootStatus status = BootloaderUtils.getStatus(tmpInArray);
		if(status.equals(BootStatus.SUCCESS)){
			CyacdRow row = cyData.getRowList().get(idx);
			Checksum8Row chk = new Checksum8Row();
			chk.update(row.getChecksum());
			chk.update(row.getArrayId());
			chk.update(Utils.uint32_get_b0(rowNumber));
			chk.update(Utils.uint32_get_b1(rowNumber));
			chk.update(Utils.uint32_get_b0(row.getDataLen()));
			chk.update(Utils.uint32_get_b1(row.getDataLen()));
			byte expchksum = chk.getChecksum();
			byte actualchksum = BootloaderUtils.getRowChksum(tmpInArray);
			if( expchksum != actualchksum){
				logDetails("Row checksum mismatch." +"\n");
				return false;
			}
			return true;
		}

		logDetails("Get row checksum fail. " + status.name() +"\n");
		return false;
	}

	private boolean exitBootloader() {
		byte[] tmp = BootloaderUtils.formpacket((byte)0x3b, null);
		// Debug
//		for(int i = 0 ; i < tmp.length; i++){
//			logDetails( Utils.byteToHex(tmp[i]) + " ");
//		}
//		logDetails("\n");
		// Debug

		int outSize = USBManager.epXfer(dev, OUT_EP, tmp);
		if( outSize != tmp.length)
		{
			logDetails("Exit bootloader command fail.\n");
			return false;
		}
		logDetails("Bootload success at "+ Utils.getCurrentTime() +"\n");
		return true;

	}

	private void logDetails(String input){
		Platform.runLater(() -> {
			txtAreaLog.appendText(input);
        });
	}

	private void updateProgressBar(double progress){
		Platform.runLater(() -> {
			statusBar.setProgress(progress);
        });
	}
}

