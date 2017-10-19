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
import org.sagebionetworks.web.client.utils.CallbackP;
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
	private Widget widget;

	@UiField
	SimplePanel files;

	@Inject
	public FilesBrowserViewImpl(FilesBrowserViewImplUiBinder binder,
			EntityTreeBrowser entityTreeBrowser) {
		widget = binder.createAndBindUi(this);
		this.entityTreeBrowser = entityTreeBrowser;
		uploaderContainer.setWidget(uploader.asWidget());
		quizInfoDialogContainer.setWidget(quizInfoDialog.asWidget());
		sharingAndDataUseContainer.setWidget(sharingAndDataUseWidget.asWidget());
		

		uploadButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.uploadButtonClicked();
			}
		});

		Widget etbW = entityTreeBrowser.asWidget();
		etbW.addStyleName("margin-top-10");
		files.setWidget(etbW);
	}

	@Override
	public void configure(String entityId) {
		entityTreeBrowser.configure(entityId);
	}
	
	public void setEntitySelectedHandler(org.sagebionetworks.web.client.events.EntitySelectedHandler handler) {
		entityTreeBrowser.setEntitySelectedHandler(handler);
	};
	
	@Override
	public void setEntityClickedHandler(CallbackP<String> callback) {
		entityTreeBrowser.setEntityClickedHandler(callback);
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
		entityTreeBrowser.clear();
	}

	@Override
	public void refreshTreeView(String entityId) {
		entityTreeBrowser.configure(entityId);
	}
}
