package org.sagebionetworks.web.unitclient.security.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;

public class UserSessionDataTest {

	@Test
	public void testToCookie() {
		UserSessionData user = new UserSessionData();
		UserProfile profile = new UserProfile();
		profile.setDisplayName("Display Name");
		profile.setOwnerId("1");
		user.setProfile(profile);
		user.setSession(new Session());
		user.getSession().setSessionToken("token");
		user.setIsSSO(false);
		try {
			String cookieString = EntityFactory.createJSONStringForEntity(user);
			System.out.println(cookieString);

			UserSessionData copy = EntityFactory.createEntityFromJSONString(cookieString, UserSessionData.class);
			assertNotNull(copy);
			assertEquals(user, copy);
		} catch (JSONObjectAdapterException e) {
			assertNotNull("UserSessionData test failure due to JSON adapter exception. " + e, null);
		}
	}

}
