package org.sagebionetworks.web.client.widget.entity.download;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AddFolderDialogWidgetViewImpl implements AddFolderDialogWidgetView {
	private Presenter presenter;
	@UiField
	Modal newFolderDialog;
	@UiField
	TextBox folderNameField;
	@UiField
	Div sharingAndDataUseContainer;
	@UiField
	Button cancelNewFolderButton;
	@UiField
	Button okNewFolderButton;
	@UiField
	Div synAlertContainer;
	Widget w;

	public interface Binder extends UiBinder<Widget, AddFolderDialogWidgetViewImpl> {
	}

	@Inject
	public AddFolderDialogWidgetViewImpl(Binder uiBinder) {
		w = uiBinder.createAndBindUi(this);
		okNewFolderButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.createFolder(folderNameField.getText());
			}
		});
		ClickHandler deleteCancelledHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		};
		newFolderDialog.addCloseHandler(deleteCancelledHandler);
		cancelNewFolderButton.addClickHandler(deleteCancelledHandler);
		folderNameField.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					okNewFolderButton.click();
				}
			}
		});
		newFolderDialog.addDomHandler(DisplayUtils.getESCKeyDownHandler(deleteCancelledHandler), KeyDownEvent.getType());
	}

	@Override
	public Widget asWidget() {
		return w;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void show() {
		folderNameField.setText("");
		setSaveEnabled(true);
		newFolderDialog.show();
		folderNameField.setFocus(true);
	}

	@Override
	public void setSharingAndDataUseWidget(IsWidget w) {
		sharingAndDataUseContainer.clear();
		sharingAndDataUseContainer.add(w);
	}

	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}

	@Override
	public void hide() {
		newFolderDialog.hide();
	}

	@Override
	public void setSaveEnabled(boolean enabled) {
		okNewFolderButton.setEnabled(enabled);
	}
}
