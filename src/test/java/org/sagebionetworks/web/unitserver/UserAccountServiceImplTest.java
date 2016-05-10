package org.sagebionetworks.web.unitserver;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.NewUser;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.repo.model.principal.AccountSetupInfo;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.server.servlet.UserAccountServiceImpl;

/**
 * Test for the UserAccountServiceImpl
 * 
 */
public class UserAccountServiceImplTest {
	SynapseProvider mockSynapseProvider;
	TokenProvider mockTokenProvider;
	ServiceUrlProvider mockUrlProvider;
	SynapseClient mockSynapse;
	UserAccountServiceImpl userAccountService;
	UserSessionData mockUserSessionData;
	String testSessionToken = "12345abcde";
	UserProfile testProfile;	

	@Before
	public void before() throws SynapseException, JSONObjectAdapterException {
		mockSynapse = Mockito.mock(SynapseClient.class);
		mockSynapseProvider = Mockito.mock(SynapseProvider.class);
		mockUrlProvider = Mockito.mock(ServiceUrlProvider.class);
		mockUserSessionData = Mockito.mock(UserSessionData.class);
		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapse);
		mockTokenProvider = Mockito.mock(TokenProvider.class);

		testProfile = new UserProfile();
		testProfile.setOwnerId("123");		
		
		userAccountService = new UserAccountServiceImpl();
		userAccountService.setSynapseProvider(mockSynapseProvider);
		userAccountService.setTokenProvider(mockTokenProvider);
		userAccountService.setServiceUrlProvider(mockUrlProvider);
		Session testSession = new Session();
		testSession.setSessionToken(testSessionToken);
		testSession.setAcceptsTermsOfUse(true);
		when(mockSynapse.createNewAccount(any(AccountSetupInfo.class))).thenReturn(testSession);
		when(mockSynapse.getUserSessionData()).thenReturn(mockUserSessionData);
		when(mockUserSessionData.getProfile()).thenReturn(testProfile);
		when(mockUserSessionData.getSession()).thenReturn(testSession);
	}

	@Test
	public void testCreateUserStep1() throws Exception {
		String email = "test@jayhodgson.com";
		String endpoint = "http://127.0.0.1:8080/Portal.html?gwt.codesvr=127.0.0.1:9321";
		userAccountService.createUserStep1(email, endpoint);
		verify(mockSynapse).newAccountEmailValidation(any(NewUser.class), eq(endpoint));
	}

	@Test
	public void testCreateUserStep2() throws Exception {
		String username = "choochoo";
		String fName = "ralph";
		String lName = "wiggum";
		String pw = "password";
		String emailValidationToken = "firstname=&lastname=&email=choochoo%40aol.com&timestamp=2014-09-02T23%3A45%3A57.788%2B0000&domain=SYNAPSE&mac=1hBX";
		AccountSetupInfo testASI = new AccountSetupInfo();
		testASI.setUsername(username);
		testASI.setFirstName(fName);
		testASI.setLastName(lName);
		testASI.setPassword(pw);
		testASI.setEmailValidationToken(emailValidationToken);

		String returnSessionToken = userAccountService.createUserStep2(username, fName, lName, pw, emailValidationToken);
		assertEquals(testSessionToken, returnSessionToken);
		ArgumentCaptor<AccountSetupInfo> arg = ArgumentCaptor.forClass(AccountSetupInfo.class);
		verify(mockSynapse).createNewAccount(arg.capture());
		AccountSetupInfo capturedSetInfo = arg.getValue();
		assertEquals(testASI, capturedSetInfo);
	}
	
	@Test
	public void testGetUserLoginBundle() throws Exception {
		userAccountService.getUserLoginBundle(testSessionToken);
		verify(mockSynapse).getUserSessionData();
		verify(mockSynapse).getUserBundle(Long.valueOf(testProfile.getOwnerId()), 63);
	}
	
	@Test
	public void testGetUserLoginBundleNotSignedToU() throws Exception {
		Session testSession = new Session();
		testSession.setSessionToken(testSessionToken);
		testSession.setAcceptsTermsOfUse(false);
		when(mockUserSessionData.getSession()).thenReturn(testSession);
		
		userAccountService.getUserLoginBundle(testSessionToken);
		verify(mockSynapse).getUserSessionData();
		verify(mockSynapse, never()).getUserBundle(anyLong(), anyInt());
	}

}
