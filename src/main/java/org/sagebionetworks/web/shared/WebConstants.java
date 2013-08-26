package org.sagebionetworks.web.shared;

import org.sagebionetworks.repo.model.util.ModelConstants;


/**
 * Constants for query parameter keys, header names, and field names used by the
 * web components.
 * 
 * All query parameter keys should be in this file as opposed to being defined
 * in individual controllers. The reason for this to is help ensure consistency
 * across controllers.
 * 
 * @author bkng, deflaux
 */
public class WebConstants {
	
	/**
	 * Regex defining a valid entity name. Characters are selected to ensure
	 * compatibility across services and clients.
	 * 
	 */
	public static final String VALID_ENTITY_NAME_REGEX = ModelConstants.VALID_ENTITY_NAME_REGEX;
	
	public static final String INVALID_ENTITY_NAME_MESSAGE = "Entity names may only contain letters, numbers, spaces, underscores, hypens, periods, plus signs, and parentheses.";
	
	public static final String INVALID_EMAIL_MESSAGE = "Invalid email address";

	public static final String PROVENANCE_API_URL = "https://sagebionetworks.jira.com/wiki/display/PLFM/Analysis+Provenance+in+Synapse";
	
	/**
	 * Regex defining a valid annotation name. Characters are selected to ensure
	 * compatibility across services and clients.
	 * 
	 */
	public static final String VALID_ANNOTATION_NAME_REGEX = "^[a-z,A-Z,0-9,_,.]+";
	public static final String VALID_URL_REGEX = "^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	public static final String VALID_EMAIL_REGEX = "^[_A-Za-z0-9-+.]+@[_A-Za-z0-9.]+";
	public static final String WIDGET_NAME_REGEX = "[a-z,A-Z,0-9,., ,\\-,\\+,(,)]";
	public static final String VALID_WIDGET_NAME_REGEX = "^"+WIDGET_NAME_REGEX+"+";
	public static final String VALID_ENTITY_ID_REGEX = "^[Ss]{1}[Yy]{1}[Nn]{1}\\d+";
	public static final String VALID_POSITIVE_NUMBER_REGEX = "^[0-9]+";
	public static final String VALID_BOOKMARK_ID_REGEX = "[_A-Za-z0-9-.[^\\s]]+";
	
	public static final String URL_PROTOCOL = "http://";
	
	// OpenID related constants

	public static final String OPEN_ID_URI = "/Portal/openid";

	public static final String OPEN_ID_PROVIDER = "OPEN_ID_PROVIDER";
	// 		e.g. https://www.google.com/accounts/o8/id
	
	// this is the parameter name for the value of the final redirect
	public static final String RETURN_TO_URL_PARAM = "RETURN_TO_URL";
	
	// a parameter to control how the final redirect is issued
	public static final String OPEN_ID_MODE = "MODE";
	// redirect "GWT" style with the session token appended ot the URL with ":"
	public static final String OPEN_ID_MODE_GWT = "GWT";
	// redirect with the sessio token as a request parameter (this is the default)
	public static final String OPEN_ID_MODE_STANDARD = "STANDARD";

	public static final String ENTITY_DESCRIPTION_FORMATTING_TIPS_HTML = "<div style=\"margin-left:20px\"><br><br>" +
			"<h3>Phrase Emphasis</h3><pre><code>*italic*   **bold**<br>_italic_   __bold__<br></code></pre><br>" +
			"<h3>Links</h3><pre><code>http://sagebase.org - automatic!</code></pre><pre><code>syn12345 - automatic!</code></pre><pre><code>An [example](http://url.com/)</code></pre><pre><code>An [example][id]. Then, anywhere else in the description,<br>define the link:<br>  [id]: http://example.com/<br></code></pre><pre><code>Custom Synapse ID link text:<br>[my text](#Synapse:syn12345)</code></pre><br>" +
			"<h3>Tables</h3><pre><code>Row 1 Content Cell 1 | Row 1 Content Cell 2  | Row 1 Content Cell 3<br>Row 2 Content Cell 1  | Row 2 Content Cell 2  | Row 2 Content Cell 3</code></pre><br>" +
			"<h3>Images</h3><pre><code>![alt text](http://path/to/img.jpg)</code></pre><br>" +
			"<h3>Headers</h3><p><pre><code># Header 1<br>## Header 2<br>###### Header 6<br></code></pre></p><p>Exclude a header from the table of contents:<pre><code>#! Header 1 <br>##! Header 2<br>######! Header 6</code></pre></p><br>" +
			"<h3>Lists</h3><p>Ordered, without paragraphs:<pre><code>1.  List item one<br>2.  List item two<br></code></pre></p><p>Unordered, with paragraphs:<pre><code>*   A list item.<br>    With multiple paragraphs.<br>*   Another list item<br></code></pre></p><p>You can nest them:<pre><code>*   Abacus<br>    * answer<br>*   Bubbles<br>    1.  bunk<br>    2.  bupkis<br>        * BELITTLER<br>    3. burper<br>*   Cunning<br></code></pre></p><br>" +
			"<h3>Blockquotes</h3><pre><code>&gt; Email-style angle brackets<br>&gt; are used for blockquotes.<br>&gt; &gt; And, they can be nested.<br>&gt; #### Headers in blockquotes<br>&gt; <br>&gt; * You can quote a list.<br>&gt; * Etc.<br></code></pre><br>" +
			"<h3>Inline Code</h3><pre><code>Wrap inline snippets of `code` with backticks.<br>You can include literal backticks<br>like \\`this\\`.<br></code></pre><br>" +
			"<h3>Preformatted Code Blocks</h3><pre><code>Wrap your code blocks in ```<br><br>This is a normal paragraph.<br><br>```<br>This is a preformatted<br>code block.<br>```</code></pre><br>" +
			"<h3>Symbols</h3><pre><code>&amp;copy; = copyright sign<br>&amp;mdash; = wide dash<br>&amp;amp; = ampersand<br>&amp;trade; = trademark TM<br>&amp;reg; = reserved mark R</code></pre><br>"+
			"</div>";

	public static final String SYNAPSE_MARKDOWN_FORMATTING_TIPS_HTML = "<div style=\"margin-left:20px\"><br><br>" +
			"<h3>Phrase Emphasis</h3><pre><code>*italic*   **bold**<br>_italic_   __bold__<br>--strike out--<br></code></pre><br>" +
			"<h3>Subscript/Superscript</h3><pre><code>~subscript~  ^superscript^<br></code></pre><br>" +
			"<h3>Links</h3><pre><code>http://sagebase.org - automatic!</code></pre><pre><code>syn12345 - automatic!</code></pre><pre><code>An [example](http://url.com/)</code></pre><pre><code>Custom Synapse ID link text:<br>[my text](#Synapse:syn12345)</code></pre></pre><pre><code>Bookmarks within page<br>To create bookmark target: ${bookmarktarget?bookmarkID=myid}<br>To link to bookmark: [my text](#Bookmark:myid)</code></pre><br>" +
			"<h3>Tables</h3><pre><code>Row 1 Content Cell 1 | Row 1 Content Cell 2  | Row 1 Content Cell 3<br>Row 2 Content Cell 1  | Row 2 Content Cell 2  | Row 2 Content Cell 3</code></pre><pre><code>Table styles:<br>short (for tables with significant number of rows)<br>text-align-center<br>text-align-right<br>border</pre></code><pre><code>To apply styles:<br>{| class=\"border text-align-center\"<br>Row 1 Content Cell 1 | Row 1 Content Cell 2  | Row 1 Content Cell 3<br>|}</pre></code><br>" +
			"<h3>Images</h3><pre><code>![alt text](http://path/to/img.jpg)</code></pre><br>" +
			"<h3>Headers</h3><p><pre><code># Header 1<br>## Header 2<br>###### Header 6<br></code></pre></p><p>Exclude a header from the table of contents:<pre><code>#! Header 1 <br>##! Header 2<br>######! Header 6</code></pre></p><br>" +
			"<h3>Lists</h3><p>Ordered, without paragraphs:<pre><code>1.  List item one<br>2.  List item two<br></code></pre></p><p>Unordered, with paragraphs:<pre><code>*   A list item.<br>    With multiple paragraphs.<br>*   Another list item<br></code></pre></p><p>You can nest them:<pre><code>*   Abacus<br>    * answer<br>*   Bubbles<br>    1.  bunk<br>    2.  bupkis<br>        * BELITTLER<br>    3. burper<br>*   Cunning<br></code></pre></p><br>" +
			"<h3>Blockquotes</h3><pre><code>&gt; Email-style angle brackets<br>&gt; are used for blockquotes.<br>&gt; &gt; And, they can be nested.<br>&gt; #### Headers in blockquotes<br>&gt; <br>&gt; * You can quote a list.<br>&gt; * Etc.<br></code></pre><br>" +
			"<h3>Inline Code</h3><pre><code>Wrap inline snippets of `code` with backticks.<br>You can include literal backticks<br>like \\`this\\`.<br></code></pre><br>" +
			"<h3>Fenced Code Blocks</h3><pre><code>Wrap your code blocks in ```<br><br>This is a normal paragraph.<br><br>```<br>This is a preformatted<br>code block.<br>```</code></pre><pre><code>To help syntax highlighting, you can add an optional language identifier<br><br>```r<br>library(synapseClient)<br>synapseLogin('usename','password')<br>syn1686521 <- getEntity('syn1686521')<br>```</code></pre><br>" +
			"<h3>Symbols</h3><pre><code>&amp;copy; = copyright sign<br>&amp;mdash; = wide dash<br>&amp;amp; = ampersand<br>&amp;trade; = trademark TM<br>&amp;reg; = reserved mark R</code></pre><br>"+
			"</div>";

	
	/*
	 * Dimensions
	 */
	public static final int DEFAULT_GRID_COLUMN_WIDTH_PX = 150;
	public static final int DEFAULT_GRID_LAYER_COLUMN_WIDTH_PX = 100;
	public static final int DEFAULT_GRID_DATE_COLUMN_WIDTH_PX = 85;

	public static final int MAX_COLUMNS_IN_GRID = 100;
	public static final int DESCRIPTION_SUMMARY_LENGTH = 450; // characters for summary
	public static final String DIV_ID_PREVIEW_SUFFIX = "_preview";

	public static final String DIV_ID_WIDGET_PREFIX = "widget_";
	
	public static final String DIV_ID_MATHJAX_PREFIX = "mathjax-";
    public static final String DIV_ID_LINK_PREFIX = "link-";
	public static final String DIV_ID_AUTOLINK_PREFIX = "autolink-";
	public static final String DIV_ID_IMAGE_PREFIX = "image-";
	public static final String FOOTNOTE_ID_WIDGET_PREFIX = "wikiFootnote";
	
	public static final String REFERENCE_ID_WIDGET_PREFIX = "wikiReference";

	public static final String PROXY_PARAM_KEY = "proxy";

	public static final String ENTITY_PARENT_ID_KEY = "parentId";

	public static final String ENTITY_EULA_ID_KEY = "eulaId";

	public static final String ENTITY_PARAM_KEY = "entityId";

	public static final String ENTITY_VERSION_PARAM_KEY = "version";

	public static final String WIKI_OWNER_ID_PARAM_KEY = "ownerId";

	public static final String WIKI_OWNER_TYPE_PARAM_KEY = "ownerType";

	public static final String WIKI_ID_PARAM_KEY = "wikiId";

	public static final String WIKI_FILENAME_PARAM_KEY = "fileName";

	public static final String FILE_HANDLE_PREVIEW_PARAM_KEY = "preview";

	public static final String FILE_HANDLE_CREATE_FILEENTITY_PARAM_KEY = "createFileEntity";

	public static final String FILE_HANDLE_FILEENTITY_PARENT_PARAM_KEY = "fileEntityParentId";

	public static final String IS_RESTRICTED_PARAM_KEY = "isRestricted";

	public static final String ADD_TO_ENTITY_ATTACHMENTS_PARAM_KEY = "isAddToAttachments";

	public static final String USER_PROFILE_PARAM_KEY = "userId";

	public static final String TOKEN_ID_PARAM_KEY = "tokenId";

	public static final String WAIT_FOR_URL = "waitForUrl";

	public static final String ENTITY_CREATEDBYPRINCIPALID_KEY = "createdByPrincipalId";

	public static final String MAKE_ATTACHMENT_PARAM_KEY = "makeAttachment";

	public static final String ETAG_KEY = "etag";

	public static final String ENTITY_VERSION_STRING = "/version/";
	
	public static final String MATHJAX_PREFIX = "\\[";
	public static final String MATHJAX_SUFFIX = "\\]";
	
	
	//Synapse Properties
	public static final String CHALLENGE_TUTORIAL_PROPERTY ="org.sagebionetworks.portal.challenge_synapse_id";
	public static final String CHALLENGE_WRITE_UP_TUTORIAL_PROPERTY ="org.sagebionetworks.portal.challenge_writeup_synapse_id";
	

	public static final String TEXT_TAB_SEPARATED_VALUES = "text/tab-separated-values";
	
}
