package org.sagebionetworks.web.server.servlet.filter;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import static org.sagebionetworks.repo.model.EntityBundle.ANNOTATIONS;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.sagebionetworks.markdown.SynapseMarkdownProcessor;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityId;
import org.sagebionetworks.repo.model.EntityIdList;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.entity.query.Condition;
import org.sagebionetworks.repo.model.entity.query.EntityFieldName;
import org.sagebionetworks.repo.model.entity.query.EntityQuery;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResults;
import org.sagebionetworks.repo.model.entity.query.EntityQueryUtils;
import org.sagebionetworks.repo.model.entity.query.Operator;
import org.sagebionetworks.repo.model.entity.query.Sort;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.repo.model.search.Hit;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl;
import org.sagebionetworks.web.shared.SearchQueryUtils;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.AsyncCallback;

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
	JSONObjectAdapter jsonObjectAdapter;
	
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
				String lowerCaseDomain = domain.toLowerCase();
				if (!lowerCaseDomain.contains("www.synapse.org")) {
					response.setContentType("text/html");
					HttpServletResponse httpResponse = (HttpServletResponse) response;
					httpResponse.setStatus(HttpServletResponse.SC_OK);
					PrintWriter out = httpResponse.getWriter();
					out.println("Synapse test site  - " + domain);
					return;
				}
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
				
				response.setContentType("text/html");
				HttpServletResponse httpResponse = (HttpServletResponse) response;
				httpResponse.setStatus(HttpServletResponse.SC_OK);
				PrintWriter out = httpResponse.getWriter();
				out.println(replacedWithFullHrefs);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			chain.doFilter(request, response);
		}
	}

	private String getHomePageHtml() throws JSONObjectAdapterException, RestServiceException{
		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE html><html><head><title>"+DisplayConstants.DEFAULT_PAGE_TITLE+"</title><meta name=\"description\" content=\""+DisplayConstants.DEFAULT_PAGE_DESCRIPTION+"\" /></head><body>");
		//add direct links to all public projects in the system
		SearchQuery query = SearchQueryUtils.getDefaultSearchQuery();
		html.append("<h1>"+DisplayConstants.DEFAULT_PAGE_TITLE+"</h1>" + DisplayConstants.DEFAULT_PAGE_DESCRIPTION + "<br />");
		
		SearchResults results = synapseClient.search(query);
		
		//append this set to the list
		while(results.getHits().size() > 0) {
			for (Hit hit : results.getHits()) {
				//add links
				html.append("<a href=\"#!Synapse:"+hit.getId()+"\">"+hit.getName()+"</a><br />");
			}
			long newStart = results.getStart() + results.getHits().size();
			query.setStart(newStart);
			results = synapseClient.search(query);
		}
		
		html.append("</body></html>");
		return html.toString();
	}
	
	private String getEntityHtml(String entityId) throws RestServiceException, JSONObjectAdapterException{
		int mask = ENTITY | ANNOTATIONS;
		EntityBundle bundle = synapseClient.getEntityBundle(entityId, mask);
		Entity entity = bundle.getEntity();
		Annotations annotations = bundle.getAnnotations();
		String name = escapeHtml(entity.getName());
		String description = escapeHtml(entity.getDescription());
		String markdown = null;
		String createdBy = null;
		try{
			UserProfile profile = synapseClient.getUserProfile(entity.getCreatedBy());
			StringBuilder createdByBuilder = new StringBuilder();
			if (profile.getFirstName() != null) {
				createdByBuilder.append(profile.getFirstName() + " ");
			}
			if (profile.getLastName() != null) {
				createdByBuilder.append(profile.getLastName() + " ");
			}
			createdByBuilder.append(profile.getUserName());

			createdBy = createdByBuilder.toString();
		}  catch (Exception e) {}
		try{
			WikiPage rootPage = synapseClient.getV2WikiPageAsV1(new WikiPageKey(entity.getId(), ObjectType.ENTITY.toString(), null));
			markdown = escapeHtml(rootPage.getMarkdown());
		} catch (Exception e) {}
		
		StringBuilder html = new StringBuilder();
		
		//note: can't set description meta tag, since it might be markdown.
		html.append("<!DOCTYPE html><html><head><title>"+name +" - "+ entity.getId()+"</title></head><body>");
		
		html.append("<h1>"+name+"</h1>");
		if (description != null) {
			html.append(description + "<br />");
		}
		if (createdBy != null) {
			html.append("Created By " + createdBy + "<br />");
		}
		if (markdown != null) {
			try {
				String wikiHtml = SynapseMarkdownProcessor.getInstance().markdown2Html(markdown, "", "");
				//extract plain text from wiki html
				markdown = Jsoup.parse(wikiHtml).text();
			} catch (IOException e) {
			}
			html.append(markdown + "<br />");
		}
		html.append("<br />");
		for (String key : annotations.getStringAnnotations().keySet()) {
			List<String> value = annotations.getStringAnnotations().get(key);
			html.append(escapeHtml(key) + escapeHtml(getValueString(value)) + "<br />");
		}
		for (String key : annotations.getLongAnnotations().keySet()) {
			List<Long> value = annotations.getLongAnnotations().get(key);
			html.append(escapeHtml(key) + escapeHtml(getValueString(value)) + "<br />");
		}
		for (String key : annotations.getDoubleAnnotations().keySet()) {
			List<Double> value = annotations.getDoubleAnnotations().get(key);
			html.append(escapeHtml(key) + escapeHtml(getValueString(value)) + "<br />");
		}
		
		//and ask for all children
		try {
			EntityQueryResults childList = synapseClient.executeEntityQuery(createGetChildrenQuery(entityId));
			for (EntityQueryResult childId : childList.getEntities()) {
				html.append("<a href=\"#!Synapse:"+childId.getId()+"\">"+childId.getName()+"</a><br />");
			}} catch(Exception e) {};
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
		SearchQuery inputQuery = EntityFactory.createEntityFromJSONString(searchQueryJson, SearchQuery.class);
		SearchResults results = synapseClient.search(inputQuery);
		
		//append this set to the list
		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE html><html><head><title>Sage Synapse: All Projects - starting from "+inputQuery.getStart()+"</title><meta name=\"description\" content=\"\" /></head><body>");
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
			String decoded = URLDecoder.decode(uglyUrl, "UTF-8");
			// dev mode
			String result = decoded.replace("gwt", "?gwt");
			result = result.replace("&"+ESCAPED_FRAGMENT, "#!");
			result = result.replace("?"+ESCAPED_FRAGMENT, "#!");
			result = result.replace(ESCAPED_FRAGMENT, "#!");
			return result;
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}


	public EntityQuery createGetChildrenQuery(String parentId) {
		EntityQuery newQuery = new EntityQuery();
		Condition condition = EntityQueryUtils.buildCondition(
				EntityFieldName.parentId, Operator.EQUALS, parentId);
		newQuery.setConditions(Arrays.asList(condition));
		newQuery.setLimit(Long.MAX_VALUE);
		newQuery.setOffset(0L);
		return newQuery;
	}
	
	@Override
	public void init(FilterConfig config) throws ServletException {
		this.sc = config.getServletContext();
		synapseClient = new SynapseClientImpl();
		synapseClient.setServiceUrlProvider(new ServiceUrlProvider());
		jsonObjectAdapter = new JSONObjectAdapterImpl();
    }
}
