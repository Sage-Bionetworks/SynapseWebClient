package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * @author jayhodgson
 *
 */
public interface FileHistoryWidgetView extends IsWidget, SynapseView{
	
	interface Presenter {
		void loadVersions(String id, int offset, int limit,
					AsyncCallback<PaginatedResults<VersionInfo>> asyncCallback);
		 
		void editCurrentVersionInfo(String entityId, String version, String comment);

		void deleteVersion(String entityId, Long versionNumber);

	}

	void setEntityBundle(EntityBundle bundle, boolean canAdmin, boolean canEdit, boolean autoShowFileHistory);
	void setFileHistoryVisible(boolean visible);
	void setPresenter(Presenter presenter);
}
