package org.sagebionetworks.web.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.server.markdownparser.MarkdownExtractor;
import org.sagebionetworks.web.server.markdownparser.TableParser;
import org.sagebionetworks.web.shared.WebConstants;

public class ServerMarkdownUtils {
	public static final String START_PRE_CODE = "<pre><code";
	public static final String END_PRE_CODE = "</code></pre>";
	
	public static final String START_CONTAINER = "<span id=\"";
	public static final String END_CONTAINER = "</span>";
	
	public static final String START_LINK_NEW_WINDOW = "<a class=\"link\" target=\"_blank\" href=\"";
	public static final String START_LINK_CURRENT_WINDOW = "<a class=\"link\" href=\"";
	
	public static final String END_LINK = "</a>";
	
	public static final String START_BLOCKQUOTE_TAG = "<blockquote>";
	public static final String HTML_LINE_BREAK = "<br />\n";
	public static final String TEMP_NEWLINE_DELIMITER = "%^&1_9d";
	public static final String TEMP_SPACE_DELIMITER = "%^&2_9d";
	public static final String TEMP_LESS_THAN_DELIMITER = "2%lt%9";
	public static final String TEMP_GREATER_THAN_DELIMITER = "2%gt%9";
	public static final String R_ASSIGNMENT = "<-";
	public static final String R_MESSED_UP_ASSIGNMENT = "< -";
	
	public static String getStartLink(String clientHostString, String href) {
		//default is to stay in the current window
		if (clientHostString == null || clientHostString.length() == 0 || href == null || href.length() == 0)
			return START_LINK_CURRENT_WINDOW;
		return href.toLowerCase().startsWith(clientHostString) ?
				START_LINK_CURRENT_WINDOW : 
				START_LINK_NEW_WINDOW;
	}
	
	/**
	 * Retrieves each container specified by saved ids and inserts the associated contents into the container
	 * @param extractor
	 * @param doc
	 */
	public static void insertExtractedContentToMarkdown(MarkdownExtractor extractor, Document doc, boolean hasHtml) {
		Set<String> foundKeys = new HashSet<String>();
		for(String key: extractor.getContainerIds()) {
			Element el = doc.getElementById(key);
			if(el != null) {
				if(hasHtml) {
					el.prepend(extractor.getContent(key));
				} else {
					el.appendText(extractor.getContent(key));
				}
				foundKeys.add(key);
			}
		}
		//clean up the container Ids that we resolved
		extractor.removeContainerIds(foundKeys);
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
	
	
	
	
	public static int appendNewTableHtml(StringBuilder builder, String regEx, String[] lines, int tableCount, int i) {
		builder.append(TableParser.TABLE_START_HTML+WidgetConstants.MARKDOWN_TABLE_ID_PREFIX+tableCount+"\" class=\"tablesorter\">");
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
		builder.append(TableParser.TABLE_END_HTML);
		
		return i;
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

	public static final String DEFAULT_CODE_CSS_CLASS = "no-highlight";
}
