package org.sagebionetworks.web.unitclient.widget.entity.registration;

import junit.framework.Assert;

import org.junit.Test;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetEncodingUtil;

public class WidgetEncodingUtilTest {

	@Test
	public void testRoundTripEncodeDecode() {
		//round trip tests
		String in = "[{value1:\"10%\" \nvalue2:[{v2a=\"1 and %0A\"},{v2b=\"2\"}]}]";
		String encoded = WidgetEncodingUtil.encodeValue(in);
		Assert.assertEquals("%5B%7Bvalue1%3A\"10%25\" %0Avalue2%3A%5B%7Bv2a%3D\"1 and %250A\"%7D%2C%7Bv2b%3D\"2\"%7D%5D%7D%5D", encoded);
		String out = WidgetEncodingUtil.decodeValue(encoded);
		Assert.assertEquals(in, out);
	
		in = "{}-_.!~*'()[]:;\n\r/?&=+,#$%";
		encoded = WidgetEncodingUtil.encodeValue(in);
		out = WidgetEncodingUtil.decodeValue(encoded);
		Assert.assertEquals(in, out);
		
		Assert.assertEquals("", WidgetEncodingUtil.encodeValue(""));
		Assert.assertEquals("", WidgetEncodingUtil.decodeValue(""));
		Assert.assertEquals("%", WidgetEncodingUtil.decodeValue("%25"));
		String oldParamName = "oldNonEscapedDot.png";  //now periods are escaped, but decoding one of these old values should not fail
		Assert.assertEquals(oldParamName, WidgetEncodingUtil.decodeValue(oldParamName));
		//test rolling decode window
		Assert.assertEquals("hi", WidgetEncodingUtil.decodeValue("hi"));
		Assert.assertEquals("123", WidgetEncodingUtil.decodeValue("123"));
		Assert.assertEquals("1234", WidgetEncodingUtil.decodeValue("1234"));
	}
	

	@Test
	public void testDoubleRoundTripEncodeDecode() {
		//round trip tests
		String in = "[{value1:\"10%\" \nvalue2:[{v2a=\"1 and %0A\"},{v2b=\"2\"}]}]";
		String encoded = WidgetEncodingUtil.encodeValue(in);
		String secondEncoded = WidgetEncodingUtil.encodeValue(encoded);		
		String secondOut = WidgetEncodingUtil.decodeValue(secondEncoded);		
		String out = WidgetEncodingUtil.decodeValue(secondOut);
		Assert.assertEquals(encoded, secondOut);
		Assert.assertEquals(in, out);
	}
	
	
	
}
