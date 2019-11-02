package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.place.ACTAccessApprovalsPlace.ACCESS_REQUIREMENT_ID_PARAM;
import static org.sagebionetworks.web.client.place.ACTAccessApprovalsPlace.EXPIRES_BEFORE_PARAM;
import static org.sagebionetworks.web.client.place.ACTAccessApprovalsPlace.SUBMITTER_ID_PARAM;
import static org.sagebionetworks.web.client.presenter.ACTAccessApprovalsPresenter.HIDE_AR_TEXT;
import static org.sagebionetworks.web.client.presenter.ACTAccessApprovalsPresenter.SHOW_AR_TEXT;
import java.util.Collections;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.dataaccess.AccessorGroup;
import org.sagebionetworks.repo.model.dataaccess.AccessorGroupRequest;
import org.sagebionetworks.repo.model.dataaccess.AccessorGroupResponse;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.ACTAccessApprovalsPlace;
import org.sagebionetworks.web.client.presenter.ACTAccessApprovalsPresenter;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.ACTAccessApprovalsView;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.accessrequirements.AccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.approval.AccessorGroupWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class ACTAccessApprovalsPresenterTest {

	ACTAccessApprovalsPresenter presenter;

	@Mock
	ACTAccessApprovalsPlace mockPlace;
	@Mock
	ACTAccessApprovalsView mockView;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	LoadMoreWidgetContainer mockLoadMoreContainer;
	@Mock
	Button mockShowHideAccessRequirementButton;
	@Mock
	DataAccessClientAsync mockDataAccessClient;
	@Mock
	SynapseSuggestBox mockPeopleSuggestWidget;
	@Mock
	UserGroupSuggestionProvider mockProvider;
	@Mock
	UserBadge mockSelectedUserBadge;
	@Mock
	AccessRequirementWidget mockAccessRequirementWidget;
	@Mock
	GlobalApplicationState mockGlobalAppState;
	@Mock
	AccessorGroupResponse mockAccessorGroupResponse;
	@Captor
	ArgumentCaptor<ClickHandler> clickHandlerCaptor;
	@Captor
	ArgumentCaptor<AccessorGroupRequest> accessorGroupRequestCaptor;
	@Mock
	AccessorGroup mockAccessorGroup;
	@Mock
	AccessorGroupWidget mockAccessorGroupWidget;
	@Mock
	UserGroupSuggestion mockUserGroupSuggestion;
	@Mock
	UserGroupHeader mockUserGroupHeader;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Captor
	ArgumentCaptor<Place> placeCaptor;

	public static final String AR_ID = "765";
	public static final String SUBMITTER_ID = "88888";
	public static final String NEXT_PAGE_TOKEN = "9876789876";
	public static final String USER_ID_SELECTED = "42";

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		presenter = new ACTAccessApprovalsPresenter(mockView, mockSynAlert, mockGinInjector, mockLoadMoreContainer, mockShowHideAccessRequirementButton, mockDataAccessClient, mockPeopleSuggestWidget, mockProvider, mockSelectedUserBadge, mockAccessRequirementWidget, mockGlobalAppState);
		AsyncMockStubber.callSuccessWith(mockAccessorGroupResponse).when(mockDataAccessClient).listAccessorGroup(any(AccessorGroupRequest.class), any(AsyncCallback.class));
		when(mockAccessorGroupResponse.getResults()).thenReturn(Collections.singletonList(mockAccessorGroup));
		when(mockAccessorGroupResponse.getNextPageToken()).thenReturn(NEXT_PAGE_TOKEN);
		when(mockGinInjector.getAccessorGroupWidget()).thenReturn(mockAccessorGroupWidget);
		when(mockUserGroupSuggestion.getHeader()).thenReturn(mockUserGroupHeader);
		when(mockUserGroupHeader.getOwnerId()).thenReturn(USER_ID_SELECTED);
		when(mockGlobalAppState.getPlaceChanger()).thenReturn(mockPlaceChanger);
	}

	@Test
	public void testConstruction() {
		verify(mockPeopleSuggestWidget).setSuggestionProvider(mockProvider);
		verify(mockPeopleSuggestWidget).setTypeFilter(TypeFilter.USERS_ONLY);
		verify(mockShowHideAccessRequirementButton).addClickHandler(clickHandlerCaptor.capture());
		verify(mockView).setAccessRequirementUIVisible(false);
		verify(mockShowHideAccessRequirementButton).setText(SHOW_AR_TEXT);
		clickHandlerCaptor.getValue().onClick(null);
		verify(mockView).setAccessRequirementUIVisible(true);
		verify(mockShowHideAccessRequirementButton).setText(HIDE_AR_TEXT);
		verify(mockView).setSynAlert(mockSynAlert);
		verify(mockView).setLoadMoreContainer(mockLoadMoreContainer);
		verify(mockView).setShowHideButton(mockShowHideAccessRequirementButton);
		verify(mockView).setAccessRequirementWidget(mockAccessRequirementWidget);
		verify(mockView).setUserPickerWidget(any(Widget.class));
		verify(mockView).setSelectedUserBadge(any(Widget.class));
		verify(mockView).setPresenter(presenter);
		verify(mockPeopleSuggestWidget).addItemSelectedHandler(any(CallbackP.class));
		verify(mockLoadMoreContainer).configure(any(Callback.class));
	}

	@Test
	public void testSetPlace() {
		Date now = new Date();
		when(mockPlace.getParam(ACCESS_REQUIREMENT_ID_PARAM)).thenReturn(AR_ID);
		when(mockPlace.getParam(EXPIRES_BEFORE_PARAM)).thenReturn(Long.toString(now.getTime()));
		when(mockPlace.getParam(SUBMITTER_ID_PARAM)).thenReturn(SUBMITTER_ID);

		presenter.setPlace(mockPlace);

		verify(mockSynAlert, atLeast(1)).clear();
		verify(mockView).setExpiresBeforeDate(now);
		// not requesting access to any subject in particular.
		RestrictableObjectDescriptor targetSubject = null;
		verify(mockAccessRequirementWidget).configure(AR_ID, targetSubject);
		verify(mockDataAccessClient).listAccessorGroup(accessorGroupRequestCaptor.capture(), any(AsyncCallback.class));
		AccessorGroupRequest request = accessorGroupRequestCaptor.getValue();
		assertEquals(now, request.getExpireBefore());
		assertEquals(SUBMITTER_ID, request.getSubmitterId());
		assertEquals(AR_ID, request.getAccessRequirementId());
		verify(mockAccessorGroupWidget).configure(mockAccessorGroup);
		assertEquals(NEXT_PAGE_TOKEN, request.getNextPageToken());
	}

	@Test
	public void testClearExpireBefore() {
		Date now = new Date();
		when(mockPlace.getParam(EXPIRES_BEFORE_PARAM)).thenReturn(Long.toString(now.getTime()));
		presenter.setPlace(mockPlace);
		verify(mockView).setExpiresBeforeDate(now);
		reset(mockDataAccessClient);

		presenter.onClearExpireBeforeFilter();

		verify(mockView).setExpiresBeforeDate(null);
		verify(mockPlace).removeParam(EXPIRES_BEFORE_PARAM);
		verify(mockDataAccessClient).listAccessorGroup(accessorGroupRequestCaptor.capture(), any(AsyncCallback.class));
		AccessorGroupRequest request = accessorGroupRequestCaptor.getValue();
		assertNull(request.getExpireBefore());
	}

	@Test
	public void testClearUserFilter() {
		when(mockPlace.getParam(SUBMITTER_ID_PARAM)).thenReturn(SUBMITTER_ID);
		presenter.setPlace(mockPlace);
		reset(mockDataAccessClient);

		presenter.onClearUserFilter();

		verify(mockView).setSelectedUserBadgeVisible(false);
		verify(mockPlace).removeParam(SUBMITTER_ID_PARAM);
		verify(mockDataAccessClient).listAccessorGroup(accessorGroupRequestCaptor.capture(), any(AsyncCallback.class));
		AccessorGroupRequest request = accessorGroupRequestCaptor.getValue();
		assertNull(request.getSubmitterId());
	}

	@Test
	public void testClearAccessRequirementFilter() {
		when(mockPlace.getParam(ACCESS_REQUIREMENT_ID_PARAM)).thenReturn(AR_ID);
		presenter.setPlace(mockPlace);
		reset(mockDataAccessClient);

		presenter.onClearAccessRequirementFilter();

		verify(mockView, times(2)).setAccessRequirementUIVisible(false);
		verify(mockShowHideAccessRequirementButton).setVisible(false);
		verify(mockPlace).removeParam(ACCESS_REQUIREMENT_ID_PARAM);
		verify(mockDataAccessClient).listAccessorGroup(accessorGroupRequestCaptor.capture(), any(AsyncCallback.class));
		AccessorGroupRequest request = accessorGroupRequestCaptor.getValue();
		assertNull(request.getAccessRequirementId());
	}

	@Test
	public void testOnSetExpiresBeforeDateSelected() {
		presenter.setPlace(mockPlace);
		reset(mockDataAccessClient);
		Date now = new Date();

		presenter.onExpiresBeforeDateSelected(now);

		verify(mockDataAccessClient).listAccessorGroup(accessorGroupRequestCaptor.capture(), any(AsyncCallback.class));
		AccessorGroupRequest request = accessorGroupRequestCaptor.getValue();
		assertEquals(now, request.getExpireBefore());
		verify(mockPlace).putParam(EXPIRES_BEFORE_PARAM, Long.toString(now.getTime()));
	}

	@Test
	public void testOnUserSelected() {
		presenter.setPlace(mockPlace);
		reset(mockDataAccessClient);

		presenter.onUserSelected(mockUserGroupSuggestion);

		verify(mockPlace).putParam(SUBMITTER_ID_PARAM, USER_ID_SELECTED);
		verify(mockSelectedUserBadge).configure(USER_ID_SELECTED);
		verify(mockDataAccessClient).listAccessorGroup(accessorGroupRequestCaptor.capture(), any(AsyncCallback.class));
		AccessorGroupRequest request = accessorGroupRequestCaptor.getValue();
		assertEquals(USER_ID_SELECTED, request.getSubmitterId());
	}
}
