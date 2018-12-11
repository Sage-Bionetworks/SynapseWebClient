package org.sagebionetworks.web.unitclient.widget.header;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.header.Header.ANONYMOUS;
import static org.sagebionetworks.web.client.widget.header.Header.N_A;
import static org.sagebionetworks.web.client.widget.header.Header.SYNAPSE_ORG;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.file.DownloadList;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.DownloadListUpdatedEvent;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Trash;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidget;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.header.HeaderView;
import org.sagebionetworks.web.client.widget.pendo.PendoSdk;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.junit.GWTMockUtilities;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.binder.EventBinder;

public class HeaderTest {

	Header header;
	@Mock
	HeaderView mockView;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	FavoriteWidget mockFavWidget;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	List<EntityHeader> entityHeaders;
	@Mock
	CookieProvider mockCookies;
	@Mock
	PendoSdk mockPendoSdk;
	@Mock
	UserProfile mockUserProfile;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	PortalGinInjector mockPortalGinInjector;
	@Mock
	EventBus mockEventBus;
	@Mock
	EventBinder<Header> mockEventBinder;
	@Mock
	DownloadList mockDownloadList;
	List<FileHandleAssociation> downloadListFhas;
	@Mock
	FileHandleAssociation mockFha1;
	@Mock
	FileHandleAssociation mockFha2;
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		when(mockView.getEventBinder()).thenReturn(mockEventBinder);
		GWTMockUtilities.disarm();
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		//by default, mock that we are on the production website
		when(mockSynapseJSNIUtils.getCurrentHostName()).thenReturn(Header.WWW_SYNAPSE_ORG);
		header = new Header(mockView, mockAuthenticationController, mockGlobalApplicationState, mockSynapseJavascriptClient, mockFavWidget, mockSynapseJSNIUtils, mockPendoSdk, mockPortalGinInjector, mockEventBus);
		entityHeaders = new ArrayList<EntityHeader>();
		AsyncMockStubber.callSuccessWith(entityHeaders).when(mockSynapseJavascriptClient).getFavorites(any(AsyncCallback.class));
		when(mockGlobalApplicationState.getFavorites()).thenReturn(entityHeaders);
		downloadListFhas = new ArrayList<>();
		when(mockDownloadList.getFilesToDownload()).thenReturn(downloadListFhas);
		AsyncMockStubber.callSuccessWith(mockDownloadList).when(mockSynapseJavascriptClient).getDownloadList(any(AsyncCallback.class));
	}
	@After
	public void tearDown() {
		GWTMockUtilities.restore();
	}

	@Test
	public void testSetPresenter() {
		verify(mockView).setPresenter(header);
		verify(mockView).setStagingAlertVisible(false);
	}

	@Test
	public void testAsWidget(){
		header.asWidget();
	}

	@Test
	public void testOnTrashClick() {
		header.onTrashClick();
		verify(mockPlaceChanger).goTo(isA(Trash.class));
	}

	@Test
	public void testOnLogoutClick() {
		header.onLogoutClick();
		verify(mockGlobalApplicationState).clearLastPlace();
		ArgumentCaptor<Place> captor = ArgumentCaptor.forClass(Place.class);
		verify(mockPlaceChanger).goTo(captor.capture());
		Place place = captor.getValue();
		assertTrue(place instanceof LoginPlace);
		assertEquals(LoginPlace.LOGOUT_TOKEN, ((LoginPlace)place).toToken());
	}

	@Test
	public void testOnLoginClick() {
		header.onLoginClick();
		ArgumentCaptor<Place> captor = ArgumentCaptor.forClass(Place.class);
		verify(mockPlaceChanger).goTo(captor.capture());
		Place place = captor.getValue();
		assertTrue(place instanceof LoginPlace);
		assertEquals(LoginPlace.LOGIN_TOKEN, ((LoginPlace)place).toToken());
	}
	
	@Test
	public void testOnLogoClick() {
		header.onLogoClick();
		ArgumentCaptor<Place> captor = ArgumentCaptor.forClass(Place.class);
		verify(mockPlaceChanger).goTo(captor.capture());
		Place place = captor.getValue();
		assertTrue(place instanceof Home);
	}

	@Test
	public void testOnFavoriteClickEmptyCase() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		header.refreshFavorites();
		verify(mockView).clearFavorite();
		verify(mockView).setEmptyFavorite();
	}

	@Test
	public void testOnFavoriteClickNonEmptyCase() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		EntityHeader entityHeader1 = new EntityHeader();
		entityHeader1.setId("syn012345");
		EntityHeader entityHeader2 = new EntityHeader();
		entityHeader2.setId("syn012345");
		entityHeaders.add(entityHeader1);
		entityHeaders.add(entityHeader2);
		header.refreshFavorites();
		verify(mockView).clearFavorite();
		verify(mockView).addFavorite(entityHeaders);
	}

	@Test
	public void testFavoriteRoundTrip() {
		// After User Logged in
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserProfile()).thenReturn(mockUserProfile);
		header.refreshFavorites();
		verify(mockView).clearFavorite();
		verify(mockSynapseJavascriptClient, times(1)).getFavorites(any(AsyncCallback.class));
		//initially empty
		verify(mockView).setEmptyFavorite();
		
		//say the user set something as a favorite
		EntityHeader entityHeader1 = new EntityHeader();
		entityHeader1.setId("syn012345");
		entityHeaders.add(entityHeader1);
		
		// User should ask for favorites each time favorites button is clicked
		header.refreshFavorites();
		verify(mockView, times(2)).clearFavorite();
		verify(mockSynapseJavascriptClient, times(2)).getFavorites(any(AsyncCallback.class));
		verify(mockView).addFavorite(entityHeaders);
	}
	
	@Test
	public void testFavoriteAnonymous() {
		// SWC-2805: User is not logged in.  This is an odd case, since the favorites menu should not be shown.
		// in this case, do not even try to update the favorites, will show whatever we had (possibly stale, like the rest of the page).
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		header.refreshFavorites();
		verify(mockView, never()).clearFavorite();
		verify(mockSynapseJavascriptClient, never()).getFavorites(any(AsyncCallback.class));
	}
	
	@Test
	public void testInitStagingAlert() {
		//case insensitive
		Mockito.reset(mockView);
		when(mockSynapseJSNIUtils.getCurrentHostName()).thenReturn("WwW.SynapsE.ORG");
		header.initStagingAlert();
		verify(mockView).setStagingAlertVisible(false);

		//staging
		Mockito.reset(mockView);
		when(mockSynapseJSNIUtils.getCurrentHostName()).thenReturn("staging.synapse.org");
		header.initStagingAlert();
		verify(mockView).setStagingAlertVisible(true);

		//local
		Mockito.reset(mockView);
		when(mockSynapseJSNIUtils.getCurrentHostName()).thenReturn("localhost");
		header.initStagingAlert();
		verify(mockView).setStagingAlertVisible(true);
	}
	
	@Test
	public void testRefresh() {
		String userId = "10001";
		String userName = "testuser";
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserProfile()).thenReturn(mockUserProfile);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(userId);
		when(mockUserProfile.getUserName()).thenReturn(userName);
		
		header.refresh();
		
		verify(mockView).setUser(mockUserProfile);
		verify(mockView).refresh();
		verify(mockView).setSearchVisible(true);
		
		verify(mockPendoSdk).initialize(userId, userName + SYNAPSE_ORG);
	}
	

	@Test
	public void testRefreshAnonymous() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		
		header.refresh();
		
		verify(mockView).setUser(null);
		verify(mockView).refresh();
		verify(mockView).setSearchVisible(true);
		verify(mockPendoSdk).initialize(ANONYMOUS, N_A);
	}
	
	@Test
	public void testRefreshNullProfile() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		String userId = "10001";
		String userName = "testuser";
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserProfile()).thenReturn(null);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(userId);
		when(mockUserProfile.getUserName()).thenReturn(userName);
		
		header.refresh();
		
		verify(mockView).setUser(null);
		verify(mockView).refresh();
		verify(mockView).setSearchVisible(true);
		verify(mockPendoSdk).initialize(ANONYMOUS, N_A);
	}
	
	@Test
	public void testOnDownloadListUpdatedEvent() {
		header.onDownloadListUpdatedEvent(new DownloadListUpdatedEvent());

		verify(mockSynapseJavascriptClient).getDownloadList(any(AsyncCallback.class));
		verify(mockView).setDownloadListUIVisible(false);
		verify(mockView).setDownloadListFileCount(0);
		
		//add a file to the download list, and fire an update event
		downloadListFhas.add(mockFha1);
		header.onDownloadListUpdatedEvent(new DownloadListUpdatedEvent());

		verify(mockView).setDownloadListUIVisible(true);
		verify(mockView).setDownloadListFileCount(1);

		downloadListFhas.add(mockFha2);
		header.onDownloadListUpdatedEvent(new DownloadListUpdatedEvent());

		verify(mockView, times(2)).setDownloadListUIVisible(true);
		verify(mockView).setDownloadListFileCount(2);
	}
	
	@Test
	public void testOnDownloadListUpdatedFailure() {
		AsyncMockStubber.callFailureWith(new Exception("error")).when(mockSynapseJavascriptClient).getDownloadList(any(AsyncCallback.class));
		header.onDownloadListUpdatedEvent(new DownloadListUpdatedEvent());

		verify(mockSynapseJavascriptClient).getDownloadList(any(AsyncCallback.class));
		verify(mockView).setDownloadListUIVisible(false);
		verify(mockSynapseJSNIUtils).consoleError(anyString());
	}
}
