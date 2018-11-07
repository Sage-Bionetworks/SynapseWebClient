package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Certificate;
import org.sagebionetworks.web.client.presenter.CertificatePresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.CertificateView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class CertificatePresenterTest {
	
	CertificatePresenter presenter;
	CertificateView mockView;
	SynapseClientAsync mockSynapseClient;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	SynapseAlert mockSynAlert;
	Certificate place;
	UserProfile profile;
	PassingRecord passingRecord;
	String principalId;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	
	@Before
	public void setup() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		mockView = mock(CertificateView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynAlert = mock(SynapseAlert.class);
		presenter = new CertificatePresenter(
				mockView, 
				mockGlobalApplicationState, 
				mockSynapseClient, 
				mockSynapseJavascriptClient, 
				adapterFactory,
				mockSynAlert);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		profile = new UserProfile();
		principalId = "1239";
		profile.setOwnerId(principalId);
		profile.setUserName("Fooooo");
		AsyncMockStubber.callSuccessWith(profile).when(mockSynapseJavascriptClient).getUserProfile(anyString(), any(AsyncCallback.class));
		when(mockAuthenticationController.getCurrentUserProfile()).thenReturn(profile);
		
		passingRecord = new PassingRecord();
		String passingRecordJson = passingRecord.writeToJSONObject(adapterFactory.createNew()).toJSONString();
		AsyncMockStubber.callSuccessWith(passingRecordJson).when(mockSynapseClient).getCertifiedUserPassingRecord(anyString(), any(AsyncCallback.class));
		
		verify(mockView).setPresenter(presenter);
		place = Mockito.mock(org.sagebionetworks.web.client.place.Certificate.class);
	}	
	
	@Test
	public void testHappyCase() {
		//on init with place, it should ask for the user profile and passing record, and send that to the view.
		when(place.toToken()).thenReturn(principalId);
		presenter.setPlace(place);
		verify(mockSynAlert).clear();
		verify(mockView, times(2)).setPresenter(presenter);
		verify(mockView).clear();
		verify(mockView).showLoading();
		verify(mockSynapseJavascriptClient).getUserProfile(eq(principalId), any(AsyncCallback.class));
		verify(mockSynapseClient).getCertifiedUserPassingRecord(eq(principalId), any(AsyncCallback.class));
		verify(mockView).showSuccess(profile, passingRecord);
	}
	
	@Test
	public void testProfileRequestError() {
		String error = "An error";
		Exception caught = new Exception(error);
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseJavascriptClient).getUserProfile(anyString(), any(AsyncCallback.class));
		presenter.initStep1(principalId);
		verify(mockSynAlert).handleException(caught);
	}
	
	@Test
	public void testNotCertified() {
		NotFoundException notFoundError = new NotFoundException();
		AsyncMockStubber.callFailureWith(notFoundError).when(mockSynapseClient).getCertifiedUserPassingRecord(anyString(), any(AsyncCallback.class));
		presenter.initStep2(principalId, profile);
		verify(mockView).showNotCertified(profile);
	}
	
	@Test
	public void testPassingRecordRequestError() {
		String error = "passing record error";
		Exception caught = new Exception(error);
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseClient).getCertifiedUserPassingRecord(anyString(), any(AsyncCallback.class));
		presenter.initStep2(principalId, profile);
		verify(mockView, never()).showNotCertified(any(UserProfile.class));
		verify(mockSynAlert).handleException(caught);
	}

	
}
