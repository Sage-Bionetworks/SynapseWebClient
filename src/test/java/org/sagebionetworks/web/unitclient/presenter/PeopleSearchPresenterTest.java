package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.PeopleSearch;
import org.sagebionetworks.web.client.presenter.PeopleSearchPresenter;
import org.sagebionetworks.web.client.view.PeopleSearchView;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class PeopleSearchPresenterTest {

	PeopleSearchPresenter presenter;
	@Mock
	PeopleSearchView mockView;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	CookieProvider mockCookies;
	@Mock
	SynapseAlert mockSynAlert;
	UserGroupHeaderResponsePage peopleList = getTestPeople();
	@Mock
	LoadMoreWidgetContainer mockLoadMoreWidgetContainer;
	@Mock
	PortalGinInjector mockPortalGinInjector;
	@Mock
	PeopleSearch mockPlace;
	@Mock
	UserBadge mockUserBadge;

	@Before
	public void setup() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		presenter = new PeopleSearchPresenter(mockView, mockGlobalApplicationState, mockSynAlert, mockLoadMoreWidgetContainer, mockPortalGinInjector, mockSynapseJavascriptClient);
		AsyncMockStubber.callSuccessWith(peopleList).when(mockSynapseJavascriptClient).getUserGroupHeadersByPrefix(anyString(), any(TypeFilter.class), anyLong(), anyLong(), any(AsyncCallback.class));

		verify(mockView).setPresenter(presenter);
		when(mockPortalGinInjector.getUserBadgeWidget()).thenReturn(mockUserBadge);
	}

	@Test
	public void testSearch() throws RestServiceException {
		String searchTerm = "test";
		when(mockPlace.getSearchTerm()).thenReturn(searchTerm);
		presenter.setPlace(mockPlace);
		verify(mockSynapseJavascriptClient).getUserGroupHeadersByPrefix(eq(searchTerm), eq(TypeFilter.USERS_ONLY), anyLong(), anyLong(), any(AsyncCallback.class));
		verify(mockView).setSearchTerm(searchTerm);
		verify(mockPortalGinInjector, times(3)).getUserBadgeWidget();
		verify(mockLoadMoreWidgetContainer, times(3)).add(any(Widget.class));
	}

	@Test
	public void testSearchFailure() throws RestServiceException {
		Exception caught = new Exception("unhandled exception");
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseJavascriptClient).getUserGroupHeadersByPrefix(anyString(), any(TypeFilter.class), anyLong(), anyLong(), any(AsyncCallback.class));
		presenter.setPlace(mockPlace);
		verify(mockSynAlert).handleException(caught);
	}

	private static UserGroupHeaderResponsePage getTestPeople() {
		UserGroupHeaderResponsePage people = new UserGroupHeaderResponsePage();
		List<UserGroupHeader> peopleList = new ArrayList<UserGroupHeader>();
		UserGroupHeader header = new UserGroupHeader();
		header.setIsIndividual(true);
		header.setOwnerId("2112");
		header.setFirstName("Geddy");
		header.setLastName("Lee");
		peopleList.add(header);
		header = new UserGroupHeader();
		header.setIsIndividual(true);
		header.setOwnerId("1221");
		header.setFirstName("Alex");
		header.setLastName("Lifeson");
		peopleList.add(header);
		header = new UserGroupHeader();
		header.setIsIndividual(true);
		header.setOwnerId("1212");
		header.setFirstName("Neil");
		header.setLastName("Peart");
		peopleList.add(header);
		header = new UserGroupHeader();
		header.setIsIndividual(false);
		header.setOwnerId("1212");
		header.setDisplayName("team gryffindor");
		peopleList.add(header);
		people.setChildren(peopleList);
		people.setTotalNumberOfResults((long) peopleList.size());
		return people;
	}

}
