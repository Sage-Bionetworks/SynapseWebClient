package org.sagebionetworks.web.client;

import com.google.inject.Inject;

public class MarkdownItImpl implements MarkdownIt {
	private SynapseJSNIUtils jsniUtils;

	@Inject
	public MarkdownItImpl(SynapseJSNIUtils jsniUtils) {
		this.jsniUtils = jsniUtils;
	}

	@Override
	public String markdown2Html(String md, String uniqueSuffix) {
		String html = _markdown2Html(md, uniqueSuffix);
		return jsniUtils.sanitizeHtml(html);
	}

	private final static native String _markdown2Html(String md, String uniqueSuffix) /*-{
		try {
			if (!$wnd.md) {
				$wnd.md = $wnd.markdownit();
				$wnd.markdownitSynapse.init_markdown_it($wnd.md,
						$wnd.markdownitSub, $wnd.markdownitSup,
						$wnd.markdownitCentertext,
						$wnd.markdownitSynapseHeading,
						$wnd.markdownitSynapseTable,
						$wnd.markdownitStrikethroughAlt,
						$wnd.markdownitContainer, $wnd.markdownitEmphasisAlt,
						$wnd.markdownitInlineComments, $wnd.markdownitBr);

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
			}

			// load the plugin to recognize Synapse markdown widget syntax (with the uniqueSuffix parameter)
			$wnd.md.use($wnd.markdownitSynapse, uniqueSuffix).use(
					$wnd.markdownitMath, uniqueSuffix);
			var results = $wnd.md.render($wnd.markdownitSynapse
					.preprocessMarkdown(md));
			// Were footnotes found (and exported)?  If so, run the processor on the footnotes, and append to the results.
			var footnotes = $wnd.markdownitSynapse.footnotes();
			if (footnotes.length !== 0) {
				//reset footnote id and rerun on footnotes that were discovered in the first pass
				$wnd.markdownitSynapse.resetFootnotes();
				var footnotesHtml = $wnd.md.render(footnotes);
				results += '<hr>' + footnotesHtml;
			}

			return results;
		} catch (err) {
			console.error(err);
			return md;
		}
	}-*/;

}
