package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import com.google.gwt.user.client.ui.IsWidget;

public interface FileHandleAssociationRowView extends IsWidget {
	void setFileNameWidget(IsWidget w);
	void setHasAccess(boolean hasAccess);
	void setCreatedBy(String createdBy);
	void setCreatedOn(String createdOn);
	void setFileSize(String fileSize);
	void setPresenter(Presenter p);
	boolean isAttached();
	public interface Presenter {
		void onRemove();
	}
}
