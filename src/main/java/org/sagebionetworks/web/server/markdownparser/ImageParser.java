package org.sagebionetworks.web.server.markdownparser;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.sagebionetworks.web.client.widget.entity.SharedMarkdownUtils;
import org.sagebionetworks.web.server.ServerMarkdownUtils;
import org.sagebionetworks.web.shared.WebConstants;

public class ImageParser extends BasicMarkdownElementParser {
	Pattern p1 = Pattern.compile(MarkdownRegExConstants.IMAGE_REGEX);;
	MarkdownExtractor extractor;

	@Override
	public void reset(List<MarkdownElementParser> simpleParsers) {
		extractor = new MarkdownExtractor();
	}
	
	private String getCurrentDivID() {
		return WebConstants.DIV_ID_IMAGE_PREFIX + extractor.getCurrentContainerId() + SharedMarkdownUtils.getPreviewSuffix(isPreview);
	}

	@Override
	public void processLine(MarkdownElements line) {
		Matcher m = p1.matcher(line.getMarkdown());
		StringBuffer sb = new StringBuffer();
		while(m.find()) {
			String src = m.group(2);
			String alt = m.group(1);
			
			StringBuilder html = new StringBuilder();
			html.append("<img src=\"");
			html.append(src + "\" alt=\"");
			html.append(alt + "\" />");
			extractor.putContainerIdToContent(getCurrentDivID(), html.toString());
			
			String containerElement = extractor.getNewElementStart(getCurrentDivID()) + extractor.getContainerElementEnd();
			m.appendReplacement(sb, containerElement);
		}
		m.appendTail(sb);
		line.updateMarkdown(sb.toString());
	}

	@Override
	public void completeParse(Document doc) {
		ServerMarkdownUtils.insertExtractedContentToMarkdown(extractor, doc, true);
	}
}
