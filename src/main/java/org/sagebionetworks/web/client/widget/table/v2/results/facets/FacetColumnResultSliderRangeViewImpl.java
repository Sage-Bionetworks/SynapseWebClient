package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import org.gwtbootstrap3.client.ui.Radio;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Strong;
import org.gwtbootstrap3.extras.slider.client.ui.Range;
import org.gwtbootstrap3.extras.slider.client.ui.RangeSlider;
import org.gwtbootstrap3.extras.slider.client.ui.base.event.SlideStopEvent;
import org.gwtbootstrap3.extras.slider.client.ui.base.event.SlideStopHandler;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FacetColumnResultSliderRangeViewImpl implements FacetColumnResultSliderRangeView {
	
	public interface Binder extends UiBinder<Widget, FacetColumnResultSliderRangeViewImpl> {	}
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
	Range range;
	
	@UiField
	Radio notSetRadio;
	@UiField
	Radio anyRadio;
	@UiField
	Radio rangeRadio;
	@UiField
	Div rangeUI;
	Range currentRange;
	
	@Inject
	public FacetColumnResultSliderRangeViewImpl(Binder binder){
		w = binder.createAndBindUi(this);
		slider.setEnabled(true);
		slider.addSlideStopHandler(new SlideStopHandler<Range>() {
			@Override
			public void onSlideStop(SlideStopEvent<Range> event) {
				currentRange = event.getValue();
				presenter.onFacetChange();
			}
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
	public void setMin(double min) {
		slider.setMin(min);
	}
	
	@Override
	public void setMax(double max) {
		slider.setMax(max);
	}
	
	@Override
	public void setRange(Range range) {
		currentRange = range;
		slider.setValue(range);
		minField.setText(range.getMinValue() + "");
		maxField.setText(range.getMaxValue() + "");
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
	public void setSliderStepSize(double step) {
		slider.setStep(step);
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
	public Range getRange() {
		return currentRange;
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
