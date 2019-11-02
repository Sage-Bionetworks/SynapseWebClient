package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface SynapseTableFormWidgetView extends IsWidget {


	void setSynAlertWidget(Widget w);

	void setRowFormWidget(Widget w);

	void setPresenter(Presenter p);

	void setSubmitButtonLoading(boolean isLoading);

	void setFormUIVisible(boolean visible);

	void setSuccessMessageVisible(boolean visible);

	void setUserBadge(Widget w);

	void setSuccessMessage(String text);

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onSubmit();

		void onReset();
	}
}
