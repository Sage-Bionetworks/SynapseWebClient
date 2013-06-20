package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.*;
import org.sagebionetworks.web.server.markdownparser.ListParser;

public class ListParserTest {
	
	ListParser parser;
	
	@Before
	public void setup(){
		parser = new ListParser();
		parser.init();
	}
	
	@Test
	public void testNestedExample(){
		String example = 
				"*   Abacus\n"+
				"    * answer\n"+
				"*   Bubbles\n"+
				"    1.  bunk\n"+
				"    2.  bupkis\n"+
				"        * BELITTLER\n"+
				"    3. burper\n"+
				"*   Cunning\n";
		String[] lines = example.split("\n");
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < lines.length; i++) {
			result.append(parser.processLine(lines[i]));
		}
		//process a final empty line (just like the processor)
		//so that 
		result.append(parser.processLine(""));
		assertEquals("<ul><li>   Abacus</li><ul><li> answer</li></ul><li>   Bubbles</li><ol><li>  bunk</li><li>  bupkis</li><ul><li> BELITTLER</li></ul><li> burper</li></ol><li>   Cunning</li></ul>", result.toString());
	}
}
