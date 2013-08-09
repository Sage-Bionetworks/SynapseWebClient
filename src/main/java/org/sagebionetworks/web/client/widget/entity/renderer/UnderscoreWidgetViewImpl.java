package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.widget.entity.registration.WidgetEncodingUtil;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.inject.Inject;

public class UnderscoreWidgetViewImpl extends LayoutContainer implements UnderscoreWidgetView {
	private Presenter presenter;
	
	@Inject
	public UnderscoreWidgetViewImpl() {
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void configure(String text) {
		this.removeAll();
		String decoded = WidgetEncodingUtil.decodeValue(text);
		addText(decoded);
		//layout(true);
	}

}
