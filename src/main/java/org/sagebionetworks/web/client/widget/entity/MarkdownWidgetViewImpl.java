package org.sagebionetworks.web.client.widget.entity;


import org.gwtbootstrap3.client.ui.html.Italic;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.mvp.AppPlaceHistoryMapper;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.place.shared.Place;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MarkdownWidgetViewImpl implements MarkdownWidgetView {
	public interface Binder extends UiBinder<Widget, MarkdownWidgetViewImpl> {
	}

	Widget widget;
	SynapseJSNIUtils jsniUtils;

	@UiField
	HTMLPanel contentPanel;

	@UiField
	SimplePanel synAlertPanel;

	@UiField
	Italic emptyPanel;
	public static GlobalApplicationState globalAppState;
	public static final EventListener relativeLinkClickHandler = event -> {
		event.preventDefault();
		if (Event.ONCLICK == event.getTypeInt()) {
			AnchorElement el = (AnchorElement) event.getCurrentTarget();
			String href = el.getHref();
			String placeToken = href.substring(href.indexOf('!'));
			AppPlaceHistoryMapper appPlaceHistoryMapper = globalAppState.getAppPlaceHistoryMapper();
			Place newPlace = appPlaceHistoryMapper.getPlace(placeToken);
			globalAppState.getPlaceChanger().goTo(newPlace);
		}
	};

	@Inject
	public MarkdownWidgetViewImpl(final Binder uiBinder, SynapseJSNIUtils jsniUtils, GlobalApplicationState globalAppState) {
		widget = uiBinder.createAndBindUi(this);
		this.jsniUtils = jsniUtils;
		MarkdownWidgetViewImpl.globalAppState = globalAppState;
	}

	@Override
	public void setSynAlertWidget(Widget synAlert) {
		synAlertPanel.setWidget(synAlert);
	}

	@Override
	public void setEmptyVisible(boolean isVisible) {
		emptyPanel.setVisible(isVisible);
		contentPanel.setVisible(!isVisible);
	}

	@Override
	public void setMarkdown(final String result) {
		contentPanel.getElement().setInnerHTML(result);
		addPlaceChangerEventHandlerToAnchors();
	}

	private void addPlaceChangerEventHandlerToAnchors() {
		// Optimization. handle all anchor links via the placechanger instead of page change
		NodeList<Element> anchors = contentPanel.getElement().getElementsByTagName("a");
		String hostPageURL = GWT.getHostPageBaseURL();
		for (int i = 0; i < anchors.getLength(); i++) {
			AnchorElement anchorElement = (AnchorElement) anchors.getItem(i);
			if (anchorElement.getHref().startsWith(hostPageURL + "#!")) {
				DOM.sinkEvents(anchorElement, Event.ONCLICK | Event.ONMOUSEOUT | Event.ONMOUSEOVER);
				DOM.setEventListener(anchorElement, relativeLinkClickHandler);
			}
		}
	}

	@Override
	public void callbackWhenAttached(final Callback callback) {
		final Timer t = new Timer() {
			@Override
			public void run() {
				if (contentPanel.isAttached()) {
					callback.invoke();
				} else {
					schedule(100);
				}
			}
		};

		t.schedule(100);
	}

	@Override
	public ElementWrapper getElementById(String id) {
		Element ele = contentPanel.getElementById(id);
		return ele == null ? null : new ElementWrapper(ele);
	}

	@Override
	public void addWidget(Widget widget, String divID) {
		contentPanel.add(widget, divID);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void clearMarkdown() {
		contentPanel.clear();
		setMarkdown("");
	}
}
