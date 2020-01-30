package org.sagebionetworks.web.unitshared.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.sagebionetworks.repo.model.search.query.SearchFieldName.Consortium;
import static org.sagebionetworks.repo.model.search.query.SearchFieldName.CreatedBy;
import static org.sagebionetworks.repo.model.search.query.SearchFieldName.CreatedOn;
import static org.sagebionetworks.repo.model.search.query.SearchFieldName.EntityType;
import static org.sagebionetworks.repo.model.search.query.SearchFieldName.ModifiedBy;
import static org.sagebionetworks.repo.model.search.query.SearchFieldName.ModifiedOn;
import static org.sagebionetworks.repo.model.search.query.SearchFieldName.Tissue;
import static org.sagebionetworks.web.shared.SearchQueryUtils.LIMIT;
import static org.sagebionetworks.web.shared.SearchQueryUtils.MAX_FACET_VALUES_COUNT;
import static org.sagebionetworks.web.shared.SearchQueryUtils.getDefaultSearchQuery;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.sagebionetworks.repo.model.search.query.KeyValue;
import org.sagebionetworks.repo.model.search.query.SearchFacetOption;
import org.sagebionetworks.repo.model.search.query.SearchFacetSort;
import org.sagebionetworks.repo.model.search.query.SearchFieldName;
import org.sagebionetworks.repo.model.search.query.SearchQuery;

public class SearchQueryUtilsTest {
	public static final SearchFieldName[] SET_VALUES = new SearchFieldName[] {EntityType, Consortium, ModifiedOn, ModifiedBy, CreatedOn, Tissue, CreatedBy};
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
