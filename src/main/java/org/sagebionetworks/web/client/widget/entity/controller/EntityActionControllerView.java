package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;

import com.google.gwt.user.client.ui.IsWidget;
/**
 * Abstraction for the view 
 * @author John
 *
 */
public interface EntityActionControllerView extends SynapseView, IsWidget {
	
	interface Presenter{
		
		/**
		 * Called if the user confirms an action.
		 * 
		 * @param action The action the user confirmed.
		 */
		public void onConfirmAction(Action action);
	}

	/**
	 * Show the user a confirm dialog.
	 * @param string
	 * @param action
	 */
	void showConfirmDialog(String string, Action action);
	
	/**
	 * Bind this view to its presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Show info to the user.
	 * @param string
	 * @param string
	 */
	void showInfo(String header, String message);

}
