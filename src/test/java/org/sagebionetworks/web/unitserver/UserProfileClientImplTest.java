package org.sagebionetworks.web.unitserver;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.NewUser;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.repo.model.principal.AccountSetupInfo;
import org.sagebionetworks.repo.model.verification.VerificationSubmission;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.server.servlet.UserAccountServiceImpl;
import org.sagebionetworks.web.server.servlet.UserProfileClientImpl;

/**
 * Test for the UserAccountServiceImpl
 * 
 */
public class UserProfileClientImplTest {
	@Mock
	SynapseProvider mockSynapseProvider;
	@Mock
	TokenProvider mockTokenProvider;
	@Mock
	ServiceUrlProvider mockUrlProvider;
	@Mock
	UserProfileClientImpl userProfileClient;
	@Mock
	UserSessionData mockUserSessionData;
	@Mock
	VerificationSubmission mockVerificationSubmission;
	String testSessionToken = "12345abcde";
	UserProfile testProfile;	

	@Mock
	SynapseClient mockSynapse;
	@Before
	public void before() throws SynapseException, JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapse);
		
		testProfile = new UserProfile();
		testProfile.setOwnerId("123");		
		
		userProfileClient = new UserProfileClientImpl();
		userProfileClient.setSynapseProvider(mockSynapseProvider);
		userProfileClient.setTokenProvider(mockTokenProvider);
		userProfileClient.setServiceUrlProvider(mockUrlProvider);
		Session testSession = new Session();
		testSession.setSessionToken(testSessionToken);
		testSession.setAcceptsTermsOfUse(true);
		when(mockSynapse.createNewAccount(any(AccountSetupInfo.class))).thenReturn(testSession);
		when(mockSynapse.getUserSessionData()).thenReturn(mockUserSessionData);
		when(mockUserSessionData.getProfile()).thenReturn(testProfile);
		when(mockUserSessionData.getSession()).thenReturn(testSession);
		when(mockSynapse.getMyProfile()).thenReturn(testProfile);
	}

	@Test
	public void testCreateVerificationSubmission() throws Exception {
		String firstName="Jack", lastName="Frost", location="everywhere", company="unknown";
		
		when(mockVerificationSubmission.getFirstName()).thenReturn(firstName);
		when(mockVerificationSubmission.getLastName()).thenReturn(lastName);
		when(mockVerificationSubmission.getLocation()).thenReturn(location);
		when(mockVerificationSubmission.getCompany()).thenReturn(company);
		
		String hostPageBaseURL = "http://127.0.0.1:8080/Portal.html?gwt.codesvr=127.0.0.1:9321";
		userProfileClient.createVerificationSubmission(mockVerificationSubmission, hostPageBaseURL);
		verify(mockSynapse).getMyProfile();
		assertEquals(firstName, testProfile.getFirstName());
		assertEquals(lastName, testProfile.getLastName());
		assertEquals(location, testProfile.getLocation());
		assertEquals(company, testProfile.getCompany());
		
		verify(mockSynapse).createVerificationSubmission(eq(mockVerificationSubmission), contains(hostPageBaseURL));
	}


}
