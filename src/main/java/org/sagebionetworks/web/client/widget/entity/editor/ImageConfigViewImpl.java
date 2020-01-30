package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.TabPane;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.ValidationUtils;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageConfigViewImpl implements ImageConfigView {
	public interface ImageConfigViewImplUiBinder extends UiBinder<Widget, ImageConfigViewImpl> {
	}

	private Widget widget;

	private Presenter presenter;
	SageImageBundle sageImageBundle;
	EntityFinder entityFinder;
	ClientCache clientCache;
	SynapseJSNIUtils synapseJSNIUtils;

	@UiField
	TextBox nameField;
	@UiField
	TextBox urlField;
	@UiField
	TextBox entityField;
	@UiField
	Button findEntitiesButton;

	@UiField
	SimplePanel fileInputWidgetContainer;
	@UiField
	SimplePanel uploadParamsPanelContainer;
	@UiField
	SimplePanel wikiAttachmentsContainer;

	@UiField
	SimplePanel synapseParamsPanelContainer;

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
	@UiField
	TabPane tab1;
	@UiField
	TabPane tab2;
	@UiField
	TabPane tab3;
	@UiField
	Text fileNameText;

	private ImageParamsPanel uploadParamsPanel, synapseParamsPanel;

	@Inject
	public ImageConfigViewImpl(ImageConfigViewImplUiBinder binder, SageImageBundle sageImageBundle, EntityFinder entityFinder, ClientCache clientCache, SynapseJSNIUtils synapseJSNIUtils, ImageParamsPanel synapseParamsPanel, ImageParamsPanel uploadParamsPanel) {
		widget = binder.createAndBindUi(this);
		this.sageImageBundle = sageImageBundle;
		this.entityFinder = entityFinder;
		this.clientCache = clientCache;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.synapseParamsPanel = synapseParamsPanel;
		this.uploadParamsPanel = uploadParamsPanel;

		uploadParamsPanelContainer.setWidget(uploadParamsPanel.asWidget());
		synapseParamsPanelContainer.setWidget(synapseParamsPanel.asWidget());

		initClickHandlers();
	}

	private void initClickHandlers() {
		findEntitiesButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				entityFinder.configure(EntityFilter.ALL_BUT_LINK, true, new SelectedHandler<Reference>() {
					@Override
					public void onSelected(Reference selected) {
						entityField.setValue(DisplayUtils.createEntityVersionString(selected));
						entityFinder.hide();
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
		fileInputWidgetContainer.setVisible(true);

		entityField.setValue("");
		urlField.setValue("");
		nameField.setValue("");

		setWikiFilesTabVisible(true);
		setExternalTabVisible(true);
		setSynapseTabVisible(true);

		synapseTabListItem.setActive(false);
		tab3.setActive(false);
		externalTabListItem.setActive(false);
		tab2.setActive(false);
		uploadTabListItem.setActive(false);
		tab1.setActive(false);

		uploadParamsPanel.clear();
		synapseParamsPanel.clear();
	}

	private ImageParamsPanel getCurrentParamsPanel() {
		if (isSynapseEntity()) {
			return synapseParamsPanel;
		} else {
			return uploadParamsPanel;
		}
	}

	@Override
	public String getAlignment() {
		return getCurrentParamsPanel().getAlignment();
	}

	@Override
	public void setAlignment(String alignment) {
		getCurrentParamsPanel().setAlignment(alignment);
	}

	@Override
	public void setScale(Integer scale) {
		getCurrentParamsPanel().setScale(scale);
	}

	@Override
	public Integer getScale() {
		return getCurrentParamsPanel().getScale();
	}

	@Override
	public void setAltText(String altText) {
		getCurrentParamsPanel().setAltText(altText);
	}

	@Override
	public String getAltText() {
		return getCurrentParamsPanel().getAltText();
	}

	@Override
	public void showUploadFailureUI(String error) {
		uploadErrorText.setText(error);
		uploadFailureUI.setVisible(true);
		uploadSuccessUI.setVisible(false);
	}

	@Override
	public void showUploadSuccessUI(String fileName) {
		fileInputWidgetContainer.setVisible(false);

		uploadFailureUI.setVisible(false);
		uploadSuccessUI.setVisible(true);
		fileNameText.setText(fileName);
	}

	@Override
	public void setFileInputWidget(Widget fileInputWidget) {
		fileInputWidgetContainer.clear();
		fileInputWidgetContainer.setWidget(fileInputWidget);
	}

	@Override
	public void setWikiAttachmentsWidget(Widget widget) {
		wikiAttachmentsContainer.setWidget(widget);
	}

	@Override
	public void setWikiAttachmentsWidgetVisible(boolean visible) {
		wikiAttachmentsContainer.setVisible(visible);
	}

	@Override
	public void checkParams() throws IllegalArgumentException {
		if (isExternal()) {
			if (!ValidationUtils.isValidUrl(urlField.getValue(), false))
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
	public void showLoading() {}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void clear() {}

	@Override
	public String getImageUrl() {
		return urlField.getValue();
	}

	@Override
	public String getExternalAltText() {
		return nameField.getValue();
	}

	@Override
	public void setImageUrl(String url) {
		urlField.setValue(url);
	}

	@Override
	public String getSynapseId() {
		return DisplayUtils.parseEntityVersionString(entityField.getValue()).getTargetId();
	}

	@Override
	public Long getVersion() {
		return DisplayUtils.parseEntityVersionString(entityField.getValue()).getTargetVersionNumber();
	}

	@Override
	public void showSynapseTab() {
		synapseTabListItem.setActive(true);
		tab3.setActive(true);
	}

	@Override
	public void setSynapseId(String synapseId) {
		entityField.setValue(synapseId);
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
	public void setExternalTabVisible(boolean visible) {
		externalTabListItem.setEnabled(visible);
	}

	@Override
	public void setWikiFilesTabVisible(boolean visible) {
		uploadTabListItem.setVisible(visible);
		tab1.setVisible(visible);
	}

	@Override
	public void setSynapseTabVisible(boolean visible) {
		synapseTabListItem.setVisible(visible);
		tab3.setVisible(visible);
	}

	@Override
	public void showExternalTab() {
		externalTabListItem.setActive(true);
		tab2.setActive(true);
	}

	@Override
	public void showWikiFilesTab() {
		uploadTabListItem.setActive(true);
		tab1.setActive(true);
	}

	/*
	 * Private Methods
	 */
}
