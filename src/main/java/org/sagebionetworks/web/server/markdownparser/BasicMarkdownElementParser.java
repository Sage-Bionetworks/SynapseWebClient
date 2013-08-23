package org.sagebionetworks.web.server.markdownparser;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class BasicMarkdownElementParser implements MarkdownElementParser {
	protected boolean isPreview;
	
	/**
	 * Can leave alone if element parser has no state 
	 */
	@Override
	public void reset() {
	}
	
	/**
	 * Can leave along if parser does not need to operate on the entire html document after processing is complete (most don't).
	 */
	@Override
	public void completeParse(StringBuilder html) {
	}

	/**
	 *  If all parsing is complete on a single line, then this method can be ignored. 
	 */
	@Override
	public boolean isInMarkdownElement() {
		return false;
	}

	/**
	 * If output html is preformatted, the this will inform the processor.
	 */
	@Override
	public boolean isBlockElement() {
		return false;
	}
	
	@Override
	public boolean isInputSingleLine() {
		return true;
	}
	
	protected String getLineWithoutHTML(String line) {
		return Jsoup.parse(line).text();
	}
	
	/**
	 * Can leave along if parser does not need to operate on the entire html document after processing is complete (most don't).
	 */
	@Override
	public void completeParse(Document doc) {
	}

	@Override
	public void setIsPreview(boolean isPreview) {
		this.isPreview= isPreview;
	}
	
	public String runSimpleParsers(String line, List<MarkdownElementParser> simpleParsers) {
		MarkdownElements elements = new MarkdownElements(line);
		for (MarkdownElementParser parser : simpleParsers) {
			parser.processLine(elements, simpleParsers);
		}
		return elements.getHtml();
	}
}
