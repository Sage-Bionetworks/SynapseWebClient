package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.APPROVAL_TYPE;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RestrictionWidgetViewImpl implements RestrictionWidgetView {
	
	IconsImageBundle iconsImageBundle;
	SynapseJSNIUtils synapseJSNIUtils;
	Presenter presenter;
	
	@Inject
	public RestrictionWidgetViewImpl(IconsImageBundle iconsImageBundle, SynapseJSNIUtils synapseJSNIUtils) {
		this.iconsImageBundle = iconsImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter=presenter;
	}

	@Override
	public Widget asWidget(String jiraFlagLink,
			boolean isAnonymous, 
			boolean hasAdministrativeAccess,
			Callback loginCallback, 
			RESTRICTION_LEVEL restrictionLevel,
			ClickHandler aboutLinkClickHandler,
			boolean showFlagLink, 
			boolean showChangeLink) {

		return EntityViewUtils.createRestrictionsWidget(
				jiraFlagLink,
				isAnonymous,
				hasAdministrativeAccess,
				loginCallback,
				restrictionLevel,
				iconsImageBundle,
				synapseJSNIUtils,
				aboutLinkClickHandler,
				showFlagLink,
				showChangeLink);
	}
	
	@Override
	public void showAccessRequirement(RESTRICTION_LEVEL restrictionLevel,
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
			Callback onHideCallback) {
		GovernanceDialogHelper.showAccessRequirement(
				restrictionLevel,
				approvalType,
				isAnonymous,
				hasAdministrativeAccess,
				hasFulfilledAccessRequirements,
				iconsImageBundle,
				accessRequirementText,
				imposeRestrictionsCallback,
				touAcceptanceCallback,
				requestACTCallback,
				loginCallback,
				jiraFlagLink, 
				onHideCallback);	
	}
	
	@Override
	public void showVerifyDataSensitiveDialog(
			Callback imposeRestrictionsCallback) {
		//callback invoked when the NO button is clicked
		Callback noCallback = new Callback() {
			@Override
			public void invoke() {
				DisplayUtils.showErrorMessage(DisplayConstants.IS_SENSITIVE_DATA_CONTACT_ACT_MESSAGE);
			}
		};
		DisplayUtils.showYesNoMessage("", DisplayConstants.IS_SENSITIVE_DATA_MESSAGE, MessageBox.QUESTION, 360, imposeRestrictionsCallback, noCallback);
	}
	
	@Override
	public void open(String url) {
		Window.open(url, "_blank", "");	
	}
	
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	@Override
	public void showLoading() {
	}
	
	@Override
	public void clear() {
	}
	
	@Override
	public Widget asWidget() {
		return null;
	}
	/*
	 * Private Methods
	 */

}
