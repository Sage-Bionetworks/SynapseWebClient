package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Panel;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.Versionable;
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
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
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
	HTMLPanel versions;
	@UiField
	Panel previousVersions;
	@UiField
	TBody previousVersionsTable;
	@UiField
	InlineLabel allVersions;
	@UiField
	SimplePanel paginationWidgetContainer;
	@UiField
	Hyperlink currentVersionLink;
	@UiField
	Button editNameButton;
	@UiField
	Button editCommentButton;
	EntityNameModalView editCommentModal, editLabelModal;
	
	private static DateTimeFormat shortDateFormat = DateTimeFormat.getShortDateFormat();
	private Presenter presenter;
	
	@Inject
	public FileHistoryWidgetViewImpl(PortalGinInjector ginInjector, EntityNameModalView editCommentDialog, EntityNameModalView editLabelDialog) {
		this.ginInjector = ginInjector;
		this.editCommentModal = editCommentDialog;
		this.editLabelModal = editLabelDialog;
		
		initWidget(uiBinder.createAndBindUi(this));
		DisplayUtils.configureShowHide(allVersions, previousVersions);
		
		editLabelModal.setPresenter(new EntityNameModalView.Presenter() {
			@Override
			public void onPrimary() {
				presenter.updateVersionLabel(editLabelModal.getName());
			}
		});
		editCommentModal.setPresenter(new EntityNameModalView.Presenter() {
			@Override
			public void onPrimary() {
				presenter.updateVersionComment(editCommentModal.getName());
			}
		});
		
		editNameButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onEditVersionLabelClicked();
			}
		});
		editCommentButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onEditVersionCommentClicked();
			}
		});
	}
	
	@Override
	public void setEntityBundle(Entity entity, boolean isShowingOlderVersion) {
		clear();
		currentVersionLink.setTargetHistoryToken(DisplayUtils.getSynapseHistoryTokenNoHash(entity.getId()));
		currentVersionLink.setVisible(isShowingOlderVersion);
		setVersionsVisible(false);
		if (entity instanceof Versionable) {
			setVersionsVisible(true);
			setFileHistoryVisible(isShowingOlderVersion);
		}
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
		fileHistoryRow.setIsVersionLink(!isVersionSelected);
	}

	@Override
	public void setFileHistoryVisible(boolean v) {
		String text = v ? DisplayConstants.HIDE_LC : DisplayConstants.SHOW_LC;
		allVersions.setText(text);
		previousVersions.setVisible(v);
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
		//reset versions ui
		setVersionsVisible(false);
		previousVersions.setVisible(false);
		allVersions.setText(DisplayConstants.SHOW_LC);
		clearVersions();
	}
	
	public void setVersionsVisible(boolean visible) {
		versions.setVisible(visible);
	}
	
	@Override
	public void setPaginationWidget(Widget widget) {
		paginationWidgetContainer.setWidget(widget);
	}
	
	@Override
	public void setEditVersionCommentButtonVisible(boolean isVisible) {
		editCommentButton.setVisible(isVisible);
	}
	@Override
	public void setEditVersionLabelButtonVisible(boolean isVisible) {
		editNameButton.setVisible(isVisible);
	}
	@Override
	public void showEditVersionLabel(String oldValue) {
		editLabelModal.configure("Edit Version Label", "Version label", DisplayConstants.OK, oldValue);
		editLabelModal.show();
	}
	@Override
	public void showEditVersionComment(String oldValue) {
		editCommentModal.configure("Edit Version Comment", "Version comment", DisplayConstants.OK, oldValue);
		editCommentModal.show();
	}
	@Override
	public void hideEditVersionLabel() {
		editLabelModal.hide();
	}
	@Override
	public void hideEditVersionComment() {
		editCommentModal.hide();
	}
	
}
