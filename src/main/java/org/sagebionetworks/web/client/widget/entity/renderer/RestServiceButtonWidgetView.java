package org.sagebionetworks.web.client.widget.entity.renderer;

import org.gwtbootstrap3.client.ui.constants.ButtonType;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface RestServiceButtonWidgetView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	void configure(String buttonText, ButtonType buttonType);
	void setSynapseAlert(Widget widget);
	
	void showSuccessMessage();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onClick();
	}
}
