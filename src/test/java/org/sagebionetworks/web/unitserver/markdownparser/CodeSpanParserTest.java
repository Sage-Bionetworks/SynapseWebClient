package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.markdownparser.CodeSpanParser;
import org.sagebionetworks.web.server.markdownparser.MarkdownElements;

public class CodeSpanParserTest {
	CodeSpanParser parser;
	
	@Before
	public void setup(){
		parser = new CodeSpanParser();
	}
	
	@Test
	public void testCodeSpan(){
		String text = "a basic `code span` test";
		MarkdownElements elements = new MarkdownElements(text);
		parser.processLine(elements);
		assertTrue(elements.getHtml().contains("<code>code span</code>"));
	}
}
