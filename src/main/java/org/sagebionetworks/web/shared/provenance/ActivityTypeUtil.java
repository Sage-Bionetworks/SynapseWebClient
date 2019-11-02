package org.sagebionetworks.web.shared.provenance;

import java.util.Iterator;
import java.util.Set;
import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.provenance.Used;

public class ActivityTypeUtil {

	public static ActivityType get(Activity activity) {
		if (activity == null || activity.getCreatedBy() == null)
			return ActivityType.UNDEFINED;

		Set<Used> used = activity.getUsed();
		if (used == null)
			return ActivityType.MANUAL;

		// look for executed
		boolean isManual = true;
		Iterator<Used> itr = used.iterator();
		while (itr.hasNext()) {
			Used ue = itr.next();
			if (ue != null && ue.getWasExecuted() != null && ue.getWasExecuted()) {
				isManual = false;
				break;
			}
		}

		return isManual ? ActivityType.MANUAL : ActivityType.CODE_EXECUTION;
	}

	/**
	 * Returns the first executed entity in the used list
	 * 
	 * @param activity
	 * @return null if no executed entities are found
	 */
	public static Used getExecuted(Activity activity) {
		if (activity == null)
			return null;

		Set<Used> used = activity.getUsed();
		if (used == null || used.size() == 0)
			return null;
		// look for executed
		Iterator<Used> itr = used.iterator();
		while (itr.hasNext()) {
			Used ue = itr.next();
			if (ue != null && ue.getWasExecuted() != null && ue.getWasExecuted()) {
				return ue;
			}
		}

		return null;
	}
}
