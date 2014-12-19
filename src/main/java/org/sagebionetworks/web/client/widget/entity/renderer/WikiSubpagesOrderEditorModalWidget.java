package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;

import org.gwtbootstrap3.client.ui.ModalSize;
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
	 * Change the size of the modal.
	 * @param size The new size of the modal.
	 */
	public void setSize(ModalSize size);
	
	/**
	 * The widget must be configured before showing the dialog.
	 * @param subpagesTree
	 */
	public void configure(WikiSubpageOrderEditorTree subpagesTree, Callback udpateOrderCallback);
	
	public WikiSubpageOrderEditorTree getTree();
//	public List<String> getCurrentOrderIdList();
	
}
