package org.sagebionetworks.web.client.utils;

import java.util.Date;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.web.client.place.Wiki;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
/**
 * Extracted from DisplayUtils
 * 
 * 
 */
public class WikiUtils {

	public static String getSynapseWikiHistoryToken(String ownerId,
			String objectType, String wikiPageId) {
		Wiki place = new Wiki(ownerId, objectType, wikiPageId);
		return "#!" + getWikiPlaceString(Wiki.class) + ":" + place.toToken();
	}

	private static String getWikiPlaceString(Class<Wiki> place) {
		String fullPlaceName = place.getName();
		fullPlaceName = fullPlaceName.replaceAll(".+\\.", "");
		return fullPlaceName;
	}

	/**
	 * Create the url to a wiki filehandle.
	 * 
	 * @param baseURl
	 * @param id
	 * @param tokenId
	 * @param fileName
	 * @return
	 */
	public static String createWikiAttachmentUrl(String baseFileHandleUrl,
			WikiPageKey wikiKey, String fileName, boolean preview) {
		// direct approach not working. have the filehandleservlet redirect us
		// to the temporary wiki attachment url instead
		// String attachmentPathName = preview ? "attachmentpreview" :
		// "attachment";
		// return repoServicesUrl
		// +"/" +wikiKey.getOwnerObjectType().toLowerCase()
		// +"/"+ wikiKey.getOwnerObjectId()
		// +"/wiki/"
		// +wikiKey.getWikiPageId()
		// +"/"+
		// attachmentPathName+"?fileName="+URL.encodePathSegment(fileName);
		String wikiIdParam = wikiKey.getWikiPageId() == null ? "" : "&"
				+ WebConstants.WIKI_ID_PARAM_KEY + "="
				+ wikiKey.getWikiPageId();

		// if preview, then avoid cache
		String nocacheParam = preview ? "&nocache=" + new Date().getTime() : "";
		return baseFileHandleUrl + "?" + WebConstants.WIKI_OWNER_ID_PARAM_KEY
				+ "=" + wikiKey.getOwnerObjectId() + "&"
				+ WebConstants.WIKI_OWNER_TYPE_PARAM_KEY + "="
				+ wikiKey.getOwnerObjectType() + "&"
				+ WebConstants.WIKI_FILENAME_PARAM_KEY + "=" + fileName + "&"
				+ WebConstants.FILE_HANDLE_PREVIEW_PARAM_KEY + "="
				+ Boolean.toString(preview) + wikiIdParam + nocacheParam;
	}
	
	public static boolean isWikiSupportedType(Entity entity) {
		return (entity instanceof FileEntity || entity instanceof Folder || entity instanceof Project); 
	}
}
