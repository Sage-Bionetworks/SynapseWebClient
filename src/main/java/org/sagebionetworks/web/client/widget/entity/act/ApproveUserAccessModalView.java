package org.sagebionetworks.web.client.widget.entity.act;

import java.util.List;

import org.sagebionetworks.repo.model.AccessRequirement;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ApproveUserAccessModalView extends IsWidget {

	void setPresenter(Presenter presenter);
	void setStates(List<String> states);
	void setUserPickerWidget(Widget w);
	void enableApprove(boolean enabled);
	void setAccessRequirement(String num, String text);
	void show();
	void hide();
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onSubmit();

		void onStateSelected(String state);
	}


}
