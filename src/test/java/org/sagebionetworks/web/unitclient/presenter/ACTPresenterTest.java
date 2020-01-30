package org.sagebionetworks.web.unitclient.presenter;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.repo.model.verification.VerificationPagedResults;
import org.sagebionetworks.repo.model.verification.VerificationStateEnum;
import org.sagebionetworks.repo.model.verification.VerificationSubmission;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.UserProfileClientAsync;
import org.sagebionetworks.web.client.place.ACTPlace;
import org.sagebionetworks.web.client.presenter.ACTPresenter;
import org.sagebionetworks.web.client.view.ACTView;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionRowViewImpl;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class ACTPresenterTest {
	@Mock
	ACTView mockView;
	@Mock
	VerificationSubmissionRowViewImpl mockRowView;
	@Mock
	UserProfileClientAsync mockUserProfileClient;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	ACTPlace mockACTPlace;
	@Mock
	SynapseAlert mockSynapseAlert;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	SynapseSuggestBox mockPeopleSuggestBox;
	@Mock
	UserGroupSuggestionProvider mockUserGroupSuggestionProvider;
	@Mock
	VerificationPagedResults mockVerificationPagedResults;
	@Mock
	VerificationSubmission mockVerificationSubmission;
	@Mock
	VerificationSubmissionWidget mockVerificationSubmissionWidget;
	@Mock
	UserBadge mockUserBadge;
	@Mock
	UserGroupSuggestion mockSuggestion;
	@Mock
	UserGroupHeader mockUserGroupHeader;
	@Mock
	LoadMoreWidgetContainer mockLoadMoreWidgetContainer;
	private static final String OWNER_ID = "12345858";
	ACTPresenter widget;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		when(mockGinInjector.getVerificationSubmissionRowViewImpl()).thenReturn(mockRowView);

		widget = new ACTPresenter(mockView, mockUserProfileClient, mockSynapseAlert, mockPeopleSuggestBox, mockUserGroupSuggestionProvider, mockGinInjector, mockGlobalApplicationState, mockUserBadge, mockLoadMoreWidgetContainer);
		AsyncMockStubber.callSuccessWith(mockVerificationPagedResults).when(mockUserProfileClient).listVerificationSubmissions(any(VerificationStateEnum.class), anyLong(), anyLong(), anyLong(), any(AsyncCallback.class));
		when(mockVerificationPagedResults.getResults()).thenReturn(Collections.singletonList(mockVerificationSubmission));
		when(mockGinInjector.getVerificationSubmissionWidget()).thenReturn(mockVerificationSubmissionWidget);
		when(mockSuggestion.getHeader()).thenReturn(mockUserGroupHeader);
		when(mockUserGroupHeader.getOwnerId()).thenReturn(OWNER_ID);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockView).setSynAlert(any(Widget.class));
		verify(mockView).setStates(anyList());
		verify(mockView).setUserPickerWidget(any(Widget.class));
		verify(mockView).setSelectedUserBadge(any(Widget.class));
		verify(mockPeopleSuggestBox).setTypeFilter(TypeFilter.USERS_ONLY);
	}

	@Test
	public void testLoadData() {
		widget.loadData();
		verify(mockLoadMoreWidgetContainer).clear();
		verify(mockSynapseAlert).clear();
		verify(mockGlobalApplicationState).pushCurrentPlace(any(Place.class));
		verify(mockGinInjector).getVerificationSubmissionWidget();
		boolean isACTMember = true;
		boolean isModal = false;
		verify(mockVerificationSubmissionWidget).configure(mockVerificationSubmission, isACTMember, isModal);
		verify(mockLoadMoreWidgetContainer).add(any(Widget.class));
		verify(mockVerificationSubmissionWidget).show();
	}

	@Test
	public void testOnStateSelected() {
		widget.setPlace(new ACTPlace(""));
		String selectedState = VerificationStateEnum.APPROVED.toString();
		widget.onStateSelected(selectedState);
		assertEquals(selectedState, widget.getPlace().getParam(ACTPlace.STATE_FILTER_PARAM));
		verify(mockView).setSelectedStateText(selectedState);
	}

	@Test
	public void testClearStateFilter() {
		widget.setPlace(new ACTPlace(""));
		reset(mockView);
		widget.onClearStateFilter();
		assertNull(widget.getPlace().getParam(ACTPlace.STATE_FILTER_PARAM));
		verify(mockView).setSelectedStateText("");
	}

	@Test
	public void testOnUserSelected() {
		widget.setPlace(new ACTPlace(""));
		reset(mockView, mockPeopleSuggestBox);
		widget.onUserSelected(mockSuggestion);
		assertEquals(OWNER_ID, widget.getPlace().getParam(ACTPlace.SUBMITTER_ID_FILTER_PARAM));
		verify(mockUserBadge).configure(OWNER_ID);
		verify(mockPeopleSuggestBox).clear();
		verify(mockView).setSelectedUserBadgeVisible(true);
	}

	@Test
	public void testClearUserFilter() {
		widget.setPlace(new ACTPlace(""));
		reset(mockView, mockPeopleSuggestBox);
		widget.onClearUserFilter();
		assertNull(widget.getPlace().getParam(ACTPlace.SUBMITTER_ID_FILTER_PARAM));
		verify(mockPeopleSuggestBox).clear();
		verify(mockView).setSelectedUserBadgeVisible(false);
	}
}
