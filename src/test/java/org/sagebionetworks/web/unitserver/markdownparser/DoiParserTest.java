package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.markdownparser.DoiAutoLinkParser;
import org.sagebionetworks.web.server.markdownparser.ItalicsParser;
import org.sagebionetworks.web.server.markdownparser.MarkdownElements;

public class DoiParserTest {
	DoiAutoLinkParser parser;
	
	@Before
	public void setup(){
		parser = new DoiAutoLinkParser();
	}
	
	@Test
	public void testDoi(){
		String exampleDoi = "doi:10.1111/j.1749-6632.2008.03755.x";
		String text = "link to the "+exampleDoi+" for the challenge";
		MarkdownElements elements = new MarkdownElements(text);
		parser.processLine(elements);
		String result = elements.getHtml();
		//link should go refer to the doi
		assertTrue(result.contains("href=\"http://dx.doi.org/"+exampleDoi+"\""));
		//and the text itself should contain "doi"
		assertTrue(result.contains(">"+exampleDoi+"<"));
	}
}
