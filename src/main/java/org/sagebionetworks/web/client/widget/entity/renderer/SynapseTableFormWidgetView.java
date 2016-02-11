package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface SynapseTableFormWidgetView extends IsWidget {

	
	void setSynAlertWidget(Widget w);
	void setRowFormWidget(Widget w);
	void setProgressWidget(Widget w);
	void setPresenter(Presenter p);
	void setSubmitButtonLoading(boolean isLoading);
	void setSuccessMessageVisible(boolean visible);
	void setSuccessMessage(String text);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onSubmit();
	}
}
