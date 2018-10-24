package org.sagebionetworks.web.client.widget.entity.file;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.InfoAlert;
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
	Presenter presenter;
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
	Text entityName;
	@UiField
	DropDownMenu dropdownMenu;
	@UiField
	AnchorListItem addToDownloadListLink;
	@UiField
	AnchorListItem programmaticOptionsLink;
	@UiField
	Div externalObjectStoreUI;
	@UiField
	Span endpoint;
	@UiField
	Span bucket;
	@UiField
	Span fileKey;
	@UiField
	Span version;
	@UiField
	Anchor currentVersionLink;
	@UiField
	Div versionInfoUI;
	@UiField
	InfoAlert addedToDownloadListAlert;
	interface FileTitleBarViewImplUiBinder extends UiBinder<Widget, FileTitleBarViewImpl> {
	}
	private String currentEntityId;
	private static FileTitleBarViewImplUiBinder uiBinder = GWT
			.create(FileTitleBarViewImplUiBinder.class);
	@Inject
	public FileTitleBarViewImpl(
			FavoriteWidget favoriteWidget,
			Md5Link md5Link, 
			GlobalApplicationState globalAppState,
			AuthenticationController authController) {
		this.favoriteWidget = favoriteWidget;
		this.md5Link = md5Link;
		
		initWidget(uiBinder.createAndBindUi(this));
		md5LinkContainer.addStyleName("inline-block margin-left-5");
		
		favoritePanel.addStyleName("inline-block");
		favoritePanel.setWidget(favoriteWidget.asWidget());
		currentVersionLink.addClickHandler(event -> {
			if (!DisplayUtils.isAnyModifierKeyDown(event)) {
				event.preventDefault();
				globalAppState.getPlaceChanger().goTo(new Synapse(currentEntityId));
			}
		});
		addToDownloadListLink.addClickHandler(event->{
			presenter.onAddToDownloadList();
		});
		programmaticOptionsLink.addClickHandler(event->{
			presenter.onProgrammaticDownloadOptions();
		});
		addedToDownloadListAlert.addClickHandler(event -> {
			Profile place = new Profile(authController.getCurrentUserPrincipalId() + "/downloads");
			globalAppState.getPlaceChanger().goTo(place);
		});
	}
	@Override
	public void setPresenter(Presenter p) {
		this.presenter = p;
	}
	@Override
	public void createTitlebar(Entity entity) {
		addedToDownloadListAlert.setVisible(false);
		currentEntityId = entity.getId();
		favoriteWidget.configure(currentEntityId);
		md5Link.clear();
		md5LinkContainer.clear();
		md5LinkContainer.add(md5Link);
		entityIcon.setType(EntityTypeUtils.getIconTypeForEntity(entity));
		currentVersionLink.setHref("#!Synapse:"+currentEntityId);
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
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void clear() {
	}

	@Override
	public void setFileLocation(String location) {
		fileLocation.setInnerText(location);
	}

	@Override
	public void setFileDownloadMenuItem(Widget w) {
		dropdownMenu.insert(w, 0);
	}
	@Override
	public void setExternalObjectStoreUIVisible(boolean visible) {
		externalObjectStoreUI.setVisible(visible);
	}
	@Override
	public void setExternalObjectStoreInfo(String endpointValue, String bucketValue, String fileKeyValue) {
		endpoint.setText(endpointValue);
		bucket.setText(bucketValue);
		fileKey.setText(fileKeyValue);
	}
	@Override
	public void setVersionUIVisible(boolean visible) {
		versionInfoUI.setVisible(visible);
	}
	@Override
	public void setVersion(Long versionNumber) {
		version.setText(versionNumber.toString());
	}
	@Override
	public void showAddedToDownloadListAlert(String message) {
		addedToDownloadListAlert.setMessage(message);
		addedToDownloadListAlert.setVisible(true);	
	}
}
