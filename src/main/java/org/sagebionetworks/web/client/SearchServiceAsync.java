package org.sagebionetworks.web.client;

import java.util.List;

import org.sagebionetworks.repo.model.entity.query.EntityQuery;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResults;
import org.sagebionetworks.web.shared.ColumnsForType;
import org.sagebionetworks.web.shared.FilterEnumeration;
import org.sagebionetworks.web.shared.SearchParameters;
import org.sagebionetworks.web.shared.TableResults;
import org.sagebionetworks.web.shared.WhereCondition;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SearchServiceAsync {

	void executeSearch(SearchParameters params, AsyncCallback<TableResults> callback);

	void getColumnsForType(String type, AsyncCallback<ColumnsForType> callback);

	void getFilterEnumerations(AsyncCallback<List<FilterEnumeration>> callback);

	/**
	 * Replaced with @see {@link org.sagebionetworks.web.client.SynapseClientAsync#executeEntityQuery(EntityQuery, AsyncCallback)}
	 */
	@Deprecated 
	void searchEntities(String fromType, List<WhereCondition> where,
			int offset, int limit, String sort, boolean ascending,
			AsyncCallback<List<String>> callback);

}
