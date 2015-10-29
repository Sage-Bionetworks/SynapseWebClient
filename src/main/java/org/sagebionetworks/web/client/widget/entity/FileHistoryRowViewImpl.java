package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormControlStatic;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.gwtbootstrap3.extras.bootbox.client.callback.ConfirmCallback;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.file.Md5Link;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileHistoryRowViewImpl implements FileHistoryRowView {
	public interface Binder extends UiBinder<Widget, FileHistoryRowViewImpl> {}
	
	@UiField
	FormControlStatic versionName;
	@UiField
	Anchor versionNameLink;
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
	Callback deleteCallback;
	UserBadge userBadge;
	private Widget widget;
	Md5Link md5Link;
	
	@Inject
	public FileHistoryRowViewImpl(Binder binder, UserBadge userBadge, Md5Link md5Link) {
		widget = binder.createAndBindUi(this);
		this.md5Link = md5Link;
		deleteButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Bootbox.confirm(DisplayConstants.PROMPT_SURE_DELETE + " version?", new ConfirmCallback() {
					@Override
					public void callback(boolean confirmed) {
						if (confirmed && deleteCallback != null) {
							deleteCallback.invoke();			
						}
					}
				});
			}
		});
		
		md5LinkContainer.add(md5Link.asWidget());
		this.userBadge = userBadge;
		modifiedByContainer.add(userBadge.asWidget());
	}
	
	
	@Override
	public void configure(Long versionNumber, String versionLinkHref, String versionName,
			String modifiedByUserId, String modifiedOn, String size,
			String md5, String versionComment, Callback deleteCallback) {
		this.versionName.setText(versionName);
		this.versionNameLink.setText(versionName);
		this.modifiedOn.setText(modifiedOn);
		this.versionComment.setText(versionComment);
		this.size.setText(size);
		md5Link.configure(md5);
		this.deleteCallback = deleteCallback;
		userBadge.configure(modifiedByUserId);
		versionNameLink.setHref(versionLinkHref);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void setCanEdit(boolean canEdit) {
		deleteButton.setVisible(canEdit);
	}
	@Override
	public void setIsVersionLink(boolean isLink) {
		versionNameLink.setVisible(isLink);
		versionName.setVisible(!isLink);
	}
}
