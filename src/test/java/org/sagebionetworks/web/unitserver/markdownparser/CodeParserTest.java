package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.*;
import org.sagebionetworks.web.server.markdownparser.CodeParser;

public class CodeParserTest {
	CodeParser parser;
	@Before
	public void setup(){
		parser = new CodeParser();
		parser.init();
	}
	
	@Test
	public void testHappyCase(){
		String language = "ruby";
		String line = "``` " + language;
		String result = parser.processLine(line);
		assertTrue(result.toLowerCase().contains("<pre"));
		assertTrue(result.toLowerCase().contains("<code"));
		assertTrue(result.toLowerCase().contains("class=\"" + language));
		assertFalse(result.toLowerCase().contains("</pre>"));
		assertFalse(result.toLowerCase().contains("</code>"));
		
		assertTrue(parser.isInMarkdownElement());
		
		//second line
		line = "some code";
		result = parser.processLine(line);
		assertFalse(result.toLowerCase().contains("<pre"));
		assertFalse(result.toLowerCase().contains("<code"));
		assertTrue(result.contains(line));
		assertTrue(result.endsWith("\n")); //should add it's own newline, since it's reporting to be a block element (therefore handling line breaks)
		assertFalse(result.toLowerCase().contains("</pre>"));
		assertFalse(result.toLowerCase().contains("</code>"));
		
		assertTrue(parser.isInMarkdownElement());
		
		//third line
		line =  "```";
		result = parser.processLine(line);
		assertFalse(result.toLowerCase().contains("<pre"));
		assertFalse(result.toLowerCase().contains("<code"));
		assertTrue(result.toLowerCase().contains("</pre>"));
		assertTrue(result.toLowerCase().contains("</code>"));
		
		assertFalse(parser.isInMarkdownElement());
	}

}
