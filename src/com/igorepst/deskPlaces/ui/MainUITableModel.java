package com.igorepst.deskPlaces.ui;

import javax.swing.table.AbstractTableModel;

public class MainUITableModel extends AbstractTableModel {

	private final int COLUMNS_NUM = 6;
	private final DeskCell[][] dataArr;

	protected MainUITableModel(DeskCell[] dataArrParam) {
		final boolean noRem = dataArrParam.length % COLUMNS_NUM == 0;
		final int rowCnt = dataArrParam.length / COLUMNS_NUM + (noRem ? 0 : 1);
		dataArr = new DeskCell[rowCnt][COLUMNS_NUM];
		DeskCell cell;
		for (int i = 0; i < rowCnt; ++i) {
			if (noRem || i != rowCnt - 1) {
				for (int j = 0; j < COLUMNS_NUM; ++j) {
					cell = dataArrParam[i * COLUMNS_NUM + j];
					cell.row = i;
					cell.column = j;
					dataArr[i][j] = cell;
				}
			} else {
				for (int j = 0; j < COLUMNS_NUM; ++j) {
					int ind = i * COLUMNS_NUM + j;
					if (ind < dataArrParam.length) {
						cell = dataArrParam[ind];
						cell.row = i;
						cell.column = j;
						dataArr[i][j] = cell;
					}
				}
			}
		}
	}

	@Override
	public int getRowCount() {
		return dataArr.length;
	}

	@Override
	public int getColumnCount() {
		return COLUMNS_NUM;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return dataArr[rowIndex][columnIndex];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return DeskCell.class;
	}
	
	@Override
	public String getColumnName(int column) {
		return null;
	}

}
