package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormControlStatic;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.widget.entity.file.Md5Link;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class VersionHistoryRowViewImpl implements VersionHistoryRowView {
	public interface Binder extends UiBinder<Widget, VersionHistoryRowViewImpl> {
	}

	@UiField
	Anchor versionNameLink;
	@UiField
	FormControlStatic versionName;
	@UiField
	FormControlStatic versionComment;
	@UiField
	SimplePanel modifiedByContainer;
	@UiField
	FormControlStatic modifiedOn;
	@UiField
	FormControlStatic size;
	@UiField
	SimplePanel md5LinkContainer;
	@UiField
	Button deleteButton;
	@UiField
	Div doiWidgetContainer;
	@UiField
	TableData sizeTableData;
	@UiField
	FormControlStatic versionNumber;
	@UiField
	Anchor versionNumberLink;
	@UiField
	TableData md5TableData;
	Callback deleteCallback;
	UserBadge userBadge;
	private Widget widget;
	Md5Link md5Link;

	@Inject
	public VersionHistoryRowViewImpl(Binder binder, UserBadge userBadge, Md5Link md5Link) {
		widget = binder.createAndBindUi(this);
		this.md5Link = md5Link;
		deleteButton.addClickHandler(event -> {
			DisplayUtils.confirmDelete(DisplayConstants.PROMPT_SURE_DELETE + " version?", () -> {
				if (deleteCallback != null) {
					deleteCallback.invoke();
				}
			});
		});

		md5LinkContainer.setWidget(md5Link.asWidget());
		this.userBadge = userBadge;
		modifiedByContainer.setWidget(userBadge.asWidget());
	}


	@Override
	public void configure(Long versionNumber, String versionLinkHref, String versionName, String modifiedByUserId, String modifiedOn, String size, String md5, String versionComment, Callback deleteCallback, IsWidget doiWidget) {
		this.versionNumber.setText(versionNumber.toString());
		this.versionNumberLink.setText(versionNumber.toString());
		this.versionNameLink.setText(versionName);
		this.versionName.setText(versionName);
		this.modifiedOn.setText(modifiedOn);
		this.versionComment.setText(versionComment);
		this.size.setText(size);
		md5Link.configure(md5);
		this.deleteCallback = deleteCallback;
		userBadge.configure(modifiedByUserId);
		versionNameLink.setHref(versionLinkHref);
		versionNumberLink.setHref(versionLinkHref);
		doiWidgetContainer.add(doiWidget);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setCanDelete(boolean canDelete) {
		deleteButton.setVisible(canDelete);
	}

	@Override
	public void setIsVersionSelected(boolean isVersionSelected) {
		if (isVersionSelected) {
			versionName.addStyleName("boldText");
			versionNumberLink.addStyleName("boldText");
		} else {
			versionName.removeStyleName("boldText");
			versionNumberLink.removeStyleName("boldText");
		}
	}

	@Override
	public void setIsUnlinked(boolean isUnlinked) {
		versionName.setVisible(isUnlinked);
		versionNameLink.setVisible(!isUnlinked);
		versionNumber.setVisible(isUnlinked);
		versionNumberLink.setVisible(!isUnlinked);
	}

	@Override
	public void setMd5TableDataVisible(boolean isVisible) {
		md5TableData.setVisible(isVisible);
	}

	@Override
	public void setSizeTableDataVisible(boolean isVisible) {
		sizeTableData.setVisible(isVisible);
	}
}
