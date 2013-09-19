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
import org.sagebionetworks.web.client.widget.entity.dialog.NameAndDescriptionEditorDialog;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FilesBrowserViewImpl extends LayoutContainer implements FilesBrowserView {

	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
	private EntityTreeBrowser entityTreeBrowser;
	private Uploader uploader;
	private CookieProvider cookies;
	
	@Inject
	public FilesBrowserViewImpl(SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle,
			Uploader uploader,
			EntityTreeBrowser entityTreeBrowser, CookieProvider cookies) {
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		this.uploader = uploader;
		this.entityTreeBrowser = entityTreeBrowser;
		this.cookies = cookies;
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
					NameAndDescriptionEditorDialog.showNameDialog(DisplayConstants.LABEL_NAME, new NameAndDescriptionEditorDialog.Callback() {					
						@Override
						public void onSave(String name, String description) {
							presenter.createFolder(name);
						}
					});
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
		
		EntityUpdatedHandler handler = new EntityUpdatedHandler() {				
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				presenter.fireEntityUpdatedEvent();
			}
		};
		// AbstractImagePrototype.create(iconsImageBundle.NavigateUp16())
		Button uploadButton = DisplayUtils.createIconButton(DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK, DisplayUtils.ButtonType.DEFAULT, "glyphicon-arrow-up");
		uploadButton.addStyleName("left display-inline");
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
		
		uploadButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				//let the uploader create the FileEntity
				window.removeAll();
				window.setSize(uploader.getDisplayWidth(), uploader.getDisplayHeight());
				window.setPlain(true);
				window.setModal(true);		
				window.setHeading(DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK);
				window.setLayout(new FitLayout());
				window.add(uploader.asWidget(entityId, new ArrayList<AccessRequirement>()), new MarginData(5));
				window.show();
			}
		});
		return uploadButton;
	}

}
