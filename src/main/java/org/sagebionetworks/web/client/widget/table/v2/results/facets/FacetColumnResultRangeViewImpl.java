package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.extras.slider.client.ui.Range;
import org.gwtbootstrap3.extras.slider.client.ui.RangeSlider;
import org.gwtbootstrap3.extras.slider.client.ui.Slider;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FacetColumnResultRangeViewImpl implements FacetColumnResultRangeView {
	
	public interface Binder extends UiBinder<Widget, FacetColumnResultRangeViewImpl> {	}
	@UiField
	Heading columnName;
	@UiField
	RangeSlider slider;
	
	Widget w;
	Presenter presenter;
	@Inject
	public FacetColumnResultRangeViewImpl(Binder binder){
		w = binder.createAndBindUi(this);
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
	public Range getSliderRange() {
		return slider.getValue();
	}
	
	@Override
	public void setSliderMin(double min) {
		slider.setMin(min);
	}
	@Override
	public void setSliderMax(double max) {
		slider.setMax(max);
	}
	@Override
	public void setSliderRange(Range range) {
		slider.setValue(range);
	}
	@Override
	public void setSliderStepSize(double step) {
		slider.setStep(step);
	}
}
