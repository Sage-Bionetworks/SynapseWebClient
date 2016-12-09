package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import java.util.Date;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Strong;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.DateTimePicker;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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
	Widget w;
	Presenter presenter;
	
	@Inject
	public FacetColumnResultDateRangeViewImpl(Binder binder){
		w = binder.createAndBindUi(this);
		applyButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				presenter.onFacetChange();
			};
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
	}
}
