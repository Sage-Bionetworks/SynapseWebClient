package org.sagebionetworks.web.shared.table;

import com.google.gwt.user.client.rpc.IsSerializable;

public class QueryResult implements IsSerializable {

	String rowSetJson;
	String executedQuery;
	QueryDetails queryDetails;
	
	public QueryResult() {
		super();
	}
	
	public QueryResult(String rowSetJson, String executedQuery,
			QueryDetails queryDetails) {
		super();
		this.rowSetJson = rowSetJson;
		this.executedQuery = executedQuery;
		this.queryDetails = queryDetails;
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

	@Override
	public String toString() {
		return "QueryResult [rowSetJson=" + rowSetJson + ", executedQuery="
				+ executedQuery + ", queryDetails=" + queryDetails + "]";
	}
		
}
