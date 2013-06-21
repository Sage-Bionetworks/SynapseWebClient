package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.markdownparser.BlockQuoteParser;

public class BlockQuoteParserTest {
	
	BlockQuoteParser parser;
	@Before
	public void setup(){
		parser = new BlockQuoteParser();
	}
	
	@Test
	public void testIsNotSingleLine(){
		assertFalse(parser.isInputSingleLine());
	}
	
	@Test
	public void testHappyCase(){
		String text = "first line text";
		String line = "> " + text;
		String result = parser.processLine(line);
		assertTrue(result.toLowerCase().contains("<blockquote"));
		assertTrue(result.contains(text));
		assertFalse(result.toLowerCase().contains("</blockquote>"));
		
		assertTrue(parser.isInMarkdownElement());
		
		//second line
		text = "second line text";
		line = " \t> " + text;
		result = parser.processLine(line);
		assertFalse(result.toLowerCase().contains("<blockquote"));
		assertTrue(result.contains(text));
		assertFalse(result.toLowerCase().contains("</blockquote>"));
		
		assertTrue(parser.isInMarkdownElement());
		
		//third line
		text = "third line not in blockquote";
		line =  text;
		result = parser.processLine(line);
		assertFalse(result.toLowerCase().contains("<blockquote"));
		assertTrue(result.contains(text));
		assertTrue(result.toLowerCase().contains("</blockquote>"));
		
		assertFalse(parser.isInMarkdownElement());
	}

	//TODO: add more to test regular expression
}
