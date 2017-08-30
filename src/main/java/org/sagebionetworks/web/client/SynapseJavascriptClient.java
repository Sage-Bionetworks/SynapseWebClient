package org.sagebionetworks.web.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpStatus;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseTooManyRequestsException;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.RestrictionInformationRequest;
import org.sagebionetworks.repo.model.RestrictionInformationResponse;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.shared.WebConstants;
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

import com.google.gwt.core.shared.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class SynapseJavascriptClient {
	RequestBuilderWrapper requestBuilder;
	AuthenticationController authController;
	JSONObjectAdapter jsonObjectAdapter;
	GlobalApplicationState globalAppState;
	GWTWrapper gwt;

	public static final String ENTITY_URI_PATH = "/entity";
	public static final String ENTITY_BUNDLE_PATH = "/bundle?mask=";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String ACCEPT = "Accept";
	public static final String SESSION_TOKEN_HEADER = "sessionToken";
	public static final String USER_AGENT = "User-Agent";
	public static final String SYNAPSE_ENCODING_CHARSET = "UTF-8";
	public static final String APPLICATION_JSON_CHARSET_UTF8 = "application/json; charset="+SYNAPSE_ENCODING_CHARSET;
	public static final String REPO_SUFFIX_VERSION = "/version";
	public static final String TEAM = "/team";
	public static final String WIKI_VERSION_PARAMETER = "?wikiVersion=";
	public static final String USER_GROUP_HEADER_PREFIX_PATH = "/userGroupHeaders?prefix=";
	public static final String OFFSET_PARAMETER = "offset=";
	public static final String LIMIT_PARAMETER = "limit=";
	
	public String repoServiceUrl; 
	@Inject
	public SynapseJavascriptClient(
			RequestBuilderWrapper requestBuilder,
			AuthenticationController authController,
			JSONObjectAdapter jsonObjectAdapter,
			GlobalApplicationState globalAppState,
			GWTWrapper gwt) {
		this.requestBuilder = requestBuilder;
		this.authController = authController;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.gwt = gwt;
		repoServiceUrl = globalAppState.getSynapseProperty(WebConstants.REPO_SERVICE_URL_KEY);
	}

	private void doGet(String url, AsyncCallback<JSONObjectAdapter> callback) {
		requestBuilder.configure(RequestBuilder.GET, url);
		requestBuilder.setHeader(ACCEPT, APPLICATION_JSON_CHARSET_UTF8);
		if (authController.isLoggedIn()) {
			requestBuilder.setHeader(SESSION_TOKEN_HEADER, authController.getCurrentUserSessionToken());
		}
		sendRequest(url, null, callback);
	}
	
	private void doPost(String url, String requestData, AsyncCallback<JSONObjectAdapter> callback) {
		requestBuilder.configure(RequestBuilder.POST, url);
		requestBuilder.setHeader(ACCEPT, APPLICATION_JSON_CHARSET_UTF8);
		requestBuilder.setHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF8);
		if (authController.isLoggedIn()) {
			requestBuilder.setHeader(SESSION_TOKEN_HEADER, authController.getCurrentUserSessionToken());
		}
		sendRequest(url, requestData, callback);
	}
	
	private void sendRequest(final String url, String requestData, final AsyncCallback<JSONObjectAdapter> callback) {
		try {
			requestBuilder.sendRequest(requestData, new RequestCallback() {
				@Override
				public void onResponseReceived(Request request,
						Response response) {
					int statusCode = response.getStatusCode();
					if (statusCode == Response.SC_OK) {
						try {
							JSONObjectAdapter jsonObject = jsonObjectAdapter.createNew(response.getText());
							callback.onSuccess(jsonObject);
						} catch (JSONObjectAdapterException e) {
							onError(null, e);
						}
					} else {
						if (statusCode == SynapseTooManyRequestsException.TOO_MANY_REQUESTS_STATUS_CODE) {
							// wait a couple of seconds and try the request again...
							gwt.scheduleExecution(new Callback() {
								@Override
								public void invoke() {
									doGet(url, callback);
								}
							}, 2000);
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
		if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
			return new UnauthorizedException(reasonStr);
		} else if (statusCode == HttpStatus.SC_FORBIDDEN) {
			return new ForbiddenException(reasonStr);
		} else if (statusCode == HttpStatus.SC_NOT_FOUND) {
			return new NotFoundException(reasonStr);
		} else if (statusCode == HttpStatus.SC_BAD_REQUEST) {
			return new BadRequestException(reasonStr);
		} else if (statusCode == HttpStatus.SC_LOCKED) {
			return new LockedException(reasonStr);
		} else if (statusCode == HttpStatus.SC_PRECONDITION_FAILED) {
			return new ConflictingUpdateException(reasonStr);
		} else if (statusCode == HttpStatus.SC_GONE) {
			return new BadRequestException(reasonStr);
		} else if (statusCode == SynapseTooManyRequestsException.TOO_MANY_REQUESTS_STATUS_CODE){
			return new TooManyRequestsException(reasonStr);
		}else {
			return new UnknownErrorException(reasonStr);
		}
	}

	public AsyncCallback<JSONObjectAdapter> wrapCallback(final CallbackP<JSONObjectAdapter> constructCallback, final AsyncCallback callback) {
		return  new AsyncCallback<JSONObjectAdapter>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
			@Override
			public void onSuccess(JSONObjectAdapter json) {
				constructCallback.invoke(json);
			}
		};
	}

	public void getEntityBundle(String entityId, int partsMask, final AsyncCallback<EntityBundle> callback) {
		getEntityBundleForVersion(entityId, null, partsMask, callback);
	}

	public void getEntityBundleForVersion(String entityId, Long versionNumber, int partsMask, final AsyncCallback<EntityBundle> callback) {
		String url = repoServiceUrl + ENTITY_URI_PATH + "/" + entityId + ENTITY_BUNDLE_PATH + partsMask;
		if (versionNumber != null) {
			url += REPO_SUFFIX_VERSION + "/" + versionNumber;
		}
		CallbackP<JSONObjectAdapter> constructCallback = new CallbackP<JSONObjectAdapter>() {
			@Override
			public void invoke(JSONObjectAdapter json) {
				try {
					callback.onSuccess(new EntityBundle(json));
				} catch (JSONObjectAdapterException e) {
					callback.onFailure(e);
				}
			}
		};
		doGet(url, wrapCallback(constructCallback, callback));
	}

	public void getTeam(String teamId, final AsyncCallback<Team> callback) {
		String url = repoServiceUrl + TEAM + "/" + teamId;
		CallbackP<JSONObjectAdapter> constructCallback = new CallbackP<JSONObjectAdapter>() {
			@Override
			public void invoke(JSONObjectAdapter json) {
				try {
					callback.onSuccess(new Team(json));
				} catch (JSONObjectAdapterException e) {
					callback.onFailure(e);
				}
			}
		};
		doGet(url, wrapCallback(constructCallback, callback));
	}
	
	public void getRestrictionInformation(String subjectId, RestrictableObjectType type, final AsyncCallback<RestrictionInformationResponse> callback)  {
		String url = repoServiceUrl + "/restrictionInformation";
		CallbackP<JSONObjectAdapter> constructCallback = new CallbackP<JSONObjectAdapter>() {
			@Override
			public void invoke(JSONObjectAdapter json) {
				try {
					callback.onSuccess(new RestrictionInformationResponse(json));
				} catch (JSONObjectAdapterException e) {
					callback.onFailure(e);
				}
			}
		};
		RestrictionInformationRequest request = new RestrictionInformationRequest();
		request.setObjectId(subjectId);
		request.setRestrictableObjectType(type);
		try {
			JSONObjectAdapter jsonAdapter = jsonObjectAdapter.createNew();
			request.writeToJSONObject(jsonAdapter);
			doPost(url, jsonAdapter.toJSONString(), wrapCallback(constructCallback, callback));
		} catch (JSONObjectAdapterException e) {
			callback.onFailure(e);
		}
	}
	
	public void getEntityChildren(EntityChildrenRequest request, final AsyncCallback<EntityChildrenResponse> callback) {
		String url = repoServiceUrl + ENTITY_URI_PATH + "/children";
		CallbackP<JSONObjectAdapter> constructCallback = new CallbackP<JSONObjectAdapter>() {
			@Override
			public void invoke(JSONObjectAdapter json) {
				try {
					callback.onSuccess(new EntityChildrenResponse(json));
				} catch (JSONObjectAdapterException e) {
					callback.onFailure(e);
				}
			}
		};
		try {
			JSONObjectAdapter jsonAdapter = jsonObjectAdapter.createNew();
			request.writeToJSONObject(jsonAdapter);
			doPost(url, jsonAdapter.toJSONString(), wrapCallback(constructCallback, callback));
		} catch (JSONObjectAdapterException e) {
			callback.onFailure(e);
		}
	}
	
	public void getV2WikiPageAsV1(WikiPageKey key, AsyncCallback<WikiPage> callback) {
		getVersionOfV2WikiPageAsV1(key, null, callback);
	}
	
	public void getVersionOfV2WikiPageAsV1(final WikiPageKey key, final Long versionNumber, final AsyncCallback<WikiPage> callback) {
		if (key.getWikiPageId() == null) {
			// get the root wiki page id first
			String url = repoServiceUrl + "/" +
					key.getOwnerObjectType().toLowerCase() + "/" + 
					key.getOwnerObjectId() + "/wikikey";
			CallbackP<JSONObjectAdapter> constructCallback = new CallbackP<JSONObjectAdapter>() {
				@Override
				public void invoke(JSONObjectAdapter json) {
					try {
						String wikiPageId = new org.sagebionetworks.repo.model.dao.WikiPageKey(json).getWikiPageId();
						key.setWikiPageId(wikiPageId);
						getVersionOfV2WikiPageAsV1WithWikiPageId(key, versionNumber, callback);
					} catch (JSONObjectAdapterException e) {
						callback.onFailure(e);
					}
				}
			};
			doGet(url, wrapCallback(constructCallback, callback));
		} else {
			getVersionOfV2WikiPageAsV1WithWikiPageId(key, versionNumber, callback);
		}
	}
	
	private void getVersionOfV2WikiPageAsV1WithWikiPageId(WikiPageKey key, Long versionNumber, final AsyncCallback<WikiPage> callback) {
		String url = repoServiceUrl + "/" +
				key.getOwnerObjectType().toLowerCase() + "/" + 
				key.getOwnerObjectId() + "/wiki/" +
				key.getWikiPageId();
		if (versionNumber != null) {
			url += WIKI_VERSION_PARAMETER + versionNumber;
		}
				
		CallbackP<JSONObjectAdapter> constructCallback = new CallbackP<JSONObjectAdapter>() {
			@Override
			public void invoke(JSONObjectAdapter json) {
				try {
					callback.onSuccess(new WikiPage(json));
				} catch (JSONObjectAdapterException e) {
					callback.onFailure(e);
				}
			}
		};
		doGet(url, wrapCallback(constructCallback, callback));
	}
	
	public void getUserGroupHeadersByPrefix(String prefix, TypeFilter type, long limit, long offset, AsyncCallback<UserGroupHeaderResponsePage> callback) {
		String encodedPrefix = gwt.encodeQueryString(prefix);
		StringBuilder builder = new StringBuilder();
		builder.append(USER_GROUP_HEADER_PREFIX_PATH);
		builder.append(encodedPrefix);
		builder.append("&" + LIMIT_PARAMETER + limit);
		builder.append( "&" + OFFSET_PARAMETER + offset);
		if(type != null){
			builder.append("&typeFilter="+type.name());
		}
		return getJSONEntity(getRepoEndpoint(), builder.toString(), UserGroupHeaderResponsePage.class);
	}
}
