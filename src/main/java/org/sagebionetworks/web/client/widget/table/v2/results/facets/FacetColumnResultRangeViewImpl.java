package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Radio;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Strong;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FacetColumnResultRangeViewImpl implements FacetColumnResultRangeView {

	public interface Binder extends UiBinder<Widget, FacetColumnResultRangeViewImpl> {
	}

	@UiField
	Strong columnName;
	@UiField
	TextBox minField;
	@UiField
	TextBox maxField;
	@UiField
	Button applyButton;
	@UiField
	Div synAlertContainer;
	Widget w;
	Presenter presenter;
	@UiField
	Radio notSetRadio;
	@UiField
	Radio anyRadio;
	@UiField
	Radio rangeRadio;
	@UiField
	HorizontalPanel rangeUI;

	@Inject
	public FacetColumnResultRangeViewImpl(Binder binder) {
		w = binder.createAndBindUi(this);
		applyButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onFacetChange();
			}
		});

		notSetRadio.addClickHandler(event -> {
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
	public void setMax(String max) {
		maxField.setValue(max);
	}

	@Override
	public void setMin(String min) {
		minField.setValue(min);
	}

	@Override
	public String getMax() {
		return maxField.getValue();
	}

	@Override
	public String getMin() {
		return minField.getValue();
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

	@Override
	public void setLowerBound(String lowerbound) {
		// no-op
	}

	@Override
	public void setUpperBound(String upperbound) {
		// no-op
	}
}
