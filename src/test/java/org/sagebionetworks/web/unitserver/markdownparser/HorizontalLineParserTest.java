package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.markdownparser.HorizontalLineParser;

public class HorizontalLineParserTest {
	HorizontalLineParser parser;
	
	@Before
	public void setup(){
		parser = new HorizontalLineParser();
	}
	
	@Test
	public void testHR1(){
		String line = "---";
		String result = parser.processLine(line);
		assertTrue(result.toLowerCase().contains("<hr>"));
		
		//or more than 3
		line = "-----";
		result = parser.processLine(line);
		assertTrue(result.toLowerCase().contains("<hr>"));
	}
	
	@Test
	public void testHR2(){
		String line = "***";
		String result = parser.processLine(line);
		assertTrue(result.toLowerCase().contains("<hr>"));
		
		//or more than 3
		line = "*******";
		result = parser.processLine(line);
		assertTrue(result.toLowerCase().contains("<hr>"));
	}
	
	@Test
	public void testNotHRs(){
		String line = "dashes or asterisks *** in the line ---";
		String result = parser.processLine(line);
		assertFalse(result.toLowerCase().contains("<hr>"));
	}

}
