package org.ykc.cyusbboothost;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Checksum16 {
	public static final Logger logger = LoggerFactory.getLogger(Checksum16.class.getName());
	int checksum = 0;

    public void update(byte b) {
    	checksum += Utils.getUInt(b);
        checksum &= 0x0000ffff;
        //logger.info("Chk:" + Integer.toHexString(checksum) + " ");
    }

    public short getChecksum(){
    	//logger.info("Chk:" + Integer.toHexString((short)(0xffff - checksum)) + " ");
    	return (short)(0xffff - checksum);
    }
    
    public static short getChecksumArray(byte[] input, int startIndex, int size){
    	int chk = 0;
    	for(int i = startIndex; i < startIndex + size; i++){
    		chk += Utils.getUInt(input[i]);
    		chk &= 0xffff;
    	}
    	return (short)(0xffff - chk);
    }
}
