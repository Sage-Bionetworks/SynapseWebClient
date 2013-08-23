package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.server.SynapseMarkdownProcessor;
import org.sagebionetworks.web.server.markdownparser.LinkParser;
import org.sagebionetworks.web.server.markdownparser.MarkdownElementParser;
import org.sagebionetworks.web.server.markdownparser.ReferenceParser;
import org.sagebionetworks.web.server.markdownparser.MarkdownElements;
import org.sagebionetworks.web.shared.WebConstants;

public class ReferenceParserTest {
	SynapseMarkdownProcessor processor; 
	ReferenceParser parser;
	List<MarkdownElementParser> simpleParsers;
	
	@Before
	public void setup(){
		parser = new ReferenceParser();
		parser.reset();
		
		processor = SynapseMarkdownProcessor.getInstance();
		simpleParsers = new ArrayList<MarkdownElementParser>();
		simpleParsers.add(new LinkParser());
	}
	
	@Test
	public void testReference() throws IOException {
		//Test different ordering of parameters
		String text = "The statement was from here ${reference?inlineWidget=true&text=Smith John%2E Cooking book%2E August 2 2013}.";
		String text2 = "The statement was from here ${reference?text=Smith John%2E Cooking book%2E August 2 2013&inlineWidget=true}.";
		MarkdownElements elements = new MarkdownElements(text);
		MarkdownElements elements2 = new MarkdownElements(text2);
		parser.processLine(elements, simpleParsers);
		parser.processLine(elements2, simpleParsers);
		
		assertTrue(elements.getMarkdown().contains("${reference?inlineWidget=true&text=Smith John%2E Cooking book%2E August 2 2013&footnoteId=1}."));
		assertTrue(elements2.getMarkdown().contains("${reference?text=Smith John%2E Cooking book%2E August 2 2013&inlineWidget=true&footnoteId=2}."));
		//Check for footnotes at end of document
		String fullText = "The statement was from here ${reference?inlineWidget=true&text=Smith John%2E Cooking book%2E August 2 2013}.\nThe statement was from here ${reference?text=Smith John%2E Cooking book%2E August 2 2013&inlineWidget=true}.";
		String result = processor.markdown2Html(fullText, false);
		assertTrue(result.contains("widgetparams=\"bookmark?text=[1]")); // bookmark, [1] that links back to reference
		assertTrue(result.contains("widgetparams=\"bookmark?text=[2]")); // bookmark, [2] that links back to reference
		assertTrue(result.contains("<p id=\"wikiFootnote1\" class=\"inlineWidgetContainer\">Smith John. Cooking book. August 2 2013</p>"));
		assertTrue(result.contains("<p id=\"wikiFootnote2\" class=\"inlineWidgetContainer\">Smith John. Cooking book. August 2 2013</p>"));
	}
	
	@Test
	public void testReferenceWithUrl() throws IOException {
		String text = "The statement was from here ${reference?text=So et al%2E %5BYahoo%5D%28http%3A%2F%2Fwww%2Eyahoo%2Ecom%29%2E July 2013&inlineWidget=true}.";
		String result = processor.markdown2Html(text, false);
		assertTrue(result.contains("widgetparams=\"bookmark?text=[1]")); // bookmark, [1] that links back to reference	
		assertTrue(result.contains("<a class=\"link\" target=\"_blank\" href=\"http://www.yahoo.com\">Yahoo</a>"));
		
	}
	
}