/**
 * markdown-it and the plugins used by MarkdownItImpl (GWT JSNI), exposed on the global scope.
 */
import markdownit from 'markdown-it'
import markdownitBr from 'markdown-it-br'
import markdownitCentertext from 'markdown-it-center-text'
import markdownitContainer from '@sage-bionetworks/markdown-it-container'
import markdownitInlineComments from 'markdown-it-inline-comments'
import markdownitStrikethroughAlt from 'markdown-it-strikethrough-alt'
import markdownitSub from 'markdown-it-sub-alt'
import markdownitSup from 'markdown-it-sup-alt'
import markdownitSynapse, {
  footnotes,
  init_markdown_it,
  preprocessMarkdown,
  resetFootnotes,
} from 'markdown-it-synapse'
import markdownitSynapseHeading from 'markdown-it-synapse-heading'
import markdownitMath from 'markdown-it-synapse-math'
import markdownitSynapseTable from 'markdown-it-synapse-table'

window.markdownit = markdownit
window.markdownitBr = markdownitBr
window.markdownitCentertext = markdownitCentertext
window.markdownitContainer = markdownitContainer
window.markdownitInlineComments = markdownitInlineComments
window.markdownitStrikethroughAlt = markdownitStrikethroughAlt
window.markdownitSub = markdownitSub
window.markdownitSup = markdownitSup
// MarkdownItImpl uses markdownitSynapse both as a markdown-it plugin and as a namespace for its helpers
window.markdownitSynapse = Object.assign(markdownitSynapse, {
  footnotes,
  init_markdown_it,
  preprocessMarkdown,
  resetFootnotes,
})
window.markdownitSynapseHeading = markdownitSynapseHeading
window.markdownitMath = markdownitMath
window.markdownitSynapseTable = markdownitSynapseTable
