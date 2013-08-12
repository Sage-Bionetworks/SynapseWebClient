package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.markdownparser.BacktickParser;
import org.sagebionetworks.web.server.markdownparser.CodeSpanParser;
import org.sagebionetworks.web.server.markdownparser.MarkdownElements;

public class BacktickParserTest {
	BacktickParser parser;
	CodeSpanParser codeParser;
	
	@Before
	public void setup() {
		parser = new BacktickParser();
		codeParser = new CodeSpanParser();
	}
	
	@Test
	public void testEscaping() {
		String text = "\\`not code span\\`";
		MarkdownElements elements = new MarkdownElements(text);
		//Escape first, then use other parsers
		parser.processLine(elements);
		codeParser.processLine(elements);
		assertEquals(elements.getHtml(), "&#96;not code span&#96;");
		assertFalse(elements.getHtml().contains("<code>not code span</code>"));
	}
}
