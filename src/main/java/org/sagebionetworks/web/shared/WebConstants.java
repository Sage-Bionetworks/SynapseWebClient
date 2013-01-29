package org.sagebionetworks.web.shared;
import org.pegdown.Extensions;
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

	public static final String PROVENANCE_API_URL = "https://sagebionetworks.jira.com/wiki/display/PLFM/Analysis+Provenance+in+Synapse";
	
	/**
	 * Regex defining a valid annotation name. Characters are selected to ensure
	 * compatibility across services and clients.
	 * 
	 */
	public static final String VALID_ANNOTATION_NAME_REGEX = "^[a-z,A-Z,0-9,_,.]+";
	public static final String VALID_URL_REGEX = "^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	public static final String WIDGET_NAME_REGEX = "[a-z,A-Z,0-9,., ,\\-,\\+,(,)]";
	public static final String VALID_WIDGET_NAME_REGEX = "^"+WIDGET_NAME_REGEX+"+";
	public static final String VALID_ENTITY_ID_REGEX = "^[Ss]{1}[Yy]{1}[Nn]{1}\\d+";
	public static final String VALID_POSITIVE_NUMBER_REGEX = "^[0-9]+";
	
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

	public static int MARKDOWN_OPTIONS = 
		Extensions.ABBREVIATIONS |		//Abbreviations in the way of PHP Markdown Extra.
		Extensions.AUTOLINKS |			//Plain (undelimited) autolinks the way Github-flavoured-Markdown implements them.
		Extensions.QUOTES |				//Beautifies single quotes, double quotes and double angle quotes
		Extensions.SMARTS |				//Beautifies apostrophes, ellipses ("..." and ". . .") and dashes ("--" and "---")		
		Extensions.TABLES |				//Tables similar to MultiMarkdown (which is in turn like the PHP Markdown Extra tables, but with colspan support).
		Extensions.SUPPRESS_ALL_HTML |	//Suppresses HTML
		Extensions.WIKILINKS;			//Support [[Wiki-style links]] with a customizable URL rendering logic.

	public static final String ENTITY_DESCRIPTION_FORMATTING_TIPS_HTML = "<div style=\"margin-left:20px\"><br><br>" +
			"<h3>Phrase Emphasis</h3><pre><code>*italic*   **bold**<br>_italic_   __bold__<br></code></pre><br>" +
			"<h3>Links</h3><pre><code>http://sagebase.org - automatic!</code></pre><pre><code>syn12345 - automatic!</code></pre><pre><code>An [example](http://url.com/)</code></pre><pre><code>An [example][id]. Then, anywhere else in the description, define the link:<br>  [id]: http://example.com/<br></code></pre><br>" +
			"<h3>Tables</h3><pre><code>First Header  | Second Header | Third Header<br>------------- | ------------- | -------------<br>Content Cell  | Content Cell  | Content Cell<br>Content Cell  | Content Cell  | Content Cell</code></pre><br>" +
			"<h3>Images</h3><pre><code>![alt text](http://path/to/img.jpg)</code></pre><br>" +
			"<h3>Headers</h3><pre><code># Header 1<br>## Header 2<br>###### Header 6<br></code></pre><br>" +
			"<h3>Lists</h3><p>Ordered, without paragraphs:<pre><code>1.  List item one<br>2.  List item two<br></code></pre></p><p>Unordered, with paragraphs:<pre><code>*   A list item.<br>    With multiple paragraphs.<br>*   Another list item<br></code></pre></p><p>You can nest them:<pre><code>*   Abacus<br>    * answer<br>*   Bubbles<br>    1.  bunk<br>    2.  bupkis<br>        * BELITTLER<br>    3. burper<br>*   Cunning<br></code></pre></p><br>" +
			"<h3>Blockquotes</h3><pre><code>&gt; Email-style angle brackets<br>&gt; are used for blockquotes.<br>&gt; &gt; And, they can be nested.<br>&gt; #### Headers in blockquotes<br>&gt; <br>&gt; * You can quote a list.<br>&gt; * Etc.<br></code></pre><br>" +
			"<h3>Code Spans</h3><pre><code>`&lt;code&gt;` spans are delimited<br>by backticks.<br>You can include literal backticks<br>like `` `this` ``.<br></code></pre><br>" +
			"<h3>Preformatted Code Blocks</h3><pre><code>Indent every line of a code block by at least 4 spaces or 1 tab.<br><br>This is a normal paragraph.<br><br>    This is a preformatted<br>    code block.</code></pre><br>" +
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
}
