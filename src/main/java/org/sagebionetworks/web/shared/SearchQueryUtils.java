package org.sagebionetworks.web.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.sagebionetworks.repo.model.search.query.KeyValue;
import org.sagebionetworks.repo.model.search.query.SearchFacetOption;
import org.sagebionetworks.repo.model.search.query.SearchFacetSort;
import org.sagebionetworks.repo.model.search.query.SearchFieldName;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import static org.sagebionetworks.repo.model.search.query.SearchFieldName.*;
public class SearchQueryUtils {

	public static final String PROJECT_NODE_TYPE = "project";
	public static final long MAX_FACET_VALUES_COUNT = 300L;
	public static final Long LIMIT = 30L;
	public static SearchQuery getDefaultSearchQuery() {		
		SearchQuery query = getBaseSearchQuery();
		
		// exclude links
		List<KeyValue> bq = new ArrayList<KeyValue>();
		KeyValue kv = new KeyValue();
		kv = new KeyValue();
		kv.setKey(SEARCH_KEY_NODE_TYPE);				
		kv.setValue(PROJECT_NODE_TYPE); 
		bq.add(kv);
		query.setBooleanQuery(bq);
		query.setSize(LIMIT);
		
		return query;
	}

	static SearchQuery getBaseSearchQuery() {
		SearchQuery query = new SearchQuery();
		// start with a blank, valid query
		query.setQueryTerm(Collections.singletonList(""));
		
		List<SearchFieldName> facetFieldNames = Arrays.asList(EntityType, Consortium, ModifiedOn, ModifiedBy, CreatedOn, Tissue, CreatedBy);
		List<SearchFacetOption> facetOptions = new ArrayList<>();
		for (SearchFieldName fieldName : facetFieldNames) {
			SearchFacetOption facetOption = new SearchFacetOption();
			facetOption.setName(fieldName);
			facetOption.setMaxResultCount(MAX_FACET_VALUES_COUNT);
			facetOption.setSortType(SearchFacetSort.COUNT);
			facetOptions.add(facetOption);
		}
		query.setFacetOptions(facetOptions);
		
		return query;
	}
	public final static String SEARCH_KEY_NODE_TYPE = "node_type";
	public final static List<String> FACETS_DISPLAY_ORDER = Arrays.asList("node_type", "consortium","disease", "modified_on", "modified_by", "created_on", "tissue",
			"num_samples", "created_by");

}
