package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.markdownparser.ItalicsParser;

public class ItalicsParserTest {
	ItalicsParser parser;
	
	@Before
	public void setup(){
		parser = new ItalicsParser();
	}
	
	@Test
	public void testItalics(){
		String text = "*this* should be italicized, and so should _that_";
		String result = parser.processLine(text);
		assertTrue(result.contains("<em>this</em>"));
		assertTrue(result.contains("<em>that</em>"));
	}
}
