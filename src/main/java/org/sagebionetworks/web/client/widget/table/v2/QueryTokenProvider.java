package org.sagebionetworks.web.client.widget.table.v2;

import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;

import com.google.inject.Inject;

/**
 * Utility for writing table Query objects to area tokens and back again.
 *  
 * @author John
 *
 */
public class QueryTokenProvider {
	
	AdapterFactory factory;
	
	@Inject
	public QueryTokenProvider(AdapterFactory factory){
		this.factory = factory;
	}
	
	/**
	 * Convert a query object to a JSON string.
	 * @param query
	 * @return A token representing a query.
	 */
	public String queryToToken(Query query){
		// Write the query to json.
		try {
			return query.writeToJSONObject(factory.createNew()).toJSONString();
		} catch (JSONObjectAdapterException e) {
			return null;
		}
	}

	/**
	 * Parse a token into a query.
	 * @param token
	 * @return The resulting Query object.  Null if there is an error parsing the query.
	 */
	public Query tokenToQuery(String token){
		try {
			return new Query(factory.createNew(token));
		} catch (JSONObjectAdapterException e) {
			return null;
		}
	}
}
