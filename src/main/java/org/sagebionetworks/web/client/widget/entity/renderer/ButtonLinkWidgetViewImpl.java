package org.sagebionetworks.web.client.widget.entity.renderer;

import static org.sagebionetworks.web.client.DisplayUtils.newWindow;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.events.ChangeSynapsePlaceEvent;
import org.sagebionetworks.web.client.mvp.AppPlaceHistoryMapper;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ButtonLinkWidgetViewImpl extends Div implements ButtonLinkWidgetView {
	private Button button;
	private static AppPlaceHistoryMapper appPlaceHistoryMapper;
	public static EventBus eventBus;
	
	public static final ClickHandler BUTTON_LINK_CLICK_HANDLER = event -> {
		event.preventDefault();
		Widget panel = (Widget)event.getSource();
		String href = panel.getElement().getAttribute("href");
		boolean openInNewWindow = panel.getElement().hasAttribute(ButtonLinkWidget.LINK_OPENS_NEW_WINDOW);
		if (openInNewWindow) {
			newWindow(href, "_blank", "");
		} else {
			if (href.contains("#!Synapse:")) {
				Place newPlace = appPlaceHistoryMapper.getPlace(href.substring(href.indexOf('!')));
				eventBus.fireEvent(new ChangeSynapsePlaceEvent((Synapse)newPlace));
			} else {
				Window.Location.assign(href);	
			}
		}
	};
	@Inject
	public ButtonLinkWidgetViewImpl(GlobalApplicationState globalAppState, GWTWrapper gwt, EventBus bus) {
		if (appPlaceHistoryMapper == null) {
			appPlaceHistoryMapper = globalAppState.getAppPlaceHistoryMapper();
			eventBus = bus;
		}
		button = new Button();
		button.addClickHandler(BUTTON_LINK_CLICK_HANDLER);
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, String buttonText, final String url, boolean isHighlight, final boolean openInNewWindow) {
		clear();
		button.setText(buttonText);
		if (isHighlight)
			button.setType(ButtonType.INFO);
		button.setHref(url);
		if (openInNewWindow) {
			button.getElement().setAttribute(ButtonLinkWidget.LINK_OPENS_NEW_WINDOW, "true");	
		}
		add(button);
	}
	
	public void showError(String error) {
		clear();
		add(new HTMLPanel(DisplayUtils.getMarkdownWidgetWarningHtml(error)));
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}	

	@Override
	public void setWidth(String width) {
		button.setWidth(width);
	}
	
	@Override
	public void setSize(ButtonSize size) {
		button.setSize(size);					
	}
}
