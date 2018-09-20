package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface DownloadListWidgetView extends IsWidget {
	void setPresenter(Presenter p);
	void setSynAlert(IsWidget w);
	void clear();
	public interface Presenter {
		void onClearDownloadList();
	}
}
