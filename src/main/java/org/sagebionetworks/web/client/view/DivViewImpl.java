package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.event.logical.shared.AttachEvent;

public class DivViewImpl extends Div implements DivView {
	Callback onAttachCallback;
	@Override
	public void setText(String text) {
		add(new Text(text));
		addAttachHandler(new AttachEvent.Handler() {
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if (event.isAttached()) {
					onAttachCallback.invoke();
				}
			}
		});
	}
	
	@Override
	public boolean isInViewport() {
		return DisplayUtils.isInViewport(this);
	}
	
	@Override
	public void setOnAttachCallback(Callback onAttachCallback) {
		this.onAttachCallback = onAttachCallback;
	}
	
}
