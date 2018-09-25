package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * @author jayhodgson
 */
public class FileHistoryWidgetViewImpl extends Composite implements FileHistoryWidgetView, IsWidget {
	
	interface FileHistoryWidgetViewImplUiBinder extends UiBinder<Widget, FileHistoryWidgetViewImpl> {
	}
	
	private static FileHistoryWidgetViewImplUiBinder uiBinder = GWT
			.create(FileHistoryWidgetViewImplUiBinder.class);
	
	private PortalGinInjector ginInjector;

	@UiField
	Panel previousVersions;
	@UiField
	TBody previousVersionsTable;
	@UiField
	Hyperlink currentVersionLink;
	@UiField
	Button editInfoButton;
	@UiField
	Button moreButton;
	@UiField
	Div synAlertContainer;
	PromptTwoValuesModalView editVersionInfoModal;
	
	private static DateTimeFormat shortDateFormat = DateTimeFormat.getShortDateFormat();
	private Presenter presenter;
	
	@Inject
	public FileHistoryWidgetViewImpl(PortalGinInjector ginInjector, PromptTwoValuesModalView editVersionInfoDialog) {
		this.ginInjector = ginInjector;
		this.editVersionInfoModal = editVersionInfoDialog;
		initWidget(uiBinder.createAndBindUi(this));		
		editVersionInfoModal.setPresenter(new PromptTwoValuesModalView.Presenter() {
			@Override
			public void onPrimary() {
				presenter.updateVersionInfo(editVersionInfoModal.getValue1(), editVersionInfoModal.getValue2());
			}
		});
		editInfoButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onEditVersionInfoClicked();
			}
		});
		moreButton.addClickHandler(event -> {
			presenter.onMore();
		});
	}
	
	@Override
	public void setEntityBundle(Entity entity, boolean isShowingOlderVersion) {
		clear();
		currentVersionLink.setTargetHistoryToken(DisplayUtils.getSynapseHistoryTokenNoHash(entity.getId()));
		currentVersionLink.setVisible(isShowingOlderVersion);
	}
	
	@Override
	public void clearVersions() {
		previousVersionsTable.clear();	
	}
	
	@Override
	public void addVersion(final VersionInfo version, boolean canEdit, boolean isVersionSelected) {
		FileHistoryRowView fileHistoryRow = ginInjector.getFileHistoryRow();
		String versionName = version.getVersionLabel();
		String modifiedByUserId = version.getModifiedByPrincipalId();
		String modifiedOn = shortDateFormat.format(version.getModifiedOn());
		String size = "";
		try{
			double sizeDouble = Double.parseDouble(version.getContentSize());
			size = DisplayUtils.getFriendlySize(sizeDouble, true);
		} catch (Throwable t) {
		}
		String md5 = version.getContentMd5();
		Callback deleteCallback = new Callback() {
			@Override
			public void invoke() {
				presenter.deleteVersion(version.getVersionNumber());
			}
		};

		String versionComment = version.getVersionComment();
		Long versionNumber = version.getVersionNumber();
		String versionHref = DisplayUtils.
				getSynapseHistoryToken(version.getId(),
				version.getVersionNumber());
		fileHistoryRow.configure(versionNumber, versionHref, "Version " + versionName, modifiedByUserId, modifiedOn, size, md5, versionComment, deleteCallback);
		previousVersionsTable.add(fileHistoryRow.asWidget());
		fileHistoryRow.setCanEdit(canEdit);
		fileHistoryRow.setIsVersionSelected(isVersionSelected);
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
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}
	
	@Override
	public void clear() {
		//reset versions ui
		clearVersions();
		currentVersionLink.setVisible(false);
	}
	
	@Override
	public void setMoreButtonVisible(boolean visible) {
		moreButton.setVisible(visible);
	}
	
	@Override
	public void setEditVersionInfoButtonVisible(boolean isVisible) {
		editInfoButton.setVisible(isVisible);
	}
	
	@Override
	public void showEditVersionInfo(String oldLabel, String oldComment) {
		editVersionInfoModal.configure("Edit Version Info", "Version label", oldLabel, "Version comment", oldComment, DisplayConstants.OK);
		editVersionInfoModal.show();
	}
	
	@Override
	public void showEditVersionInfoError(String error) {
		editVersionInfoModal.showError(error);
	}
	
	@Override
	public void hideEditVersionInfo() {
		editVersionInfoModal.hide();
	}
	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
}
