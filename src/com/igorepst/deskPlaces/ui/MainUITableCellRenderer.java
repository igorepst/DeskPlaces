package com.igorepst.deskPlaces.ui;

import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

public class MainUITableCellRenderer extends JLabel implements
		TableCellRenderer {

	protected MainUITableCellRenderer() {
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setVerticalTextPosition(SwingConstants.BOTTOM);
		setHorizontalTextPosition(SwingConstants.CENTER);
		setHorizontalAlignment(SwingConstants.CENTER);
		setFont(new Font("Arial", Font.BOLD, 20));
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (value instanceof DeskCell) {
			DeskCell deskCell = (DeskCell) value;
			setIcon(deskCell.icon);
			setText(deskCell.name);
		}
		else{
			setIcon(null);
			setText(null);
		}
		return this;
	}

}
