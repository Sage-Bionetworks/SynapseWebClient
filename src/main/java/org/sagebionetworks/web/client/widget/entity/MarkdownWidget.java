package org.sagebionetworks.web.client.widget.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.MarkdownIt;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Lightweight widget used to resolve markdown
 * 
 * @author Jay
 *
 */
public class MarkdownWidget implements MarkdownWidgetView.Presenter, IsWidget {

	private SynapseJavascriptClient jsClient;
	private MarkdownIt markdownIt;
	private SynapseJSNIUtils synapseJSNIUtils;
	private WidgetRegistrar widgetRegistrar;
	private CookieProvider cookies;
	AuthenticationController authenticationController;
	GWTWrapper gwt;

	PortalGinInjector ginInjector;
	private ResourceLoader resourceLoader;
	private String md;
	private MarkdownWidgetView view;
	private SynapseAlert synAlert;
	private WikiPageKey wikiKey;
	private Long wikiVersionInView;

	@Inject
	public MarkdownWidget(SynapseJavascriptClient jsClient, SynapseJSNIUtils synapseJSNIUtils, WidgetRegistrar widgetRegistrar, CookieProvider cookies, ResourceLoader resourceLoader, GWTWrapper gwt, PortalGinInjector ginInjector, MarkdownWidgetView view, SynapseAlert synAlert, MarkdownIt markdownIt) {
		super();
		this.jsClient = jsClient;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.widgetRegistrar = widgetRegistrar;
		this.cookies = cookies;
		this.resourceLoader = resourceLoader;
		this.gwt = gwt;
		this.ginInjector = ginInjector;
		this.view = view;
		this.synAlert = synAlert;
		this.markdownIt = markdownIt;
		view.setSynAlertWidget(synAlert.asWidget());
	}

	/**
	 * Configure this widget using markdown only. Note that if no wiki key is given, then some Synapse
	 * widgets may not work properly (widgets that depend on wiki attachments for example).
	 * 
	 * @param md
	 */
	public void configure(final String md) {
		configure(md, null, null);
	}

	@Override
	public void configure(final String md, final WikiPageKey wikiKey, final Long wikiVersionInView) {
		clear();
		this.md = md;
		this.wikiKey = wikiKey;
		this.wikiVersionInView = wikiVersionInView;
		final String uniqueSuffix = "-" + new Date().getTime() + "" + gwt.nextRandomInt();
		view.callbackWhenAttached(new Callback() {
			@Override
			public void invoke() {
				try {
					String result = markdownIt.markdown2Html(md, uniqueSuffix);
					loadHtml(uniqueSuffix, result);
				} catch (RuntimeException e) { // JavaScriptException
					synAlert.showError(e.getMessage());
				}
			}
		});
	}

	public void loadHtml(String uniqueSuffix, String result) {
		if (result != null && !result.isEmpty()) {
			view.setEmptyVisible(false);
			view.setMarkdown(result);
			synapseJSNIUtils.highlightCodeBlocks();
			synapseJSNIUtils.loadSummaryDetailsShim();
			loadMath(uniqueSuffix);
			loadWidgets(wikiKey, wikiVersionInView, uniqueSuffix);
			loadTableSorters();

		} else {
			view.setEmptyVisible(true);
		}
	}

	@Override
	public void clear() {
		synAlert.clear();
		view.clearMarkdown();
		view.setEmptyVisible(false);
	}

	public void loadTableSorters() {
		synapseJSNIUtils.loadTableSorters();
	}

	public void loadMath(String suffix) {
		ClientProperties.fixResourceToCdnEndpoint(ClientProperties.MATH_PROCESSOR_JS, synapseJSNIUtils.getCdnEndpoint());
		// look for every element that has the right format
		int i = 0;
		String currentWidgetDiv = WidgetConstants.DIV_ID_MATHJAX_PREFIX + i + suffix;
		ElementWrapper el = view.getElementById(currentWidgetDiv);
		while (el != null) {
			final Element loadElement = el.getElement();
			final AsyncCallback<Void> mathProcessorLoadedCallback = new AsyncCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					synapseJSNIUtils.processMath(loadElement);
				}

				@Override
				public void onFailure(Throwable caught) {}
			};
			if (resourceLoader.isLoaded(ClientProperties.MATH_PROCESSOR_JS))
				// already loaded
				synapseJSNIUtils.processMath(loadElement);
			else
				resourceLoader.requires(ClientProperties.MATH_PROCESSOR_JS, mathProcessorLoadedCallback);
			i++;
			currentWidgetDiv = WidgetConstants.DIV_ID_MATHJAX_PREFIX + i + suffix;
			el = view.getElementById(currentWidgetDiv);
		}
	}

	public Set<String> loadWidgets(WikiPageKey wikiKey, Long wikiVersionInView, String suffix) {
		Set<String> contentTypes = new HashSet<String>();
		// look for every element that has the right format
		int i = 0;
		String currentWidgetDiv = org.sagebionetworks.markdown.constants.WidgetConstants.DIV_ID_WIDGET_PREFIX + i + suffix;
		ElementWrapper el = view.getElementById(currentWidgetDiv);
		while (el != null) {
			el.removeAllChildren();
			// based on the contents of the element, create the correct widget descriptor and renderer
			String innerText = el.getAttribute("data-widgetParams");
			if (innerText != null) {
				try {
					innerText = innerText.trim();
					String contentType = widgetRegistrar.getWidgetContentType(innerText);
					Map<String, String> widgetDescriptor = widgetRegistrar.getWidgetDescriptor(innerText);
					IsWidget presenter = widgetRegistrar.getWidgetRendererForWidgetDescriptor(wikiKey, contentType, widgetDescriptor, new Callback() {
						@Override
						public void invoke() {
							refresh();
						}
					}, wikiVersionInView);
					if (presenter == null)
						throw new IllegalArgumentException("Unable to render widget from the specified markdown.");
					view.addWidget(presenter.asWidget(), currentWidgetDiv);
					contentTypes.add(contentType);
				} catch (Throwable e) {
					// try our best to load all of the widgets. if one fails to load, then fail quietly.
					String message = innerText;
					if (e.getMessage() != null)
						message += ": " + e.getMessage();
					view.addWidget(new HTMLPanel(DisplayUtils.getMarkdownWidgetWarningHtml(message)), currentWidgetDiv);
				}
			}

			i++;
			currentWidgetDiv = org.sagebionetworks.markdown.constants.WidgetConstants.DIV_ID_WIDGET_PREFIX + i + suffix;
			el = view.getElementById(currentWidgetDiv);
		}
		return contentTypes;
	}

	public void loadMarkdownFromWikiPage(final WikiPageKey wikiKey, final boolean isIgnoreLoadingFailure) {
		synAlert.clear();
		// get the wiki page
		jsClient.getV2WikiPageAsV1(wikiKey, new AsyncCallback<WikiPage>() {
			@Override
			public void onSuccess(WikiPage page) {
				wikiKey.setWikiPageId(page.getId());
				configure(page.getMarkdown(), wikiKey, null);
			}

			@Override
			public void onFailure(Throwable caught) {
				if (!isIgnoreLoadingFailure)
					synAlert.showError(DisplayConstants.ERROR_LOADING_WIKI_FAILED + caught.getMessage());
			}
		});
	}

	public void refresh() {
		configure(md, wikiKey, wikiVersionInView);
	}

	public Widget asWidget() {
		return view.asWidget();
	}

}
