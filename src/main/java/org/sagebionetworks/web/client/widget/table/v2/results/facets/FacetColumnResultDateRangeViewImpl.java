package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import java.util.Date;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Radio;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Strong;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.DateTimePicker;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FacetColumnResultDateRangeViewImpl implements FacetColumnResultDateRangeView {
	
	public interface Binder extends UiBinder<Widget, FacetColumnResultDateRangeViewImpl> {}
	@UiField
	Strong columnName;
	@UiField
	DateTimePicker minDateTimePicker;
	@UiField
	DateTimePicker maxDateTimePicker;
	@UiField
	Div synAlertContainer;
	@UiField
	Button applyButton;
	@UiField
	Radio notSetRadio;
	@UiField
	Radio anyRadio;
	@UiField
	Radio rangeRadio;
	@UiField
	HorizontalPanel rangeUI;
	Widget w;
	Presenter presenter;
	
	@Inject
	public FacetColumnResultDateRangeViewImpl(Binder binder){
		w = binder.createAndBindUi(this);
		applyButton.addClickHandler(event -> {
			presenter.onFacetChange();
		});
		notSetRadio.addClickHandler(event-> {
			rangeUI.setVisible(false);
			presenter.onFacetChange();
		});
		anyRadio.addClickHandler(event -> {
			rangeUI.setVisible(false);
			presenter.onFacetChange();
		});
		rangeRadio.addClickHandler(event -> {
			rangeUI.setVisible(true);
		});
	}

	@Override
	public Widget asWidget() {
		return w;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public Date getMin() {
		return minDateTimePicker.getValue();
	}
	@Override
	public void setMin(Date min) {
		minDateTimePicker.setValue(min);
	}
	@Override
	public Date getMax() {
		return maxDateTimePicker.getValue();
	}
	
	@Override
	public void setMax(Date max) {
		maxDateTimePicker.setValue(max);
	}
	
	@Override
	public void setSynAlert(Widget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
	@Override
	public void setColumnName(String name) {
		columnName.setText(name);
		String radioName = name.replaceAll("\\W", "") + "_radios";
		notSetRadio.setName(radioName);
		anyRadio.setName(radioName);
		rangeRadio.setName(radioName);
	}
	
	@Override
	public boolean isNotSet() {
		return notSetRadio.getValue();
	}
	@Override
	public boolean isAnyValue() {
		return anyRadio.getValue();
	}
	@Override
	public void setIsAnyValue() {
		anyRadio.setValue(true, true);
	}
	@Override
	public void setIsNotSet() {
		notSetRadio.setValue(true, true);
	}
	@Override
	public void setIsRange() {
		rangeRadio.setValue(true, true);
		rangeUI.setVisible(true);
	}
}
