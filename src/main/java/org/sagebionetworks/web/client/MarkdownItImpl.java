package org.sagebionetworks.web.client;


public class MarkdownItImpl implements MarkdownIt {
	@Override
	public String markdown2Html(String md, String uniqueSuffix) {
		return _markdown2Html(md, uniqueSuffix);
	}
	
	private final static native String _markdown2Html(String md, String uniqueSuffix) /*-{
		function sendLinksToNewWindow() {
			var defaultRender = $wnd.md.renderer.rules.link_open
					|| function(tokens, idx, options, env, self) {
						return self.renderToken(tokens, idx, options);
					};

			$wnd.md.renderer.rules.link_open = function(tokens, idx, options,
					env, self) {
				// If you are sure other plugins can't add `target` - drop check below
				var aIndex = tokens[idx].attrIndex('target');
				var hrefIndex = tokens[idx].attrIndex('href');
				if (aIndex < 0) {
					if (hrefIndex < 0
							|| !tokens[idx].attrs[hrefIndex][1]
									.startsWith('#!')) {
						tokens[idx].attrPush([ 'target', '_blank' ]); // add new attribute
					}
				} else {
					tokens[idx].attrs[aIndex][1] = '_blank'; // replace value of existing attr
				}

				// pass token to default renderer.
				return defaultRender(tokens, idx, options, env, self);
			};
		}

		function initLinkify() {
			$wnd.md.linkify.add('@', {
				validate : function(text, pos, self) {
					var tail = text.slice(pos);
					if (!self.re.username) {
						self.re.username = new RegExp(
								'^([a-zA-Z0-9_]){1,15}(?!_)(?=$|'
										+ self.re.src_ZPCc + ')');
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
				normalize : function(match) {
					match.url = '#!Profile:' + match.url.replace(/^@/, '');
				}
			});

			$wnd.md.linkify.add('syn', {
				validate : function(text, pos, self) {
					var tail = text.slice(pos);
					if (!self.re.synapse) {
						self.re.synapse = new RegExp(
								'^([0-9]+[.]?[0-9]*)+(?!_)(?=$|'
										+ self.re.src_ZPCc + ')');
					}
					if (self.re.synapse.test(tail)) {
						return tail.match(self.re.synapse)[0].length;
					}
					return 0;
				},
				normalize : function(match) {
					match.url = '#!Synapse:'
							+ match.url.replace(/[.]/, '/version/');
				}
			});
		}

		function initREs() {
			if (!$wnd.md.utils.synapseRE) {
				$wnd.md.utils.synapseRE = new RegExp('^syn([0-9]+[.]?[0-9]*)+');
			}
			if (!$wnd.md.utils.wwwRE) {
				$wnd.md.utils.wwwRE = new RegExp('^www[.]{1}[a-zA-Z0-9_]+');
			}
			
			if (!$wnd.md.utils.usernameRE) {
				$wnd.md.utils.usernameRE = new RegExp('^@([a-zA-Z0-9_]){1,150}');
			}
		}

		function link(state, silent) {
			var attrs, code, label, labelEnd, labelStart, pos, res, ref, title, token, href = '', oldPos = state.pos, max = state.posMax, start = state.pos, parseLinkLabel = $wnd.md.helpers.parseLinkLabel, parseLinkDestination = $wnd.md.helpers.parseLinkDestination, parseLinkTitle = $wnd.md.helpers.parseLinkTitle, isSpace = $wnd.md.utils.isSpace, normalizeReference = $wnd.md.utils.normalizeReference;

			if (state.src.charCodeAt(state.pos) !== 0x5B) {
				return false;
			} // [
			labelStart = state.pos + 1;
			labelEnd = parseLinkLabel(state, state.pos, true);

			// parser failed to find ']', so it's not a valid link
			if (labelEnd < 0) {
				return false;
			}

			pos = labelEnd + 1;
			if (pos < max && state.src.charCodeAt(pos) === 0x28) { // (
				//
				// Inline link
				//

				// [link](  <href>  "title"  )
				//        ^^ skipping these spaces
				pos++;
				for (; pos < max; pos++) {
					code = state.src.charCodeAt(pos);
					if (!isSpace(code) && code !== 0x0A) {
						break;
					}
				}
				if (pos >= max) {
					return false;
				}

				// [link](  <href>  "title"  )
				//          ^^^^^^ parsing link destination
				start = pos;
				res = parseLinkDestination(state.src, pos, state.posMax);
				if (res.ok) {
					var testString = res.str;
					if ($wnd.md.utils.synapseRE.test(testString)) {
						//this is a synapse ID
						res.str = '#!Synapse:' + testString.replace(/[.]/, '/version/');
					} else if ($wnd.md.utils.wwwRE.test(testString)) {
						res.str = 'http://' + testString;
					}
					href = state.md.normalizeLink(res.str);
					if (state.md.validateLink(href)) {
						pos = res.pos;
					} else {
						href = '';
					}
				}

				// [link](  <href>  "title"  )
				//                ^^ skipping these spaces
				start = pos;
				for (; pos < max; pos++) {
					code = state.src.charCodeAt(pos);
					if (!isSpace(code) && code !== 0x0A) {
						break;
					}
				}

				// [link](  <href>  "title"  )
				//                  ^^^^^^^ parsing link title
				res = parseLinkTitle(state.src, pos, state.posMax);
				if (pos < max && start !== pos && res.ok) {
					title = res.str;
					pos = res.pos;

					// [link](  <href>  "title"  )
					//                         ^^ skipping these spaces
					for (; pos < max; pos++) {
						code = state.src.charCodeAt(pos);
						if (!isSpace(code) && code !== 0x0A) {
							break;
						}
					}
				} else {
					title = '';
				}

				if (pos >= max || state.src.charCodeAt(pos) !== 0x29) { // )
					state.pos = oldPos;
					return false;
				}
				pos++;
			} else {
				//
				// Link reference
				//
				if (typeof state.env.references === 'undefined') {
					return false;
				}

				if (pos < max && state.src.charCodeAt(pos) === 0x5B) { // [
					start = pos + 1;
					pos = parseLinkLabel(state, pos);
					if (pos >= 0) {
						label = state.src.slice(start, pos++);
					} else {
						pos = labelEnd + 1;
					}
				} else {
					pos = labelEnd + 1;
				}

				// covers label === '' and label === undefined
				// (collapsed reference link and shortcut reference link respectively)
				if (!label) {
					label = state.src.slice(labelStart, labelEnd);
				}

				ref = state.env.references[normalizeReference(label)];
				if (!ref) {
					state.pos = oldPos;
					return false;
				}
				href = ref.href;
				title = ref.title;
			}

			//
			// We found the end of the link, and know for a fact it's a valid link;
			// so all that's left to do is to call tokenizer.
			//
			if (!silent) {
				state.pos = labelStart;
				state.posMax = labelEnd;

				token = state.push('link_open', 'a', 1);
				token.attrs = attrs = [ [ 'href', href ] ];
				if (title) {
					attrs.push([ 'title', title ]);
				}

				state.md.inline.tokenize(state);

				token = state.push('link_close', 'a', -1);
			}

			state.pos = pos;
			state.posMax = max;
			return true;
		};

		function initMarkdownIt() {
			$wnd.md = $wnd.markdownit().set({
				html : false,
				breaks : true,
				linkify : true,
				maxNesting : 100
			});
			$wnd.md.disable([ 'heading' ]);
			$wnd.md.use($wnd.markdownitSub).use($wnd.markdownitSup).use(
					$wnd.markdownitCentertext).use(
					$wnd.markdownitSynapseHeading);

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
			sendLinksToNewWindow();
			initLinkify();
			initREs();
			$wnd.md.inline.ruler.at('link', link);
		}

		if (!$wnd.md) {
			initMarkdownIt();
		}
		//load the plugin to recognize Synapse markdown widget syntax (with the uniqueSuffix parameter)
		$wnd.md.use($wnd.markdownitSynapse, uniqueSuffix).use(
				$wnd.markdownitMath, uniqueSuffix);

		return $wnd.md.render(md);
	}-*/;
	
}
