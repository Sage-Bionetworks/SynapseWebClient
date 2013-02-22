package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
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

public class TableOfContentsWidgetViewImpl extends LayoutContainer implements TableOfContentsWidgetView {

	private Presenter presenter;
	private boolean hasLoaded;
	@Inject
	public TableOfContentsWidgetViewImpl() {
	}
	
	@Override
	public void configure() {
		this.removeAll();
		hasLoaded = false;
	}	
	
	//special component.  The TOC only has the info it needs (which it gets via page reflection) after being rendered!
	@Override
	protected void onLoad() {
		super.onLoad();
		if (!hasLoaded) {
			hasLoaded = true;
			FlowPanel linkContainer = new FlowPanel();
			linkContainer.add(new HTML("<h4>Table Of Contents</h4>"));
			HTMLPanel parentPanel = (HTMLPanel)this.getParent();
			//look for these special header ids (that were added by the markdown processor for us), and create links to them
			String id = WidgetConstants.MARKDOWN_HEADING_ID_PREFIX;
			int i = 0;
			Element heading = parentPanel.getElementById(id + i);
			while (heading != null) {
				String text = heading.getInnerHTML();
				String level = heading.getAttribute("level");
				//base the size of the text on the level
				String tocStyleName = "toc-small";
				if (level.equals("h1"))
					tocStyleName = "toc-h1";
				else if (level.equals("h2"))
					tocStyleName = "toc-h2";
				else if (level.equals("h3"))
					tocStyleName = "toc-h3";
				else if (level.equals("h4"))
					tocStyleName = "toc-h4";
				//create links to all headers in the page
				final Element scrollToElement = heading;
				SimplePanel wrapper = new SimplePanel();
				Anchor a = new Anchor(text);
				a.addStyleName("link");
				a.addStyleName(tocStyleName);
				
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
			layout(true);
		}
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
