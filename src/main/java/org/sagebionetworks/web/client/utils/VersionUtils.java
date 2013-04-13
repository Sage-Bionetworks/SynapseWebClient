package org.sagebionetworks.web.client.utils;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.web.shared.WebConstants;
/**
 * Extracted from DisplayUtils
 * 
 *
 */
public class VersionUtils {

	public static String getVersionDisplay(Reference ref) {
		if (ref == null) return null;
		return getVersionDisplay(ref.getTargetId(), ref.getTargetVersionNumber());
	}
	
	public static String getVersionDisplay(String id, Long versionNumber) {
		String version = id;
		if(versionNumber != null) {
			version += " (#" + versionNumber + ")";
		}
		return version;		
	}
	public static Reference parseEntityVersionString(String entityVersion) {
		String[] parts = entityVersion.split(WebConstants.ENTITY_VERSION_STRING);
		Reference ref = null;
		if(parts.length > 0) {
			ref = new Reference();
			ref.setTargetId(parts[0]);
			if(parts.length > 1) {
				try {
					ref.setTargetVersionNumber(Long.parseLong(parts[1]));
				} catch(NumberFormatException e) {}
			}
		}		
		return ref;		
	}
	
	public static String createEntityVersionString(Reference ref) {
		return createEntityVersionString(ref.getTargetId(), ref.getTargetVersionNumber());
	}
	
	public static String createEntityVersionString(String id, Long version) {
		if(version != null)
			return id+WebConstants.ENTITY_VERSION_STRING+version;
		else 
			return id;		
	}
	
	public static String getVersionDisplay(Versionable versionable) {		
		String version = "";
		if(versionable == null || versionable.getVersionNumber() == null) return version;

		if(versionable.getVersionLabel() != null && !versionable.getVersionNumber().toString().equals(versionable.getVersionLabel())) {
			version = versionable.getVersionLabel() + " (" + versionable.getVersionNumber() + ")";
		} else {
			version = versionable.getVersionNumber().toString(); 			
		}
		return version;
	}
}
