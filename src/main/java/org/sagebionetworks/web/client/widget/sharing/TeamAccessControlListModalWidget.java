package org.sagebionetworks.web.client.widget.sharing;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.sharing.TeamAccessControlListEditor.HasChangesHandler;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamAccessControlListModalWidget implements AccessControlListModalWidgetView.Presenter, HasChangesHandler {

	public static final String OK = "OK";
	public static final String CANCEL = "Cancel";
	AccessControlListModalWidgetView view;
	TeamAccessControlListEditor editor;
	Callback changeCallback;
	
	@Inject
	public TeamAccessControlListModalWidget(
			AccessControlListModalWidgetView view,
			TeamAccessControlListEditor editor) {
		super();
		this.view = view;
		this.editor = editor;
		this.view.setPresenter(this);
		this.view.addEditor(this.editor.asWidget());
	}

	public void showSharing(Callback changeCallback) {
		this.changeCallback = changeCallback;
		this.editor.refresh();
		view.setLoading(false);
		view.showDialog();
	}

	public void configure(Team team) {
		editor.configure(team, this);
		view.setPrimaryButtonVisible(true);
		view.setDefaultButtonText(CANCEL);
	}

	public Widget asWidget() {
		return view.asWidget();
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
