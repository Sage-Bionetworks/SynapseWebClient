package org.sagebionetworks.web.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
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
	
	private SynapseMarkdownProcessor() {
	}
	
	public static SynapseMarkdownProcessor getInstance() {
		if (singleton == null) {
			singleton = new SynapseMarkdownProcessor();
			singleton.init();
		}
		return singleton;
	}
	
	public void init() {
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
		
		for (MarkdownElementParser parser : allElementParsers) {
			parser.init();
		}
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
//		lastTime = System.currentTimeMillis();
		//played with other forms of html stripping, 
		//and this method has been the least destructive (compared to clean() with various WhiteLists, or using java HTMLEditorKit to do it).
		markdown = Jsoup.parse(markdown).text();
		markdown = ServerMarkdownUtils.restoreWhitespace(markdown);
		
		//now make the main single pass to identify markdown elements and create the output
		markdown = StringUtils.replace(markdown, ServerMarkdownUtils.R_MESSED_UP_ASSIGNMENT, ServerMarkdownUtils.R_ASSIGNMENT);
//		reportTime("suppress/escape html");
		String html = processMarkdown(markdown);
//		reportTime("markdownToHtml");
		if (html == null) {
			//if the markdown processor fails to convert the md to html (will return null in this case), return the raw markdown instead. (as ugly as it might be, it's better than no information).
			return originalMarkdown; 
		}
		
//		reportTime("addSynapseLinks");
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
		//these are the processors that report they are in the middle of the element
		List<MarkdownElementParser> currentlyProcessingElementProcessors = new ArrayList<MarkdownElementParser>();
		//other element processors
		List<MarkdownElementParser> otherElementProcessors = new ArrayList<MarkdownElementParser>();
		//initialize all processors in the "other" list
		otherElementProcessors.addAll(allElementParsers);
		
		for (String line : markdown.split("\n")) {
			//first do parsers we're currently "in"
			
			for (MarkdownElementParser parser : currentlyProcessingElementProcessors) {
				line = parser.processLine(line);
			}
			
			//then the rest
			for (MarkdownElementParser parser : otherElementProcessors) {
				line = parser.processLine(line);
			}
			
			List<MarkdownElementParser> newCurrentlyProcessingElementProcessors = new ArrayList<MarkdownElementParser>();
			List<MarkdownElementParser> newOtherElementProcessors = new ArrayList<MarkdownElementParser>();
			//add all from the still processing list (maintain order)
			for (MarkdownElementParser parser : currentlyProcessingElementProcessors) {
				if (parser.isInMarkdownElement())
					newCurrentlyProcessingElementProcessors.add(parser);
				else
					newOtherElementProcessors.add(parser);
			}
			
			//process the rest
			for (MarkdownElementParser parser : otherElementProcessors) {
				if (parser.isInMarkdownElement()) //add to the front (reverse their order so that they can have the opportunity to be well formed)
					newCurrentlyProcessingElementProcessors.add(0, parser);
				else
					newOtherElementProcessors.add(parser);
			}
			
			currentlyProcessingElementProcessors = newCurrentlyProcessingElementProcessors;
			otherElementProcessors = newOtherElementProcessors;
			
			output.append(line);
			//also tack on a <br />, unless we are preformatted
			boolean isPreformatted = false;
			for (MarkdownElementParser parser : allElementParsers) {
				if (parser.isInMarkdownElement() && parser.isBlockElement()) {
					isPreformatted = true;
					break;
				}
			}
			output.append(isPreformatted ? "\n" : ServerMarkdownUtils.HTML_LINE_BREAK);
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
	//				reportTime("Jsoup parse");
		ServerMarkdownUtils.assignIdsToHeadings(doc);
	//				reportTime("Assign IDs to Headings");
		ServerMarkdownUtils.sendAllLinksToNewWindow(doc);
	//				reportTime("sendAllLinksToNewWindow");
		Elements anchors = doc.getElementsByTag("a");
		anchors.addClass("link");
		
		Elements tables = doc.getElementsByTag("table");
		tables.addClass("markdowntable");
		
	//				reportTime("add link class");
		ServerMarkdownUtils.addWidgets(doc, isPreview);
	//				reportTime("addWidgets");
		SynapseAutoLinkDetector.getInstance().createLinks(doc);
		DoiAutoLinkDetector.getInstance().createLinks(doc);
		UrlAutoLinkDetector.getInstance().createLinks(doc);
		return doc.html();
	}
}
