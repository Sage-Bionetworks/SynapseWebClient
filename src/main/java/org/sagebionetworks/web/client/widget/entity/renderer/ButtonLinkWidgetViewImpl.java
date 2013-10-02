package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.DisplayUtils;
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
	@Inject
	public ButtonLinkWidgetViewImpl() {
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, String buttonText, final String url) {
		removeAll();
		Button button = new Button(buttonText);
		button.removeStyleName("gwt-Button");
		button.addStyleName("btn btn-default btn-lg");
		button.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow(url, "", "");
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
