package org.sagebionetworks.web.unitclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.ClientLogger;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationStateImpl;
import org.sagebionetworks.web.client.GlobalApplicationStateView;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
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
	
	SynapseClientAsync mockSynapseClient;
	CookieProvider mockCookieProvider;
	PlaceController mockPlaceController;
	EventBus mockEventBus;
	GlobalApplicationStateImpl globalApplicationState;
	JiraURLHelper mockJiraURLHelper;
	AppPlaceHistoryMapper mockAppPlaceHistoryMapper;
	SynapseJSNIUtils mockSynapseJSNIUtils;
	ClientLogger mockLogger;
	GlobalApplicationStateView mockView;
	HashMap<String, String> testProps;
	@Mock
	ClientCache mockLocalStorage;
	@Mock
	GWTWrapper mockGWT;
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		mockCookieProvider = Mockito.mock(CookieProvider.class);
		mockPlaceController = Mockito.mock(PlaceController.class);
		mockEventBus = Mockito.mock(EventBus.class);
		mockJiraURLHelper = Mockito.mock(JiraURLHelper.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockAppPlaceHistoryMapper = mock(AppPlaceHistoryMapper.class);
		mockSynapseJSNIUtils = mock(SynapseJSNIUtils.class);
		mockLogger = mock(ClientLogger.class);
		mockView = mock(GlobalApplicationStateView.class);
		AsyncMockStubber.callSuccessWith("v1").when(mockSynapseClient).getSynapseVersions(any(AsyncCallback.class));
		testProps = new HashMap<String, String>();
		AsyncMockStubber.callSuccessWith(testProps).when(mockSynapseClient).getSynapseProperties(any(AsyncCallback.class));
		
		globalApplicationState = new GlobalApplicationStateImpl(mockView, mockCookieProvider,mockJiraURLHelper, mockEventBus, mockSynapseClient, mockSynapseJSNIUtils, mockLogger, mockLocalStorage, mockGWT);
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
	public void testUncaughtJSExceptions() {
		Throwable t = new RuntimeException("uncaught");
		globalApplicationState.handleUncaughtException(t);
		verify(mockSynapseJSNIUtils).consoleError(anyString());
		verify(mockLogger).errorToRepositoryServices(anyString(), eq(t));
	}
	
	@Test
	public void testUncaughtJSExceptionsFailedServiceLog() {
		Throwable t = new RuntimeException("uncaught");
		//when we try to log the error to the repository services, 
		//make sure we still send the error to the console, 
		//and that calling does not throw any other uncaught exception.
		doThrow(new NullPointerException()).when(mockLogger).errorToRepositoryServices(anyString(), any(Throwable.class));
		globalApplicationState.handleUncaughtException(t);
		verify(mockLogger).errorToRepositoryServices(anyString(), eq(t));
		//called twice.  Once for the uncaught exception, and once to inform that the repo call method failed
		verify(mockSynapseJSNIUtils, times(2)).consoleError(anyString());
	}
	
	@Test
	public void testUnwrap(){
		Throwable actualException = new Exception("I am dead, Horatio");
		Set<Throwable> causes = new HashSet<Throwable>();
		causes.add(actualException);
		UmbrellaException umbrellaException = new UmbrellaException(causes);
		
		//single exception being wrapped, unwrap it!
		assertEquals(actualException, globalApplicationState.unwrap(umbrellaException));
		//pass through for non-umbrella exceptions
		assertEquals(actualException, globalApplicationState.unwrap(actualException));
		
		//more than one cause, just log the umbrella exception
		causes.add(new Exception("more than one"));
		assertEquals(umbrellaException, globalApplicationState.unwrap(umbrellaException));
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

	@SuppressWarnings("unchecked")
	@Test
	public void testCheckVersionCompatibility() {
		globalApplicationState.initSynapseProperties(new Callback() {
			@Override
			public void invoke() {}
		});
		AsyncCallback<VersionState> callback = mock(AsyncCallback.class);
		globalApplicationState.checkVersionCompatibility(callback);
		verify(mockSynapseClient, times(2)).getSynapseVersions(any(AsyncCallback.class));
		verify(mockView, never()).showVersionOutOfDateGlobalMessage();
		//verify callback was given the correct version
		ArgumentCaptor<VersionState> captor = ArgumentCaptor.forClass(VersionState.class);
		verify(callback).onSuccess(captor.capture());
		assertEquals("v1", captor.getValue().getVersion());
		
		// simulate change repo version
		reset(mockSynapseClient);
		AsyncMockStubber.callSuccessWith("v2").when(mockSynapseClient).getSynapseVersions(any(AsyncCallback.class));
		callback = mock(AsyncCallback.class);
		globalApplicationState.checkVersionCompatibility(callback);
		verify(mockSynapseClient).getSynapseVersions(any(AsyncCallback.class));
		verify(mockView).showVersionOutOfDateGlobalMessage();
		//verify callback was given the currently loaded version (not the current version)
		captor = ArgumentCaptor.forClass(VersionState.class);
		verify(callback).onSuccess(captor.capture());
		assertEquals("v1", captor.getValue().getVersion());
	}
	
	@Test
	public void testInitSynapseProperties() {
		String key = "k1";
		String value = "v1";
		testProps.put(key, value);
		Callback mockCallback = mock(Callback.class);
		globalApplicationState.initSynapseProperties(mockCallback);
		
		verify(mockView).initGlobalViewProperties();
		verify(mockSynapseClient).getSynapseProperties(any(AsyncCallback.class));
		verify(mockLocalStorage).put(eq(key), eq(value), anyLong());
		verify(mockLocalStorage).put(eq(GlobalApplicationStateImpl.PROPERTIES_LOADED_KEY), eq(Boolean.TRUE.toString()), anyLong());
		when(mockLocalStorage.get(key)).thenReturn(value);
		assertEquals(value, globalApplicationState.getSynapseProperty(key));
		assertNull(globalApplicationState.getSynapseProperty("foo"));
		//also sets synapse versions on app load
		verify(mockSynapseClient).getSynapseVersions(any(AsyncCallback.class));
		assertEquals("v1", globalApplicationState.getSynapseVersion());
		verify(mockCallback).invoke();
	}
	
	@Test
	public void testSynapsePropertiesCached() {
		Callback mockCallback = mock(Callback.class);
		when(mockLocalStorage.get(GlobalApplicationStateImpl.PROPERTIES_LOADED_KEY)).thenReturn(Boolean.TRUE.toString());
		globalApplicationState.initSynapseProperties(mockCallback);
		
		verify(mockSynapseClient, never()).getSynapseProperties(any(AsyncCallback.class));
		verify(mockSynapseClient).getSynapseVersions(any(AsyncCallback.class));
		assertEquals("v1", globalApplicationState.getSynapseVersion());
		verify(mockCallback).invoke();
		
		ArgumentCaptor<Callback> deferredCallback = ArgumentCaptor.forClass(Callback.class);
		verify(mockGWT).scheduleDeferred(deferredCallback.capture());
		
		//invoke this deferred callback
		String key = "k1";
		String value = "bar";
		testProps.put(key, value);
		deferredCallback.getValue().invoke();
		//local synapse properties are updated
		verify(mockSynapseClient).getSynapseProperties(any(AsyncCallback.class));
		verify(mockLocalStorage).put(eq(key), eq(value), anyLong());
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
	public void testClearCurrentPlace() {
		globalApplicationState.clearCurrentPlace();
		verify(mockCookieProvider).removeCookie(CookieKeys.CURRENT_PLACE);
	}
	
	@Test
	public void testPushCurrentPlace(){
		String newToken = "/some/new/token";
		Place mockPlace = mock(Place.class);
		when(mockAppPlaceHistoryMapper.getToken(mockPlace)).thenReturn(newToken);
		globalApplicationState.pushCurrentPlace(mockPlace);
		//should have set the last place (to the current), and the current place (as requested)
		verify(mockCookieProvider).setCookie(eq(CookieKeys.LAST_PLACE), anyString(), any(Date.class));
		verify(mockCookieProvider).setCookie(eq(CookieKeys.CURRENT_PLACE), anyString(), any(Date.class));
		verify(mockGWT).newItem(newToken, false);
		
		//if I push the same place again, it should not push the history state again
		when(mockCookieProvider.getCookie(CookieKeys.CURRENT_PLACE)).thenReturn("current place is set");
		when(mockAppPlaceHistoryMapper.getPlace(anyString())).thenReturn(mockPlace);
		globalApplicationState.pushCurrentPlace(mockPlace);
		//verify that these were still only called once
		verify(mockCookieProvider).setCookie(eq(CookieKeys.LAST_PLACE), anyString(), any(Date.class));
		verify(mockCookieProvider).setCookie(eq(CookieKeys.CURRENT_PLACE), anyString(), any(Date.class));
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
		verify(mockCookieProvider).setCookie(eq(CookieKeys.CURRENT_PLACE), anyString(), any(Date.class));
		verify(mockGWT).replaceItem(newToken, false);
		
		//if I push the same place again, it should not push the history state again
		when(mockCookieProvider.getCookie(CookieKeys.CURRENT_PLACE)).thenReturn("current place is set");
		when(mockAppPlaceHistoryMapper.getPlace(anyString())).thenReturn(mockPlace);
		globalApplicationState.replaceCurrentPlace(mockPlace);
		//verify that these were still only called once
		verify(mockCookieProvider).setCookie(eq(CookieKeys.LAST_PLACE), anyString(), any(Date.class));
		verify(mockCookieProvider).setCookie(eq(CookieKeys.CURRENT_PLACE), anyString(), any(Date.class));
		verify(mockGWT).replaceItem(newToken, false);
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
}
