package org.sagebionetworks.web.client.widget.entity.act;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface UserBadgeListView extends IsWidget {
	
	public interface Presenter{
		List<String> getFileHandleIds();
		void deleteSelected();
		void selectNone();
		void selectAll();
	}
	
	void setPresenter(Presenter presenter);
	void setToolbarVisible(boolean visible);
	void setSelectorWidget(Widget widget);
	void setUploadWidgetVisible(boolean visible);
	void addFileLink(Widget fileLinkWidget);
	void clearFileLinks();
	void setCanDelete(boolean canDelete);
}
