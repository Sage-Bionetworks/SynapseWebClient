package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.html.Text;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PythonClientGetEntityWidgetViewImpl extends com.google.gwt.user.client.ui.Composite {

	public interface Binder extends UiBinder<Widget, PythonClientGetEntityWidgetViewImpl> {}
	
	@UiField
	Text download;
	
	@UiField
	Text getPath;	
	
	Widget widget;
	
	@Inject
	public PythonClientGetEntityWidgetViewImpl(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
		initWidget(widget);
	}
	
	public void configure(String id) {	
		String safeId = SafeHtmlUtils.fromString(id).asString();
		download.setText(safeId	+ " = " + "syn.get('" + safeId + "')");
		getPath.setText("filepath = " + safeId + ".path");
	}
	
	public Widget asWidget() {
		return widget;
	}
}
