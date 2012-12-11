package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.ArrayList;

import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.events.CancelEvent;
import org.sagebionetworks.web.client.events.CancelHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.entity.dialog.NameAndDescriptionEditorDialog;
import org.sagebionetworks.web.client.widget.entity.download.LocationableUploader;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FilesBrowserViewImpl extends LayoutContainer implements FilesBrowserView {

	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
	private EntityTreeBrowser entityTreeBrowser;
	private LocationableUploader locationableUploader;
		
	@Inject
	public FilesBrowserViewImpl(SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle,
			LocationableUploader locationableUploader,
			EntityTreeBrowser entityTreeBrowser) {
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		this.locationableUploader = locationableUploader;
		this.entityTreeBrowser = entityTreeBrowser;
		this.setLayout(new FitLayout());
	}
	
	@Override
	protected void onRender(com.google.gwt.user.client.Element parent, int index) {
		super.onRender(parent, index);		
		
	};

	@Override
	public void configure(String entityId) {
		configure(entityId, null);
	}		
	
	@Override
	public void configure(String entityId, String title) {
		this.removeAll(true);
		LayoutContainer lc = new LayoutContainer();
		lc.addStyleName("span-24 notopmargin");
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);
		LayoutContainer topbar = new LayoutContainer();		
		LayoutContainer left = new LayoutContainer();
		left.setStyleName("left span-17 notopmargin");
		LayoutContainer right = new LayoutContainer();
		right.setStyleName("right span-7 notopmargin");
		topbar.add(left);
		topbar.add(right);

		if(title == null) title = "&nbsp;"; 
		SafeHtmlBuilder shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant("<h3>" + title + "</h3>");
		left.add(new HTML(shb.toSafeHtml()));

		Button upload = getUploadButton(); 
		upload.addStyleName("right last");
		right.add(upload);

		Button addFolder = new Button(DisplayConstants.ADD_FOLDER, AbstractImagePrototype.create(iconsImageBundle.synapseFolderAdd16()), new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				NameAndDescriptionEditorDialog.showNameDialog(DisplayConstants.LABEL_NAME, new NameAndDescriptionEditorDialog.Callback() {					
					@Override
					public void onSave(String name, String description) {
						presenter.createFolder(name);
					}
				});
			}
		});
		addFolder.setHeight(25);
		addFolder.addStyleName("right");
		right.add(addFolder, new MarginData(0, 3, 0, 0));
		
		LayoutContainer files = new LayoutContainer();
		files.setStyleName("span-24 notopmargin");
		entityTreeBrowser.configure(entityId);
		files.add(entityTreeBrowser.asWidget());
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
		entityTreeBrowser.configure(entityId);
	}
	
	/**
	 * TODO : this should be replaced by DisplayUtils.getUploadButton with the locationable uploader able to create 
	 * an entity and upload file in a single transaction it modified to create a new 
	 */
	private Button getUploadButton() {
		
		EntityUpdatedHandler handler = new EntityUpdatedHandler() {				
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				Entity entity = locationableUploader.getEntity();				
				if(entity != null)  {					
					presenter.renameChildToFilename(entity.getId());
				} else {
					presenter.fireEntityUpdatedEvent();
				}
			}
		};
		Button uploadButton = new Button(DisplayConstants.TEXT_UPLOAD_FILE, AbstractImagePrototype.create(iconsImageBundle.NavigateUp16()));
		uploadButton.setHeight(25);
		final Window window = new Window();  
		locationableUploader.clearHandlers();
		// add user defined handler
		locationableUploader.addPersistSuccessHandler(handler);
		
		// add handlers for closing the window
		locationableUploader.addPersistSuccessHandler(new EntityUpdatedHandler() {			
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				window.hide();
			}
		});
		locationableUploader.addCancelHandler(new CancelHandler() {				
			@Override
			public void onCancel(CancelEvent event) {
				window.hide();
			}
		});
		
		uploadButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				presenter.createEntityForUpload(new AsyncCallback<Entity>() {
					@Override
					public void onSuccess(Entity entity) {
						window.removeAll();
						window.setSize(400, 320);
						window.setPlain(true);
						window.setModal(true);		
						window.setBlinkModal(true);
						window.setHeading(DisplayConstants.TEXT_UPLOAD_FILE);
						window.setLayout(new FitLayout());			
						window.add(locationableUploader.asWidget(entity, new ArrayList<AccessRequirement>()), new MarginData(5));
						window.show();
					}
					@Override
					public void onFailure(Throwable caught) {
					}
				});
			}
		});
		return uploadButton;
	}
}
