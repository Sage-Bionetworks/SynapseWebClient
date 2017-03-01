package org.sagebionetworks.web.client.widget.display;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ProjectDisplayView {
	public interface Presenter {

		Widget asWidget();

		void onSave();

		void clear();
		
	}

	Widget asWidget();

	void setSynAlertWidget(IsWidget asWidget);
	
	void setPresenter(Presenter presenter);

	void clear();

	void hide();

	void show();

	void onSave();

	
}