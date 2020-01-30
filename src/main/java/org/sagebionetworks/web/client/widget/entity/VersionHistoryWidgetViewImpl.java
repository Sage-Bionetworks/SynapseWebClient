package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.view.bootstrap.table.TableHeader;
import org.sagebionetworks.web.client.widget.doi.DoiWidgetV2;
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
public class VersionHistoryWidgetViewImpl extends Composite implements VersionHistoryWidgetView, IsWidget {

	interface VersionHistoryWidgetViewImplUiBinder extends UiBinder<Widget, VersionHistoryWidgetViewImpl> {
	}

	private static VersionHistoryWidgetViewImplUiBinder uiBinder = GWT.create(VersionHistoryWidgetViewImplUiBinder.class);

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
	@UiField
	TableHeader sizeTableHeader;
	@UiField
	TableHeader md5TableHeader;
	@UiField
	org.sagebionetworks.web.client.view.bootstrap.table.Table versionTable;
	@UiField
	Div emptyUI;
	CallbackP<List<String>> versionValuesCallback;
	PromptForValuesModalView editVersionInfoModal;
	boolean isTable = false;
	private static DateTimeFormat shortDateFormat = DateTimeFormat.getShortDateFormat();
	private Presenter presenter;

	@Inject
	public VersionHistoryWidgetViewImpl(PortalGinInjector ginInjector, PromptForValuesModalView editVersionInfoDialog) {
		this.ginInjector = ginInjector;
		this.editVersionInfoModal = editVersionInfoDialog;
		initWidget(uiBinder.createAndBindUi(this));
		getElement().setAttribute("highlight-box-title", "Version History");
		versionValuesCallback = values -> {
			presenter.updateVersionInfo(values.get(0), values.get(1));
		};
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
		isTable = entity instanceof Table;
		sizeTableHeader.setVisible(!isTable);
		md5TableHeader.setVisible(!isTable);
		currentVersionLink.setTargetHistoryToken(DisplayUtils.getSynapseHistoryTokenNoHash(entity.getId()));
		currentVersionLink.setVisible(isShowingOlderVersion);
		if (isShowingOlderVersion) {
			setVisible(true);
		}
	}

	@Override
	public void clearVersions() {
		previousVersionsTable.clear();
		emptyUI.setVisible(false);
		versionTable.setVisible(true);
	}

	@Override
	public void addVersion(String entityId, final VersionInfo version, boolean canEdit, boolean isVersionSelected) {
		VersionHistoryRowView fileHistoryRow = ginInjector.getFileHistoryRow();
		fileHistoryRow.setMd5TableDataVisible(!isTable);
		fileHistoryRow.setSizeTableDataVisible(!isTable);
		fileHistoryRow.setIsUnlinked(isVersionSelected);
		DoiWidgetV2 doiWidget = ginInjector.getDoiWidget();
		doiWidget.setLabelVisible(false);
		String versionName = version.getVersionLabel();
		String modifiedByUserId = version.getModifiedByPrincipalId();
		String modifiedOn = shortDateFormat.format(version.getModifiedOn());
		String size = "";
		try {
			double sizeDouble = Double.parseDouble(version.getContentSize());
			size = DisplayUtils.getFriendlySize(sizeDouble, true);
		} catch (Throwable t) {
		}
		String md5 = version.getContentMd5();
		Callback deleteCallback = () -> {
			presenter.deleteVersion(version.getVersionNumber());
		};

		String versionComment = version.getVersionComment();
		Long versionNumber = version.getVersionNumber();
		String versionHref = DisplayUtils.getSynapseHistoryToken(version.getId(), version.getVersionNumber());
		fileHistoryRow.configure(versionNumber, versionHref, "Version " + versionName, modifiedByUserId, modifiedOn, size, md5, versionComment, deleteCallback, doiWidget);
		previousVersionsTable.add(fileHistoryRow.asWidget());
		fileHistoryRow.setCanDelete(canEdit);
		fileHistoryRow.setIsVersionSelected(isVersionSelected);
		doiWidget.configure(entityId, ObjectType.ENTITY, versionNumber);
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
	public void clear() {
		// reset versions ui
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
		List<String> prompts = new ArrayList<>();
		prompts.add("Version label");
		prompts.add("Version comment");
		List<String> initialValues = new ArrayList<>();
		initialValues.add(oldLabel);
		initialValues.add(oldComment);
		editVersionInfoModal.configureAndShow("Edit Version Info", prompts, initialValues, versionValuesCallback);
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

	@Override
	public void showNoResults() {
		emptyUI.setVisible(true);
		versionTable.setVisible(false);
	}
}
