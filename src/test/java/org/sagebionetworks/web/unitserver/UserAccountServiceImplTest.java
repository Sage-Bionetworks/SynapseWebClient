package org.sagebionetworks.web.unitserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

import net.oauth.OAuthException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.server.RestTemplateProvider;
import org.sagebionetworks.web.server.RestTemplateProviderImpl;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.UserAccountServiceImpl;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.shared.users.UserRegistration;

import com.sun.grizzly.http.SelectorThread;

/**
 * This is a unit test of the UserAccountServiceImpl service.
 * It depends on a local stub implementation of the platform API
 * to be deployed locally.
 * 
 * @author dburdick
 *
 */

// TO BE DELETED WHEN BRUCE REPLACES UserAccountService with Syanpse Java client
public class UserAccountServiceImplTest {
	
	private static Logger logger = Logger.getLogger(UserAccountServiceImplTest.class.getName());
	
	/**
	 * This is our handle to the local grizzly container.
	 * It can be used to communicate with the container or 
	 * shut it down.
	 */
	private static SelectorThread selector = null;
	
	private static String serviceHost = "localhost";
	private static int servicePort = 9998;
	private static URL serviceUrl = null;
	
	// This is our service.
	private static UserAccountServiceImpl service = null;
	private static RestTemplateProvider provider = null;
	
	private UserRegistration user1 = new UserRegistration("test@test.com", "test", "user", "test user");
	private String user1password = "password";
	private UserRegistration user2 = new UserRegistration("bar@foo.com", "bar", "foo", "barfoo");
	private String user2password = "otherpass";
	
	
	@BeforeClass
	public static void beforeClass() throws Exception{
		// Start the local stub implementation of the the platform
		// api.  This stub services runs in a local grizzly/jersey 
		// container.
		
//		// First setup the url
		serviceUrl = new URL("https://fakeurl.com");
//		// Now start the container
//		selector = LocalAuthServiceStub.startServer(serviceHost, servicePort);
		
		// Create the RestProvider
		int timeout = 1000*60*2; // 2 minute timeout
		int maxTotalConnections = 1; // Only need one for this test.
		provider = new RestTemplateProviderImpl(timeout, maxTotalConnections);
		// Create the service
		service = new UserAccountServiceImpl();
		// Inject the required values
		service.setRestTemplate(provider);
		ServiceUrlProvider urlProvider = new ServiceUrlProvider();
		urlProvider.setAuthServicePrivateUrl(serviceUrl.toString() + "/auth/v1");		
		service.setServiceUrlProvider(urlProvider);
		
//		LocalAuthServiceStub.groups.add(group1);
//		LocalAuthServiceStub.groups.add(group2);
//		LocalAuthServiceStub.users.add(user1acl);
//		LocalAuthServiceStub.users.add(user2acl);

	}
	
	/**
	 * Clear all of the data in the stub service.
	 */
	private static void clearStubData(){
	}
	
	/**
	 * Clear all of the data in the stub service.
	 */
	private static void generateRandomData(int number){
		clearStubData();
	}
	
	@AfterClass
	public static void afterClass(){
		// Shut down the grizzly container at the end of this suite.
		if(selector != null){
			selector.stopEndpoint();
		}
	}
	
	@After
	public void tearDown(){
		// After each test clean out all data
		clearStubData();
	}
	
	
	@Test
	public void testValidate(){
		// Create an instance that is not setup correctly
		UserAccountServiceImpl dummy = new UserAccountServiceImpl();
		try{
			dummy.validateService();
			fail("The dummy was not initialized so it should have failed validation");
		}catch(IllegalStateException e){
			//expected;
		}
		// Set the template
		dummy.setRestTemplate(provider);
		try{
			dummy.validateService();
			fail("The dummy was not initialized so it should have failed validation");
		}catch(IllegalStateException e){
			//expected;
		}
		// After setting the url it should pass validation.
		ServiceUrlProvider urlProvider = new ServiceUrlProvider();
		urlProvider.setRepositoryServiceUrl(serviceUrl.toString() + "repo/v1/");		
		dummy.setServiceUrlProvider(urlProvider);
	}
	
	@Ignore
	@Test
	public void testCreateUser() throws Exception {
		try {
			service.createUser(user1);
		} catch (RestServiceException e) {
			fail(e.getMessage());
		}
		
		// assure user was actually created		
		String userDataJson = service.initiateSession(user1.getEmail(), user1password, false);
		UserSessionData userData = EntityFactory.createEntityFromJSONString(userDataJson, UserSessionData.class);
		Assert.assertNotNull(userData.getSessionToken());
	}
	
	@Ignore
	@Test(expected=UnauthorizedException.class)
	public void testUnAuthenticateUser() throws Exception {
		// try fake user
		service.initiateSession("junk", "junk", false);
	}
		
	@Ignore
	@Test
	public void testAuthenticateUser() throws Exception {
		// auth real user
		String userdataJson = service.initiateSession(user1.getEmail(), user1password, false);
		UserSessionData userData = EntityFactory.createEntityFromJSONString(userdataJson, UserSessionData.class);
		Assert.assertNotNull(userData.getSessionToken());
	}
		
	@Test
	public void testSendPasswordResetEmail(){
		try {
			service.sendPasswordResetEmail(user1.getEmail());
		} catch (RestServiceException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testSendSetApiPasswordEmail() {
		try {
			service.sendSetApiPasswordEmail();
		} catch (RestServiceException e) {
			fail(e.getMessage());
		}
	}
		
	@Ignore
	@Test
	public void testTerminateSession() throws Exception {
		String userdataJson = service.initiateSession(user1.getEmail(), user1password, false);
		
		if(userdataJson == null) fail("test setup error: user doesn't exist");
		UserSessionData userData = EntityFactory.createEntityFromJSONString(userdataJson, UserSessionData.class);
		// terminate unknown session
		service.terminateSession("junk");

		
		// terminate real session
		service.terminateSession(userData.getSessionToken());			
	}
		
	@Test
	public void testGetAuthServiceUrl() {
		String authServiceUrl = service.getPrivateAuthServiceUrl();
		
		try {
			URI testUri = new URI(authServiceUrl);
		} catch (URISyntaxException e) {
			fail("The Auth Service URL returned was not valid.");
		}
	}
	
	@Test
	public void testGetFastPassUrl() throws OAuthException, IOException, URISyntaxException {
		try {
			//anonymous user test
			String fastPassUrl = service.getFastPassSupportUrl();
			assertEquals(fastPassUrl, "");
			
			//This tests the FastPass library, it makes a call to getsatisfaction.com to form the authenticated SSO url string.
			fastPassUrl = service.getFastPassSupportUrl("myemail@foo.com", "Mr. Bar", "x1169");
			//the fastpass url contains all of the right pieces
			Assert.assertTrue(fastPassUrl.indexOf("uid=x1169") > -1);
			Assert.assertTrue(fastPassUrl.indexOf("email=myemail%40foo.com") > -1);
			Assert.assertTrue(fastPassUrl.indexOf("name=Mr.%20Bar") > -1);
		} catch (RestServiceException e) {
			fail("The fastpass URL returned was not valid.");
		}
	}
	
	
	@Test
	public void testGetSynapseWebUrl() {
		String synapseWebUrl = service.getSynapseWebUrl();
		try {
			URI testUri = new URI(synapseWebUrl);
		} catch (URISyntaxException e) {
			fail("The Synapse URL returned was not valid.");
		}
	}
}
