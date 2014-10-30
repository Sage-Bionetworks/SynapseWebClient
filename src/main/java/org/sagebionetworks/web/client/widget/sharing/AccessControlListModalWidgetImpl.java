package org.sagebionetworks.web.client.widget.sharing;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AccessControlListModalWidgetImpl implements AccessControlListModalWidget {

	AccessControlListModalWidgetView view;
	AccessControlListEditor editor;
	
	@Inject
	public AccessControlListModalWidgetImpl(
			AccessControlListModalWidgetView view,
			AccessControlListEditor editor) {
		super();
		this.view = view;
		this.editor = editor;
		this.view.addEditor(this.editor.asWidget());
	}

	@Override
	public void showSharing(Callback changeCallback) {
		this.editor.refresh();
		view.showDialog();
	}

	@Override
	public void configure(Entity entity, boolean canChangePermission) {
		editor.setResource(entity, canChangePermission);
		view.setPrimaryButtonEnabled(false);
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

}
