package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.markdownparser.HeadingParser;

public class HeadingParserTest {
	HeadingParser parser;
	
	@Before
	public void setup(){
		parser = new HeadingParser();
	}
	
	@Test
	public void testHeading(){
		String text = "Basic Heading";
		String line = "### " + text;
		String result = parser.processLine(line);
		assertTrue(result.contains("<h3>"));
		assertTrue(result.contains(text));
		assertTrue(result.contains("</h3>"));
	}
	
	@Test
	public void testHeadingInBlockquote(){
		String text = "Basic Heading";
		String line = "> ### " + text;
		String result = parser.processLine(line);
		assertTrue(result.contains("<h3>"));
		assertTrue(result.contains(text));
		assertTrue(result.contains("</h3>"));
	}
}
