package org.sagebionetworks.web.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.server.markdownparser.BlockQuoteParser;
import org.sagebionetworks.web.server.markdownparser.BoldParser;
import org.sagebionetworks.web.server.markdownparser.CodeParser;
import org.sagebionetworks.web.server.markdownparser.CodeSpanParser;
import org.sagebionetworks.web.server.markdownparser.HeadingParser;
import org.sagebionetworks.web.server.markdownparser.HorizontalLineParser;
import org.sagebionetworks.web.server.markdownparser.ImageParser;
import org.sagebionetworks.web.server.markdownparser.ItalicsParser;
import org.sagebionetworks.web.server.markdownparser.LinkParser;
import org.sagebionetworks.web.server.markdownparser.ListParser;
import org.sagebionetworks.web.server.markdownparser.MarkdownElementParser;
import org.sagebionetworks.web.server.markdownparser.TableParser;
import org.sagebionetworks.web.server.markdownparser.WikiSubpageParser;

public class SynapseMarkdownProcessor {
	private static SynapseMarkdownProcessor singleton = null;
	private List<MarkdownElementParser> allElementParsers = new ArrayList<MarkdownElementParser>();

	public static SynapseMarkdownProcessor getInstance() {
		if (singleton == null) {
			singleton = new SynapseMarkdownProcessor();
		}
		return singleton;
	}

	
	private SynapseMarkdownProcessor() {
		init();
	}
	
	private void init() {
		//initialize all markdown element parsers
		allElementParsers.add(new HorizontalLineParser());
		allElementParsers.add(new TableParser());
		allElementParsers.add(new CodeParser());
		allElementParsers.add(new CodeSpanParser());
		allElementParsers.add(new BoldParser());
		allElementParsers.add(new BlockQuoteParser());
		allElementParsers.add(new HeadingParser());
		allElementParsers.add(new ImageParser());
		allElementParsers.add(new ItalicsParser());
		allElementParsers.add(new LinkParser());
		allElementParsers.add(new ListParser());
		allElementParsers.add(new WikiSubpageParser());
	}
	
	/**
	 * This converts the given markdown to html using the given markdown processor.
	 * It also post processes the output html, including:
	 * *sending all links to a new window.
	 * *applying the markdown css classname to entities supported by the markdown.
	 * *auto detects Synapse IDs (and creates links out of them)
	 * *auto detects generic urls (and creates links out of them)
	 * *resolve Widgets!
	 * @param panel
	 * @throws IOException 
	 */
	public String markdown2Html(String markdown, Boolean isPreview) throws IOException {
		String originalMarkdown = markdown;
		if (markdown == null) return DisplayUtils.getDefaultWikiMarkdown();
		//trick to maintain newlines when suppressing all html
		if (markdown != null) {
			markdown = ServerMarkdownUtils.preserveWhitespace(markdown);
		}
		//played with other forms of html stripping, 
		//and this method has been the least destructive (compared to clean() with various WhiteLists, or using java HTMLEditorKit to do it).
		markdown = Jsoup.parse(markdown).text();
		markdown = ServerMarkdownUtils.restoreWhitespace(markdown);
		
		//now make the main single pass to identify markdown elements and create the output
		markdown = StringUtils.replace(markdown, ServerMarkdownUtils.R_MESSED_UP_ASSIGNMENT, ServerMarkdownUtils.R_ASSIGNMENT);
		String html = processMarkdown(markdown);
		if (html == null) {
			//if the markdown processor fails to convert the md to html (will return null in this case), return the raw markdown instead. (as ugly as it might be, it's better than no information).
			return originalMarkdown; 
		}
		//URLs are automatically resolved from the markdown processor
		html = "<div class=\"markdown\">" + postProcessHtml(html, isPreview) + "</div>";
		return html;
	}
	
	public String processMarkdown(String markdown) {
		//first, reset all of the parsers
		for (MarkdownElementParser parser : allElementParsers) {
			parser.reset();
		}
		//go through the document once, and apply all markdown parsers to it
		StringBuilder output = new StringBuilder();
		
		//these are the parsers that only take a single line as input (element does not span across lines)
		List<MarkdownElementParser> simpleParsers = new ArrayList<MarkdownElementParser>();
		
		//these are the processors that report they are in the middle of a multiline element
		List<MarkdownElementParser> activeComplexParsers = new ArrayList<MarkdownElementParser>();
		//the rest of the multiline processors not currently in the middle of an element
		List<MarkdownElementParser> inactiveComplexParsers = new ArrayList<MarkdownElementParser>();
		
		//initialize all processors either in the simple list, or in the inactive list
		for (MarkdownElementParser parser : allElementParsers) {
			if (parser.isInputSingleLine())
				simpleParsers.add(parser);
			else
				inactiveComplexParsers.add(parser);
		}
		
		List<String> allLines = new ArrayList<String>();
		for (String line : markdown.split("\n")) {
			allLines.add(line);
		}
		allLines.add("");
		for (String line : allLines) {
			//process the simple processors first
			for (MarkdownElementParser parser : simpleParsers) {
				line = parser.processLine(line);
			}
			
			//then do parsers we're currently in the middle of
			for (MarkdownElementParser parser : activeComplexParsers) {
				line = parser.processLine(line);
			}
			
			//then the inactive multiline parsers
			for (MarkdownElementParser parser : inactiveComplexParsers) {
				line = parser.processLine(line);
			}
			
			List<MarkdownElementParser> newActiveComplexParsers = new ArrayList<MarkdownElementParser>();
			List<MarkdownElementParser> newInactiveComplexParsers = new ArrayList<MarkdownElementParser>();
			//add all from the still processing list (maintain order)
			for (MarkdownElementParser parser : activeComplexParsers) {
				if (parser.isInMarkdownElement())
					newActiveComplexParsers.add(parser);
				else
					newInactiveComplexParsers.add(parser);
			}
			
			//sort the rest
			for (MarkdownElementParser parser : inactiveComplexParsers) {
				if (parser.isInMarkdownElement()) //add to the front (reverse their order so that they can have the opportunity to be well formed)
					newActiveComplexParsers.add(0, parser);
				else
					newInactiveComplexParsers.add(parser);
			}
			
			activeComplexParsers = newActiveComplexParsers;
			inactiveComplexParsers = newInactiveComplexParsers;
			
			output.append(line);
			//also tack on a <br />, unless we are a block element (those parsers handle their own newlines
			boolean isInMiddleOfBlockElement = false;
			for (MarkdownElementParser parser : allElementParsers) {
				if (parser.isInMarkdownElement() && parser.isBlockElement()) {
					isInMiddleOfBlockElement = true;
					break;
				}
			}
			if (!isInMiddleOfBlockElement)
				output.append(ServerMarkdownUtils.HTML_LINE_BREAK);
		}
		
		for (MarkdownElementParser parser : allElementParsers) {
			parser.completeParse(output);
		}
		return output.toString();
	}
	
	/**
	 * After markdown is converted into html, postprocess that html
	 * @param markdown
	 * @return
	 */
	public String postProcessHtml(String html, boolean isPreview) {
		//using jsoup, since it's already in this project!
		Document doc = Jsoup.parse(html);
		ServerMarkdownUtils.assignIdsToHeadings(doc);
		
		ServerMarkdownUtils.addWidgets(doc, isPreview);
		SynapseAutoLinkDetector.getInstance().createLinks(doc);
		DoiAutoLinkDetector.getInstance().createLinks(doc);
		UrlAutoLinkDetector.getInstance().createLinks(doc);
		return doc.html();
	}
}
