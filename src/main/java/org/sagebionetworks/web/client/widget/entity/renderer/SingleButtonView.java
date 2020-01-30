package org.sagebionetworks.web.client.widget.entity.renderer;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.web.client.utils.Callback;
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
	 * 
	 * @param string
	 */
	void setButtonText(String string);

	/**
	 * Set the type of the button
	 * 
	 * @param type
	 */
	void setButtonType(ButtonType type);

	/**
	 * Show/hide the button.
	 * 
	 * @param b
	 */
	void setButtonVisible(boolean visible);

	/**
	 * Enable/disable the button.
	 * 
	 * @param b
	 */
	void setButtonEnabled(boolean enabled);

	/**
	 * Bind this view to its presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	void setDataLoadingText(String loadingText);

	/**
	 * Set the loading state
	 * 
	 * @param b
	 */
	void setLoading(boolean loading);

	void setButtonSize(ButtonSize size);

	void showConfirmDialog(String message, Callback okCallback);

	void addWidget(Widget widget);

	void clearWidgets();

	void addStyleNames(String styles);

	void setButtonIcon(IconType icon);
}
