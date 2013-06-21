package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.markdownparser.ImageParser;

public class ImageParserTest {
	ImageParser parser;
	
	@Before
	public void setup(){
		parser = new ImageParser();
	}
	
	@Test
	public void test(){
		String url = "http://test.com/a.png";
		String altText = "An Image";
		String line = "![" + altText+ "](" + url + ")";
		String result = parser.processLine(line);
		
		assertTrue(result.contains("<img"));
		assertTrue(result.contains("src=\"" + url));
		assertTrue(result.contains("alt=\"" + altText));
	}
}
