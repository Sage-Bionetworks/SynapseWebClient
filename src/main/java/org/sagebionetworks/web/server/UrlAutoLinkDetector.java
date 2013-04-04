package org.sagebionetworks.web.server;

import java.util.regex.Matcher;

public class UrlAutoLinkDetector extends AutoLinkDetector {

	private static UrlAutoLinkDetector instance;
	protected UrlAutoLinkDetector(){};
	public static UrlAutoLinkDetector getInstance() {
		if (instance == null)
			instance = new UrlAutoLinkDetector();
		return instance;
	}
	
	@Override
	public String getRegularExpression() {
		return "\\b((https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])"; //from http://stackoverflow.com/questions/163360/regular-expresion-to-match-urls-java 
	}

	@Override
	public int getCorrectGroupCount() {
		return 2;
	}

	@Override
	public String getLinkHtml(Matcher matcher) {
		return ServerMarkdownUtils.getUrlHtml(matcher.group(1));
	}

}
