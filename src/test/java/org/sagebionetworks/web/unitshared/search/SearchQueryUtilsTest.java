package org.sagebionetworks.web.unitshared.search;

import static org.junit.Assert.*;
import static org.sagebionetworks.repo.model.search.query.SearchFieldName.*;
import static org.sagebionetworks.web.shared.SearchQueryUtils.*;

import java.util.*;
import org.junit.Test;
import org.sagebionetworks.repo.model.search.query.*;

public class SearchQueryUtilsTest {
	public static final SearchFieldName[] SET_VALUES = new SearchFieldName[] { EntityType, Consortium, Disease, ModifiedOn, ModifiedBy, CreatedOn, NumSamples, Tissue, CreatedBy };
	public static final Set<SearchFieldName> EXPECTED_FACET_FIELDS = new HashSet<>(Arrays.asList(SET_VALUES));
	
	@Test
	public void testGetDefaultSearchQuery() {
		SearchQuery query = getDefaultSearchQuery();
		
		// verify query term is unset
		assertEquals(1, query.getQueryTerm().size());
		assertEquals("", query.getQueryTerm().get(0));
		
		// verify facet options
		List<SearchFacetOption> options = query.getFacetOptions();
		for (SearchFacetOption facetOption : options) {
			assertTrue(EXPECTED_FACET_FIELDS.contains(facetOption.getName()));
			assertEquals(MAX_FACET_VALUES_COUNT, facetOption.getMaxResultCount().longValue());
			assertEquals(SearchFacetSort.COUNT, facetOption.getSortType());
		}
		
		// verify limit
		assertEquals(LIMIT, query.getSize());
		
		// verify boolean query
		List<KeyValue> bq = query.getBooleanQuery();
		assertEquals(0, bq.size());
	}
}
