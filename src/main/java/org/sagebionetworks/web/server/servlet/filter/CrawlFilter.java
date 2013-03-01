package org.sagebionetworks.web.server.servlet.filter;

import static org.sagebionetworks.web.shared.EntityBundleTransport.ANNOTATIONS;
import static org.sagebionetworks.web.shared.EntityBundleTransport.ENTITY;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.message.ObjectType;
import org.sagebionetworks.repo.model.search.Hit;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

/**
 * This filter detects ajax crawler (Google).  If so, it takes over the renders the javascript page and handles the response.
 *
 */
public class CrawlFilter implements Filter {

	public static final String ESCAPED_FRAGMENT = "_escaped_fragment_=";
	ServletContext sc;
	
	/**
	 * Injected with Gin
	 */
	private SynapseClientImpl synapseClient;

	@Override
	public void destroy() {
		sc = null;
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRqst = (HttpServletRequest) request;
		// Is this an ugly url that we need to convert/handle?
		String queryString = httpRqst.getQueryString();
		if (queryString != null && queryString.contains(ESCAPED_FRAGMENT)) {
			try {
				String uri = httpRqst.getRequestURI();
				int port = request.getServerPort();
				String domain = request.getServerName();
				String scheme = request.getScheme();
				//build an html page for this request
				String html = "";
				String fixedQueryString = uri + rewriteQueryString(queryString);
				if (fixedQueryString.contains("#!Home")) {
					//send back info about the site
					html = getHomePageHtml();
					
				} else if (fixedQueryString.contains("#!Synapse")) {
					//index information about the synapse entity
					String entityId = fixedQueryString.substring(fixedQueryString.indexOf(":",fixedQueryString.indexOf("#!"))+1);
					html=getEntityHtml(entityId);
					
				} else if (fixedQueryString.contains("#!Search")) {
					//index all projects
					String searchQueryJson = fixedQueryString.substring(fixedQueryString.indexOf(":",fixedQueryString.indexOf("#!"))+1);
					html=getAllProjectsHtml(URLDecoder.decode(searchQueryJson));
				}
				
				URL url = new URL(scheme, domain, port, fixedQueryString);
				//replace all relative links with full links due to this Google AJAX crawler support chicken-dance
				String originalUrl = url.toString();
				String toPage = originalUrl.substring(0, originalUrl.indexOf("#")+1);
				String replacedWithFullHrefs = html.replace("href=\"#", "href=\""+toPage);
				
				String mt = sc.getMimeType(uri);
				response.setContentType(mt);
				HttpServletResponse httpResponse = (HttpServletResponse) response;
				httpResponse.setStatus(HttpServletResponse.SC_OK);
				ServletOutputStream out = httpResponse.getOutputStream();
				out.println(replacedWithFullHrefs);
				out.flush();
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			chain.doFilter(request, response);
		}
	}

	private String getHomePageHtml(){
		StringBuilder html = new StringBuilder();
		html.append("<html><head><title>"+DisplayConstants.DEFAULT_PAGE_TITLE+"</title><meta name=\"description\" content=\""+DisplayConstants.DEFAULT_PAGE_DESCRIPTION+"\" /></head><body>");
		//add Search Projects link
		html.append("<a href=\"#!Search:{%22returnFields%22:[%22name%22,%22description%22,%22id%22,%22node_type_r%22,%22created_by_r%22,%22created_on%22,%22modified_by_r%22,%22modified_on%22,%22path%22],%20%22facet%22:[%22node_type%22,%22species%22,%22disease%22,%22modified_on%22,%22created_on%22,%22tissue%22,%22num_samples%22,%22created_by%22],%20%22booleanQuery%22:[{%22value%22:%22project%22,%20%22key%22:%22node_type%22}],%20%22queryTerm%22:[%22%22]}\">All Projects</a>");
		html.append("</body></html>");
		return html.toString();
	}
	
	private String getEntityHtml(String entityId) throws RestServiceException, JSONObjectAdapterException{
		int mask = ENTITY | ANNOTATIONS;
		EntityBundleTransport entityTransport = synapseClient.getEntityBundle(entityId, mask);
		Entity entity = EntityFactory.createEntityFromJSONString(entityTransport.getEntityJson(), Entity.class);
		Annotations annotations = EntityFactory.createEntityFromJSONString(entityTransport.getAnnotationsJson(), Annotations.class);
		
		String name = entity.getName();
		String description = entity.getDescription();
		String markdown = null;
		
		try{
			String wikiPageJson = synapseClient.getWikiPage(new WikiPageKey(entity.getId(), ObjectType.ENTITY.toString(), null));
			WikiPage rootPage = EntityFactory.createEntityFromJSONString(wikiPageJson, WikiPage.class);
			markdown = rootPage.getMarkdown();
		} catch (Exception e) {}
		
		StringBuilder html = new StringBuilder();
		
		//note: can't set description meta tag, since it might be markdown.
		html.append("<html><head><title>"+entity.getId()+": "+name+"</title><meta name=\"description\" content=\"\" /></head><body>");
		
		html.append("<h5>Name</h5> " + name + "<br />");
		if (description != null)
			html.append("<h5>Description</h5> " + description + "<br />");
		if (markdown != null)
			html.append("<h5>Wiki</h5> " + markdown + "<br />");
		html.append("<h5>Annotations</h5> <br />");
		for (String key : annotations.getStringAnnotations().keySet()) {
			List<String> value = annotations.getStringAnnotations().get(key);
			html.append(key + getValueString(value) + "<br />");
		}
		for (String key : annotations.getLongAnnotations().keySet()) {
			List<Long> value = annotations.getLongAnnotations().get(key);
			html.append(key + getValueString(value) + "<br />");
		}
		for (String key : annotations.getDoubleAnnotations().keySet()) {
			List<Double> value = annotations.getDoubleAnnotations().get(key);
			html.append(key + getValueString(value) + "<br />");
		}
		
		html.append("</body></html>");
		return html.toString();
	}
	
	private String getValueString(List value) {
		StringBuilder valueBuilder = new StringBuilder();
		if (value != null) {
			valueBuilder.append(": ");
			for (Object object : value) {
				if (object != null)
					valueBuilder.append(object.toString() + " ");
			}
		}
		return valueBuilder.toString();
	}
	
	private String getAllProjectsHtml(String searchQueryJson) throws RestServiceException, JSONObjectAdapterException{
		EntityWrapper entityWrapper = synapseClient.search(searchQueryJson);
		SearchResults results = EntityFactory.createEntityFromJSONString(entityWrapper.getEntityJson(), SearchResults.class);
		SearchQuery inputQuery = EntityFactory.createEntityFromJSONString(searchQueryJson, SearchQuery.class);
		//append this set to the list
		StringBuilder html = new StringBuilder();
		html.append("<html><head><title>Sage Synapse: All Projects - starting from "+inputQuery.getStart()+"</title><meta name=\"description\" content=\"\" /></head><body>");
		for (Hit hit : results.getHits()) {
			//add links
			html.append("<a href=\"#!Synapse:"+hit.getId()+"\">"+hit.getName()+"</a><br />");
		}
		//add another link for the next page of results
		long newStart = results.getStart() + results.getHits().size();
		inputQuery.setStart(newStart);
		String newJson = EntityFactory.createJSONStringForEntity(inputQuery);
		html.append("<a href=\"#!Search:"+URLEncoder.encode(newJson)+"\">Next Page</a><br />");
		
		html.append("</body></html>");
		return html.toString();
	}
	
	public String rewriteQueryString(String uglyUrl) {
		try {
			String decoded = URIUtil.decode(uglyUrl, "UTF-8");
			// dev mode
			String result = decoded.replace("gwt", "?gwt");
			result = result.replace("&"+ESCAPED_FRAGMENT, "#!");
			result = result.replace("?"+ESCAPED_FRAGMENT, "#!");
			result = result.replace(ESCAPED_FRAGMENT, "#!");
			return result;
		} catch (URIException e) {
			return "";
		}
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		this.sc = config.getServletContext();
		synapseClient = new SynapseClientImpl();
		synapseClient.setServiceUrlProvider(new ServiceUrlProvider());
    }
}
