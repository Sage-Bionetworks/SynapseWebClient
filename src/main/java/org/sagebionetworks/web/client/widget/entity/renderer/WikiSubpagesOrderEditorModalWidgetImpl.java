package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditor.HasChangesHandler;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditor;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidgetView;

import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

// TODO: Generalize entire class with ACLModalWidget??
public class WikiSubpagesOrderEditorModalWidgetImpl implements	WikiSubpagesOrderEditorModalWidget,
																AccessControlListModalWidgetView.Presenter,
																HasChangesHandler {
	
	AccessControlListModalWidgetView view;	// TODO: Generalize this ACLModalWidgetView
	WikiSubpagesOrderEditor editor;
	Callback changeCallback;
	
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
	public void show(Callback changeCallback) {
		this.changeCallback = changeCallback;
		//this.editor.refresh();
		view.setLoading(false);
		view.showDialog();
	}

	@Override
	public void configure(Tree subpagesTree) {
		editor.configure(subpagesTree, this);
		view.addEditor(editor.asWidget());
	}

	@Override
	public void onPrimary() {
		view.setLoading(true);
		editor.pushChangesToSynapse(new Callback() {
			@Override
			public void invoke() {
				view.hideDialog();
				changeCallback.invoke();
			}
		});
	}

	@Override
	public void hasChanges(boolean hasChanges) {
		view.setLoading(false);
		view.setPrimaryButtonEnabled(hasChanges);
	}
}
