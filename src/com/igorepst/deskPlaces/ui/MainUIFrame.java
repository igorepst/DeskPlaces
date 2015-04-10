package com.igorepst.deskPlaces.ui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import com.igorepst.deskPlaces.util.Settings;
import com.igorepst.deskPlaces.util.Util;

public class MainUIFrame extends JDialog {

	private static final String MOVE_TO_DEF_COMMAND = "moveToDef";
	private static final String MOVE_TO_SCR_COMMAND = "moveToScr";
	private final JTable table;
	private JScrollPane scrollPane;

	private MainUIFrame() {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
		}

		table = new JTable();
		table.setFillsViewportHeight(true);
		table.setShowGrid(false);
		table.setDefaultRenderer(DeskCell.class, new MainUITableCellRenderer());
		table.setOpaque(false);

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2
						&& SwingUtilities.isLeftMouseButton(e)) {
					Point p = e.getPoint();
					final int row = table.rowAtPoint(p);
					if (row > -1) {
						final int column = table.columnAtPoint(p);
						if (column > -1) {
							Object value = table.getModel().getValueAt(row,
									column);
							if (value instanceof DeskCell) {
								((DeskCell) value).runCmd();
							}
						}
					}
				}
			}
		});

		scrollPane = new JScrollPane(table);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		scrollPane.getVerticalScrollBar().setUnitIncrement(35);

		if (Settings.isDecorations()) {
			setTitle(Settings.DESK_PLACES_NAME);
		} else {
			setUndecorated(true);
			setBackground(new Color(0, 0, 0, 0));
		}
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		TranslucentPane translucentPane = new TranslucentPane();
		translucentPane.setLayout(new BorderLayout());
		setContentPane(translucentPane);

		translucentPane.add(scrollPane, BorderLayout.CENTER);

		// setLocation(0, 0);
		setSize(1920, 1080);
		// setLocation(1921, 0);
		showOnScreen(Settings.getDisplay());
		setVisible(true);

		addKeyBindings(table);
		setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	private void addKeyBindings(JComponent comp) {
		for (int i = 24; --i >= 1;) {
			comp.getInputMap().put(KeyStroke.getKeyStroke("F" + i),
					MainUIFrame.MOVE_TO_SCR_COMMAND);
		}
		comp.getActionMap().put(MainUIFrame.MOVE_TO_SCR_COMMAND,
				new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						showOnScreen(Settings.getDisplay());
					}
				});

		// Debug
		comp.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.ALT_MASK
						| InputEvent.SHIFT_MASK),
				MainUIFrame.MOVE_TO_DEF_COMMAND);
		comp.getActionMap().put(MainUIFrame.MOVE_TO_DEF_COMMAND,
				new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						showOnScreen(Settings.DEFAULT_DISPLAY);
					}
				});
	}

	private void showOnScreen(int display) {
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice device = null;
		if (display > Settings.DEFAULT_DISPLAY) {
			GraphicsDevice[] gds = ge.getScreenDevices();
			display -= 1;
			if (gds.length > display) {
				device = gds[display];
			}
		}
		if (device == null) {
			device = ge.getDefaultScreenDevice();
		}
		setLocation(device.getDefaultConfiguration().getBounds().x, getY());
		toFront();
	}

	private void updateRowHeights() {
		try {
			for (int row = 0; row < table.getRowCount(); row++) {
				int rowHeight = table.getRowHeight();

				for (int column = 0; column < table.getColumnCount(); column++) {
					Component comp = table.prepareRenderer(
							table.getCellRenderer(row, column), row, column);
					rowHeight = Math.max(rowHeight,
							comp == null ? 0 : comp.getPreferredSize().height);
				}

				table.setRowHeight(row, rowHeight);
			}
		} catch (ClassCastException e) {
		}
	}

	private static class TranslucentPane extends JPanel {

		private TranslucentPane() {
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setComposite(AlphaComposite.SrcOver.derive(0.6f));
			g2d.setColor(getBackground());
			g2d.fillRect(0, 0, getWidth(), getHeight());

		}

	}

	public static void main(String[] args) {
		String cfgName = "settings.cfg";
		if (args.length > 0) {
			String arg = args[0];
			if (arg != null && !arg.isEmpty()) {
				cfgName = arg;
			}
		}
		Settings.read(cfgName);

		List<DeskCell> dataList = new ArrayList<>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					Settings.getDefFile()), "UTF-8"));
			String line;
			String[] splitArr;
			Pattern splitPattern = Pattern.compile(Util.dataDivider);
			while ((line = br.readLine()) != null) {
				splitArr = splitPattern.split(line);
				if (splitArr.length < 3) {
					continue;
				}
				File dir = new File(splitArr[2]);
				if (!dir.isDirectory()) {
					continue;
				}
				dataList.add(new DeskCell(splitArr[0], splitArr[1], dir,
						splitArr.length == 3 ? null : splitArr[3]));
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}

		MainUIFrame uiFrame = new MainUIFrame();
		DeskCell[] cells = dataList.toArray(new DeskCell[dataList.size()]);
		Arrays.sort(cells);
		uiFrame.table.setModel(new MainUITableModel(cells));
		uiFrame.scrollPane.setColumnHeader(null);
		uiFrame.updateRowHeights();
	}

}
