package org.sagebionetworks.web.client;

public class MarkdownItImpl implements MarkdownIt {
	@Override
	public String markdown2Html(String md, String uniqueSuffix) {
		return _markdown2Html(md, uniqueSuffix);
	}

	private final static native String _markdown2Html(String md,
			String uniqueSuffix) /*-{
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

		function initMarkdownTableStyle() {
			var defaultRender = $wnd.md.renderer.rules.table_open
					|| function(tokens, idx, options, env, self) {
						return self.renderToken(tokens, idx, options);
					};

			$wnd.md.renderer.rules.table_open = function(tokens, idx, options,
					env, self) {
				var aIndex = tokens[idx].attrIndex('class');
				if (aIndex < 0) {
					tokens[idx].attrPush([ 'class', 'markdowntable' ]); // add new attribute
				} else {
					tokens[idx].attrs[aIndex][1] += 'markdowntable'; // add value to existing attr
				}

				// pass token to default renderer.
				return defaultRender(tokens, idx, options, env, self);
			};
		}
		
		//TODO: remove extra style once we remove the old markdown processor, and just change the ".markdown p" css class instead
		function initParagraphStyle() {
			var defaultRender = $wnd.md.renderer.rules.paragraph_open
					|| function(tokens, idx, options, env, self) {
						return self.renderToken(tokens, idx, options);
					};

			$wnd.md.renderer.rules.paragraph_open = function(tokens, idx, options,
					env, self) {
				var aIndex = tokens[idx].attrIndex('style');
				if (aIndex < 0) {
					tokens[idx].attrPush([ 'style', 'margin: 10px 0 10px 0;' ]); // add new attribute
				} else {
					tokens[idx].attrs[aIndex][1] += ' margin: 10px 0 10px 0; '; // add value to existing attr
				}

				// pass token to default renderer.
				return defaultRender(tokens, idx, options, env, self);
			};
		}
		
		//TODO: remove extra style once we remove the old markdown processor, and just change the ".markdown heading" css classes instead
		function initSynapseHeadingStyle() {
			var defaultRender = $wnd.md.renderer.rules.synapse_heading_open
					|| function(tokens, idx, options, env, self) {
						return self.renderToken(tokens, idx, options);
					};

			
			$wnd.md.renderer.rules.synapse_heading_open = function(tokens, idx, options,
					env, self) {
				var level = tokens[idx].tag.substring(1);
				var paddingTop = 35 - ((level-1) * 6);  //map to 5-35 px top 
				var paddingBottom = 14 - (level * 2);  //map to 2-12
				var styleValue = 'padding-top: ' + paddingTop + 'px;' + ' padding-bottom: ' + paddingBottom + 'px;';
				var aIndex = tokens[idx].attrIndex('style');
				if (aIndex < 0) {
					tokens[idx].attrPush([ 'style', styleValue ]); // add new attribute
				} else {
					tokens[idx].attrs[aIndex][1] += styleValue; // add value to existing attr
				}

				// pass token to default renderer.
				return defaultRender(tokens, idx, options, env, self);
			};
		}

		function initLinkify() {
			$wnd.md.linkify.set({ fuzzyLink: false });
			
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
			
			//synapse (may have version or wiki page id)
			$wnd.md.linkify.add('syn', {
				validate : function(text, pos, self) {
					var tail = text.slice(pos);
					if (!self.re.synapse) {
						self.re.synapse = new RegExp(
								'^([0-9]{3,}[.]?[0-9]*(\\/wiki\\/[0-9]+)?)+(?!_)(?=$|'
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
			
			$wnd.md.linkify.add('doi:10.', {
				validate : function(text, pos, self) {
					var tail = text.slice(pos);
					if (!self.re.doi) {
						self.re.doi = new RegExp(
								'^[0-9]+[/]{1}[a-zA-Z0-9_.]+(?!_)(?=$|'
										+ self.re.src_ZPCc + ')');
					}
					if (self.re.doi.test(tail)) {
						return tail.match(self.re.doi)[0].length;
					}
					return 0;
				},
				normalize : function(match) {
					match.url = 'http://dx.doi.org/' + match.url;
				}
			});
		}

		function initREs() {
			if (!$wnd.md.utils.gridLayoutColumnParamRE) {
				$wnd.md.utils.gridLayoutColumnParamRE = new RegExp("^\\s*(width[=]{1})?\\s*(.*)[}]{1}\\s*$");
			}
			if (!$wnd.md.utils.synapseRE) {
				$wnd.md.utils.synapseRE = new RegExp('^syn([0-9]+[.]?[0-9]*)+');
			}
			if (!$wnd.md.utils.urlWithoutProtocolRE) {
				$wnd.md.utils.urlWithoutProtocolRE = new RegExp('^([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([\\/\w \\.-]*)*\\/?.*$');
			}
			if (!$wnd.md.utils.doiRE) {
				$wnd.md.utils.doiRE = new RegExp('^doi:10[.]{1}[0-9]+[/]{1}[a-zA-Z0-9_.]+$');
			}
			if (!$wnd.md.utils.ulMarkerRE) {
				$wnd.md.utils.ulMarkerRE = new RegExp("^\\s*[*-+>]{1}[^|]*$");
			}
			if (!$wnd.md.utils.olMarkerRE) {
				$wnd.md.utils.olMarkerRE = new RegExp("^\\s*\\d+\\s*[.)]{1}[^|]*$");
			}
			if (!$wnd.md.utils.spacesRE) {
				$wnd.md.utils.spacesRE = new RegExp("[ ]{7}", 'g');
			}
		}
		
		function preprocessMarkdown(md) {
			// add an extra newline after anything that looks like a list
			var splitMD = md.split('\n');
			md = '';
			var isPreviousLineInList = false;
			var isCurrentLineInList = false;
			for(var i = 0; i < splitMD.length; i++) {
				isCurrentLineInList = $wnd.md.utils.ulMarkerRE.test(splitMD[i]) || $wnd.md.utils.olMarkerRE.test(splitMD[i]);
				if (isCurrentLineInList) {
					// SWC-2988: and replace each group of 7 spaces with 4 (so that markdown-it list rule recognizes sublists).
					splitMD[i] = splitMD[i].replace($wnd.md.utils.spacesRE, '    ');
				}
				if (isPreviousLineInList && !isCurrentLineInList) {
					md += '\n';
				}
				md += splitMD[i] + '\n';
				isPreviousLineInList = isCurrentLineInList;
			}
			
			return md;
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
					//!!!!!!!!!!!!!!!!!! Changed for Synapse  !!!!!!!!!!!!!!!!!!!!!!!!!/
					var testString = res.str;
					if ($wnd.md.utils.synapseRE.test(testString)) {
						//this is a synapse ID
						res.str = '#!Synapse:'
								+ testString.replace(/[.]/, '/version/');
					} else if ($wnd.md.utils.urlWithoutProtocolRE.test(testString)) {
						res.str = 'http://' + testString;
					} else if ($wnd.md.utils.doiRE.test(testString)) {
						res.str = 'http://dx.doi.org/' + testString;
					}
					//!!!!!!!!!!!!!! End of change for Synapse  !!!!!!!!!!!!!!!!!!!!!!/
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
		
		//Define custom scanDelims that does not conclude that a token can open or close based on whitespace
		function scanDelims(src, posMax, start) {
		  var pos = start, count, can_open, can_close,
		      max = posMax,
		      marker = src.charCodeAt(start);
		  while (pos < max && src.charCodeAt(pos) === marker) { pos++; }
		  count = pos - start;
		  can_open  = true;
		  can_close = true;
		  return {
		    can_open:  can_open,
		    can_close: can_close,
		    length:    count
		  };
		};
		
		//Copy of markdown-it emphasis function, but uses the scanDelims function above instead of StateInline.prototype.scanDelims()
		function emphasis(state, silent) {
		  var i, scanned, token,
		      start = state.pos,
		      marker = state.src.charCodeAt(start);
		
		  if (silent) { return false; }
		
		  if (marker !== 0x5F && marker !== 0x2A ) { return false; } // '_' or '*'
		
		  scanned = scanDelims(state.src, state.posMax, state.pos);
		
		  for (i = 0; i < scanned.length; i++) {
		    token         = state.push('text', '', 0);
		    token.content = String.fromCharCode(marker);
		
		    state.delimiters.push({
		      // Char code of the starting marker (number).
		      //
		      marker: marker,
		
		      // An amount of characters before this one that's equivalent to
		      // current one. In plain English: if this delimiter does not open
		      // an emphasis, neither do previous `jump` characters.
		      //
		      // Used to skip sequences like "*****" in one step, for 1st asterisk
		      // value will be 0, for 2nd it's 1 and so on.
		      //
		      jump:   i,
		
		      // A position of the token this delimiter corresponds to.
		      //
		      token:  state.tokens.length - 1,
		
		      // Token level.
		      //
		      level:  state.level,
		
		      // If this delimiter is matched as a valid opener, `end` will be
		      // equal to its position, otherwise it's `-1`.
		      //
		      end:    -1,
		
		      // Boolean flags that determine if this delimiter could open or close
		      // an emphasis.
		      //
		      open:   scanned.can_open,
		      close:  scanned.can_close
		    });
		  }
		
		  state.pos += scanned.length;
		
		  return true;
		};

		function initMarkdownIt() {
			$wnd.md = $wnd.markdownit();
			$wnd.md.set({
				html : false,
				breaks : true,
				linkify : true,
				maxNesting : 100
			});
			$wnd.md.disable([ 'heading' ]);
			$wnd.md.disable([ 'lheading' ]);
			$wnd.md.use($wnd.markdownitSub)
				.use($wnd.markdownitSup)
				.use($wnd.markdownitCentertext)
				.use($wnd.markdownitSynapseHeading)
				.use($wnd.markdownitSynapseTable)
				.use($wnd.markdownitStrikethroughAlt);

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
			$wnd.md.use($wnd.markdownitContainer, 'row', 
				{
					marker: '{row}',
					minMarkerCount: 1,
					render: function (tokens, idx) {
						if (tokens[idx].nesting === 1) {
							// opening tag
							return '<div class="container-fluid"><div class="row">';
						} else {
							// closing tag
							return '</div></div>\n';
						}
					},
					validate: function(params) {
						return true;
					}
				});
			$wnd.md.use($wnd.markdownitContainer, 'column', 
				{
					marker: '{column',
					endMarker: '{column}',
					minMarkerCount: 1,
					render: function (tokens, idx) {
						if (tokens[idx].nesting === 1) {
							// opening tag
							var m = $wnd.md.utils.gridLayoutColumnParamRE.exec(tokens[idx].info);
							return '<div class="col-sm-' + $wnd.md.utils.escapeHtml(m[2]) + '">';
						} else {
							// closing tag
							return '</div>\n';
						}
					},
					validate: function(params) {
						return $wnd.md.utils.gridLayoutColumnParamRE.test(params);
					}
				});
			sendLinksToNewWindow();
			initLinkify();
			initMarkdownTableStyle();
			//TODO: remove special paragraph renderer after release
			initParagraphStyle();
			initSynapseHeadingStyle();
			initREs();
			$wnd.md.inline.ruler.at('link', link);
			$wnd.md.inline.ruler.at('emphasis', emphasis);
		}

		if (!$wnd.md) {
			initMarkdownIt();
		}
		// load the plugin to recognize Synapse markdown widget syntax (with the uniqueSuffix parameter)
		$wnd.md.use($wnd.markdownitSynapse, uniqueSuffix)
			.use($wnd.markdownitMath, uniqueSuffix)
			.use($wnd.markdownitEmphasisAlt);
		var results = $wnd.md.render(preprocessMarkdown(md));
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
