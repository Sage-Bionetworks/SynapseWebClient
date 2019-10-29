package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.JoinTeamSignedToken;
import org.sagebionetworks.repo.model.ResponseMessage;
import org.sagebionetworks.repo.model.SignedTokenInterface;
import org.sagebionetworks.repo.model.message.NotificationSettingsSignedToken;
import org.sagebionetworks.repo.model.principal.EmailValidationSignedToken;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.mvp.AppActivityMapper;
import org.sagebionetworks.web.client.place.SignedToken;
import org.sagebionetworks.web.client.place.Team;
import org.sagebionetworks.web.client.presenter.SignedTokenPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.SignedTokenView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

@RunWith(MockitoJUnitRunner.class)
public class SignedTokenPresenterTest {
	
	SignedTokenPresenter presenter;
	@Mock
	SignedTokenView mockView;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	GWTWrapper mockGWTWrapper;
	@Mock
	SynapseAlert mockSynapseAlert;
	@Mock
	SignedToken testPlace;
	@Mock
	UserBadge mockUserBadge;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	public static final String TEST_TOKEN = "314159bar";
	public static final String TEST_HOME_PAGE_BASE = "https://www.synapse.org/";
	public static final String SUCCESS_RESPONSE_MESSAGE = "successfully did something";
	List<AccessRequirement> accessRequirements;
	@Mock
	AccessRequirement mockAccessRequirement;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	PopupUtilsView mockPopupUtils;
	@Captor
	ArgumentCaptor<AsyncCallback> asyncCaptor;
	JoinTeamSignedToken defaultToken = new JoinTeamSignedToken();
	
	@Before
	public void setup(){
		presenter = new SignedTokenPresenter(mockView, mockSynapseClient, mockGWTWrapper, mockSynapseAlert, mockGlobalApplicationState, mockUserBadge, mockAuthenticationController, mockPopupUtils);
		verify(mockView).setPresenter(presenter);
		
		when(testPlace.getSignedEncodedToken()).thenReturn(TEST_TOKEN);
		when(mockGWTWrapper.getHostPageBaseURL()).thenReturn(TEST_HOME_PAGE_BASE);
		ResponseMessage responseMessage = new ResponseMessage();
		responseMessage.setMessage(SUCCESS_RESPONSE_MESSAGE);
		
		AsyncMockStubber.callSuccessWith(responseMessage).when(mockSynapseClient).handleSignedToken(any(SignedTokenInterface.class), anyString(), any(AsyncCallback.class));
		
		//by default, decode into a JoinTeamSignedToken
		AsyncMockStubber.callSuccessWith(defaultToken).when(mockSynapseClient).hexDecodeAndDeserialize(anyString(), any(AsyncCallback.class));
		
		verify(mockView).setSynapseAlert(any(Widget.class));
		verify(mockView).setPresenter(presenter);
		verify(mockView).setUnsubscribingUserBadge(any(Widget.class));
		// by default, the team has no access requirements (so it should just handle the signed token like any other signed token request).
		accessRequirements = new ArrayList<AccessRequirement>();
		AsyncMockStubber.callSuccessWith(accessRequirements).when(mockSynapseClient).getTeamAccessRequirements(anyString(), any(AsyncCallback.class));
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
	}	
	
	@Test
	public void testSetPlaceJoinTeamSignedToken() {
		presenter.setPlace(testPlace);
		
		verify(mockSynapseClient).hexDecodeAndDeserialize(anyString(), any(AsyncCallback.class));
		verify(mockSynapseClient).handleSignedToken(any(SignedTokenInterface.class), anyString(), any(AsyncCallback.class));
		verify(mockSynapseAlert).clear();
		verify(mockView, times(2)).clear();
		verify(mockView, atLeast(2)).setLoadingVisible(anyBoolean());
		verify(mockPopupUtils).showInfo(SUCCESS_RESPONSE_MESSAGE);
		verify(mockPlaceChanger).goTo(isA(Team.class));
	}
	
	@Test
	public void testSetPlaceEmailValidationSignedToken() {
		AsyncMockStubber.callSuccessWith(new EmailValidationSignedToken()).when(mockSynapseClient).hexDecodeAndDeserialize(anyString(), any(AsyncCallback.class));
		
		presenter.setPlace(testPlace);
		
		verify(mockSynapseClient).hexDecodeAndDeserialize(anyString(), any(AsyncCallback.class));
		verify(mockSynapseClient).handleSignedToken(any(SignedTokenInterface.class), anyString(), any(AsyncCallback.class));
		verify(mockSynapseAlert).clear();
		verify(mockView, times(2)).clear();
		verify(mockView, atLeast(2)).setLoadingVisible(anyBoolean());
		verify(mockView).showSuccess(SUCCESS_RESPONSE_MESSAGE);
	}
	
	@Test
	public void testSetPlaceFailure() {
		Exception ex = new Exception("something bad happened");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).handleSignedToken(any(SignedTokenInterface.class), anyString(), any(AsyncCallback.class));
		presenter.setPlace(testPlace);
		verify(mockSynapseClient).hexDecodeAndDeserialize(anyString(), any(AsyncCallback.class));
		verify(mockSynapseClient).handleSignedToken(any(SignedTokenInterface.class), anyString(), any(AsyncCallback.class));
		verify(mockSynapseAlert).handleException(ex);
		verify(mockView, times(2)).clear();
		verify(mockView, atLeast(2)).setLoadingVisible(anyBoolean());
	}
	
	@Test
	public void testSetPlaceWithAccessRequirements() {
		accessRequirements.add(mockAccessRequirement);
		presenter.setPlace(testPlace);
		
		verify(mockSynapseClient).hexDecodeAndDeserialize(anyString(), any(AsyncCallback.class));
		verify(mockSynapseClient, never()).handleSignedToken(any(SignedTokenInterface.class), anyString(), any(AsyncCallback.class));
		verify(mockSynapseAlert).clear();
		verify(mockPlaceChanger).goTo(isA(Team.class));
	}
	
	@Test
	public void testSetPlaceFailureToGetTeamAccessRequirements() {
		Exception ex = new Exception("something bad happened");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getTeamAccessRequirements(anyString(), any(AsyncCallback.class));
		presenter.setPlace(testPlace);
		
		verify(mockSynapseAlert).handleException(ex);
	}
	
	@Test
	public void testJoinTeamExpiredSessionToken() {
		reset(mockSynapseClient);
		AsyncMockStubber.callSuccessWith(new JoinTeamSignedToken()).when(mockSynapseClient).hexDecodeAndDeserialize(anyString(), any(AsyncCallback.class));
		presenter.setPlace(testPlace);
		
		// verify rpc attempt, and simulate an UnauthorizedException		
		verify(mockSynapseClient).getTeamAccessRequirements(anyString(), asyncCaptor.capture());
		Exception ex = new UnauthorizedException("bad session");
		asyncCaptor.getValue().onFailure(ex);
		verify(mockAuthenticationController).logoutUser();
		verify(mockSynapseAlert, never()).handleException(ex);
		
		//verify that it tried to call rpc again
		verify(mockSynapseClient, times(2)).getTeamAccessRequirements(anyString(), asyncCaptor.capture());
		//if the second attempt is successful, then it should try to handle the signed token.
		asyncCaptor.getAllValues().get(1).onSuccess(accessRequirements);
		verify(mockSynapseClient).handleSignedToken(any(SignedTokenInterface.class), anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testJoinTeamExpiredSessionTokenMultipleErrors() {
		reset(mockSynapseClient);
		AsyncMockStubber.callSuccessWith(new JoinTeamSignedToken()).when(mockSynapseClient).hexDecodeAndDeserialize(anyString(), any(AsyncCallback.class));
		presenter.setPlace(testPlace);
		
		// verify rpc attempt, and simulate an UnauthorizedException		
		verify(mockSynapseClient).getTeamAccessRequirements(anyString(), asyncCaptor.capture());
		Exception ex = new UnauthorizedException("bad session");
		asyncCaptor.getValue().onFailure(ex);
		verify(mockAuthenticationController).logoutUser();
		verify(mockSynapseAlert, never()).handleException(ex);
		
		//verify that it tried to call rpc again
		verify(mockSynapseClient, times(2)).getTeamAccessRequirements(anyString(), asyncCaptor.capture());
		//if it runs into another error (even if it's an UnauthorizedException) it should not try again.
		asyncCaptor.getAllValues().get(1).onFailure(ex);
		verify(mockSynapseAlert).handleException(ex);
	}
	
	@Test
	public void testSetPlaceFailureHexDecode() {
		Exception ex = new Exception("something bad happened during hex decode");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).hexDecodeAndDeserialize(anyString(), any(AsyncCallback.class));
		presenter.setPlace(testPlace);
		verify(mockSynapseClient).hexDecodeAndDeserialize(anyString(), any(AsyncCallback.class));
		verify(mockSynapseClient, never()).handleSignedToken(any(SignedTokenInterface.class), anyString(), any(AsyncCallback.class));
		verify(mockView).clear();
		//loading shown and hidden for single async call
		verify(mockView, times(2)).setLoadingVisible(anyBoolean());
		verify(mockSynapseAlert).handleException(ex);
	}
	
	@Test
	public void testSetPlaceUnsubscribe() {
		//For the unsubscribe token, a special view is shown.
		NotificationSettingsSignedToken token = new NotificationSettingsSignedToken();
		AsyncMockStubber.callSuccessWith(token).when(mockSynapseClient).hexDecodeAndDeserialize(anyString(), any(AsyncCallback.class));
		presenter.setPlace(testPlace);
		
		verify(mockSynapseClient).hexDecodeAndDeserialize(anyString(), any(AsyncCallback.class));
		verify(mockSynapseClient, never()).handleSignedToken(any(SignedTokenInterface.class), anyString(), any(AsyncCallback.class));
		verify(mockSynapseAlert).clear();
		verify(mockView).clear();
		verify(mockView, times(2)).setLoadingVisible(anyBoolean());
		verify(mockView).showConfirmUnsubscribe(token);

		//simulate confirmation of unsubscribe
		presenter.unsubscribeConfirmed(token);
		verify(mockSynapseClient).handleSignedToken(any(NotificationSettingsSignedToken.class), anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testSetOkClicked() {
		presenter.okClicked();
		verify(mockPlaceChanger).goTo(AppActivityMapper.getDefaultPlace());
	}
}
