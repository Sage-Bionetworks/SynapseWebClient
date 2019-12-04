package org.sagebionetworks.web.client.widget.entity.act;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface RevokeUserAccessModalView extends IsWidget {

	void setPresenter(Presenter presenter);

	void setSynAlert(Widget asWidget);

	void setUserPickerWidget(Widget w);

	void setRevokeProcessing(boolean processing);

	void showInfo(String message);

	void show();

	void hide();

	public interface Presenter {
		void onRevoke();
	}

}
