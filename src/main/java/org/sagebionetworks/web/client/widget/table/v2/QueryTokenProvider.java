package org.sagebionetworks.web.client.widget.table.v2;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GWTWrapper;

import com.google.inject.Inject;

/**
 * Utility for writing table Query objects to area tokens and back again.
 *  
 * @author John
 *
 */
public class QueryTokenProvider {
	
	private static final String UTF_8 = "UTF-8";
	AdapterFactory factory;
	GWTWrapper gwt;
	
	@Inject
	public QueryTokenProvider(AdapterFactory factory, GWTWrapper gwt){
		this.factory = factory;
		this.gwt = gwt;
	}
	
	/**
	 * Convert a query object to a JSON string.
	 * @param query
	 * @return A token representing a query.
	 */
	public String queryToToken(Query query){
		// Write the query to json.
		try {
			// First to json
			String json = query.writeToJSONObject(factory.createNew()).toJSONString();
			// the token is the json base64 encoded.
			String base64 = new String(Base64.encodeBase64(json.getBytes(UTF_8)), UTF_8);
			return base64;
		} catch (Exception e) {
			// we failed to create a token for the given query.
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
			// The token is base64 encoded json.
			String json = new String(Base64.decodeBase64(token.getBytes(UTF_8)), UTF_8);
			return new Query(factory.createNew(json));
		} catch (Exception e) {
			// If anything goes wrong reading the token null is returned.
			return null;
		}
	}
	
}
