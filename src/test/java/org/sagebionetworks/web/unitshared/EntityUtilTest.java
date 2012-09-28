package org.sagebionetworks.web.unitshared;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.transform.JSONEntityFactory;
import org.sagebionetworks.web.client.transform.JSONEntityFactoryImpl;
import org.sagebionetworks.web.shared.EntityUtil;
import org.sagebionetworks.web.shared.EntityWrapper;

public class EntityUtilTest {
	@Test
	public void testCreateLockDownDataAccessRequirementAsEntityWrapper() throws Exception {
		String entityId = "101";
		JSONObjectAdapter jsonObjectAdapter = new JSONObjectAdapterImpl();
		EntityWrapper ew = EntityUtil.
			createLockDownDataAccessRequirementAsEntityWrapper(entityId, jsonObjectAdapter);

		AdapterFactory adapterFactory = new AdapterFactoryImpl();
		JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(adapterFactory);
		AccessRequirement ar = jsonEntityFactory.createEntity(ew.getEntityJson(), 
				(Class<AccessRequirement>)Class.forName(ew.getEntityClassName()));
	
		assertEquals(entityId, ar.getEntityIds().get(0));
		assertTrue(ar instanceof ACTAccessRequirement);
	}

}
