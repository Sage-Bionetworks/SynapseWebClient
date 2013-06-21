package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.markdownparser.CodeSpanParser;

public class CodeSpanParserTest {
	CodeSpanParser parser;
	
	@Before
	public void setup(){
		parser = new CodeSpanParser();
	}
	
	@Test
	public void testCodeSpan(){
		String text = "a basic `code span` test";
		String result = parser.processLine(text);
		assertTrue(result.contains("<code>code span</code>"));
	}
}
