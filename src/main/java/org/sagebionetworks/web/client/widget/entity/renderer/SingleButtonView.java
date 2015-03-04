package org.sagebionetworks.web.client.widget.entity.renderer;

import org.gwtbootstrap3.extras.bootbox.client.callback.ConfirmCallback;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
public interface SingleButtonView extends IsWidget {
	
	interface Presenter {
		/**
		 * Called when the user clicks the button.
		 */
		public void onClick();
	}

	/**
	 * Set the text on the button
	 * @param string
	 */
	void setButtonText(String string);
	
	/**
	 * Show/hide the button.
	 * @param b
	 */
	void setButtonVisible(boolean visible);

	/**
	 * Enable/disable the button.
	 * @param b
	 */
	void setButtonEnabled(boolean enabled);

	/**
	 * Bind this view to its presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Set the loading state
	 * @param b
	 */
	void setLoading(boolean loading);
	
	void showConfirmDialog(String message, ConfirmCallback okCallback);
	void addWidget(Widget widget);
	void clearWidgets();
}
