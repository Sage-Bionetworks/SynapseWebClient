package org.sagebionetworks.web.client.widget.entity.act;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.InlineCheckBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.dataaccess.AccessType;
import org.sagebionetworks.repo.model.dataaccess.AccessorChange;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.SelectableListItem;
import org.sagebionetworks.web.client.widget.profile.ProfileCertifiedValidatedWidget;
import org.sagebionetworks.web.client.widget.user.BadgeSize;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;


public class UserBadgeItem implements IsWidget, SelectableListItem {
	public interface UserBadgeItemUiBinder extends UiBinder<Widget, UserBadgeItem> {}
	
	@UiField
	InlineCheckBox select;
	@UiField
	Div userBadgeContainer;
	@UiField
	Button dropdown;
	@UiField
	AnchorListItem renew;
	@UiField
	AnchorListItem revoke;
	
	Widget widget;
	
	String userId;
	Callback selectionChangedCallback;
	PortalGinInjector portalGinInjector;
	AccessType accessType;

	@Inject
	public UserBadgeItem(UserBadgeItemUiBinder binder,
			PortalGinInjector portalGinInjector) {
		widget = binder.createAndBindUi(this);
		this.portalGinInjector = portalGinInjector;
		select.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (selectionChangedCallback != null) {
					selectionChangedCallback.invoke();
				}
			}
		});
		
		renew.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showRenew();
			}
		});
		revoke.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showRevoke();
			}
		});
	}
	
	private void showRenew() {
		accessType = AccessType.RENEW_ACCESS;
		dropdown.setText(renew.getText());
		userBadgeContainer.removeStyleName("strikeout-links lightgrey-links");
	}
	
	private void showRevoke() {
		accessType = AccessType.REVOKE_ACCESS;
		dropdown.setText(revoke.getText());
		userBadgeContainer.addStyleName("strikeout-links lightgrey-links");
	}
	
	public UserBadgeItem configure(AccessorChange change) {
		userId = change.getUserId();
		accessType = change.getType();
		boolean isGainAccess = AccessType.GAIN_ACCESS.equals(accessType);
		dropdown.setVisible(!isGainAccess);
		if (!isGainAccess) {
			select.setVisible(false);
		}
		
		switch(accessType) {
			case RENEW_ACCESS:
				showRenew();
				break;
			case REVOKE_ACCESS:
				showRevoke();
				break;
			default:
		}
		
		UserBadge userBadge = portalGinInjector.getUserBadgeWidget();
		userBadge.configure(userId);
		userBadge.setSize(BadgeSize.DEFAULT);
		userBadge.setCustomClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setSelected(!isSelected());
				if (selectionChangedCallback != null) {
					selectionChangedCallback.invoke();
				}
			}
		});
		userBadgeContainer.add(userBadge.asWidget());
		
		ProfileCertifiedValidatedWidget w = portalGinInjector.getProfileCertifiedValidatedWidget();
		w.configure(Long.parseLong(userId));
		w.asWidget().addStyleName("margin-left-5");
		userBadgeContainer.add(w.asWidget());
		
		return this;
	}
	
	public UserBadgeItem setSelectionChangedCallback(Callback callback) {
		selectionChangedCallback = callback;
		return this;
	}
	
	public boolean isSelected() {
		return select.getValue();
	}
	
	public void setSelected(boolean selected){
		if (select.isVisible()) {
			select.setValue(selected, true);	
		}
	}
	
	public void setSelectVisible(boolean visible) {
		select.setVisible(visible);
		if (!visible) {
			userBadgeContainer.setMarginLeft(20);
		}
	}
	
	public String getUserId() {
		return userId;
	}
	
	public AccessType getAccessType() {
		return accessType;
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	public void setAccessTypeDropdownEnabled(boolean enabled) {
		dropdown.setEnabled(enabled);
	}
}
