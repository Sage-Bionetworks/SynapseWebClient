package org.sagebionetworks.web.client;

import java.util.Arrays;
import java.util.HashSet;

import org.sagebionetworks.web.client.resources.WebResource;

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
	public static final WebResource MATH_PROCESSOR_JS = new WebResource("js/katex-0.10.1.min.js");
	public static final WebResource AWS_SDK_JS = new WebResource("js/aws-sdk-2.494.0.min.js");
	public static final String QUERY_SERVICE_PREFIX = "/query?query=";
	public static final String EVALUATION_QUERY_SERVICE_PREFIX = "/evaluation/submission/query?query=";
	
	public static final WebResource SYNAPSE_REACT_COMPONENTS_JS = new WebResource("js/SRC/synapse-react-client.production.min.js");
	public static final WebResource PROP_TYPES_JS = new WebResource("https://unpkg.com/prop-types@15.6.2/prop-types.min.js");
	public static final WebResource REACT_MEASURE_JS = new WebResource("https://unpkg.com/react-measure@2.2.2/dist/index.umd.js");
	public static final WebResource REACT_TOOLTIP_JS = new WebResource("https://unpkg.com/react-tooltip@3.9.2/standalone/react-tooltip.min.js");
	
	public static final WebResource DIFF_LIB_JS = new WebResource("js/diff/difflib.js");
	public static final WebResource DIFF_VIEW_JS = new WebResource("js/diff/diffview.js");
	
	public static void fixResourceToCdnEndpoint(WebResource resource, String cdnEndpoint) {
		String currentUrl = resource.getUrl();
		if (cdnEndpoint != null && !currentUrl.startsWith(cdnEndpoint) && !currentUrl.toLowerCase().startsWith("http")) {
			resource.setUrl(cdnEndpoint + currentUrl);
		}
	}
}

