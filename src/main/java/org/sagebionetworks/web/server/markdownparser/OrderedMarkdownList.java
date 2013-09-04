package org.sagebionetworks.web.server.markdownparser;

public class OrderedMarkdownList extends MarkdownList {
	private int orderValue;

	public OrderedMarkdownList(int depth, String startSymbol) {
		super(depth, startSymbol);
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
		return "<ol start=\"" + getStartSymbol() + "\">";
	}
	@Override
	public String getEndListHtml() {
		return "</ol>";
	}
}
