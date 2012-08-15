package org.sagebionetworks.web.shared.provenance;

import java.util.Iterator;
import java.util.Set;

import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.provenance.UsedEntity;

public class ActivityTypeUtil {

	public static ActivityType get(Activity activity) {
		if(activity == null) return ActivityType.UNDEFINED;
		
		Set<UsedEntity> used = activity.getUsed();
		if(used == null || used.size() == 0) return ActivityType.UNDEFINED;

		// look for executed
		boolean isManual = true;
		Iterator<UsedEntity> itr = used.iterator();
		while(itr.hasNext()) {
			UsedEntity ue = itr.next();
			if(ue != null && ue.getWasExecuted() != null && ue.getWasExecuted()) {
				isManual = false;
			}
		}
		
		return isManual ? ActivityType.MANUAL : ActivityType.CODE_EXECUTION;
	}
	
	/**
	 * Returns the first executed entity in the used list
	 * @param activity
	 * @return null if no executed entities are found
	 */
	public static UsedEntity getExecuted(Activity activity) {
		if(activity == null) return null;
		
		Set<UsedEntity> used = activity.getUsed();
		// look for executed
		Iterator<UsedEntity> itr = used.iterator();
		while(itr.hasNext()) {
			UsedEntity ue = itr.next();
			if(ue != null && ue.getWasExecuted() != null && ue.getWasExecuted()) {
				return ue;
			}
		}
		
		return null;		
	}
}
