package org.sagebionetworks.web.client;

import java.util.List;

public class ArrayUtils {

	public static double[] getDoubleArray(List<String> l) {
		double[] d = new double[l.size()];
		for (int i = 0; i < l.size(); i++) {
			d[i] = Double.valueOf(l.get(i));
		}
		return d;
	}

	public static String[] getStringArray(List<String> l) {
		String[] d = new String[l.size()];
		for (int i = 0; i < l.size(); i++) {
			d[i] = l.get(i);
		}
		return d;
	}

}
