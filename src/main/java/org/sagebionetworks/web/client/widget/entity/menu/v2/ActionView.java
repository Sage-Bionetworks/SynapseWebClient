package org.sagebionetworks.web.client.widget.entity.menu.v2;

import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for an individual action.
 * 
 * @author jhill
 *
 */
public interface ActionView extends IsWidget {
	
	interface Presenter {
		/**
		 * The action was selected.
		 * @param action
		 */
		void onClicked(Action action);
	}

	void setText(String text);

	void setIcon(IconType icon);

	void setEnabled(boolean enabled);

	void setVisible(boolean visible);
	
	/**
	 * Bind this view to its presenter and its action.
	 * @param presenter
	 * @param action
	 */
	void setPresenter(Presenter presenter, Action action);
}
