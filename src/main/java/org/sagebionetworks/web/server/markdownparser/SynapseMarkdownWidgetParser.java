package org.sagebionetworks.web.server.markdownparser;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.sagebionetworks.web.client.widget.entity.SharedMarkdownUtils;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.server.ServerMarkdownUtils;
import org.sagebionetworks.web.shared.WebConstants;

public class SynapseMarkdownWidgetParser extends BasicMarkdownElementParser {
	Pattern p= Pattern.compile(MarkdownRegExConstants.SYNAPSE_MARKDOWN_WIDGET_REGEX, Pattern.CASE_INSENSITIVE);
	String suffix = SharedMarkdownUtils.getPreviewSuffix(isPreview);
	MarkdownExtractor extractor;
	
	@Override
	public void reset() {
		extractor = new MarkdownExtractor();
	}
	
	private String getCurrentDivID() {
		return WebConstants.DIV_ID_WIDGET_SYNTAX_PREFIX + extractor.getCurrentContainerId() + suffix;
	}
	
	private String getNewElementStart() {
		StringBuilder sb = new StringBuilder();
		sb.append(extractor.getContainerElementStart() + getCurrentDivID());
		sb.append("\">");
		return sb.toString();
	}
	
	@Override
	public void processLine(MarkdownElements line,
			List<MarkdownElementParser> simpleParsers) {
		Matcher m = p.matcher(line.getMarkdown());
		StringBuffer sb = new StringBuffer();
		while(m.find()) {	
			String containerElement = getNewElementStart() + extractor.getContainerElementEnd();
			m.appendReplacement(sb, containerElement);
			
			StringBuilder html = new StringBuilder();
			html.append(m.group(2));
			extractor.putContainerIdToContent(getCurrentDivID(), html.toString());
		}
		m.appendTail(sb);
		line.updateMarkdown(sb.toString());
	}
	
	@Override
	public void completeParse(StringBuilder html) {
		String subpagesWidgetMarkdown = SharedMarkdownUtils.getWikiSubpagesMarkdown();
		//If the wikipages was inserted on completeParse, we need to add it to the map of widgets
		if(html.substring(0, subpagesWidgetMarkdown.length()).equals(subpagesWidgetMarkdown)) {
			String containerElement = getNewElementStart() + extractor.getContainerElementEnd();
			html.replace(0, subpagesWidgetMarkdown.length(), "");
			html.insert(0, containerElement);
			
			StringBuilder content = new StringBuilder();
			content.append(WidgetConstants.WIKI_SUBPAGES_CONTENT_TYPE);
			extractor.putContainerIdToContent(getCurrentDivID(), content.toString());
		}
	}
	
	@Override
	public void completeParse(Document doc) {
		Set<String> ids = extractor.getContainerIds();
		//For each widget syntax found, wrap with appropriate widget containers for renderers
		for(String key: ids) {
			boolean inlineWidget = false;
			Element el = doc.getElementById(key);
			Node childNode = (Node) el;
			if(el != null) {
				String content = extractor.getContent(key);
				int syntaxPrefixLen = WebConstants.DIV_ID_WIDGET_SYNTAX_PREFIX.length();
				//Extract just the id + suffix
				String id = key.substring(syntaxPrefixLen);
				String widgetHtml = SharedMarkdownUtils.getWidgetHTML(id, content);
				if(content.contains(WidgetConstants.INLINE_WIDGET_KEY)) {
					inlineWidget = true;
				}
				
				StringBuilder outerContainer = new StringBuilder();
				if(inlineWidget) {
					outerContainer.append("<span></span>");
				} else {
					outerContainer.append("<div></div>");
				}
				
				//Surround with widget holder and appropriate container
				childNode.wrap(outerContainer.toString()).wrap(widgetHtml);
				childNode.remove();
			}
			
		}
	}

}
