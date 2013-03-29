package org.sagebionetworks.web.server;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public abstract class AutoLinkDetector {
	
	public void createLinks(Document doc) {
		String regEx = getRegularExpression();
		Elements elements = doc.select("*:matchesOwn(" + regEx + "):not(a,code)");  	
		// selector is case insensitive
		Pattern pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
		for (Element element : elements) {
			//only process the TextNode children (ignore others)
			for (Node childNode : element.childNodes()) {
				if (childNode instanceof TextNode) {
					String oldText = ((TextNode) childNode).text();
					// find it in the text
					Matcher matcher = pattern.matcher(oldText);
					StringBuilder sb = new StringBuilder();
					int previousFoundIndex = 0;
					while (matcher.find() && matcher.groupCount() == getCorrectGroupCount()) {
						sb.append(oldText.substring(previousFoundIndex, matcher.start(1)));
						sb.append(getLinkHtml(matcher));
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
	public abstract String getRegularExpression();
	public abstract int getCorrectGroupCount();
	public abstract String getLinkHtml(Matcher matcher);
}
