/*! markdown-it-math 3.0.2 https://github.com/runarberg/markdown-it-math @license MIT */
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.markdownitMath = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
'use strict';
var prefix = 'mathjax-';
var divIndex = 0;
function inlineParser(state, silent) {
  var content,
      token,
      found = false,
      max = state.posMax,
      start = state.pos,
      open = '$$',
      close = '$$';

  if (state.src.slice(state.pos, state.pos + open.length) !== open) {
    return false;
  }
  if (silent) {
    return false;
  } // don't run any pairs in validation mode
  if (start + open.length >= max) {
    return false;
  }

  state.pos = start + open.length;

  while (state.pos < max) {
    if (state.src.slice(state.pos, state.pos + close.length) === close) {
      found = true;
      break;
    }
    state.md.inline.skipToken(state);
  }

  if (!found) {
    state.pos = start;
    return false;
  }

  content = state.src.slice(start + open.length, state.pos);

  if (content.indexOf('\n') > -1) {
    state.pos = start;
    return false;
  }

  // found!
  state.posMax = state.pos;
  state.pos = start + open.length;

  // Earlier we checked !silent, but this implementation does not need it
  token         = state.push('math_inline', 'math', 0);
  token.content = content;
  state.pos = state.posMax + close.length;
  state.posMax = max;
  return true;
}

function blockParser(state, startLine, endLine, silent) {
  var params, nextLine, token,
      haveEndMarker = false,
      pos = state.bMarks[startLine] + state.tShift[startLine],
      open = '$$\n',
      close = '$$\n';

  /* need enough room for full sequence:
  $$                startLine
  \begin{aligned}
  \end{aligned}
  $$                endLine = startLine + 3
  */
  if (endLine <= startLine + 3 || state.src.slice(pos, pos + open.length) !== open) {
    return false;
  }

  // Since start is found, we can report success here in validation mode
  if (silent) { return true; }

  // search end of block
  nextLine = startLine;

  for (;;) {
    if (haveEndMarker) {
      break;
    }

    nextLine++;

    if (nextLine > endLine) {
      // unclosed block is not valid
      return false;
    }

    pos = state.bMarks[nextLine] + state.tShift[nextLine];

    // on last line, pattern may end without newline
    if (nextLine === endLine) {
      // last line is the line after what would be the ending '$$' or close
      if (state.src.slice(pos - close.trim().length, pos) === close.trim()) {
        // if this is a valid ending, do not include the ending line, which would
        // only contain $$
        nextLine--;
        haveEndMarker = true;
      } else {
        // isn't closed by the last line
        return false;
      }
    } else if (state.src.slice(pos, pos + close.length) !== close) {
      // max + 1 to grab newline character
      continue;
    }

    pos += close.length;

    // make sure tail has spaces only
    pos = state.skipSpaces(pos);

    // found!
    haveEndMarker = true;
  }

  state.line = nextLine + 1;
  token = state.push('math_block', 'math', 0);
  token.block = true;
  token.content = state.getLines(startLine + 1, nextLine, 0, true);
  token.info = params;
  token.markup = open;
  return true;
}

function makeRenderer(suffix) {
  return function(tokens, idx) {
    return '<span id="' + prefix + divIndex++ + suffix +
     '" class="math">' + tokens[idx].content + '</span>';
  };
}


module.exports = function math_plugin(md, suffix) {
  divIndex = 0;
  // Default options
  md.inline.ruler.before('escape', 'math_inline', inlineParser);
  md.block.ruler.after('blockquote', 'math_block', blockParser, {
    alt: [ 'paragraph', 'reference', 'blockquote', 'list' ]
  });
  var renderer = makeRenderer(suffix);
  md.renderer.rules.math_inline = renderer;
  md.renderer.rules.math_block = renderer;
};

},{}]},{},[1])(1)
});
