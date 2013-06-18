package org.sagebionetworks.web.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.server.markdownparser.CodeParser;
import org.sagebionetworks.web.server.markdownparser.HorizontalLineParser;
import org.sagebionetworks.web.server.markdownparser.MarkdownElementParser;
import org.sagebionetworks.web.server.markdownparser.TableParser;
import org.sagebionetworks.web.server.markdownparser.WikiSubpageParser;

public class AlphaServerMarkdownProcessor {
	private static AlphaServerMarkdownProcessor singleton = null;
	private List<MarkdownElementParser> elementParsers = new ArrayList<MarkdownElementParser>();
	
	private AlphaServerMarkdownProcessor() {
	}
	
	public static AlphaServerMarkdownProcessor getInstance() {
		if (singleton == null) {
			singleton = new AlphaServerMarkdownProcessor();
			singleton.init();
		}
		return singleton;
	}
	
	public void init() {
		//initialize all markdown element parsers
		elementParsers.add(new HorizontalLineParser());
		elementParsers.add(new TableParser());
		elementParsers.add(new CodeParser());
		elementParsers.add(new WikiSubpageParser());
		
		for (MarkdownElementParser parser : elementParsers) {
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
		for (MarkdownElementParser parser : elementParsers) {
			parser.reset();
		}
		//go through the document once, and apply all markdown parsers to it
		StringBuilder output = new StringBuilder();
		for (String line : markdown.split("\n")) {
			for (MarkdownElementParser parser : elementParsers) {
				line = parser.processLine(line);
			}
			output.append(line);
		}
		for (MarkdownElementParser parser : elementParsers) {
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
