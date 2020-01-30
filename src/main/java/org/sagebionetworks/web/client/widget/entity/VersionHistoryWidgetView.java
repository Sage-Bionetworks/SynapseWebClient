package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.web.client.SynapseView;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * @author jayhodgson
 *
 */
public interface VersionHistoryWidgetView extends IsWidget, SynapseView {

	interface Presenter {
		void updateVersionInfo(String newLabel, String newComment);

		void deleteVersion(Long versionNumber);

		void onEditVersionInfoClicked();

		void onMore();
	}

	void setEntityBundle(Entity entity, boolean autoShowFileHistory);

	void setPresenter(Presenter presenter);

	void clearVersions();

	void addVersion(String entityId, VersionInfo version, boolean canEdit, boolean isVersionSelected);

	void setEditVersionInfoButtonVisible(boolean isVisible);

	void showEditVersionInfo(String oldLabel, String oldComment);

	void showEditVersionInfoError(String error);

	void hideEditVersionInfo();

	void setMoreButtonVisible(boolean visible);

	void setSynAlert(IsWidget w);

	void showNoResults();
}
