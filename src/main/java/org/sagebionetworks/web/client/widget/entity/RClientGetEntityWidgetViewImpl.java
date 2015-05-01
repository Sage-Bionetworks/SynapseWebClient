package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.html.Text;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RClientGetEntityWidgetViewImpl extends com.google.gwt.user.client.ui.Composite {

	public interface Binder extends UiBinder<Widget, RClientGetEntityWidgetViewImpl> {}
	
	@UiField
	Text getEntity;	
	
	@UiField
	Text loadEntity;	
	
	Widget widget;
	
	@Inject
	public RClientGetEntityWidgetViewImpl(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
		initWidget(widget);
	}
	
	public void configure(String id, Long versionNumber) {
		String middle = " <- synGet(";
		String safeId = SafeHtmlUtils.fromString(id).asString();
		String idString = "id='" + safeId + "'";
		String versionString = versionNumber == null ? "" : ", version='"+versionNumber+"'";
		getEntity.setText(safeId
				+ middle + idString + versionString +")");
		loadEntity.setText(safeId
				+ middle + idString + versionString+", load=T)");
	}
	
	public Widget asWidget() {
		return widget;
	}
}
