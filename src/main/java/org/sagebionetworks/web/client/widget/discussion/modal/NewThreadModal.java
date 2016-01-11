package org.sagebionetworks.web.client.widget.discussion.modal;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A simple modal dialog for adding a new thread.
 */
public class NewThreadModal implements NewThreadModalView.Presenter{

	private NewThreadModalView view;

	@Inject
	public NewThreadModal(
			NewThreadModalView view
			) {
		this.view = view;
		view.setPresenter(this);
	}

	@Override
	public void show() {
		view.showDialog();
	}

	@Override
	public void hide() {
		view.hideDialog();
	}

	@Override
	public void onSave() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCancel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

}
