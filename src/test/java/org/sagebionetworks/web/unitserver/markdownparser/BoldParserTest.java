package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.markdownparser.BoldParser;

public class BoldParserTest {
	BoldParser parser;
	
	@Before
	public void setup(){
		parser = new BoldParser();
	}
	
	@Test
	public void testBold(){
		String text = "**this** should be bold, and so should __that__";
		String result = parser.processLine(text);
		assertTrue(result.contains("<strong>this</strong>"));
		assertTrue(result.contains("<strong>that</strong>"));
	}
}
