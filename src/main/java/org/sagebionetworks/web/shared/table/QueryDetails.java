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
	
	private Long offset;
	private Long limit;
	private String sortedColumnName;
	private SortDirection sortDirection;
		
	public QueryDetails() {		
	}
	
	public QueryDetails(Long offset, Long limit, String sortedColumnName,
			SortDirection sortDirection) {
		super();
		this.offset = offset;
		this.limit = limit;
		this.sortedColumnName = sortedColumnName;
		this.sortDirection = sortDirection;
	}

	public Long getOffset() {
		return offset;
	}

	public void setOffset(Long offset) {
		this.offset = offset;
	}

	public Long getLimit() {
		return limit;
	}

	public void setLimit(Long limit) {
		this.limit = limit;
	}

	public String getSortedColumnName() {
		return sortedColumnName;
	}

	public void setSortedColumnName(String sortedColumnName) {
		this.sortedColumnName = sortedColumnName;
	}

	public SortDirection getSortDirection() {
		return sortDirection;
	}

	public void setSortDirection(SortDirection sortDirection) {
		this.sortDirection = sortDirection;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((limit == null) ? 0 : limit.hashCode());
		result = prime * result + ((offset == null) ? 0 : offset.hashCode());
		result = prime * result
				+ ((sortDirection == null) ? 0 : sortDirection.hashCode());
		result = prime * result
				+ ((sortedColumnName == null) ? 0 : sortedColumnName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueryDetails other = (QueryDetails) obj;
		if (limit == null) {
			if (other.limit != null)
				return false;
		} else if (!limit.equals(other.limit))
			return false;
		if (offset == null) {
			if (other.offset != null)
				return false;
		} else if (!offset.equals(other.offset))
			return false;
		if (sortDirection != other.sortDirection)
			return false;
		if (sortedColumnName == null) {
			if (other.sortedColumnName != null)
				return false;
		} else if (!sortedColumnName.equals(other.sortedColumnName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "QueryDetails [offset=" + offset + ", limit=" + limit
				+ ", sortedColumnName=" + sortedColumnName + ", sortDirection="
				+ sortDirection + "]";
	}
	

}
