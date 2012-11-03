package org.sagebionetworks.web.server.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.sagebionetworks.client.Synapse;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.KeyValue;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.server.HttpUtils;
import org.sagebionetworks.web.server.NcboUtils;

import com.google.inject.Inject;

public class SimpleSearchService extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String QUERY_PARAM = "query";
	private static final String JSONP_PARAM = "callback";
	private static final String LIMIT_PARAM = "limit";
	private static final String OFFSET_PARAM = "offset";
	private static final String WILDCARD = "*";

	
	static private Log log = LogFactory.getLog(SynapseClientImpl.class);
	@SuppressWarnings("unused")
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	AutoGenFactory entityFactory = new AutoGenFactory();
	
	/**
	 * Injected with Gin
	 */
	@SuppressWarnings("unused")
	private ServiceUrlProvider urlProvider;

	/**
	 * Essentially the constructor. Setup synapse client.
	 * 
	 * @param provider
	 */
	@Inject
	public void setServiceUrlProvider(ServiceUrlProvider provider) {
		this.urlProvider = provider;
	}

	/**
	 * Injected with Gin
	 */
	@SuppressWarnings("unused")
	private SynapseProvider synapseProvider = new SynapseProviderImpl();

	/**
	 * This allows tests provide mock Synapse ojbects
	 * 
	 * @param provider
	 */
	public void setSynapseProvider(SynapseProvider provider) {
		this.synapseProvider = provider;
	}


	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// required params
		String searchTerm = request.getParameter(QUERY_PARAM);

		// optional params
		String jsonpCallback = request.getParameter(JSONP_PARAM);
		Long limit = request.getParameter(LIMIT_PARAM) == null ? null : Long.parseLong(request.getParameter(LIMIT_PARAM)); 
		Long offset = request.getParameter(OFFSET_PARAM) == null ? null : Long.parseLong(request.getParameter(OFFSET_PARAM));

		if(searchTerm == null) {
			HttpUtils.respondBadRequest(response, "Required parameter: " + QUERY_PARAM + " not provided.");		
		} else {		
			// setup query
			SearchQuery searchQuery = getDefaultSimpleQuery();

			searchQuery.setQueryTerm(Arrays.asList(new String[] {searchTerm + WILDCARD}));						
			if(limit != null) searchQuery.setSize(limit);
			if(offset != null) searchQuery.setStart(offset);			
			
			// execute query
			Synapse synapseClient = createSynapseClient(UserDataProvider.getThreadLocalUserToken(request));
			
			try {
				SearchResults searchResults = synapseClient.search(searchQuery);
				JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
				searchResults.writeToJSONObject(adapter);
				HttpUtils.respondJSONString(response, adapter.toJSONString(), jsonpCallback);				
			} catch (Exception e) {
				HttpUtils.respondError(response, "An error occurred accessing the simple search service");
			}
		}		
	}

	/**
	 * The synapse client is stateful so we must create a new one for each
	 * request
	 */
	private Synapse createSynapseClient(String sessionToken) {
		// Create a new syanpse
		Synapse synapseClient = synapseProvider.createNewClient();
		synapseClient.setSessionToken(sessionToken);
		synapseClient.setRepositoryEndpoint(urlProvider
				.getRepositoryServiceUrl());
		synapseClient.setAuthEndpoint(urlProvider.getPublicAuthBaseUrl());
		return synapseClient;
	}

	private SearchQuery getDefaultSimpleQuery() {
		SearchQuery query = new SearchQuery();
		// start with a blank, valid query
		query.setQueryTerm(Arrays.asList(new String[] {""}));		
		query.setReturnFields(Arrays.asList(new String[] {"name","id", "node_type_r","path"}));
		
		// exclude links
		List<KeyValue> bq = new ArrayList<KeyValue>();
		KeyValue kv = new KeyValue();
		kv.setKey("node_type");
		kv.setValue("link");
		kv.setNot(true);
		bq.add(kv);
		query.setBooleanQuery(bq);
		
		return query;
	}
	
}
