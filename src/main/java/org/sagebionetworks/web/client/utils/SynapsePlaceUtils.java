package org.sagebionetworks.web.client.utils;
import org.sagebionetworks.web.client.place.Synapse;
/**
 * Extracted from DispalyUtils.
 *
 */
public class SynapsePlaceUtils {
	
	public static String getSynapseHistoryToken(String entityId) {
		return "#" + getSynapseHistoryTokenNoHash(entityId, null);
	}
	
	public static String getSynapseHistoryTokenNoHash(String entityId) {
		return getSynapseHistoryTokenNoHash(entityId, null);
	}
	
	public static String getSynapseHistoryToken(String entityId, Long versionNumber) {
		return "#" + getSynapseHistoryTokenNoHash(entityId, versionNumber);
	}
	
	public static String getSynapseHistoryTokenNoHash(String entityId, Long versionNumber) {
		Synapse place = new Synapse(entityId, versionNumber);
		return "!"+ getPlaceString(Synapse.class) + ":" + place.toToken();
	}
	
	/*
	 * Private methods
	 */
	private static String getPlaceString(Class<Synapse> place) {
		String fullPlaceName = place.getName();		
		fullPlaceName = fullPlaceName.replaceAll(".+\\.", "");
		return fullPlaceName;
	}

	public static String createEntityLink(String id, String version,
			String display) {
		return "<a href=\"" + getSynapseHistoryToken(id) + "\">" + display + "</a>";
	}
}
