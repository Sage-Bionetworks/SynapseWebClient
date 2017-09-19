package org.sagebionetworks.web.client;
import static com.google.gwt.http.client.RequestBuilder.GET;
import static com.google.gwt.http.client.RequestBuilder.POST;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_GONE;
import static org.apache.http.HttpStatus.SC_LOCKED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_PRECONDITION_FAILED;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.sagebionetworks.client.exceptions.SynapseTooManyRequestsException.TOO_MANY_REQUESTS_STATUS_CODE;
import static org.sagebionetworks.web.shared.WebConstants.REPO_SERVICE_URL_KEY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.sagebionetworks.repo.model.principal.AliasList;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityIdList;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.IdList;
import org.sagebionetworks.repo.model.PaginatedIds;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.RestrictionInformationRequest;
import org.sagebionetworks.repo.model.RestrictionInformationResponse;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.discussion.CreateDiscussionThread;
import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.EntityThreadCounts;
import org.sagebionetworks.repo.model.discussion.Forum;
import org.sagebionetworks.repo.model.discussion.UpdateThreadMessage;
import org.sagebionetworks.repo.model.discussion.UpdateThreadTitle;
import org.sagebionetworks.repo.model.entity.Direction;
import org.sagebionetworks.repo.model.entity.SortBy;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.repo.model.subscription.SubscriberPagedResults;
import org.sagebionetworks.repo.model.subscription.Topic;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseJavascriptFactory.OBJECT_TYPE;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.ConflictingUpdateException;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.LockedException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.TooManyRequestsException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * Used to call Synapse backend services directly from the client.
 * Reflection is not supported (you can't call Class.newInstance() in this js code), so each method has a construction callback where they create the return object.
 * 
 * @author Jay
 *
 */
public class SynapseJavascriptClient {
	public static final String TYPE_FILTER_PARAMETER = "&typeFilter=";
	public static final String WIKI = "/wiki/";
	public static final String WIKI2 = "/wiki2/";
	public static final String WIKIKEY = "/wikikey";
	public static final String CHILDREN = "/children";
	public static final String RESTRICTION_INFORMATION = "/restrictionInformation";
	public static final String USER_PROFILE_PATH = "/userProfile";
	public static final String USER_GROUP_HEADER_BY_ALIAS = "/userGroupHeaders/aliases";
	public static final String USER_GROUP_HEADER_BATCH_PATH = "/userGroupHeaders/batch?ids=";
	private static final String ENTITY = "/entity";
	private static final String PROJECT = "/project";
	private static final String FORUM = "/forum";
	private static final String THREAD = "/thread";
	private static final String THREADS = "/threads";
	private static final String THREAD_COUNT = "/threadcount";
	private static final String THREAD_TITLE = "/title";
	private static final String DISCUSSION_MESSAGE = "/message";
	private static final String REPLY = "/reply";
	private static final String REPLIES = "/replies";
	private static final String REPLY_COUNT = "/replycount";
	private static final String URL = "/messageUrl";
	private static final String PIN = "/pin";
	private static final String UNPIN = "/unpin";
	private static final String RESTORE = "/restore";
	private static final String MODERATORS = "/moderators";
	private static final String SUBSCRIPTION = "/subscription";
	
	private static final String THREAD_COUNTS = "/threadcounts";
	private static final String ENTITY_THREAD_COUNTS = ENTITY + THREAD_COUNTS;
	
	public static final int RETRY_REQUEST_DELAY_MS = 2000;
	RequestBuilderWrapper requestBuilder;
	AuthenticationController authController;
	JSONObjectAdapter jsonObjectAdapter;
	GlobalApplicationState globalAppState;
	GWTWrapper gwt;
	SynapseJavascriptFactory jsFactory;

	public static final String ENTITY_URI_PATH = "/entity";
	public static final String USER = "/user";
	public static final String BUNDLE_MASK_PATH = "/bundle?mask=";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String ACCEPT = "Accept";
	public static final String SESSION_TOKEN_HEADER = "sessionToken";
	public static final String USER_AGENT = "User-Agent";
	public static final String SYNAPSE_ENCODING_CHARSET = "UTF-8";
	public static final String APPLICATION_JSON_CHARSET_UTF8 = "application/json; charset="+SYNAPSE_ENCODING_CHARSET;
	public static final String REPO_SUFFIX_VERSION = "/version";
	public static final String TEAM = "/team";
	public static final String WIKI_VERSION_PARAMETER = "?wikiVersion=";
	public static final String FAVORITE_URI_PATH = "/favorite";
	public static final String USER_GROUP_HEADER_PREFIX_PATH = "/userGroupHeaders?prefix=";
	
	public static final String MEMBERSHIP_REQUEST = "/membershipRequest";
	public static final String OPEN_MEMBERSHIP_REQUEST_COUNT = MEMBERSHIP_REQUEST + "/openRequestCount";
	
	public static final String MEMBERSHIP_INVITATION = "/membershipInvitation";
	public static final String OPEN_MEMBERSHIP_INVITATION_COUNT = MEMBERSHIP_INVITATION + "/openInvitationCount";
	
	public static final String OFFSET_PARAMETER = "offset=";
	public static final String LIMIT_PARAMETER = "limit=";
	private static final String NEXT_PAGE_TOKEN_PARAM = "nextPageToken=";
	
	public String repoServiceUrl; 
	
	@Inject
	public SynapseJavascriptClient(
			RequestBuilderWrapper requestBuilder,
			AuthenticationController authController,
			JSONObjectAdapter jsonObjectAdapter,
			GlobalApplicationState globalAppState,
			GWTWrapper gwt,
			SynapseJavascriptFactory jsFactory) {
		this.requestBuilder = requestBuilder;
		this.authController = authController;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.globalAppState = globalAppState;
		this.gwt = gwt;
		this.jsFactory = jsFactory;
	}
	private String getRepoServiceUrl() {
		if (repoServiceUrl == null) {
			repoServiceUrl = globalAppState.getSynapseProperty(REPO_SERVICE_URL_KEY);
		}
		return repoServiceUrl;
	}
	private void doGet(String url, OBJECT_TYPE responseType, AsyncCallback callback) {
		requestBuilder.configure(GET, url);
		requestBuilder.setHeader(ACCEPT, APPLICATION_JSON_CHARSET_UTF8);
		if (authController.isLoggedIn()) {
			requestBuilder.setHeader(SESSION_TOKEN_HEADER, authController.getCurrentUserSessionToken());
		}
		sendRequest(url, null, responseType, callback);
	}
	
	private void doPost(String url, String requestData, OBJECT_TYPE responseType, AsyncCallback callback) {
		requestBuilder.configure(POST, url);
		requestBuilder.setHeader(ACCEPT, APPLICATION_JSON_CHARSET_UTF8);
		requestBuilder.setHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF8);
		if (authController.isLoggedIn()) {
			requestBuilder.setHeader(SESSION_TOKEN_HEADER, authController.getCurrentUserSessionToken());
		}
		sendRequest(url, requestData, responseType, callback);
	}
	
	private void sendRequest(final String url, final String requestData, final OBJECT_TYPE responseType, final AsyncCallback callback) {
		try {
			requestBuilder.sendRequest(requestData, new RequestCallback() {
				@Override
				public void onResponseReceived(Request request,
						Response response) {
					int statusCode = response.getStatusCode();
					if (statusCode == SC_OK) {
						try {
							JSONObjectAdapter jsonObject = jsonObjectAdapter.createNew(response.getText());
							callback.onSuccess(jsFactory.newInstance(responseType, jsonObject));
						} catch (JSONObjectAdapterException e) {
							onError(null, e);
						}
					} else {
						if (statusCode == TOO_MANY_REQUESTS_STATUS_CODE) {
							// wait a couple of seconds and try the request again...
							gwt.scheduleExecution(new Callback() {
								@Override
								public void invoke() {
									sendRequest(url, requestData, responseType, callback);
								}
							}, RETRY_REQUEST_DELAY_MS);
						} else {
							// getException() based on status code, 
							// instead of using org.sagebionetworks.client.ClientUtils.throwException() and ExceptionUtil.convertSynapseException() (neither of which can be referenced here)
							onError(request, getException(statusCode, response.getStatusText()));
						}
					}
				}

				@Override
				public void onError(Request request, Throwable exception) {
					callback.onFailure(exception);
				}
			});
		} catch (final Exception e) {
			callback.onFailure(e);
		}
	}
	
	public static RestServiceException getException(int statusCode, String reasonStr) {
		if (statusCode == SC_UNAUTHORIZED) {
			return new UnauthorizedException(reasonStr);
		} else if (statusCode == SC_FORBIDDEN) {
			return new ForbiddenException(reasonStr);
		} else if (statusCode == SC_NOT_FOUND) {
			return new NotFoundException(reasonStr);
		} else if (statusCode == SC_BAD_REQUEST) {
			return new BadRequestException(reasonStr);
		} else if (statusCode == SC_LOCKED) {
			return new LockedException(reasonStr);
		} else if (statusCode == SC_PRECONDITION_FAILED) {
			return new ConflictingUpdateException(reasonStr);
		} else if (statusCode == SC_GONE) {
			return new BadRequestException(reasonStr);
		} else if (statusCode == TOO_MANY_REQUESTS_STATUS_CODE){
			return new TooManyRequestsException(reasonStr);
		}else {
			return new UnknownErrorException(reasonStr);
		}
	}

	public void getEntityBundle(String entityId, int partsMask, final AsyncCallback<EntityBundle> callback) {
		getEntityBundleForVersion(entityId, null, partsMask, callback);
	}

	public void getEntityBundleForVersion(String entityId, Long versionNumber, int partsMask, final AsyncCallback<EntityBundle> callback) {
		String url = getRepoServiceUrl() + ENTITY_URI_PATH + "/" + entityId;
		if (versionNumber != null) {
			url += REPO_SUFFIX_VERSION + "/" + versionNumber;
		}
		url += BUNDLE_MASK_PATH + partsMask;
		doGet(url, OBJECT_TYPE.EntityBundle, callback);
	}

	public void getTeam(String teamId, final AsyncCallback<Team> callback) {
		String url = getRepoServiceUrl() + TEAM + "/" + teamId;
		doGet(url, OBJECT_TYPE.Team, callback);
	}
	
	public void getRestrictionInformation(String subjectId, RestrictableObjectType type, final AsyncCallback<RestrictionInformationResponse> callback)  {
		String url = getRepoServiceUrl() + RESTRICTION_INFORMATION;
		RestrictionInformationRequest request = new RestrictionInformationRequest();
		request.setObjectId(subjectId);
		request.setRestrictableObjectType(type);
		try {
			JSONObjectAdapter jsonAdapter = jsonObjectAdapter.createNew();
			request.writeToJSONObject(jsonAdapter);
			doPost(url, jsonAdapter.toJSONString(), OBJECT_TYPE.RestrictionInformationResponse, callback);
		} catch (JSONObjectAdapterException e) {
			callback.onFailure(e);
		}
	}
	
	public void getEntityChildren(EntityChildrenRequest request, final AsyncCallback<EntityChildrenResponse> callback) {
		String url = getRepoServiceUrl() + ENTITY_URI_PATH + CHILDREN;
		try {
			JSONObjectAdapter jsonAdapter = jsonObjectAdapter.createNew();
			request.writeToJSONObject(jsonAdapter);
			doPost(url, jsonAdapter.toJSONString(), OBJECT_TYPE.EntityChildrenResponse, callback);
		} catch (JSONObjectAdapterException e) {
			callback.onFailure(e);
		}
	}
	
	public void getV2WikiPageAsV1(WikiPageKey key, AsyncCallback<WikiPage> callback) {
		getVersionOfV2WikiPageAsV1(key, null, callback);
	}
	
	public void getVersionOfV2WikiPageAsV1(final WikiPageKey key, final Long versionNumber, final AsyncCallback<WikiPage> callback) {
		if (key.getWikiPageId() == null) {
			AsyncCallback<String> wikiPageIdKeyCallback = new AsyncCallback<String>() {
				@Override
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
				@Override
				public void onSuccess(String wikiPageIdKey) {
					key.setWikiPageId(wikiPageIdKey);
					getVersionOfV2WikiPageAsV1WithWikiPageId(key, versionNumber, callback);
				}
			};
			getRootWikiPageKey(key.getOwnerObjectType(), key.getOwnerObjectId(), wikiPageIdKeyCallback);
		} else {
			getVersionOfV2WikiPageAsV1WithWikiPageId(key, versionNumber, callback);
		}
	}
	
	public void getV2WikiPage(final WikiPageKey key, AsyncCallback<V2WikiPage> callback) {
		getV2WikiPage(key, null, callback);
	}
	
	public void getV2WikiPage(final WikiPageKey key, final Long versionNumber, final AsyncCallback<V2WikiPage> callback) {
		if (key.getWikiPageId() == null) {
			AsyncCallback<String> wikiPageIdKeyCallback = new AsyncCallback<String>() {
				@Override
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
				@Override
				public void onSuccess(String wikiPageIdKey) {
					key.setWikiPageId(wikiPageIdKey);
					getV2WikiPageWithPageId(key, versionNumber, callback);
				}
			};
			getRootWikiPageKey(key.getOwnerObjectType(), key.getOwnerObjectId(), wikiPageIdKeyCallback);
		} else {
			getV2WikiPageWithPageId(key, versionNumber, callback);
		}
	}
	
	private void getV2WikiPageWithPageId(WikiPageKey key, Long versionNumber, final AsyncCallback<V2WikiPage> callback) {
		String url = getRepoServiceUrl() + "/" +
				key.getOwnerObjectType().toLowerCase() + "/" + 
				key.getOwnerObjectId() + WIKI2 +
				key.getWikiPageId();
		if (versionNumber != null) {
			url += WIKI_VERSION_PARAMETER + versionNumber;
		}
		doGet(url, OBJECT_TYPE.V2WikiPage, callback);
	}
	
	public void getRootWikiPageKey(String ownerObjectType, String ownerObjectId, final AsyncCallback<String> wikiPageIdKeyCallback) {
		// get the root wiki page id first
		String url = getRepoServiceUrl() + "/" +
				ownerObjectType.toLowerCase() + "/" + 
				ownerObjectId + WIKIKEY;
		
		AsyncCallback<org.sagebionetworks.repo.model.dao.WikiPageKey> wikiPageKeyCallback = new AsyncCallback<org.sagebionetworks.repo.model.dao.WikiPageKey>() {
			@Override
			public void onFailure(Throwable caught) {
				wikiPageIdKeyCallback.onFailure(caught);
			}
			@Override
			public void onSuccess(org.sagebionetworks.repo.model.dao.WikiPageKey wikiPageKey) {
				wikiPageIdKeyCallback.onSuccess(wikiPageKey.getWikiPageId());
			}
		};
		
		doGet(url, OBJECT_TYPE.WikiPageKey, wikiPageKeyCallback);
	}
	
	private void getVersionOfV2WikiPageAsV1WithWikiPageId(WikiPageKey key, Long versionNumber, final AsyncCallback<WikiPage> callback) {
		String url = getRepoServiceUrl() + "/" +
				key.getOwnerObjectType().toLowerCase() + "/" + 
				key.getOwnerObjectId() + WIKI +
				key.getWikiPageId();
		if (versionNumber != null) {
			url += WIKI_VERSION_PARAMETER + versionNumber;
		}
		doGet(url, OBJECT_TYPE.WikiPage, callback);
	}
	

	public void getUserGroupHeadersByPrefix(String prefix, TypeFilter type, long limit, long offset, final AsyncCallback<UserGroupHeaderResponsePage> callback) {
		String encodedPrefix = gwt.encodeQueryString(prefix);
		StringBuilder builder = new StringBuilder();
		builder.append(getRepoServiceUrl());
		builder.append(USER_GROUP_HEADER_PREFIX_PATH);
		builder.append(encodedPrefix);
		builder.append("&" + LIMIT_PARAMETER + limit);
		builder.append( "&" + OFFSET_PARAMETER + offset);
		if(type != null){
			builder.append(TYPE_FILTER_PARAMETER+type.name());
		}
		
		doGet(builder.toString(), OBJECT_TYPE.UserGroupHeaderResponsePage, callback);
	}

	public void listUserProfiles(List<String> userIds, final AsyncCallback<List<UserProfile>> callback) {
		List<Long> userIdsLong = new ArrayList<>();
		for (String userId : userIds) {
			userIdsLong.add(Long.parseLong(userId));
		}
		listUserProfilesFromUserIds(userIdsLong, callback);
	}

	private void listUserProfilesFromUserIds(List<Long> userIds, final AsyncCallback<List<UserProfile>> callback) {
		String url = getRepoServiceUrl() + USER_PROFILE_PATH;
		try {
			IdList idList = new IdList();
			idList.setList(userIds);
			JSONObjectAdapter jsonAdapter = jsonObjectAdapter.createNew();
			idList.writeToJSONObject(jsonAdapter);
			doPost(url, jsonAdapter.toJSONString(), OBJECT_TYPE.ListWrapperUserProfile, callback);
		} catch (JSONObjectAdapterException e) {
			callback.onFailure(e);
		}
	}
	
	public void getFavorites(final AsyncCallback<List<EntityHeader>> callback) {
		String url = getRepoServiceUrl() +
				FAVORITE_URI_PATH + "?" + OFFSET_PARAMETER + "0"
				+ "&" +LIMIT_PARAMETER+"200";
		AsyncCallback<List<EntityHeader>> paginatedResultsCallback = new AsyncCallback<List<EntityHeader>>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
			public void onSuccess(List<EntityHeader> results) {
				//sort by name
				Collections.sort(results, new Comparator<EntityHeader>() {
			        @Override
			        public int compare(EntityHeader o1, EntityHeader o2) {
			        	return o1.getName().compareToIgnoreCase(o2.getName());
			        }
				});
				callback.onSuccess(results);
			};
		};
		doGet(url, OBJECT_TYPE.PaginatedResultsEntityHeader, paginatedResultsCallback);
	}
	
	public void getUserBundle(Long principalId, int mask, AsyncCallback<UserBundle> callback) {
		String url = getRepoServiceUrl() + USER + "/" + principalId + BUNDLE_MASK_PATH + mask;
		doGet(url, OBJECT_TYPE.UserBundle, callback);
	}
	public void getOpenMembershipInvitationCount(AsyncCallback<Long> callback) {
		String url = getRepoServiceUrl() + OPEN_MEMBERSHIP_INVITATION_COUNT;
		doGet(url, OBJECT_TYPE.Count, callback);
	}

	public void getOpenMembershipRequestCount(AsyncCallback<Long> callback) {
		String url = getRepoServiceUrl() + OPEN_MEMBERSHIP_REQUEST_COUNT;
		doGet(url, OBJECT_TYPE.Count, callback);
	}

	public void getUserGroupHeadersByAlias(
			ArrayList<String> aliases,
			AsyncCallback<List<UserGroupHeader>> callback) {
		String url = getRepoServiceUrl() + USER_GROUP_HEADER_BY_ALIAS;
		try {
			AliasList list = new AliasList();
			list.setList(aliases);
			JSONObjectAdapter jsonAdapter = jsonObjectAdapter.createNew();
			list.writeToJSONObject(jsonAdapter);
			doPost(url, jsonAdapter.toJSONString(), OBJECT_TYPE.UserGroupHeaderResponse, callback);
		} catch (JSONObjectAdapterException e) {
			callback.onFailure(e);
		}
	}
	public void getUserGroupHeadersById(
			List<String> ids,
			AsyncCallback<UserGroupHeaderResponsePage> callback) {
		String url = getRepoServiceUrl() + USER_GROUP_HEADER_BATCH_PATH + listToString(ids);
		doGet(url, OBJECT_TYPE.UserGroupHeaderResponsePage, callback);
	}
	
	private String listToString(List<String> ids) {
		StringBuilder sb = new StringBuilder();
		for (String id : ids) {
			sb.append(id);
			sb.append(',');
		}
		// Remove the trailing comma
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
	

	public void getEntity(String entityId, OBJECT_TYPE type, AsyncCallback<Entity> callback) {
		getEntityByID(entityId, type, null, callback);
	}
	
	public void getEntity(String entityId, AsyncCallback<Entity> callback) {
		getEntityByID(entityId, OBJECT_TYPE.Entity, null, callback);
	}
	
	public void getEntityForVersion(String entityId, Long versionNumber, AsyncCallback<Entity> callback) {
		getEntityByID(entityId, OBJECT_TYPE.Entity, null, callback);
	}
	
	private void getEntityByID(String entityId, OBJECT_TYPE type, Long versionNumber, AsyncCallback<Entity> callback) {
		String url = getRepoServiceUrl() + ENTITY_URI_PATH + "/" + entityId;
		if (versionNumber != null) {
			url += REPO_SUFFIX_VERSION + "/" + versionNumber;
		}
		doGet(url, type , callback);
	}
	
	public void isDocker(String projectId, AsyncCallback<Boolean> callback) {
		EntityChildrenRequest request = getEntityChildrenRequest(projectId, EntityType.dockerrepo);
		getEntityChildren(request, getEntityChildrenExistCallback(callback));
	}
	
	public void isFileOrFolder(String projectId, AsyncCallback<Boolean> callback) {
		EntityChildrenRequest request = getEntityChildrenRequest(projectId, EntityType.file, EntityType.folder);
		getEntityChildren(request, getEntityChildrenExistCallback(callback));
	}
	
	public void isTable(String projectId, AsyncCallback<Boolean> callback) {
		EntityChildrenRequest request = getEntityChildrenRequest(projectId, EntityType.table, EntityType.entityview);
		getEntityChildren(request, getEntityChildrenExistCallback(callback));
	}
			
	private AsyncCallback<EntityChildrenResponse> getEntityChildrenExistCallback(final AsyncCallback<Boolean> callback) {
		return new AsyncCallback<EntityChildrenResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
			@Override
			public void onSuccess(EntityChildrenResponse result) {
				callback.onSuccess(!result.getPage().isEmpty());
			}
		};
	}
	
	private EntityChildrenRequest getEntityChildrenRequest(String parentId, EntityType... types) {
		EntityChildrenRequest request = new EntityChildrenRequest();
		request.setNextPageToken(null);
		request.setParentId(parentId);
		request.setSortBy(SortBy.NAME);
		request.setSortDirection(Direction.ASC);
		List<EntityType> includeTypes = new ArrayList<EntityType>();
		for (int i = 0; i < types.length; i++) {
			includeTypes.add(types[i]);
		}
		request.setIncludeTypes(includeTypes);
		return request;
	}
	
	public void getForumByProjectId(String projectId, AsyncCallback<Forum> callback) {
		String url = getRepoServiceUrl() + PROJECT+"/"+projectId+FORUM;
		doGet(url, OBJECT_TYPE.Forum, callback);
	}
	
	public void getThread(String threadId, AsyncCallback<DiscussionThreadBundle> callback) {
		String url = getRepoServiceUrl() + THREAD+"/"+threadId;
		doGet(url, OBJECT_TYPE.DiscussionThreadBundle, callback);
	}
	
	public void getReply(String replyId, AsyncCallback<DiscussionReplyBundle> callback) {
		String url = getRepoServiceUrl() + REPLY+"/"+replyId;
		doGet(url, OBJECT_TYPE.DiscussionReplyBundle, callback);
	}
	public void getThreadUrl(String messageKey, AsyncCallback<String> callback) {
		String url = getRepoServiceUrl() + THREAD+URL+"?messageKey="+messageKey;
		doGet(url, OBJECT_TYPE.MessageURL, callback);
	}
	public void getReplyUrl(String messageKey, AsyncCallback<String> callback) {
		String url = getRepoServiceUrl() + REPLY+URL+"?messageKey="+messageKey;
		doGet(url, OBJECT_TYPE.MessageURL, callback);
	}
	
	public void getThreadCountForForum(String forumId, DiscussionFilter filter, AsyncCallback<Long> callback) {
		String url = getRepoServiceUrl() + FORUM+"/"+forumId+THREAD_COUNT + "?filter="+filter;
		doGet(url, OBJECT_TYPE.ThreadCount, callback);
	}

	public void getReplyCountForThread(String threadId, DiscussionFilter filter, AsyncCallback<Long> callback) {
		String url = getRepoServiceUrl() + THREAD+"/"+threadId+REPLY_COUNT + "?filter="+filter;
		doGet(url, OBJECT_TYPE.ThreadCount, callback);
	}
	
	public void getEntityThreadCount(List<String> entityIds, AsyncCallback<EntityThreadCounts> callback) {
		String url = getRepoServiceUrl() + ENTITY_THREAD_COUNTS;
		EntityIdList idList = new EntityIdList();
		idList.setIdList(entityIds);
		try {
			JSONObjectAdapter jsonAdapter = jsonObjectAdapter.createNew();
			idList.writeToJSONObject(jsonAdapter);
			doPost(url, jsonAdapter.toJSONString(), OBJECT_TYPE.EntityThreadCounts, callback);
		} catch (JSONObjectAdapterException e) {
			callback.onFailure(e);
		}
	}
	
	public void getModerators(String forumId, Long limit, Long offset, AsyncCallback<PaginatedIds> callback) {
		String url = getRepoServiceUrl() + FORUM+"/"+forumId+MODERATORS+"?"+LIMIT_PARAMETER+limit+"&"+OFFSET_PARAMETER+offset;
		doGet(url, OBJECT_TYPE.PaginatedIds, callback);
	}

	public void getSubscribers(Topic topic, String nextPageToken, AsyncCallback<SubscriberPagedResults> callback) {
		String url = getRepoServiceUrl() + SUBSCRIPTION+"/subscribers";
		if (nextPageToken != null) {
			url += "?" + NEXT_PAGE_TOKEN_PARAM + nextPageToken;
		}
		try {
			JSONObjectAdapter jsonAdapter = jsonObjectAdapter.createNew();
			topic.writeToJSONObject(jsonAdapter);
			doPost(url, jsonAdapter.toJSONString(), OBJECT_TYPE.SubscriberPagedResults, callback);
		} catch (JSONObjectAdapterException e) {
			callback.onFailure(e);
		}
	}

	public void getSubscribersCount(Topic topic, AsyncCallback<Long> callback) {
		String url = getRepoServiceUrl() + SUBSCRIPTION+"/subscribers/count";
		try {
			JSONObjectAdapter jsonAdapter = jsonObjectAdapter.createNew();
			topic.writeToJSONObject(jsonAdapter);
			doPost(url, jsonAdapter.toJSONString(), OBJECT_TYPE.SubscriberCount, callback);
		} catch (JSONObjectAdapterException e) {
			callback.onFailure(e);
		}
	}
}

