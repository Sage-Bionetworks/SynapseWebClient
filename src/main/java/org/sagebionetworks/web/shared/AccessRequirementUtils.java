package org.sagebionetworks.web.shared;

import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.AccessRequirement;


public class AccessRequirementUtils {

	public static List<AccessRequirement> filterAccessRequirements(List<AccessRequirement> unfilteredList, ACCESS_TYPE filter) {
		List<AccessRequirement> filteredAccessRequirements = new ArrayList<AccessRequirement>();
		if (unfilteredList != null) {
			for (AccessRequirement accessRequirement : unfilteredList) {
				if (filter.equals(accessRequirement.getAccessType())) {
					filteredAccessRequirements.add(accessRequirement);
				}
			}
		}
		return filteredAccessRequirements;
	}

}
