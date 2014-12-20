package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;

import org.gwtbootstrap3.client.ui.ModalSize;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget.UpdateOrderHintCallback;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Tree;

public interface WikiSubpagesOrderEditorModalWidget extends IsWidget {

	/**
	 * Show the sharing dialog.
	 * @param changeCallback
	 */
	public void show(UpdateOrderHintCallback updateOrderHintCallback);
	
	public WikiSubpageOrderEditorTree getTree();

	void configure(List<JSONEntity> wikiHeaders, String ownerObjectName);
	
}
