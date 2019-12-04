package org.sagebionetworks.web.client.widget.sharing;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for a dialog that shows an ACL editor for an evaluation
 * 
 * @author jay
 *
 */
public interface EvaluationAccessControlListModalWidget extends IsWidget {

	/**
	 * Show the sharing dialog.
	 */
	public void show();

	/**
	 * The widget must be configured before showing the dialog.
	 * 
	 * @param evaluation
	 */
	public void configure(Evaluation evaluation, Callback changeCallback);

}
