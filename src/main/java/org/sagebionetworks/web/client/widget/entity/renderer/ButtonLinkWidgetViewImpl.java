package org.sagebionetworks.web.client.widget.entity.renderer;

import static org.sagebionetworks.web.client.DisplayUtils.newWindow;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.events.ChangeSynapsePlaceEvent;
import org.sagebionetworks.web.client.mvp.AppPlaceHistoryMapper;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ButtonLinkWidgetViewImpl extends Div implements ButtonLinkWidgetView {
	private Button button;
	private static AppPlaceHistoryMapper appPlaceHistoryMapper;
	public static EventBus eventBus;
	public static final String SYNAPSE_PLACE_FRAGMENT = "#!Synapse:";
	public static final ClickHandler BUTTON_LINK_CLICK_HANDLER = event -> {
		if (!DisplayUtils.isAnyModifierKeyDown(event)) {
			event.preventDefault();
			Button button = (Button) event.getSource();
			button.setEnabled(false);
			String href = button.getElement().getAttribute("href");
			boolean openInNewWindow = button.getElement().hasAttribute(ButtonLinkWidget.LINK_OPENS_NEW_WINDOW);
			if (openInNewWindow) {
				newWindow(href, "_blank", "");
			} else {
				if (href.contains(SYNAPSE_PLACE_FRAGMENT) && Window.Location.getHref().contains(SYNAPSE_PLACE_FRAGMENT)) {
					Place newPlace = appPlaceHistoryMapper.getPlace(href.substring(href.indexOf('!')));
					eventBus.fireEvent(new ChangeSynapsePlaceEvent((Synapse) newPlace));
				} else {
					Window.Location.assign(href);
				}
			}
			Timer timer = new Timer() {
				public void run() {
					button.setEnabled(true);
				}
			};
			timer.schedule(2000);
		}
	};

	@Inject
	public ButtonLinkWidgetViewImpl(GlobalApplicationState globalAppState, EventBus bus) {
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

	@Override
	public void addStyleNames(String styleNames) {
		if (styleNames != null && styleNames.length() > 0) {
			button.addStyleName(styleNames);
		}
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
