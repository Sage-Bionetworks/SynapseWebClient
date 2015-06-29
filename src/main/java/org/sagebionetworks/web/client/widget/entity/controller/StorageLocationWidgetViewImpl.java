package org.sagebionetworks.web.client.widget.entity.controller;

import java.util.List;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.Radio;
import org.gwtbootstrap3.client.ui.SuggestBox;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class StorageLocationWidgetViewImpl implements StorageLocationWidgetView {
	
	public interface StorageLocationWidgetViewImplUiBinder 
			extends UiBinder<Widget, StorageLocationWidgetViewImpl> {}
	
	@UiField
	Modal modal;
	
	@UiField
	SimplePanel synAlertPanel;
	
	@UiField
	TextBox bucketField;
	@UiField
	TextBox externalS3BannerField;
	@UiField
	DropDownMenu externalS3BannerOptions;
	@UiField
	Button externalS3BannerDropdownButton;
	@UiField
	TextBox baseKeyField;
	
	@UiField
	TextBox sftpUrlField;
	
	@UiField
	TextBox sftpBannerField;
	@UiField
	DropDownMenu sftpBannerOptions;
	@UiField
	Button sftpBannerDropdownButton;
	
	@UiField
	Radio synapseStorageButton;
	@UiField
	Radio externalS3Button;
	@UiField
	Radio sftpButton;
	
	@UiField
	Button saveButton;
	
	@UiField
	Button cancelButton;
	
	@UiField
	Div s3Collapse;
	@UiField
	Div sftpCollapse;
	
	Widget widget;
	Presenter presenter;
	
	@Inject
	public StorageLocationWidgetViewImpl(StorageLocationWidgetViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
		saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSave();
			}
		});
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				modal.hide();
			}
		});
		
		synapseStorageButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				s3Collapse.setVisible(false);
				sftpCollapse.setVisible(false);
			}
		});
		externalS3Button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				s3Collapse.setVisible(true);
				sftpCollapse.setVisible(false);
			}
		});
		sftpButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				s3Collapse.setVisible(false);
				sftpCollapse.setVisible(true);
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setSynAlertWidget(IsWidget synAlert) {
		synAlertPanel.setWidget(synAlert);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void clear() {
		bucketField.setText("");
		baseKeyField.setText("");
		sftpUrlField.setText("sftp://");
		externalS3BannerField.setText("");
		sftpBannerField.setText("");
		selectSynapseStorage();
		s3Collapse.setVisible(false);
		sftpCollapse.setVisible(false);
		externalS3BannerOptions.clear();
		sftpBannerOptions.clear();
	}

	@Override
	public void hide() {
		modal.hide();
	}

	@Override
	public void show() {
		modal.show();
	}

	@Override
	public void selectSynapseStorage() {
		synapseStorageButton.setValue(true);
		s3Collapse.setVisible(false);
		sftpCollapse.setVisible(false);
	}

	@Override
	public boolean isSynapseStorageSelected() {
		return synapseStorageButton.getValue();
	}

	@Override
	public void selectExternalS3Storage() {
		externalS3Button.setValue(true);
		s3Collapse.setVisible(true);
		sftpCollapse.setVisible(false);
	}

	@Override
	public boolean isExternalS3StorageSelected() {
		return externalS3Button.getValue();
	}

	@Override
	public String getBucket() {
		return bucketField.getValue();
	}

	@Override
	public void selectSFTPStorage() {
		sftpButton.setValue(true);
		s3Collapse.setVisible(false);
		sftpCollapse.setVisible(true);
	}

	@Override
	public boolean isSFTPStorageSelected() {
		return sftpButton.getValue();
	}

	@Override
	public String getSFTPUrl() {
		return sftpUrlField.getValue();
	}
	@Override
	public String getExternalS3Banner() {
		return externalS3BannerField.getValue();
	}
	@Override
	public String getSFTPBanner() {
		return sftpBannerField.getValue();
	}
	@Override
	public String getBaseKey() {
		return baseKeyField.getValue();
	}
	
	@Override
	public void setBaseKey(String baseKey) {
		baseKeyField.setValue(baseKey);
	}
	@Override
	public void setBucket(String bucket) {
		bucketField.setValue(bucket);
	}
	@Override
	public void setExternalS3Banner(String banner) {
		externalS3BannerField.setValue(banner);
	}
	@Override
	public void setSFTPBanner(String banner) {
		sftpBannerField.setValue(banner);
	}
	@Override
	public void setSFTPUrl(String url) {
		sftpUrlField.setValue(url);
	}
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
	@Override
	public void setBannerSuggestions(List<String> banners) {
		addBannerOptions(sftpBannerField, sftpBannerOptions, banners);
		addBannerOptions(externalS3BannerField, externalS3BannerOptions, banners);
	}
	
	private void addBannerOptions(final TextBox field, DropDownMenu menu, List<String> banners) {
		menu.clear();
		for (final String banner : banners) {
			AnchorListItem item = new AnchorListItem();
			item.setText(banner);
			item.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					field.setText(banner);
				}
			});
			menu.add(item);
		}
	}
	@Override
	public void setBannerDropdownVisible(boolean isVisible) {
		externalS3BannerDropdownButton.setVisible(isVisible);
		sftpBannerDropdownButton.setVisible(isVisible);
	}
}
