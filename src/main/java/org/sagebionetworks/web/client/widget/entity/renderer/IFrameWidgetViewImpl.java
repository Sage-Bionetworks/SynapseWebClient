package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class IFrameWidgetViewImpl implements IFrameWidgetView {

	public interface IFrameWidgetViewImplUiBinder extends UiBinder<Widget, IFrameWidgetViewImpl> {}
	
	@UiField
	IFrameElement iframe;
	
	Widget widget;
	
	@Inject
	public IFrameWidgetViewImpl(IFrameWidgetViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
	}
	
	@Override
	public void clear() {
		iframe.setSrc("");
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void configure(String url) {
		iframe.setSrc(url);
	}
	
	@Override
	public void setWidth(String width) {
		iframe.setAttribute("width", width);
	}
	
	@Override
	public void setHeight(String height) {
		iframe.setAttribute("height", height);
	}

}
