package org.sagebionetworks.web.client;

import static com.google.gwt.http.client.RequestBuilder.DELETE;
import static com.google.gwt.http.client.RequestBuilder.GET;
import static com.google.gwt.http.client.RequestBuilder.POST;
import static com.google.gwt.http.client.RequestBuilder.PUT;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_GONE;
import static org.apache.http.HttpStatus.SC_LOCKED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_PRECONDITION_FAILED;
import static org.apache.http.HttpStatus.SC_SERVICE_UNAVAILABLE;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.sagebionetworks.client.exceptions.SynapseTooManyRequestsException.TOO_MANY_REQUESTS_STATUS_CODE;
import static org.sagebionetworks.web.client.utils.FutureUtils.getFuture;
import static org.sagebionetworks.web.shared.WebConstants.AUTH_PUBLIC_SERVICE_URL_KEY;
import static org.sagebionetworks.web.shared.WebConstants.CONTENT_TYPE;
import static org.sagebionetworks.web.shared.WebConstants.FILE_SERVICE_URL_KEY;
import static org.sagebionetworks.web.shared.WebConstants.REPO_SERVICE_URL_KEY;
import static org.sagebionetworks.web.shared.WebConstants.SYNAPSE_VERSION_KEY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.ErrorResponseCode;
import org.sagebionetworks.repo.model.IdList;
import org.sagebionetworks.repo.model.InviteeVerificationSignedToken;
import org.sagebionetworks.repo.model.LogEntry;
import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.repo.model.MembershipInvtnSignedToken;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.PaginatedIds;
import org.sagebionetworks.repo.model.PaginatedTeamIds;
import org.sagebionetworks.repo.model.ProjectHeader;
import org.sagebionetworks.repo.model.ProjectListSortColumn;
import org.sagebionetworks.repo.model.ProjectListType;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.RestrictionInformationRequest;
import org.sagebionetworks.repo.model.RestrictionInformationResponse;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMember;
import org.sagebionetworks.repo.model.TeamMemberTypeFilterOptions;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.annotation.v2.Annotations;
import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;
import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.auth.ChangePasswordInterface;
import org.sagebionetworks.repo.model.auth.LoginRequest;
import org.sagebionetworks.repo.model.auth.LoginResponse;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.repo.model.auth.Username;
import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyOrder;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadOrder;
import org.sagebionetworks.repo.model.discussion.Forum;
import org.sagebionetworks.repo.model.docker.DockerCommit;
import org.sagebionetworks.repo.model.docker.DockerCommitSortBy;
import org.sagebionetworks.repo.model.doi.v2.Doi;
import org.sagebionetworks.repo.model.doi.v2.DoiAssociation;
import org.sagebionetworks.repo.model.entity.Direction;
import org.sagebionetworks.repo.model.entity.EntityLookupRequest;
import org.sagebionetworks.repo.model.entity.SortBy;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.repo.model.file.AddPartResponse;
import org.sagebionetworks.repo.model.file.BatchFileRequest;
import org.sagebionetworks.repo.model.file.BatchFileResult;
import org.sagebionetworks.repo.model.file.BatchPresignedUploadUrlRequest;
import org.sagebionetworks.repo.model.file.BatchPresignedUploadUrlResponse;
import org.sagebionetworks.repo.model.file.DownloadList;
import org.sagebionetworks.repo.model.file.DownloadOrder;
import org.sagebionetworks.repo.model.file.DownloadOrderSummaryRequest;
import org.sagebionetworks.repo.model.file.DownloadOrderSummaryResponse;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileHandleAssociationList;
import org.sagebionetworks.repo.model.file.MultipartUploadRequest;
import org.sagebionetworks.repo.model.file.MultipartUploadStatus;
import org.sagebionetworks.repo.model.file.UploadDestination;
import org.sagebionetworks.repo.model.oauth.OAuthProvider;
import org.sagebionetworks.repo.model.principal.AliasList;
import org.sagebionetworks.repo.model.principal.NotificationEmail;
import org.sagebionetworks.repo.model.principal.PrincipalAliasRequest;
import org.sagebionetworks.repo.model.principal.PrincipalAliasResponse;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.repo.model.subscription.SortByType;
import org.sagebionetworks.repo.model.subscription.SubscriberPagedResults;
import org.sagebionetworks.repo.model.subscription.Subscription;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.repo.model.subscription.SubscriptionPagedResults;
import org.sagebionetworks.repo.model.subscription.SubscriptionRequest;
import org.sagebionetworks.repo.model.subscription.Topic;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.SnapshotRequest;
import org.sagebionetworks.repo.model.table.SnapshotResponse;
import org.sagebionetworks.repo.model.table.ViewType;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiOrderHint;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseJavascriptFactory.OBJECT_TYPE;
import org.sagebionetworks.web.client.cache.EntityId2BundleCache;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.EntityPageTop;
import org.sagebionetworks.web.shared.TeamMemberBundle;
import org.sagebionetworks.web.shared.TeamMemberPagedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.ConflictException;
import org.sagebionetworks.web.shared.exceptions.ConflictingUpdateException;
import org.sagebionetworks.web.shared.exceptions.DeprecatedServiceException;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.LockedException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.ResultNotReadyException;
import org.sagebionetworks.web.shared.exceptions.SynapseDownException;
import org.sagebionetworks.web.shared.exceptions.TooManyRequestsException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.common.base.Joiner;
import com.google.common.util.concurrent.FluentFuture;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
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
	public static final String BUNDLE2 = "/bundle2";
	public static final String TABLE_SNAPSHOT = "/table/snapshot";
	public static final String SESSION = "/session";
	public static final String TYPE_FILTER_PARAMETER = "&typeFilter=";
	public static final String CHALLENGE = "/challenge";
	public static final String WIKI = "/wiki/";
	public static final String WIKI2 = "/wiki2/";
	public static final String WIKIKEY = "/wikikey";
	public static final String WIKI_ORDER_HINT = "/wiki2orderhint";
	public static final String WIKI_HEADER_TREE = "/wikiheadertree2";
	public static final String CHILDREN = "/children";
	public static final String RESTRICTION_INFORMATION = "/restrictionInformation";
	public static final String USER_PROFILE_PATH = "/userProfile";
	public static final String USER_GROUP_HEADER_BY_ALIAS = "/userGroupHeaders/aliases";
	public static final String USER_GROUP_HEADER_BATCH_PATH = "/userGroupHeaders/batch?ids=";
	public static final String ENTITY = "/entity";
	public static final String PROJECT = "/project";
	public static final String FORUM = "/forum";
	public static final String THREAD = "/thread";
	public static final String THREADS = "/threads";
	public static final String REPLIES = "/replies";
	public static final String THREAD_COUNT = "/threadcount";
	public static final String REPLY = "/reply";
	public static final String REPLY_COUNT = "/replycount";
	public static final String URL = "/messageUrl";
	public static final String MODERATORS = "/moderators";
	public static final String SUBSCRIPTION = "/subscription";
	public static final String ALL = "/all";
	public static final String FILE_HANDLE_BATCH = "/fileHandle/batch";
	public static final String THREAD_COUNTS = "/threadcounts";
	public static final String ATTACHMENT_HANDLES = "attachmenthandles";
	private static final String PROFILE_IMAGE = "/image";
	public static final String OBJECT = "/object";
	public static final String ETAG = "etag";
	public static final String GENERATED_PATH = "/generated";
	public static final String GENERATED_BY_SUFFIX = "/generatedBy";
	public static final String OPEN_MEMBERSHIP_REQUEST = "/openRequest";
	public static final String UPLOAD_DESTINATIONS = "/uploadDestinations";
	public static final String PRINCIPAL = "/principal";
	public static final String DOI = "/doi";
	public static final String DOI_ASSOCIATION = DOI + "/association";
	public static final String ID_PARAMETER = "id=";
	public static final String TYPE_PARAMETER = "type=";
	public static final String VERSION_PARAMETER = "version=";
	public static final int INITIAL_RETRY_REQUEST_DELAY_MS = 1000;
	public static final int MAX_LOG_ENTRY_LABEL_SIZE = 200;
	private static final String LOG = "/log";
	AuthenticationController authController;
	JSONObjectAdapter jsonObjectAdapter;
	GWTWrapper gwt;
	SynapseJavascriptFactory jsFactory;
	PortalGinInjector ginInjector;
	SynapseJSNIUtils jsniUtils;
	SynapseProperties synapseProperties;
	EntityId2BundleCache entityIdBundleCache;

	public static final String USER = "/user";
	public static final String BUNDLE_MASK_PATH = "/bundle?mask=";
	public static final String ACCEPT = "Accept";
	public static final String SESSION_TOKEN_HEADER = "sessionToken";
	public static final String SYNAPSE_ENCODING_CHARSET = "UTF-8";
	public static final String APPLICATION_JSON_CHARSET_UTF8 = "application/json; charset="+SYNAPSE_ENCODING_CHARSET;
	public static final String REPO_SUFFIX_VERSION = "/version";
	public static final String TEAM = "/team";
	public static final String MEMBER = "/member";
	public static final String WIKI_VERSION_PARAMETER = "?wikiVersion=";
	public static final String FAVORITE_URI_PATH = "/favorite";
	public static final String USER_GROUP_HEADER_PREFIX_PATH = "/userGroupHeaders?prefix=";
	
	public static final String MEMBERSHIP_REQUEST = "/membershipRequest";
	public static final String OPEN_MEMBERSHIP_REQUEST_COUNT = MEMBERSHIP_REQUEST + "/openRequestCount";
	
	public static final String MEMBERSHIP_INVITATION = "/membershipInvitation";
	public static final String OPEN_MEMBERSHIP_INVITATION_COUNT = MEMBERSHIP_INVITATION + "/openInvitationCount";
	public static final String INVITEE_VERIFICATION_SIGNED_TOKEN = "/inviteeVerificationSignedToken";
	public static final String INVITEE_ID = "/inviteeId";
	public static final String ICON = "/icon";
	public static final String PROJECTS_URI_PATH = "/projects";
	public static final String DOCKER_TAG = "/dockerTag";
	public static final String OFFSET_PARAMETER = "offset=";
	public static final String LIMIT_PARAMETER = "limit=";
	public static final String OBJECT_TYPE_PARAMETER = "objectType=";
	private static final String NEXT_PAGE_TOKEN_PARAM = "nextPageToken=";
	public static final String SKIP_TRASH_CAN_PARAM = "skipTrashCan";
	private static final String ASCENDING_PARAM = "ascending=";
	
	public static final String USER_PASSWORD_RESET = "/user/password/reset";
	public static final String USER_CHANGE_PASSWORD = "/user/changePassword";
	
	public static final String COLUMN = "/column";
	public static final String COLUMN_VIEW_DEFAULT = COLUMN + "/tableview/defaults/";
	public static final String TABLE = "/table";
	public static final String TABLE_QUERY = TABLE + "/query";
	public static final String TABLE_QUERY_NEXTPAGE = TABLE_QUERY + "/nextPage";
	public static final String TABLE_DOWNLOAD_CSV = TABLE + "/download/csv";
	public static final String TABLE_UPLOAD_CSV = TABLE + "/upload/csv";
	public static final String TABLE_UPLOAD_CSV_PREVIEW = TABLE + "/upload/csv/preview";
	public static final String TABLE_APPEND = TABLE + "/append";
	public static final String TABLE_TRANSACTION = TABLE+"/transaction";
	public static final String FILE = "/file";
	public static final String FILE_BULK = FILE+"/bulk";
	public static final String ACTIVITY_URI_PATH = "/activity";
	
	public static final String ASYNC_START = "/async/start";
	public static final String ASYNC_GET = "/async/get/";
	public static final String AUTH_OAUTH_2 = "/oauth2";
	public static final String AUTH_OAUTH_2_ALIAS = AUTH_OAUTH_2+"/alias";
	public static final String DOWNLOAD_LIST = "/download/list";
	public static final String DOWNLOAD_LIST_ADD = DOWNLOAD_LIST+"/add";
	public static final String DOWNLOAD_LIST_REMOVE = DOWNLOAD_LIST+"/remove";
	public static final String DOWNLOAD_LIST_CLEAR = DOWNLOAD_LIST+"/clear";
	
	public static final String DOWNLOAD_ORDER = "/download/order";
	public static final String DOWNLOAD_ORDER_HISTORY = DOWNLOAD_ORDER+"/history";
	public static final String STORAGE_REPORT = "/storageReport";
	public static final String SEARCH = "/search";
	public static final String TEAM_MEMBERS = "/teamMembers/";
	public static final String NAME_FRAGMENT_FILTER = "fragment=";
	public static final String NAME_MEMBERTYPE_FILTER = "memberType=";
	public static final String VERSION_INFO = "/version";
	public static final String FILE_PREVIEW = "/filepreview";
	public static final String REDIRECT_PARAMETER = "redirect=";
	public static final String ACCOUNT = "/account";
	public static final String EMAIL_VALIDATION = "/emailValidation";
	public static final String PORTAL_ENDPOINT_PARAM = "portalEndpoint=";
	public static final int LIMIT_50 = 50;
	
	public Map<String, List<Request>> requestsMap;
	
	public String repoServiceUrl,fileServiceUrl, authServiceUrl, synapseVersionInfo; 
	@Inject
	public SynapseJavascriptClient(
			JSONObjectAdapter jsonObjectAdapter,
			SynapseProperties synapseProperties,
			GWTWrapper gwt,
			SynapseJavascriptFactory jsFactory,
			PortalGinInjector ginInjector,
			SynapseJSNIUtils jsniUtils,
			EntityId2BundleCache entityIdBundleCache) {
		this.authController = ginInjector.getAuthenticationController();
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.synapseProperties = synapseProperties;
		this.gwt = gwt;
		this.jsFactory = jsFactory;
		this.ginInjector = ginInjector;
		this.jsniUtils = jsniUtils;
		this.entityIdBundleCache = entityIdBundleCache;
		requestsMap = new HashMap<String, List<Request>>();
		// periodically clean up requests map
		gwt.scheduleFixedDelay(() -> {
			cleanupRequestsMap();
		}, 4000);
	}
	
	public void cleanupRequestsMap() {
		for (String key : requestsMap.keySet()) {
			List<Request> newRequestList = new ArrayList<Request>();
			for (Request request : requestsMap.get(key)) {
				if (request.isPending()) {
					newRequestList.add(request);
				}
			}
			requestsMap.put(key, newRequestList);
		}
	}
	
	/**
	 * For testing purposes only.  Returns Requests associated to the given url.
	 * Requests are periodically removed from this list once they leave the Pending state.
	 * @param forUrl
	 * @return
	 */
	public List<Request> getRequests(String forUrl) {
		return requestsMap.get(forUrl);
	}
	
	public void cancelAllPendingRequests() {
		for (String key : requestsMap.keySet()) {
			cancelPendingRequests(key);
		}
	}

	public void cancelPendingRequests(String forUrl) {
		for (Request request : requestsMap.get(forUrl)) {
			if (request.isPending()) {
				request.cancel();	
			}
		}
	}
	
	private String getRepoServiceUrl() {
		if (repoServiceUrl == null) {
			repoServiceUrl = synapseProperties.getSynapseProperty(REPO_SERVICE_URL_KEY);
		}
		return repoServiceUrl;
	}
	
	private String getAuthServiceUrl() {
		if (authServiceUrl == null) {
			
			authServiceUrl = synapseProperties.getSynapseProperty(AUTH_PUBLIC_SERVICE_URL_KEY);
		}
		return authServiceUrl;
	}
	
	private String getFileServiceUrl() {
		if (fileServiceUrl == null) {
			fileServiceUrl = synapseProperties.getSynapseProperty(FILE_SERVICE_URL_KEY);
		}
		return fileServiceUrl;
	}
	
	private String getSynapseVersionInfo() {
		if (synapseVersionInfo == null) {
			synapseVersionInfo = synapseProperties.getSynapseProperty(SYNAPSE_VERSION_KEY);
		}
		return synapseVersionInfo;
	}
	
	private Request doDelete(String url, AsyncCallback callback) {
		RequestBuilderWrapper requestBuilder = ginInjector.getRequestBuilder();
		requestBuilder.configure(DELETE, url);
		requestBuilder.setHeader(ACCEPT, APPLICATION_JSON_CHARSET_UTF8);
		if (authController.isLoggedIn()) {
			requestBuilder.setHeader(SESSION_TOKEN_HEADER, authController.getCurrentUserSessionToken());
		}
		// never cancel a DELETE request
		boolean canCancel = false;
		return sendRequest(requestBuilder, null, OBJECT_TYPE.None, INITIAL_RETRY_REQUEST_DELAY_MS, canCancel, callback);
	}

	private Request doGet(String url, OBJECT_TYPE responseType, AsyncCallback callback) {
		return doGet(url, responseType, APPLICATION_JSON_CHARSET_UTF8, authController.getCurrentUserSessionToken(), callback);
	}
	
	public Request doGetString(String url, boolean forceAnonymous, AsyncCallback callback) {
		String sessionToken = forceAnonymous ? null : authController.getCurrentUserSessionToken();
		return doGet(url, OBJECT_TYPE.String, null, sessionToken, callback);
	}
	
	private Request doGet(String url, OBJECT_TYPE responseType, String acceptedResponseType, String sessionToken, AsyncCallback callback) {
		RequestBuilderWrapper requestBuilder = ginInjector.getRequestBuilder();
		requestBuilder.configure(GET, url);
		if (acceptedResponseType != null) {
			requestBuilder.setHeader(ACCEPT, acceptedResponseType);	
		}
		if (sessionToken != null) {
			requestBuilder.setHeader(SESSION_TOKEN_HEADER, sessionToken);
		}
		// can always cancel a GET request
		boolean canCancel = true;
		return sendRequest(requestBuilder, null, responseType, INITIAL_RETRY_REQUEST_DELAY_MS, canCancel, callback);
	}
	
	private Request doPostOrPut(RequestBuilder.Method method, String url, JSONEntity requestObject, OBJECT_TYPE responseType, boolean canCancel, AsyncCallback callback) {
		String requestData = null;
		if (requestObject != null) {
			try {
				JSONObjectAdapter adapter = jsonObjectAdapter.createNew();
				requestObject.writeToJSONObject(adapter);
				requestData = adapter.toJSONString();
			} catch (JSONObjectAdapterException exception) {
				callback.onFailure(exception);
				return null;
			}
		}
		RequestBuilderWrapper requestBuilder = ginInjector.getRequestBuilder();
		requestBuilder.configure(method, url);
		requestBuilder.setHeader(ACCEPT, APPLICATION_JSON_CHARSET_UTF8);
		requestBuilder.setHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF8);
		if (authController.isLoggedIn()) {
			requestBuilder.setHeader(SESSION_TOKEN_HEADER, authController.getCurrentUserSessionToken());
		}
		return sendRequest(requestBuilder, requestData, responseType, INITIAL_RETRY_REQUEST_DELAY_MS, canCancel, callback);
	}
	
	private Request doPost(String url, JSONEntity requestObject, OBJECT_TYPE responseType, boolean canCancel, AsyncCallback callback) {
		return doPostOrPut(POST, url, requestObject, responseType, canCancel, callback);
	}
	
	private Request doPut(String url, JSONEntity requestObject, OBJECT_TYPE responseType, AsyncCallback callback) {
		// never cancel a PUT request
		boolean canCancel = false;
		return doPostOrPut(PUT, url, requestObject, responseType, false, callback);
	}

	
	private Request sendRequest(final RequestBuilderWrapper requestBuilder, final String requestData, final OBJECT_TYPE responseType, final int retryDelay, boolean canCancel, final AsyncCallback callback) {
		try {
			Request request = requestBuilder.sendRequest(requestData, new RequestCallback() {
				@Override
				public void onResponseReceived(Request request,
						Response response) {
					int statusCode = response.getStatusCode();
					// if it's a 200 level response, it's OK
					if (statusCode > 199 && statusCode < 300) {
						try {
							Object responseObject;
							if (OBJECT_TYPE.None.equals(responseType)) {
								responseObject = null;
							} else if (OBJECT_TYPE.String.equals(responseType)) {
								responseObject = response.getText();
							} else if (OBJECT_TYPE.AsyncResponse.equals(responseType) && statusCode == 202) {
								JSONObjectAdapter jsonObject = jsonObjectAdapter.createNew(response.getText());
								//SWC-4114: remove requestBody, attempted construction can result in a json object adapter exception in PartialRow
								jsonObject.put("requestBody", (String)null);
								responseObject = new AsynchronousJobStatus(jsonObject);
								throw new ResultNotReadyException((AsynchronousJobStatus)responseObject);
							} else {
								JSONObjectAdapter jsonObject = jsonObjectAdapter.createNew(response.getText());
								responseObject = jsFactory.newInstance(responseType, jsonObject);
							}
							if (callback != null) {
								callback.onSuccess(responseObject);
							}
						} catch (JSONObjectAdapterException e) {
							onError(null, e);
						} catch (ResultNotReadyException e) {
							onError(request, e);
						} catch (Exception e) {
							onError(null, e);
						}
					} else {
						// Status code could be 0 if the preflight request failed, or if the network connection is down.
						if (statusCode == TOO_MANY_REQUESTS_STATUS_CODE || statusCode == 0) {
							// wait a couple of seconds and try the request again...
							gwt.scheduleExecution(new Callback() {
								@Override
								public void invoke() {
									sendRequest(requestBuilder, requestData, responseType, retryDelay*2, canCancel, callback);
								}
							}, retryDelay);
						} else {
							// getException() based on status code,
							// instead of using org.sagebionetworks.client.ClientUtils.throwException() and ExceptionUtil.convertSynapseException() (neither of which can be referenced here)
							String responseText = response.getStatusText();
							ErrorResponseCode responseCode = null;
							try {
								// try to get the reason
								JSONObjectAdapter jsonObject = jsonObjectAdapter.createNew(response.getText());
								if (jsonObject.has("reason")) {
									responseText = jsonObject.get("reason").toString();
								}
								if (jsonObject.has("errorCode")) {
									responseCode = ErrorResponseCode.valueOf(jsonObject.get("errorCode").toString());
								}
							} catch (Exception e) {
								jsniUtils.consoleError("Unable to get reason/code for error: " + e.getMessage());
							}
							onError(request, getException(statusCode, responseText, responseCode));
						}
					}
				}

				@Override
				public void onError(Request request, Throwable exception) {
					if (callback != null) {
						callback.onFailure(exception);	
					}
				}
			});
			
			if (canCancel) {
				String currentUrl = gwt.getCurrentURL();
				if (!requestsMap.containsKey(currentUrl))  {
					requestsMap.put(currentUrl, new ArrayList<>());
				}
				requestsMap.get(currentUrl).add(request);
			}
			return request;
		} catch (final Exception e) {
			if (callback != null) {
				callback.onFailure(e);	
			}
			return null;
		}
	}
	
	public static RestServiceException getException(int statusCode, String reasonStr, ErrorResponseCode code) {
		switch (statusCode) {
			case SC_UNAUTHORIZED :
				return new UnauthorizedException(reasonStr, code);
			case SC_FORBIDDEN :
				return new ForbiddenException(reasonStr, code);
			case SC_NOT_FOUND :
				return new NotFoundException(reasonStr, code);
			case SC_BAD_REQUEST :
				return new BadRequestException(reasonStr, code);
			case SC_LOCKED :
				return new LockedException(reasonStr, code);
			case SC_PRECONDITION_FAILED :
				return new ConflictingUpdateException(reasonStr, code);
			case SC_GONE : 
				return new DeprecatedServiceException(reasonStr, code);
			case TOO_MANY_REQUESTS_STATUS_CODE :
				return new TooManyRequestsException(reasonStr, code);
			case SC_SERVICE_UNAVAILABLE :
				return new SynapseDownException(reasonStr, code);
			case SC_CONFLICT :
				return new ConflictException(reasonStr, code);
			default :
				return new UnknownErrorException(reasonStr, code);
		}
	}
	
	public void getEntityBundle(String entityId, EntityBundleRequest request, final AsyncCallback<EntityBundle> callback) {
		getEntityBundleForVersion(entityId, null, request, callback);
	}
	
	public void populateEntityBundleCache(String entityId) {
		getEntityBundleFromCache(entityId, null);
	}
	
	/**
	 * If bundle is found in local js cache, then this will immediately call onSuccess() with the cached version.
	 * Note that the current entity bundle will still be retrieved, and onSuccess() may be called again if there's a newer version (so write your onSuccess accordingly)!
	 * @param entityId
	 * @param partsMask
	 * @param callback
	 */
	public void getEntityBundleFromCache(String entityId, final AsyncCallback<EntityBundle> callback) {
		EntityBundle cachedBundle = entityIdBundleCache.get(entityId);
		if (cachedBundle != null) {
			jsniUtils.consoleLog("Cache hit: " + entityId);
			if (callback != null) {
				callback.onSuccess(cachedBundle);	
			}
		} else {
			jsniUtils.consoleLog("Cache miss: " + entityId);
		}
		getEntityBundleForVersion(entityId, null, EntityPageTop.ALL_PARTS_REQUEST, new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle latestEntityBundle) {
				if (!latestEntityBundle.equals(cachedBundle)) {
					if (cachedBundle != null) {
						jsniUtils.consoleLog("Cache out of date. Updating cache and calling onSuccess() again: " + entityId);	
					} 
					
					entityIdBundleCache.put(entityId, latestEntityBundle);
					if (callback != null) {
						callback.onSuccess(latestEntityBundle);
					}
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				if (callback != null) {
					callback.onFailure(caught);
				}
			}
		});
	}

	public void getJSON(String uri, AsyncCallback<JSONObjectAdapter> callback) {
		String url = getRepoServiceUrl() + uri;
		doGet(url, OBJECT_TYPE.JSON, callback);
	}
	public void getEntityBundleForVersion(String entityId, Long versionNumber, EntityBundleRequest request, final AsyncCallback<EntityBundle> callback) {
		String url = getRepoServiceUrl() + ENTITY + "/" + entityId;
		if (versionNumber != null) {
			url += REPO_SUFFIX_VERSION + "/" + versionNumber;
		}
		url += BUNDLE2;
		doPost(url, request, OBJECT_TYPE.EntityBundle, true, callback);
	}

	public void getTeam(String teamId, final AsyncCallback<Team> callback) {
		String url = getRepoServiceUrl() + TEAM + "/" + teamId;
		doGet(url, OBJECT_TYPE.Team, callback);
	}

	public void createTeam(Team team, final AsyncCallback<Team> callback) {
		String url = getRepoServiceUrl() + TEAM;
		doPost(url, team, OBJECT_TYPE.Team, false, callback);
	}
	
	public FluentFuture<Team> getTeam(String teamId) {
		String url = getRepoServiceUrl() + TEAM + "/" + teamId;
		return getFuture(cb -> doGet(url, OBJECT_TYPE.Team, cb));
	}

	public FluentFuture<DoiAssociation> getDoiAssociation(String objectId, ObjectType objectType, Long objectVersion) {
		String url = getRepoServiceUrl() + DOI_ASSOCIATION
				+ "?" + ID_PARAMETER + objectId
				+ "&" + TYPE_PARAMETER + objectType;
		if (objectVersion != null) {
			url += "&" + VERSION_PARAMETER + objectVersion;
		}
		String finalUrl = url;
		return getFuture(cb -> doGet(finalUrl, OBJECT_TYPE.Doi, cb));
	}

	public FluentFuture<Doi> getDoi(String objectId, ObjectType objectType, Long objectVersion) {
		String url = getRepoServiceUrl() + DOI
				+ "?" + ID_PARAMETER + objectId
				+ "&" + TYPE_PARAMETER + objectType;
		if (objectVersion != null) {
			url += "&" + VERSION_PARAMETER + objectVersion;
		}
		String finalUrl = url;
		return getFuture(cb -> doGet(finalUrl, OBJECT_TYPE.Doi, cb));
	}

	public void getRestrictionInformation(String subjectId, RestrictableObjectType type, final AsyncCallback<RestrictionInformationResponse> callback)  {
		String url = getRepoServiceUrl() + RESTRICTION_INFORMATION;
		RestrictionInformationRequest request = new RestrictionInformationRequest();
		request.setObjectId(subjectId);
		request.setRestrictableObjectType(type);
		doPost(url, request, OBJECT_TYPE.RestrictionInformationResponse, true, callback);
	}
	
	public Request getEntityChildren(EntityChildrenRequest request, final AsyncCallback<EntityChildrenResponse> callback) {
		String url = getRepoServiceUrl() + ENTITY + CHILDREN;
		return doPost(url, request, OBJECT_TYPE.EntityChildrenResponse, true, callback);
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

	
	public void getWikiAttachmentFileHandles(WikiPageKey key, Long versionNumber, AsyncCallback<List<FileHandle>> callback) {
		String url = getRepoServiceUrl() + "/" +
				key.getOwnerObjectType().toLowerCase() + "/" + 
				key.getOwnerObjectId() + WIKI +
				key.getWikiPageId() + "/" + 
				ATTACHMENT_HANDLES;
		if (versionNumber != null) {
			url += WIKI_VERSION_PARAMETER + versionNumber;
		}
		doGet(url, OBJECT_TYPE.FileHandleResults, callback);
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
	
	public void getUserProfile(String userId, AsyncCallback<UserProfile> callback) {
		String url = getRepoServiceUrl() + USER_PROFILE_PATH;
		if (userId == null) {
			// get my profile
			doGet(url, OBJECT_TYPE.UserProfile, callback);
		} else {
			// get target profile
			url += "/" + userId;
			doGet(url, OBJECT_TYPE.UserProfile, callback);
		}
	}

	public FluentFuture<UserProfile> getUserProfile(String userId) {
		String url = getRepoServiceUrl() + USER_PROFILE_PATH;
		// If userId is null, we get the current user's profile.
		if (userId != null) {
			// If it's not null, we get the indicated user's profile.
			url += "/" + userId;
		}
		final String finalUrl = url;
		return getFuture(cb -> doGet(finalUrl, OBJECT_TYPE.UserProfile, cb));
	}
	
	public void listUserProfiles(List<String> userIds, final AsyncCallback<List> callback) {
		List<Long> userIdsLong = new ArrayList<>();
		for (String userId : userIds) {
			userIdsLong.add(Long.parseLong(userId));
		}
		listUserProfilesFromUserIds(userIdsLong, callback);
	}

	private void listUserProfilesFromUserIds(List<Long> userIds, final AsyncCallback<List> callback) {
		String url = getRepoServiceUrl() + USER_PROFILE_PATH;
		IdList idList = new IdList();
		idList.setList(userIds);
		doPost(url, idList, OBJECT_TYPE.ListWrapperUserProfile, true, callback);
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
	
	public void getChallenges(String userId, Integer limit, Integer offset, AsyncCallback<List<Challenge>> callback) {
		String url = getRepoServiceUrl() + 
				CHALLENGE+"?participantId="+userId;
		if  (limit!=null) {
			url+=	"&"+LIMIT_PARAMETER+limit;
		}
		if  (offset!=null) {
			url+="&"+OFFSET_PARAMETER+offset;
		}
		doGet(url, OBJECT_TYPE.ChallengePagedResults, callback);
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
		AliasList list = new AliasList();
		list.setList(aliases);
		doPost(url, list, OBJECT_TYPE.UserGroupHeaderResponse, true, callback);
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
		getEntityByID(entityId, OBJECT_TYPE.Entity, versionNumber, callback);
	}
	
	private void getEntityByID(String entityId, OBJECT_TYPE type, Long versionNumber, AsyncCallback<Entity> callback) {
		String url = getRepoServiceUrl() + ENTITY + "/" + entityId;
		if (versionNumber != null) {
			url += REPO_SUFFIX_VERSION + "/" + versionNumber;
		}
		doGet(url, type , callback);
	}
	
	public void isWiki(String projectId, AsyncCallback<Boolean> callback) {
		getRootWikiPageKey(ObjectType.ENTITY.toString(), projectId, new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof NotFoundException) {
					callback.onSuccess(false);
				} else {
					callback.onFailure(caught);
				}
			}
			@Override
			public void onSuccess(String result) {
				callback.onSuccess(true);
			}
		});
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
	
	public void getModerators(String forumId, Long limit, Long offset, AsyncCallback<PaginatedIds> callback) {
		String url = getRepoServiceUrl() + FORUM+"/"+forumId+MODERATORS+"?"+LIMIT_PARAMETER+limit+"&"+OFFSET_PARAMETER+offset;
		doGet(url, OBJECT_TYPE.PaginatedIds, callback);
	}

	public void getSubscribers(Topic topic, String nextPageToken, AsyncCallback<SubscriberPagedResults> callback) {
		String url = getRepoServiceUrl() + SUBSCRIPTION+"/subscribers";
		if (nextPageToken != null) {
			url += "?" + NEXT_PAGE_TOKEN_PARAM + nextPageToken;
		}
		doPost(url, topic, OBJECT_TYPE.SubscriberPagedResults, true, callback);
	}

	public void getSubscribersCount(Topic topic, AsyncCallback<Long> callback) {
		String url = getRepoServiceUrl() + SUBSCRIPTION+"/subscribers/count";
		doPost(url, topic, OBJECT_TYPE.SubscriberCount, true, callback);
	}
	
	public void getFileHandleAndUrlBatch(BatchFileRequest request, AsyncCallback<BatchFileResult> callback) {
		String url = getFileServiceUrl() + FILE_HANDLE_BATCH;
		doPost(url, request, OBJECT_TYPE.BatchFileResult, true, callback);
	}
	
	public void getEntityHeaderBatch(List<String> entityIds, AsyncCallback<ArrayList<EntityHeader>> callback) {
		List<Reference> list = new ArrayList<Reference>();
		for (String entityId : entityIds) {
			Reference ref = new Reference();
			ref.setTargetId(entityId);
			list.add(ref);
		}
		getEntityHeaderBatchFromReferences(list, callback);
	}
	
	public void getEntityHeaderBatchFromReferences(List<Reference> list, AsyncCallback<ArrayList<EntityHeader>> callback) {
		String url = getRepoServiceUrl() + ENTITY + "/header";
		ReferenceList refList = new ReferenceList();
		refList.setReferences(list);
		doPost(url, refList, OBJECT_TYPE.PaginatedResultsEntityHeader, true, callback);
	}

	public FluentFuture<MembershipInvitation> getMembershipInvitation(MembershipInvtnSignedToken token) {
		String url = getRepoServiceUrl() + MEMBERSHIP_INVITATION + "/" + token.getMembershipInvitationId();
		return getFuture(cb -> doPost(url, token, OBJECT_TYPE.MembershipInvitation, true, cb));
	}

	public FluentFuture<InviteeVerificationSignedToken> getInviteeVerificationSignedToken(String membershipInvitationId) {
		String url = getRepoServiceUrl() + MEMBERSHIP_INVITATION + "/" + membershipInvitationId + INVITEE_VERIFICATION_SIGNED_TOKEN;
		return getFuture(cb -> doGet(url, OBJECT_TYPE.InviteeVerificationSignedToken, cb));
	}

	public FluentFuture<Void> updateInviteeId(InviteeVerificationSignedToken token) {
		String url = getRepoServiceUrl() + MEMBERSHIP_INVITATION + "/" + token.getMembershipInvitationId() + INVITEE_ID;
		return getFuture(cb -> doPut(url, token, OBJECT_TYPE.None, cb));
	}

	public void deleteMembershipInvitation(String id, AsyncCallback<Void> callback) {
		String url = getRepoServiceUrl() + MEMBERSHIP_INVITATION + "/" + id;
		doDelete(url, callback);
	}
	
	public void deleteEntityById(String id, AsyncCallback<Void> callback) {
		deleteEntityById(id, false, callback);
	}
	public void deleteEntityById(String id, boolean skipTrash, AsyncCallback<Void> callback) {
		String url = getRepoServiceUrl() + ENTITY + "/" + id;
		if (skipTrash) {
			url = url + "?" + SKIP_TRASH_CAN_PARAM + "=true";
		}
		doDelete(url, callback);
	}
	
	public void updateV2WikiPage(WikiPageKey key, V2WikiPage toUpdate, AsyncCallback<V2WikiPage> callback){
		String url = getRepoServiceUrl() + "/" +
				key.getOwnerObjectType().toLowerCase() + "/" + 
				key.getOwnerObjectId() + WIKI2 +
				key.getWikiPageId();
		
		doPut(url, toUpdate, OBJECT_TYPE.V2WikiPage, callback);
	}

	public void updateEntity(Entity toUpdate, String generatedByID, Boolean newVersion, AsyncCallback<Entity> callback){
		String url = getRepoServiceUrl() + ENTITY + "/" + toUpdate.getId() + "?";
		if (generatedByID != null) {
			url += "generatedBy=" + generatedByID;
		}
		if (newVersion != null) {
			if (!url.endsWith("?")) {
				url += "&";
			}
			url += "newVersion=" + newVersion.toString();
		}

		doPut(url, toUpdate, OBJECT_TYPE.Entity, callback);
	}
	
	public void createSnapshot(String entityId, String comment, String label, String activityId, AsyncCallback<SnapshotResponse> callback){
		// POST /entity/{id}/table/snapshot
		String url = getRepoServiceUrl() + ENTITY + "/" + entityId + TABLE_SNAPSHOT;
		SnapshotRequest request = new SnapshotRequest();
		request.setSnapshotComment(comment);
		request.setSnapshotLabel(label);
		request.setSnapshotActivityId(activityId);
		doPost(url, request, OBJECT_TYPE.SnapshotResponse, false, callback);
	}

	public void updateV2WikiOrderHint(WikiPageKey key, V2WikiOrderHint toUpdate, AsyncCallback<V2WikiOrderHint> callback) {
		String url = getRepoServiceUrl() + "/" +
			key.getOwnerObjectType().toLowerCase() + "/" + 
			key.getOwnerObjectId() + WIKI_ORDER_HINT;
		doPut(url, toUpdate, OBJECT_TYPE.V2WikiOrderHint, callback);
	}
	public void getV2WikiOrderHint(WikiPageKey key, AsyncCallback<V2WikiOrderHint> callback) {
		String url = getRepoServiceUrl() + "/" +
				key.getOwnerObjectType().toLowerCase() + "/" + 
				key.getOwnerObjectId() + WIKI_ORDER_HINT;
		doGet(url, OBJECT_TYPE.V2WikiOrderHint, callback);
	}
	
	public FluentFuture<Entity> createEntity(Entity entity) {
		String url = getRepoServiceUrl() + ENTITY;
		return getFuture(cb -> doPost(url, entity, OBJECT_TYPE.Entity, false, cb));
	}
	
	public FluentFuture<List<ColumnModel>> getDefaultColumnsForView(ViewType viewType) {
		String url = getRepoServiceUrl() + COLUMN_VIEW_DEFAULT + viewType.name();
		return getFuture(cb -> doGet(url, OBJECT_TYPE.ListWrapperColumnModel, cb));
	}
	
	public FluentFuture<Void> deleteMembershipRequest(String requestId) {
		String url = getRepoServiceUrl() + MEMBERSHIP_REQUEST + "/" + requestId;
		return getFuture(cb -> doDelete(url, cb));
	}
	
	public FluentFuture<Void> deleteMembershipInvitation(String inviteId) {
		String url = getRepoServiceUrl() + MEMBERSHIP_INVITATION + "/" + inviteId;
		return getFuture(cb -> doDelete(url, cb));
	}
	
	private FluentFuture<Void> logError(LogEntry entry) {
		String url = getRepoServiceUrl() + LOG;
		return getFuture(cb -> doPost(url, entry, OBJECT_TYPE.None, true, cb));
	}
	
	public FluentFuture<Void> logError(Throwable ex) {
		LogEntry entry = new LogEntry();
		String exceptionString = gwt.getCurrentHistoryToken().substring(1) + ":" + ex.getMessage();
		String versionInfo = getSynapseVersionInfo();
		if (versionInfo.contains("-")) {
			versionInfo = versionInfo.substring(0, versionInfo.indexOf('-'));	
		}
		String outputExceptionString = exceptionString.substring(0, Math.min(exceptionString.length(), MAX_LOG_ENTRY_LABEL_SIZE));
		entry.setLabel(versionInfo + ":" + outputExceptionString);
		entry.setMessage(gwt.getCurrentURL() + " : \n" + ex.getMessage());
		return logError(entry);
	}
	
	public FluentFuture<PaginatedTeamIds> getUserTeams(String userId, boolean isAscendingOrder, String nextPageToken) {
		String urlBuilder = getRepoServiceUrl() + USER + "/" + userId + TEAM + "/id?" + 
				ASCENDING_PARAM + Boolean.toString(isAscendingOrder) + "&sort=TEAM_NAME";
		if (nextPageToken != null) {
			urlBuilder += "&" + NEXT_PAGE_TOKEN_PARAM + nextPageToken;
		}
		final String url = urlBuilder;
		return getFuture(cb -> doGet(url, OBJECT_TYPE.PaginatedTeamIds, cb));
	}
	
	public FluentFuture<List<Team>> listTeams(List<String> teamIds) {
		List<Long> teamIdsLong = new ArrayList<>();
		for (String teamId : teamIds) {
			teamIdsLong.add(Long.parseLong(teamId));
		}
		String url = getRepoServiceUrl() + TEAM + "List";
		IdList idList = new IdList();
		idList.setList(teamIdsLong);
		return getFuture(cb -> doPost(url, idList, OBJECT_TYPE.ListWrapperTeam, true, cb));
	}
	
	public void login(LoginRequest loginRequest, AsyncCallback<LoginResponse> callback) {
		String url = getAuthServiceUrl() + "/login";
		doPost(url, loginRequest, OBJECT_TYPE.LoginResponse, false, callback);
	}
	
	public void logout() {
		if (authController.isLoggedIn()) {
			String url = getAuthServiceUrl() + SESSION;
			doDelete(url, null);	
		}
	}

	public void getMyProjects(ProjectListType projectListType, int limit, int offset, ProjectListSortColumn sortBy, SortDirection sortDir, AsyncCallback<List<ProjectHeader>> projectHeadersCallback) {
		getProjects(projectListType, null, null, limit, offset, sortBy, sortDir, projectHeadersCallback);
	}
	
	public void getProjectsForTeam(String teamId, int limit, int offset, ProjectListSortColumn sortBy, SortDirection sortDir, AsyncCallback<List<ProjectHeader>> projectHeadersCallback) {
		getProjects(ProjectListType.TEAM, null, teamId, limit, offset, sortBy, sortDir, projectHeadersCallback);
	}
	
	public void getUserProjects(String userId, int limit, int offset, ProjectListSortColumn sortBy, SortDirection sortDir, AsyncCallback<List<ProjectHeader>> projectHeadersCallback) {
		getProjects(ProjectListType.ALL, userId, null, limit, offset, sortBy, sortDir, projectHeadersCallback);
	}
	
	private void getProjects(ProjectListType projectListType, String userId, String teamId, int limit, int offset, ProjectListSortColumn sortBy, SortDirection sortDir, AsyncCallback<List<ProjectHeader>> projectHeadersCallback) {
		String url = getRepoServiceUrl() + PROJECTS_URI_PATH;
		if (userId != null) {
			url += USER + '/' + userId;
		}
		
		if (sortBy == null) {
			sortBy = ProjectListSortColumn.LAST_ACTIVITY;
		}
		if (sortDir == null) {
			sortDir = SortDirection.DESC;
		}

		url += '?' + OFFSET_PARAMETER + offset + '&' + LIMIT_PARAMETER + limit + "&sort=" + sortBy.name() + "&sortDirection="
				+ sortDir.name();
		
		if (teamId != null) {
			url += "&teamId=" + teamId;
		}
		if (projectListType != null) {
			url += "&filter=" + projectListType;
		}

		doGet(url, OBJECT_TYPE.PaginatedResultProjectHeader, projectHeadersCallback);
	}
	
	private String getEndpoint(AsynchType type) {
		String endpoint;
		switch(type) {
			case BulkFileDownload:
			case AddFileToDownloadList:
				endpoint = getFileServiceUrl();
				break;
			default:
				endpoint = getRepoServiceUrl();
		}
		return endpoint;
	}
	public void getAsynchJobResults(AsynchType type, String jobId, AsynchronousRequestBody request, AsyncCallback<AsynchronousResponseBody> callback) {
		String url = type.getResultUrl(jobId, request);
		doGet(getEndpoint(type) + url, OBJECT_TYPE.AsyncResponse, callback);
	}

	public void startAsynchJob(AsynchType type, AsynchronousRequestBody request, AsyncCallback<String> callback) {
		String url = type.getStartUrl(request);
		doPost(getEndpoint(type) + url, request, OBJECT_TYPE.AsyncJobId, false, callback);
	}
	
	public void getEntitiesGeneratedBy(String activityId, Integer limit, Integer offset, AsyncCallback<ArrayList<Reference>> callback) {
		String url = getRepoServiceUrl() + ACTIVITY_URI_PATH + "/" + activityId + GENERATED_PATH + "?" + OFFSET_PARAMETER + offset + "&" + LIMIT_PARAMETER + limit;
		doGet(url, OBJECT_TYPE.PaginatedResultReference, callback);
	}
	
	public void createActivityAndLinkToEntity(Activity activity, Entity entity, AsyncCallback<Entity> callback) {
		String url = getRepoServiceUrl() + ACTIVITY_URI_PATH;
		doPost(url, activity, OBJECT_TYPE.Activity, false, new AsyncCallback<Activity>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
			@Override
			public void onSuccess(Activity newActivity) {
				updateEntity(entity, newActivity.getId(), false, callback);
			}
		});
	}

	public void updateActivity(Activity activity, AsyncCallback<Activity> callback) {
		String url = getRepoServiceUrl() + ACTIVITY_URI_PATH + "/" + activity.getId();
		doPut(url, activity, OBJECT_TYPE.Activity, callback);
	}

	public void getActivityForEntityVersion(String entityId, Long versionNumber, AsyncCallback<Activity> callback) {
		String url = getRepoServiceUrl() + ENTITY + "/" + entityId;
		if (versionNumber != null) {
			url += REPO_SUFFIX_VERSION + "/" + versionNumber;
		}
		url += GENERATED_BY_SUFFIX;
		doGet(url, OBJECT_TYPE.Activity, callback);
	}

	public void getDockerTaggedCommits(
			String entityId, 
			Long limit, 
			Long offset,
			DockerCommitSortBy sortBy, 
			Boolean ascending,
			AsyncCallback<ArrayList<DockerCommit>> callback) {
		String url = getRepoServiceUrl() + ENTITY+"/"+entityId+ DOCKER_TAG;
		List<String> requestParams = new ArrayList<String>();
		if (limit!=null) {
			requestParams.add(LIMIT_PARAMETER+limit);
		}
		if (offset!=null) {
			requestParams.add(OFFSET_PARAMETER+offset);
		}
		if (sortBy!=null) {
			requestParams.add("sort="+sortBy.name());
		}
		if (ascending!=null) {
			requestParams.add("ascending="+ascending);
		}
		if (!requestParams.isEmpty()) {
			url += "?" + Joiner.on('&').join(requestParams);
		}

		
		doGet(url, OBJECT_TYPE.PaginatedDockerCommit, callback);
	}

	public void startMultipartUpload(MultipartUploadRequest request, Boolean forceRestart, AsyncCallback<MultipartUploadStatus> callback) {
		String url = getFileServiceUrl() + "/file/multipart";
		//the restart parameter is optional.
		if(forceRestart != null){
			url += "?forceRestart=" + forceRestart.toString();
		}
		doPost(url, request, OBJECT_TYPE.MultipartUploadStatus, false, callback);
	}

	public void getMultipartPresignedUrlBatch(BatchPresignedUploadUrlRequest request, AsyncCallback<BatchPresignedUploadUrlResponse> callback) {
		String url = getFileServiceUrl() + "/file/multipart/" + request.getUploadId() + "/presigned/url/batch";
		doPost(url, request, OBJECT_TYPE.BatchPresignedUploadUrlResponse, false, callback);
	}

	public void addPartToMultipartUpload(String uploadId, int partNumber, String partMD5Hex, AsyncCallback<AddPartResponse> callback) {
		String url = getFileServiceUrl() + "/file/multipart/" + uploadId + "/add/" + partNumber + "?partMD5Hex=" + partMD5Hex;
		doPut(url, null, OBJECT_TYPE.AddPartResponse, callback);
	}

	public void completeMultipartUpload(String uploadId, AsyncCallback<MultipartUploadStatus> callback) {
		String url = getFileServiceUrl() + "/file/multipart/" + uploadId + "/complete";
		doPut(url, null, OBJECT_TYPE.MultipartUploadStatus, callback);
	}
	
	public void getOpenMembershipRequestCount(String teamId, AsyncCallback<Long> callback) {
		String url = getRepoServiceUrl() + TEAM + "/" + teamId + OPEN_MEMBERSHIP_REQUEST + "?" + OFFSET_PARAMETER + "0&" + LIMIT_PARAMETER + "1";
		doGet(url, OBJECT_TYPE.PaginatedResultsTotalNumberOfResults, callback);
	}
	
	public void getPrincipalAlias(PrincipalAliasRequest request, AsyncCallback<PrincipalAliasResponse> callback) {
		String url = getRepoServiceUrl() + PRINCIPAL+"/alias/";
		doPost(url, request, OBJECT_TYPE.PrincipalAliasResponse, true, callback);
	}
	
	public void unbindOAuthProvidersUserId(OAuthProvider provider, String alias, AsyncCallback<Void> callback) {
		String url = getAuthServiceUrl() + AUTH_OAUTH_2_ALIAS + "?provider="+
				gwt.encodeQueryString(provider.name())+
				"&"+"alias="+gwt.encodeQueryString(alias);
		doDelete(url, callback);
	}
	
	public void lookupChild(String entityName, String containerEntityId, AsyncCallback<String> callback) {
		EntityLookupRequest request = new EntityLookupRequest();
		request.setEntityName(entityName);
		request.setParentId(containerEntityId);
		String url = getRepoServiceUrl() + ENTITY+"/child";
		doPost(url, request, OBJECT_TYPE.EntityId, true, callback);
	}
	
	public void getUploadDestinations(String parentEntityId, AsyncCallback<List<UploadDestination>> callback) {
		String url = getFileServiceUrl() + ENTITY + "/" + parentEntityId + UPLOAD_DESTINATIONS;
		doGet(url, OBJECT_TYPE.ListWrapperUploadDestinations, callback);
	}
	
	public void getAllSubscriptions(SubscriptionObjectType objectType, Long limit, Long offset, SortByType sortByType, org.sagebionetworks.repo.model.subscription.SortDirection sortDirection, AsyncCallback<SubscriptionPagedResults> callback) {
		String url = getRepoServiceUrl() + SUBSCRIPTION+"/all?" + OBJECT_TYPE_PARAMETER + objectType.name() + "&" + LIMIT_PARAMETER+limit + "&" + OFFSET_PARAMETER+offset;
		if (sortByType!=null) {
			url += "&sortBy="+sortByType.name();
		}
		if (sortDirection!=null) {
			url += "&sortDirection="+sortDirection.name();
		}
		doGet(url, OBJECT_TYPE.SubscriptionPagedResults, callback);
	}
	public void deleteTeamMember(String teamId, String memberId, AsyncCallback<Void> callback) {
		String url = getRepoServiceUrl() + TEAM + "/" + teamId + MEMBER + "/" + memberId;
		doDelete(url, callback);
	}

	public void addFileToDownloadList(String fileHandleId, String fileEntityId, AsyncCallback<DownloadList> callback) {
		List<FileHandleAssociation> toAdd = new ArrayList<FileHandleAssociation>();
		FileHandleAssociation fha = new FileHandleAssociation();
		fha.setFileHandleId(fileHandleId);
		fha.setAssociateObjectType(FileHandleAssociateType.FileEntity);
		fha.setAssociateObjectId(fileEntityId);
		toAdd.add(fha);
		addFilesToDownloadList(toAdd, callback);
	}
	
	public void addFilesToDownloadList(List<FileHandleAssociation> toAdd, AsyncCallback<DownloadList> callback) {
		FileHandleAssociationList request = new FileHandleAssociationList();
		request.setList(toAdd);
		String url = getFileServiceUrl() + DOWNLOAD_LIST_ADD;
		doPost(url, request, OBJECT_TYPE.DownloadList, false, callback);
	}
	
	public void removeFileFromDownloadList(FileHandleAssociation fha, AsyncCallback<DownloadList> callback) {
		List<FileHandleAssociation> toRemove = new ArrayList<FileHandleAssociation>();
		toRemove.add(fha);
		removeFilesFromDownloadList(toRemove, callback);
	}

	public void removeFilesFromDownloadList(List<FileHandleAssociation> toRemove, AsyncCallback<DownloadList> callback){
		FileHandleAssociationList request = new FileHandleAssociationList();
		request.setList(toRemove);
		String url = getFileServiceUrl() + DOWNLOAD_LIST_REMOVE;
		doPost(url, request, OBJECT_TYPE.DownloadList, false, callback);
	}
	
	public void clearDownloadList(AsyncCallback<Void> callback) {
		String url = getFileServiceUrl() + DOWNLOAD_LIST;
		doDelete(url, callback);
	}
	
	public void getDownloadList(AsyncCallback<DownloadList> callback) {
		String url = getFileServiceUrl() + DOWNLOAD_LIST;
		doGet(url, OBJECT_TYPE.DownloadList, callback);
	}
	
	public void createDownloadOrderFromUsersDownloadList(String zipFileName, AsyncCallback<DownloadOrder> callback) {
		String url = getFileServiceUrl() + DOWNLOAD_ORDER+"?zipFileName="+zipFileName;
		doPost(url, null, OBJECT_TYPE.DownloadOrder, false, callback);
	}

	public void getDownloadOrder(String orderId, AsyncCallback<DownloadOrder> callback) {
		String url = getFileServiceUrl() + "/download/order/"+orderId;
		doGet(url, OBJECT_TYPE.DownloadOrder, callback);
	}
	
	public void getDownloadOrderHistory(DownloadOrderSummaryRequest request, AsyncCallback<DownloadOrderSummaryResponse> callback) {
		String url = getFileServiceUrl() + "/download/order/history";
		doPost(url, request, OBJECT_TYPE.DownloadOrderSummaryResponse, false, callback);
	}

	public void initSession(String token) {
		initSession(token, null);
	}

	/**
	 * call to ask the server to set the session cookie
	 * @param token
	 * @param callback
	 */
	public void initSession(String token, AsyncCallback<Void> callback) {
		Session s = new Session();
		s.setSessionToken(token);
		String url = jsniUtils.getSessionCookieUrl();
		doPost(url, s, OBJECT_TYPE.None, false, callback);
	}
	
	public void subscribe(Topic topic, AsyncCallback<Subscription> callback) {
		String url = getRepoServiceUrl() + SUBSCRIPTION ;
		doPost(url, topic, OBJECT_TYPE.Subscription, false, callback);
	}
	public void unsubscribe(String subscriptionId, AsyncCallback<Void> callback) {
		String url = getRepoServiceUrl() + SUBSCRIPTION + "/" + subscriptionId;
		doDelete(url, callback);
	}
	public void subscribeToAll(SubscriptionObjectType type, AsyncCallback<Subscription> callback) {
		String url = getRepoServiceUrl() + SUBSCRIPTION + ALL + "?objectType="+type.toString();
		doPost(url, null, OBJECT_TYPE.Subscription, false, callback);
	}
	public void getSubscription(String subscriptionId, AsyncCallback<Subscription> callback) {
		String url = getRepoServiceUrl() + SUBSCRIPTION + "/"+subscriptionId;
		doGet(url, OBJECT_TYPE.Subscription, callback);
	}
	public void listSubscription(SubscriptionRequest request, AsyncCallback<SubscriptionPagedResults> callback) {
		String url = getRepoServiceUrl() + SUBSCRIPTION + "/list";
		doPost(url, request, OBJECT_TYPE.SubscriptionPagedResults, true, callback);
	}
	public void getSearchResults(SearchQuery request, AsyncCallback<SearchResults> callback) {
		String url = getRepoServiceUrl() + SEARCH;
		doPost(url, request, OBJECT_TYPE.SearchResults, true, callback);
	}
	public void getColumnModelsForTableEntity(String tableEntityId, AsyncCallback<List<ColumnModel>> callback) {
		String url = getRepoServiceUrl() + ENTITY + "/" + tableEntityId + COLUMN;
		doGet(url, OBJECT_TYPE.PaginatedColumnModelsResults, callback);
	}
	public Request getEntityVersions(String entityId, int offset, int limit, AsyncCallback<List<VersionInfo>> callback) {
		String url = getRepoServiceUrl() + ENTITY + "/" + entityId + REPO_SUFFIX_VERSION
				+ "?" + OFFSET_PARAMETER + offset + "&" + LIMIT_PARAMETER + limit;
		return doGet(url, OBJECT_TYPE.PaginatedResultsVersionInfo, callback);
	}
	public void getThreadsForEntity(String entityId, Long limit, Long offset,
			DiscussionThreadOrder order, Boolean ascending, DiscussionFilter filter,
			AsyncCallback<List<DiscussionThreadBundle>> callback) {
		String url = getThreadsURL(ENTITY, entityId, limit, offset, order, ascending, filter);
		doGet(url, OBJECT_TYPE.PaginatedResultsDiscussionThreadBundle, callback);
	}
	public void getThreadsForForum(String forumId, Long limit, Long offset,
			DiscussionThreadOrder order, Boolean ascending, DiscussionFilter filter,
			AsyncCallback<List<DiscussionThreadBundle>> callback) {
		String url = getThreadsURL(FORUM, forumId, limit, offset, order, ascending, filter);
		doGet(url, OBJECT_TYPE.PaginatedResultsDiscussionThreadBundle, callback);
	}
	
	private String getThreadsURL(String associatedObjectType, String objectId, Long limit, Long offset,
			DiscussionThreadOrder order, Boolean ascending, DiscussionFilter filter) {
		String url = getRepoServiceUrl() + associatedObjectType+"/"+objectId+THREADS
				+"?"+LIMIT_PARAMETER+limit+"&"+OFFSET_PARAMETER+offset;
		if (order != null) {
			url += "&sort="+order.name();
		}
		if (ascending != null) {
			url += "&ascending="+ascending;
		}
		url += "&filter="+filter.toString();
		return url;
	}
	
	public void getRepliesForThread(String threadId,
			Long limit, Long offset, DiscussionReplyOrder order, Boolean ascending,
			DiscussionFilter filter, AsyncCallback<List<DiscussionReplyBundle>> callback) {
		
		String url = getRepoServiceUrl() + THREAD+"/"+threadId+REPLIES
				+"?"+LIMIT_PARAMETER+limit+"&"+OFFSET_PARAMETER+offset;
		if (order != null) {
			url += "&sort="+order.name();
		}
		if (ascending != null) {
			url += "&ascending="+ascending;
		}
		url += "&filter="+filter;
		doGet(url, OBJECT_TYPE.PaginatedResultsDiscussionReplyBundle, callback);
	}
	
	public void getV2WikiHeaderTree(String ownerId, String ownerType,
			AsyncCallback<List<V2WikiHeader>> callback) {
		getV2WikiHeaderTree(ownerId, ownerType, callback, 0, new ArrayList<V2WikiHeader>());
	}
	
	private void getV2WikiHeaderTree(String ownerId, String ownerType,
			AsyncCallback<List<V2WikiHeader>> finalCallback, int offset, List<V2WikiHeader> fullWikiHeaderList) {
		// continue asking for v2 wiki headers until result is empty
		String url = getRepoServiceUrl() + "/" +
				ownerType.toLowerCase() + "/" + ownerId + WIKI_HEADER_TREE +
				"?"+LIMIT_PARAMETER+LIMIT_50+"&"+OFFSET_PARAMETER+offset;
		doGet(url, OBJECT_TYPE.PaginatedResultsV2WikiHeader, new AsyncCallback<List<V2WikiHeader>>() {
			@Override
			public void onFailure(Throwable caught) {
				finalCallback.onFailure(caught);
			}
			@Override
			public void onSuccess(List<V2WikiHeader> results) {
				fullWikiHeaderList.addAll(results);
				if (results.isEmpty() || results.size() < LIMIT_50) {
					finalCallback.onSuccess(fullWikiHeaderList);
				} else {
					getV2WikiHeaderTree(ownerId, ownerType, finalCallback, offset+LIMIT_50, fullWikiHeaderList);
				}
			}
		});
	}
	
	public void createEntity(Entity entity, AsyncCallback<Entity> cb) {
		String url = getRepoServiceUrl() + ENTITY;
		doPost(url, entity, OBJECT_TYPE.Entity, false, cb);
	}
	
	public void sendPasswordResetEmail(String emailAddress, AsyncCallback<Void> cb) {
		String url = getAuthServiceUrl() + USER_PASSWORD_RESET;
		Username username = new Username();
		username.setEmail(emailAddress);
		url += "?passwordResetEndpoint=" + gwt.encodeQueryString(gwt.getHostPageBaseURL() + "#!PasswordResetSignedToken:");
		doPost(url, username, OBJECT_TYPE.None, false, cb);
	}
	
	public void changePassword(ChangePasswordInterface changePasswordRequest, AsyncCallback<Void> cb) {
		String url = getAuthServiceUrl() + USER_CHANGE_PASSWORD;
		doPost(url, changePasswordRequest, OBJECT_TYPE.None, false, cb);
	}
	
	public void addTeamMember(String userId, String teamId, AsyncCallback<Void> cb) {
		String teamEndpoint = gwt.encodeQueryString(gwt.getHostPageBaseURL() + "#!Team:");
		String signedTokenEndpoint = gwt.encodeQueryString(gwt.getHostPageBaseURL() + "#!SignedToken:");
		String url = getRepoServiceUrl() + TEAM + "/" + teamId + MEMBER +"/" + userId + "?teamEndpoint=" + teamEndpoint + "&notificationUnsubscribeEndpoint="+signedTokenEndpoint;
		doPut(url, null, OBJECT_TYPE.None, cb);
	}
	
	/**
	 * If logged in, refresh the current session token to render it usable for another 24 hours
	 */
	public void refreshCurrentSessionToken() {
		if (authController.isLoggedIn()) {
			String url = getAuthServiceUrl() + SESSION;
			Session session = new Session();
			session.setSessionToken(authController.getCurrentUserSessionToken());
			doPut(url, session, OBJECT_TYPE.None, null);

			// also set the session cookie (to update the expiration)
			initSession(authController.getCurrentUserSessionToken());
		}
	}
	public void getTeamMembers(String teamId, String fragment, TeamMemberTypeFilterOptions memberType, Integer limit, Integer offset, AsyncCallback<TeamMemberPagedResults> callback) {
		// first gather the team members
		String url = getRepoServiceUrl() + TEAM_MEMBERS + teamId + "?" + OFFSET_PARAMETER + offset + "&" + LIMIT_PARAMETER+limit;
		if (fragment != null) {
			url += "&" + NAME_FRAGMENT_FILTER + gwt.encodeQueryString(fragment);
		}
		if (memberType != null) {
			url += "&" + NAME_MEMBERTYPE_FILTER + gwt.encodeQueryString(memberType.toString());
		}
		AsyncCallback<List<TeamMember>> paginatedResultsCallback = new AsyncCallback<List<TeamMember>>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
			public void onSuccess(List<TeamMember> teamMembers) {
				getTeamMembersStep2(teamMembers, limit, offset, callback);
			};
		};
		doGet(url, OBJECT_TYPE.PaginatedResultsTeamMember, paginatedResultsCallback);
	}
	
	public void getTeamMembersStep2(List<TeamMember> teamMembers, Integer limit, Integer offset, AsyncCallback<TeamMemberPagedResults> callback) {
		// second step, get all user profiles (in bulk)
		List<String> userIds = new ArrayList<>();
		for (TeamMember member : teamMembers) {
			userIds.add(member.getMember().getOwnerId());
		}
		AsyncCallback<List> profilesCallback = new AsyncCallback<List>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
			@Override
			public void onSuccess(List profiles) {
				List<TeamMemberBundle> teamMemberBundles = new ArrayList<TeamMemberBundle>();
				for (int i = 0; i < userIds.size(); i++) {
					teamMemberBundles.add(new TeamMemberBundle((UserProfile)profiles.get(i), teamMembers.get(i).getIsAdmin(), teamMembers.get(i).getTeamId()));
				}
				TeamMemberPagedResults results = new TeamMemberPagedResults();
				results.setResults(teamMemberBundles);
				int totalNumberOfResults = offset + limit;
				if (teamMembers.size() >= limit) {
					totalNumberOfResults++;
				}
				results.setTotalNumberOfResults(new Long(totalNumberOfResults));
				callback.onSuccess(results);
			}
		};
		listUserProfiles(userIds, profilesCallback);
	}
	
	public void updateAnnotations(String entityId, Annotations annotations, AsyncCallback<Annotations> cb) {
		String url = getRepoServiceUrl() + ENTITY + "/" + entityId + "/annotations2";
		doPut(url, annotations, OBJECT_TYPE.Annotations, cb);
	}

	public Request getNotificationEmail(AsyncCallback<NotificationEmail> cb) {
		String url = getRepoServiceUrl() + "/notificationEmail";
		return doGet(url, OBJECT_TYPE.NotificationEmail, cb);
	}

	public Request getFileEntityTemporaryUrlForVersion(String entityId, Long versionNumber, boolean preview, AsyncCallback<String> cb) {
		String filePath = preview ? FILE_PREVIEW : FILE;
		String url = getRepoServiceUrl() + ENTITY + "/" + entityId + VERSION_INFO + "/"
				+ versionNumber + filePath + "?" + REDIRECT_PARAMETER
				+ "false";
		return doGet(url, OBJECT_TYPE.String, cb);
	}
	
	public void additionalEmailValidation(String userId, String emailAddress, String callbackUrl, AsyncCallback<Void> cb) {
		String url = getRepoServiceUrl() + ACCOUNT + "/" + userId + "/"
				+ EMAIL_VALIDATION + "?" + PORTAL_ENDPOINT_PARAM + callbackUrl;
		Username username = new Username();
		username.setEmail(emailAddress);
		doPost(url, username, OBJECT_TYPE.None, false, cb);
	}
}

