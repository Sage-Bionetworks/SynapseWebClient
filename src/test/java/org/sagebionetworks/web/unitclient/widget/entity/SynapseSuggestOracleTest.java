package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.GWTTimer;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestOracle;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestionBundle;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Response;

public class SynapseSuggestOracleTest {

	SynapseSuggestBox mockSuggestBox;
	UserGroupSuggestionProvider mockSuggestionProvider;
	SynapseSuggestOracle presenter;
	UserGroupSuggestion mockSuggestion;
	SuggestOracle.Callback mockCallback;
	SuggestOracle.Request mockRequest;
	GWTTimer mockTimer;

	int pageSize = 10;
	int offset = 0;
	SynapseSuggestionBundle suggBundle;
	String query = "test";

	public final String USERNAME = "username";

	@Before
	public void setup() {
		mockSuggestBox = mock(SynapseSuggestBox.class);
		mockSuggestionProvider = mock(UserGroupSuggestionProvider.class);
		mockSuggestion = mock(UserGroupSuggestion.class);
		mockCallback = mock(SuggestOracle.Callback.class);
		mockRequest = mock(SuggestOracle.Request.class);
		when(mockRequest.getQuery()).thenReturn(query);
		mockTimer = mock(GWTTimer.class);
		presenter = new SynapseSuggestOracle(mockTimer);
		presenter.configure(mockSuggestBox, pageSize, mockSuggestionProvider);
		presenter.requestSuggestions(mockRequest, mockCallback);
		List<UserGroupSuggestion> suggList = new LinkedList<>();
		for (int i = 0; i < pageSize; i++) {
			suggList.add(mockSuggestion);
		}
		int totalResults = 42;
		suggBundle = new SynapseSuggestionBundle(suggList, totalResults);
	}

	@Test
	public void testConfigure() {
		presenter.configure(mockSuggestBox, pageSize, mockSuggestionProvider);
		mockTimer.configure(any(Runnable.class));
	}

	@Test
	public void testGetSuggestionsWithoutExactMatch() {
		when(mockSuggestBox.getText()).thenReturn(USERNAME);
		UserGroupHeader mockHeader = mock(UserGroupHeader.class);
		when(mockHeader.getUserName()).thenReturn("notUsername");
		when(mockSuggestion.getHeader()).thenReturn(mockHeader);
		AsyncMockStubber.callSuccessWith(suggBundle).when(mockSuggestionProvider).getSuggestions(any(TypeFilter.class), anyInt(), anyInt(), anyInt(), anyString(), any(AsyncCallback.class));
		presenter.getSuggestions(offset);
		verify(mockSuggestBox).showLoading();
		verify(mockSuggestBox).setSelectedSuggestion((UserGroupSuggestion) isNull());
		verify(mockSuggestionProvider).getSuggestions(eq(TypeFilter.ALL), eq(offset), eq(pageSize), anyInt(), eq(query), any(AsyncCallback.class));
		verify(mockSuggestBox).hideLoading();
		verify(mockSuggestBox).updateFieldStateForSuggestions((int) suggBundle.getTotalNumberOfResults(), offset);
		verify(mockCallback).onSuggestionsReady(eq(mockRequest), any(Response.class));
	}

	@Test
	public void testGetSuggestionsFailure() {
		Exception caught = new Exception("an error message");
		AsyncMockStubber.callFailureWith(caught).when(mockSuggestionProvider).getSuggestions(any(TypeFilter.class), anyInt(), anyInt(), anyInt(), anyString(), any(AsyncCallback.class));
		presenter.getSuggestions(offset);
		verify(mockSuggestBox).showLoading();
		verify(mockSuggestionProvider).getSuggestions(eq(TypeFilter.ALL), eq(offset), eq(pageSize), anyInt(), eq(query), any(AsyncCallback.class));
		verify(mockSuggestBox).hideLoading();
		verify(mockSuggestBox).handleOracleException(caught);
	}



}
