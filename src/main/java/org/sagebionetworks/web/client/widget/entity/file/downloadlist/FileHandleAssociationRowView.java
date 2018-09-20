package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import com.google.gwt.user.client.ui.IsWidget;

public interface FileHandleAssociationRowView extends IsWidget {
	void setFileNameWidget(IsWidget w);
	void setHasAccess(boolean hasAccess);
	void setCreatedBy(IsWidget w);
	void setCreatedOn(String createdOn);
	void setFileSize(String fileSize);
	void setEntityId(String entityId);
	void setPresenter(Presenter p);
	public interface Presenter {
		void onDelete();
	}
}
