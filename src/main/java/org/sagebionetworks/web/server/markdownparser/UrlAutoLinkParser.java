package org.sagebionetworks.web.server.markdownparser;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.sagebionetworks.web.client.widget.entity.SharedMarkdownUtils;
import org.sagebionetworks.web.server.ServerMarkdownUtils;
import org.sagebionetworks.web.shared.WebConstants;

public class UrlAutoLinkParser extends BasicMarkdownElementParser {
	Pattern p = Pattern.compile(MarkdownRegExConstants.LINK_URL);
	MarkdownExtractor extractor;

	@Override
	public void reset() {
		extractor = new MarkdownExtractor();
	}
	
	private String getCurrentDivID() {
		return WebConstants.DIV_ID_AUTOLINK_PREFIX + extractor.getCurrentContainerId() + SharedMarkdownUtils.getPreviewSuffix(isPreview);
	}
	
	private String getNewElementStart() {
		StringBuilder sb = new StringBuilder();
		sb.append(extractor.getContainerElementStart() + getCurrentDivID());
		sb.append("\">");
		return sb.toString();
	}
	
	@Override
	public void processLine(MarkdownElements line, List<MarkdownElementParser> simpleParsers) {
		Matcher m = p.matcher(line.getMarkdown());
		StringBuffer sb = new StringBuffer();
		while(m.find()) {
			String url = m.group(1).trim();
			
			String containerElement = getNewElementStart() + extractor.getContainerElementEnd();
			m.appendReplacement(sb, containerElement);
			
			StringBuilder html = new StringBuilder();
			html.append(ServerMarkdownUtils.START_LINK);
			html.append(url + "\">");
			html.append(url + ServerMarkdownUtils.END_LINK);
			extractor.putContainerIdToContent(getCurrentDivID(), html.toString());
		}
		m.appendTail(sb);
		line.updateMarkdown(sb.toString());
	}

	@Override
	public void completeParse(Document doc) {
		ServerMarkdownUtils.insertExtractedContentToMarkdown(extractor, doc, true);
	}
	
}
