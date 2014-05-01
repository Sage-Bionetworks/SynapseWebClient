package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.APPROVAL_TYPE;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface RestrictionWidgetView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void open(String url);
	
	public Widget asWidget(String jiraFlagLink,
			boolean isAnonymous, 
			boolean hasAdministrativeAccess,
			Callback loginCallback, 
			RESTRICTION_LEVEL restrictionLevel,
			ClickHandler aboutLinkClickHandler,
			boolean showFlagLink, 
			boolean showChangeLink);
	
	public void showAccessRequirement(
			 RESTRICTION_LEVEL restrictionLevel, 
			 APPROVAL_TYPE approvalType,
			 boolean isAnonymous,
			 boolean hasAdministrativeAccess,
			 boolean hasFulfilledAccessRequirements,
			 IconsImageBundle iconsImageBundle,
			 String accessRequirementText,  
			 Callback imposeRestrictionsCallback, 
			 Callback touAcceptanceCallback, 
			 Callback requestACTCallback,
			 Callback loginCallback,
			 String jiraFlagLink,
			 Callback showNextRestrictionCallback);
	/**
	 * If user indicates that data is sensitive, then view will invoke callback to lockdown the current entity 
	 * @param imposeRestrictionsCallback
	 */
	public void showVerifyDataSensitiveDialog(Callback imposeRestrictionsCallback);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

	}

}
