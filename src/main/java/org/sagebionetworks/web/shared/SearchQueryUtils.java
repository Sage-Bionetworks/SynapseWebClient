package org.sagebionetworks.web.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sagebionetworks.repo.model.search.query.KeyValue;
import org.sagebionetworks.repo.model.search.query.SearchQuery;

public class SearchQueryUtils {

	public static final Long LIMIT = 30L;
	public static SearchQuery getDefaultSearchQuery() {		
		SearchQuery query = getBaseSearchQueryNoFacets();
		
		// exclude links
		List<KeyValue> bq = new ArrayList<KeyValue>();
		KeyValue kv = new KeyValue();
		kv = new KeyValue();
		kv.setKey(SEARCH_KEY_NODE_TYPE);				
		kv.setValue("project"); 
		bq.add(kv);
		query.setBooleanQuery(bq);
		
		query.setFacet(FACETS_DISPLAY_ORDER);
		query.setSize(LIMIT);
		
		return query;
	}

	public static SearchQuery getAllTypesSearchQuery() {		
		SearchQuery query = getBaseSearchQueryNoFacets();
		
		// exclude links
		List<KeyValue> bq = new ArrayList<KeyValue>();
		KeyValue kv = new KeyValue();
		kv.setKey("node_type");
		kv.setValue("link");
		kv.setNot(true);
		bq.add(kv);
		query.setBooleanQuery(bq);
		
		query.setFacet(FACETS_DISPLAY_ORDER);
		
		return query;
	}

	static SearchQuery getBaseSearchQueryNoFacets() {
		SearchQuery query = new SearchQuery();
		// start with a blank, valid query
		query.setQueryTerm(Arrays.asList(new String[] {""}));		
		query.setReturnFields(Arrays.asList(new String[] {"name","description","id", "node_type_r", "created_by_r", "created_on", "modified_by_r", "modified_on", "path"}));
		return query;
	}

	/*
	 * Search
	 */
	public final static String SEARCH_KEY_NODE_TYPE = "node_type";
	public final static String SEARCH_KEY_SPECIES = "species";
	public final static String SEARCH_KEY_DISEASE = "disease";
	public final static String SEARCH_KEY_MODIFIED_ON = "modified_on";
	public final static String SEARCH_KEY_CREATED_ON = "created_on";
	public final static String SEARCH_KEY_TISSUE = "tissue";
	public final static String SEARCH_KEY_NUM_SAMPLES = "num_samples";
	public final static String SEARCH_KEY_CREATED_BY = "created_by";
	public final static List<String> FACETS_DISPLAY_ORDER = Arrays
	.asList(new String[] { SEARCH_KEY_NODE_TYPE, SEARCH_KEY_SPECIES,
			SEARCH_KEY_DISEASE, SEARCH_KEY_MODIFIED_ON,
			SEARCH_KEY_CREATED_ON, SEARCH_KEY_TISSUE,
			SEARCH_KEY_NUM_SAMPLES, SEARCH_KEY_CREATED_BY });

}
