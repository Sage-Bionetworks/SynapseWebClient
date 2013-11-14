package org.sagebionetworks.web.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.sagebionetworks.web.client.widget.entity.SharedMarkdownUtils;
import org.sagebionetworks.web.server.markdownparser.BacktickParser;
import org.sagebionetworks.web.server.markdownparser.BlockQuoteParser;
import org.sagebionetworks.web.server.markdownparser.BoldParser;
import org.sagebionetworks.web.server.markdownparser.BookmarkTargetParser;
import org.sagebionetworks.web.server.markdownparser.CodeParser;
import org.sagebionetworks.web.server.markdownparser.CodeSpanParser;
import org.sagebionetworks.web.server.markdownparser.DoiAutoLinkParser;
import org.sagebionetworks.web.server.markdownparser.HeadingParser;
import org.sagebionetworks.web.server.markdownparser.HorizontalLineParser;
import org.sagebionetworks.web.server.markdownparser.ImageParser;
import org.sagebionetworks.web.server.markdownparser.ItalicsParser;
import org.sagebionetworks.web.server.markdownparser.LinkParser;
import org.sagebionetworks.web.server.markdownparser.ListParser;
import org.sagebionetworks.web.server.markdownparser.MarkdownElementParser;
import org.sagebionetworks.web.server.markdownparser.MarkdownElements;
import org.sagebionetworks.web.server.markdownparser.MarkdownRegExConstants;
import org.sagebionetworks.web.server.markdownparser.MathParser;
import org.sagebionetworks.web.server.markdownparser.ReferenceParser;
import org.sagebionetworks.web.server.markdownparser.StrikeoutParser;
import org.sagebionetworks.web.server.markdownparser.SubscriptParser;
import org.sagebionetworks.web.server.markdownparser.SuperscriptParser;
import org.sagebionetworks.web.server.markdownparser.SynapseAutoLinkParser;
import org.sagebionetworks.web.server.markdownparser.SynapseMarkdownWidgetParser;
import org.sagebionetworks.web.server.markdownparser.TableParser;
import org.sagebionetworks.web.server.markdownparser.TildeParser;
import org.sagebionetworks.web.server.markdownparser.UnderscoreParser;
import org.sagebionetworks.web.server.markdownparser.UrlAutoLinkParser;

public class SynapseMarkdownProcessor {
	private static SynapseMarkdownProcessor singleton = null;
	private List<MarkdownElementParser> allElementParsers = new ArrayList<MarkdownElementParser>();
	
	//efficient hack to preserve strings that the html stripping process ruins
	private Map<Pattern, String> preservers = new HashMap<Pattern, String>();
	private Map<Pattern, String> restorers = new HashMap<Pattern, String>();
	
	private CodeParser codeParser;
	private MathParser mathParser;
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
		//protect widget syntax
		allElementParsers.add(new ReferenceParser());
		allElementParsers.add(new BookmarkTargetParser());
		allElementParsers.add(new SynapseMarkdownWidgetParser());
		
		//parsers that handle escaping
		allElementParsers.add(new TildeParser());
		allElementParsers.add(new UnderscoreParser());
		allElementParsers.add(new BacktickParser());
		//other parsers should not affect code spans
		allElementParsers.add(new CodeSpanParser());
		//parsers protecting urls go before other simple parsers
		allElementParsers.add(new ImageParser());
		allElementParsers.add(new LinkParser());
		allElementParsers.add(new UrlAutoLinkParser());
		
		//initialize other markdown element parsers
		allElementParsers.add(new BlockQuoteParser());
		allElementParsers.add(new BoldParser());	
		codeParser = new CodeParser();
		allElementParsers.add(codeParser);
		mathParser = new MathParser();
		allElementParsers.add(mathParser);
		allElementParsers.add(new DoiAutoLinkParser());
		allElementParsers.add(new HeadingParser());
		allElementParsers.add(new HorizontalLineParser());
		allElementParsers.add(new ItalicsParser());
		allElementParsers.add(new ListParser());
		allElementParsers.add(new StrikeoutParser());
		allElementParsers.add(new SubscriptParser());
		allElementParsers.add(new SuperscriptParser());
		allElementParsers.add(new SynapseAutoLinkParser());
		allElementParsers.add(new TableParser());
		
		//preservers
		preservers.put(Pattern.compile(MarkdownRegExConstants.NEWLINE_REGEX), ServerMarkdownUtils.TEMP_NEWLINE_DELIMITER);
		preservers.put(Pattern.compile(MarkdownRegExConstants.SPACE_REGEX), ServerMarkdownUtils.TEMP_SPACE_DELIMITER);
		preservers.put(Pattern.compile(MarkdownRegExConstants.LT_REGEX), ServerMarkdownUtils.TEMP_LESS_THAN_DELIMITER);
		preservers.put(Pattern.compile(MarkdownRegExConstants.GT_REGEX), ServerMarkdownUtils.TEMP_GREATER_THAN_DELIMITER);
		
		//restorers
		restorers.put(Pattern.compile("("+Pattern.quote(ServerMarkdownUtils.TEMP_NEWLINE_DELIMITER)+")"), "\n");
		restorers.put(Pattern.compile("("+Pattern.quote(ServerMarkdownUtils.TEMP_SPACE_DELIMITER)+")"), " ");
		restorers.put(Pattern.compile("("+Pattern.quote(ServerMarkdownUtils.TEMP_LESS_THAN_DELIMITER)+")"), "&lt;");
		restorers.put(Pattern.compile("("+Pattern.quote(ServerMarkdownUtils.TEMP_GREATER_THAN_DELIMITER)+")"), "&gt;");
	}
	
	private String applyPatternReplacements(String markdown, Map<Pattern, String> pattern2Replacement) {
		String returnMarkdown = markdown;
		for (Pattern p : pattern2Replacement.keySet()) {
			Matcher m = p.matcher(returnMarkdown);
			returnMarkdown = m.replaceAll(pattern2Replacement.get(p));
		}
		return returnMarkdown;
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
	public String markdown2Html(String markdown, Boolean isPreview, String clientHostString) throws IOException {
		String originalMarkdown = markdown;
		if (markdown == null) return "";
		
		//trick to maintain newlines when suppressing all html
		if (markdown != null) {
			markdown = applyPatternReplacements(markdown, preservers);
		}
		//played with other forms of html stripping, 
		//and this method has been the least destructive (compared to clean() with various WhiteLists, or using java HTMLEditorKit to do it).
		markdown = Jsoup.parse(markdown).text();
		markdown = applyPatternReplacements(markdown, restorers);

		//now make the main single pass to identify markdown elements and create the output
		markdown = StringUtils.replace(markdown, ServerMarkdownUtils.R_MESSED_UP_ASSIGNMENT, ServerMarkdownUtils.R_ASSIGNMENT);

		String html = processMarkdown(markdown, allElementParsers, isPreview, clientHostString);
		if (html == null) {
			//if the markdown processor fails to convert the md to html (will return null in this case), return the raw markdown instead. (as ugly as it might be, it's better than no information).
			return originalMarkdown; 
		}
		//URLs are automatically resolved from the markdown processor
		html = "<div class=\"markdown\">" + postProcessHtml(html, isPreview) + "</div>";
		
		return html;
	}
	
	public String processMarkdown(String markdown, List<MarkdownElementParser> parsers, boolean isPreview, String clientHostString) {
		//go through the document once, and apply all markdown parsers to it
		StringBuilder output = new StringBuilder();
		
		//these are the parsers that only take a single line as input (element does not span across lines)
		List<MarkdownElementParser> simpleParsers = new ArrayList<MarkdownElementParser>();
		
		//these are the processors that report they are in the middle of a multiline element
		List<MarkdownElementParser> activeComplexParsers = new ArrayList<MarkdownElementParser>();
		//the rest of the multiline processors not currently in the middle of an element
		List<MarkdownElementParser> inactiveComplexParsers = new ArrayList<MarkdownElementParser>();
		
		//initialize all processors either in the simple list, or in the inactive list
		for (MarkdownElementParser parser : parsers) {
			if (parser.isInputSingleLine())
				simpleParsers.add(parser);
			else
				inactiveComplexParsers.add(parser);
		}
		
		//reset all of the parsers
		String lowerClientHostString = clientHostString == null ? "" : clientHostString.toLowerCase();
		for (MarkdownElementParser parser : parsers) {
			parser.reset(simpleParsers);
			parser.setIsPreview(isPreview);
			parser.setClientHostString(lowerClientHostString);
		}
		
		List<String> allLines = new ArrayList<String>();
		for (String line : markdown.split("\n")) {
			allLines.add(line);
		}
		allLines.add("");
		for (String line : allLines) {
			MarkdownElements elements = new MarkdownElements(line);
			//do parsers we're currently in the middle of
			for (MarkdownElementParser parser : activeComplexParsers) {
				parser.processLine(elements);
			}
			
			//only give the option to start new multiline element (complex parser) or process simple elements if we're not in a code block (or a math block)
			if (!codeParser.isInMarkdownElement() && !mathParser.isInMarkdownElement()){
				//then the inactive multiline parsers
				for (MarkdownElementParser parser : inactiveComplexParsers) {
					parser.processLine(elements);
				}
				
				//process the simple processors after complex parsers (the complex parsers clean up the markdown)
				for (MarkdownElementParser parser : simpleParsers) {
					parser.processLine(elements);
				}
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
			
			output.append(elements.getHtml());
			//also tack on a <br />, unless we are a block element (those parsers handle their own newlines
			boolean isInMiddleOfBlockElement = false;
			for (MarkdownElementParser parser : parsers) {
				if (parser.isInMarkdownElement() && parser.isBlockElement()) {
					isInMiddleOfBlockElement = true;
					break;
				}
			}
			if (!isInMiddleOfBlockElement)
				output.append(ServerMarkdownUtils.HTML_LINE_BREAK);
		}
		
		for (MarkdownElementParser parser : parsers) {
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
		for (MarkdownElementParser parser : allElementParsers) {
			parser.completeParse(doc);
		}
		ServerMarkdownUtils.assignIdsToHeadings(doc);
		doc.outputSettings().prettyPrint(false);
		return doc.html();
	}
}
