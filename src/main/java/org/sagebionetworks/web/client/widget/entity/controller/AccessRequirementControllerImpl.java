package org.sagebionetworks.web.client.widget.entity.controller;

import java.util.List;

import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.AccessRequirementUtils;
import org.sagebionetworks.repo.model.EntityBundle;

import com.google.inject.Inject;

public class AccessRequirementControllerImpl implements AccessRequirementController {
	
	@Inject
	public AccessRequirementControllerImpl() {
		super();
	}

	@Override
	public void checkDownloadFromEntity(EntityBundle downloadFrom, Callback callback) {
		// TODO: This will use the new access restriction dialog when it is read.
		callback.invoke();
	}

	@Override
	public boolean hasUnmetDownloadRestriction(EntityBundle uploadTo) {
		List<AccessRequirement> filteredList = AccessRequirementUtils.filterAccessRequirements(uploadTo.getAccessRequirements(), ACCESS_TYPE.DOWNLOAD);
		return !filteredList.isEmpty();
	}

}
