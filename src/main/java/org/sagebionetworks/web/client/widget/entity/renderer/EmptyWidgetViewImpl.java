package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EmptyWidgetViewImpl extends SimplePanel implements EmptyWidgetView {

	@Inject
	public EmptyWidgetViewImpl() {}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void setPresenter(Presenter presenter) {}
}
