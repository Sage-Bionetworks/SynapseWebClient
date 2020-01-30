package org.sagebionetworks.web.client.widget.table.v2;

import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import com.google.inject.Inject;

/**
 * Utility for writing table Query objects to area tokens and back again. Encoding/decoding done via
 * native js methods btoa and atob.
 * 
 * @author John
 *
 */
public class QueryTokenProvider {

	AdapterFactory factory;

	@Inject
	public QueryTokenProvider(AdapterFactory factory) {
		this.factory = factory;
	}

	/**
	 * Convert a query object to a JSON string.
	 * 
	 * @param query
	 * @return A token representing a query.
	 */
	public String queryToToken(Query query) {
		// Write the query to json.
		try {
			// First to json
			String json = query.writeToJSONObject(factory.createNew()).toJSONString();
			// the token is the json base64 encoded.
			return _encode(json);
		} catch (Exception e) {
			// we failed to create a token for the given query.
			return null;
		}
	}

	public native String _encode(String s) /*-{
																					return btoa(s);
																					}-*/;

	public native String _decode(String s) /*-{
																					return atob(s);
																					}-*/;

	/**
	 * Parse a token into a query.
	 * 
	 * @param token
	 * @return The resulting Query object. Null if there is an error parsing the query.
	 */
	public Query tokenToQuery(String token) {
		try {
			// The token is base64 encoded json.
			String json = _decode(token);
			return new Query(factory.createNew(json));
		} catch (Exception e) {
			// If anything goes wrong reading the token null is returned.
			return null;
		}
	}

}
