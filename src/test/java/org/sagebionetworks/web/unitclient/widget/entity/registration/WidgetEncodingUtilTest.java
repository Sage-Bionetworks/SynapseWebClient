package org.sagebionetworks.web.unitclient.widget.entity.registration;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetEncodingUtil;

public class WidgetEncodingUtilTest {

	@Test
	public void testRoundTripEncodeDecode() {
		// round trip tests
		String in = "[#!/Synapse:syn123{value1:\"10%\" \nvalue2:[{v2a=\"1 and %0A\"},{v2b=\"2\"}]}]";
		String encoded = WidgetEncodingUtil.encodeValue(in);
		assertEquals("%5B#!/Synapse:syn123%7Bvalue1:\"10%25\" %0Avalue2:%5B%7Bv2a%3D\"1 and %250A\"%7D%2C%7Bv2b%3D\"2\"%7D%5D%7D%5D", encoded);
		String out = WidgetEncodingUtil.decodeValue(encoded);
		assertEquals(in, out);

		in = "{}-_.!~*'()[]:;\n\r/?&=+,#$%";
		encoded = WidgetEncodingUtil.encodeValue(in);
		out = WidgetEncodingUtil.decodeValue(encoded);
		assertEquals(in, out);

		// SWC-4685: verify that if these were previously encoded in the markdown, they're still properly
		// decoded
		encoded = "%2F %23 %3A %21";
		out = WidgetEncodingUtil.decodeValue(encoded);
		assertEquals("/ # : !", out);

		assertEquals("", WidgetEncodingUtil.encodeValue(""));
		assertEquals("", WidgetEncodingUtil.decodeValue(""));
		assertEquals("%", WidgetEncodingUtil.decodeValue("%25"));
		String oldParamName = "oldNonEscapedDot.png"; // now periods are escaped, but decoding one of these old values should not fail
		assertEquals(oldParamName, WidgetEncodingUtil.decodeValue(oldParamName));
		// test rolling decode window
		assertEquals("hi", WidgetEncodingUtil.decodeValue(WidgetEncodingUtil.encodeValue("hi")));
		assertEquals("123", WidgetEncodingUtil.decodeValue(WidgetEncodingUtil.encodeValue("123")));
		assertEquals("1234", WidgetEncodingUtil.decodeValue(WidgetEncodingUtil.encodeValue("1234")));
		assertEquals("123$", WidgetEncodingUtil.decodeValue(WidgetEncodingUtil.encodeValue("123$")));
		assertEquals("$123$", WidgetEncodingUtil.decodeValue(WidgetEncodingUtil.encodeValue("$123$")));
		assertEquals("$1$2$", WidgetEncodingUtil.decodeValue(WidgetEncodingUtil.encodeValue("$1$2$")));
		assertEquals("$1$2$3", WidgetEncodingUtil.decodeValue(WidgetEncodingUtil.encodeValue("$1$2$3")));
		assertEquals("$1$2$34", WidgetEncodingUtil.decodeValue(WidgetEncodingUtil.encodeValue("$1$2$34")));
		assertEquals("$1$2$345", WidgetEncodingUtil.decodeValue(WidgetEncodingUtil.encodeValue("$1$2$345")));
		assertEquals("$1$2$3456", WidgetEncodingUtil.decodeValue(WidgetEncodingUtil.encodeValue("$1$2$3456")));
		assertEquals("syn1234/version/5", WidgetEncodingUtil.decodeValue(WidgetEncodingUtil.encodeValue("syn1234/version/5"))); // regression test
	}

	@Test
	public void testDoubleRoundTripEncodeDecode() {
		// round trip tests
		String in = "[{value1:\"10%\" \nvalue2:[{v2a=\"1 and %0A\"},{v2b=\"2\"}]}]";
		String encoded = WidgetEncodingUtil.encodeValue(in);
		String secondEncoded = WidgetEncodingUtil.encodeValue(encoded);
		String secondOut = WidgetEncodingUtil.decodeValue(secondEncoded);
		String out = WidgetEncodingUtil.decodeValue(secondOut);
		assertEquals(encoded, secondOut);
		assertEquals(in, out);
	}
}
