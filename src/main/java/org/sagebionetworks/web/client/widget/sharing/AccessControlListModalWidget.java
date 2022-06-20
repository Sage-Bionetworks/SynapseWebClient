package org.sagebionetworks.web.client.widget.sharing;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for a dialog that shows an ACL editor for an entity.
 * 
 * @author jhill
 *
 */
public interface AccessControlListModalWidget extends IsWidget {

	/**
	 * Show the sharing dialog.
	 * 
	 * @param changeCallback
	 */
	public void showSharing(Callback changeCallback);

	/**
	 * The widget must be configured before showing the dialog.
	 * 
	 * @param entity
	 * @param canChangePermission
	 */
	public void configure(Entity entity, boolean canChangePermission);

}
