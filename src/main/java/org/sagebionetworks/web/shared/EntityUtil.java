package org.sagebionetworks.web.shared;

import java.util.Arrays;

import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;

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
}
