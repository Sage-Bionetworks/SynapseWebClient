package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author jayhodgson
 *
 */
public interface FileHistoryWidgetView extends IsWidget, SynapseView{
	
	interface Presenter {
		void updateVersionInfo(String newLabel, String newComment);
		void deleteVersion(Long versionNumber);
		void onEditVersionInfoClicked();
	}
	
	void setEntityBundle(Entity entity, boolean autoShowFileHistory);
	void setPresenter(Presenter presenter);
	void setPaginationWidget(Widget widget);
	void clearVersions();
	void addVersion(VersionInfo version, boolean canEdit, boolean isVersionSelected);
	void setEditVersionInfoButtonVisible(boolean isVisible);
	void showEditVersionInfo(String oldLabel, String oldComment);
	void hideEditVersionInfo();
}
