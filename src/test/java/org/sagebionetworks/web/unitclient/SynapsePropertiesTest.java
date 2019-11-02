package org.sagebionetworks.web.unitclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.StackConfigServiceAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.SynapsePropertiesImpl;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SynapsePropertiesTest {
	SynapseProperties synapseProperties;
	HashMap<String, String> testProps;
	@Mock
	StackConfigServiceAsync mockStackConfigService;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Mock
	Callback mockCallback;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		testProps = new HashMap<String, String>();
		AsyncMockStubber.callSuccessWith(testProps).when(mockStackConfigService).getSynapseProperties(any(AsyncCallback.class));

		synapseProperties = new SynapsePropertiesImpl(mockStackConfigService, mockSynapseJSNIUtils);
	}

	@Test
	public void testInitSynapseProperties() {
		String key = "k1";
		String value = "v1";
		testProps.put(key, value);

		String publicPrincipalId = "1";
		String anonymousPrincipalId = "2";
		String authenticatedPrincipalId = "3";
		testProps.put(WebConstants.PUBLIC_ACL_PRINCIPAL_ID, publicPrincipalId);
		testProps.put(WebConstants.ANONYMOUS_USER_PRINCIPAL_ID, anonymousPrincipalId);
		testProps.put(WebConstants.AUTHENTICATED_ACL_PRINCIPAL_ID, authenticatedPrincipalId);

		synapseProperties.initSynapseProperties(mockCallback);

		verify(mockStackConfigService).getSynapseProperties(any(AsyncCallback.class));
		assertEquals(value, synapseProperties.getSynapseProperty(key));
		assertNull(synapseProperties.getSynapseProperty("foo"));

		PublicPrincipalIds publicPrincipalIds = synapseProperties.getPublicPrincipalIds();
		assertEquals(publicPrincipalId, publicPrincipalIds.getPublicAclPrincipalId().toString());
		assertEquals(anonymousPrincipalId, publicPrincipalIds.getAnonymousUserPrincipalId().toString());
		assertEquals(authenticatedPrincipalId, publicPrincipalIds.getAuthenticatedAclPrincipalId().toString());
		verify(mockCallback).invoke();
	}

	@Test
	public void testInitSynapsePropertiesFailure() {
		String errorMessage = "unable to get properties";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockStackConfigService).getSynapseProperties(any(AsyncCallback.class));

		synapseProperties.initSynapseProperties(mockCallback);

		verify(mockSynapseJSNIUtils).consoleError(errorMessage);
		verify(mockCallback).invoke();
	}
}
