package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.markdownparser.HorizontalLineParser;
import org.sagebionetworks.web.server.markdownparser.MarkdownElements;

public class HorizontalLineParserTest {
	HorizontalLineParser parser;
	
	@Before
	public void setup(){
		parser = new HorizontalLineParser();
	}
	
	@Test
	public void testHR1(){
		String line = "---";
		MarkdownElements elements = new MarkdownElements(line);
		parser.processLine(elements, null);
		String result = elements.getHtml();
		assertTrue(result.toLowerCase().contains("<hr>"));
		
		//or more than 3
		line = "-----";
		elements = new MarkdownElements(line);
		parser.processLine(elements, null);
		result = elements.getHtml();
		assertTrue(result.toLowerCase().contains("<hr>"));
	}
	
	@Test
	public void testHR2(){
		String line = "***";
		MarkdownElements elements = new MarkdownElements(line);
		parser.processLine(elements, null);
		String result = elements.getHtml();
		assertTrue(result.toLowerCase().contains("<hr>"));
		
		//or more than 3
		line = "*******";
		elements = new MarkdownElements(line);
		parser.processLine(elements, null);
		result = elements.getHtml();
		assertTrue(result.toLowerCase().contains("<hr>"));
	}
	
	@Test
	public void testNotHRs(){
		String line = "dashes or asterisks *** in the line ---";
		MarkdownElements elements = new MarkdownElements(line);
		parser.processLine(elements, null);
		String result = elements.getHtml();
		assertFalse(result.toLowerCase().contains("<hr>"));
	}

}
