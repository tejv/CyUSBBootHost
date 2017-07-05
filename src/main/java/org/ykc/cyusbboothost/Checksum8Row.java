package org.ykc.cyusbboothost;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.collections.ObservableList;

public class Checksum8Row {
	public static final Logger logger = LoggerFactory.getLogger(Checksum8Row.class.getName());
	int checksum = 0;

    public void update(byte b) {
//    	logger.info("Input:" + Integer.toHexString(b) + " ");
    	checksum += Utils.getUInt(b);
        checksum &= 0xff;
//        logger.info("Chk:" + Integer.toHexString(checksum) + " ");
    }

    public byte getChecksum(){
//    	logger.info("Chk result:" + Integer.toHexString((byte)(checksum)) + " ");
    	return (byte)(checksum);
    }

    public static byte getChecksumArray(byte[] input, int startIndex, int size){
    	int chk = 0;
    	for(int i = startIndex; i < startIndex + size; i++){
    		chk += Utils.getUInt(input[i]);
    		chk &= 0xff;
    	}
    	return (byte)(chk);
    }

	public static byte getChecksumArray(ObservableList<Byte> input, int startIndex, short size) {
    	int chk = 0;
    	for(int i = startIndex; i < startIndex + size; i++){
    		chk += Utils.getUInt(input.get(i));
    		chk &= 0xff;
    	}
    	return (byte)(chk);
	}
}
