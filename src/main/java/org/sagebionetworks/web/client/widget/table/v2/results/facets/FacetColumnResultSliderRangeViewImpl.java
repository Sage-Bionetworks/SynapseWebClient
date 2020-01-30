package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Radio;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Strong;
import org.gwtbootstrap3.extras.slider.client.ui.Range;
import org.gwtbootstrap3.extras.slider.client.ui.RangeSlider;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FacetColumnResultSliderRangeViewImpl implements FacetColumnResultRangeView {
	public static final int NUMBER_OF_STEPS = 200;

	public interface Binder extends UiBinder<Widget, FacetColumnResultSliderRangeViewImpl> {
	}

	@UiField
	Strong columnName;
	@UiField
	RangeSlider slider;

	@UiField
	Span minField;
	@UiField
	Span maxField;

	Widget w;
	Presenter presenter;

	@UiField
	Radio notSetRadio;
	@UiField
	Radio anyRadio;
	@UiField
	Radio rangeRadio;
	@UiField
	Div rangeUI;
	@UiField
	Div synAlertContainer;
	@UiField
	Button applyButton;
	Range currentRange;
	Double currentSelectedMin, currentSelectedMax, currentLowerbound, currentUpperbound;

	@Inject
	public FacetColumnResultSliderRangeViewImpl(Binder binder) {
		w = binder.createAndBindUi(this);
		slider.setEnabled(true);
		slider.addSlideStopHandler(event -> {
			currentRange = event.getValue();
		});
		applyButton.addClickHandler(event -> {
			presenter.onFacetChange();
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
	public void setMin(String min) {
		if (min != null) {
			currentSelectedMin = Double.valueOf(min);
		}
		updateRange();
	}

	@Override
	public void setMax(String max) {
		if (max != null) {
			currentSelectedMax = Double.valueOf(max);
		}
		updateRange();
	}

	private void updateRange() {
		if (currentSelectedMin != null && currentSelectedMax != null) {
			currentRange = new Range(currentSelectedMin, currentSelectedMax);
			slider.setValue(currentRange);
			minField.setText(currentRange.getMinValue() + "");
			maxField.setText(currentRange.getMaxValue() + "");
		}
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
	public String getMin() {
		return Double.toString(currentRange.getMinValue());
	}

	@Override
	public String getMax() {
		return Double.toString(currentRange.getMaxValue());
	}

	@Override
	public void setSynAlert(Widget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}

	@Override
	public void setLowerBound(String lowerbound) {
		if (lowerbound != null) {
			currentLowerbound = Double.parseDouble(lowerbound);
			slider.setMin(currentLowerbound);
		}
		updateStepSize();
	}

	@Override
	public void setUpperBound(String upperbound) {
		if (upperbound != null) {
			currentUpperbound = Double.parseDouble(upperbound);
			slider.setMax(currentUpperbound);
		}
		updateStepSize();
	}

	private void updateStepSize() {
		if (currentLowerbound != null && currentUpperbound != null) {
			double stepSize = getStepSize(currentLowerbound, currentUpperbound);
			slider.setStep(stepSize);
		}
	}

	public double getStepSize(Number min, Number max) {
		double stepSize = 1;
		if (min != null && max != null) {
			stepSize = Math.round((max.doubleValue() - min.doubleValue()) / NUMBER_OF_STEPS);
			if (stepSize < 1) {
				stepSize = 1;
			}
		}
		return stepSize;
	}
}
