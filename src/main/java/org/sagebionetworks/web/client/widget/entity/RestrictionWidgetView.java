package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.APPROVAL_TYPE;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface RestrictionWidgetView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void open(String url);
	
	public Widget asWidget(
			 String jiraFlagLink, 
			 boolean isAnonymous, 
			 boolean hasAdministrativeAccess,
			 String accessRequirementText,
			 Callback touAcceptanceCallback,
			 Callback requestACTCallback,
			 Callback imposeRestrictionsCallback,
			 Callback loginCallback,
			 RESTRICTION_LEVEL restrictionLevel, 
			 APPROVAL_TYPE approvalType,
			 boolean hasFulfilledAccessRequirements);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

	}

}
