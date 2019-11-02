package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.user.client.ui.IsWidget;

public interface ProvenanceURLDialogWidgetView extends IsWidget {

	public interface Presenter {

		void onSave();

		void configure(Callback confirmCallback);

		void show();

		void hide();

		String getURLName();

		String getURLAddress();

	}

	void setPresenter(Presenter presenter);

	void show();

	void hide();

	void clear();

	String getURLAddress();

	String getURLName();

	void setSynAlertWidget(IsWidget synAlert);

}
