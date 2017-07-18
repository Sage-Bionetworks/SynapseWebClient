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
	public void createTitlebar(Entity entity) {
		favoriteWidget.configure(entity.getId());
		md5Link.clear();
		md5LinkContainer.clear();
		md5LinkContainer.add(md5Link);
		entityIcon.setType(EntityTypeUtils.getIconTypeForEntity(entity));
	}
	
	@Override
	public void setExternalUrlUIVisible(boolean visible) {
		UIObject.setVisible(externalUrlUI, visible);	
	}
	@Override
	public void setFilenameContainerVisible(boolean visible) {
		fileNameContainer.setVisible(visible);
	}
	@Override
	public void setFilename(String fileNameString) {
		fileName.setInnerText(fileNameString);	
	}
	@Override
	public void setFileSize(String fileSizeString) {
		fileSize.setInnerText(fileSizeString);
	}
	@Override
	public void setMd5(String md5) {
		md5Link.configure(md5);	
	}
	@Override
	public void setEntityName(String name) {
		entityName.setText(name);
	}
	@Override
	public void setExternalUrl(String url) {
		externalUrl.setInnerText(url);
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
