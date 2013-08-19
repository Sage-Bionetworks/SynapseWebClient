package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetEncodingUtil;

public class ImageParser extends BasicMarkdownElementParser {
	Pattern p1 = Pattern.compile(MarkdownRegExConstants.IMAGE_REGEX);;
	
	@Override
	public void processLine(MarkdownElements line) {
		Matcher m = p1.matcher(line.getMarkdown());
		StringBuffer sb = new StringBuffer();
		while(m.find()) {
			//Create link by preparing widget syntax for the renderer
			String encodedUrl = WidgetEncodingUtil.encodeValue(m.group(2));
			
			//${image?alt=text&fileName=src&fromWeb=true}
			String updated = WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.IMAGE_CONTENT_TYPE + "?" + 
			WidgetConstants.IMAGE_WIDGET_ALT_KEY + "=" + m.group(1) + "&" + WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY + "=" + encodedUrl + 
			"&" + WidgetConstants.IMAGE_WIDGET_FROM_WEB_KEY + "=true" + WidgetConstants.WIDGET_END_MARKDOWN;
			
			//Escape the replacement string for appendReplacement
			updated = Matcher.quoteReplacement(updated);
			m.appendReplacement(sb, updated);
		}
		m.appendTail(sb);
		line.updateMarkdown(sb.toString());
	}
}
