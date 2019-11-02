package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.html.Span;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserIdCellEditorViewImpl implements UserIdCellEditorView {

	public interface Binder extends UiBinder<Widget, UserIdCellEditorViewImpl> {
	}

	@UiField
	Span suggestBoxContainer;
	@UiField
	FocusPanel userIdCellRendererContainer;

	Widget widget;

	@Inject
	public UserIdCellEditorViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public void setSynapseSuggestBoxWidget(Widget w) {
		suggestBoxContainer.clear();
		suggestBoxContainer.add(w);
	}

	@Override
	public void setUserIdCellRenderer(Widget w) {
		userIdCellRendererContainer.clear();
		userIdCellRendererContainer.add(w);
	}

	@Override
	public void showEditor(boolean visible) {
		suggestBoxContainer.setVisible(visible);
		userIdCellRendererContainer.setVisible(!visible);
	}

	@Override
	public void setUserIdCellRendererClickHandler(ClickHandler clickHandler) {
		userIdCellRendererContainer.addClickHandler(clickHandler);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
}
