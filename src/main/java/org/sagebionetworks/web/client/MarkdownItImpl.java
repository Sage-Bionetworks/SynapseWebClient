package org.sagebionetworks.web.client;


public class MarkdownItImpl implements MarkdownIt {
	@Override
	public String markdown2Html(String md, String uniqueSuffix) {
		return _markdown2Html(md, uniqueSuffix);
	}
	
	private final static native String _markdown2Html(String md, String uniqueSuffix) /*-{
		function sendLinksToNewWindow() {
			var defaultRender = $wnd.markdownitSingleton.renderer.rules.link_open || function(tokens, idx, options, env, self) {
			  return self.renderToken(tokens, idx, options);
			};
			
			$wnd.markdownitSingleton.renderer.rules.link_open = function (tokens, idx, options, env, self) {
			  // If you are sure other plugins can't add `target` - drop check below
			  var aIndex = tokens[idx].attrIndex('target');
			  var hrefIndex = tokens[idx].attrIndex('href');
			  if (aIndex < 0) {
			  	if (hrefIndex < 0 || !tokens[idx].attrs[hrefIndex][1].startsWith('#!')) {
			  		tokens[idx].attrPush(['target', '_blank']); // add new attribute
			  	}
			  } else {
			    tokens[idx].attrs[aIndex][1] = '_blank';    // replace value of existing attr
			  }
			
			  // pass token to default renderer.
			  return defaultRender(tokens, idx, options, env, self);
			};
		}
		
		function initLinkify() {
			$wnd.markdownitSingleton.linkify.add('@', {
			  validate: function (text, pos, self) {
			    var tail = text.slice(pos);
			    if (!self.re.username) {
			      self.re.username =  new RegExp(
			        '^([a-zA-Z0-9_]){1,15}(?!_)(?=$|' + self.re.src_ZPCc + ')'
			      );
			    }
			    if (self.re.username.test(tail)) {
			      // Linkifier allows punctuation chars before prefix,
			      // but we additionally disable `@` ("@@mention" is invalid)
			      if (pos >= 2 && tail[pos - 2] === '@') {
			        return false;
			      }
			      return tail.match(self.re.username)[0].length;
			    }
			    return 0;
			  },
			  normalize: function (match) {
			    match.url = '#!Profile:' + match.url.replace(/^@/, '');
			  }
			});
			
			$wnd.markdownitSingleton.linkify.add('syn', {
			  validate: function (text, pos, self) {
			    var tail = text.slice(pos);
			    if (!self.re.synapse) {
			      self.re.synapse =  new RegExp(
			        '^([0-9]+[.]?[0-9]*)+(?!_)(?=$|' + self.re.src_ZPCc + ')'
			      );
			    }
			    if (self.re.synapse.test(tail)) {
			      return tail.match(self.re.synapse)[0].length;
			    }
			    return 0;
			  },
			  normalize: function (match) {
			    match.url = '#!Synapse:' + match.url.replace(/[.]/, '/version/');
			  }
			});
		}
		
		function initMarkdownIt() {
			$wnd.markdownitSingleton = $wnd.markdownit()
				.set({ 
					html: false, 
					breaks: true,
					linkify: true,
					maxNesting: 100 });
			$wnd.markdownitSingleton.disable([ 'heading' ]);
			$wnd.markdownitSingleton
				.use($wnd.markdownitSub)
				.use($wnd.markdownitSup)
				.use($wnd.markdownitCentertext)
				.use($wnd.markdownitSynapseHeading)
				;
			
			$wnd.markdownitSingleton
				.set({
				  highlight: function (str, lang) {
				    if (lang && $wnd.hljs.getLanguage(lang)) {
				      try {
				      	return $wnd.hljs.highlight(lang, str).value;
				      } catch (__) {}
				    }
				    return ''; // use external default escaping
				  }
				});
			sendLinksToNewWindow();
			initLinkify();
		}
		
		if (!$wnd.markdownitSingleton) {
			initMarkdownIt();
		}
		//load the plugin to recognize Synapse markdown widget syntax (with the uniqueSuffix parameter)
		$wnd.markdownitSingleton
			.use($wnd.markdownitSynapse, uniqueSuffix)
			.use($wnd.markdownitMath, uniqueSuffix);
		
		return $wnd.markdownitSingleton.render(md);
	}-*/;
	
}
