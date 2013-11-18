package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.markdownparser.MarkdownElements;
import org.sagebionetworks.web.server.markdownparser.ReferenceParser;
import org.sagebionetworks.web.server.markdownparser.SynapseMarkdownWidgetParser;

public class SynapseMarkdownWidgetParserTest {
	SynapseMarkdownWidgetParser parser;
	ReferenceParser refParser;
	
	@Before
	public void setup() {
		parser = new SynapseMarkdownWidgetParser();
		parser.reset(null);
		
		refParser = new ReferenceParser();
		refParser.reset(null);
	}
	
	@Test
	public void test() {
		String text = "This is an example ${provenance?entityList=syn1234567&depth=1&showExpand=true} of extracting.";
		MarkdownElements elements = new MarkdownElements(text);
		parser.processLine(elements);
		StringBuilder sb = new StringBuilder();
		String result = elements.getHtml();
		sb.append(result);
		assertTrue(result.contains("<span"));
		assertFalse(result.contains("${provenance?entityList=syn1234567&depth=1&showExpand=true}"));
		
		parser.completeParse(sb);
		Document doc = Jsoup.parse(sb.toString());
		parser.completeParse(doc);
		assertTrue(doc.html().contains("provenance?entityList=syn1234567&amp;depth=1&amp;showExpand=true"));
		assertFalse(doc.html().contains("wikipages"));
	}
	
	@Test
	public void testReference() {
		String text = "This is a ref ${reference?text=So et al%2E B%2A%2Aold%2A%2Aed%2E Traveling%2E&inlineWidget=true}.";
		MarkdownElements elements = new MarkdownElements(text);
		refParser.processLine(elements);
		parser.processLine(elements);
		String result = elements.getHtml();
		assertTrue(result.contains("<span"));
	}
}
