package org.sagebionetworks.web.client.widget.entity.restriction.v2;

import org.sagebionetworks.web.client.SynapseView;
import com.google.gwt.user.client.ui.IsWidget;

public interface RestrictionWidgetView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	void configureCurrentAccessComponent(String entityId, Long versionNumber);
	void showFlagUI();
	void showFolderRestrictionsLink(String entityId);
	void showChangeLink();

	void showVerifyDataSensitiveDialog();

	void setNotSensitiveHumanDataMessageVisible(boolean visible);

	Boolean isYesHumanDataRadioSelected();

	Boolean isNoHumanDataRadioSelected();

	void setSynAlert(IsWidget w);

	public void setImposeRestrictionModalVisible(boolean visible);

	void showFolderRestrictionUI();

	void showNoRestrictionsUI();

	void showControlledUseUI();


	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void changeClicked();

		void reportIssueClicked();

		void imposeRestrictionOkClicked();

		void imposeRestrictionCancelClicked();

		void yesHumanDataClicked();

		void notHumanDataClicked();
	}
}
