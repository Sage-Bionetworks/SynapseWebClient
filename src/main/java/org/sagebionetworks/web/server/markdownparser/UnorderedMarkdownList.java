package org.sagebionetworks.web.server.markdownparser;

public class UnorderedMarkdownList extends MarkdownList {
	public UnorderedMarkdownList(int depth, String startSymbol) {
		super(depth, startSymbol);
	}
	@Override
	public String getStartListHtml() {
		return "<ul>";
	}
	@Override
	public String getEndListHtml() {
		return "</ul>";
	}

}
