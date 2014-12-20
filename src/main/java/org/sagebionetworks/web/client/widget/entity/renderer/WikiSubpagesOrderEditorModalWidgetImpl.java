package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;

import org.gwtbootstrap3.client.ui.ModalSize;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditor.HasChangesHandler;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget.UpdateOrderHintCallback;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidgetView;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpagesOrderEditorModalWidgetImpl implements	WikiSubpagesOrderEditorModalWidget,
																AccessControlListModalWidgetView.Presenter,
																HasChangesHandler {
	
	AccessControlListModalWidgetView view;	// TODO: Generalize this ACLModalWidgetView
	WikiSubpagesOrderEditor editor;
	UpdateOrderHintCallback updateOrderHintCallback;
	
	@Inject
	public WikiSubpagesOrderEditorModalWidgetImpl(
			AccessControlListModalWidgetView view,	
			WikiSubpagesOrderEditor editor) {
		super();
		this.view = view;
		this.editor = editor;
		this.view.setPresenter(this);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void show(UpdateOrderHintCallback updateOrderHintCallback) {
		this.updateOrderHintCallback = updateOrderHintCallback;
		//hasChanges(false);
		view.setLoading(false);
		view.showDialog();
		editor.initializeState();
	}

	@Override
	public void configure(List<JSONEntity> wikiHeaders, String ownerObjectName) {
		editor.configure(wikiHeaders, ownerObjectName, this);
		view.addEditor(editor.asWidget());
	}

	@Override
	public void onPrimary() {
		view.setLoading(true);
		updateOrderHintCallback.updateOrderHint(editor.getTree().getIdListOrderHint());
		view.hideDialog();
	}

	@Override
	public void hasChanges(boolean hasChanges) {
		view.setLoading(false);
		view.setPrimaryButtonEnabled(hasChanges);
	}

	@Override
	public WikiSubpageOrderEditorTree getTree() {
		return editor.getTree();
	}
	
	@Override
	public void setSize(ModalSize size) {
		view.setSize(size);
	}

}
