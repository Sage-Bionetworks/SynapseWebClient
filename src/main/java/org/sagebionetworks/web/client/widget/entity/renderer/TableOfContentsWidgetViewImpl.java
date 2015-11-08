package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.shared.WidgetConstants;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.HeadingElement;
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
			JsArray<Element> headingElements = _localHeaderElements(this.getElement());
			
			//look for these special header ids (that were added by the markdown processor for us), and create links to them
			
			if (headingElements.length() == 0) {
				//no entries.  add an informative message
				linkContainer.add(new HTML("<p class=\"smallGreyText\">"+DisplayConstants.NO_HEADERS_FOUND+"</p>"));
			}
			for (int j = 0; j < headingElements.length(); j++) {
				Element heading = headingElements.get(j);
				String tocStyle = heading.getAttribute("toc-style");
				if (DisplayUtils.isDefined(tocStyle)) {
					String text = heading.getInnerHTML();
					//create links to all headers in the page
					final Element scrollToElement = heading;
					SimplePanel wrapper = new SimplePanel();
					Anchor a = new Anchor();
					a.setHTML(text);
					a.addStyleName("link");
					a.addStyleName(tocStyle);
					
					a.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							Window.scrollTo(0, scrollToElement.getOffsetTop());
						}
					});
					wrapper.add(a);
					linkContainer.add(wrapper);
				}
			}
			
			add(linkContainer);
		}
	}
	
	private static native JsArray<Element> _localHeaderElements(Element el) /*-{
		//find all header elements in the DOM
		var allHeaderElements = $wnd.jQuery(":header");
		//filter all header elements down to the collection of header elements that are descendents of the local markdown element
		var markdownEl = $wnd.jQuery(el).closest("div.markdown");
		return $wnd.jQuery(markdownEl).find(allHeaderElements);
	}-*/;

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
