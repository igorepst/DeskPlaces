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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import com.igorepst.deskPlaces.util.Settings;
import com.igorepst.deskPlaces.util.Util;

public class MainUIFrame {

	private interface UIInt {
		void setDeskCells(DeskCell[] cells);

		void setVisible(boolean isVisible);
	}

	private static class MainUIDialog extends JDialog implements UIInt {
		private final MainUIFrame mainUIframe;

		private MainUIDialog() {
			setUndecorated(true);
			setBackground(new Color(0, 0, 0, 0));
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			mainUIframe = new MainUIFrame();
			setContentPane(mainUIframe.buildUI(this));
		}

		@Override
		public void setDeskCells(DeskCell[] cells) {
			mainUIframe.setDeskCells(cells);
		}
	}

	private static class MainUIJframe extends JFrame implements UIInt {
		private final MainUIFrame mainUIframe;

		private static final int ICON_DIM = 18;
		private static final String MAIN_ICON_NAME = null;
		private static final String CLOSE_ICON_NAME = null;

		private MainUIJframe() {
			setTitle(Settings.DESK_PLACES_NAME);
			BufferedImage mainIcon = null;
			if (MainUIJframe.MAIN_ICON_NAME != null) {
				try {
					mainIcon = ImageIO.read(ClassLoader
							.getSystemResource(MainUIJframe.MAIN_ICON_NAME));
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (mainIcon != null) {
					setIconImage(mainIcon);
					mainIcon = Util.getScaledImage(mainIcon,
							MainUIJframe.ICON_DIM);
				}
			}
			setUndecorated(true);
			setBackground(new Color(0, 0, 0, 0));
			setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			mainUIframe = new MainUIFrame();
			JPanel mainPanel = mainUIframe.buildUI(this);
			JPanel titlebar = new JPanel();
			titlebar.setLayout(new BorderLayout(10, 0));
			JLabel titleLbl = new JLabel(Settings.DESK_PLACES_NAME,
					mainIcon == null ? null : new ImageIcon(mainIcon),
					SwingConstants.LEADING);
			titleLbl.setFont(titleLbl.getFont().deriveFont(18f));
			titlebar.add(titleLbl);
			BufferedImage closeIcon = null;
			if (MainUIJframe.CLOSE_ICON_NAME != null) {
				try {
					closeIcon = ImageIO.read(ClassLoader
							.getSystemResource(MainUIJframe.CLOSE_ICON_NAME));
					closeIcon = Util.getScaledImage(closeIcon,
							MainUIJframe.ICON_DIM);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			JButton closeBtn = new JButton(new AbstractAction(null,
					closeIcon == null ? null : new ImageIcon(closeIcon)) {

				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			titlebar.add(closeBtn, BorderLayout.LINE_END);

			mainPanel.add(titlebar, BorderLayout.NORTH);
			setContentPane(mainPanel);
		}

		@Override
		public void setDeskCells(DeskCell[] cells) {
			mainUIframe.setDeskCells(cells);
		}
	}

	private static final String MOVE_TO_DEF_COMMAND = "moveToDef";
	private static final String MOVE_TO_SCR_COMMAND = "moveToScr";
	private static final String RUN_CMD_COMMAND = "runCmd";
	private JTable table;
	private JScrollPane scrollPane;
	private DeskCell lastSelected = null;

	private MainUIFrame() {
	}

	public void setDeskCells(DeskCell[] cells) {
		table.setModel(new MainUITableModel(cells));
		updateRowHeights();
	}

	private TranslucentPane buildUI(Window window) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
		}

		table = new JTable();
		table.setFillsViewportHeight(true);
		table.setShowGrid(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDragEnabled(false);
		table.setCellSelectionEnabled(true);
		table.setDefaultRenderer(DeskCell.class, new MainUITableCellRenderer());
		table.setOpaque(false);
		table.setTableHeader(null);

		MouseAdapter mad = new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2
						&& SwingUtilities.isLeftMouseButton(e)) {
					DeskCell dc = getCellAt(e.getPoint());
					if (dc != null) {
						dc.runCmd();
					}
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				setSelectedCell(e.getPoint());
			}
		};

		table.addMouseListener(mad);
		table.addMouseMotionListener(mad);

		scrollPane = new JScrollPane(table);
		scrollPane.setOpaque(false);
		scrollPane.setColumnHeader(null);
		scrollPane.getViewport().setOpaque(false);
		scrollPane.getVerticalScrollBar().setUnitIncrement(35);

		// setLocation(0, 0);
		window.setSize(1920, 1080);
		// setLocation(1921, 0);
		showOnScreen(Settings.getDisplay(), window);

		addKeyBindings(window);
		window.setCursor(new Cursor(Cursor.HAND_CURSOR));

		Point p = table.getMousePosition();
		if (p != null) {
			setSelectedCell(p);
		}
		TranslucentPane translucentPane = new TranslucentPane();
		translucentPane.setLayout(new BorderLayout());

		translucentPane.add(scrollPane, BorderLayout.CENTER);
		return translucentPane;
	}

	private void setSelectedCell(Point p) {
		DeskCell dc = getCellAt(p);
		if (lastSelected == null) {
			if (dc != null) {
				table.setRowSelectionInterval(dc.row, dc.row);
				table.setColumnSelectionInterval(dc.column, dc.column);
				lastSelected = dc;
			}
		} else {
			if (dc == null) {
				table.clearSelection();
				lastSelected = null;
			} else if (!lastSelected.equals(dc)) {
				table.setRowSelectionInterval(dc.row, dc.row);
				table.setColumnSelectionInterval(dc.column, dc.column);
				lastSelected = dc;
			}
		}
	}

	private DeskCell getCellAt(Point p) {
		final int row = table.rowAtPoint(p);
		if (row > -1) {
			final int column = table.columnAtPoint(p);
			if (column > -1) {
				Object value = table.getModel().getValueAt(row, column);
				if (value instanceof DeskCell) {
					return (DeskCell) value;
				}
			}
		}
		return null;
	}

	private void addKeyBindings(final Window window) {
		for (int i = 24; --i >= 1;) {
			table.getInputMap().put(KeyStroke.getKeyStroke("F" + i),
					MainUIFrame.MOVE_TO_SCR_COMMAND);
		}
		table.getActionMap().put(MainUIFrame.MOVE_TO_SCR_COMMAND,
				new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						showOnScreen(Settings.getDisplay(), window);
					}
				});

		table.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.ALT_MASK
						| InputEvent.SHIFT_MASK),
				MainUIFrame.MOVE_TO_DEF_COMMAND);
		table.getActionMap().put(MainUIFrame.MOVE_TO_DEF_COMMAND,
				new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						showOnScreen(Settings.DEFAULT_DISPLAY, window);
					}
				});

		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				MainUIFrame.RUN_CMD_COMMAND);
		table.getActionMap().put(MainUIFrame.RUN_CMD_COMMAND,
				new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						int col = table.getSelectedColumn();
						if (col > -1) {
							int row = table.getSelectedRow();
							if (row > -1) {
								Object obj = table.getModel().getValueAt(row,
										col);
								if (obj instanceof DeskCell) {
									((DeskCell) obj).runCmd();
								}
							}
						}
					}
				});
	}

	private void showOnScreen(int display, final Window window) {
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
		window.setLocation(device.getDefaultConfiguration().getBounds().x,
				window.getY());
		window.toFront();
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

		DeskCell[] cells = dataList.toArray(new DeskCell[dataList.size()]);
		Arrays.sort(cells);
		final UIInt uiFrame = Settings.isDecorations() ? new MainUIJframe()
				: new MainUIDialog();
		uiFrame.setDeskCells(cells);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				uiFrame.setVisible(true);
			}
		});
	}

}
