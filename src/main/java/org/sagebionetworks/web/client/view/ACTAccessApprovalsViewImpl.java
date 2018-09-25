package org.sagebionetworks.web.client.view;

import java.util.Date;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.DateTimePicker;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.base.events.ChangeDateEvent;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.base.events.ChangeDateHandler;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ACTAccessApprovalsViewImpl implements ACTAccessApprovalsView {

	public interface ACTViewImplUiBinder extends UiBinder<Widget, ACTAccessApprovalsViewImpl> {}
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
	
	private Presenter presenter;
	private Header headerWidget;
	
	Widget widget;
	@Inject
	public ACTAccessApprovalsViewImpl(ACTViewImplUiBinder binder,
			Header headerWidget
			) {
		widget = binder.createAndBindUi(this);
		this.headerWidget = headerWidget;
		headerWidget.configure();
		
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
	}
	
	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		headerWidget.configure();
		headerWidget.refresh();	
		Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public void setAccessRequirementUIVisible(boolean visible) {
		accessRequirementUI.setVisible(visible);
	}
	@Override
	public void setClearAccessRequirementFilterButtonVisible(boolean visible) {
		clearAccessRequirementFilter.setVisible(visible);
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
