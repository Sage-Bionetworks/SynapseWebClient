package org.sagebionetworks.web.client.widget.entity.file;

import com.google.gwt.user.client.ui.IsWidget;

public interface AddToDownloadListView extends IsWidget {
	void hideAll();
	void setPackageSizeSummary(IsWidget w);
	void showConfirmAdd();
	void setAsynchronousProgressWidget(IsWidget w);
	void showAsynchronousProgressWidget();
	void showSuccess(int fileCount);
	void add(IsWidget w);
	void setPresenter(Presenter p);
	public interface Presenter {
		void onConfirmAddToDownloadList();
	}
}
