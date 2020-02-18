package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import org.sagebionetworks.web.client.security.AuthenticationController;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DownloadListWidget implements IsWidget {
	DownloadListWidgetView view;
	AuthenticationController authController;
	@Inject
	public DownloadListWidget(DownloadListWidgetView view) {
		this.view = view;
	}
	
	public void refresh() {
		view.refreshView();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
