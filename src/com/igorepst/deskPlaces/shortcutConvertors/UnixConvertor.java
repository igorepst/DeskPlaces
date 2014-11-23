package com.igorepst.deskPlaces.shortcutConvertors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

public class UnixConvertor {

	public static final String DATA_HEADER = "[Data]";
	private static final String inDir = "D:\\My Documents\\multPlaces\\lin";
	public static final String outFile = "D:\\My Documents\\multPlaces\\multPlace.cfg";
	private static final String namePrefix = "Name=";
	private static final int namePrefixLen = UnixConvertor.namePrefix.length();
	private static final String iconPrefix = "Icon=";
	private static final String execPrefix = "Exec=";
	private static final Pattern splitPattern = Pattern.compile("/");
	private static final String paramStr = "--random";
	public static final char dataDivider = ';';

	public static void main(String[] args) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(UnixConvertor.outFile, "UTF-8");
			out.println(UnixConvertor.DATA_HEADER);
			for (File xdgFile : new File(UnixConvertor.inDir).listFiles()) {
				BufferedReader br = null;
				try {
					br = new BufferedReader(new FileReader(xdgFile));
					String line, name = null, icon = null, playlist = null, param = null;
					String[] splitArr;
					while ((line = br.readLine()) != null) {
						if (line.startsWith(UnixConvertor.namePrefix)) {
							name = line.substring(UnixConvertor.namePrefixLen);
						} else if (line.startsWith(UnixConvertor.iconPrefix)) {
							splitArr = UnixConvertor.splitPattern.split(line);
							icon = splitArr[splitArr.length - 1];
						} else if (line.startsWith(UnixConvertor.execPrefix)) {
							splitArr = UnixConvertor.splitPattern.split(line);
							playlist = splitArr[splitArr.length - 1];
							if (line.indexOf(UnixConvertor.paramStr) > -1) {
								param = UnixConvertor.paramStr;
							}
						}
					}
					if (playlist != null && !playlist.isEmpty()) {
						StringBuilder bld = new StringBuilder(name == null ? ""
								: name).append(UnixConvertor.dataDivider);
						bld.append(icon == null ? "" : icon)
								.append(UnixConvertor.dataDivider)
								.append(playlist)
								.append(UnixConvertor.dataDivider)
								.append(param == null ? "" : param);
						out.println(bld.toString());
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (br != null) {
						try {
							br.close();
						} catch (IOException e) {
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

}
