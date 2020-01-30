package org.sagebionetworks.web.client.widget.sharing;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityTypeUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditor.HasChangesHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AccessControlListModalWidgetImpl implements AccessControlListModalWidget, AccessControlListModalWidgetView.Presenter, HasChangesHandler {

	public static final String OK = "OK";
	public static final String CANCEL = "Cancel";
	AccessControlListModalWidgetView view;
	AccessControlListEditor editor;
	Callback changeCallback;

	@Inject
	public AccessControlListModalWidgetImpl(AccessControlListModalWidgetView view, AccessControlListEditor editor) {
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
		String entityTypeName = EntityTypeUtils.getDisplayName(EntityTypeUtils.getEntityTypeForClass(entity.getClass()));
		view.setTitle(entityTypeName + " Sharing Settings");
		if (canChangePermission) {
			view.setPrimaryButtonVisible(true);
			view.setDefaultButtonText(CANCEL);
		} else {
			view.setPrimaryButtonVisible(false);
			view.setDefaultButtonText(OK);
		}
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void onPrimary() {
		view.setLoading(true);
		editor.pushChangesToSynapse(false, new Callback() {
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
