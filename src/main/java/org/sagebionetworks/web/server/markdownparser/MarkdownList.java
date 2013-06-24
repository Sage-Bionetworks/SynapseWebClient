package org.sagebionetworks.web.server.markdownparser;

public abstract class MarkdownList {
	private int depth;
	public MarkdownList(int depth) {
		super();
		this.depth = depth;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public abstract String getStartListHtml();
	public void addListItemHtml(MarkdownElements line, String item) {
		line.prependElement("<li>");
		line.updateMarkdown(item);
		line.appendElement("</li>");
	}
	public abstract String getEndListHtml();
}
