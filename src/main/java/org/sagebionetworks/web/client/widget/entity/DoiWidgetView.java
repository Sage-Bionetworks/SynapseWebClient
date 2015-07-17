package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface DoiWidgetView extends IsWidget, SynapseView {

	void setPresenter(Presenter presenter);
	public interface Presenter {
		public void getDoiPrefix(AsyncCallback<String> callback);
		public String getDoiHtml(String prefix, boolean isReady);
	}
	void showDoiCreated(String doiText);
	void showDoiInProgress();
	void showDoiError();
	void setVisible(boolean visible);

}
