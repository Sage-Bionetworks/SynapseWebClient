package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.GWTTimer;
import org.sagebionetworks.web.client.widget.search.SuggestionProvider;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestOracle;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestion;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestionBundle;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBox;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Response;

public class SynapseSuggestOracleTest {

	UserGroupSuggestBox mockSuggestBox;
	SuggestionProvider mockSuggestionProvider;
	SynapseSuggestOracle presenter;
	SynapseSuggestion mockSuggestion;
	SuggestOracle.Callback mockCallback;
	SuggestOracle.Request mockRequest;
	GWTTimer mockTimer;
	
	int pageSize = 10;
	int offset = 0;
	SynapseSuggestionBundle suggBundle;
	String query = "test";
	
	@Before
	public void setup() {
		mockSuggestBox = mock(UserGroupSuggestBox.class);
		mockSuggestionProvider = mock(SuggestionProvider.class);
		mockSuggestion = mock(SynapseSuggestion.class);
		mockCallback = mock(SuggestOracle.Callback.class);
		mockRequest = mock(SuggestOracle.Request.class);
		Mockito.when(mockRequest.getQuery()).thenReturn(query);
		mockTimer = mock(GWTTimer.class);
		presenter = new SynapseSuggestOracle(mockTimer);
		presenter.configure(mockSuggestBox, pageSize, mockSuggestionProvider);
		presenter.requestSuggestions(mockRequest, mockCallback);
		List<SynapseSuggestion> suggList = new LinkedList<SynapseSuggestion>();
		for (int i = 0; i < pageSize; i++) {
			suggList.add(mockSuggestion);
		}
		int totalResults = 42;
		suggBundle = new SynapseSuggestionBundle(suggList, totalResults);
	}
	
	@Test
	public void testGetSuggestions() {
		AsyncMockStubber.callSuccessWith(suggBundle).when(mockSuggestionProvider).getSuggestions(anyInt(), anyInt(), anyInt(), anyString(), any(AsyncCallback.class));
		presenter.getSuggestions(offset);
		verify(mockSuggestBox).showLoading();
		verify(mockSuggestionProvider).getSuggestions(eq(offset), eq(pageSize),
				anyInt(), eq(query), any(AsyncCallback.class));
		verify(mockSuggestBox).hideLoading();
		verify(mockSuggestBox).updateFieldStateForSuggestions((int)suggBundle.getTotalNumberOfResults(), offset);
		verify(mockCallback).onSuggestionsReady(eq(mockRequest), any(Response.class));
	}

	@Test
	public void testGetSuggestionsFailure() {
		Exception caught = new Exception("an error message");
		AsyncMockStubber.callFailureWith(caught).when(mockSuggestionProvider).getSuggestions(anyInt(), anyInt(), anyInt(), anyString(), any(AsyncCallback.class));
		presenter.getSuggestions(offset);
		verify(mockSuggestBox).showLoading();
		verify(mockSuggestionProvider).getSuggestions(eq(offset), eq(pageSize),
				anyInt(), eq(query), any(AsyncCallback.class));
		verify(mockSuggestBox).hideLoading();
		verify(mockSuggestBox).handleOracleException(caught);
	}
	

	
}
