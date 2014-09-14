package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.quiz.MultichoiceAnswer;
import org.sagebionetworks.repo.model.quiz.MultichoiceQuestion;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.repo.model.quiz.Question;
import org.sagebionetworks.repo.model.quiz.Quiz;
import org.sagebionetworks.repo.model.quiz.QuizResponse;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.place.Certificate;
import org.sagebionetworks.web.client.presenter.CertificatePresenter;
import org.sagebionetworks.web.client.presenter.QuizPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.CertificateView;
import org.sagebionetworks.web.client.view.QuizView;
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
	ClientCache mockCache;
	Certificate place;
	UserProfile profile;
	PassingRecord passingRecord;
	String principalId;
	
	@Before
	public void setup() throws JSONObjectAdapterException {
		mockView = mock(CertificateView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockCache = mock(ClientCache.class);
		presenter = new CertificatePresenter(mockView, mockAuthenticationController, mockGlobalApplicationState, mockSynapseClient, adapterFactory,mockCache);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserSessionData()).thenReturn(new UserSessionData());
		profile = new UserProfile();
		principalId = "1239";
		profile.setOwnerId(principalId);
		profile.setUserName("Fooooo");
		String userProfileJson = profile.writeToJSONObject(adapterFactory.createNew()).toJSONString(); 
		AsyncMockStubber.callSuccessWith(userProfileJson).when(mockSynapseClient).getUserProfile(anyString(), any(AsyncCallback.class));
		
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
		
		verify(mockView, times(2)).setPresenter(presenter);
		verify(mockView).clear();
		verify(mockView).showLoading();
		verify(mockSynapseClient).getUserProfile(eq(principalId), any(AsyncCallback.class));
		verify(mockSynapseClient).getCertifiedUserPassingRecord(eq(principalId), any(AsyncCallback.class));
		verify(mockView).showSuccess(profile, passingRecord);
	}
	
	@Test
	public void testProfileRequestError() {
		String error = "An error";
		AsyncMockStubber.callFailureWith(new Exception(error)).when(mockSynapseClient).getUserProfile(anyString(), any(AsyncCallback.class));
		presenter.initStep1(principalId);
		verify(mockView).showErrorMessage(error);
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
		AsyncMockStubber.callFailureWith(new Exception(error)).when(mockSynapseClient).getCertifiedUserPassingRecord(anyString(), any(AsyncCallback.class));
		presenter.initStep2(principalId, profile);
		verify(mockView).showErrorMessage(error);
	}

	
}
