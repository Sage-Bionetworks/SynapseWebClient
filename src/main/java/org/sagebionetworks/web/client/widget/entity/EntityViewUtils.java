package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;

public class EntityViewUtils {

	public static String restrictionDescriptor(RESTRICTION_LEVEL restrictionLevel) {
		switch (restrictionLevel) {
		case OPEN:
			return DisplayConstants.NONE;
		case RESTRICTED:
		case CONTROLLED:
			return DisplayConstants.CONTROLLED_USE;
		default:
			throw new IllegalArgumentException(restrictionLevel.toString());
		}
	}
}
