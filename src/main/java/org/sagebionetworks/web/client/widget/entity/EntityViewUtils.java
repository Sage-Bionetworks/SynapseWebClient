package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.constants.Placement;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

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
	
	public static ImageResource getShieldIcon(RESTRICTION_LEVEL restrictionLevel, IconsImageBundle iconsImageBundle) {
		switch (restrictionLevel) {
		case OPEN:
			return null;
		case RESTRICTED:
		case CONTROLLED:
			return iconsImageBundle.shieldRed16();
		default:
			throw new IllegalArgumentException(restrictionLevel.toString());
		}
	}
	
	
}
