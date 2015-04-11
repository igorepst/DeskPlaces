package com.igorepst.deskPlaces.ui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.igorepst.deskPlaces.util.ImgFile;
import com.igorepst.deskPlaces.util.Settings;
import com.igorepst.deskPlaces.util.Util;

public class DeskCell implements Comparable<DeskCell> {
	private static final File IMAGE_DIR = new File(Settings.getImageDir());
	public static final File DESK_HOME_DIR = new File(
			System.getProperty("user.home"), "." + Settings.DESK_PLACES_NAME);
	public static final File IMAGE_DIR_THUMBS = new File(
			DeskCell.DESK_HOME_DIR, Settings.THUMBS_DIR_PREFIX
					+ Settings.getImageDim());
	private static List<ImgFile> thumbs, images;

	static {
		if (DeskCell.IMAGE_DIR_THUMBS.isDirectory()) {
			DeskCell.thumbs = Util.getImgFiles(DeskCell.IMAGE_DIR_THUMBS);
		} else {
			DeskCell.IMAGE_DIR_THUMBS.mkdirs();
			DeskCell.thumbs = new ArrayList<ImgFile>(0);
		}
		try {
			Util.deleteOldThumbs();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected final String name;
	private final File dir;
	private final String param;
	protected final ImageIcon icon;

	protected int column, row;

	protected DeskCell(final String name, final String icon, final File dir,
			final String param) {
		this.name = name;
		this.param = param;
		if (icon == null || icon.isEmpty()) {
			this.icon = null;
		} else {
			ImgFile imgFile = new ImgFile(icon, null);
			File thumbsFile = resolveIcon(imgFile, DeskCell.thumbs);
			BufferedImage img = null;
			try {
				if (thumbsFile == null) {
					if (DeskCell.images == null) {
						DeskCell.images = Util.getImgFiles(DeskCell.IMAGE_DIR);
					}
					File fullImageFile = resolveIcon(imgFile, DeskCell.images);
					if (fullImageFile != null) {
						thumbsFile = new File(DeskCell.IMAGE_DIR_THUMBS,
								fullImageFile.getName());
						img = ImageIO.read(fullImageFile);
						final int IMAGE_DIM = Settings.getImageDim();
						if (img.getWidth() == IMAGE_DIM
								&& img.getHeight() == IMAGE_DIM) {
							Files.copy(
									fullImageFile.toPath(),
									thumbsFile.toPath(),
									java.nio.file.StandardCopyOption.REPLACE_EXISTING,
									java.nio.file.StandardCopyOption.COPY_ATTRIBUTES,
									java.nio.file.LinkOption.NOFOLLOW_LINKS);
						} else {
							img = Util.getScaledImage(img, Settings.getImageDim());
							ImageIO.write(img, Util.resolveImageFormat(icon),
									thumbsFile);
						}
					}
				} else {
					img = ImageIO.read(thumbsFile);
				}

			} catch (IOException ioex) {
			}
			this.icon = img == null ? null : new ImageIcon(img);
		}
		this.dir = dir;
	}

	private File resolveIcon(final ImgFile icon, List<ImgFile> list) {
		final int ind = Collections.binarySearch(list, icon);
		return ind < 0 ? null : list.get(ind).file;
	}

	protected void runCmd() {
		if (!Settings.isRunCommandDefined()) {
			System.err.println(Settings.RUNBY_SETTING_NAME + " is not defined for " + this);
			return;
		}
		String plName = Util.getPlaylist(dir, param != null);
		if (plName == null || plName.isEmpty()) {
			System.err.println("Cannot build playlist for " + this);
			return;
		}
		List<String> commands = Settings.getRunByCmd(plName);
		if (commands == null) {
			System.err.println(Settings.RUNBY_SETTING_NAME + " is not defined for " + this);
			return;
		}
		ProcessBuilder pb = new ProcessBuilder(commands);
		try {
			pb.start();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public int compareTo(DeskCell o) {
		return o == null ? 1 : (name == null ? -1 : name.compareTo(o.name));
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DeskCell other = (DeskCell) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

}
