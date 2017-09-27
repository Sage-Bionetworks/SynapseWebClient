package org.sagebionetworks.web.client;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.Count;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.PaginatedIds;
import org.sagebionetworks.repo.model.Preview;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.RestrictionInformationResponse;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.dao.WikiPageKey;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.EntityThreadCounts;
import org.sagebionetworks.repo.model.discussion.Forum;
import org.sagebionetworks.repo.model.discussion.MessageURL;
import org.sagebionetworks.repo.model.discussion.ThreadCount;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.repo.model.principal.UserGroupHeaderResponse;
import org.sagebionetworks.repo.model.subscription.SubscriberCount;
import org.sagebionetworks.repo.model.subscription.SubscriberPagedResults;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;

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
		UserGroupHeaderResponse,
		UserBundle,
		Count,
		PaginatedResultsEntityHeader,
		V2WikiPage,
		DockerRepository,
		FileEntity,
		Project,
		Folder,
		EntityView,
		TableEntity,
		Link,
		Preview,
		Entity, // used for services where we don't know what type of entity is returned (but object has concreteType set)
		Forum,
		DiscussionThreadBundle,
		DiscussionReplyBundle,
		MessageURL,
		ThreadCount,
		EntityThreadCounts,
		PaginatedIds,
		SubscriberPagedResults,
		SubscriberCount
	}

	/**
	 * Create a new instance of a concrete class using the object type
	 * @throws JSONObjectAdapterException 
	 */
	public Object newInstance(OBJECT_TYPE type, JSONObjectAdapter json) throws JSONObjectAdapterException {
		if (OBJECT_TYPE.Entity.equals(type) && json.has("concreteType")) {
			// attempt to construct based on concreteType
			String concreteType = json.getString("concreteType");
			if (FileEntity.class.getName().equals(concreteType)) {
				type = OBJECT_TYPE.FileEntity;
			} else if (Folder.class.getName().equals(concreteType)) {
				type = OBJECT_TYPE.Folder;
			} else if (EntityView.class.getName().equals(concreteType)) {
				type = OBJECT_TYPE.EntityView;
			} else if (TableEntity.class.getName().equals(concreteType)) {
				type = OBJECT_TYPE.TableEntity;
			} else if (Project.class.getName().equals(concreteType)) {
				type = OBJECT_TYPE.Project;
			} else if (Link.class.getName().equals(concreteType)) {
				type = OBJECT_TYPE.Link;
			} else if (Preview.class.getName().equals(concreteType)) {
				type = OBJECT_TYPE.Preview;
			} else if (DockerRepository.class.getName().equals(concreteType)) {
				type = OBJECT_TYPE.DockerRepository;
			} else {
				throw new IllegalArgumentException("No match found for : "+ concreteType);
			}
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
		case PaginatedResultsEntityHeader :
			// json really represents a PaginatedResults (cannot reference here in js)
			List<EntityHeader> entityHeaderList = new ArrayList<>();
			JSONArrayAdapter resultsJsonArray = json.getJSONArray("results");
			for (int i = 0; i < resultsJsonArray.length(); i++) {
				JSONObjectAdapter jsonObject = resultsJsonArray.getJSONObject(i);
				entityHeaderList.add(new EntityHeader(jsonObject));
			}
			return entityHeaderList;
		case ListWrapperUserProfile :
			// json really represents a ListWrapper, but we can't reference ListWrapper here because it uses Class.forName() (breaks gwt compile)
			List<UserProfile> list = new ArrayList<>();
			JSONArrayAdapter jsonArray = json.getJSONArray("list");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObjectAdapter jsonObject = jsonArray.getJSONObject(i);
				list.add(new UserProfile(jsonObject));
			}

			return list;
		default:
			throw new IllegalArgumentException("No match found for : "+ type);
		}
	}
}

