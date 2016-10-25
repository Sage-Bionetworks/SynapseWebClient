package org.sagebionetworks.web.client.widget.entity.act;

import java.util.List;

import org.sagebionetworks.repo.model.AccessRequirement;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ApproveUserAccessModalView extends IsWidget {

	void setPresenter(Presenter presenter);
	void setSynAlert(Widget asWidget);
	void setStates(List<String> states);
	void setUserPickerWidget(Widget w);
	String getAccessRequirement();
	void setAccessRequirement(String num, String text);
	void setProcessing(boolean processing);
	void setEmailButtonText(String string);
	void showInfo(String string);
	void show();
	void hide();
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onSubmit();

		void onStateSelected(String state);

		void selectEmail();

		void sendEmail();
	}


}
