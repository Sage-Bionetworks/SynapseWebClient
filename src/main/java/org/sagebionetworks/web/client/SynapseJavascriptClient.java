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
import java.util.List;

import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityType;
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
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;
import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.auth.LoginRequest;
import org.sagebionetworks.repo.model.auth.LoginResponse;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.Forum;
import org.sagebionetworks.repo.model.docker.DockerCommit;
import org.sagebionetworks.repo.model.docker.DockerCommitSortBy;
import org.sagebionetworks.repo.model.doi.v2.Doi;
import org.sagebionetworks.repo.model.doi.v2.DoiAssociation;
import org.sagebionetworks.repo.model.entity.Direction;
import org.sagebionetworks.repo.model.entity.EntityLookupRequest;
import org.sagebionetworks.repo.model.entity.SortBy;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
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
import org.sagebionetworks.repo.model.principal.PrincipalAliasRequest;
import org.sagebionetworks.repo.model.principal.PrincipalAliasResponse;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.repo.model.subscription.Etag;
import org.sagebionetworks.repo.model.subscription.SortByType;
import org.sagebionetworks.repo.model.subscription.SubscriberPagedResults;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.repo.model.subscription.SubscriptionPagedResults;
import org.sagebionetworks.repo.model.subscription.Topic;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ViewType;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiOrderHint;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseJavascriptFactory.OBJECT_TYPE;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.ConflictException;
import org.sagebionetworks.web.shared.exceptions.ConflictingUpdateException;
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
	public static final String TYPE_FILTER_PARAMETER = "&typeFilter=";
	public static final String CHALLENGE = "/challenge";
	public static final String WIKI = "/wiki/";
	public static final String WIKI2 = "/wiki2/";
	public static final String WIKIKEY = "/wikikey";
	public static final String WIKI_ORDER_HINT = "/wiki2orderhint";
	public static final String CHILDREN = "/children";
	public static final String RESTRICTION_INFORMATION = "/restrictionInformation";
	public static final String USER_PROFILE_PATH = "/userProfile";
	public static final String USER_GROUP_HEADER_BY_ALIAS = "/userGroupHeaders/aliases";
	public static final String USER_GROUP_HEADER_BATCH_PATH = "/userGroupHeaders/batch?ids=";
	public static final String ENTITY = "/entity";
	public static final String PROJECT = "/project";
	public static final String FORUM = "/forum";
	public static final String THREAD = "/thread";
	public static final String THREAD_COUNT = "/threadcount";
	public static final String REPLY = "/reply";
	public static final String REPLY_COUNT = "/replycount";
	public static final String URL = "/messageUrl";
	public static final String MODERATORS = "/moderators";
	public static final String SUBSCRIPTION = "/subscription";
	public static final String FILE_HANDLE_BATCH = "/fileHandle/batch";
	public static final String THREAD_COUNTS = "/threadcounts";
	public static final String ATTACHMENT_HANDLES = "attachmenthandles";
	private static final String PROFILE_IMAGE = "/image";
	private static final String PROFILE_IMAGE_PREVIEW = PROFILE_IMAGE+"/preview";
	private static final String REDIRECT_PARAMETER = "redirect=";
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
	public static final String DOCKER_COMMIT = "/dockerCommit";
	public static final String OFFSET_PARAMETER = "offset=";
	public static final String LIMIT_PARAMETER = "limit=";
	public static final String OBJECT_TYPE_PARAMETER = "objectType=";
	private static final String NEXT_PAGE_TOKEN_PARAM = "nextPageToken=";
	public static final String SKIP_TRASH_CAN_PARAM = "skipTrashCan";
	private static final String ASCENDING_PARAM = "ascending=";
	
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
	
	public String repoServiceUrl,fileServiceUrl, authServiceUrl, synapseVersionInfo; 
	@Inject
	public SynapseJavascriptClient(
			JSONObjectAdapter jsonObjectAdapter,
			SynapseProperties synapseProperties,
			GWTWrapper gwt,
			SynapseJavascriptFactory jsFactory,
			PortalGinInjector ginInjector,
			SynapseJSNIUtils jsniUtils) {
		this.authController = ginInjector.getAuthenticationController();
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.synapseProperties = synapseProperties;
		this.gwt = gwt;
		this.jsFactory = jsFactory;
		this.ginInjector = ginInjector;
		this.jsniUtils = jsniUtils;
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
	
	private void doDelete(String url, AsyncCallback callback) {
		RequestBuilderWrapper requestBuilder = ginInjector.getRequestBuilder();
		requestBuilder.configure(DELETE, url);
		requestBuilder.setHeader(ACCEPT, APPLICATION_JSON_CHARSET_UTF8);
		if (authController.isLoggedIn()) {
			requestBuilder.setHeader(SESSION_TOKEN_HEADER, authController.getCurrentUserSessionToken());
		}
		sendRequest(requestBuilder, null, OBJECT_TYPE.None, INITIAL_RETRY_REQUEST_DELAY_MS, callback);
	}

	private void doGet(String url, OBJECT_TYPE responseType, AsyncCallback callback) {
		doGet(url, responseType, APPLICATION_JSON_CHARSET_UTF8, authController.getCurrentUserSessionToken(), callback);
	}
	
	public void doGetString(String url, boolean forceAnonymous, AsyncCallback callback) {
		String sessionToken = forceAnonymous ? null : authController.getCurrentUserSessionToken();
		doGet(url, OBJECT_TYPE.String, null, sessionToken, callback);
	}
	
	private void doGet(String url, OBJECT_TYPE responseType, String acceptedResponseType, String sessionToken, AsyncCallback callback) {
		RequestBuilderWrapper requestBuilder = ginInjector.getRequestBuilder();
		requestBuilder.configure(GET, url);
		if (acceptedResponseType != null) {
			requestBuilder.setHeader(ACCEPT, acceptedResponseType);	
		}
		if (sessionToken != null) {
			requestBuilder.setHeader(SESSION_TOKEN_HEADER, sessionToken);
		}
		sendRequest(requestBuilder, null, responseType, INITIAL_RETRY_REQUEST_DELAY_MS, callback);
	}
	
	private void doPostOrPut(RequestBuilder.Method method, String url, JSONEntity requestObject, OBJECT_TYPE responseType, AsyncCallback callback) {
		String requestData = null;
		if (requestObject != null) {
			try {
				JSONObjectAdapter adapter = jsonObjectAdapter.createNew();
				requestObject.writeToJSONObject(adapter);
				requestData = adapter.toJSONString();
			} catch (JSONObjectAdapterException exception) {
				callback.onFailure(exception);
				return;
			}
		}
		RequestBuilderWrapper requestBuilder = ginInjector.getRequestBuilder();
		requestBuilder.configure(method, url);
		requestBuilder.setHeader(ACCEPT, APPLICATION_JSON_CHARSET_UTF8);
		requestBuilder.setHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF8);
		if (authController.isLoggedIn()) {
			requestBuilder.setHeader(SESSION_TOKEN_HEADER, authController.getCurrentUserSessionToken());
		}
		sendRequest(requestBuilder, requestData, responseType, INITIAL_RETRY_REQUEST_DELAY_MS, callback);
	}
	
	private void doPost(String url, JSONEntity requestObject, OBJECT_TYPE responseType, AsyncCallback callback) {
		doPostOrPut(POST, url, requestObject, responseType, callback);
	}
	
	private void doPut(String url, JSONEntity requestObject, OBJECT_TYPE responseType, AsyncCallback callback) {
		doPostOrPut(PUT, url, requestObject, responseType, callback);
	}

	
	private void sendRequest(final RequestBuilderWrapper requestBuilder, final String requestData, final OBJECT_TYPE responseType, final int retryDelay, final AsyncCallback callback) {
		try {
			requestBuilder.sendRequest(requestData, new RequestCallback() {
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
									sendRequest(requestBuilder, requestData, responseType, retryDelay*2, callback);
								}
							}, retryDelay);
						} else {
							// getException() based on status code,
							// instead of using org.sagebionetworks.client.ClientUtils.throwException() and ExceptionUtil.convertSynapseException() (neither of which can be referenced here)
							String responseText = response.getStatusText();
							try {
								// try to get the reason
								JSONObjectAdapter jsonObject = jsonObjectAdapter.createNew(response.getText());
								if (jsonObject.has("reason")) {
									responseText = jsonObject.get("reason").toString();
								}
							} catch (Exception e) {
								jsniUtils.consoleError("Unable to get reason for error: " + e.getMessage());
							}
							onError(request, getException(statusCode, responseText));
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
		} catch (final Exception e) {
			if (callback != null) {
				callback.onFailure(e);	
			}
		}
	}
	
	public static RestServiceException getException(int statusCode, String reasonStr) {
		switch (statusCode) {
			case SC_UNAUTHORIZED :
				return new UnauthorizedException(reasonStr);
			case SC_FORBIDDEN :
				return new ForbiddenException(reasonStr);
			case SC_NOT_FOUND :
				return new NotFoundException(reasonStr);
			case SC_BAD_REQUEST :
				return new BadRequestException(reasonStr);
			case SC_LOCKED :
				return new LockedException(reasonStr);
			case SC_PRECONDITION_FAILED :
				return new ConflictingUpdateException(reasonStr);
			case SC_GONE : 
				return new BadRequestException(reasonStr);
			case TOO_MANY_REQUESTS_STATUS_CODE :
				return new TooManyRequestsException(reasonStr);
			case SC_SERVICE_UNAVAILABLE :
				return new SynapseDownException(reasonStr);
			case SC_CONFLICT :
				return new ConflictException(reasonStr);
			default :
				return new UnknownErrorException(reasonStr);
		}
	}
	
	public void getEntityBundle(String entityId, int partsMask, final AsyncCallback<EntityBundle> callback) {
		getEntityBundleForVersion(entityId, null, partsMask, callback);
	}

	public void getJSON(String uri, AsyncCallback<JSONObjectAdapter> callback) {
		String url = getRepoServiceUrl() + uri;
		doGet(url, OBJECT_TYPE.JSON, callback);
	}
	public void getEntityBundleForVersion(String entityId, Long versionNumber, int partsMask, final AsyncCallback<EntityBundle> callback) {
		String url = getRepoServiceUrl() + ENTITY + "/" + entityId;
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

	public void createTeam(Team team, final AsyncCallback<Team> callback) {
		String url = getRepoServiceUrl() + TEAM;
		doPost(url, team, OBJECT_TYPE.Team, callback);
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
		doPost(url, request, OBJECT_TYPE.RestrictionInformationResponse, callback);
	}
	
	public void getEntityChildren(EntityChildrenRequest request, final AsyncCallback<EntityChildrenResponse> callback) {
		String url = getRepoServiceUrl() + ENTITY + CHILDREN;
		doPost(url, request, OBJECT_TYPE.EntityChildrenResponse, callback);
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
		doPost(url, idList, OBJECT_TYPE.ListWrapperUserProfile, callback);
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
		doPost(url, list, OBJECT_TYPE.UserGroupHeaderResponse, callback);
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
		doPost(url, topic, OBJECT_TYPE.SubscriberPagedResults, callback);
	}

	public void getSubscribersCount(Topic topic, AsyncCallback<Long> callback) {
		String url = getRepoServiceUrl() + SUBSCRIPTION+"/subscribers/count";
		doPost(url, topic, OBJECT_TYPE.SubscriberCount, callback);
	}
	
	public void getFileHandleAndUrlBatch(BatchFileRequest request, AsyncCallback<BatchFileResult> callback) {
		String url = getFileServiceUrl() + FILE_HANDLE_BATCH;
		doPost(url, request, OBJECT_TYPE.BatchFileResult, callback);
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
		doPost(url, refList, OBJECT_TYPE.PaginatedResultsEntityHeader, callback);
	}

	public FluentFuture<MembershipInvitation> getMembershipInvitation(MembershipInvtnSignedToken token) {
		String url = getRepoServiceUrl() + MEMBERSHIP_INVITATION + "/" + token.getMembershipInvitationId();
		return getFuture(cb -> doPost(url, token, OBJECT_TYPE.MembershipInvitation, cb));
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
	
	public void updateV2WikiOrderHint(WikiPageKey key, V2WikiOrderHint toUpdate, AsyncCallback<V2WikiOrderHint> callback) {
		String url = getRepoServiceUrl() + "/" +
			key.getOwnerObjectType().toLowerCase() + "/" + 
			key.getOwnerObjectId() + WIKI_ORDER_HINT;
		doPut(url, toUpdate, OBJECT_TYPE.V2WikiOrderHint, callback);
	}
	
	public FluentFuture<Entity> createEntity(Entity entity) {
		String url = getRepoServiceUrl() + ENTITY;
		return getFuture(cb -> doPost(url, entity, OBJECT_TYPE.Entity, cb));
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
		return getFuture(cb -> doPost(url, entry, OBJECT_TYPE.None, cb));
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
		return getFuture(cb -> doPost(url, idList, OBJECT_TYPE.ListWrapperTeam, cb));
	}
	
	public void login(LoginRequest loginRequest, AsyncCallback<LoginResponse> callback) {
		String url = getAuthServiceUrl() + "/login";
		doPost(url, loginRequest, OBJECT_TYPE.LoginResponse, callback);
	}
	
	public void logout() {
		String url = getAuthServiceUrl() + "/session";
		doDelete(url, null);
	}

	public void getMyProjects(ProjectListType projectListType, int limit, int offset, ProjectListSortColumn sortBy, SortDirection sortDir, AsyncCallback<List<ProjectHeader>> projectHeadersCallback) {
		getProjects(projectListType, null, null, limit, offset, sortBy, sortDir, projectHeadersCallback);
	}
	
	public void getProjectsForTeam(String teamId, int limit, int offset, ProjectListSortColumn sortBy, SortDirection sortDir, AsyncCallback<List<ProjectHeader>> projectHeadersCallback) {
		getProjects(ProjectListType.TEAM_PROJECTS, null, teamId, limit, offset, sortBy, sortDir, projectHeadersCallback);
	}
	
	public void getUserProjects(String userId, int limit, int offset, ProjectListSortColumn sortBy, SortDirection sortDir, AsyncCallback<List<ProjectHeader>> projectHeadersCallback) {
		getProjects(ProjectListType.OTHER_USER_PROJECTS, userId, null, limit, offset, sortBy, sortDir, projectHeadersCallback);
	}
	
	private void getProjects(ProjectListType projectListType, String userId, String teamId, int limit, int offset, ProjectListSortColumn sortBy, SortDirection sortDir, AsyncCallback<List<ProjectHeader>> projectHeadersCallback) {
		String url = getRepoServiceUrl() + PROJECTS_URI_PATH + '/' + projectListType.name();
		if (userId != null) {
			url += USER + '/' + userId;
		}
		if (teamId != null) {
			url += TEAM + '/' + teamId;
		}

		if (sortBy == null) {
			sortBy = ProjectListSortColumn.LAST_ACTIVITY;
		}
		if (sortDir == null) {
			sortDir = SortDirection.DESC;
		}

		url += '?' + OFFSET_PARAMETER + offset + '&' + LIMIT_PARAMETER + limit + "&sort=" + sortBy.name() + "&sortDirection="
				+ sortDir.name();
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
		doPost(getEndpoint(type) + url, request, OBJECT_TYPE.AsyncJobId, callback);
	}
	
	public void getEtag(String objectId, ObjectType objectType, AsyncCallback<Etag> callback) {
		String url =  getRepoServiceUrl() + OBJECT+"/"+objectId+"/"+objectType.name()+"/"+ETAG;
		doGet(url, OBJECT_TYPE.Etag, callback);
	}
	
	public void getEntitiesGeneratedBy(String activityId, Integer limit, Integer offset, AsyncCallback<ArrayList<Reference>> callback) {
		String url = getRepoServiceUrl() + ACTIVITY_URI_PATH + "/" + activityId + GENERATED_PATH + "?" + OFFSET_PARAMETER + offset + "&" + LIMIT_PARAMETER + limit;
		doGet(url, OBJECT_TYPE.PaginatedResultReference, callback);
	}
	
	public void getActivityForEntityVersion(String entityId, Long versionNumber, AsyncCallback<Activity> callback) {
		String url = getRepoServiceUrl() + ENTITY + "/" + entityId;
		if (versionNumber != null) {
			url += REPO_SUFFIX_VERSION + "/" + versionNumber;
		}
		url += GENERATED_BY_SUFFIX;
		doGet(url, OBJECT_TYPE.Activity, callback);
	}

	public void getDockerCommits(
			String entityId, 
			Long limit, 
			Long offset,
			DockerCommitSortBy sortBy, 
			Boolean ascending,
			AsyncCallback<ArrayList<DockerCommit>> callback) {
		String url = getRepoServiceUrl() + ENTITY+"/"+entityId+DOCKER_COMMIT;
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
		doPost(url, request, OBJECT_TYPE.MultipartUploadStatus, callback);
	}

	public void getMultipartPresignedUrlBatch(BatchPresignedUploadUrlRequest request, AsyncCallback<BatchPresignedUploadUrlResponse> callback) {
		String url = getFileServiceUrl() + "/file/multipart/" + request.getUploadId() + "/presigned/url/batch";
		doPost(url, request, OBJECT_TYPE.BatchPresignedUploadUrlResponse, callback);
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
		doPost(url, request, OBJECT_TYPE.PrincipalAliasResponse, callback);
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
		doPost(url, request, OBJECT_TYPE.EntityId, callback);
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
		doPost(url, request, OBJECT_TYPE.DownloadList, callback);
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
		doPost(url, request, OBJECT_TYPE.DownloadList, callback);
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
		doPost(url, null, OBJECT_TYPE.DownloadOrder, callback);
	}

	public void getDownloadOrder(String orderId, AsyncCallback<DownloadOrder> callback) {
		String url = getFileServiceUrl() + "/download/order/"+orderId;
		doGet(url, OBJECT_TYPE.DownloadOrder, callback);
	}
	
	public void getDownloadOrderHistory(DownloadOrderSummaryRequest request, AsyncCallback<DownloadOrderSummaryResponse> callback) {
		String url = getFileServiceUrl() + "/download/order/history";
		doPost(url, request, OBJECT_TYPE.DownloadOrderSummaryResponse, callback);
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
		doPost(url, s, OBJECT_TYPE.None, callback);
	}
}

