package org.sagebionetworks.web.client.widget.entity.file;

import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.file.ExternalFileHandle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandleInterface;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileTitleBarViewImpl extends Composite implements FileTitleBarView {

	private Presenter presenter;
	private Md5Link md5Link;
	private FavoriteWidget favoriteWidget;
	
	@UiField
	HTMLPanel panel;
	@UiField
	HTMLPanel fileFoundContainer;
	@UiField
	HTMLPanel fileNameContainer;
	
	@UiField
	SimplePanel md5LinkContainer;
	@UiField
	Icon entityIcon;
	@UiField
	SpanElement fileName;
	@UiField
	SpanElement fileSize;
	@UiField
	SpanElement fileLocation;
	@UiField
	SimplePanel favoritePanel;
	@UiField
	DivElement externalUrlUI;
	@UiField
	SpanElement externalUrl;
	@UiField
	Heading entityName;
	@UiField
	Span fileDownloadButtonContainer;
	
	interface FileTitleBarViewImplUiBinder extends UiBinder<Widget, FileTitleBarViewImpl> {
	}

	private static FileTitleBarViewImplUiBinder uiBinder = GWT
			.create(FileTitleBarViewImplUiBinder.class);
	
	@Inject
	public FileTitleBarViewImpl(SageImageBundle sageImageBundle,
			FavoriteWidget favoriteWidget,
			Md5Link md5Link) {
		this.favoriteWidget = favoriteWidget;
		this.md5Link = md5Link;
		
		initWidget(uiBinder.createAndBindUi(this));
		md5LinkContainer.addStyleName("inline-block margin-left-5");
		
		favoritePanel.addStyleName("inline-block");
		favoritePanel.setWidget(favoriteWidget.asWidget());
	}
	
	@Override
	public void createTitlebar(
			EntityBundle entityBundle, 
			EntityType entityType, 
			AuthenticationController authenticationController) {
		Entity entity = entityBundle.getEntity();

		favoriteWidget.configure(entity.getId());
		
		md5Link.clear();
		md5LinkContainer.clear();
		md5LinkContainer.add(md5Link);

		entityIcon.setType(EntityTypeUtils.getIconTypeForEntity(entity));
		
		//fileHandle is null if user can't access the filehandle associated with this file entity
		FileHandle fileHandle = DisplayUtils.getFileHandle(entityBundle);
		boolean isFilenamePanelVisible = fileHandle != null;
		fileNameContainer.setVisible(isFilenamePanelVisible);
		entityName.setText(entity.getName());
		if (isFilenamePanelVisible) {
			fileName.setInnerText(entityBundle.getFileName());
			//don't ask for the size if it's external, just display that this is external data
			boolean isExternalFile = fileHandle instanceof ExternalFileHandle;
			UIObject.setVisible(externalUrlUI, isExternalFile);
			if (isExternalFile) {
				ExternalFileHandle externalFileHandle = (ExternalFileHandle)fileHandle;
				externalUrl.setInnerText(externalFileHandle.getExternalURL());
				if (externalFileHandle.getContentSize() != null) {
					fileSize.setInnerText("| "+DisplayUtils.getFriendlySize(externalFileHandle.getContentSize().doubleValue(), true));
				} else {
					fileSize.setInnerText("");	
				}
				
				fileLocation.setInnerText("| External Storage");
				String md5 = externalFileHandle.getContentMd5();
				if (md5 != null) {
					md5Link.configure(md5);
				}
			}
			else if (fileHandle instanceof S3FileHandleInterface){
				final S3FileHandleInterface s3FileHandle = (S3FileHandleInterface)fileHandle;
				presenter.setS3Description();

				fileSize.setInnerText("| "+DisplayUtils.getFriendlySize(s3FileHandle.getContentSize().doubleValue(), true));
				final String md5 = s3FileHandle.getContentMd5();
				if (md5 != null) {
					md5Link.configure(md5);
				} 
			}
		}
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
	public void setFileLocation(String location) {
		fileLocation.setInnerText(location);
	}

	@Override
	public void setFileDownloadButton(Widget w) {
		fileDownloadButtonContainer.clear();
		fileDownloadButtonContainer.add(w);
	}
}
