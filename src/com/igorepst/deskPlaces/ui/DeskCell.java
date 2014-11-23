package com.igorepst.deskPlaces.ui;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class DeskCell implements Comparable<DeskCell> {

	private static final File IMAGE_DIR = new File(
			"D:\\My Documents\\multPlaces\\icons\\");
	protected static final int IMAGE_DIM = 220;
	private static final File IMAGE_DIR_THUMBS = new File(DeskCell.IMAGE_DIR,
			".thumbs-" + DeskCell.IMAGE_DIM);
	private static final String PLAYLISTS_DIR = "D:\\My Documents\\multPlaces\\playlists\\";

	static {
		if (!DeskCell.IMAGE_DIR_THUMBS.isDirectory()) {
			DeskCell.IMAGE_DIR_THUMBS.mkdirs();
		}
	}

	private static final String VLC_COMM = "D:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe";
	protected final String name;
	private final String playlistFile, param;
	protected final ImageIcon icon;

	protected DeskCell(final String name, final String icon,
			final String playlist, final String param) {
		this.name = name;
		this.param = param;
		if (icon != null && !icon.isEmpty()) {
			File file = new File(DeskCell.IMAGE_DIR_THUMBS, icon);
			BufferedImage img = null;
			try {
				if (file.exists()) {
					img = ImageIO.read(file);
				} else {
					File tmp = new File(DeskCell.IMAGE_DIR, icon);
					img = ImageIO.read(tmp);
					if (img.getWidth() != DeskCell.IMAGE_DIM
							|| img.getHeight() != DeskCell.IMAGE_DIM) {
						img = getScaledImage(img);
						ImageIO.write(img, getFormat(icon), file);
					} else {
						Files.copy(
								tmp.toPath(),
								file.toPath(),
								java.nio.file.StandardCopyOption.REPLACE_EXISTING,
								java.nio.file.StandardCopyOption.COPY_ATTRIBUTES,
								java.nio.file.LinkOption.NOFOLLOW_LINKS);
					}
				}

			} catch (IOException ioex) {
			}
			this.icon = img == null ? null : new ImageIcon(img);
		} else {
			this.icon = null;
		}
		playlistFile = new File(DeskCell.PLAYLISTS_DIR, playlist)
				.getAbsolutePath();
	}

	private String getFormat(final String icon) {
		String format;
		String extension = "";
		int i = icon.lastIndexOf('.');
		if (i > 0) {
			extension = icon.substring(i + 1).toUpperCase(
					Locale.getDefault());
		}
		switch (extension) {
		case "GIF":
		case "PNG":
			format = extension;
			break;
		case "JPG":
		case "JPEG":
		case "JPE":
			format = "JPG";
		default:
			format = "PNG";
			break;
		}
		return format;
	}

	protected void runCmd() {
		ProcessBuilder pb = new ProcessBuilder(DeskCell.VLC_COMM,
				"--no-qt-recentplay", "--fullscreen", "--loop",
				"--playlist-tree", "--playlist-autostart",
				param == null ? "--no-random" : param, playlistFile);
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
	
	private BufferedImage getScaledImage(Image srcImg){
	    BufferedImage resizedImg = new BufferedImage(DeskCell.IMAGE_DIM, DeskCell.IMAGE_DIM, Transparency.TRANSLUCENT);
	    Graphics2D g2 = resizedImg.createGraphics();
	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g2.drawImage(srcImg, 0, 0, DeskCell.IMAGE_DIM, DeskCell.IMAGE_DIM, null);
	    g2.dispose();
	    return resizedImg;
	}

}
