package org.sagebionetworks.web.client;

import java.util.Arrays;
import java.util.HashSet;

import org.sagebionetworks.web.client.resources.WebResource;
import org.sagebionetworks.web.client.resources.WebResource.ResourceType;

public class ClientProperties {

	public static final String HELP_EMAIL_ADDRESS = "synapseInfo@sagebase.org";
	public static final String BREADCRUMB_SEP = "&nbsp;&raquo;&nbsp;";
	
	public static final String DEFAULT_PLACE_TOKEN = "0";
	
	public static final String SYNAPSE_ID_PREFIX = "syn";
	
	public static final String[] IMAGE_CONTENT_TYPES = new String[] {"image/bmp","image/pjpeg","image/jpeg","image/jpg", "image/jpe","image/gif","image/png", "image/svg+xml"};
	public static final HashSet<String> IMAGE_CONTENT_TYPES_SET = new HashSet<String>(Arrays.asList(IMAGE_CONTENT_TYPES));
	
	public static final String[] TABLE_CONTENT_TYPES = new String[] {"application/vnd.ms-excel", "text/csv","text/tab-separated-values","text/plain", "text/txt", "text", "text/", "text/tsv"};
	public static final HashSet<String> TABLE_CONTENT_TYPES_SET = new HashSet<String>(Arrays.asList(TABLE_CONTENT_TYPES));
	
	public static final String[] CODE_EXTENSIONS = new String[] {".cwl", ".wdl", ".json"};
	public static final HashSet<String> CODE_EXTENSIONS_SET = new HashSet<String>(Arrays.asList(CODE_EXTENSIONS));
	
	
	public static final double BASE = 1024, KB = BASE, MB = KB*BASE, GB = MB*BASE, TB = GB*BASE;
	
	/**
	 * Sometimes we are forced to use a table to center an image in a fixed space. 
	 * This is the third option from: http://stackoverflow.com/questions/388180/how-to-make-an-image-center-vertically-horizontally-inside-a-bigger-div
	 * It should only be used when the first two options are not an option.
	 * Place your image between the start and end.
	 */
	public static final String IMAGE_CENTERING_TABLE_START = "<table width=\"100%\" height=\"100%\" align=\"center\" valign=\"center\"><tr><td>";
	public static final String IMAGE_CENTERING_TABLE_END = "</td></tr></table>";
	public static final String STYLE_DISPLAY_INLINE = "inline-block";
	
	/*
	 * JavaScript WebResources
	 */
	public static final WebResource SYNAPSE_REACT_COMPONENTS_JS = new WebResource("js/react-components/synapse-react-components.min.js", ResourceType.JAVASCRIPT);
	public static final WebResource MATHJAX_LOADER_JS = new WebResource("js/mathjax-loader.js", ResourceType.JAVASCRIPT);
	public static final WebResource MATHJAX_JS = new WebResource("https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.1/MathJax.js?config=default", ResourceType.JAVASCRIPT);
	public static final WebResource PLOTLY_JS = new WebResource("https://cdn.plot.ly/plotly-1.33.1.min.js", ResourceType.JAVASCRIPT);
	public static final WebResource PLOTLY_REACT_JS = new WebResource("https://unpkg.com/react-plotly.js@1.3.0/dist/create-plotly-component.min.js", ResourceType.JAVASCRIPT);
	public static final WebResource AWS_SDK_JS = new WebResource("js/aws-sdk-2.86.0.min.js", ResourceType.JAVASCRIPT);
	public static final String QUERY_SERVICE_PREFIX = "/query?query=";
	public static final String EVALUATION_QUERY_SERVICE_PREFIX = "/evaluation/submission/query?query=";
	
	public static final WebResource DIFF_LIB_JS = new WebResource("js/diff/difflib.js", ResourceType.JAVASCRIPT);
	public static final WebResource DIFF_VIEW_JS = new WebResource("js/diff/diffview.js", ResourceType.JAVASCRIPT);
}

