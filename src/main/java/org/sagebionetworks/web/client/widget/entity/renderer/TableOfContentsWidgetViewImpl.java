package org.sagebionetworks.web.client.widget.entity.renderer;

import static org.sagebionetworks.markdown.constants.WidgetConstants.DIV_ID_WIDGET_PREFIX;

import java.util.HashMap;
import java.util.Map;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseJSNIUtilsImpl;
import org.sagebionetworks.web.shared.WidgetConstants;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TableOfContentsWidgetViewImpl extends FlowPanel implements TableOfContentsWidgetView {

	private Presenter presenter;
	private boolean hasLoaded;
	private Map<String, String> tagName2Style;
	// Matches a widget element id.  Group 1 contains the suffix (that all widgets will use for this particular markdown renderer).
	private RegExp widgetIdRegEx = RegExp.compile("^widget[-]{1}\\d+([-]{1}.*)$", "i");
	// Matches a widget element id.  Group 1 contains prefix, Group 2 contains suffix.  Used to replace widget index (with the next available index, so that the markdown renderer will find it). 
	private RegExp widgetExistsRegEx = RegExp.compile("(widget[-]{1})\\d+([-]{1})", "i");
	// Matches widget parameters.  Group 1 contains full param definition minus the end quote.  Group 2 contains end quote.
	// Used to insert new isTOC parameter into widget definition (badge will ignore clicks).
	private RegExp widgetParamsRegEx = RegExp.compile("(data[-]widgetparams[=]{1}[\"]{1}[^\"]*)([\"]{1})", "i");
	// Matches any html inside of the widget span.  Used to remove any other html (for example, mentioning adds a child user/team anchor, used in notification emails).
	private RegExp widgetInnerHtmlRegEx = RegExp.compile("(<span data[-]widgetparams[^>]*>).*(<\\/span>)", "i");
	
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
			String currentWidgetDiv = DIV_ID_WIDGET_PREFIX + i + widgetSuffix;
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
			// Go through widgets found in heading, and replace widget index with n+1.
			// MarkdownWidget will find these new widgets to process.
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
						// remove any inner html
						headingHtml = widgetInnerHtmlRegEx.replace(headingHtml, "$1$2");
						// replace the wiki widget index in the id
						headingHtml = widgetExistsRegEx.replace(headingHtml, "$1" + newWidgetIndex + "$2");
						// also add the isTOC parameter to the widget definition.  Replace with the first group + new param + second group.
						headingHtml = widgetParamsRegEx.replace(headingHtml, "$1&"+WidgetConstants.IS_TOC_KEY+"=true$2");
						newWidgetIndex++;
					}
					
					HTML html = new HTML("<a class=\"link " + tocStyle + "\">" + headingHtml + "</a>");
					
					html.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							SynapseJSNIUtilsImpl._scrollIntoView(scrollToElement);
						}
					});
					
					linkContainer.add(html);
				}
			}
			
			add(linkContainer);
		}
	}
	
	private static native JsArray<Element> _localHeaderElements(Element el) /*-{
		try {
			//find all header elements in the DOM
			var allHeaderElements = $wnd.jQuery(":header");
			//filter all header elements down to the collection of header elements that are descendents of the local markdown element
			var markdownEl = $wnd.jQuery(el).closest("div.markdown");
			return $wnd.jQuery(markdownEl).find(allHeaderElements);
		} catch (err) {
			console.error(err);
		}
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
}
