package com.igorepst.deskPlaces.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import com.igorepst.deskPlaces.util.Settings;

public class MainUITableCellRenderer extends JLabel implements
		TableCellRenderer {

	private static final Border EMPTY_BRD = BorderFactory.createEmptyBorder(5,
			5, 5, 5);
	private static final Border SELECTED_BRD = BorderFactory.createLineBorder(
			Color.red, 5, true);

	protected MainUITableCellRenderer() {
		setVerticalTextPosition(SwingConstants.BOTTOM);
		setHorizontalTextPosition(SwingConstants.CENTER);
		setHorizontalAlignment(SwingConstants.CENTER);
		setFont(Settings.getLabelFont());
		setBorder(MainUITableCellRenderer.EMPTY_BRD);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (value instanceof DeskCell) {
			DeskCell deskCell = (DeskCell) value;
			setIcon(deskCell.icon);
			setText(deskCell.name);
		} else {
			setIcon(null);
			setText(null);
		}
		setBorder(isSelected ? MainUITableCellRenderer.SELECTED_BRD:MainUITableCellRenderer.EMPTY_BRD);
		return this;
	}

}
