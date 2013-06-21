package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.server.markdownparser.TableParser;

public class TableParserTest {
	TableParser parser;
	@Before
	public void setup(){
		parser = new TableParser();
	}
	
	@Test
	public void testExampleTable(){
		String exampleLine1 = "Row 1 Content Cell 1 | Row 1 Content Cell 2  | Row 1 Content Cell 3";
		String exampleLine2 = "Row 2 Content Cell 1  | Row 2 Content Cell 2  | Row 2 Content Cell 3";
		StringBuilder tableOutput = new StringBuilder();
		tableOutput.append(parser.processLine(exampleLine1));
		tableOutput.append(parser.processLine(exampleLine2));
		tableOutput.append(parser.processLine(""));
		//check for a few items
		String html = tableOutput.toString();
		assertTrue(html.contains("<table id=\""+WidgetConstants.MARKDOWN_TABLE_ID_PREFIX+"0"));
		assertTrue(html.contains("<tr><th>Row 1 Content Cell 1 </th>"));
		assertTrue(html.contains("</table>"));
	}
}
