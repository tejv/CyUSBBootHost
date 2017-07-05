package org.ykc.cyusbboothost;

import java.io.ObjectInputStream.GetField;

public class CyacdHeader {
	private int siliconId;
	private byte siliconRev;
	private byte checksumType;

	public CyacdHeader(int siliconId, byte siliconRev, byte checksumType) {
		this.siliconId = siliconId;
		this.siliconRev = siliconRev;
		this.checksumType = checksumType;
	}

	public int getSiliconId() {
		return siliconId;
	}

	public byte getSiliconRev() {
		return siliconRev;
	}

	public byte getChecksumType() {
		return checksumType;
	}

	public String getHeaderString(){
		String hdr = "";
		hdr += "Silicon ID: 0x" + Utils.intToHex(siliconId);
		hdr += " Silicon Rev: 0x" + Utils.byteToHex(siliconRev);
		hdr += " Checksum Type: 0x" + Utils.byteToHex(checksumType) + "\n";
		return hdr;
	}
}
