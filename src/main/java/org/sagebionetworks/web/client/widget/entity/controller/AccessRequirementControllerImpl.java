package org.sagebionetworks.web.client.widget.entity.controller;

import java.util.List;

import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.EntityAccessRequirementsWidget;
import org.sagebionetworks.web.shared.AccessRequirementUtils;

import com.google.inject.Inject;

public class AccessRequirementControllerImpl implements AccessRequirementController {
	
	EntityAccessRequirementsWidget entityAccessRequirementsWidget;

	@Inject
	public AccessRequirementControllerImpl(
			EntityAccessRequirementsWidget entityAccessRequirementsWidget) {
		super();
		this.entityAccessRequirementsWidget = entityAccessRequirementsWidget;
	}

	@Override
	public void checkUploadToEntity(EntityBundle uploadTo, final Callback callback) {
		entityAccessRequirementsWidget.showUploadAccessRequirements(uploadTo.getEntity().getId(), new CallbackP<Boolean>() {
			@Override
			public void invoke(Boolean ready) {
				if(ready){
					callback.invoke();
				}
			}
		});
	}

	@Override
	public void checkDownloadFromEntity(EntityBundle downloadFrom, Callback callback) {
		List<AccessRequirement> filteredList = AccessRequirementUtils.filterAccessRequirements(downloadFrom.getAccessRequirements(), ACCESS_TYPE.DOWNLOAD);
		if(filteredList.isEmpty()){
			callback.invoke();
		}else{
			for(AccessRequirement ar: downloadFrom.getUnmetAccessRequirements()){

			}
		}
	}

	@Override
	public boolean hasUnmetDownloadRestriction(EntityBundle uploadTo) {
		List<AccessRequirement> filteredList = AccessRequirementUtils.filterAccessRequirements(uploadTo.getAccessRequirements(), ACCESS_TYPE.DOWNLOAD);
		return !filteredList.isEmpty();
	}

}
