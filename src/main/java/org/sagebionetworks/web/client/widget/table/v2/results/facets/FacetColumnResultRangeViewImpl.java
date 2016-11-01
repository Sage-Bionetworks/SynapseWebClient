package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FacetColumnResultRangeViewImpl implements FacetColumnResultRangeView {
	
	public interface Binder extends UiBinder<Widget, FacetColumnResultRangeViewImpl> {	}
	@UiField
	Heading columnName;
	@UiField
	TextBox minField;
	@UiField
	TextBox maxField;
	@UiField
	Button applyButton;
	Widget w;
	Presenter presenter;
	
	@Inject
	public FacetColumnResultRangeViewImpl(Binder binder){
		w = binder.createAndBindUi(this);
		applyButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onFacetChange();
			}
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
}
