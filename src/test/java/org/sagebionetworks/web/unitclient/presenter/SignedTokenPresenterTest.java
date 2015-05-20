package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.ResponseMessage;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.SignedToken;
import org.sagebionetworks.web.client.place.Wiki;
import org.sagebionetworks.web.client.presenter.SignedTokenPresenter;
import org.sagebionetworks.web.client.presenter.SynapseWikiPresenter;
import org.sagebionetworks.web.client.view.SignedTokenView;
import org.sagebionetworks.web.client.view.SynapseWikiView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.server.servlet.NotificationTokenType;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class SignedTokenPresenterTest {
	
	SignedTokenPresenter presenter;
	SignedTokenView mockView;
	SynapseClientAsync mockSynapseClient;
	GWTWrapper mockGWTWrapper;
	SynapseAlert mockSynapseAlert;
	SignedToken testPlace;
	GlobalApplicationState mockGlobalApplicationState;
	public static final String TEST_TOKEN = "314159bar";
	public static final String TEST_HOME_PAGE_BASE = "https://www.synapse.org/";
	public static final String SUCCESS_RESPONSE_MESSAGE = "successfully did something";
	@Before
	public void setup(){
		mockView = mock(SignedTokenView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockGWTWrapper = mock(GWTWrapper.class);
		mockSynapseAlert = mock(SynapseAlert.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		presenter = new SignedTokenPresenter(mockView, mockSynapseClient, mockGWTWrapper, mockSynapseAlert, mockGlobalApplicationState);
		verify(mockView).setPresenter(presenter);
		
		testPlace = mock(SignedToken.class);
		when(testPlace.getTokenType()).thenReturn(NotificationTokenType.JoinTeam.toString());
		when(testPlace.getSignedEncodedToken()).thenReturn(TEST_TOKEN);
		when(mockGWTWrapper.getHostPageBaseURL()).thenReturn(TEST_HOME_PAGE_BASE);
		ResponseMessage responseMessage = new ResponseMessage();
		responseMessage.setMessage(SUCCESS_RESPONSE_MESSAGE);
		AsyncMockStubber.callSuccessWith(responseMessage).when(mockSynapseClient).handleSignedToken(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		
		verify(mockView).setSynapseAlert(any(Widget.class));
		verify(mockView).setPresenter(presenter);
	}	
	
	@Test
	public void testSetPlace() {
		presenter.setPlace(testPlace);
		
		verify(mockSynapseAlert).clear();
		verify(mockView).clear();
		
		verify(mockView).showSuccess(SUCCESS_RESPONSE_MESSAGE);
	}
	
	@Test
	public void testSetPlaceFailure() {
		Exception ex = new Exception("something bad happened");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).handleSignedToken(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		presenter.setPlace(testPlace);
		verify(mockSynapseAlert).handleException(ex);
	}
	
	@Test
	public void testSetOkClicked() {
		presenter.okClicked();
		verify(mockGlobalApplicationState).gotoLastPlace();
	}
}
