package org.sagebionetworks.web.client.widget.entity.controller;

import java.util.List;

import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.AccessRequirementUtils;

public class AccessRestrictionControllerImpl implements AccessRestrictionController {

	@Override
	public void checkUploadToEntity(EntityBundle uploadTo, Callback callback) {
		List<AccessRequirement> filteredList = AccessRequirementUtils.filterAccessRequirements(uploadTo.getAccessRequirements(), ACCESS_TYPE.UPLOAD);
		if(filteredList.isEmpty()){
			callback.invoke();
		}else{
			for(AccessRequirement ar: uploadTo.getUnmetAccessRequirements()){
				if(ACCESS_TYPE.UPLOAD.equals(ar.getAccessType())){
					
				}
			}
		}
	}

	@Override
	public void checkDownloadFromEntity(EntityBundle downloadFrom, Callback callback) {
		// TODO Auto-generated method stub
		
	}

}
