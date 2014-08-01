package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ButtonLinkWidgetViewImpl extends LayoutContainer implements ButtonLinkWidgetView {

	private Presenter presenter;
	private GWTWrapper gwt;
	
	@Inject
	public ButtonLinkWidgetViewImpl(GWTWrapper gwt) {
		this.gwt = gwt;
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, String buttonText, final String url, boolean isHighlight, final boolean openInNewWindow) {
		removeAll();
		Button button = new Button(buttonText);
		button.removeStyleName("gwt-Button");
		String buttonColorStyle = isHighlight ? "btn-info" : "btn-default";
		button.addStyleName("btn "+buttonColorStyle+" btn-lg");
		button.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				if (openInNewWindow)
					DisplayUtils.newWindow(url, "", "");
				else
					gwt.assignThisWindowWith(url);
			}
		});
		add(button);
		layout(true);
	}
	
	public void showError(String error) {
		removeAll();
		add(new HTMLPanel(DisplayUtils.getMarkdownWidgetWarningHtml(error)));
		layout(true);
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
		
	
	/*
	 * Private Methods
	 */

}
