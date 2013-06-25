package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.doi.DoiStatus;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface DoiWidgetView extends IsWidget, SynapseView {

	void setPresenter(Presenter presenter);
	void showCreateDoi();
	void showDoi(DoiStatus doi);
	public interface Presenter {
		public void createDoi();
		public void getDoiPrefix(AsyncCallback<String> callback);
		public String getDoiHtml(String prefix, boolean isReady);
	}

}
