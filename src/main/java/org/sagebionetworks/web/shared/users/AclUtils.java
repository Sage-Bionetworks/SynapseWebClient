package org.sagebionetworks.web.shared.users;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.sagebionetworks.repo.model.ACCESS_TYPE;

public class AclUtils {

	private static Map<PermissionLevel, Set<ACCESS_TYPE>> permToACCESS_TYPE;

	private static Map<ACCESS_TYPE, Set<PermissionLevel>> accessTypeToPerm;

	// Build up a list of increasing permissions, starting with VIEW
	// It's acceptable to use the shallow copy of the ArrayList(arrList) constructor
	// because the elements are all ENUMS.
	static {
		permToACCESS_TYPE = new HashMap<PermissionLevel, Set<ACCESS_TYPE>>();
		TreeSet<ACCESS_TYPE> accessList = new TreeSet<ACCESS_TYPE>();

		accessList.add(ACCESS_TYPE.READ);
		permToACCESS_TYPE.put(PermissionLevel.CAN_VIEW, new TreeSet<ACCESS_TYPE>(accessList));

		//Score in Evaluation (must clean up new access_type, does not modify existing permission levels)
		permToACCESS_TYPE.put(PermissionLevel.CAN_SCORE_EVALUATION, addTypes(accessList, ACCESS_TYPE.READ_PRIVATE_SUBMISSION, ACCESS_TYPE.UPDATE_SUBMISSION));
				
		//Participate in Evaluation (must clean up new access_type, does not modify existing permission levels)
		permToACCESS_TYPE.put(PermissionLevel.CAN_PARTICIPATE_EVALUATION, addTypes(accessList, ACCESS_TYPE.PARTICIPATE));
		
		accessList.add(ACCESS_TYPE.UPDATE);
		
		accessList.add(ACCESS_TYPE.CREATE);
		permToACCESS_TYPE.put(PermissionLevel.CAN_EDIT, new TreeSet<ACCESS_TYPE>(accessList));
		
		accessList.add(ACCESS_TYPE.DELETE);
		permToACCESS_TYPE.put(PermissionLevel.CAN_EDIT_DELETE, new TreeSet<ACCESS_TYPE>(accessList));
		
		accessList.add(ACCESS_TYPE.CHANGE_PERMISSIONS);
		permToACCESS_TYPE.put(PermissionLevel.CAN_ADMINISTER, new TreeSet<ACCESS_TYPE>(accessList));
		
		//Administer an Evaluation (must clean up new access_types, does not modify existing permission levels)
		permToACCESS_TYPE.put(PermissionLevel.CAN_ADMINISTER_EVALUATION, addTypes(accessList, ACCESS_TYPE.PARTICIPATE, ACCESS_TYPE.READ_PRIVATE_SUBMISSION, ACCESS_TYPE.UPDATE_SUBMISSION, ACCESS_TYPE.DELETE_SUBMISSION));
		
		accessList.add(ACCESS_TYPE.DOWNLOAD);
		permToACCESS_TYPE.put(PermissionLevel.OWNER, new TreeSet<ACCESS_TYPE>(accessList));

		// Build the reverse mapping from the first map
		accessTypeToPerm = new HashMap<ACCESS_TYPE, Set<PermissionLevel>>();
		for (ACCESS_TYPE type : ACCESS_TYPE.values()) {
			TreeSet<PermissionLevel> levels = new TreeSet<PermissionLevel>();

			for (Entry<PermissionLevel, Set<ACCESS_TYPE>> entry : permToACCESS_TYPE
					.entrySet()) {
				if (entry.getValue().contains(type)) {
					levels.add(entry.getKey());
				}
			}
			accessTypeToPerm.put(type, levels);
		}
	}

	private static Set<ACCESS_TYPE> addTypes(Set<ACCESS_TYPE> accessList, ACCESS_TYPE...access_TYPEs) {
		TreeSet<ACCESS_TYPE> newSet = new TreeSet<ACCESS_TYPE>(accessList);
		for (ACCESS_TYPE type : access_TYPEs) {
			newSet.add(type);
		}
		return newSet;
	}
	
	public static PermissionLevel getPermissionLevel(Set<ACCESS_TYPE> accessTypes) {
		for (PermissionLevel level : PermissionLevel.values()) {
			if (accessTypes.equals(permToACCESS_TYPE.get(level))) {
				return level;
			}
		}
		return null;
	}

	public static Set<ACCESS_TYPE> getACCESS_TYPEs(PermissionLevel permissionLevel) {
		return permToACCESS_TYPE.get(permissionLevel);
	}

	public static Set<PermissionLevel> getPermisionLevels(ACCESS_TYPE accessType) {
		return accessTypeToPerm.get(accessType);
	}

	public static ACCESS_TYPE getACCESS_TYPE(String accessType) {
		ACCESS_TYPE valueOf;
		try {
			valueOf = ACCESS_TYPE.valueOf(accessType);
		} catch (IllegalArgumentException e) {
			valueOf = null;
		}

		return valueOf;
	}
	
}
