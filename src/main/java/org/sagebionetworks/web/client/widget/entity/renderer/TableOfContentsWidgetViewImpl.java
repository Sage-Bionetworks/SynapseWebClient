package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.HashMap;
import java.util.Map;

import org.sagebionetworks.markdown.constants.MarkdownRegExConstants;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.entity.ElementWrapper;
import org.sagebionetworks.web.shared.WidgetConstants;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SimpleHtmlSanitizer;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TableOfContentsWidgetViewImpl extends FlowPanel implements TableOfContentsWidgetView {

	private Presenter presenter;
	private boolean hasLoaded;
	private Map<String, String> tagName2Style;
	private RegExp widgetIdRegEx = RegExp.compile("^widget[-]{1}\\d+([-]{1}.*)$", "i");
	private RegExp widgetExistsRegEx = RegExp.compile("widget[-]{1}\\d+[-]{1}", "i");
	@Inject
	public TableOfContentsWidgetViewImpl() {
		//build up the tag name to css class name here
		tagName2Style = new HashMap<String, String>();
		for (int i = 0; i < 6; i++) {
			tagName2Style.put("H" + (i+1), "toc-indent" + i);
		}
	}
	
	private String getWidgetSuffix() {
		String tocId = getElement().getParentElement().getParentElement().getParentElement().getId();
		MatchResult matcher = widgetIdRegEx.exec(tocId);
		String widgetSuffix = "";
		if (matcher.getGroupCount() > 1) {
			widgetSuffix = matcher.getGroup(1);
		}
		return widgetSuffix;
	}
	
	private int getNewWidgetIndex() {
		String widgetSuffix = getWidgetSuffix();
		int i = 0;
		Element el = null;
		do {
			i++;
			String currentWidgetDiv = org.sagebionetworks.markdown.constants.WidgetConstants.DIV_ID_WIDGET_PREFIX + i + widgetSuffix;
			el = DOM.getElementById(currentWidgetDiv);
		} while (el != null);
		return i;
	}
	
	@Override
	protected void onAttach() {
		super.onAttach();
		if (!hasLoaded) {
			hasLoaded = true;
			FlowPanel linkContainer = new FlowPanel();
			JsArray<Element> headingElements = _localHeaderElements(this.getElement());
			
			//look for these special header ids (that were added by the markdown processor for us), and create links to them
			
			if (headingElements.length() == 0) {
				//no entries.  add an informative message
				linkContainer.add(new HTML("<p class=\"smallGreyText\">"+DisplayConstants.NO_HEADERS_FOUND+"</p>"));
			}
			// determine element id of this toc element.  find last widget index.
			int newWidgetIndex = getNewWidgetIndex();
			String widgetIdSuffix = getWidgetSuffix();
			String regEx = org.sagebionetworks.markdown.constants.WidgetConstants.DIV_ID_WIDGET_PREFIX + "(\\d+)" + widgetIdSuffix;
			// Go through widgets found in heading, and replace widget index with n+1.
			// MarkdownWidget will go around and look for additional widgets after load

			for (int j = 0; j < headingElements.length(); j++) {
				Element heading = headingElements.get(j);
				String tagName = heading.getTagName();
				String tocStyle = tagName2Style.get(tagName);
				if (heading.hasAttribute("toc-style") || heading.hasAttribute("toc")) {
					//create links to all headers in the page
					final Element scrollToElement = heading;
					String headingHtml = heading.getInnerHTML();
					// this currently only allows a single wiki widget per heading
					if (widgetExistsRegEx.test(headingHtml)) {
						headingHtml = headingHtml.replaceFirst(regEx, org.sagebionetworks.markdown.constants.WidgetConstants.DIV_ID_WIDGET_PREFIX + newWidgetIndex + widgetIdSuffix);
						newWidgetIndex++;
					}
					
					HTML html = new HTML("<a class=\"link " + tocStyle + "\">" + headingHtml + "</a>");
					
					html.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							Window.scrollTo(0, scrollToElement.getOffsetTop());
						}
					});
					
					linkContainer.add(html);
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
