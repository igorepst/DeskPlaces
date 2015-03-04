package com.igorepst.deskPlaces.util;

import java.io.File;

public class ImgFile implements Comparable<ImgFile> {
	public final String fileName;
	public final File file;

	public ImgFile(String fileName, File file) {
		this.file = file;
		this.fileName = fileName;
	}

	@Override
	public int compareTo(ImgFile o) {
		return fileName.compareTo(o.fileName);
	}
}
