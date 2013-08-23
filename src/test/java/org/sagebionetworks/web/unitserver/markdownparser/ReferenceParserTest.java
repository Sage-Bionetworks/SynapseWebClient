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
		
		LinkParser linkParser = new LinkParser();
		linkParser.reset();
		processor = SynapseMarkdownProcessor.getInstance();
		simpleParsers = new ArrayList<MarkdownElementParser>();
		simpleParsers.add(linkParser);
	}
	
	@Test
	public void testReference() throws IOException {
		//Test different ordering of parameters
		String text = "The statement was from here ${reference?inlineWidget=true&text=Smith John%2E Cooking book%2E August 2 2013}.";
		String text2 = "The statement was from here ${reference?text=Smith John%2E Cooking book%2E August 2 2013&inlineWidget=true}.";
		MarkdownElements elements = new MarkdownElements(text);
		MarkdownElements elements2 = new MarkdownElements(text2);
		StringBuilder output = new StringBuilder();
		parser.processLine(elements, simpleParsers);
		output.append(elements.getHtml());
		parser.processLine(elements2, simpleParsers);
		output.append(elements2.getHtml());
		String result = output.toString();
		assertTrue(result.contains("${reference?inlineWidget=true&text=Smith John%2E Cooking book%2E August 2 2013&footnoteId=1}."));
		assertTrue(result.contains("${reference?text=Smith John%2E Cooking book%2E August 2 2013&inlineWidget=true&footnoteId=2}."));
		//Check for footnotes at end of document
		assertTrue(result.contains("<p class=\"inlineWidgetContainer\" id=\"wikiReference1\"></p>"));
		assertTrue(result.contains("${reference?inlineWidget=true&text=Smith John%2E Cooking book%2E August 2 2013&footnoteId=1}"));
		assertTrue(result.contains("<p class=\"inlineWidgetContainer\" id=\"wikiReference2\"></p>"));
		assertTrue(result.contains("${reference?text=Smith John%2E Cooking book%2E August 2 2013&inlineWidget=true&footnoteId=2}"));
	}
	
	@Test
	public void testReferenceWithUrl() throws IOException {
		String text = "The statement was from here ${reference?text=So et al%2E %5BYahoo%5D%28http%3A%2F%2Fwww%2Eyahoo%2Ecom%29%2E July 2013&inlineWidget=true}.";
		MarkdownElements elements = new MarkdownElements(text);
		StringBuilder output = new StringBuilder();
		parser.processLine(elements, simpleParsers);
		output.append(elements.getHtml());
		
		elements = new MarkdownElements("");
		parser.processLine(elements, simpleParsers);
		output.append(elements.getHtml());
		parser.completeParse(output);
		
		String result = output.toString();
		//After complete parse, this is footnote section
		assertTrue(result.contains("<hr>")); 
		//See if link regex is detected. Check for container of link.
		assertTrue(result.contains("<div class=\"inline-block\" id=\"link-0\"></div>"));
		
	}
	
}