package org.sagebionetworks.web.client.widget.table.modal.download;

import com.google.gwt.user.client.ui.IsWidget;

public interface DownloadFilePageView extends IsWidget {

	void setAction(String url);

	void submit();

}
