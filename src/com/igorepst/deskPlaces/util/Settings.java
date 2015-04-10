package com.igorepst.deskPlaces.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Settings {

	private static final char settingsDivider = '=';
	private static final String settingsComment = "#";
	public static final String DESK_PLACES_NAME = "DeskPlaces";
	public static final String THUMBS_DIR_PREFIX = "thumbs-";

	private static final String DISPLAY_SETTING_NAME = "display";
	private static final String DECORATIONS_SETTING_NAME = "decorations";
	private static final String RUNBY_SETTING_NAME = "runBy";
	private static final String IMAGE_DIM_SETTING_NAME = "iconSize";
	private static final String IMAGE_DIR_SETTING_NAME = "iconsDir";
	private static final String DEF_FILE_SETTING_NAME = "definitionsFile";
	private static final String PLAYLIST = "$PLAYLIST$";

	public static final int DEFAULT_DISPLAY = 0;

	private static int display = Settings.DEFAULT_DISPLAY;
	private static boolean decorations = true;
	private static int playlistPlace = 0;
	private static int imageDim = 220;
	private static String imageDir = "";
	private static String defFile = "";
	private static List<String> commands = new ArrayList<String>();

	private Settings() {
	}

	public static void read(String filePath) {
		if (filePath == null || filePath.isEmpty()) {
			throw new IllegalArgumentException("filePath is null or empty");
		}
		File setFile = new File(filePath);
		if (!setFile.isFile()) {
			throw new IllegalArgumentException(setFile
					+ " doesn't exist or is not a file");
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					setFile), "UTF-8"));
			String line, key, value;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith(Settings.settingsComment)) {
					continue;
				}
				int ind = line.indexOf(Settings.settingsDivider);
				if (ind < 1 || ind == line.length() - 1) {
					continue;
				}
				key = line.substring(0, ind).trim();
				value = line.substring(ind + 1).trim();

				switch (key) {
				case DISPLAY_SETTING_NAME:
					try {
						Settings.display = Integer.parseInt(value);
						if (Settings.display < Settings.DEFAULT_DISPLAY) {
							Settings.display = Settings.DEFAULT_DISPLAY;
						}
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
					break;
				case DECORATIONS_SETTING_NAME:
					Settings.decorations = Boolean.parseBoolean(value);
					break;
				case RUNBY_SETTING_NAME:
					ind = value.indexOf(Settings.PLAYLIST);
					if (ind == -1) {
						Settings.parseRunBy(value);
						Settings.playlistPlace = Settings.commands.size();
						Settings.commands.add(null);
					} else {
						Settings.parseRunBy(value.substring(0, ind));
						Settings.playlistPlace = Settings.commands.size();
						Settings.commands.add(null);
						Settings.parseRunBy(value.substring(ind
								+ Settings.PLAYLIST.length()));
					}
					break;
				case IMAGE_DIM_SETTING_NAME:
					try {
						int dim = Integer.parseInt(value);
						if (dim > 0) {
							Settings.imageDim = dim;
						}
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
					break;
				case IMAGE_DIR_SETTING_NAME:
					Settings.imageDir = value;
					break;
				case DEF_FILE_SETTING_NAME:
					Settings.defFile = value;
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private static void parseRunBy(String toParse) {
		if (toParse.isEmpty()) {
			return;
		}
		StringBuilder bld = new StringBuilder(toParse.length());
		boolean dquote = false;
		for (char c : toParse.toCharArray()) {
			if (dquote) {
				if (c == '"') {
					dquote = false;
				}
				bld.append(c);
			} else {
				if (c == '"') {
					dquote = true;
					bld.append(c);
				} else if (c == ' ') {
					String s = bld.toString();
					if (!s.isEmpty()) {
						Settings.commands.add(s);
					}
					bld.setLength(0);
				} else {
					bld.append(c);
				}
			}
		}
		String s = bld.toString();
		if (!s.isEmpty()) {
			Settings.commands.add(s);
		}
	}

	public static int getDisplay() {
		return Settings.display;
	}

	public static boolean isDecorations() {
		return Settings.decorations;
	}

	public static List<String> getRunByCmd(final String playlistPath) {
		if (Settings.commands.size() == 0) {
			Settings.commands.add(playlistPath);
		} else {
			Settings.commands.set(Settings.playlistPlace, playlistPath);
		}
		return new ArrayList<>(Settings.commands);
	}

	public static int getImageDim() {
		return Settings.imageDim;
	}

	public static String getImageDir() {
		return Settings.imageDir;
	}

	public static String getDefFile() {
		return Settings.defFile;
	}

}
