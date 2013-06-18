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
	public String getListItemHtml(String item) {
		return "<li>"+item+"</li>";
	}
	public abstract String getEndListHtml();
}
