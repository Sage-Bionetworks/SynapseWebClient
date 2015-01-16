package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.presenter.LoginPresenter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageConfigViewImpl implements ImageConfigView {
	public interface ImageConfigViewImplUiBinder extends UiBinder<Widget, ImageConfigViewImpl> {}
	private Widget widget;
	
	private Presenter presenter;
	SageImageBundle sageImageBundle;
	EntityFinder entityFinder;
	ClientCache clientCache;
	SynapseJSNIUtils synapseJSNIUtils;
	private Widget fileInputWidget;
	Button uploadButton = new Button(DisplayConstants.IMAGE_CONFIG_UPLOAD);
	
	@UiField
	TextBox nameField;
	@UiField
	TextBox urlField;
	@UiField
	TextBox entityField;
	@UiField
	Button findEntitiesButton;
	
	
	@UiField
	SimplePanel uploadTab;
	
	@UiField
	TabListItem uploadTabListItem;
	@UiField
	TabListItem externalTabListItem;
	@UiField
	TabListItem synapseTabListItem;
	@UiField
	FlowPanel uploadSuccessUI;
	@UiField
	FlowPanel uploadFailureUI;
	@UiField
	Text uploadErrorText;
	
	private ImageParamsPanel uploadParamsPanel, synapseParamsPanel;
	
	@Inject
	public ImageConfigViewImpl(
			ImageConfigViewImplUiBinder binder,
			SageImageBundle sageImageBundle, EntityFinder entityFinder, ClientCache clientCache, SynapseJSNIUtils synapseJSNIUtils,
			ImageParamsPanel synapseParamsPanel,
			ImageParamsPanel uploadParamsPanel
			) {
		widget = binder.createAndBindUi(this);
		this.sageImageBundle = sageImageBundle;
		this.entityFinder = entityFinder;
		this.clientCache = clientCache;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.synapseParamsPanel = synapseParamsPanel;
		this.uploadParamsPanel = uploadParamsPanel;
		uploadButton.setType(ButtonType.INFO);
		initClickHandlers();
	}
	
	private void initClickHandlers() {
		uploadButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.uploadFileClicked();				
			}
		});
		
		findEntitiesButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				entityFinder.configure(false, new SelectedHandler<Reference>() {					
					@Override
					public void onSelected(Reference selected) {
						if(selected.getTargetId() != null) {
							entityField.setValue(DisplayUtils.createEntityVersionString(selected));
							entityFinder.hide();
						} else {
							showErrorMessage(DisplayConstants.PLEASE_MAKE_SELECTION);
						}
					}
				});
				entityFinder.show();
			}
		});
	}
	
	@Override
	public void initView() {
		uploadSuccessUI.setVisible(false);
		uploadFailureUI.setVisible(false);
	}
	
	@Override
	public String getAlignment() {
		if (isSynapseEntity())
			return synapseParamsPanel.getAlignment();
		else
			return uploadParamsPanel.getAlignment();
	}
	
	@Override
	public void setAlignment(String alignment) {
		if (isSynapseEntity())
			synapseParamsPanel.setAlignment(alignment);
		else
			uploadParamsPanel.setAlignment(alignment);
		
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, DialogCallback dialogCallback) {
		uploadTab.clear();
		fileInputWidget.setVisible(true);
		uploadButton.setVisible(true);
		FlowPanel container = new FlowPanel();
	    container.add(fileInputWidget);
	    container.add(uploadButton);
	    container.add(uploadParamsPanel.asWidget());
		uploadTab.add(container);
	}
	
	@Override
	public void showUploadFailureUI(String error) {
		uploadErrorText.setText(error);
		uploadFailureUI.setVisible(true);
		uploadSuccessUI.setVisible(false);
	}
	
	@Override
	public void showUploadSuccessUI() {
		fileInputWidget.setVisible(false);
		uploadButton.setVisible(false);

		uploadFailureUI.setVisible(false);
		uploadSuccessUI.setVisible(true);
	}
	
	@Override
	public void setFileInputWidget(Widget fileInputWidget) {
		this.fileInputWidget = fileInputWidget;
	}
	
	@Override
	public void setUploadButtonEnabled(boolean enabled) {
		uploadButton.setEnabled(enabled);
	}
	
	
	@Override
	public void checkParams() throws IllegalArgumentException {
		if (isExternal()) {
			if (!LoginPresenter.isValidUrl(urlField.getValue(), false))
				throw new IllegalArgumentException(DisplayConstants.IMAGE_CONFIG_INVALID_URL_MESSAGE);
			if (!DisplayUtils.isDefined(nameField.getValue()))
				throw new IllegalArgumentException(DisplayConstants.IMAGE_CONFIG_INVALID_ALT_TEXT_MESSAGE);
		} else if (isSynapseEntity()) {
			if (!DisplayUtils.isDefined(entityField.getValue()))
				throw new IllegalArgumentException(DisplayConstants.INVALID_SYNAPSE_ID_MESSAGE);
		} 
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
	}
	
	@Override
	public String getImageUrl() {
		return urlField.getValue();
	}
	@Override
	public String getAltText() {
		return nameField.getValue();
	}
	@Override
	public void setImageUrl(String url) {
		urlField.setValue(url);
	}
	
	@Override
	public String getSynapseId() {
		return entityField.getValue();
	}
	
	@Override
	public void setSynapseId(String synapseId) {
		entityField.setValue(synapseId);
		synapseTabListItem.showTab();
	}
	
	@Override
	public boolean isExternal() {
		return externalTabListItem.isActive();
	}
	
	
	@Override
	public boolean isSynapseEntity() {
		return synapseTabListItem.isActive();
	}
	
	@Override
	public void setExternalVisible(boolean visible) {
		externalTabListItem.setEnabled(visible);
	}
	/*
	 * Private Methods
	 */

}
