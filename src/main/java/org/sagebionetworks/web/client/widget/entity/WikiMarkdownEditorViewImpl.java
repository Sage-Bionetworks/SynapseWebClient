package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Lightweight widget used to resolve markdown
 * 
 * @author Jay
 *
 */
public class WikiMarkdownEditorViewImpl implements WikiMarkdownEditorView {

	public interface Binder extends UiBinder<Widget, WikiMarkdownEditorViewImpl> {
	}

	private Presenter presenter;

	@UiField
	public Modal editorDialog;
	@UiField
	public TextBox titleField;
	// dialog for the formatting guide
	@UiField
	public Div markdownEditorContainer;

	@UiField
	public Button saveButton;
	@UiField
	public Button cancelButton;
	@UiField
	public Button deleteButton;

	// this UI widget
	Widget widget;



	@Inject
	public WikiMarkdownEditorViewImpl(Binder binder) {
		super();
		this.widget = binder.createAndBindUi(this);
		ClickHandler onCancel = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.cancelClicked();
			}
		};

		ClickHandler onSave = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.saveClicked();
			}
		};
		ClickHandler onDelete = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.deleteClicked();
			}
		};

		saveButton.addClickHandler(onSave);
		cancelButton.addClickHandler(onCancel);
		cancelButton.addDomHandler(DisplayUtils.getPreventTabHandler(cancelButton), KeyDownEvent.getType());
		editorDialog.addCloseHandler(onCancel);
		deleteButton.addClickHandler(onDelete);
		editorDialog.addDomHandler(DisplayUtils.getESCKeyDownHandler(onCancel), KeyDownEvent.getType());
	}

	@Override
	public void setSaving(boolean isSaving) {
		if (isSaving)
			saveButton.state().loading();
		else
			saveButton.state().reset();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void showEditorModal() {
		editorDialog.show();
	}

	@Override
	public void hideEditorModal() {
		editorDialog.hide();
	}

	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void clear() {
		saveButton.state().reset();
	}

	@Override
	public void setTitleEditorVisible(boolean visible) {
		titleField.setVisible(visible);
	}

	@Override
	public String getTitle() {
		return titleField.getValue();
	}

	@Override
	public void setTitle(String title) {
		titleField.setValue(title);
	}

	@Override
	public void setMarkdownEditorWidget(Widget markdownEditorWidget) {
		markdownEditorContainer.clear();
		markdownEditorContainer.add(markdownEditorWidget);
	}

	@Override
	public void setDeleteButtonVisible(boolean visible) {
		deleteButton.setVisible(visible);
	}
}
