package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.search.GroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestionBundle;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class GroupSuggestionProviderTest {

	GroupSuggestionProvider presenter;
	SynapseClientAsync mockSynapseClient;
	SynapseJSNIUtils mockJSNI;
	AsyncCallback<SynapseSuggestionBundle> mockCallback;
	
	int offset = 0;
	int pageSize = 10;
	int width = 568;
	String prefix = "test";
	
	
	@Before
	public void setup() {
		mockCallback = mock(AsyncCallback.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockJSNI = mock(SynapseJSNIUtils.class);
		presenter = new GroupSuggestionProvider(mockSynapseClient, mockJSNI);
	}
	
	@Test
	public void testGetSuggestions() {	
		ArgumentCaptor<SynapseSuggestionBundle> captor = ArgumentCaptor.forClass(SynapseSuggestionBundle.class);
		PaginatedResults<Team> testPage = getResponsePage();
		AsyncMockStubber.callSuccessWith(testPage).when(mockSynapseClient).getTeamsBySearch(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));		
		presenter.getSuggestions(offset, pageSize, width, prefix, mockCallback);
		verify(mockSynapseClient).getTeamsBySearch(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));	
		verify(mockCallback).onSuccess(captor.capture());
		SynapseSuggestionBundle testBundle = captor.getValue();
		assertEquals(testBundle.getTotalNumberOfResults(), 6);
		assertEquals(testBundle.getSuggestionBundle().size(), 2);
	}
	
	@Test
	public void testGetSuggestionsFailure() {
		Exception caught = new Exception("this is an exception");
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseClient).getTeamsBySearch(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));		
		presenter.getSuggestions(offset, pageSize, width, prefix, mockCallback);
		verify(mockSynapseClient).getTeamsBySearch(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));	
		verify(mockCallback).onFailure(caught);
	}
	
	private PaginatedResults<Team> getResponsePage() {
		PaginatedResults<Team> testPage = new PaginatedResults();
		
		Team team1 = new Team();
		team1.setId("id1");
		team1.setName("name1");
		
		Team team2 = new Team();
		team2.setId("id2");
		team2.setName("name2");
		
		List<Team> children = new ArrayList<Team>();
		children.add(team1);
		children.add(team2);
		
		testPage.setResults(children);
		testPage.setTotalNumberOfResults(6);
		
		return testPage;
	}
	
}
