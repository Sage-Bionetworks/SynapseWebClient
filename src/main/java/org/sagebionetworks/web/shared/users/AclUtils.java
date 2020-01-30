package org.sagebionetworks.web.shared.users;

import static org.sagebionetworks.repo.model.ACCESS_TYPE.CREATE;
import static org.sagebionetworks.repo.model.ACCESS_TYPE.DELETE;
import static org.sagebionetworks.repo.model.ACCESS_TYPE.DOWNLOAD;
import static org.sagebionetworks.repo.model.ACCESS_TYPE.READ;
import static org.sagebionetworks.repo.model.ACCESS_TYPE.READ_PRIVATE_SUBMISSION;
import static org.sagebionetworks.repo.model.ACCESS_TYPE.SUBMIT;
import static org.sagebionetworks.repo.model.ACCESS_TYPE.UPDATE;
import static org.sagebionetworks.repo.model.ACCESS_TYPE.UPDATE_SUBMISSION;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.ResourceAccess;
import org.sagebionetworks.repo.model.util.ModelConstants;

public class AclUtils {

	private static Map<PermissionLevel, Set<ACCESS_TYPE>> permToACCESS_TYPE;

	private static Map<ACCESS_TYPE, Set<PermissionLevel>> accessTypeToPerm;

	// Build up a list of increasing permissions, starting with VIEW
	// It's acceptable to use the shallow copy of the ArrayList(arrList) constructor
	// because the elements are all ENUMS.
	static {
		permToACCESS_TYPE = new HashMap<PermissionLevel, Set<ACCESS_TYPE>>();

		permToACCESS_TYPE.put(PermissionLevel.CAN_VIEW, new TreeSet<ACCESS_TYPE>(Arrays.asList(READ)));
		permToACCESS_TYPE.put(PermissionLevel.CAN_DOWNLOAD, new TreeSet<ACCESS_TYPE>(Arrays.asList(READ, DOWNLOAD)));

		permToACCESS_TYPE.put(PermissionLevel.CAN_EDIT, new TreeSet<ACCESS_TYPE>(Arrays.asList(CREATE, READ, DOWNLOAD, UPDATE)));
		permToACCESS_TYPE.put(PermissionLevel.CAN_EDIT_DELETE, new TreeSet<ACCESS_TYPE>(Arrays.asList(CREATE, READ, DOWNLOAD, UPDATE, DELETE)));

		permToACCESS_TYPE.put(PermissionLevel.CAN_ADMINISTER, ModelConstants.ENTITY_ADMIN_ACCESS_PERMISSIONS);

		// Note, PARTICIPATE is no longer used, but to removed it would require updating all existing
		// Evaluation ACLs
		permToACCESS_TYPE.put(PermissionLevel.CAN_SUBMIT_EVALUATION, new TreeSet<ACCESS_TYPE>(Arrays.asList(READ, SUBMIT)));
		permToACCESS_TYPE.put(PermissionLevel.CAN_SCORE_EVALUATION, new TreeSet<ACCESS_TYPE>(Arrays.asList(READ, READ_PRIVATE_SUBMISSION, UPDATE_SUBMISSION)));

		permToACCESS_TYPE.put(PermissionLevel.CAN_ADMINISTER_EVALUATION, ModelConstants.EVALUATION_ADMIN_ACCESS_PERMISSIONS);

		permToACCESS_TYPE.put(PermissionLevel.CAN_MESSAGE_TEAM, ModelConstants.TEAM_MESSENGER_PERMISSIONS);
		permToACCESS_TYPE.put(PermissionLevel.CAN_ADMINISTER_TEAM, ModelConstants.TEAM_ADMIN_PERMISSIONS);

		// Build the reverse mapping from the first map
		accessTypeToPerm = new HashMap<ACCESS_TYPE, Set<PermissionLevel>>();
		for (ACCESS_TYPE type : ACCESS_TYPE.values()) {
			TreeSet<PermissionLevel> levels = new TreeSet<PermissionLevel>();

			for (Entry<PermissionLevel, Set<ACCESS_TYPE>> entry : permToACCESS_TYPE.entrySet()) {
				if (entry.getValue().contains(type)) {
					levels.add(entry.getKey());
				}
			}
			accessTypeToPerm.put(type, levels);
		}
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

	/**
	 * Return a set of principal IDs that have the given ACCESS_TYPE in the given AccessControlList.
	 * 
	 * @return
	 */
	public static Set<Long> getPrincipalIds(AccessControlList acl, ACCESS_TYPE t) {
		Set<ResourceAccess> resourceAccessSet = acl.getResourceAccess();
		Set<Long> principalIds = new HashSet<Long>();
		for (ResourceAccess ra : resourceAccessSet) {
			if (ra.getAccessType().contains(t)) {
				principalIds.add(ra.getPrincipalId());
			}
		}
		return principalIds;
	}
}
