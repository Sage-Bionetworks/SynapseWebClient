package org.sagebionetworks.web.client.widget.sharing;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.sharing.EvaluationAccessControlListEditor.HasChangesHandler;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EvaluationAccessControlListModalWidgetImpl implements EvaluationAccessControlListModalWidget, AccessControlListModalWidgetView.Presenter, HasChangesHandler {

	public static final String OK = "OK";
	public static final String CANCEL = "Cancel";
	AccessControlListModalWidgetView view;
	EvaluationAccessControlListEditor editor;
	Callback changeCallback;
	
	@Inject
	public EvaluationAccessControlListModalWidgetImpl(
			AccessControlListModalWidgetView view,
			EvaluationAccessControlListEditor editor) {
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
	public void configure(Evaluation entity) {
		editor.configure(entity, this);
		view.setPrimaryButtonVisible(true);
		view.setDefaultButtonText(CANCEL);
	}

	@Override
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
