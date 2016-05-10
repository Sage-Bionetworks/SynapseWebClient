package org.sagebionetworks.web.client;

public class MarkdownItImpl implements MarkdownIt {
	
	//on first reference, initialize md library and regular expressions
	static {
		_staticMarkdownInit();
	}
	
	private final static native String _staticMarkdownInit() /*-{
		$wnd.md = $wnd.markdownit();
		$wnd.markdownitSynapse.init_markdown_it($wnd.md, 
			$wnd.markdownitSub, 
			$wnd.markdownitSup, 
			$wnd.markdownitCentertext,
			$wnd.markdownitSynapseHeading,
			$wnd.markdownitSynapseTable,
			$wnd.markdownitStrikethroughAlt,
			$wnd.markdownitContainer,
			$wnd.markdownitEmphasisAlt
			);
		
	     $wnd.md.set({
	        highlight : function(str, lang) {
	          if (lang && $wnd.hljs.getLanguage(lang)) {
	            try {
	              return $wnd.hljs.highlight(lang, str).value;
	            } catch (__) {
	            }
	          }
	          return ''; // use external default escaping
	        }
	      });
	}-*/;
	
	@Override
	public String markdown2Html(String md, String uniqueSuffix) {
		return _markdown2Html(md, uniqueSuffix);
	}

	private final static native String _markdown2Html(String md,
			String uniqueSuffix) /*-{
				
		// load the plugin to recognize Synapse markdown widget syntax (with the uniqueSuffix parameter)
		$wnd.md.use($wnd.markdownitSynapse, uniqueSuffix)
			.use($wnd.markdownitMath, uniqueSuffix);
		var results = $wnd.md.render($wnd.markdownitSynapse.preprocessMarkdown(md));
		// Were footnotes found (and exported)?  If so, run the processor on the footnotes, and append to the results.
		var footnotes = $wnd.markdownitSynapse.footnotes();
		if(footnotes.length !== 0) {
			//reset footnote id and rerun on footnotes that were discovered in the first pass
			$wnd.markdownitSynapse.resetFootnoteId();
			var footnotesHtml = $wnd.md.render(footnotes);
			results += '<hr>' + footnotesHtml;
		}
		
		return results;
	}-*/;

}
