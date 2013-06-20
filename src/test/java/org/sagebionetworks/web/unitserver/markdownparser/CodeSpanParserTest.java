package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.*;
import org.sagebionetworks.web.server.markdownparser.BoldParser;
import org.sagebionetworks.web.server.markdownparser.CodeSpanParser;

public class CodeSpanParserTest {
	CodeSpanParser parser;
	
	@Before
	public void setup(){
		parser = new CodeSpanParser();
		parser.init();
	}
	
	@Test
	public void testCodeSpan(){
		String text = "a basic `code span` test";
		String result = parser.processLine(text);
		assertTrue(result.contains("<code>code span</code>"));
	}
}
