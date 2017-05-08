package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.Map;

import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.ValidationUtils;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PlotlyConfigViewImpl implements PlotlyConfigView {
	private Presenter presenter;
	public interface PlotlyConfigViewImplUiBinder extends UiBinder<Widget, PlotlyConfigViewImpl> {}
	Widget widget;
	
	@Inject
	public PlotlyConfigViewImpl(PlotlyConfigViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
	}
	
	
	@Override
	public Widget asWidget() {
		return widget;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
}
