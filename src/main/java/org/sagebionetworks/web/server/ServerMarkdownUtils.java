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
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;

import eu.henkelmann.actuarius.ActuariusTransformer;

public class ServerMarkdownUtils {
	
	private static final String NEWLINE_WITH_SPACES = "  \n";
	private static final String TEMP_NEWLINE_DELIMITER = "%^&1_9d";
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
		if (markdown == null) return "";
		//trick to maintain newlines when suppressing all html
		if (markdown != null) {
			markdown = markdown.replace("\n", TEMP_NEWLINE_DELIMITER);
		}
//		lastTime = System.currentTimeMillis();
		markdown = Jsoup.parse(markdown).text();
		markdown = markdown.replace(TEMP_NEWLINE_DELIMITER, NEWLINE_WITH_SPACES);
//		reportTime("suppress/escape html");
		markdown = resolveTables(markdown);
//		reportTime("resolved tables");
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
//		reportTime("add link class");
		ServerMarkdownUtils.addWidgets(doc, isPreview);
//		reportTime("addWidgets");
		ServerMarkdownUtils.addSynapseLinks(doc);
//		reportTime("addSynapseLinks");
		//URLs are automatically resolved from the markdown processor
		String returnHtml = "<div class=\"markdown\">" + doc.html() + "</div>";
		return returnHtml;
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
				    img.attr("src", createAttachmentUrl(attachmentUrl, entityId, tokenId, tokenId,DisplayUtils.ENTITY_PARAM_KEY));
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
	        builder.append("&"+DisplayUtils.TOKEN_ID_PARAM_KEY+"=");
	        builder.append(tokenId);
	        builder.append("&"+DisplayUtils.WAIT_FOR_URL+"=true");
	        return builder.toString();
	}

	public static void addSynapseLinks(Document doc) {
		// in this case, I still need a regular expression to find the synapse ids.
		// find all elements whose text contains a synapse id pattern (but not anchors)
		// replace the TextNode element children with Elements, whose html contain a link to relevant synapse entity.
		// regular expression: look for non-word characters (0 or more), followed by "syn" and a number, followed by more non-word characters (0 or more).
		// capture the synapse id in a group (the paranthesis).
		String regEx = "\\W*(syn\\d+)\\W*";
		Elements elements = doc.select("*:matchesOwn(" + regEx + "):not(a,code)");  	// selector is case insensitive
		Pattern pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
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
					while (matcher.find() && matcher.groupCount() == 1) {
						sb.append(oldText.substring(previousFoundIndex, matcher.start(1)));
						sb.append(ServerMarkdownUtils.getSynAnchorHtml(matcher.group(1))); //the actual synapse Id group (not the non-word characters that might surround it)
						previousFoundIndex = matcher.end(1);
					}
					if (previousFoundIndex < oldText.length() - 1)
						// substring, go from the previously found index to the end
						sb.append(oldText.substring(previousFoundIndex));
					Element newElement = doc.createElement("span"); //wrap new html in a span, since it needs a container!
					newElement.html(sb.toString());
					childNode.replaceWith(newElement);		
				}
			}
		}
	}
	
	public static String resolveTables(String rawMarkdown) {
		//find all tables, and replace the raw text with html table
		String regEx = ".*[|]{1}.+[|]{1}.*";
		String[] lines = rawMarkdown.split(NEWLINE_WITH_SPACES);
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
				sb.append(lines[i] + NEWLINE_WITH_SPACES);
				i++;
			}
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
		String suffix = isPreview ? DisplayConstants.DIV_ID_PREVIEW_SUFFIX : "";
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
					while (matcher.find()) {
						if (matcher.groupCount() == 2) {
							sb.append(oldText.substring(previousFoundIndex, matcher.start()));
							sb.append(ServerMarkdownUtils.getWidgetHTML(widgetsFound, suffix, matcher.group(2)));
							widgetsFound++;
							previousFoundIndex = matcher.end(1);
						}
					}
					if (previousFoundIndex < oldText.length() - 1)
						// substring, go from the previously found index to the end
						sb.append(oldText.substring(previousFoundIndex));
					Element newElement = doc.createElement("div"); //wrap new html in a span, since it needs a container!
					newElement.html(sb.toString());
					childNode.replaceWith(newElement);
				}
			}
		}
	}

	public static String getUrlHtml(String url){
		StringBuilder sb = new StringBuilder();
		sb.append("<a target=\"_blank\" class=\"link auto-detected-url\" href=\"");
	    sb.append(url.trim());
	    sb.append("\">");
	    sb.append(url);
	    sb.append("</a>");
	    return sb.toString();
	}

	public static String getSynAnchorHtml(String synId){
		StringBuilder sb = new StringBuilder();
		sb.append("<a target=\"_blank\" class=\"link auto-detected-synapse-link\" href=\"#!Synapse:");
	    sb.append(synId.toLowerCase().trim());
	    sb.append("\">");
	    sb.append(synId);
	    sb.append("</a>");
	    return sb.toString();
	}

	public static String getYouTubeHTML(String videoId){
		
		StringBuilder sb = new StringBuilder();
		sb.append("<iframe width=\"560\" height=\"315\" src=\"http://www.youtube.com/embed/");
		sb.append(videoId);
		sb.append("\" frameborder=\"0\" allowfullscreen></iframe>");
	    return sb.toString();
	}
	
	public static String getWidgetHTML(int widgetIndex, String suffix, String widgetProperties){
		StringBuilder sb = new StringBuilder();
		sb.append("<div id=\"");
		sb.append(DisplayConstants.DIV_ID_WIDGET_PREFIX);
		sb.append(widgetIndex);
		sb.append(suffix);
		sb.append("\" widgetParams=\"");
		sb.append(widgetProperties);
		sb.append("\">");
		sb.append("</div>");
	    return sb.toString();
	}

}
