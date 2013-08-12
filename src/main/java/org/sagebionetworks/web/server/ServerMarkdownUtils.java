package org.sagebionetworks.web.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.widget.entity.SharedMarkdownUtils;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.server.markdownparser.MarkdownRegExConstants;
import org.sagebionetworks.web.shared.WebConstants;

import eu.henkelmann.actuarius.ActuariusTransformer;

public class ServerMarkdownUtils {
	
	//At the beginning of the line, if there are >=0 whitespace characters, or '>', followed by exactly 3 '`', then it's a match
	public static final String FENCE_CODE_BLOCK_REGEX = "^[> \t\n\f\r]*[`]{3}";
	//At the beginning of the line, if there are >=0 whitespace characters, or '>', followed by ` exactly 4 times, then it's a match
	//for starting code blocks, capture the language parameter by looking for word characters (or a hyphen) one or more times
	public static final String START_CODE_BLOCK_REGEX = "^[> \t\n\f\r]*[`]{4}\\s*([a-zA-Z_0-9-]+)\\s*$";
	public static final String END_CODE_BLOCK_REGEX = "^[> \t\n\f\r]*[`]{4}\\s*$";
	
	public static final String START_PRE_CODE = "<pre><code";
	public static final String END_PRE_CODE = "</code></pre>";
	public static final String HTML_LINE_BREAK = "<br />\n";
	public static final String TEMP_NEWLINE_DELIMITER = "%^&1_9d";
	public static final String TEMP_SPACE_DELIMITER = "%^&2_9d";
	public static final String TEMP_LESS_THAN_DELIMITER = "2%lt%9";
	public static final String TEMP_GREATER_THAN_DELIMITER = "2%gt%9";
	public static final String R_ASSIGNMENT = "<-";
	public static final String R_MESSED_UP_ASSIGNMENT = "< -";
	
	
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
	public static String markdown2Html(String markdown, Boolean isPreview, ActuariusTransformer markdownProcessor) throws IOException {
		String originalMarkdown = markdown;
		if (markdown == null) return SharedMarkdownUtils.getDefaultWikiMarkdown();
		//trick to maintain newlines when suppressing all html
		if (markdown != null) {
			markdown = preserveWhitespace(markdown);
		}
//		lastTime = System.currentTimeMillis();
		//played with other forms of html stripping, 
		//and this method has been the least destructive (compared to clean() with various WhiteLists, or using java HTMLEditorKit to do it).
		markdown = Jsoup.parse(markdown).text();
		markdown = restoreWhitespace(markdown);
		markdown = markdown.replace(R_MESSED_UP_ASSIGNMENT, R_ASSIGNMENT);
//		reportTime("suppress/escape html");
		markdown = resolveHorizontalRules(markdown);
		markdown = resolveTables(markdown);
		markdown = resolveCodeWithLanguage(markdown);
//		reportTime("resolved tables");
		markdown = addSubpagesIfNotPresent(markdown);
		markdown = fixNewLines(markdown);
		markdown = markdownProcessor.apply(markdown);
//		reportTime("markdownToHtml");
		if (markdown == null) {
			//if the markdown processor fails to convert the md to html (will return null in this case), return the raw markdown instead. (as ugly as it might be, it's better than no information).
			return originalMarkdown; 
		}
		//using jsoup, since it's already in this project!
		Document doc = Jsoup.parse(markdown);
//		reportTime("Jsoup parse");
		ServerMarkdownUtils.assignIdsToHeadings(doc);
//		reportTime("Assign IDs to Headings");
		ServerMarkdownUtils.sendAllLinksToNewWindow(doc);
//		reportTime("sendAllLinksToNewWindow");
		Elements anchors = doc.getElementsByTag("a");
		anchors.addClass("link");
		
		Elements tables = doc.getElementsByTag("table");
		tables.addClass("markdowntable");
		
//		reportTime("add link class");
		ServerMarkdownUtils.addWidgets(doc, isPreview);
//		reportTime("addWidgets");
		SynapseAutoLinkDetector.getInstance().createLinks(doc);
		DoiAutoLinkDetector.getInstance().createLinks(doc);
		UrlAutoLinkDetector.getInstance().createLinks(doc);
//		reportTime("addSynapseLinks");
		//URLs are automatically resolved from the markdown processor
		String returnHtml = "<div class=\"markdown\">" + doc.html() + "</div>";
		return returnHtml;
	}
	
	public static String preserveWhitespace(String markdown){
		return markdown.replace("\n", TEMP_NEWLINE_DELIMITER).replace(" ", TEMP_SPACE_DELIMITER);
	}
	
	public static String restoreWhitespace(String markdown){
		return markdown.replace(TEMP_NEWLINE_DELIMITER, "\n").replace(TEMP_SPACE_DELIMITER, " ");
	}
	

	/**
	 * adds a reference to the subpages wiki widget at the top of the page if it isn't already in the markdown
	 * @param markdown
	 * @return
	 */
	public static String addSubpagesIfNotPresent(String markdown) {
		String subpagesMarkdown = SharedMarkdownUtils.getWikiSubpagesMarkdown();
		String newMarkdown = markdown;
		if (!markdown.contains(subpagesMarkdown)) {
			String noAutoWikiSubpages = SharedMarkdownUtils.getNoAutoWikiSubpagesMarkdown();
			if (markdown.contains(noAutoWikiSubpages)) {
				//found.  delete this string, and do not include subpages markdown
				newMarkdown = markdown.replace(noAutoWikiSubpages, "");
			}
			else {
				newMarkdown = subpagesMarkdown + "\n"+ markdown;	
			}
		}
		return newMarkdown;
	}
	
	/**
	 * adds html line breaks to every line, unless it suspects that the line will be in a preformatted code block
	 * @param markdown
	 * @return
	 */
	public static String fixNewLines(String markdown) {
		if (markdown == null || markdown.length() == 0) return markdown;
		String regEx = FENCE_CODE_BLOCK_REGEX;
		Pattern p = Pattern.compile(regEx);
		StringBuilder sb = new StringBuilder();
		boolean isSuspectedCode = false;
		for (String line : markdown.split("\n")) {
			boolean currentLineHasFence = line.contains(START_PRE_CODE) || line.contains(END_PRE_CODE) || p.matcher(line).matches();
			if (currentLineHasFence) {
				//flip
				isSuspectedCode = !isSuspectedCode;
			}
			sb.append(line);
			//add a <br> if we're not in a code block (unless it's the current line that has the ```)
			if (!isSuspectedCode && !currentLineHasFence)
				sb.append(HTML_LINE_BREAK);
			sb.append("\n");
		}
		return sb.toString();
	}
	
//	private static long lastTime;
//	private static void reportTime(String description) {
//		long currentTime = System.currentTimeMillis();
//		System.out.println(description + ": " + (currentTime-lastTime));
//		lastTime = currentTime;
//	}

	private static int getLargestHeadingLevel(Document doc){
		int i = 0; //start at 0
		for (; i < 7; i++) {
			if (!(doc.getElementsByTag("h"+i).isEmpty()))
				break;
		}
		return i;
	}
	
	public static void assignIdsToHeadings(Document doc) {
		//find the biggest heading level
		int largestHeadingLevel = getLargestHeadingLevel(doc);
		Map<String, String> headingLevel2StyleName = new HashMap<String, String>();
		int indentLevel = 0;
		for (int i = largestHeadingLevel; i < 7; i++, indentLevel++) {
			headingLevel2StyleName.put("h" + i, "toc-indent"+indentLevel);
		}
		
		Elements hTags = doc.select("h0, h1, h2, h3, h4, h5, h6");
		int headingIndex = 0;
		for (int i = 0; i < hTags.size(); i++) {
			Element hTag = hTags.get(i);
			boolean skip = false;
			for (Node node : hTag.childNodes()) {
				if (node instanceof TextNode) {
					TextNode textNode = (TextNode) node;
					String text = textNode.getWholeText();
					if (text.startsWith("!")) {
						skip=true;
						textNode.replaceWith(TextNode.createFromEncoded(text.substring(1), textNode.baseUri()));
					}
				}
			}
			if (!skip) {
				hTag.attr("id", WidgetConstants.MARKDOWN_HEADING_ID_PREFIX+headingIndex);
				hTag.attr("level", hTag.tag().getName());
				hTag.attr("toc-style", headingLevel2StyleName.get(hTag.tag().getName()));
				headingIndex++;
			}
		}
	}
	
	public static void sendAllLinksToNewWindow(Document doc) {
		Elements elements = doc.getElementsByTag("a");
		elements.attr("target", "_blank");
	}


	public static void resolveAttachmentImages(Document doc, String attachmentUrl) {
		Elements images = doc.select("img");
		for (Iterator iterator = images.iterator(); iterator.hasNext();) {
			Element img = (Element) iterator.next();
			String src = img.attr("src");
			if (src.startsWith(DisplayConstants.ENTITY_DESCRIPTION_ATTACHMENT_PREFIX)){
		    	String[] tokens = src.split("/");
		    	if (tokens.length > 5) {
			        String entityId = tokens[2];
				    String tokenId = tokens[4] +"/"+ tokens[5];
				    img.attr("src", ServerMarkdownUtils.createAttachmentUrl(attachmentUrl, entityId, tokenId, tokenId,WebConstants.ENTITY_PARAM_KEY));
		    	}
			}
		}
	}

	/**
	 * Create the url to an attachment image.
	 * @param baseURl
	 * @param id
	 * @param tokenId
	 * @param fileName
	 * @return
	 */
	public static String createAttachmentUrl(String baseURl, String id, String tokenId, String fileName, String paramKey){
	        StringBuilder builder = new StringBuilder();
	        builder.append(baseURl);
	        builder.append("?"+paramKey+"=");
	        builder.append(id);
	        builder.append("&"+WebConstants.TOKEN_ID_PARAM_KEY+"=");
	        builder.append(tokenId);
	        builder.append("&"+WebConstants.WAIT_FOR_URL+"=true");
	        return builder.toString();
	}
	
	public static String resolveHorizontalRules(String rawMarkdown) {
		//find all horizontal rules
		//match if we have 3 or more '-' or '*', and it's the only thing on the line
		String regEx1 = MarkdownRegExConstants.HR_REGEX1;
		String regEx2 = MarkdownRegExConstants.HR_REGEX2;
		String[] lines = rawMarkdown.split("\n");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < lines.length; i++) {
			String testLine = lines[i].replaceAll(" ", "");
			boolean isHr = testLine.matches(regEx1) || testLine.matches(regEx2);
			if (isHr) {
				//output hr
				sb.append("<hr>\n");
			} else {
				//just add the line and move on
				sb.append(lines[i] + "\n");
			}
		}
		
		return sb.toString();
	}
	
	
	public static String resolveTables(String rawMarkdown) {
		//find all tables, and replace the raw text with html table
		String regEx = MarkdownRegExConstants.TABLE_REGEX;
		String[] lines = rawMarkdown.split("\n");
		StringBuilder sb = new StringBuilder();
		int tableCount = 0;
		int i = 0;
		while (i < lines.length) {
			boolean looksLikeTable = lines[i].matches(regEx);
			if (looksLikeTable) {
				//create a table, and consume until the regEx stops
				i = appendNewTableHtml(sb, regEx, lines, tableCount, i);
				tableCount++;
			} else {
				//just add the line and move on
				sb.append(lines[i] + "\n");
				i++;
			}
		}
		
		return sb.toString();
	}
	
	public static String resolveCodeWithLanguage(String markdown) {
		if (markdown == null || markdown.length() == 0) return markdown;
		String startCodeBlockRegex = START_CODE_BLOCK_REGEX;
		String endCodeBlockRegex = END_CODE_BLOCK_REGEX;
		Pattern p1 = Pattern.compile(startCodeBlockRegex);
		Pattern p2 = Pattern.compile(endCodeBlockRegex);
		StringBuilder sb = new StringBuilder();
		for (String line : markdown.split("\n")) {
			Matcher p1Matcher = p1.matcher(line);
			Matcher p2Matcher = p2.matcher(line);
			if (p1Matcher.matches() && p1Matcher.groupCount() == 1) {
				//start pre code with the specified language
				String language = p1Matcher.group(1);
				sb.append(START_PRE_CODE + " class=\""+language.toLowerCase()+"\">");
				
			} else if (p2Matcher.matches()){
				//end pre code
				sb.append(END_PRE_CODE);
			} else {
				//else neither
				sb.append(line);	
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	
	public static int appendNewTableHtml(StringBuilder builder, String regEx, String[] lines, int tableCount, int i) {
		builder.append("<table id=\""+WidgetConstants.MARKDOWN_TABLE_ID_PREFIX+tableCount+"\" class=\"tablesorter\">");
		//header
		builder.append("<thead>");
		builder.append("<tr>");
		String[] cells = lines[i].split("\\|");
		for (int j = 0; j < cells.length; j++) {
			builder.append("<th>");
			builder.append(cells[j]);
			builder.append("</th>");
		}
		builder.append("</tr>");
		builder.append("</thead>");
		builder.append("<tbody>");
		i++;
		while (i < lines.length && lines[i].matches(regEx)) {
			builder.append("<tr>");
			cells = lines[i].split("\\|");
			for (int j = 0; j < cells.length; j++) {
				builder.append("<td>");
				builder.append(cells[j]);
				builder.append("</td>");
			}
			builder.append("</tr>");
			i++;
		}
		builder.append("</tbody>");
		builder.append("</table>");
		
		return i;
	}
	
	public static void addWidgets(Document doc, Boolean isPreview) {
		String suffix = isPreview ? WebConstants.DIV_ID_PREVIEW_SUFFIX : "";
		// using a regular expression to find our special widget notation, replace with a div with the widget name
		String regEx = "\\W*?("+WidgetConstants.WIDGET_START_MARKDOWN_ESCAPED+"([^\\}]*)\\})\\W*?"; //reluctant qualification so that it finds multiple per line
		Elements elements = doc.select("*:matchesOwn(" + regEx + ")");  	// selector is case insensitive
		Pattern pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
		int widgetsFound = 0;
		for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
			Element element = (Element) iterator.next();
			//only process the TextNode children (ignore others)
			for (Iterator iterator2 = element.childNodes().iterator(); iterator2.hasNext();) {
				Node childNode = (Node) iterator2.next();
				if (childNode instanceof TextNode) {
					String oldText = ((TextNode) childNode).text();
					// find it in the text
					Matcher matcher = pattern.matcher(oldText);
					StringBuilder sb = new StringBuilder();
					int previousFoundIndex = 0;
					boolean childFound = false;
					boolean inlineWidget = false;
					while (matcher.find()) {
						childFound = true;
						if (matcher.groupCount() == 2) {
							sb.append(oldText.substring(previousFoundIndex, matcher.start()));
							sb.append(SharedMarkdownUtils.getWidgetHTML(widgetsFound, suffix, matcher.group(2)));
							if(matcher.group(2).contains(WidgetConstants.INLINE_WIDGET_KEY)) {
								inlineWidget = true;
							}
							widgetsFound++;
							previousFoundIndex = matcher.end(1);
						}
					}
					if (childFound) {
						if (previousFoundIndex <= oldText.length() - 1)
							// substring, go from the previously found index to the end
							sb.append(oldText.substring(previousFoundIndex));
						//wrap new html in an appropriate tag, since it needs a container!
						Element newElement;
						if(inlineWidget) {
							newElement = doc.createElement("span");
						} else {
							newElement = doc.createElement("div"); 					
						}
						newElement.html(sb.toString());
						childNode.replaceWith(newElement);
					}
				}
			}
		}
	}

	public static String getSynAnchorHtml(String synId){
		return "<a class=\"link\" href=\"#!Synapse:" + synId +"\">" + synId + "</a>";
	}
	
	public static String getDoiLink(String fullDoi, String doiName){
		return "<a target=\"_blank\" class=\"link\" href=\"http://dx.doi.org/" +
				doiName + "\">" + fullDoi +"</a>";
	}
	
	public static String getUrlHtml(String url){
		return "<a target=\"_blank\" class=\"link\" href=\"" + url.trim() + "\">" + url+ "</a>";
	}
}
