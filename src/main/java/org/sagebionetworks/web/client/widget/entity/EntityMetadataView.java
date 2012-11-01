package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface EntityMetadataView extends IsWidget {

	public void setPresenter(Presenter p);

	public void setEntityBundle(EntityBundle bundle);

	public interface Presenter {

		void loadVersions(String id, int offset, int limit,
				AsyncCallback<PaginatedResults<VersionInfo>> asyncCallback);

	}
}
