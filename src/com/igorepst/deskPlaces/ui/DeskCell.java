package com.igorepst.deskPlaces.ui;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
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
import com.igorepst.deskPlaces.util.Util;

public class DeskCell implements Comparable<DeskCell> {

	private static final int IMAGE_DIM = 220;
	private static final File IMAGE_DIR_THUMBS = new File(Util.IMAGE_DIR,
			".thumbs-" + DeskCell.IMAGE_DIM);
	private static List<ImgFile> thumbs, images;

	static {
		if (DeskCell.IMAGE_DIR_THUMBS.isDirectory()) {
			DeskCell.thumbs = Util.getImgFiles(DeskCell.IMAGE_DIR_THUMBS);
		} else {
			DeskCell.IMAGE_DIR_THUMBS.mkdirs();
			DeskCell.thumbs = new ArrayList<ImgFile>(0);
		}
	}

	// private static final String VLC_COMM =
	// "D:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe";
	protected final String name;
	private final File dir;
	private final String param;
	protected final ImageIcon icon;

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
						DeskCell.images = Util.getImgFiles(Util.IMAGE_DIR);
					}
					File fullImageFile = resolveIcon(imgFile, DeskCell.images);
					if (fullImageFile != null) {
						thumbsFile = new File(DeskCell.IMAGE_DIR_THUMBS,
								fullImageFile.getName());
						img = ImageIO.read(fullImageFile);
						if (img.getWidth() == DeskCell.IMAGE_DIM
								&& img.getHeight() == DeskCell.IMAGE_DIM) {
							Files.copy(
									fullImageFile.toPath(),
									thumbsFile.toPath(),
									java.nio.file.StandardCopyOption.REPLACE_EXISTING,
									java.nio.file.StandardCopyOption.COPY_ATTRIBUTES,
									java.nio.file.LinkOption.NOFOLLOW_LINKS);
						} else {
							img = getScaledImage(img);
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
		// ProcessBuilder pb = new ProcessBuilder(DeskCell.VLC_COMM,
		// "--no-qt-recentplay", "--fullscreen", "--loop",
		// "--playlist-tree", "--playlist-autostart",
		// param == null ? "--no-random" : param, playlistFile);
		// long lo = System.currentTimeMillis();
		String plName = Util.getPlaylist(dir, param != null);
		// System.out.println("time="+(System.currentTimeMillis()-lo));
		if (plName == null || plName.isEmpty()) {
			return;
		}
		ProcessBuilder pb = new ProcessBuilder(
				"D:\\Program Files\\MPC-HC\\mpc-hc64.exe", plName, "/play",
				"/fullscreen", "/monitor", "2");
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

	private BufferedImage getScaledImage(Image srcImg) {
		BufferedImage resizedImg = new BufferedImage(DeskCell.IMAGE_DIM,
				DeskCell.IMAGE_DIM, Transparency.TRANSLUCENT);
		Graphics2D g2 = resizedImg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg, 0, 0, DeskCell.IMAGE_DIM, DeskCell.IMAGE_DIM, null);
		g2.dispose();
		return resizedImg;
	}

}
