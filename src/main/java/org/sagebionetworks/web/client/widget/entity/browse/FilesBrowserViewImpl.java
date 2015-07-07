package org.sagebionetworks.web.client.widget.entity.browse;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonToolBar;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.SharingAndDataUseConditionWidget;
import org.sagebionetworks.web.client.widget.entity.download.QuizInfoDialog;
import org.sagebionetworks.web.client.widget.entity.download.UploadDialogWidget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FilesBrowserViewImpl implements FilesBrowserView {

	public interface FilesBrowserViewImplUiBinder extends
			UiBinder<Widget, FilesBrowserViewImpl> {
	}

	private Presenter presenter;
	private EntityTreeBrowser entityTreeBrowser;
	private UploadDialogWidget uploader;
	private QuizInfoDialog quizInfoDialog;
	private SharingAndDataUseConditionWidget sharingAndDataUseWidget;
	private Widget widget;

	@UiField
	SimplePanel uploaderContainer;
	@UiField
	SimplePanel quizInfoDialogContainer;
	@UiField
	ButtonToolBar topBar;
	@UiField
	Button uploadButton;
	@UiField
	Button addFolderButton;
	@UiField
	SimplePanel files;
	@UiField
	Modal newFolderDialog;
	@UiField
	TextBox folderNameField;
	@UiField
	SimplePanel sharingAndDataUseContainer;
	@UiField
	Button cancelNewFolderButton;
	@UiField
	Button okNewFolderButton;

	@Inject
	public FilesBrowserViewImpl(FilesBrowserViewImplUiBinder binder,
			UploadDialogWidget uploader,
			SharingAndDataUseConditionWidget sharingAndDataUseWidget,
			QuizInfoDialog quizInfoDialog, EntityTreeBrowser entityTreeBrowser) {
		widget = binder.createAndBindUi(this);
		this.uploader = uploader;
		this.entityTreeBrowser = entityTreeBrowser;
		this.sharingAndDataUseWidget = sharingAndDataUseWidget;
		this.quizInfoDialog = quizInfoDialog;
		uploaderContainer.setWidget(uploader.asWidget());
		quizInfoDialogContainer.setWidget(quizInfoDialog.asWidget());
		sharingAndDataUseContainer.setWidget(sharingAndDataUseWidget.asWidget());
		addFolderButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// for additional functionality, it now creates the folder up
				// front, and the dialog will rename (and change share and data
				// use)
				presenter.addFolderClicked();
			}
		});

		uploadButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.uploadButtonClicked();
			}
		});

		okNewFolderButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.updateFolderName(folderNameField.getText());
			}
		});

		cancelNewFolderButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.deleteFolder(true);
			}
		});

		folderNameField.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					okNewFolderButton.click();
				}
			}
		});

		Widget etbW = entityTreeBrowser.asWidget();
		etbW.addStyleName("margin-top-10");
		files.setWidget(etbW);
	}

	@Override
	public void configure(String entityId, boolean canCertifiedUserAddChild) {
		entityTreeBrowser.configure(entityId);
		if (canCertifiedUserAddChild) {
			topBar.setVisible(true);
		}
	}

	@Override
	public void showQuizInfoDialog() {
		quizInfoDialog.show();
	}

	@Override
	public void showUploadDialog(String entityId){
		EntityUpdatedHandler handler = new EntityUpdatedHandler() {
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				presenter.fireEntityUpdatedEvent();
			}
		};
		uploader.configure(DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK, null,
				entityId, handler, null, true);
		uploader.setUploaderLinkNameVisible(true);
		uploader.show();
	}

	@Override
	public void showFolderEditDialog(final String folderEntityId) {
		folderNameField.setText("");
		Callback refreshSharingAndDataUseWidget = new Callback() {
			@Override
			public void invoke() {
				// entity was updated by the sharing and data use widget.
				sharingAndDataUseWidget.setEntity(folderEntityId);
			}
		};
		sharingAndDataUseWidget.configure(folderEntityId, true,
				refreshSharingAndDataUseWidget);
		newFolderDialog.show();
		addFolderButton.setFocus(false);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {

	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
		topBar.setVisible(false);
	}

	@Override
	public void refreshTreeView(String entityId) {
		entityTreeBrowser.configure(entityId);
	}

	@Override
	public void setNewFolderDialogVisible(boolean visible) {
		if (visible) {
			newFolderDialog.show();
		} else {
			newFolderDialog.hide();
		}
	}
}
