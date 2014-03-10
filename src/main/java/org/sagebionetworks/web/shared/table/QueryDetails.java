package org.sagebionetworks.web.shared.table;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Details needed by the view for a given query
 * 
 * @author dburdick
 *
 */
public class QueryDetails implements IsSerializable {
	
	public enum SortDirection { ASC, DESC };
	
	private Integer offset;
	private Integer limit;
	private String sortedColumnId;
	private SortDirection sortDirection;
		
	public QueryDetails() {		
	}
	
	public QueryDetails(Integer offset, Integer limit, String sortedColumnId,
			SortDirection sortDirection) {
		super();
		this.offset = offset;
		this.limit = limit;
		this.sortedColumnId = sortedColumnId;
		this.sortDirection = sortDirection;
	}
	
	public Integer getOffset() {
		return offset;
	}
	public void setOffset(Integer offset) {
		this.offset = offset;
	}
	public Integer getLimit() {
		return limit;
	}
	public void setLimit(Integer limit) {
		this.limit = limit;
	}
	public String getSortedColumnId() {
		return sortedColumnId;
	}
	public void setSortedColumnId(String sortedColumnId) {
		this.sortedColumnId = sortedColumnId;
	}
	public SortDirection getSortDirection() {
		return sortDirection;
	}
	public void setSortDirection(SortDirection sortDirection) {
		this.sortDirection = sortDirection;
	}
	
	
	
}
