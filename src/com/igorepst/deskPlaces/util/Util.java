package com.igorepst.deskPlaces.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import com.igorepst.deskPlaces.ui.DeskCell;

public class Util {

	private static final String PLAYLIST_NAME = "deskPlacesPlaylist.m3u";
	public static final String dataDivider = "\\|";

	private static final String[] MEDIA_EXTS = { "mkv", "avi", "mp4", "mpg",
			"ts", "divx", "vob", "mpeg", "mpe", "m1v", "m2v", "mpv2", "mp2v",
			"m2p", "pva", "evo", "tp", "trp", "m2t", "m2ts", "mts", "rec",
			"ifo", "webm", "m4v", "mp4v", "mpv4", "hdmov", "mov", "3gp",
			"3gpp", "3ga", "3g2", "3gp2", "flv", "f4v", "ogm", "ogv", "rm",
			"rmvb", "rt", "ram", "rpm", "rmm", "rp", "smi", "smil", "wmv",
			"wmp", "wm", "asf", "smk", "bik", "fli", "flc", "flic", "dsm",
			"dsv", "dsa", "dss", "ivf", "swf", "amv", "ac3", "dts", "aif",
			"aifc", "aiff", "alac", "amr", "ape", "apl", "au", "snd", "cda",
			"flac", "m4a", "m4b", "m4r", "aac", "mid", "midi", "rmi", "mka",
			"mp3", "mpa", "mp2", "m1a", "m2a", "mpc", "ofr", "ofs", "ogg",
			"oga", "opus", "ra", "tak", "tta", "wav", "wma", "wv", "aob", "mlp" };

	private enum POSSIBLE_IMAGES {
		JPG("jpg", "jpeg", "jpe"), PNG("png"), GIF("gif");

		private static final POSSIBLE_IMAGES[] VALUES = POSSIBLE_IMAGES
				.values();

		private final String[] extensions;

		private POSSIBLE_IMAGES(final String... extensions) {
			this.extensions = extensions;
			if (extensions.length > 1) {
				Arrays.sort(this.extensions);
			}
		}
	};

	private static final TreeSet<String> mediaSet = new TreeSet<>();
	private static final TreeSet<String> imgSet = new TreeSet<>();

	private static int currentIndex;

	static {
		for (String format : Util.MEDIA_EXTS) {
			Util.mediaSet.add(format.toLowerCase(Locale.ENGLISH));
		}

		for (POSSIBLE_IMAGES pi : POSSIBLE_IMAGES.VALUES) {
			for (String ext : pi.extensions) {
				Util.imgSet.add(ext.toLowerCase(Locale.ENGLISH));
			}
		}
	}

	public static String resolveImageFormat(String imageName) {
		int i = imageName.lastIndexOf('.');
		if (i >= 0) {
			String extension = imageName.substring(i + 1).toLowerCase(
					Locale.getDefault());
			for (POSSIBLE_IMAGES pi : POSSIBLE_IMAGES.VALUES) {
				if (Arrays.binarySearch(pi.extensions, extension) >= 0) {
					return pi.name();
				}
			}
		}
		return POSSIBLE_IMAGES.PNG.name();
	}

	private static boolean accept(String name, Set<String> set) {
		Util.currentIndex = name.lastIndexOf('.');
		if (Util.currentIndex == -1 || Util.currentIndex == 0
				|| Util.currentIndex == name.length() - 1) {
			return false;
		}
		String ext = name.substring(Util.currentIndex + 1).toLowerCase(
				Locale.ENGLISH);
		return set.contains(ext);
	}

	private static List<String> getFileNamesRecursively(final File directory) {
		if (directory.isFile()
				&& Util.accept(directory.getName(), Util.mediaSet)) {
			ArrayList<String> list = new ArrayList<>(1);
			list.add(directory.getAbsolutePath());
			return list;
		}
		if (!directory.isDirectory()) {
			return null;
		}
		ArrayList<String> list = new ArrayList<>();
		Util.getFileNamesRecursively(directory, list);
		return list;
	}

	public static String getPlaylist(final File directory,
			final boolean randomize) {
		List<String> list = Util.getFileNamesRecursively(directory);
		if (list == null || list.isEmpty()) {
			return null;
		}
		if (list.size() > 1) {
			if (randomize) {
				Collections.shuffle(list);
			} else {
				// As File.list doesn't guarantee an AB order
				Collections.sort(list, new NaturalOrderComparator());
			}
		}
		return Util.writeToPlaylist(list);
	}

	private static String writeToPlaylist(List<String> list) {
		File plFile = new File(System.getProperty("java.io.tmpdir"),
				Util.PLAYLIST_NAME);
		PrintWriter fw = null;
		try {
			fw = new PrintWriter(plFile, "UTF-8");
			for (String fn : list) {
				fw.println(fn);
			}
		} catch (IOException ignore) {
			return null;
		} finally {
			if (fw != null) {
				fw.close();
			}
		}
		return plFile.getAbsolutePath();
	}

	private static void getFileNamesRecursively(File file, List<String> list) {
		for (File f : file.listFiles()) {
			if (f.isDirectory()) {
				Util.getFileNamesRecursively(f, list);
			} else if (Util.accept(f.getPath(), Util.mediaSet)) {
				list.add(f.getAbsolutePath());
			}
		}
	}

	public static List<ImgFile> getImgFiles(final File directory) {
		File[] files = directory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile();
			}
		});
		List<ImgFile> list = new ArrayList<ImgFile>(files.length);
		String name;
		for (File file : files) {
			name = file.getName();
			if (Util.accept(name, Util.imgSet)) {
				list.add(new ImgFile(name.substring(0, Util.currentIndex), file));
			}
		}
		Collections.sort(list);
		return list;
	}

	public static void deleteOldThumbs() throws IOException {
		final Path start = DeskCell.DESK_HOME_DIR.toPath();
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException e)
					throws IOException {
				if (e == null) {
					if (!dir.equals(start)) {
						Files.delete(dir);
					}
					return FileVisitResult.CONTINUE;
				} else {
					// directory iteration failed
					throw e;
				}
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir,
					BasicFileAttributes attrs) throws IOException {
				if (dir.equals(start)) {
					return FileVisitResult.CONTINUE;
				}
				if (dir.equals(DeskCell.IMAGE_DIR_THUMBS.toPath())) {
					return FileVisitResult.SKIP_SUBTREE;
				}
				return dir.toFile().getName()
						.startsWith(Settings.THUMBS_DIR_PREFIX) ? FileVisitResult.CONTINUE
						: FileVisitResult.SKIP_SUBTREE;
			}
		});

	}

	public static BufferedImage getScaledImage(Image srcImg, final int IMAGE_DIM) {
		BufferedImage resizedImg = new BufferedImage(IMAGE_DIM, IMAGE_DIM,
				Transparency.TRANSLUCENT);
		Graphics2D g2 = resizedImg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg, 0, 0, IMAGE_DIM, IMAGE_DIM, null);
		g2.dispose();
		return resizedImg;
	}

	public static String transformSubst(final String subst){
		return subst.toLowerCase(Locale.getDefault()).replace('/', '\\');
	}
}
