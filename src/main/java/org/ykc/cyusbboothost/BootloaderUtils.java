package org.ykc.cyusbboothost;

public class BootloaderUtils {
	public static final int SOP_IDX = 0;
	public static final byte SOP_VALUE = 1;
	public static final int CMD_IDX = 1;
	public static final int STATUS_IDX = 1;	
	public static final int DATA_LEN_LSB_IDX = 2;
	public static final int DATA_LEN_MSB_IDX = 3;
	public static final int DATA_START_IDX = 4;	
	public static final byte EOP_VALUE = (byte)0x17;
	public static final int BASE_PKT_LENGTH = 7;
	public static final int MAX_PKT_LENGTH = 64;

	
	public enum BootStatus
	{
		SUCCESS,
		RSVD1,
		BOOTLOADER_ERR_VERIFY,
		BOOTLOADER_ERR_LENGTH,
		BOOTLOADER_ERR_DATA,
		BOOTLOADER_ERR_CMD,
		RSVD6,
		RSVD7,
		BOOTLOADER_ERR_CHECKSUM,
		RSVD9,
		BOOTLOADER_ERR_ROW,
		RSVD11,
		BOOTLOADER_ERR_APP,
		BOOTLOADER_ERR_ACTIVE,
		RSVD14,
		BOOTLOADER_ERR_UNK,
		BAD_PACKET
	}
	
	public static byte[] formpacket(byte cmd, byte[] dataArray){
		Checksum16 chksum = new Checksum16();
		int dataLen = 0;
		if(dataArray != null){
			dataLen = dataArray.length;
		}
		byte[] pkt = new byte[BASE_PKT_LENGTH + dataLen];
//		byte[] pkt = new byte[MAX_PKT_LENGTH];		
		pkt[SOP_IDX] = SOP_VALUE;
		pkt[CMD_IDX] = cmd;
		chksum.update(cmd);
		byte dataLenLSB = Utils.uint32_get_b0(dataLen);
		pkt[DATA_LEN_LSB_IDX] = dataLenLSB;
		chksum.update(dataLenLSB);
		byte dataLenMSB = Utils.uint32_get_b1(dataLen);
		pkt[DATA_LEN_MSB_IDX] = dataLenMSB;
		chksum.update(dataLenMSB);
		if(dataArray != null){
			for(int i = 0; i < dataLen; i++){
				pkt[i + DATA_START_IDX] = dataArray[i]; 
				chksum.update(dataArray[i]);
			}
		}
		int chk = chksum.getChecksum();
		pkt[DATA_LEN_MSB_IDX + dataLen + 1] = Utils.uint32_get_b0(chk);
		pkt[DATA_LEN_MSB_IDX + dataLen + 2] = Utils.uint32_get_b1(chk);
		pkt[DATA_LEN_MSB_IDX + dataLen + 3] = EOP_VALUE;
		return pkt;
	}
	
	public static BootStatus getStatus(byte[] respPkt){
		try {
			if(respPkt[SOP_IDX] != SOP_VALUE)
				throw new RuntimeException(); 
			int dataLen = Utils.get_uint32(respPkt[DATA_LEN_LSB_IDX], respPkt[DATA_LEN_MSB_IDX], (byte)0, (byte)0);
			short chksum = Checksum16.getChecksumArray(respPkt, STATUS_IDX, 3 + dataLen);
			short respchksum = Utils.get_uint16(respPkt[DATA_LEN_MSB_IDX + dataLen + 1], respPkt[DATA_LEN_MSB_IDX + dataLen + 2]);
			if(chksum != respchksum)
				throw new RuntimeException(); 
			if(respPkt[DATA_LEN_MSB_IDX + dataLen + 3] != EOP_VALUE)
				throw new RuntimeException();
			int status = respPkt[STATUS_IDX];
			return BootStatus.values()[status];	
		} catch (Exception e) {
			return BootStatus.BAD_PACKET;
		}
	}

	public static int getSiID(byte[] respPkt){
		try {
			return Utils.get_uint32(respPkt[DATA_START_IDX], respPkt[DATA_START_IDX + 1], 
					                respPkt[DATA_START_IDX + 2], respPkt[DATA_START_IDX + 3]);
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static byte getRowChksum(byte[] respPkt){
		try {
			return respPkt[DATA_START_IDX];
		} catch (Exception e) {
			return 0;
		}
	}
	
}
