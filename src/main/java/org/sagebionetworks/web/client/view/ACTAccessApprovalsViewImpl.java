package org.sagebionetworks.web.client.view;

import java.util.Date;
import java.util.List;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.Well;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Strong;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.DateTimePicker;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.base.events.ChangeDateEvent;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.base.events.ChangeDateHandler;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ACTAccessApprovalsViewImpl implements ACTAccessApprovalsView {

	public interface ACTViewImplUiBinder extends UiBinder<Widget, ACTAccessApprovalsViewImpl> {}

	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	@UiField
	DateTimePicker expiresBeforeDatePicker;
	@UiField
	Div synAlertContainer;
	@UiField
	Div accessRequirementContainer;
	@UiField
	Div showHideAccessRequirementButtonContainer;
	@UiField
	Div tableData;
	@UiField
	Button clearDateFilter;
	@UiField
	Button clearUserFilter;
	@UiField
	Button clearAccessRequirementFilter;
	@UiField
	Panel accessRequirementUI;
	@UiField
	Div userSelectContainer;
	@UiField
	Div currentUserContainer;
	@UiField
	Button reviewRequestsButton;
	
	private Presenter presenter;
	private Header headerWidget;
	private Footer footerWidget;
	
	Widget widget;
	@Inject
	public ACTAccessApprovalsViewImpl(ACTViewImplUiBinder binder,
			Header headerWidget, 
			Footer footerWidget
			) {
		widget = binder.createAndBindUi(this);
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		
		clearDateFilter.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onClearExpireBeforeFilter();
			}
		});
		clearUserFilter.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onClearUserFilter();
			}
		});
		clearAccessRequirementFilter.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onClearAccessRequirementFilter();
			}
		});
		
		expiresBeforeDatePicker.addChangeDateHandler(new ChangeDateHandler() {
			@Override
			public void onChangeDate(ChangeDateEvent evt) {
				presenter.onExpiresBeforeDateSelected(expiresBeforeDatePicker.getValue());
			}
		});
		reviewRequestsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onReviewRequests();
			}
		});
	}
	
	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		header.clear();
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
		headerWidget.refresh();	
		Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public void setAccessRequirementUIVisible(boolean visible) {
		accessRequirementUI.setVisible(visible);
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
	public void setExpiresBeforeDate(Date date) {
		expiresBeforeDatePicker.setValue(date);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void setLoadMoreContainer(IsWidget w) {
		tableData.clear();
		tableData.add(w);
	}
	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
	
	@Override
	public void setAccessRequirementWidget(IsWidget w) {
		accessRequirementContainer.clear();
		accessRequirementContainer.add(w);
	}
	@Override
	public void setShowHideButton(IsWidget button) {
		showHideAccessRequirementButtonContainer.clear();
		showHideAccessRequirementButtonContainer.add(button);
	}

	@Override
	public void setUserPickerWidget(IsWidget w) {
		userSelectContainer.clear();
		userSelectContainer.add(w);
	}
	
	@Override
	public void setSelectedUserBadge(IsWidget w) {
		currentUserContainer.clear();
		currentUserContainer.add(w);
	}

	@Override
	public void setSelectedUserBadgeVisible(boolean visible) {
		currentUserContainer.setVisible(visible);
	}
}
