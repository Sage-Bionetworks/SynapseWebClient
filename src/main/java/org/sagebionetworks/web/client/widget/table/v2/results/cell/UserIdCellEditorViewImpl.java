package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import java.util.Date;

import org.gwtbootstrap3.client.ui.html.Span;

import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserIdCellEditorViewImpl implements UserIdCellEditorView {
	
	public interface Binder extends UiBinder<Widget, UserIdCellEditorViewImpl> {}
	
	@UiField
	Span suggestBoxContainer;
	
	Widget widget;
	
	@Inject
	public UserIdCellEditorViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
	}

	@Override
	public void setSynapseSuggestBoxWidget(Widget w) {
		suggestBoxContainer.clear();
		suggestBoxContainer.add(w);
	}
	@Override
	public Widget asWidget() {
		return widget;
	}
}
