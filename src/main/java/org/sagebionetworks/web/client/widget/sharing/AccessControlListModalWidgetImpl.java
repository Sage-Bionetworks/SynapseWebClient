package org.sagebionetworks.web.client.widget.sharing;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditor.HasChangesHandler;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AccessControlListModalWidgetImpl implements AccessControlListModalWidget, AccessControlListModalWidgetView.Presenter, HasChangesHandler {

	AccessControlListModalWidgetView view;
	AccessControlListEditor editor;
	Callback changeCallback;
	
	@Inject
	public AccessControlListModalWidgetImpl(
			AccessControlListModalWidgetView view,
			AccessControlListEditor editor) {
		super();
		this.view = view;
		this.editor = editor;
		this.view.setPresenter(this);
		this.view.addEditor(this.editor.asWidget());
	}

	@Override
	public void showSharing(Callback changeCallback) {
		this.changeCallback = changeCallback;
		this.editor.refresh();
		view.setLoading(false);
		view.showDialog();
	}

	@Override
	public void configure(Entity entity, boolean canChangePermission) {
		editor.configure(entity, canChangePermission, this);
		if(canChangePermission){
			view.setPrimaryButtonVisible(true);
			view.setDefaultButtonText("Cancel");
		}else{
			view.setPrimaryButtonVisible(false);
			view.setDefaultButtonText("OK");
		}
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void onPrimary() {
		view.setLoading(true);
		editor.pushChangesToSynapse(false, this.changeCallback);
		view.hideDialog();
	}

	@Override
	public void hasChanges(boolean hasChanges) {
		view.setPrimaryButtonEnabled(hasChanges);
	}

}
