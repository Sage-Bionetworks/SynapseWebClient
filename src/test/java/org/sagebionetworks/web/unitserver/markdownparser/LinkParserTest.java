package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.*;
import org.sagebionetworks.web.server.markdownparser.BoldParser;
import org.sagebionetworks.web.server.markdownparser.ItalicsParser;
import org.sagebionetworks.web.server.markdownparser.LinkParser;

public class LinkParserTest {
	LinkParser parser;
	
	@Before
	public void setup(){
		parser = new LinkParser();
		parser.init();
	}
	
	@Test
	public void testBold(){
		String text = "This Is A Test";
		String href = "http://example.com";
		String line = "[" + text + "](" + href +")";
		String result = parser.processLine(line);
		assertTrue(result.contains("<a"));
		assertTrue(result.contains("href=\"" + href));
		assertTrue(result.contains(text));
	}
}
