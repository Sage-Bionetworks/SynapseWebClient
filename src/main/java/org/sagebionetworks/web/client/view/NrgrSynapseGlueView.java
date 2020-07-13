package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface NrgrSynapseGlueView extends IsWidget {
	void setSynAlert(IsWidget w);
	void refreshHeader();
	void setPresenter(Presenter presenter);
	String getNrgrToken();
	void setNrgrToken(String token);

	public interface Presenter {
		void onSubmitToken();
	}
}
