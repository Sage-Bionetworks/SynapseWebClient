package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.markdownparser.CodeParser;
import org.sagebionetworks.web.server.markdownparser.MarkdownElements;

public class CodeParserTest {
	CodeParser parser;
	@Before
	public void setup(){
		parser = new CodeParser();
	}
	
	@Test
	public void testHappyCase(){
		String language = "ruby";
		String line = "``` " + language;
		MarkdownElements elements = new MarkdownElements(line);
		parser.processLine(elements, null);
		String result = elements.getHtml().toLowerCase();
		assertTrue(result.contains("<pre"));
		assertTrue(result.contains("<code"));
		assertTrue(result.contains("class=\"" + language));
		assertFalse(result.contains("</pre>"));
		assertFalse(result.contains("</code>"));
		
		assertTrue(parser.isInMarkdownElement());
		
		//second line
		line = "some code";
		elements = new MarkdownElements(line);
		parser.processLine(elements, null);
		result = elements.getHtml().toLowerCase();
		assertFalse(result.contains("<pre"));
		assertFalse(result.contains("<code"));
		assertTrue(result.contains(line));
		assertFalse(result.startsWith("\n"));
		assertFalse(result.contains("</pre>"));
		assertFalse(result.toLowerCase().contains("</code>"));
		
		assertTrue(parser.isInMarkdownElement());
		
		//third line
		line = "third line";
		elements = new MarkdownElements(line);
		parser.processLine(elements, null);
		result = elements.getHtml().toLowerCase();
		assertTrue(result.startsWith("\n"));
		
		//forth line
		line =  "```";
		elements = new MarkdownElements(line);
		parser.processLine(elements, null);
		result = elements.getHtml().toLowerCase();
		assertFalse(result.contains("<pre"));
		assertFalse(result.contains("<code"));
		assertTrue(result.contains("</pre>"));
		assertTrue(result.contains("</code>"));
		
		assertFalse(parser.isInMarkdownElement());
	}

}
