package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.SynapseView;

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
	
	/**
	 * If user indicates that data is sensitive, then view will invoke callback to lockdown the current entity 
	 * @param imposeRestrictionsCallback
	 */
	public void showVerifyDataSensitiveDialog();
	
	void showControlledUseUI();
	void showUnmetRequirementsIcon();
	void showMetRequirementsIcon();
	
	void showNoRestrictionsUI();
	void showFlagUI();
	void showAnonymousFlagUI();
	void showChangeLink(ClickHandler changeLinkClickHandler);
	void showShowLink(ClickHandler showLinkClickHandler);
	
	void showFlagModal();
	void showAnonymousFlagModal();
	void setAccessRequirementDialog(Widget dialog);
	
	void setImposeRestrictionOkButtonEnabled(boolean enable);
	void setNotSensitiveHumanDataMessageVisible(boolean visible);
	Boolean isYesHumanDataRadioSelected();
	Boolean isNoHumanDataRadioSelected();
	
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void flagData();
		void anonymousFlagModalOkClicked();
		void reportIssueClicked();
		void anonymousReportIssueClicked();
		void imposeRestrictionClicked();
		
		void yesHumanDataClicked();
		void notHumanDataClicked();
	}

}
