package org.sagebionetworks.web.server.markdownparser;

public class OrderedMarkdownList extends MarkdownList {
	private int orderValue;

	public OrderedMarkdownList(int depth) {
		super(depth);
		this.orderValue = 1;
	}
	
	/**
	 * Currently not used, but could be used to restrict it to the correct order
	 * @return
	 */
	public int getOrderValue() {
		return orderValue;
	}
	@Override
	public String getStartListHtml() {
		return "<ol>";
	}
	@Override
	public String getEndListHtml() {
		return "</ol>";
	}
}
