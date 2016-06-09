package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class DockerTabViewImpl implements DockerTabView {
	Presenter presenter;
	Widget widget;
	public interface TabsViewImplUiBinder extends UiBinder<Widget, DockerTabViewImpl> {}

	public DockerTabViewImpl() {
		TabsViewImplUiBinder binder = GWT.create(TabsViewImplUiBinder.class);
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
