package org.sagebionetworks.web.client.widget.entity.registration;

import java.util.HashMap;
import java.util.Map;

public class WidgetEncodingUtil {

	private static Map<Character, String> c2h = new HashMap<Character, String>();
	private static Map<String, Character> h2c = new HashMap<String, Character>();

	static {
		// encodes { } - _ . ~ * ' ( ) [ ] ; ? & = + , $ % |
		c2h.put('{', "%7B");
		c2h.put('}', "%7D");
		c2h.put('-', "%2D");
		c2h.put('_', "%5F");
		c2h.put('.', "%2E");
		c2h.put('~', "%7E");
		c2h.put('*', "%2A");
		c2h.put('`', "%60");
		c2h.put('\'', "%27");
		c2h.put('(', "%28");
		c2h.put(')', "%29");
		c2h.put('[', "%5B");
		c2h.put(']', "%5D");
		c2h.put(';', "%3B");
		c2h.put('\n', "%0A"); // LF
		c2h.put('\r', "%0D"); // CR
		c2h.put('?', "%3F");
		c2h.put('&', "%26");
		c2h.put('=', "%3D");
		c2h.put('+', "%2B");
		c2h.put(',', "%2C");
		c2h.put('$', "%24");
		c2h.put('%', "%25");
		c2h.put('|', "%7C");


		// reverse lookup for decode
		for (Character v : c2h.keySet()) {
			h2c.put(c2h.get(v), v);
		}
		// support decoding these values, but do not encode them since they don't conflict with Synapse
		// markdown widget syntax.
		h2c.put("%2F", '/');
		h2c.put("%23", '#');
		h2c.put("%3A", ':');
		h2c.put("%21", '!');
	}

	public static String encodeValue(String value) {
		if (value == null)
			return null;

		StringBuilder newValue = new StringBuilder(value);
		// and encode everything that URL says that it doesn't encode (and more).
		int totalCount = newValue.length();
		for (int i = 0; i < totalCount; i++) {
			char c = newValue.charAt(i);
			if (c2h.containsKey(c)) {
				newValue.deleteCharAt(i); // -1 character
				String replacement = c2h.get(c);
				newValue.insert(i, replacement); // +(replacement.length()) characters
				totalCount += replacement.length() - 1;
				i += replacement.length() - 1;
			}
		}
		return newValue.toString();
	}

	public static String decodeValue(String value) {
		final int WINDOW = 3;

		if (value == null)
			return null;

		// detect the hex codes using a sliding window (of 3 characters)
		// if the input value is less than 3 in length, then there's nothing to decode
		if (value.length() < WINDOW) {
			return value;
		}
		// build up the output
		StringBuilder output = new StringBuilder();

		int start = 0;
		int end = WINDOW;
		for (; end <= value.length();) {
			String currentSubString = value.substring(start, end);
			if (h2c.containsKey(currentSubString)) {
				// found one, add the resolved character to the output and skip ahead
				output.append(h2c.get(currentSubString));
				start += WINDOW;
				end += WINDOW;
			} else {
				// is not one, just append the character at start, and move the window over
				output.append(value.charAt(start));
				start++;
				end++;
			}
		}

		// append any trailing characters
		output.append(value.substring(start));

		return output.toString();
	}

}
