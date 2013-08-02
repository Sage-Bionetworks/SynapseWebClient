package org.sagebionetworks.web.server.markdownparser;

import org.jsoup.Jsoup;
import org.sagebionetworks.web.server.DoiAutoLinkDetector;

public class DoiAutoLinkParser extends BasicMarkdownElementParser {
	DoiAutoLinkDetector detector = DoiAutoLinkDetector.getInstance();
	
	@Override
	public void processLine(MarkdownElements line) {
		detector.createLinks(Jsoup.parse(line.getHtml()));
	}

}
