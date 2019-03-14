package org.sagebionetworks.web.client.widget.lazyload;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.LoadingSpinner;

import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LazyLoadWikiWidgetWrapperViewImpl implements LazyLoadWikiWidgetWrapperView {

	public interface Binder extends UiBinder<Widget, LazyLoadWikiWidgetWrapperViewImpl> {	}
	Widget widget;
	Callback onAttachCallback;
	@UiField
	Span widgetContainer;
	@UiField
	Span loadingUI;
	
	@Inject
	public LazyLoadWikiWidgetWrapperViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		widget.addAttachHandler(new AttachEvent.Handler() {
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if (event.isAttached()) {
					onAttach();
				}
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public boolean isInViewport() {
		return DisplayUtils.isInViewport(widget, 500);
	}

	@Override
	public boolean isAttached() {
		return widget.isAttached();
	}

	@Override
	public void setOnAttachCallback(Callback onAttachCallback) {
		this.onAttachCallback = onAttachCallback;
	}

	protected void onAttach() {
		if (onAttachCallback != null) {
			onAttachCallback.invoke();
		}
	}
	
	@Override
	public void showLoading() {
		widgetContainer.setVisible(false);
		loadingUI.setVisible(true);
	}
	
	@Override
	public void showError(String text) {
		loadingUI.add(new Alert(text, AlertType.WARNING));
	}
	
	@Override
	public void showWidget(Widget w, String cssSelector) {
		if (widget.isAttached()) {
			widgetContainer.clear();
			widgetContainer.add(w);
			loadingUI.setVisible(false);
			widgetContainer.setVisible(true);
			w.addStyleName(cssSelector);
		}
	}

}
