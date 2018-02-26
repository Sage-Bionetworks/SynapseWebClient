package org.sagebionetworks.web.unitserver;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.Whitebox;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.repo.model.file.UploadDestination;
import org.sagebionetworks.repo.model.principal.AccountSetupInfo;
import org.sagebionetworks.repo.model.project.StorageLocationSetting;
import org.sagebionetworks.repo.model.verification.VerificationSubmission;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.StackConfigServiceImpl;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.server.servlet.UserProfileClientImpl;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

public class StackConfigServiceTest {
	@Mock
	SynapseProvider mockSynapseProvider;
	@Mock
	TokenProvider mockTokenProvider;
	@Mock
	ServiceUrlProvider mockUrlProvider;
	@Mock
	UserSessionData mockUserSessionData;
	String testSessionToken = "12345abcde";
	@Mock
	SynapseClient mockSynapse;
	@Mock
	ThreadLocal<HttpServletRequest> mockThreadLocal;
	@Mock 
	HttpServletRequest mockRequest;
	StackConfigServiceImpl stackConfigService;
	
	@Before
	public void before() throws SynapseException, JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapse);
		
		UserProfile testProfile = new UserProfile();
		testProfile.setOwnerId("123");		
		
		stackConfigService = new StackConfigServiceImpl();
		stackConfigService.setSynapseProvider(mockSynapseProvider);
		stackConfigService.setTokenProvider(mockTokenProvider);
		stackConfigService.setServiceUrlProvider(mockUrlProvider);
		Session testSession = new Session();
		testSession.setSessionToken(testSessionToken);
		testSession.setAcceptsTermsOfUse(true);
		when(mockSynapse.createNewAccount(any(AccountSetupInfo.class))).thenReturn(testSession);
		when(mockSynapse.getUserSessionData()).thenReturn(mockUserSessionData);
		when(mockUserSessionData.getProfile()).thenReturn(testProfile);
		when(mockUserSessionData.getSession()).thenReturn(testSession);
		when(mockSynapse.getMyProfile()).thenReturn(testProfile);
		
		Whitebox.setInternalState(stackConfigService, "perThreadRequest", mockThreadLocal);
		when(mockThreadLocal.get()).thenReturn(mockRequest);
		when(mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");
	}
	@Test
	public void testGetStorageLocationSettingProperty() throws SynapseException, RestServiceException {
		String defaultStorageId = stackConfigService.getSynapseProperties().get(SynapseClientImpl.DEFAULT_STORAGE_ID_PROPERTY_KEY);
		assertNotNull(defaultStorageId);
	}
}
