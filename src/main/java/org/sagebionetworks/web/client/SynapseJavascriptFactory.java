package org.sagebionetworks.web.client;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.ChallengePagedResults;
import org.sagebionetworks.repo.model.Count;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityId;
import org.sagebionetworks.repo.model.EntityInstanceFactory;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.InviteeVerificationSignedToken;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.repo.model.PaginatedIds;
import org.sagebionetworks.repo.model.PaginatedTeamIds;
import org.sagebionetworks.repo.model.Preview;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.ProjectHeader;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.RestrictionInformationResponse;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.asynch.AsyncJobId;
import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBodyInstanceFactory;
import org.sagebionetworks.repo.model.auth.LoginResponse;
import org.sagebionetworks.repo.model.dao.WikiPageKey;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.EntityThreadCounts;
import org.sagebionetworks.repo.model.discussion.Forum;
import org.sagebionetworks.repo.model.discussion.MessageURL;
import org.sagebionetworks.repo.model.discussion.ThreadCount;
import org.sagebionetworks.repo.model.docker.DockerCommit;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.repo.model.doi.v2.Doi;
import org.sagebionetworks.repo.model.doi.v2.DoiResponse;
import org.sagebionetworks.repo.model.file.AddPartResponse;
import org.sagebionetworks.repo.model.file.BatchFileResult;
import org.sagebionetworks.repo.model.file.BatchPresignedUploadUrlResponse;
import org.sagebionetworks.repo.model.file.DownloadList;
import org.sagebionetworks.repo.model.file.DownloadOrder;
import org.sagebionetworks.repo.model.file.DownloadOrderSummaryResponse;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.repo.model.file.MultipartUploadStatus;
import org.sagebionetworks.repo.model.file.UploadDestination;
import org.sagebionetworks.repo.model.file.UploadDestinationInstanceFactory;
import org.sagebionetworks.repo.model.principal.PrincipalAliasResponse;
import org.sagebionetworks.repo.model.principal.UserGroupHeaderResponse;
import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.subscription.Etag;
import org.sagebionetworks.repo.model.subscription.SubscriberCount;
import org.sagebionetworks.repo.model.subscription.SubscriberPagedResults;
import org.sagebionetworks.repo.model.subscription.Subscription;
import org.sagebionetworks.repo.model.subscription.SubscriptionPagedResults;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiOrderHint;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.shared.exceptions.ResultNotReadyException;

public class SynapseJavascriptFactory {
	public enum OBJECT_TYPE {
		EntityBundle,
		Team,
		RestrictionInformationResponse,
		EntityChildrenResponse,
		WikiPageKey,
		UserGroupHeaderResponsePage,
		WikiPage,
		ListWrapperUserProfile,
		ListWrapperTeam,
		ListWrapperUploadDestinations,
		SubscriptionPagedResults,
		UserGroupHeaderResponse,
		UserBundle,
		Count,
		PaginatedResultsEntityHeader,
		PaginatedResultProjectHeader,
		PaginatedResultReference,
		V2WikiPage,
		V2WikiOrderHint,
		DockerRepository,
		PaginatedDockerCommit,
		FileEntity,
		Project,
		Folder,
		EntityView,
		TableEntity,
		Link,
		Preview,
		Entity, // used for services where we don't know what type of entity is returned (but object has concreteType set)
		EntityId,
		Forum,
		DiscussionThreadBundle,
		DiscussionReplyBundle,
		MessageURL,
		ThreadCount,
		EntityThreadCounts,
		PaginatedIds,
		SubscriberPagedResults,
		SubscriberCount,
		BatchFileResult,
		UserProfile,
		FileHandleResults,
		AsyncResponse,
		JSON,
		MembershipInvitation,
		InviteeVerificationSignedToken,
		ListWrapperColumnModel,
		PaginatedTeamIds,
		AsyncJobId,
		LoginResponse,
		ChallengePagedResults,
		Etag,
		Activity,
		MultipartUploadStatus,
		BatchPresignedUploadUrlResponse,
		AddPartResponse,
		PaginatedResultsTotalNumberOfResults,
		PrincipalAliasResponse,
		DownloadList,
		DownloadOrder,
		DownloadOrderSummaryResponse,
		Doi,
		Subscription,
		SearchResults,
		None,
		String
	}

	/**
	 * Create a new instance of a concrete class using the object type
	 * @throws JSONObjectAdapterException 
	 * @throws ResultNotReadyException 
	 */
	public Object newInstance(OBJECT_TYPE type, JSONObjectAdapter json) throws JSONObjectAdapterException, ResultNotReadyException {
		if (OBJECT_TYPE.Entity.equals(type) && json.has("concreteType")) {
			// attempt to construct based on concreteType
			String concreteType = json.getString("concreteType");
			Entity entity = EntityInstanceFactory.singleton().newInstance(concreteType);
			entity.initializeFromJSONObject(json);
			return entity;
		}
		if (OBJECT_TYPE.AsyncResponse.equals(type)) {
			String concreteType = json.getString("concreteType");
			AsynchronousResponseBodyInstanceFactory asyncResponseFactory = AsynchronousResponseBodyInstanceFactory.singleton();
			AsynchronousResponseBody response = asyncResponseFactory.newInstance(concreteType);
			response.initializeFromJSONObject(json);
			return response;
		}
		switch (type) {
		case EntityBundle :
			return new EntityBundle(json);
		case Team :
			return new Team(json);
		case RestrictionInformationResponse :
			return new RestrictionInformationResponse(json);
		case EntityChildrenResponse :
			return new EntityChildrenResponse(json);
		case WikiPageKey :
			return new WikiPageKey(json);
		case UserGroupHeaderResponsePage :
			return new UserGroupHeaderResponsePage(json);
		case WikiPage :
			return new WikiPage(json);
		case UserGroupHeaderResponse :
			return new UserGroupHeaderResponse(json).getList();
		case UserBundle :
			return new UserBundle(json);
		case Count :
			return new Count(json).getCount();
		case V2WikiPage :
			return new V2WikiPage(json);
		case V2WikiOrderHint :
			return new V2WikiOrderHint(json);
		case FileEntity :
			return new FileEntity(json);
		case DockerRepository :
			return new DockerRepository(json);
		case Project :
			return new Project(json);
		case Folder :
			return new Folder(json);
		case EntityView :
			return new EntityView(json);
		case TableEntity :
			return new TableEntity(json);
		case Link :
			return new Link(json);
		case Preview :
			return new Preview(json);
		case Forum :
			return new Forum(json);
		case DiscussionThreadBundle : 
			return new DiscussionThreadBundle(json);
		case DiscussionReplyBundle :
			return new DiscussionReplyBundle(json);
		case MessageURL :
			return new MessageURL(json).getMessageUrl();
		case ThreadCount :
			return new ThreadCount(json).getCount();
		case EntityThreadCounts : 
			return new EntityThreadCounts(json);
		case PaginatedIds : 
			return new PaginatedIds(json);
		case SubscriberPagedResults :
			return new SubscriberPagedResults(json);
		case SubscriberCount :
			return new SubscriberCount(json).getCount();
		case BatchFileResult :
			return new BatchFileResult(json);
		case UserProfile :
			return new UserProfile(json);
		case FileHandleResults :
			return new FileHandleResults(json).getList();
		case PaginatedResultsTotalNumberOfResults :
			return json.getLong("totalNumberOfResults");
		case PrincipalAliasResponse :
			return new PrincipalAliasResponse(json);
		case EntityId :
			return new EntityId(json).getId();
		case DownloadList : 
			return new DownloadList(json);
		case DownloadOrder : 
			return new DownloadOrder(json);
		case DownloadOrderSummaryResponse :
			return new DownloadOrderSummaryResponse(json);
		case ChallengePagedResults:
			return new ChallengePagedResults(json).getResults();
		case SubscriptionPagedResults :
			return new SubscriptionPagedResults(json);
		case Subscription : 
			return new Subscription(json);
		case SearchResults : 
			return new SearchResults(json);
		case JSON :
			return json;
		case PaginatedResultsEntityHeader :
			// json really represents a PaginatedResults (cannot reference here in js)
			List<EntityHeader> entityHeaderList = new ArrayList<>();
			JSONArrayAdapter resultsJsonArray = json.getJSONArray("results");
			for (int i = 0; i < resultsJsonArray.length(); i++) {
				JSONObjectAdapter jsonObject = resultsJsonArray.getJSONObject(i);
				entityHeaderList.add(new EntityHeader(jsonObject));
			}
			return entityHeaderList;
		case PaginatedDockerCommit :
			// json really represents a PaginatedResults (cannot reference here in js)
			List<DockerCommit> dockerCommitList = new ArrayList<>();
			JSONArrayAdapter dockerCommitJsonArray = json.getJSONArray("results");
			for (int i = 0; i < dockerCommitJsonArray.length(); i++) {
				JSONObjectAdapter jsonObject = dockerCommitJsonArray.getJSONObject(i);
				dockerCommitList.add(new DockerCommit(jsonObject));
			}
			return dockerCommitList;
		case PaginatedResultProjectHeader : 
			// json really represents a PaginatedResults (cannot reference here in js)
			List<ProjectHeader> projectHeaderList = new ArrayList<>();
			JSONArrayAdapter projectResultsJsonArray = json.getJSONArray("results");
			for (int i = 0; i < projectResultsJsonArray.length(); i++) {
				JSONObjectAdapter jsonObject = projectResultsJsonArray.getJSONObject(i);
				projectHeaderList.add(new ProjectHeader(jsonObject));
			}
			return projectHeaderList;
		case PaginatedResultReference : 
			// json really represents a PaginatedResults (cannot reference here in js)
			List<Reference> referenceList = new ArrayList<>();
			JSONArrayAdapter referenceResultsJsonArray = json.getJSONArray("results");
			for (int i = 0; i < referenceResultsJsonArray.length(); i++) {
				JSONObjectAdapter jsonObject = referenceResultsJsonArray.getJSONObject(i);
				referenceList.add(new Reference(jsonObject));
			}
			return referenceList;
		case ListWrapperColumnModel :
			List<ColumnModel> columnModelList = new ArrayList<>();
			JSONArrayAdapter columnModelJsonList = json.getJSONArray("list");
			for (int i = 0; i < columnModelJsonList.length(); i++) {
				JSONObjectAdapter jsonObject = columnModelJsonList.getJSONObject(i);
				columnModelList.add(new ColumnModel(jsonObject));
			}
			return columnModelList;
		case ListWrapperUserProfile :
			// json really represents a ListWrapper, but we can't reference ListWrapper here because it uses Class.forName() (breaks gwt compile)
			List<UserProfile> list = new ArrayList<>();
			JSONArrayAdapter jsonArray = json.getJSONArray("list");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObjectAdapter jsonObject = jsonArray.getJSONObject(i);
				list.add(new UserProfile(jsonObject));
			}

			return list;
		case ListWrapperTeam :
			List<Team> teamList = new ArrayList<>();
			JSONArrayAdapter jsonTeamArray = json.getJSONArray("list");
			for (int i = 0; i < jsonTeamArray.length(); i++) {
				JSONObjectAdapter jsonObject = jsonTeamArray.getJSONObject(i);
				teamList.add(new Team(jsonObject));
			}
			return teamList;
		case ListWrapperUploadDestinations :
			List<UploadDestination> uploadDestinationList = new ArrayList<>();
			UploadDestinationInstanceFactory uploadDestinationFactory = UploadDestinationInstanceFactory.singleton();
			JSONArrayAdapter jsonUploadDestinationsArray = json.getJSONArray("list");
			for (int i = 0; i < jsonUploadDestinationsArray.length(); i++) {
				JSONObjectAdapter jsonObject = jsonUploadDestinationsArray.getJSONObject(i);
				String concreteType = jsonObject.getString("concreteType");
				UploadDestination response = uploadDestinationFactory.newInstance(concreteType);
				response.initializeFromJSONObject(jsonObject);
				uploadDestinationList.add(response);
			}
			return uploadDestinationList;
		case Doi:
			return new Doi(json);
		case MembershipInvitation:
			return new MembershipInvitation(json);
		case InviteeVerificationSignedToken:
			return new InviteeVerificationSignedToken(json);
		case PaginatedTeamIds:
			return new PaginatedTeamIds(json);
		case AsyncJobId:
			return new AsyncJobId(json).getToken();
		case LoginResponse:
			return new LoginResponse(json);
		case Etag:
			return new Etag(json);
		case Activity:
			return new Activity(json);
		case MultipartUploadStatus : 
			return new MultipartUploadStatus(json);
		case BatchPresignedUploadUrlResponse :
			return new BatchPresignedUploadUrlResponse(json);
		case AddPartResponse :
			return new AddPartResponse(json);
		default:
			throw new IllegalArgumentException("No match found for : "+ type);
		}
	}
}

