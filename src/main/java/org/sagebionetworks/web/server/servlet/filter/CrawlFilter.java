package org.sagebionetworks.web.server.servlet.filter;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.sagebionetworks.markdown.SynapseMarkdownProcessor;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMemberTypeFilterOptions;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.annotation.v2.Annotations;
import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValue;
import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyOrder;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadOrder;
import org.sagebionetworks.repo.model.discussion.Forum;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.repo.model.search.Hit;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.place.TeamSearch;
import org.sagebionetworks.web.server.servlet.DiscussionForumClientImpl;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.SearchQueryUtils;
import org.sagebionetworks.web.shared.TeamMemberBundle;
import org.sagebionetworks.web.shared.TeamMemberPagedResults;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import com.google.gwt.thirdparty.guava.common.base.Supplier;
import com.google.gwt.thirdparty.guava.common.base.Suppliers;

/**
 * This filter detects ajax crawler (Google). If so, it takes over the renders the javascript page
 * and handles the response.
 *
 */
public class CrawlFilter implements Filter {

	private static final String DISCUSSION_THREAD_ID = "/discussion/threadId=";
	public static final String ESCAPED_FRAGMENT = "_escaped_fragment_=";
	ServletContext sc;

	private final Supplier<String> homePageCached = Suppliers.memoizeWithExpiration(homePageSupplier(), 1, TimeUnit.DAYS);
	public static final int MAX_CHILD_PAGES = 5;

	public String getCachedHomePageHtml() {
		return homePageCached.get();
	}

	private Supplier<String> homePageSupplier() {
		return new Supplier<String>() {
			public String get() {
				try {
					return getHomePageHtml();
				} catch (JSONObjectAdapterException | RestServiceException e) {
					return e.getMessage();
				}
			}
		};
	}

	/**
	 * Injected with Gin
	 */
	private SynapseClientImpl synapseClient;
	private DiscussionForumClientImpl discussionForumClient;
	JSONObjectAdapter jsonObjectAdapter;

	@Override
	public void destroy() {
		sc = null;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRqst = (HttpServletRequest) request;
		// Is this an ugly url that we need to convert/handle?
		String queryString = httpRqst.getQueryString();
		if (queryString != null && queryString.contains(ESCAPED_FRAGMENT)) {
			try {
				String uri = httpRqst.getRequestURI();
				int port = request.getServerPort();
				String domain = request.getServerName();
				String lowerCaseDomain = domain.toLowerCase();
				if (!(lowerCaseDomain.contains("www.synapse.org") || lowerCaseDomain.contains("127.0.0.1"))) {
					response.setContentType("text/html");
					HttpServletResponse httpResponse = (HttpServletResponse) response;
					httpResponse.setStatus(HttpServletResponse.SC_OK);
					PrintWriter out = httpResponse.getWriter();
					out.println("Synapse test site  - " + domain);
					return;
				}
				String scheme = request.getScheme();
				// build an html page for this request
				String html = "";
				String fixedQueryString = uri + rewriteQueryString(queryString);
				if (fixedQueryString.contains("#!Home")) {
					// send back info about the site
					html = getCachedHomePageHtml();
				} else if (fixedQueryString.contains("#!Synapse")) {
					if (fixedQueryString.contains(DISCUSSION_THREAD_ID)) {
						String threadId = fixedQueryString.substring(fixedQueryString.indexOf(DISCUSSION_THREAD_ID) + DISCUSSION_THREAD_ID.length());
						html = getThreadHtml(threadId);
					} else {
						// index information about the synapse entity
						String entityId = fixedQueryString.substring(fixedQueryString.indexOf(":", fixedQueryString.indexOf("#!")) + 1);
						html = getEntityHtml(entityId);
					}
				} else if (fixedQueryString.contains("#!Search")) {
					// index all projects
					String searchQueryJson = fixedQueryString.substring(fixedQueryString.indexOf(":", fixedQueryString.indexOf("#!")) + 1);
					html = getAllProjectsHtml(URLDecoder.decode(searchQueryJson));
				} else if (fixedQueryString.contains("#!TeamSearch")) {
					// index all teams
					String startIndex = fixedQueryString.substring(fixedQueryString.indexOf(TeamSearch.START_DELIMITER, fixedQueryString.indexOf("#!")) + TeamSearch.START_DELIMITER.length());
					html = getAllTeamsHtml(startIndex);
				} else if (fixedQueryString.contains("#!Team")) {
					// index team (including members)
					String teamId = fixedQueryString.substring(fixedQueryString.indexOf(":", fixedQueryString.indexOf("#!")) + 1);
					html = getTeamHtml(teamId);
				} else if (fixedQueryString.contains("#!Profile")) {
					// index team (including members)
					String profileId = fixedQueryString.substring(fixedQueryString.indexOf(":", fixedQueryString.indexOf("#!")) + 1);
					html = getProfileHtml(profileId);
				}

				URL url = new URL(scheme, domain, port, fixedQueryString);
				// replace all relative links with full links due to this Google AJAX crawler support chicken-dance
				String originalUrl = url.toString();
				String toPage = originalUrl.substring(0, originalUrl.indexOf("#") + 1);
				String replacedWithFullHrefs = html.replace("href=\"#", "href=\"" + toPage);

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

	private String getHomePageHtml() throws JSONObjectAdapterException, RestServiceException {
		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE html><html><head><title>" + DisplayConstants.DEFAULT_PAGE_TITLE + "</title><meta name=\"description\" content=\"" + DisplayConstants.DEFAULT_PAGE_DESCRIPTION + "\" /></head><body>");
		// add direct links to all public projects in the system
		SearchQuery query = SearchQueryUtils.getDefaultSearchQuery();
		html.append("<h1>" + DisplayConstants.DEFAULT_PAGE_TITLE + "</h1>" + DisplayConstants.DEFAULT_PAGE_DESCRIPTION + "<br />");
		// add link to team search
		html.append("<h3><a href=\"https://www.synapse.org/#!TeamSearch:" + TeamSearch.START_DELIMITER + "0\">Teams</a></h3><br />");
		SearchResults results = synapseClient.search(query);
		// append this set to the list
		while (results.getHits().size() > 0) {
			for (Hit hit : results.getHits()) {
				// add links
				html.append("<a href=\"https://www.synapse.org/#!Synapse:" + hit.getId() + "\">" + hit.getName() + "</a><br />");
			}
			long newStart = results.getStart() + results.getHits().size();
			query.setStart(newStart);
			results = synapseClient.search(query);
		}
		html.append("</body></html>");
		return html.toString();
	}

	private String getCreatedByString(String userId) throws RestServiceException {
		UserProfile profile = synapseClient.getUserProfile(userId);
		return getUserProfileString(profile);
	}

	private String getUserProfileString(UserProfile profile) {
		StringBuilder createdByBuilder = new StringBuilder();
		createdByBuilder.append("<a href=\"https://www.synapse.org/#!Profile:" + profile.getOwnerId() + "\">");
		if (profile.getFirstName() != null) {
			createdByBuilder.append(profile.getFirstName() + " ");
		}
		if (profile.getLastName() != null) {
			createdByBuilder.append(profile.getLastName() + " ");
		}
		createdByBuilder.append(profile.getUserName());
		createdByBuilder.append("</a>");
		return createdByBuilder.toString();
	}

	private String getEntityHtml(String entityId) throws RestServiceException, JSONObjectAdapterException {
		EntityBundleRequest bundleRequest = new EntityBundleRequest();
		bundleRequest.setIncludeEntity(true);
		bundleRequest.setIncludeAnnotations(true);
		EntityBundle bundle = synapseClient.getEntityBundle(entityId, bundleRequest);
		Entity entity = bundle.getEntity();
		Annotations annotations = bundle.getAnnotations();
		String name = escapeHtml(entity.getName());
		String description = escapeHtml(entity.getDescription());
		String markdown = null;
		String createdBy = null;
		try {
			createdBy = getCreatedByString(entity.getCreatedBy());
		} catch (Exception e) {
		}
		try {
			WikiPage rootPage = synapseClient.getV2WikiPageAsV1(new WikiPageKey(entity.getId(), ObjectType.ENTITY.toString(), null));
			markdown = escapeHtml(rootPage.getMarkdown());
		} catch (Exception e) {
		}

		StringBuilder html = new StringBuilder();

		// note: can't set description meta tag, since it might be markdown.
		html.append("<!DOCTYPE html><html><head><title>" + name + " - " + entity.getId() + "</title></head><body>");

		html.append("<h1>" + name + "</h1>");
		if (description != null) {
			html.append(description + "<br />");
		}
		if (createdBy != null) {
			html.append("Created By " + createdBy + "<br />");
		}
		if (markdown != null) {
			try {
				String wikiHtml = SynapseMarkdownProcessor.getInstance().markdown2Html(markdown, "", "");
				// extract plain text from wiki html
				markdown = Jsoup.parse(wikiHtml).text();
			} catch (IOException e) {
			}
			html.append(markdown + "<br />");
		}
		html.append("<br />");
		Map<String, AnnotationsValue> annotationMap = annotations.getAnnotations();
		for (String key : annotationMap.keySet()) {
			AnnotationsValue values = annotationMap.get(key);
			List<String> value = values.getValue();
			html.append(escapeHtml(key) + escapeHtml(getValueString(value)) + "<br />");
		}
		// and link to the discussion forum (all threads and replies) if this is a project.
		if (entity instanceof Project) {
			Forum forum = discussionForumClient.getForumByProjectId(entity.getId());
			if (forum != null) {
				String forumId = forum.getId();
				long currentOffset = 0;
				PaginatedResults<DiscussionThreadBundle> paginatedThreads;
				do {
					paginatedThreads = discussionForumClient.getThreadsForForum(forumId, 20L, currentOffset, DiscussionThreadOrder.PINNED_AND_LAST_ACTIVITY, false, DiscussionFilter.EXCLUDE_DELETED);
					List<DiscussionThreadBundle> threadList = paginatedThreads.getResults();
					for (DiscussionThreadBundle thread : threadList) {
						html.append("<a href=\"https://www.synapse.org/#!Synapse:" + entity.getId() + DISCUSSION_THREAD_ID + thread.getId() + "\">" + thread.getTitle() + "</a><br />");
					}
					currentOffset += 20;
				} while (!paginatedThreads.getResults().isEmpty());

			}
		}

		// and ask for all children
		// only show the first few pages.
		EntityChildrenRequest request = createGetChildrenQuery(entityId);
		EntityChildrenResponse childList;
		int i = 0;
		do {
			childList = synapseClient.getEntityChildren(request);
			for (EntityHeader childId : childList.getPage()) {
				html.append("<a href=\"https://www.synapse.org/#!Synapse:" + childId.getId() + "\">" + childId.getId() + "</a><br />");
			}
			request.setNextPageToken(childList.getNextPageToken());
			i++;
		} while (i < MAX_CHILD_PAGES && childList.getNextPageToken() != null);
		html.append("</body></html>");
		return html.toString();
	}

	private String getThreadHtml(String threadId) throws JSONObjectAdapterException, RestServiceException, IOException {
		StringBuilder html = new StringBuilder();
		DiscussionThreadBundle thread = discussionForumClient.getThread(threadId);
		html.append("<!DOCTYPE html><html><head><title>" + thread.getTitle() + "</title></head><body>");
		html.append("<h1>" + thread.getTitle() + "</h1>");
		try {
			String url = discussionForumClient.getThreadUrl(thread.getMessageKey());
			html.append("<h4>" + getURLContents(url) + "</h4>");
		} catch (Exception e1) {
		}

		String createdBy = null;
		try {
			createdBy = getCreatedByString(thread.getCreatedBy());
		} catch (Exception e) {
		}
		html.append("Created by " + createdBy + "<br>");
		PaginatedResults<DiscussionReplyBundle> replies = discussionForumClient.getRepliesForThread(thread.getId(), 100L, 0L, DiscussionReplyOrder.CREATED_ON, false, DiscussionFilter.EXCLUDE_DELETED);
		for (DiscussionReplyBundle reply : replies.getResults()) {
			try {
				String replyURL = discussionForumClient.getReplyUrl(reply.getMessageKey());
				html.append(getURLContents(replyURL) + "<br>");
			} catch (Exception e) {
			}
		}
		html.append("</body></html>");
		return html.toString();
	}

	private String getTeamHtml(String teamId) throws JSONObjectAdapterException, RestServiceException, IOException {
		StringBuilder html = new StringBuilder();
		Team team = synapseClient.getTeam(teamId);

		html.append("<!DOCTYPE html><html><head><title>" + team.getName() + "</title></head><body>");
		html.append("<h1>" + team.getName() + "</h1>");
		if (team.getDescription() != null) {
			html.append("<h3>" + team.getDescription() + "</h3>");
		}
		TeamMemberPagedResults teamMembers = synapseClient.getTeamMembers(team.getId(), "", TeamMemberTypeFilterOptions.ALL, 20, 0);
		for (TeamMemberBundle teamMember : teamMembers.getResults()) {
			try {
				html.append(getUserProfileString(teamMember.getUserProfile()) + "<br>");
			} catch (Exception e) {
			}
		}
		html.append("</body></html>");
		return html.toString();
	}


	private String getProfileHtml(String profileId) throws JSONObjectAdapterException, RestServiceException, IOException {
		StringBuilder html = new StringBuilder();
		UserProfile profile = synapseClient.getUserProfile(profileId);
		String display = profile.getFirstName() + " " + profile.getLastName() + " " + profile.getUserName();
		html.append("<!DOCTYPE html><html><head><title>" + display + "</title></head><body>");
		html.append("<h1>" + display + "</h1>");
		if (profile.getSummary() != null) {
			html.append("<h4>" + profile.getSummary() + "</h4>");
		}
		if (profile.getLocation() != null) {
			html.append("<p>" + profile.getLocation() + "</p>");
		}
		if (profile.getPosition() != null) {
			html.append("<p>" + profile.getPosition() + "</p>");
		}
		if (profile.getIndustry() != null) {
			html.append("<p>" + profile.getIndustry() + "</p>");
		}
		if (profile.getCompany() != null) {
			html.append("<p>" + profile.getCompany() + "</p>");
		}

		html.append("</body></html>");
		return html.toString();
	}

	private String getURLContents(String urlTarget) throws IOException {
		URL url = new URL(urlTarget);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		try {
			conn.setRequestProperty(WebConstants.CONTENT_TYPE, WebConstants.TEXT_PLAIN_CHARSET_UTF8);
			InputStream in = new GZIPInputStream(conn.getInputStream());
			try {
				return IOUtils.toString(in, "UTF-8");
			} finally {
				IOUtils.closeQuietly(in);
			}
		} finally {
			conn.disconnect();
		}
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

	private String getAllProjectsHtml(String searchQueryJson) throws RestServiceException, JSONObjectAdapterException {
		SearchQuery inputQuery = EntityFactory.createEntityFromJSONString(searchQueryJson, SearchQuery.class);
		SearchResults results = synapseClient.search(inputQuery);

		// append this set to the list
		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE html><html><head><title>Sage Synapse: All Projects - starting from " + inputQuery.getStart() + "</title><meta name=\"description\" content=\"\" /></head><body>");
		for (Hit hit : results.getHits()) {
			// add links
			html.append("<a href=\"https://www.synapse.org/#!Synapse:" + hit.getId() + "\">" + hit.getName() + "</a><br />");
		}
		// add another link for the next page of results
		long newStart = results.getStart() + results.getHits().size();
		inputQuery.setStart(newStart);
		String newJson = EntityFactory.createJSONStringForEntity(inputQuery);
		html.append("<a href=\"https://www.synapse.org/#!Search:" + URLEncoder.encode(newJson) + "\">Next Page</a><br />");

		html.append("</body></html>");
		return html.toString();
	}

	private String getAllTeamsHtml(String startIndex) throws RestServiceException {
		int start = Integer.parseInt(startIndex);
		PaginatedResults<Team> teams = synapseClient.getTeamsBySearch("", 50, start);

		// append this set to the list
		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE html><html><head><title>Sage Synapse: All Teams - from " + startIndex + "</title><meta name=\"description\" content=\"\" /></head><body>");
		for (Team team : teams.getResults()) {
			// add links
			html.append("<a href=\"https://www.synapse.org/#!Team:" + team.getId() + "\">" + team.getName() + "</a><br />");
		}
		// add another link for the next page of results
		long newStart = start + teams.getResults().size();
		html.append("<h4><a href=\"https://www.synapse.org/#!TeamSearch:" + TeamSearch.START_DELIMITER + newStart + "\">Next Page</a></h4><br />");

		html.append("</body></html>");
		return html.toString();
	}

	public String rewriteQueryString(String uglyUrl) {
		try {
			String decoded = URLDecoder.decode(uglyUrl, "UTF-8");
			// dev mode
			String result = decoded.replace("gwt", "?gwt");
			result = result.replace("&" + ESCAPED_FRAGMENT, "#!");
			result = result.replace("?" + ESCAPED_FRAGMENT, "#!");
			result = result.replace(ESCAPED_FRAGMENT, "#!");
			return result;
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}


	public EntityChildrenRequest createGetChildrenQuery(String parentId) {
		EntityChildrenRequest newQuery = new EntityChildrenRequest();
		newQuery.setParentId(parentId);
		List<EntityType> types = new ArrayList<EntityType>();
		types.add(EntityType.file);
		types.add(EntityType.folder);
		newQuery.setIncludeTypes(types);
		return newQuery;
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		this.sc = config.getServletContext();
		synapseClient = new SynapseClientImpl();
		discussionForumClient = new DiscussionForumClientImpl();
		jsonObjectAdapter = new JSONObjectAdapterImpl();
	}
}
