package org.ykc.cyusbboothost;

public class CRC16 {
	int crc = 0xFFFF;
	int polynomial = 0x1021;

    public void update(byte b) {
        for (int i = 0; i < 8; i++) {
            boolean bit = ((b   >> (7-i) & 1) == 1);
            boolean c15 = ((crc >> 15    & 1) == 1);
            crc <<= 1;
            if (c15 ^ bit) crc ^= polynomial;
        }
        crc &= 0xffff;
    }

    public short getCRC(){
    	return (short)crc;
    }
}
