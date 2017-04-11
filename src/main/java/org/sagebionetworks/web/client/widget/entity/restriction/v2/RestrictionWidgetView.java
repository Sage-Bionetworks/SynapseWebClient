package org.sagebionetworks.web.client.widget.entity.restriction.v2;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

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
	void showChangeLink();
	void showShowLink();
	void showShowUnmetLink();
	
	void showFlagModal();
	void showAnonymousFlagModal();
	
	void setImposeRestrictionOkButtonEnabled(boolean enable);
	void setNotSensitiveHumanDataMessageVisible(boolean visible);
	Boolean isYesHumanDataRadioSelected();
	Boolean isNoHumanDataRadioSelected();
	void setSynAlert(IsWidget w);
	public void setImposeRestrictionModalVisible(boolean visible);

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void flagData();
		void anonymousFlagModalOkClicked();
		void reportIssueClicked();
		void anonymousReportIssueClicked();
		void imposeRestrictionOkClicked();
		void imposeRestrictionCancelClicked();

		void yesHumanDataClicked();
		void notHumanDataClicked();
		void linkClicked();
	}
}
