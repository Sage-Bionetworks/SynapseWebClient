package org.sagebionetworks.web.server.markdownparser;

import org.jsoup.Jsoup;
import org.sagebionetworks.web.server.UrlAutoLinkDetector;

public class UrlAutoLinkParser extends BasicMarkdownElementParser {
	UrlAutoLinkDetector detector = UrlAutoLinkDetector.getInstance();
	
	@Override
	public void processLine(MarkdownElements line) {
		detector.createLinks(Jsoup.parse(line.getHtml()));
	}
}
