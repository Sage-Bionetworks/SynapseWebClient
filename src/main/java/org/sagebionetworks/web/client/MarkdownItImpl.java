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
			if (!$wnd.md.utils.urlWithoutProtocolRE) {
				$wnd.md.utils.urlWithoutProtocolRE = new RegExp('^(www\.)?[-a-zA-Z0-9@:%._\+~#=]{2,256}\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_\+.~#?&//=]*)');
			}
			if (!$wnd.md.utils.tableClassStartRE) {
				$wnd.md.utils.tableClassStartRE = new RegExp('^\\s*{[|]{1}\\s+class\\s*=\\s*"\\s*(.*)"\\s*');
			}
			if (!$wnd.md.utils.tableClassEndRE) {
				$wnd.md.utils.tableClassEndRE = new RegExp('^\\s*[|]{1}}\\s*');
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
					//!!!!!!!!!!!!!!!!!! Changed for Synapse  !!!!!!!!!!!!!!!!!!!!!!!!!/
					var testString = res.str;
					if ($wnd.md.utils.synapseRE.test(testString)) {
						//this is a synapse ID
						res.str = '#!Synapse:'
								+ testString.replace(/[.]/, '/version/');
					} else if ($wnd.md.utils.urlWithoutProtocolRE.test(testString)) {
						res.str = 'http://' + testString;
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
		}
		;

		function getLine(state, line) {
			var pos = state.bMarks[line] + state.blkIndent, max = state.eMarks[line];

			return state.src.substr(pos, max - pos);
		}

		function escapedSplit(str) {
			var result = [], pos = 0, max = str.length, ch, escapes = 0, lastPos = 0, backTicked = false, lastBackTick = 0;

			ch = str.charCodeAt(pos);

			while (pos < max) {
				if (ch === 0x60 && (escapes % 2 === 0)) { // `
					backTicked = !backTicked;
					lastBackTick = pos;
				} else if (ch === 0x7c && (escapes % 2 === 0) && !backTicked) { // |
					result.push(str.substring(lastPos, pos));
					lastPos = pos + 1;
				} else if (ch === 0x5c) { // \
					escapes++;
				} else {
					escapes = 0;
				}

				pos++;

				// If there was an un-closed backtick, go back to just after
				// the last backtick, but as if it was a normal character
				if (pos === max && backTicked) {
					backTicked = false;
					pos = lastBackTick + 1;
				}

				ch = str.charCodeAt(pos);
			}

			result.push(str.substring(lastPos));

			return result;
		}
		function table(state, startLine, endLine, silent) {
			var ch, lineText, pos, i, nextLine, columns, columnCount, token, t, tableLines, tbodyLines, classNames, tableBodyStartLine, headerLine;
			// should have at least two lines (!!! Synapse change, used to be 3 due to required ---|---|--- line).  Header and single row.
			if (startLine + 1 > endLine) {
				return false;
			}

			pos = state.bMarks[startLine] + state.tShift[startLine];
			if (pos >= state.eMarks[startLine]) {
				return false;
			}
			ch = state.src.charCodeAt(pos);

			lineText = getLine(state, startLine);
			//look for optional class definition start, like '{| class="border"'
			if ($wnd.md.utils.tableClassStartRE.test(lineText)) {
				//this table definition includes class names, so the start marker is {| and end marker will be |} 
				classNames = lineText.match($wnd.md.utils.tableClassStartRE)[1];
				headerLine = startLine + 1;
			} else {
				headerLine = startLine;
			}

			if (state.sCount[headerLine] < state.blkIndent) {
				return false;
			}
			
			pos = state.bMarks[headerLine] + state.tShift[headerLine];
			if (pos >= state.eMarks[headerLine]) {
				return false;
			}
			
			//read column headers
			lineText = getLine(state, headerLine).trim();
			if (lineText.indexOf('|') === -1) {
				return false;
			}
			columns = escapedSplit(lineText.replace(/^\||\|$/g, ''));

			// header row will define an amount of columns in the entire table,
			// and align row shouldn't be smaller than that (the rest of the rows can)
			columnCount = columns.length;
			
			if (silent) {
				return true;
			}

			token = state.push('table_open', 'table', 1);
			token.map = tableLines = [ startLine, 0 ];
			if (classNames) {
				token.attrs = [ [ 'class', ' ' + classNames + ' ' ] ];
				//start line of the table (header) is really the second line.
				startLine++;
			}

			token = state.push('thead_open', 'thead', 1);
			token.map = [ startLine, startLine + 1 ];

			token = state.push('tr_open', 'tr', 1);
			token.map = [ startLine, startLine + 1 ];

			for (i = 0; i < columns.length; i++) {
				token = state.push('th_open', 'th', 1);
				token.map = [ startLine, startLine + 1 ];
				
				token = state.push('inline', '', 0);
				token.content = columns[i].trim();
				token.map = [ startLine, startLine + 1 ];
				token.children = [];

				token = state.push('th_close', 'th', -1);
			}

			token = state.push('tr_close', 'tr', -1);
			token = state.push('thead_close', 'thead', -1);

			nextLine = headerLine + 1;
			lineText = getLine(state, nextLine).trim();
			
			//If this line is of the form ---|---|---, then we need to skip.  Else, it starts here
			if (/^[-:| ]+$/.test(lineText)) {
				tableBodyStartLine = headerLine + 2;
			} else {
				tableBodyStartLine = headerLine + 1;
			}
			token = state.push('tbody_open', 'tbody', 1);
			token.map = tbodyLines = [ tableBodyStartLine, 0 ];
			
			for (nextLine = tableBodyStartLine; nextLine < endLine; nextLine++) {
				if (state.sCount[nextLine] < state.blkIndent) {
					break;
				}

				lineText = getLine(state, nextLine).trim();
				if ($wnd.md.utils.tableClassEndRE.test(lineText)) {
					//end of table with class definitions. Include this line in the table definition
					nextLine++;
					break;
				}
				if (lineText.indexOf('|') === -1) {
					break;
				}
				columns = escapedSplit(lineText.replace(/^\||\|$/g, ''));

				token = state.push('tr_open', 'tr', 1);
				for (i = 0; i < columnCount; i++) {
					token = state.push('td_open', 'td', 1);
					
					token = state.push('inline', '', 0);
					token.content = columns[i] ? columns[i].trim() : '';
					token.children = [];

					token = state.push('td_close', 'td', -1);
				}
				token = state.push('tr_close', 'tr', -1);
			}
			token = state.push('tbody_close', 'tbody', -1);
			token = state.push('table_close', 'table', -1);

			tableLines[1] = tbodyLines[1] = nextLine;
			state.line = nextLine;
			return true;
		}
		;

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
			initMarkdownTableStyle();
			initREs();
			$wnd.md.inline.ruler.at('link', link);
			$wnd.md.block.ruler.at('table', table);
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
