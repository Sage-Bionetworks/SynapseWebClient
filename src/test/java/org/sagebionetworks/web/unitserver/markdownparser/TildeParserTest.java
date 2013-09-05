package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.markdownparser.MarkdownElements;
import org.sagebionetworks.web.server.markdownparser.SubscriptParser;
import org.sagebionetworks.web.server.markdownparser.TildeParser;

public class TildeParserTest {
	TildeParser parser;
	SubscriptParser subscriptParser;
	
	@Before
	public void setup() {
		parser = new TildeParser();
		subscriptParser = new SubscriptParser();
	}
	
	@Test
	public void testEscaping() {
		String text = "There's \\~10 people but we want ~5.";
		MarkdownElements elements = new MarkdownElements(text);
		parser.processLine(elements);
		subscriptParser.processLine(elements);
		String result = elements.getHtml();
		assertEquals(result, "There's &#126;10 people but we want ~5.");
		assertFalse(result.contains("<sub>10 people but we want </sub>5."));
	}
}
