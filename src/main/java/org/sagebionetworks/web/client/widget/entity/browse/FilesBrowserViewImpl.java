package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.ArrayList;

import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.CancelEvent;
import org.sagebionetworks.web.client.events.CancelHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.SharingAndDataUseConditionWidget;
import org.sagebionetworks.web.client.widget.entity.download.QuizInfoWidget;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FilesBrowserViewImpl extends LayoutContainer implements FilesBrowserView {

	private Presenter presenter;
	private EntityTreeBrowser entityTreeBrowser;
	private Uploader uploader;
	private QuizInfoWidget quizInfoWidget;
	private SharingAndDataUseConditionWidget sharingAndDataUseWidget;
	
	@Inject
	public FilesBrowserViewImpl(SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle,
			Uploader uploader,
			EntityTreeBrowser entityTreeBrowser, 
			CookieProvider cookies,
			SharingAndDataUseConditionWidget sharingAndDataUseWidget,
			QuizInfoWidget quizInfoWidget) {
		this.uploader = uploader;
		this.entityTreeBrowser = entityTreeBrowser;
		this.sharingAndDataUseWidget = sharingAndDataUseWidget;
		this.quizInfoWidget = quizInfoWidget;
		this.setLayout(new FitLayout());
	}
	
	@Override
	protected void onRender(com.google.gwt.user.client.Element parent, int index) {
		super.onRender(parent, index);		
		
	};

	@Override
	public void configure(String entityId, boolean canEdit) {
		configure(entityId, canEdit, null);
	}		
	
	@Override
	public void configure(String entityId, boolean canEdit, String title) {
		this.removeAll(true);
		LayoutContainer lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);
		LayoutContainer topbar = new LayoutContainer();		
		boolean isTitle = (title!=null);
		if(isTitle) {
			SafeHtmlBuilder shb = new SafeHtmlBuilder();
			shb.appendHtmlConstant("<h3>" + title + "</h3>");
			topbar.add(new HTML(shb.toSafeHtml()));
		}
		
		if(canEdit) {
			Button upload = getUploadButton(entityId);
			upload.addStyleName("margin-right-5");
			// AbstractImagePrototype.create(iconsImageBundle.synapseFolderAdd16())
			Button addFolder = DisplayUtils.createIconButton(DisplayConstants.ADD_FOLDER, DisplayUtils.ButtonType.DEFAULT, "glyphicon-plus");
			addFolder.addClickHandler(new ClickHandler() {				
				@Override
				public void onClick(ClickEvent event) {
					//for additional functionality, it now creates the folder up front, and the dialog will rename (and change share and data use)
					presenter.addFolderClicked();
				}
			});
		
			topbar.add(upload);
			topbar.add(addFolder, new MarginData(0, 3, 0, 0));
		}
		
		LayoutContainer files = new LayoutContainer();
		entityTreeBrowser.configure(entityId, true);
		Widget etbW = entityTreeBrowser.asWidget();
		etbW.addStyleName("margin-top-10");
		files.add(etbW);
		//If we are showing the buttons or a title, then add the topbar.  Otherwise don't
		if (canEdit || isTitle)
			lc.add(topbar);
		lc.add(files);
		lc.layout();
		this.add(lc);
		this.layout(true);
	}
	
	@Override
	public void showQuizInfoDialog(final CallbackP<Boolean> callback) {
		FilesBrowserViewImpl.showQuizInfoDialog(callback, quizInfoWidget);
	}
	
	public static void showQuizInfoDialog(final CallbackP<Boolean> callback, QuizInfoWidget quizInfoWidget) {
		final Window dialog = new Window();
		dialog.setMaximizable(false);
		dialog.setSize(420, 270);
		dialog.setPlain(true);
		dialog.setModal(true);
		dialog.setLayout(new FitLayout());
		dialog.setBorders(false);
		dialog.setHeading("Join the Synapse Certified User Community");

		quizInfoWidget.configure(new CallbackP<Boolean>() {
			@Override
			public void invoke(Boolean tutorialClicked) {
				dialog.hide();
				callback.invoke(tutorialClicked);
			}
		});
		dialog.add(quizInfoWidget.asWidget());
		dialog.show();
		DisplayUtils.center(dialog);
	}
	
	@Override
	public void showUploadDialog(String entityId){
		EntityUpdatedHandler handler = new EntityUpdatedHandler() {				
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				presenter.fireEntityUpdatedEvent();
			}
		};

		final Window window = new Window();
		uploader.clearHandlers();
		// add user defined handler
		uploader.addPersistSuccessHandler(handler);
		
		// add handlers for closing the window
		uploader.addPersistSuccessHandler(new EntityUpdatedHandler() {			
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				window.hide();
			}
		});
		uploader.addCancelHandler(new CancelHandler() {				
			@Override
			public void onCancel(CancelEvent event) {
				window.hide();
			}
		});
		
		//let the uploader create the FileEntity
		window.removeAll();
		window.setPlain(true);
		window.setModal(true);
		window.setLayout(new FitLayout());
		window.setHeading(DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK);
		window.add(uploader.asWidget(entityId, new ArrayList<AccessRequirement>()), new MarginData(5));
		window.show();
		window.setSize(uploader.getDisplayWidth(), uploader.getDisplayHeight());
	}
	
	@Override
	public void showFolderEditDialog(final String folderEntityId) {
		SimplePanel sharingAndDataUseContainer = new SimplePanel();
		Callback refreshSharingAndDataUseWidget = new Callback() {
			@Override
			public void invoke() {
				//entity was updated by the sharing and data use widget.
				sharingAndDataUseWidget.setEntity(folderEntityId);
			}
		};
		sharingAndDataUseWidget.configure(folderEntityId, true, refreshSharingAndDataUseWidget);
		sharingAndDataUseContainer.add(sharingAndDataUseWidget.asWidget());

		final Dialog dialog = new Dialog();
		dialog.setMaximizable(false);
		dialog.setSize(460, 260);
		dialog.setPlain(true);
		dialog.setModal(true);
		dialog.setHideOnButtonClick(true);
		dialog.setLayout(new FitLayout());
		dialog.setBorders(false);
		dialog.setButtons(Dialog.OKCANCEL);
		dialog.setHeading("New Folder");

		final FormPanel panel = new FormPanel();
		panel.setHeaderVisible(false);
		panel.setFrame(false);
		panel.setBorders(false);
		panel.setShadow(false);
		panel.setBodyBorder(false);
		panel.setFieldWidth(345);

		final TextField<String> nameField = new TextField<String>();
				nameField.setFieldLabel(DisplayConstants.LABEL_NAME);
		panel.add(nameField);			
		panel.add(sharingAndDataUseContainer);
		dialog.getButtonBar().removeAll();
		final com.extjs.gxt.ui.client.widget.button.Button okButton = new com.extjs.gxt.ui.client.widget.button.Button(DisplayConstants.OK);
		okButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				dialog.hide(okButton);
				String nameVal = nameField.getValue();
				nameField.clear();
				presenter.updateFolderName(nameVal, folderEntityId);
			}
		});
		dialog.addButton(okButton);
		
		final com.extjs.gxt.ui.client.widget.button.Button cancelButton = new com.extjs.gxt.ui.client.widget.button.Button(DisplayConstants.BUTTON_CANCEL, new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				dialog.hide();
			}
		});
		dialog.addButton(cancelButton);
		
		dialog.addListener(Events.Hide, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				//window is hiding.  if it was because of any reason other than the ok button being clicked, then cancel folder creation (delete it)
				if (be instanceof WindowEvent && ((WindowEvent)be).getButtonClicked() != okButton) {
					cancelFolderCreation(dialog, nameField, folderEntityId);
				}
			};
		});
		// Enter key in name field submits
		new KeyNav<ComponentEvent>(nameField) {
			@Override
			public void onEnter(ComponentEvent ce) {
				super.onEnter(ce);
				if(okButton.isEnabled())
					okButton.fireEvent(Events.Select);
			}
		};

		//and name textfield should have focus by default
		nameField.focus();
		
		dialog.add(panel);
		dialog.show();
	}

	private void cancelFolderCreation(Dialog dialog, TextField<String> nameField, String folderEntityId){
		nameField.clear();
		presenter.deleteFolder(folderEntityId, true);
	}
	
	@Override
	public Widget asWidget() {
		return this;
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
	}

	@Override
	public void refreshTreeView(String entityId) {
		entityTreeBrowser.configure(entityId, true);
	}
	
	/**
	 * TODO : this should be replaced by DisplayUtils.getUploadButton with the locationable uploader able to create 
	 * an entity and upload file in a single transaction it modified to create a new 
	 */
	private Button getUploadButton(final String entityId) {
		Button uploadButton = DisplayUtils.createIconButton(DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK, DisplayUtils.ButtonType.DEFAULT, "glyphicon-arrow-up");
		uploadButton.addStyleName("left display-inline");
		
		uploadButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				presenter.uploadButtonClicked();
			}
		});
		return uploadButton;
	}
	
	

}
