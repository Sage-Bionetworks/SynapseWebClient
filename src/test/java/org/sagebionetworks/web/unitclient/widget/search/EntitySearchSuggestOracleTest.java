package org.sagebionetworks.web.unitclient.widget.search;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.search.EntitySearchSuggestOracle.EntitySuggestion.MAX_ENTITY_PATH_DISPLAY_LENGTH;
import static org.sagebionetworks.web.client.widget.search.EntitySearchSuggestOracle.EntitySuggestion.PATH_SEPARATOR;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.search.Hit;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.web.client.GWTTimer;
import org.sagebionetworks.web.client.StringUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.search.EntitySearchSuggestOracle;
import org.sagebionetworks.web.client.widget.search.EntitySearchSuggestOracle.EntitySuggestion;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;

@RunWith(MockitoJUnitRunner.class)
public class EntitySearchSuggestOracleTest {

	EntitySearchSuggestOracle entitySearchOracle;
	
	@Mock
	SearchResults mockSearchResults;
	@Mock
	SuggestOracle.Callback mockCallback;
	@Mock
	SuggestOracle.Request mockRequest;
	@Mock
	GWTTimer mockTimer;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	SynapseJSNIUtils mockJsniUtils;
	public static final String SEARCH_TERM = "test search";
	@Mock
	Hit mockHit1;
	@Mock
	EntityPath mockEntityPath1;
	List<EntityHeader> entityPath1;
	public static final String HIT1_ENTITY_ID = "syn11111";
	public static final String HIT1_PROJECT_NAME = "Test Project";
	
	@Mock
	Hit mockHit2;
	@Mock
	EntityPath mockEntityPath2;
	List<EntityHeader> entityPath2;
	public static final String HIT2_ENTITY_ID = "syn22222";
	public static final String HIT2_PROJECT_NAME_TOO_LONG = "Test Project 2 that has a name that's far too long for the display";
	public static final String HIT2_TRUNCATED_PROJECT_NAME = StringUtils.truncateValues((PATH_SEPARATOR + HIT2_PROJECT_NAME_TOO_LONG).trim(), MAX_ENTITY_PATH_DISPLAY_LENGTH, true);
	public static final String HIT2_TARGET_FILE_NAME = "Target file entity";
	
	@Captor
	ArgumentCaptor<SuggestOracle.Response> responseCaptor;
	
	@Captor
	ArgumentCaptor<SearchQuery> searchQueryCaptor;
	
	List<Hit> hitResults;
	
	@Before
	public void setup() {
		when(mockRequest.getQuery()).thenReturn(SEARCH_TERM);
		
		// Set up the search hit results.
		// In this test, it returns 2 results.  The first is a Project, the second is a FileEntity (in a Project that has far too long of a name to display)
		hitResults = new ArrayList<>();
		
		EntityHeader rootEntityHeader = new EntityHeader();
		rootEntityHeader.setName("Synapse root node");
		entityPath1 = new ArrayList<>();
		entityPath1.add(rootEntityHeader);
		EntityHeader hit1Project = new EntityHeader();
		hit1Project.setName(HIT1_PROJECT_NAME);
		entityPath1.add(hit1Project);
		when(mockHit1.getPath()).thenReturn(mockEntityPath1);
		when(mockEntityPath1.getPath()).thenReturn(entityPath1);
		when(mockHit1.getId()).thenReturn(HIT1_ENTITY_ID);
		when(mockHit1.getName()).thenReturn(HIT1_PROJECT_NAME);
		
		entityPath2 = new ArrayList<>();
		entityPath2.add(rootEntityHeader);
		EntityHeader hit2Project = new EntityHeader();
		hit2Project.setName(HIT2_PROJECT_NAME_TOO_LONG);
		entityPath2.add(hit2Project);
		EntityHeader hit2File = new EntityHeader();
		hit2File.setName(HIT2_TARGET_FILE_NAME);
		entityPath2.add(hit2File);
		when(mockHit2.getPath()).thenReturn(mockEntityPath2);
		when(mockEntityPath2.getPath()).thenReturn(entityPath2);
		when(mockHit2.getId()).thenReturn(HIT2_ENTITY_ID);
		when(mockHit2.getName()).thenReturn(HIT2_TARGET_FILE_NAME);
		
		entitySearchOracle = new EntitySearchSuggestOracle(mockTimer, mockJsClient, mockJsniUtils);
		hitResults.add(mockHit1);
		hitResults.add(mockHit2);
		when(mockSearchResults.getHits()).thenReturn(hitResults);
		
		AsyncMockStubber.callSuccessWith(mockSearchResults).when(mockJsClient).getSearchResults(any(SearchQuery.class), any(AsyncCallback.class));
	}
	
	@Test
	public void testConstructor() {
		mockTimer.configure(any(Runnable.class));
	}
	
	@Test
	public void testRequestSuggestions() {
		entitySearchOracle.requestSuggestions(mockRequest, mockCallback);

		//verify old timer is canceled, and we schedule it to run once the user pauses for DELAY ms
		mockTimer.cancel();
		mockTimer.schedule(EntitySearchSuggestOracle.DELAY);
	}

	@Test
	public void testGetNewSuggestions() {
		int offset = 0;
		// set up request
		entitySearchOracle.requestSuggestions(mockRequest, mockCallback);
		entitySearchOracle.getNewSuggestions(offset);
		
		verify(mockJsClient).getSearchResults(searchQueryCaptor.capture(), any(AsyncCallback.class));
		//verify SearchQuery
		SearchQuery searchQuery = searchQueryCaptor.getValue();
		assertEquals(1, searchQuery.getQueryTerm().size());
		assertEquals(SEARCH_TERM, searchQuery.getQueryTerm().get(0));
		
		verify(mockCallback).onSuggestionsReady(eq(mockRequest), responseCaptor.capture());
		ArrayList<EntitySuggestion> suggestions = (ArrayList<EntitySuggestion>)responseCaptor.getValue().getSuggestions();
		assertEquals(2, suggestions.size());

		EntitySuggestion suggestion1 = suggestions.get(0);
		assertEquals(HIT1_ENTITY_ID, suggestion1.getEntityId());
		assertEquals(PATH_SEPARATOR + HIT1_PROJECT_NAME, suggestion1.getDisplayString());
		assertEquals(HIT1_PROJECT_NAME, suggestion1.getReplacementString());
		
		EntitySuggestion suggestion2 = suggestions.get(1);
		assertEquals(HIT2_ENTITY_ID, suggestion2.getEntityId());
		assertEquals(HIT2_TRUNCATED_PROJECT_NAME + PATH_SEPARATOR + HIT2_TARGET_FILE_NAME, suggestion2.getDisplayString());
		assertEquals(HIT2_TARGET_FILE_NAME, suggestion2.getReplacementString());
	}
}
