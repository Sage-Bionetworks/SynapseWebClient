package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;

import com.google.gwt.user.client.ui.IsWidget;

public interface IFrameWidgetView extends IsWidget {

	public interface Presenter extends WidgetRendererPresenter {
		
	}

	void configure(String string);

	void setHeight(String height);

	void setWidth(String width);

	void clear();
}
