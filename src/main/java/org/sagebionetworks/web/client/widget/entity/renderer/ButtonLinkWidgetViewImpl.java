package org.sagebionetworks.web.client.widget.entity.renderer;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ButtonLinkWidgetViewImpl extends FlowPanel implements ButtonLinkWidgetView {

	private Presenter presenter;
	private GWTWrapper gwt;
	private Button button;
	
	@Inject
	public ButtonLinkWidgetViewImpl(GWTWrapper gwt) {
		this.gwt = gwt;
		button = new Button();
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, String buttonText, final String url, boolean isHighlight, final boolean openInNewWindow) {
		clear();
		button.setText(buttonText);
		if (isHighlight)
			button.setType(ButtonType.INFO);
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
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void setWidth(String width) {
		button.setWidth(width);
	}
	
	@Override
	public void setSize(ButtonSize size) {
		button.setSize(size);					
	}
	
	
	/*
	 * Private Methods
	 */

}
