package org.sagebionetworks.web.unitclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationStateImpl;
import org.sagebionetworks.web.client.GlobalApplicationStateView;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.StackConfigServiceAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.mvp.AppActivityMapper;
import org.sagebionetworks.web.client.mvp.AppPlaceHistoryMapper;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.footer.VersionState;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GlobalApplicationStateImplTest {
	@Mock
	StackConfigServiceAsync mockStackConfigService;
	CookieProvider mockCookieProvider;
	PlaceController mockPlaceController;
	EventBus mockEventBus;
	GlobalApplicationStateImpl globalApplicationState;
	JiraURLHelper mockJiraURLHelper;
	AppPlaceHistoryMapper mockAppPlaceHistoryMapper;
	SynapseJSNIUtils mockSynapseJSNIUtils;
	GlobalApplicationStateView mockView;
	@Mock
	ClientCache mockLocalStorage;
	@Mock
	GWTWrapper mockGWT;
	@Mock
	DateTimeUtils mockDateTimeUtils;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	SynapseProperties mockSynapseProperties;
	@Captor
	ArgumentCaptor<Callback> synapsePropertiesInitCallbackCaptor;
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		mockCookieProvider = Mockito.mock(CookieProvider.class);
		mockPlaceController = Mockito.mock(PlaceController.class);
		mockEventBus = Mockito.mock(EventBus.class);
		mockJiraURLHelper = Mockito.mock(JiraURLHelper.class);
		mockAppPlaceHistoryMapper = mock(AppPlaceHistoryMapper.class);
		mockSynapseJSNIUtils = mock(SynapseJSNIUtils.class);
		mockView = mock(GlobalApplicationStateView.class);
		AsyncMockStubber.callSuccessWith("v1").when(mockStackConfigService).getSynapseVersions(any(AsyncCallback.class));
		
		globalApplicationState = new GlobalApplicationStateImpl(mockView, mockCookieProvider,mockJiraURLHelper, mockEventBus, mockStackConfigService, mockSynapseJSNIUtils, mockLocalStorage, mockGWT, mockDateTimeUtils, mockJsClient, mockSynapseProperties);
		globalApplicationState.setPlaceController(mockPlaceController);
		globalApplicationState.setAppPlaceHistoryMapper(mockAppPlaceHistoryMapper);
	}
	
	/**
	 * 
	 */
	@Test
	public void testGoToNewPlace(){
		Synapse currenPlace = new Synapse("syn123");
		// Start with the current place 
		when(mockPlaceController.getWhere()).thenReturn(currenPlace);

		PlaceChanger changer = globalApplicationState.getPlaceChanger();
		assertNotNull(changer);
		// 
		Synapse newPlace = new Synapse("syn456");
		changer.goTo(newPlace);
		// Since this is not the current place it should actaully go there.
		verify(mockPlaceController).goTo(newPlace);
	}
	
	@Test
	public void testGoToNewPlaceError(){
		Synapse currentPlace = new Synapse("syn123");
		// Start with the current place 
		when(mockPlaceController.getWhere()).thenReturn(currentPlace);
		PlaceChanger changer = globalApplicationState.getPlaceChanger();
		assertNotNull(changer);
		Synapse newPlace = new Synapse("syn456");
		String errorMessage = "error on goto";
		doThrow(new RuntimeException(errorMessage)).when(mockPlaceController).goTo(any(Place.class));
		changer.goTo(newPlace);
		verify(mockSynapseJSNIUtils).consoleError(errorMessage);
	}

	
	@Test
	public void testUncaughtJSExceptions() {
		Throwable t = new RuntimeException("uncaught");
		globalApplicationState.handleUncaughtException(t);
		verify(mockSynapseJSNIUtils).consoleError(t);
		verify(mockJsClient).logError(eq(t));
	}
	
	@Test
	public void testUnwrap(){
		Throwable actualException = new Exception("I am dead, Horatio");
		Set<Throwable> causes = new LinkedHashSet<Throwable>();
		causes.add(actualException);
		com.google.web.bindery.event.shared.UmbrellaException umbrellaException = new com.google.web.bindery.event.shared.UmbrellaException(causes);
		Set<Throwable> umbrellaUmbrellaCauses = new HashSet<Throwable>();
		umbrellaUmbrellaCauses.add(umbrellaException);
		UmbrellaException umbrellaUmbrellaException = new UmbrellaException(umbrellaUmbrellaCauses);
		
		//single exception being wrapped twice with umbrella exceptions, unwrap it!
		assertEquals(actualException, globalApplicationState.unwrap(umbrellaUmbrellaException));
		//pass through for non-umbrella exceptions
		assertEquals(actualException, globalApplicationState.unwrap(actualException));
		
		//more than one cause, log one of the exceptions
		Throwable secondException = new Exception("Thou livest.");
		causes.add(secondException);
		assertEquals(actualException, globalApplicationState.unwrap(umbrellaException));
	}
	
	@Test
	public void testGoToCurrent(){
		Synapse currenPlace = new Synapse("syn123");
		// Start with the current place 
		when(mockPlaceController.getWhere()).thenReturn(currenPlace);
		PlaceChanger changer = globalApplicationState.getPlaceChanger();
		assertNotNull(changer);
		changer.goTo(currenPlace);
		// Since we are already there then just reload the page by firing an event
		verify(mockEventBus).fireEvent(any(PlaceChangeEvent.class));
	}

	private void verifyAndInvokeSynapsePropertiesInitCallback() {
		verify(mockSynapseProperties).initSynapseProperties(synapsePropertiesInitCallbackCaptor.capture());
		synapsePropertiesInitCallbackCaptor.getValue().invoke();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCheckVersionCompatibility() {
		globalApplicationState.init(new Callback() {
			@Override
			public void invoke() {}
		});
		verifyAndInvokeSynapsePropertiesInitCallback();
		
		AsyncCallback<VersionState> callback = mock(AsyncCallback.class);
		globalApplicationState.checkVersionCompatibility(callback);
		verify(mockStackConfigService).getSynapseVersions(any(AsyncCallback.class));
		verify(mockView, never()).showVersionOutOfDateGlobalMessage();
		//verify callback was given the correct version
		ArgumentCaptor<VersionState> captor = ArgumentCaptor.forClass(VersionState.class);
		verify(callback).onSuccess(captor.capture());
		assertEquals("v1", captor.getValue().getVersion());
		
		// simulate change repo version
		reset(mockStackConfigService);
		AsyncMockStubber.callSuccessWith("v2").when(mockStackConfigService).getSynapseVersions(any(AsyncCallback.class));
		callback = mock(AsyncCallback.class);
		globalApplicationState.checkVersionCompatibility(callback);
		verify(mockStackConfigService).getSynapseVersions(any(AsyncCallback.class));
		verify(mockView).showVersionOutOfDateGlobalMessage();
		//verify callback was given the currently loaded version (not the current version)
		captor = ArgumentCaptor.forClass(VersionState.class);
		verify(callback).onSuccess(captor.capture());
		assertEquals("v1", captor.getValue().getVersion());
		
		AsyncMockStubber.callSuccessWith("v3").when(mockStackConfigService).getSynapseVersions(any(AsyncCallback.class));
		globalApplicationState.checkVersionCompatibility(callback);
		//showVersionOutOfDateGlobalMessage() has still only been called once, despite detecting another version change
		verify(mockView).showVersionOutOfDateGlobalMessage();
		
	}
	
	@Test
	public void testCheckVersionCompatibilityFailure() {
		Exception ex = new Exception("couldn't get version");
		AsyncMockStubber.callFailureWith(ex).when(mockStackConfigService).getSynapseVersions(any(AsyncCallback.class));
		globalApplicationState.init(() -> {});
		verifyAndInvokeSynapsePropertiesInitCallback();
		AsyncCallback<VersionState> callback = mock(AsyncCallback.class);
		globalApplicationState.checkVersionCompatibility(callback);
		verify(mockStackConfigService).getSynapseVersions(any(AsyncCallback.class));
		verify(mockView).showGetVersionError(ex.getMessage());
		verify(callback).onFailure(ex);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCheckVersionCompatibilityCheckedRecently() {
		when(mockLocalStorage.get(GlobalApplicationStateImpl.RECENTLY_CHECKED_SYNAPSE_VERSION)).thenReturn(Boolean.TRUE.toString());
		AsyncCallback<VersionState> callback = mock(AsyncCallback.class);
		globalApplicationState.init(() -> {});
		globalApplicationState.checkVersionCompatibility(mock(AsyncCallback.class));
		verifyAndInvokeSynapsePropertiesInitCallback();
		reset(mockStackConfigService);
		globalApplicationState.checkVersionCompatibility(callback);
		
		verify(mockStackConfigService, never()).getSynapseVersions(any(AsyncCallback.class));
		verify(mockView, never()).showVersionOutOfDateGlobalMessage();
		//verify callback was given the correct version
		ArgumentCaptor<VersionState> captor = ArgumentCaptor.forClass(VersionState.class);
		verify(callback).onSuccess(captor.capture());
		assertEquals("v1", captor.getValue().getVersion());
	}
	
	@Test
	public void testInitSynapseProperties() {
		Callback mockCallback = mock(Callback.class);
		globalApplicationState.init(mockCallback);
		
		verify(mockCallback, never()).invoke();
		verifyAndInvokeSynapsePropertiesInitCallback();
		
		verify(mockView).initGlobalViewProperties();
		verify(mockCallback).invoke();
	}
	
	@Test
	public void testGetLastPlaceWhenSet() {
		//history value is set in the cookies
		when(mockCookieProvider.getCookie(CookieKeys.LAST_PLACE)).thenReturn("a history value");
		//and the history value resolves to a place
		Place mockPlace = mock(Place.class);
		when(mockAppPlaceHistoryMapper.getPlace(anyString())).thenReturn(mockPlace);
		
		Place lastPlace = globalApplicationState.getLastPlace();
		assertEquals(mockPlace, lastPlace);
	}
	
	@Test
	public void testGetLastPlaceDefaultPlace() {
		//next line is not really necessary, but to make this explicit
		when(mockCookieProvider.getCookie(CookieKeys.LAST_PLACE)).thenReturn(null);
		//and the history value resolves to a place
		Place mockDefaultPlace = mock(Place.class);
		Place returnedPlace = globalApplicationState.getLastPlace(mockDefaultPlace);
		assertEquals(mockDefaultPlace, returnedPlace);
	}
	
	@Test
	public void testGetLastPlaceNullDefault() {
		//next line is not really necessary, but to make this explicit
		when(mockCookieProvider.getCookie(CookieKeys.LAST_PLACE)).thenReturn(null);
		//and the history value resolves to a place
		Place returnedPlace = globalApplicationState.getLastPlace(null);
		assertEquals(AppActivityMapper.getDefaultPlace(), returnedPlace);
	}
	
	@Test
	public void testClearLastPlace() {
		globalApplicationState.clearLastPlace();
		verify(mockCookieProvider).removeCookie(CookieKeys.LAST_PLACE);
	}
	
	@Test
	public void testPushCurrentPlace(){
		String newToken = "/some/new/token";
		Place mockPlace = mock(Place.class);
		when(mockAppPlaceHistoryMapper.getToken(mockPlace)).thenReturn(newToken);
		globalApplicationState.pushCurrentPlace(mockPlace);
		//should have set the last place (to the current), and the current place (as requested)
		verify(mockCookieProvider).setCookie(eq(CookieKeys.LAST_PLACE), anyString(), any(Date.class));
		verify(mockGWT).newItem(newToken, false);
		
		//if I push the same place again, it should not push the history state again
		when(mockAppPlaceHistoryMapper.getPlace(anyString())).thenReturn(mockPlace);
		globalApplicationState.pushCurrentPlace(mockPlace);
		//verify that these were still only called once
		verify(mockCookieProvider).setCookie(eq(CookieKeys.LAST_PLACE), anyString(), any(Date.class));
		verify(mockGWT).newItem(newToken, false);
	}
	
	@Test
	public void testReplaceCurrentPlace(){
		String newToken = "/some/new/token";
		Place mockPlace = mock(Place.class);
		when(mockAppPlaceHistoryMapper.getToken(mockPlace)).thenReturn(newToken);
		globalApplicationState.replaceCurrentPlace(mockPlace);
		//should have set the last place (to the current), and the current place (as requested)
		verify(mockCookieProvider).setCookie(eq(CookieKeys.LAST_PLACE), anyString(), any(Date.class));
		verify(mockGWT).replaceItem(newToken, false);
		
		//if I push the same place again, it should not push the history state again
		when(mockAppPlaceHistoryMapper.getPlace(anyString())).thenReturn(mockPlace);
		globalApplicationState.replaceCurrentPlace(mockPlace);
		//verify that these were still only called once
		verify(mockCookieProvider).setCookie(eq(CookieKeys.LAST_PLACE), anyString(), any(Date.class));
		verify(mockGWT).replaceItem(newToken, false);
	}
	
	@Test
	public void testReplaceCurrentPlaceFailure(){
		String error = "failure to push current place!";
		doThrow(new RuntimeException(error)).when(mockGWT).replaceItem(anyString(), anyBoolean());
		String newToken = "/some/new/token";
		Place mockPlace = mock(Place.class);
		when(mockAppPlaceHistoryMapper.getToken(mockPlace)).thenReturn(newToken);
		globalApplicationState.replaceCurrentPlace(mockPlace);
		verify(mockGWT).replaceItem(newToken, false);
		verify(mockSynapseJSNIUtils).consoleError(error);
	}

	@Test
	public void testInitOnPopStateHandler() {
		globalApplicationState.initOnPopStateHandler();
		verify(mockSynapseJSNIUtils).initOnPopStateHandler();
	}
	
	@Test
	public void testRefreshPage() {
		Synapse place = new Synapse("syn1234");
		// Start with the current place 
		when(mockPlaceController.getWhere()).thenReturn(place);

		Place mockPlace = mock(Place.class);
		String historyToken = "!Synapse:syn123";
		String currentUrl = "https://www.synapse.org/#"+historyToken;
		when(mockSynapseJSNIUtils.getCurrentURL()).thenReturn(currentUrl);
		when(mockAppPlaceHistoryMapper.getPlace(historyToken)).thenReturn(mockPlace);
		
		globalApplicationState.refreshPage();
		verify(mockPlaceController).goTo(mockPlace);
		
		reset(mockPlaceController);
		when(mockPlaceController.getWhere()).thenReturn(place);
		historyToken = "!Synapse:syn123/wiki/12345";
		currentUrl = "https://www.synapse.org/#"+historyToken;
		when(mockSynapseJSNIUtils.getCurrentURL()).thenReturn(currentUrl);
		when(mockAppPlaceHistoryMapper.getPlace(historyToken)).thenReturn(mockPlace);
	
		globalApplicationState.refreshPage();
		verify(mockPlaceController).goTo(mockPlace);
		
		reset(mockPlaceController, mockAppPlaceHistoryMapper);
		when(mockPlaceController.getWhere()).thenReturn(place);
		historyToken = "";
		currentUrl = "https://www.synapse.org/"+historyToken;
		when(mockSynapseJSNIUtils.getCurrentURL()).thenReturn(currentUrl);
		globalApplicationState.refreshPage();
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(mockAppPlaceHistoryMapper).getPlace(captor.capture());
		assertEquals(GlobalApplicationStateImpl.DEFAULT_REFRESH_PLACE, captor.getValue());
	}
	
	@Test
	public void testSetShowLocalTime() {
		globalApplicationState.setShowUTCTime(false);
		verify(mockDateTimeUtils).setShowUTCTime(false);
		verify(mockCookieProvider).setCookie(eq(CookieKeys.SHOW_DATETIME_IN_UTC), eq(Boolean.FALSE.toString()), any(Date.class));
	}
	
	@Test
	public void testIsShowingUTCTime() {
		when(mockDateTimeUtils.isShowingUTCTime()).thenReturn(false);
		assertFalse(globalApplicationState.isShowingUTCTime());
		verify(mockDateTimeUtils).isShowingUTCTime();
	}
	
	@Test
	public void testSetShowUTCTime() {
		globalApplicationState.setShowUTCTime(true);
		verify(mockDateTimeUtils).setShowUTCTime(true);
		verify(mockCookieProvider).setCookie(eq(CookieKeys.SHOW_DATETIME_IN_UTC), eq(Boolean.TRUE.toString()), any(Date.class));
	}
}
