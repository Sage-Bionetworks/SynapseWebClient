package org.sagebionetworks.web.shared;

import java.util.Arrays;

import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;

public class EntityUtil {

	/**
	 * Creates and access requirement used to temporary "lock down" a dataset pending 
	 * review by the Access and Compliance Team
	 * 
	 * @return
	 */
	public static AccessRequirement createLockDownDataAccessRequirement(String entityId) {
		ACTAccessRequirement ar = new ACTAccessRequirement();
		ar.setAccessType(ACCESS_TYPE.DOWNLOAD);
		ar.setActContactInfo("Access restricted pending review by Synapse Access and Compliance Team.");
		ar.setEntityIds(Arrays.asList(new String[]{entityId}));
		return ar;
	}
	
	public static EntityWrapper createLockDownDataAccessRequirementAsEntityWrapper(
			String entityId, JSONObjectAdapter jsonObjectAdapter) throws JSONObjectAdapterException {
		AccessRequirement ar = createLockDownDataAccessRequirement(entityId);
		JSONObjectAdapter arJson = ar.writeToJSONObject(jsonObjectAdapter.createNew());
		String arClassName = ar.getClass().getName();
		return new EntityWrapper(arJson.toJSONString(), arClassName, null);
	}
}
