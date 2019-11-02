package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.html.Span;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ContainerItemCountWidgetViewImpl implements ContainerItemCountWidgetView {
	@UiField
	Span itemCountField;
	Widget widget;

	public interface Binder extends UiBinder<Widget, ContainerItemCountWidgetViewImpl> {
	}

	@Inject
	public ContainerItemCountWidgetViewImpl(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
	}

	@Override
	public void showCount(Long count) {
		itemCountField.setText(count.toString());
		widget.setVisible(true);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void clear() {
		itemCountField.clear();
	}

	@Override
	public void hide() {
		widget.setVisible(false);
	}
}
