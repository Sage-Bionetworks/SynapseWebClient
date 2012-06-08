package org.sagebionetworks.web.unitshared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.transform.JSONEntityFactory;
import org.sagebionetworks.web.client.transform.JSONEntityFactoryImpl;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.transform.NodeModelCreatorImpl;



public class PaginatedResultsTest {
	@Test
	public void testRoundTrip() throws Exception {
		AdapterFactory adapterFactory = new AdapterFactoryImpl();
		JSONEntityFactory factory = new JSONEntityFactoryImpl(adapterFactory);
		NodeModelCreator nodeModelCreator = new NodeModelCreatorImpl(factory, adapterFactory.createNew());
		
		
		// first serialize
		org.sagebionetworks.repo.model.PaginatedResults<UserProfile> ups = new org.sagebionetworks.repo.model.PaginatedResults<UserProfile>();
		ups.setTotalNumberOfResults(100L);
		List<UserProfile> results = new ArrayList<UserProfile>();
		UserProfile up = new UserProfile();
		up.setDisplayName("foo");
		up.setOwnerId("101");
		results.add(up);
		ups.setResults(results);
		ups.setPaging(new HashMap<String,String>());
		
		JSONObjectAdapter upJson = ups.writeToJSONObject(adapterFactory.createNew());
		String jsonString = upJson.toJSONString();
		
		// now deserialize
		org.sagebionetworks.web.shared.PaginatedResults<UserProfile> userProfilesShared = 
			nodeModelCreator.createPaginatedResults(jsonString, UserProfile.class);

	}

}
