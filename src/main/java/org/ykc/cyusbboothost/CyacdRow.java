package org.ykc.cyusbboothost;

import javafx.collections.ObservableList;

public class CyacdRow {
	private byte arrayId;
	private short rowNumber;
	private short dataLen;
	private ObservableList<Byte> data;
	private byte checksum;

	public CyacdRow(byte arrayId, short rowNumber, short dataLen, ObservableList<Byte> data, byte checksum) {
		this.arrayId = arrayId;
		this.rowNumber = rowNumber;
		this.dataLen = dataLen;
		this.data = data;
		this.checksum = checksum;
	}

	public byte getArrayId() {
		return arrayId;
	}

	public short getRowNumber() {
		return rowNumber;
	}

	public short getDataLen() {
		return dataLen;
	}

	public ObservableList<Byte> getData() {
		return data;
	}

	public byte getChecksum() {
		return checksum;
	}

	public String getRowString(){
		String row = "";
		row += "Array ID: 0x" + Utils.byteToHex(arrayId);
		row += " Row No: 0x" + Utils.shortToHex(rowNumber);
		row += " Len: 0x" + Utils.shortToHex(dataLen);
		row += " Flash: ";
		for(int i = 0 ; i < data.size(); i++){
			row += Utils.byteToHex(data.get(i)) + " ";
		}
		row += "Checksum: 0x" + Utils.byteToHex(checksum) + "\n";
		return row;
	}
}
