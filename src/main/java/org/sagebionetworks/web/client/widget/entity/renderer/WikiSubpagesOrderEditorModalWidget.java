package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Tree;

public interface WikiSubpagesOrderEditorModalWidget extends IsWidget {

	/**
	 * Show the sharing dialog.
	 * @param changeCallback
	 */
	public void show(Callback changeCallback);
	
	/**
	 * The widget must be configured before showing the dialog.
	 * @param subpagesTree
	 */
	public void configure(Tree subpagesTree, Callback udpateOrderCallback);
	
	public Tree getTree();
	public List<String> getCurrentOrderIdList();
	
}
