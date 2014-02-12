package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.APPROVAL_TYPE;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;

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
			boolean isAnonymous, boolean hasAdministrativeAccess,
			String accessRequirementText, Callback touAcceptanceCallback,
			Callback requestACTCallback, Callback imposeRestrictionsCallback,
			Callback loginCallback, RESTRICTION_LEVEL restrictionLevel,
			APPROVAL_TYPE approvalType, boolean hasFulfilledAccessRequirements,
			boolean showFlagLink, boolean showChangeLink) {
		return EntityViewUtils.createRestrictionsWidget(
				jiraFlagLink,
				isAnonymous,
				hasAdministrativeAccess,
				accessRequirementText,
				touAcceptanceCallback,
				requestACTCallback,
				imposeRestrictionsCallback,
				loginCallback,
				restrictionLevel,
				approvalType,
				hasFulfilledAccessRequirements,
				iconsImageBundle,
				synapseJSNIUtils,
				showFlagLink,
				showChangeLink);
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
