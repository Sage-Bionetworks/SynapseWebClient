package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.server.markdownparser.MarkdownElements;
import org.sagebionetworks.web.server.markdownparser.TableParser;

public class TableParserTest {
	TableParser parser;
	@Before
	public void setup(){
		parser = new TableParser();
		parser.reset();
	}
	
	@Test
	public void testExampleTable(){
		String start = "{| class=\"border\"";
		String exampleLine1 = "Row 1 Content Cell 1 | Row 1 Content Cell 2  | Row 1 Content Cell 3";
		String exampleLine2 = "Row 2 Content Cell 1  | Row 2 Content Cell 2  | Row 2 Content Cell 3";
		String end = "|}";
		StringBuilder tableOutput = new StringBuilder();
		MarkdownElements elements = new MarkdownElements(start);
		parser.processLine(elements);
		tableOutput.append(elements.getHtml());
		
		elements = new MarkdownElements(exampleLine1);
		parser.processLine(elements);
		tableOutput.append(elements.getHtml());
		
		elements = new MarkdownElements(exampleLine2);
		parser.processLine(elements);
		tableOutput.append(elements.getHtml());
		
		elements = new MarkdownElements(end);
		parser.processLine(elements);
		tableOutput.append(elements.getHtml());
		
		elements = new MarkdownElements("");
		parser.processLine(elements);
		tableOutput.append(elements.getHtml());
		//check for a few items
		String html = tableOutput.toString();
		System.out.println("HTML: " + html);
		assertTrue(html.contains("<table id=\""+WidgetConstants.MARKDOWN_TABLE_ID_PREFIX+"0"));
		assertTrue(html.contains("class=\"tablesorter markdowntable border\""));
		assertTrue(html.contains("<tr><td>Row 1 Content Cell 1 </td>"));
		assertTrue(html.contains("</table>"));
	}
	
	@Test
	public void testTableWithHeader() {
		String exampleLine1 = "Row 1 Content Cell 1 | Row 1 Content Cell 2  | Row 1 Content Cell 3";
		String exampleLine2 = "--|--|--";
		StringBuilder tableOutput = new StringBuilder();
		MarkdownElements elements = new MarkdownElements(exampleLine1);
		parser.processLine(elements);
		tableOutput.append(elements.getHtml());
		
		elements = new MarkdownElements(exampleLine2);
		parser.processLine(elements);
		tableOutput.append(elements.getHtml());
		
		elements = new MarkdownElements("");
		parser.processLine(elements);
		tableOutput.append(elements.getHtml());
		//check for a few items
		String html = tableOutput.toString();
		System.out.println("HTML: " + html);
		assertTrue(html.contains("<tr><th>Row 1 Content Cell 1 </th>"));
		assertFalse(html.contains("--"));
	}
}
