package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.PeopleSearch;
import org.sagebionetworks.web.client.presenter.PeopleSearchPresenter;
import org.sagebionetworks.web.client.presenter.TeamSearchPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.PeopleSearchView;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class PeopleSearchPresenterTest {
	
	PeopleSearchPresenter presenter;
	PeopleSearchView mockView;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	SynapseClientAsync mockSynapse;
	CookieProvider mockCookies;
	UserGroupHeaderResponsePage peopleList = getTestPeople();
	
	@Before
	public void setup() throws JSONObjectAdapterException{
		mockView = mock(PeopleSearchView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapse = mock(SynapseClientAsync.class);
		mockCookies = mock(CookieProvider.class);
		presenter = new PeopleSearchPresenter(mockView, mockSynapse, mockAuthenticationController, mockGlobalApplicationState);
		
		AsyncMockStubber.callSuccessWith(peopleList).when(mockSynapse).getUserGroupHeadersByPrefix(
				anyString(), anyLong(), anyLong(), any(AsyncCallback.class));
		
		verify(mockView).setPresenter(presenter);
	}	
	
	@Test
	public void testSetPlace() {
		PeopleSearch place = Mockito.mock(PeopleSearch.class);
		presenter.setPlace(place);
	}
	
	@Test
	public void testSearch() throws RestServiceException {
		presenter.search("test", null);
		verify(mockView).configure(anyList(), anyString());
	}
	
	@Test
	public void testSearchFailure() throws RestServiceException {
		AsyncMockStubber.callFailureWith(new Exception("unhandled exception")).when(mockSynapse).getUserGroupHeadersByPrefix(
				anyString(), anyLong(), anyLong(), any(AsyncCallback.class));
		presenter.search("test", null);
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testCanPublicJoin() throws RestServiceException {
		//can public join is interpretted as false if null
		Team team = new Team();
		team.setCanPublicJoin(null);
		assertFalse(TeamSearchPresenter.getCanPublicJoin(team));
		team.setCanPublicJoin(false);
		assertFalse(TeamSearchPresenter.getCanPublicJoin(team));

		team.setCanPublicJoin(true);
		assertTrue(TeamSearchPresenter.getCanPublicJoin(team));
	}
	
	private static UserGroupHeaderResponsePage getTestPeople() {
		UserGroupHeaderResponsePage people = new UserGroupHeaderResponsePage();
		List<UserGroupHeader> peopleList = new ArrayList<UserGroupHeader>();
		UserGroupHeader header = new UserGroupHeader();
		header.setOwnerId("2112");
		header.setFirstName("Geddy");
		header.setLastName("Lee");
		peopleList.add(header);
		header = new UserGroupHeader();
		header.setOwnerId("1221");
		header.setFirstName("Alex");
		header.setLastName("Lifeson");
		peopleList.add(header);
		header = new UserGroupHeader();
		header.setOwnerId("1212");
		header.setFirstName("Neil");
		header.setLastName("Peart");
		peopleList.add(header);
		people.setChildren(peopleList);
		people.setTotalNumberOfResults((long) peopleList.size());
		return people;
	}

}