package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;

import org.gwtbootstrap3.client.ui.ModalSize;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditor.HasChangesHandler;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget.UpdateOrderHintCallback;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidgetView;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpagesOrderEditorModalWidgetImpl implements	WikiSubpagesOrderEditorModalWidget,
																WikiSubpagesOrderEditorModalWidgetView.Presenter,
																HasChangesHandler {
	
	WikiSubpagesOrderEditorModalWidgetView view;
	WikiSubpagesOrderEditor editor;
	UpdateOrderHintCallback updateOrderHintCallback;
	
	@Inject
	public WikiSubpagesOrderEditorModalWidgetImpl(
			WikiSubpagesOrderEditorModalWidgetView view,	
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
	public void configure(List<V2WikiHeader> wikiHeaders, String ownerObjectName) {
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

}
