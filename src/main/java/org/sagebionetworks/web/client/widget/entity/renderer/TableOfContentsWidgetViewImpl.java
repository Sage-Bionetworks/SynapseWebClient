package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.shared.WidgetConstants;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TableOfContentsWidgetViewImpl extends FlowPanel implements TableOfContentsWidgetView {

	private Presenter presenter;
	private boolean hasLoaded;
	
	@Inject
	public TableOfContentsWidgetViewImpl() {
	}
	@Override
	protected void onAttach() {
		super.onAttach();
		if (!hasLoaded) {
			hasLoaded = true;
			FlowPanel linkContainer = new FlowPanel();
			HTMLPanel parentPanel = (HTMLPanel)this.getParent();
			//look for these special header ids (that were added by the markdown processor for us), and create links to them
			String id = WidgetConstants.MARKDOWN_HEADING_ID_PREFIX;
			int i = 0;
			Element heading = parentPanel.getElementById(id + i);
			if (heading == null) {
				//no entries.  add an informative message
				linkContainer.add(new HTML("<p class=\"smallGreyText\">"+DisplayConstants.NO_HEADERS_FOUND+"</p>"));
			}
			
			while (heading != null) {
				String text = heading.getInnerHTML();
				//create links to all headers in the page
				final Element scrollToElement = heading;
				SimplePanel wrapper = new SimplePanel();
				Anchor a = new Anchor();
				a.setHTML(text);
				a.addStyleName("link");
				a.addStyleName(heading.getAttribute("toc-style"));
				
				a.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						Window.scrollTo(0, scrollToElement.getOffsetTop());
					}
				});
				wrapper.add(a);
				linkContainer.add(wrapper);
				i++;
				heading = parentPanel.getElementById(id + i);
			}
			
			add(linkContainer);
		}
	}
	
	@Override
	public void configure() {
		this.clear();
		hasLoaded = false;
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
