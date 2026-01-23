package org.sagebionetworks.web.server.servlet.filter;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseResultNotReadyException;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMember;
import org.sagebionetworks.repo.model.TeamMemberTypeFilterOptions;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.annotation.v2.Annotations;
import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValue;
import org.sagebionetworks.repo.model.dao.WikiPageKey;
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
import org.sagebionetworks.repo.model.search.query.KeyValue;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.repo.model.table.Dataset;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.place.TeamSearch;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.SearchQueryUtils;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

/**
 * This class helps the HtmlInjectorFilter to provide content for crawler (bots) in the response html.
 */
public class CrawlFilter {

  private static final Log log = LogFactory.getLog(CrawlFilter.class);

  public static final String META_ROBOTS_NOINDEX =
    "<meta name=\"robots\" content=\"noindex\">";
  SynapseClient synapseClient = null;
  JSONObjectAdapter jsonObjectAdapter = null;
  public static final int MAX_CHILD_PAGES = 5;
  public static final int QUERY_RESULTS_PART_MASK = 0x1;
  private static final long MAX_QUERY_ROWS = 1000L;
  // max wait of 9 seconds for async query results (time spent between attempts, each attempt also takes time)
  private static final int MAX_ASYNC_QUERY_ATTEMPTS = 30;
  private static final long ASYNC_QUERY_DELAY_MS = 300L;

  // Markdown processor
  private static Parser parser = Parser.builder().build();
  private static String synapseWikiWidgetDefinitionRegex = "[$][{].*[}]";
  private static Pattern wikiWidgetPattern = Pattern.compile(
    synapseWikiWidgetDefinitionRegex,
    Pattern.CASE_INSENSITIVE
  );

  private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

  public static String removeSynapseWikiWidgets(String markdown) {
    return wikiWidgetPattern.matcher(markdown).replaceAll("");
  }

  public void init(SynapseClient synapseClient) {
    this.synapseClient = synapseClient;

    df.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  public String getHomePageHtml()
    throws JSONObjectAdapterException, RestServiceException, UnsupportedEncodingException {
    StringBuilder html = new StringBuilder();

    // add direct links to all public projects in the system
    SearchQuery query = SearchQueryUtils.getDefaultSearchQuery();
    KeyValue projectsOnly = new KeyValue();
    projectsOnly.setKey("node_type");
    projectsOnly.setValue("project");
    query.getBooleanQuery().add(projectsOnly);
    query.setQueryTerm(Collections.singletonList(""));
    //limit to 50 at a time
    query.setSize(50L);
    query.setStart(0L);

    html.append(
      "<h1>" +
      DisplayConstants.DEFAULT_PAGE_TITLE +
      "</h1>" +
      DisplayConstants.DEFAULT_PAGE_DESCRIPTION +
      "<br />"
    );
    // add link to team search
    html.append(
      "<h3><a href=\"https://www.synapse.org/TeamSearch:" +
      TeamSearch.START_DELIMITER +
      "0\">Teams</a></h3><br />"
    );

    // SWC-7552 : link to data catalog
    html.append(
      "<h3><a href=\"https://www.synapse.org/DataCatalog:0\">Data Catalog</a></h3><br />"
    );

    String newJson = EntityFactory.createJSONStringForEntity(query);

    html.append(
      "<h3><a href=\"https://www.synapse.org/Search:" +
      URLEncoder.encode(newJson, "UTF-8") +
      "\">Projects</a></h3><br />"
    );
    html.append("</body></html>");
    return html.toString();
  }

  private String getCreatedByString(String userId)
    throws RestServiceException, SynapseException {
    UserProfile profile = synapseClient.getUserProfile(userId);
    return getUserProfileString(profile);
  }

  private String getUserProfileString(UserProfile profile) {
    StringBuilder createdByBuilder = new StringBuilder();
    createdByBuilder.append(
      "<a href=\"https://www.synapse.org/Profile:" +
      profile.getOwnerId() +
      "\">"
    );
    createdByBuilder.append(getDisplayName(profile));
    createdByBuilder.append(profile.getUserName());
    createdByBuilder.append("</a>");
    return createdByBuilder.toString();
  }

  public static String getDisplayName(UserProfile profile) {
    StringBuilder displayNameBuilder = new StringBuilder();
    if (profile.getFirstName() != null) {
      displayNameBuilder.append(profile.getFirstName() + " ");
    }
    if (profile.getLastName() != null) {
      displayNameBuilder.append(profile.getLastName() + " ");
    }
    return displayNameBuilder.toString();
  }

  public static String getPlainTextWiki(String entityId, WikiPage rootPage) {
    String plainTextWiki = null;
    if (rootPage != null) {
      try {
        String markdown = escapeHtml(rootPage.getMarkdown());
        if (markdown != null) {
          try {
            Node document = parser.parse(removeSynapseWikiWidgets(markdown));
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            String wikiHtml = renderer.render(document);
            // extract plain text from wiki html
            plainTextWiki = Jsoup.parse(wikiHtml).text();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      } catch (Exception e) {}
    }
    return plainTextWiki;
  }

  public BotHtml getEntityHtml(EntityBundle bundle)
    throws RestServiceException, JSONObjectAdapterException, SynapseException {
    BotHtml response = new BotHtml();
    Entity entity = bundle.getEntity();
    String entityId = entity.getId();
    if (entity instanceof Dataset) {
      // attempt to get the latest stable version (instead of the draft)
      try {
        Long latestEntityVersion = SynapseClientImpl.getLatestEntityVersion(
          entity.getId(),
          synapseClient
        );
        if (latestEntityVersion != null) {
          EntityBundleRequest bundleRequest = new EntityBundleRequest();
          bundleRequest.setIncludeEntity(true);
          bundleRequest.setIncludeAnnotations(true);

          bundle =
            synapseClient.getEntityBundleV2(
              entityId,
              latestEntityVersion,
              bundleRequest
            );
          entity = bundle.getEntity();
        }
      } catch (RestServiceException e) {
        e.printStackTrace();
      }
    }
    Annotations annotations = bundle.getAnnotations();
    String name = escapeHtml(entity.getName());
    String description = escapeHtml(entity.getDescription());
    String createdBy = null;
    WikiPage rootPage = null;
    try {
      createdBy = getCreatedByString(entity.getCreatedBy());
      WikiPageKey key = new WikiPageKey();
      key.setOwnerObjectId(entityId);
      key.setOwnerObjectType(ObjectType.ENTITY);
      key.setWikiPageId(null);
      rootPage = synapseClient.getWikiPage(key);
    } catch (Exception e) {}
    String plainTextWiki = getPlainTextWiki(entity.getId(), rootPage);

    StringBuilder html = new StringBuilder();

    if (annotations.getAnnotations().containsKey("noindex")) {
      response.setHead(META_ROBOTS_NOINDEX);
    }

    html.append("<h1>" + name + "</h1>");
    html.append("<h2>" + entityId + "</h2>");
    if (description != null) {
      html.append(description + "<br />");
    }
    if (createdBy != null) {
      html.append("Created By " + createdBy + "<br />");
    }
    if (plainTextWiki != null) {
      html.append(plainTextWiki + "<br />");
    }
    html.append("<br />");
    Map<String, AnnotationsValue> annotationMap = annotations.getAnnotations();
    for (String key : annotationMap.keySet()) {
      AnnotationsValue values = annotationMap.get(key);
      List<String> value = values.getValue();
      html.append(
        escapeHtml(key) + escapeHtml(getValueString(value)) + "<br />"
      );
    }
    // and link to the discussion forum (all threads and replies) if this is a project.
    if (entity instanceof Project) {
      Forum forum = synapseClient.getForumByProjectId(entity.getId());
      if (forum != null) {
        String forumId = forum.getId();
        long currentOffset = 0;
        PaginatedResults<DiscussionThreadBundle> paginatedThreads;
        do {
          paginatedThreads =
            convertPaginated(
              synapseClient.getThreadsForForum(
                forumId,
                20L,
                currentOffset,
                DiscussionThreadOrder.PINNED_AND_LAST_ACTIVITY,
                false,
                DiscussionFilter.EXCLUDE_DELETED
              )
            );
          List<DiscussionThreadBundle> threadList =
            paginatedThreads.getResults();
          for (DiscussionThreadBundle thread : threadList) {
            html.append(
              "<a href=\"https://www.synapse.org/Synapse:" +
              entity.getId() +
              HtmlInjectionFilter.DISCUSSION_THREAD_ID +
              thread.getId() +
              "\">" +
              thread.getTitle() +
              "</a><br />"
            );
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
        html.append(
          "<a href=\"https://www.synapse.org/Synapse:" +
          childId.getId() +
          "\">" +
          childId.getId() +
          "</a><br />"
        );
      }
      request.setNextPageToken(childList.getNextPageToken());
      i++;
    } while (i < MAX_CHILD_PAGES && childList.getNextPageToken() != null);
    response.setBody(html.toString());
    String datasetScript = getDatasetScriptElement(bundle, plainTextWiki);
    if (response.getHead() != null && !response.getHead().isEmpty()) {
      response.setHead(response.getHead() + datasetScript);
    } else {
      response.setHead(datasetScript);
    }
    return response;
  }

  private String getDatasetScriptElement(
    EntityBundle bundle,
    String plainTextWiki
  ) {
    String content = getDatasetScriptElementContent(bundle, plainTextWiki);
    if (content == null || content.isEmpty()) {
      return "";
    }
    StringBuilder html = new StringBuilder();
    html.append("<script type=\"application/ld+json\">");
    html.append(content);
    html.append("</script>");
    return html.toString();
  }

  private String getDatasetScriptElementContent(
    EntityBundle bundle,
    String plainTextWiki
  ) {
    // If entity id is in the croissant mapping table, then add the JSON-LD script element
    if (synapseClient != null) {
      String s3FileURL = "";
      try {
        // get all rows from the croissant mapping table as the anonymous user to utilize server cached result
        String asyncJobToken = synapseClient.queryTableEntityBundleAsyncStart(
          WebConstants.DATASET_MINIMAL_CROISSANT_FILE_CRAWL_RESPONSE_SQL,
          null,
          MAX_QUERY_ROWS,
          QUERY_RESULTS_PART_MASK,
          WebConstants.DATASET_MINIMAL_CROISSANT_TABLE_ID
        );
        QueryResultBundle queryResultBundle = waitForQueryResultBundle(
          asyncJobToken,
          WebConstants.DATASET_MINIMAL_CROISSANT_TABLE_ID
        );
        if (
          queryResultBundle != null &&
          queryResultBundle.getQueryResult() != null &&
          queryResultBundle.getQueryResult().getQueryResults() != null
        ) {
          RowSet rowSet = queryResultBundle.getQueryResult().getQueryResults();
          List<Row> rows = rowSet.getRows();
          if (rows != null && !rows.isEmpty()) {
            for (Row row : rows) {
              List<String> values = row.getValues();
              String entityId = getRowValue(
                values,
                WebConstants.DATASET_MINIMAL_CROISSANT_DATASET_COLUMN_INDEX
              );
              // check for matching entity id
              if (
                entityId == null ||
                entityId.isEmpty() ||
                !entityId.equals(bundle.getEntity().getId())
              ) {
                continue;
              }
              s3FileURL =
                getRowValue(
                  values,
                  WebConstants.DATASET_MINIMAL_CROISSANT_MINIMAL_CROISSANT_FILE_S3_OBJECT_COLUMN_INDEX
                );
              if (s3FileURL == null || s3FileURL.isEmpty()) {
                continue;
              }
              // read file content from s3FileURL
              String fileContent = getURLContents(s3FileURL, false);
              // append to html
              return fileContent;
            }
          }
        }
      } catch (SynapseException e) {
        log.error("Failed to query dataset croissant file mapping table", e);
      } catch (InterruptedException e) {
        log.warn("Dataset croissant file mapping table query interrupted", e);
        Thread.currentThread().interrupt();
      } catch (IOException e) {
        log.error(
          "Dataset croissant file content read from S3 URL failed: " +
          s3FileURL,
          e
        );
      }
    }
    return "";
  }

  public String getThreadHtml(
    DiscussionThreadBundle thread,
    String threadContent
  )
    throws JSONObjectAdapterException, RestServiceException, IOException, SynapseException {
    StringBuilder html = new StringBuilder();
    html.append("<h4>" + threadContent + "</h4>");
    String createdBy = null;
    try {
      createdBy = getCreatedByString(thread.getCreatedBy());
    } catch (Exception e) {}
    html.append("Created by " + createdBy + "<br>");
    PaginatedResults<DiscussionReplyBundle> replies = convertPaginated(
      synapseClient.getRepliesForThread(
        thread.getId(),
        100L,
        0L,
        DiscussionReplyOrder.CREATED_ON,
        false,
        DiscussionFilter.EXCLUDE_DELETED
      )
    );
    for (DiscussionReplyBundle reply : replies.getResults()) {
      try {
        String replyURL = synapseClient
          .getReplyUrl(reply.getMessageKey())
          .toString();
        html.append(getURLContents(replyURL, true) + "<br>");
      } catch (Exception e) {}
    }
    return html.toString();
  }

  public String getTeamHtml(Team team)
    throws JSONObjectAdapterException, RestServiceException, IOException, SynapseException {
    StringBuilder html = new StringBuilder();

    html.append("<h1>" + team.getName() + "</h1>");
    if (team.getDescription() != null) {
      html.append("<h3>" + team.getDescription() + "</h3>");
    }
    org.sagebionetworks.reflection.model.PaginatedResults<
      TeamMember
    > teamMembers = synapseClient.getTeamMembers(
      team.getId(),
      "",
      TeamMemberTypeFilterOptions.ALL,
      20,
      0
    );
    List<Long> userIds = new ArrayList<Long>();
    for (TeamMember member : teamMembers.getResults()) {
      userIds.add(Long.parseLong(member.getMember().getOwnerId()));
    }
    List<UserProfile> profiles = synapseClient.listUserProfiles(userIds);
    for (UserProfile teamMember : profiles) {
      try {
        html.append(getUserProfileString(teamMember) + "<br>");
      } catch (Exception e) {}
    }
    return html.toString();
  }

  public BotHtml getProfileHtml(UserProfile profile)
    throws JSONObjectAdapterException, RestServiceException, IOException {
    BotHtml response = new BotHtml();
    StringBuilder html = new StringBuilder();
    String display =
      profile.getFirstName() +
      " " +
      profile.getLastName() +
      " " +
      profile.getUserName();

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

    StringBuilder head = new StringBuilder();
    head.append(
      "<meta property=\"profile:first_name\" content=\"" +
      profile.getFirstName() +
      "\">"
    );
    head.append(
      "<meta property=\"profile:last_name\" content=\"" +
      profile.getLastName() +
      "\">"
    );
    head.append(
      "<meta property=\"profile:username\" content=\"" +
      profile.getUserName() +
      "\">"
    );

    response.setHead(head.toString());
    response.setBody(html.toString());

    return response;
  }

  private String getURLContents(String urlTarget, boolean useGzip)
    throws IOException {
    URL url = new URL(urlTarget);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    try {
      conn.setRequestProperty(
        WebConstants.CONTENT_TYPE,
        WebConstants.TEXT_PLAIN_CHARSET_UTF8
      );
      InputStream in = useGzip
        ? new GZIPInputStream(conn.getInputStream())
        : conn.getInputStream();
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
        if (object != null) valueBuilder.append(object.toString() + " ");
      }
    }
    return valueBuilder.toString();
  }

  public String getAllProjectsHtml(SearchQuery inputQuery)
    throws RestServiceException, JSONObjectAdapterException, UnsupportedEncodingException, SynapseException {
    SearchResults results = synapseClient.search(inputQuery);

    // append this set to the list
    StringBuilder html = new StringBuilder();
    for (Hit hit : results.getHits()) {
      // add links
      html.append(
        "<a href=\"https://www.synapse.org/Synapse:" +
        hit.getId() +
        "\">" +
        hit.getName() +
        "</a><br />"
      );
    }
    // add another link for the next page of results
    long newStart = results.getStart() + results.getHits().size();
    inputQuery.setStart(newStart);
    String newJson = EntityFactory.createJSONStringForEntity(inputQuery);
    html.append(
      "<a href=\"https://www.synapse.org/Search:" +
      URLEncoder.encode(newJson, "UTF-8") +
      "\">Next Page</a><br />"
    );
    return html.toString();
  }

  public String getAllTeamsHtml(Integer start)
    throws RestServiceException, SynapseException {
    org.sagebionetworks.reflection.model.PaginatedResults<Team> teams =
      synapseClient.getTeams("", 50, start);
    // append this set to the list
    StringBuilder html = new StringBuilder();

    for (Team team : teams.getResults()) {
      // add links
      html.append(
        "<a href=\"https://www.synapse.org/Team:" +
        team.getId() +
        "\">" +
        team.getName() +
        "</a><br />"
      );
    }
    // add another link for the next page of results
    long newStart = start + teams.getResults().size();
    html.append(
      "<h4><a href=\"https://www.synapse.org/TeamSearch:" +
      TeamSearch.START_DELIMITER +
      newStart +
      "\">Next Page</a></h4><br />"
    );
    return html.toString();
  }

  public EntityChildrenRequest createGetChildrenQuery(String parentId) {
    EntityChildrenRequest newQuery = new EntityChildrenRequest();
    newQuery.setParentId(parentId);
    List<EntityType> types = new ArrayList<EntityType>();
    for (EntityType type : EntityType.values()) {
      if (EntityType.link != type) {
        types.add(type);
      }
    }
    newQuery.setIncludeTypes(types);
    return newQuery;
  }

  /**
   * Helper to convert from the non-gwt compatible PaginatedResults to the compatible type.
   *
   * @param in
   * @return
   */
  public <T extends JSONEntity> PaginatedResults<T> convertPaginated(
    org.sagebionetworks.reflection.model.PaginatedResults<T> in
  ) {
    return new PaginatedResults<T>(
      in.getResults(),
      in.getTotalNumberOfResults()
    );
  }

  // SWC-7552
  public String getDataCatalogHtml() {
    StringBuilder html = new StringBuilder();
    html.append(
      "<h1>" +
      WebConstants.DATA_CATALOG_PAGE_TITLE +
      "</h1>" +
      WebConstants.DATA_CATALOG_PAGE_DESCRIPTION +
      "<br />"
    );
    if (synapseClient != null) {
      try {
        String asyncJobToken = synapseClient.queryTableEntityBundleAsyncStart(
          WebConstants.DATA_CATALOG_CRAWL_RESPONSE_SQL,
          null,
          MAX_QUERY_ROWS,
          QUERY_RESULTS_PART_MASK,
          WebConstants.DATA_CATALOG_TABLE_ID_ON_PRODUCTION
        );
        QueryResultBundle queryResultBundle = waitForQueryResultBundle(
          asyncJobToken,
          WebConstants.DATA_CATALOG_TABLE_ID_ON_PRODUCTION
        );
        if (
          queryResultBundle != null &&
          queryResultBundle.getQueryResult() != null &&
          queryResultBundle.getQueryResult().getQueryResults() != null
        ) {
          RowSet rowSet = queryResultBundle.getQueryResult().getQueryResults();
          List<Row> rows = rowSet.getRows();
          if (rows != null && !rows.isEmpty()) {
            for (Row row : rows) {
              List<String> values = row.getValues();
              String name = getRowValue(
                values,
                WebConstants.DATA_CATALOG_NAME_COLUMN_INDEX
              );
              if (name == null || name.isEmpty()) {
                continue;
              }
              String description = getRowValue(
                values,
                WebConstants.DATA_CATALOG_DESCRIPTION_COLUMN_INDEX
              );
              String link = getRowValue(
                values,
                WebConstants.DATA_CATALOG_LINK_COLUMN_INDEX
              );
              html.append("<div>");
              html.append("<h3>");
              if (link != null && !link.isEmpty()) {
                String escapedLink = escapeHtml(link);
                html.append("<a href=\"");
                html.append(escapedLink);
                html.append("\">");
                html.append(escapeHtml(name));
                html.append("</a>");
              } else {
                html.append(escapeHtml(name));
              }
              html.append("</h3>");
              if (description != null && !description.isEmpty()) {
                html.append(escapeHtml(description));
                html.append("<br />");
              }
              html.append("</div><br />");
            }
          }
        }
      } catch (SynapseException e) {
        log.error("Failed to query data catalog table", e);
      } catch (InterruptedException e) {
        log.warn("Data catalog query interrupted", e);
        Thread.currentThread().interrupt();
      }
    }
    return html.toString();
  }

  private QueryResultBundle waitForQueryResultBundle(
    String asyncJobToken,
    String tableId
  )
    throws SynapseException, SynapseResultNotReadyException, InterruptedException {
    int attempt = 0;
    while (attempt < MAX_ASYNC_QUERY_ATTEMPTS) {
      try {
        return synapseClient.queryTableEntityBundleAsyncGet(
          asyncJobToken,
          tableId
        );
      } catch (SynapseResultNotReadyException e) {
        attempt++;
        if (attempt >= MAX_ASYNC_QUERY_ATTEMPTS) {
          throw e;
        }
        Thread.sleep(ASYNC_QUERY_DELAY_MS);
      }
    }
    return null;
  }

  private static String getRowValue(List<String> values, int index) {
    if (values == null || values.size() <= index) {
      return null;
    }
    return values.get(index);
  }
}
