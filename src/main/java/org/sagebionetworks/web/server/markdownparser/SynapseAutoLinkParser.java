package org.sagebionetworks.web.server.markdownparser;

import org.jsoup.Jsoup;
import org.sagebionetworks.web.server.SynapseAutoLinkDetector;

public class SynapseAutoLinkParser extends BasicMarkdownElementParser {
	SynapseAutoLinkDetector detector = SynapseAutoLinkDetector.getInstance();
	
	@Override
	public void processLine(MarkdownElements line) {
		detector.createLinks(Jsoup.parse(line.getHtml()));
	}

}
