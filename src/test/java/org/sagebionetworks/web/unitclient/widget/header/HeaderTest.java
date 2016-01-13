package org.sagebionetworks.web.unitclient.widget.header;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Trash;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidget;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.header.HeaderView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class HeaderTest {

	Header header;
	HeaderView mockView;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	SynapseClientAsync mockSynapseClient;
	SynapseJSNIUtils mockSynapseJSNIUtils;
	PlaceChanger mockPlaceChanger;
	FavoriteWidget mockFavWidget;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	List<EntityHeader> entityHeaders;

	@Before
	public void setup(){
		mockView = Mockito.mock(HeaderView.class);		
		mockAuthenticationController = Mockito.mock(AuthenticationController.class);
		mockGlobalApplicationState = Mockito.mock(GlobalApplicationState.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockFavWidget = mock(FavoriteWidget.class);
		mockSynapseJSNIUtils = mock(SynapseJSNIUtils.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		//by default, mock that we are on the production website
		when(mockSynapseJSNIUtils.getCurrentHostName()).thenReturn(Header.WWW_SYNAPSE_ORG);
		header = new Header(mockView, mockAuthenticationController, mockGlobalApplicationState, mockSynapseClient, mockFavWidget, mockSynapseJSNIUtils);
		entityHeaders = new ArrayList<EntityHeader>();
		AsyncMockStubber.callSuccessWith(entityHeaders).when(mockSynapseClient).getFavorites(any(AsyncCallback.class));
		when(mockGlobalApplicationState.getFavorites()).thenReturn(entityHeaders);
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
	public void testOnDashboardClickLoggedIn() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn("008");
		header.onDashboardClick();
		verify(mockPlaceChanger).goTo(any(Profile.class));
	}

	@Test
	public void testOnDashboardClickAnonymous() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		header.onDashboardClick();
		verify(mockPlaceChanger).goTo(any(LoginPlace.class));
	}

	@Test
	public void testOnTrashClick() {
		header.onTrashClick();
		verify(mockPlaceChanger).goTo(any(Trash.class));
	}

	@Test
	public void testOnLogoutClick() {
		header.onLogoutClick();
		verify(mockGlobalApplicationState).clearCurrentPlace();
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
	public void testOnRegisterClick() {
		header.onRegisterClick();
		verify(mockPlaceChanger).goTo(any(RegisterAccount.class));
	}

	@Test
	public void testOnFavoriteClickEmptyCase() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		header.onFavoriteClick();
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
		header.onFavoriteClick();
		verify(mockView).clearFavorite();
		verify(mockView).addFavorite(entityHeaders);
	}

	@Test
	public void testFavoriteRoundTrip() {
		// After User Logged in
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		UserSessionData userSessionData = new UserSessionData();
		when(mockAuthenticationController.getCurrentUserSessionData()).thenReturn(userSessionData);
		header.onFavoriteClick();
		verify(mockView).showFavoritesLoading();
		verify(mockView).clearFavorite();
		verify(mockSynapseClient, times(1)).getFavorites(any(AsyncCallback.class));
		//initially empty
		verify(mockView).setEmptyFavorite();
		
		//say the user set something as a favorite
		EntityHeader entityHeader1 = new EntityHeader();
		entityHeader1.setId("syn012345");
		entityHeaders.add(entityHeader1);
		
		// User should ask for favorites each time favorites button is clicked
		header.onFavoriteClick();
		verify(mockView, times(2)).showFavoritesLoading();
		verify(mockView, times(2)).clearFavorite();
		verify(mockSynapseClient, times(2)).getFavorites(any(AsyncCallback.class));
		verify(mockView).addFavorite(entityHeaders);
	}
	
	@Test
	public void testFavoriteAnonymous() {
		// SWC-2805: User is not logged in.  This is an odd case, since the favorites menu should not be shown.
		// in this case, do not even try to update the favorites, will show whatever we had (possibly stale, like the rest of the page).
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		header.onFavoriteClick();
		verify(mockView, never()).showFavoritesLoading();
		verify(mockView, never()).clearFavorite();
		verify(mockSynapseClient, never()).getFavorites(any(AsyncCallback.class));
	}
	
	@Test
	public void testShowLargeLogo() {
		header.configure(true);
		verify(mockView).showLargeLogo();
		verify(mockView, never()).showSmallLogo();
	}
	@Test
	public void testShowSmallLogo() {
		header.configure(false);
		verify(mockView, never()).showLargeLogo();
		verify(mockView).showSmallLogo();
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
}
