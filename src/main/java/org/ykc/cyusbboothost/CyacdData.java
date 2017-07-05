package org.ykc.cyusbboothost;

import javafx.collections.ObservableList;

public class CyacdData {
	private CyacdHeader cyHeader;
	private ObservableList<CyacdRow> rowList;

	public CyacdData(CyacdHeader cyHeader, ObservableList<CyacdRow> rowList) {
		this.cyHeader = cyHeader;
		this.rowList = rowList;
	}

	public CyacdHeader getCyHeader() {
		return cyHeader;
	}

	public ObservableList<CyacdRow> getRowList() {
		return rowList;
	}
}
