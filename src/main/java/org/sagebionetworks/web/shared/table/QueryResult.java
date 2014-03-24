package org.sagebionetworks.web.shared.table;

import com.google.gwt.user.client.rpc.IsSerializable;

public class QueryResult implements IsSerializable {

	String rowSetJson;
	String executedQuery;
	QueryDetails queryDetails;
	Integer totalRowCount = null;
	
	public QueryResult() {
		super();
	}
	
	public QueryResult(String rowSetJson, String executedQuery,
			QueryDetails queryDetails, Integer totalRowCount) {
		super();
		this.rowSetJson = rowSetJson;
		this.executedQuery = executedQuery;
		this.queryDetails = queryDetails;
		this.totalRowCount = totalRowCount;
	}

	public String getRowSetJson() {
		return rowSetJson;
	}

	public void setRowSetJson(String rowSetJson) {
		this.rowSetJson = rowSetJson;
	}

	public String getExecutedQuery() {
		return executedQuery;
	}

	public void setExecutedQuery(String executedQuery) {
		this.executedQuery = executedQuery;
	}

	public QueryDetails getQueryDetails() {
		return queryDetails;
	}

	public void setQueryDetails(QueryDetails queryDetails) {
		this.queryDetails = queryDetails;
	}

	public Integer getTotalRowCount() {
		return totalRowCount;
	}

	public void setTotalRowCount(Integer totalRowCount) {
		this.totalRowCount = totalRowCount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((executedQuery == null) ? 0 : executedQuery.hashCode());
		result = prime * result
				+ ((queryDetails == null) ? 0 : queryDetails.hashCode());
		result = prime * result
				+ ((rowSetJson == null) ? 0 : rowSetJson.hashCode());
		result = prime * result
				+ ((totalRowCount == null) ? 0 : totalRowCount.hashCode());
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
		QueryResult other = (QueryResult) obj;
		if (executedQuery == null) {
			if (other.executedQuery != null)
				return false;
		} else if (!executedQuery.equals(other.executedQuery))
			return false;
		if (queryDetails == null) {
			if (other.queryDetails != null)
				return false;
		} else if (!queryDetails.equals(other.queryDetails))
			return false;
		if (rowSetJson == null) {
			if (other.rowSetJson != null)
				return false;
		} else if (!rowSetJson.equals(other.rowSetJson))
			return false;
		if (totalRowCount == null) {
			if (other.totalRowCount != null)
				return false;
		} else if (!totalRowCount.equals(other.totalRowCount))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "QueryResult [rowSetJson=" + rowSetJson + ", executedQuery="
				+ executedQuery + ", queryDetails=" + queryDetails
				+ ", totalRowCount=" + totalRowCount + "]";
	}
		
}
