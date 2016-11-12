package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import org.gwtbootstrap3.client.ui.html.Strong;
import org.gwtbootstrap3.extras.slider.client.ui.Range;
import org.gwtbootstrap3.extras.slider.client.ui.RangeSlider;
import org.gwtbootstrap3.extras.slider.client.ui.base.event.SlideStopEvent;
import org.gwtbootstrap3.extras.slider.client.ui.base.event.SlideStopHandler;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FacetColumnResultSliderRangeViewImpl implements FacetColumnResultSliderRangeView {
	
	public interface Binder extends UiBinder<Widget, FacetColumnResultSliderRangeViewImpl> {	}
	@UiField
	Strong columnName;
	@UiField
	RangeSlider slider;
	
	Widget w;
	Presenter presenter;
	double min, max, step;
	Range range;
	
	@Inject
	public FacetColumnResultSliderRangeViewImpl(Binder binder){
		w = binder.createAndBindUi(this);
		slider.setEnabled(true);
		slider.addSlideStopHandler(new SlideStopHandler<Range>() {
			@Override
			public void onSlideStop(SlideStopEvent<Range> event) {
				presenter.onFacetChange(event.getValue());
			}
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
		slider.setValue(range);
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
	}
}
