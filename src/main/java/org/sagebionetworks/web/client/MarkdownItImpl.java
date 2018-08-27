package org.sagebionetworks.web.client;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.resources.WebResource;
import org.sagebionetworks.web.client.resources.WebResource.ResourceType;
import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class MarkdownItImpl implements MarkdownIt {
	private SynapseJSNIUtils jsniUtils;
	private ResourceLoader resourceLoader;
	public static boolean isLoaded = false;
	private HTMLSanitizer htmlSanitizer;
	@Inject
	public MarkdownItImpl(SynapseJSNIUtils jsniUtils, ResourceLoader resourceLoader, HTMLSanitizer htmlSanitizer) {
		this.resourceLoader = resourceLoader;
		this.jsniUtils = jsniUtils;
		this.htmlSanitizer = htmlSanitizer;
	}
	
	@Override
	public void markdown2Html(String md, String uniqueSuffix, CallbackP<String> callbackHtml) {
		if (!isLoaded) {
			// load markdown it and plugins and call this again
			WebResource pluginsJs = new WebResource(jsniUtils.getCdnEndpoint() + "js/markdown-it-plugins-"+ClientProperties.MARKDOWN_PLUGINS_VERSION+".min.js", ResourceType.JAVASCRIPT);
			List<WebResource> resources = new ArrayList<WebResource>();
			resources.add(ClientProperties.MARKDOWN_IT_JS);
			resources.add(pluginsJs);
			resourceLoader.requires(resources, new AsyncCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					isLoaded = true;
					markdown2Html(md, uniqueSuffix, callbackHtml);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					jsniUtils.consoleError("Unable to load markdown-it and plugins");
				}
			});
			
		} else {
			String html = _markdown2Html(md, uniqueSuffix);
			htmlSanitizer.sanitizeHtml(html, sanitizedHtml -> {
				callbackHtml.invoke(sanitizedHtml);
			});
		}
	}

	private final static native String _markdown2Html(String md,
			String uniqueSuffix) /*-{
		if (!$wnd.markdownit) {
			console.error("Attempted to convert markdown to html before markdown-it was ready.");
			return "";
		}
		if (!$wnd.md) {
		  $wnd.md = $wnd.markdownit();
		  $wnd.markdownitSynapse.init_markdown_it($wnd.md,
		    $wnd.markdownitSub,
		    $wnd.markdownitSup,
		    $wnd.markdownitCentertext,
		    $wnd.markdownitSynapseHeading,
		    $wnd.markdownitSynapseTable,
		    $wnd.markdownitStrikethroughAlt,
		    $wnd.markdownitContainer,
		    $wnd.markdownitEmphasisAlt,
		    $wnd.markdownitInlineComments,
		    $wnd.markdownitBr
		  );
		
		  $wnd.md.set({
		    highlight: function (str, lang) {
		      if (lang && $wnd.hljs.getLanguage(lang)) {
		        try {
		          return $wnd.hljs.highlight(lang, str).value;
		        } catch (__) {
		        }
		      }
		      return ''; // use external default escaping
		    }
		  });
		}
		
		// load the plugin to recognize Synapse markdown widget syntax (with the uniqueSuffix parameter)
		$wnd.md.use($wnd.markdownitSynapse, uniqueSuffix)
		  .use($wnd.markdownitMath, uniqueSuffix);
		var results = $wnd.md.render($wnd.markdownitSynapse.preprocessMarkdown(md));
		// Were footnotes found (and exported)?  If so, run the processor on the footnotes, and append to the results.
		var footnotes = $wnd.markdownitSynapse.footnotes();
		if (footnotes.length !== 0) {
		  //reset footnote id and rerun on footnotes that were discovered in the first pass
		  $wnd.markdownitSynapse.resetFootnoteId();
		  var footnotesHtml = $wnd.md.render(footnotes);
		  results += '<hr>' + footnotesHtml;
		}
		
		return results;
	}-*/;

}
