package org.sagebionetworks.web.server.markdownparser;

public class OrderedMarkdownList extends MarkdownList {
	private int orderValue;
	private String startSymbol;

	public OrderedMarkdownList(int depth, String startSymbol) {
		super(depth);
		this.orderValue = 1;
		this.startSymbol = startSymbol;
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
		return "<ol start=\"" + startSymbol + "\">";
	}
	@Override
	public String getEndListHtml() {
		return "</ol>";
	}
}
