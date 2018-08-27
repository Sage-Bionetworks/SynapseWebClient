package org.sagebionetworks.web.client;

import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.resources.WebResource;
import org.sagebionetworks.web.client.resources.WebResource.ResourceType;
import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class HTMLSanitizerImpl implements HTMLSanitizer {
	public static boolean isLoaded = false;
	public boolean isFilterXssInitialized = false;
	ResourceLoader resourceLoader;
	SynapseJSNIUtilsImpl jsniUtils;
	@Inject
	public HTMLSanitizerImpl(ResourceLoader resourceLoader, SynapseJSNIUtilsImpl jsniUtils) {
		this.resourceLoader = resourceLoader;
		this.jsniUtils = jsniUtils;
	}

	@Override
	public void sanitizeHtml(String html, CallbackP<String> sanitizedHtmlCallback) {
		if (!isLoaded) {
			WebResource XSS_JS = new WebResource(jsniUtils.getCdnEndpoint() + "js/xss.min.js", ResourceType.JAVASCRIPT);
			resourceLoader.requires(XSS_JS, new AsyncCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					isLoaded = true;
					sanitizeHtml(html, sanitizedHtmlCallback);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					jsniUtils.consoleError("Unable to sanitize html: " + caught.getMessage());
				}
			});
		} else {
			if (!isFilterXssInitialized) {
				//init
				initFilterXss();
				isFilterXssInitialized = true;
			}
			sanitizedHtmlCallback.invoke(_sanitizeHtml(html));
		}
	}

	private final static native void initFilterXss() /*-{
		var options = {
				whiteList: {
				    a:      ['target', 'href', 'title'],
				    abbr:   ['title'],
				    address: [],
				    area:   ['shape', 'coords', 'href', 'alt'],
				    article: [],
				    aside:  [],
				    audio:  ['autoplay', 'controls', 'loop', 'preload', 'src'],
				    b:      [],
				    bdi:    ['dir'],
				    bdo:    ['dir'],
				    big:    [],
				    blockquote: ['cite'],
				    body:   [],
				    br:     [],
				    caption: [],
				    center: [],
				    cite:   [],
				    code:   [],
				    col:    ['align', 'valign', 'span', 'width'],
				    colgroup: ['align', 'valign', 'span', 'width'],
				    dd:     [],
				    del:    ['datetime'],
				    details: ['open'],
				    div:    ['class'],
				    dl:     [],
				    dt:     [],
				    em:     [],
				    font:   ['color', 'size', 'face'],
				    footer: [],
				    h1:     ['toc'],
				    h2:     ['toc'],
				    h3:     ['toc'],
				    h4:     ['toc'],
				    h5:     ['toc'],
				    h6:     ['toc'],
				    head:   [],
				    header: [],
				    hr:     [],
				    html:   [],
				    i:      [],
				    img:    ['src', 'alt', 'title', 'width', 'height'],
				    ins:    ['datetime'],
				    li:     [],
				    mark:   [],
				    nav:    [],
				    noscript: [],
				    ol:     [],
				    p:      [],
				    pre:    [],
				    s:      [],
				    section:[],
				    small:  [],
				    span:   ['widgetparams', 'class', 'id'],
				    sub:    [],
				    summary: [],
				    sup:    [],
				    strong: [],
				    table:  ['width', 'border', 'align', 'valign', 'class'],
				    tbody:  ['align', 'valign'],
				    td:     ['width', 'rowspan', 'colspan', 'align', 'valign'],
				    tfoot:  ['align', 'valign'],
				    th:     ['width', 'rowspan', 'colspan', 'align', 'valign', 'class'],
				    thead:  ['align', 'valign'],
				    tr:     ['rowspan', 'align', 'valign'],
				    tt:     [],
				    u:      [],
				    ul:     [],
				    video:  ['autoplay', 'controls', 'loop', 'preload', 'src', 'height', 'width']
				},
				stripIgnoreTagBody: true,  // filter out all tags not in the whitelist
				allowCommentTag: false,
				css: false,
				onIgnoreTag: function (tag, html, options) {
					if (tag === '!doctype') {
				      // do not filter doctype
				      return html;
				    }
				},
				safeAttrValue: function (tag, name, value) {
					// returning nothing removes the value
					if (tag === 'img' && name === 'src') {
						if (value && (value.startsWith('data:image/') || value.startsWith('http'))) {
							return value;
						}
					} else {
						return value; 
					}
				}
			};
			$wnd.xss = new $wnd.filterXSS.FilterXSS(options);
	}-*/;
	
	private final static native String _sanitizeHtml(String html) /*-{
		try {
			return $wnd.xss.process(html);
		} catch (err) {
			console.error(err);
		}
	}-*/;
}
