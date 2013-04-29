package org.sagebionetworks.web.unitshared.users;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.web.shared.users.AclUtils;
import org.sagebionetworks.web.shared.users.PermissionLevel;

import scala.actors.threadpool.Arrays;

public class AclUtilsTest {

	@Test
	public void testGetPermissionLevel() {
		assertEquals(PermissionLevel.CAN_VIEW, AclUtils.getPermissionLevel(getReadAccessTypeSet()));
		assertEquals(PermissionLevel.CAN_EDIT, AclUtils.getPermissionLevel(getEditAccessTypeSet()));
		assertEquals(PermissionLevel.CAN_EDIT_DELETE, AclUtils.getPermissionLevel(getEditDeleteAccessTypeSet()));
		assertEquals(PermissionLevel.CAN_ADMINISTER, AclUtils.getPermissionLevel(getAdminAccessTypeSet()));
		assertEquals(PermissionLevel.OWNER, AclUtils.getPermissionLevel(getOwnerAccessTypeSet()));
	}

	
	@Test
	public void testGetACCESS_TYPEs() {
		assertEquals(getReadAccessTypeSet(), AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_VIEW));
		assertEquals(getEditAccessTypeSet(), AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_EDIT));
		assertEquals(getEditDeleteAccessTypeSet(), AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_EDIT_DELETE));
		assertEquals(getAdminAccessTypeSet(), AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_ADMINISTER));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetPermissionLevels() {		
		
		assertEquals(new HashSet<PermissionLevel>(Arrays.asList(new PermissionLevel[] { PermissionLevel.CAN_EDIT, PermissionLevel.CAN_EDIT_DELETE, PermissionLevel.CAN_ADMINISTER, PermissionLevel.OWNER })), 
				AclUtils.getPermisionLevels(ACCESS_TYPE.CREATE));
		
		assertEquals(new HashSet<PermissionLevel>(Arrays.asList(new PermissionLevel[] { PermissionLevel.CAN_VIEW, PermissionLevel.CAN_EDIT, PermissionLevel.CAN_EDIT_DELETE, PermissionLevel.CAN_ADMINISTER, PermissionLevel.OWNER })), 
				AclUtils.getPermisionLevels(ACCESS_TYPE.READ));
		
		assertEquals(new HashSet<PermissionLevel>(Arrays.asList(new PermissionLevel[] { PermissionLevel.CAN_EDIT, PermissionLevel.CAN_EDIT_DELETE, PermissionLevel.CAN_ADMINISTER, PermissionLevel.OWNER })), 
				AclUtils.getPermisionLevels(ACCESS_TYPE.UPDATE));
		
		assertEquals(new HashSet<PermissionLevel>(Arrays.asList(new PermissionLevel[] { PermissionLevel.CAN_EDIT_DELETE, PermissionLevel.CAN_ADMINISTER, PermissionLevel.OWNER })), 
				AclUtils.getPermisionLevels(ACCESS_TYPE.DELETE));
		
		assertEquals(new HashSet<PermissionLevel>(Arrays.asList(new PermissionLevel[] { PermissionLevel.CAN_ADMINISTER, PermissionLevel.OWNER })), 
				AclUtils.getPermisionLevels(ACCESS_TYPE.CHANGE_PERMISSIONS));	

		assertEquals(new HashSet<PermissionLevel>(Arrays.asList(new PermissionLevel[] { PermissionLevel.OWNER })), 
				AclUtils.getPermisionLevels(ACCESS_TYPE.DOWNLOAD));	

	}
		
	/*
	 * Private methods
	 */
	private Set<ACCESS_TYPE> getReadAccessTypeSet() {
		Set<ACCESS_TYPE> set = new HashSet<ACCESS_TYPE>();		
		set.add(ACCESS_TYPE.READ);
		return set;
	}
	
	private Set<ACCESS_TYPE> getEditAccessTypeSet() {
		Set<ACCESS_TYPE> set = new HashSet<ACCESS_TYPE>();		
		set.add(ACCESS_TYPE.CREATE);
		set.add(ACCESS_TYPE.READ);
		set.add(ACCESS_TYPE.UPDATE);
		return set;
	}
	
	private Set<ACCESS_TYPE> getEditDeleteAccessTypeSet() {
		Set<ACCESS_TYPE> set = new HashSet<ACCESS_TYPE>();		
		set.add(ACCESS_TYPE.CREATE);
		set.add(ACCESS_TYPE.READ);
		set.add(ACCESS_TYPE.UPDATE);
		set.add(ACCESS_TYPE.DELETE);
		return set;
	}
	
	private Set<ACCESS_TYPE> getAdminAccessTypeSet() {
		Set<ACCESS_TYPE> set = new HashSet<ACCESS_TYPE>();		
		set.add(ACCESS_TYPE.CREATE);
		set.add(ACCESS_TYPE.READ);
		set.add(ACCESS_TYPE.UPDATE);
		set.add(ACCESS_TYPE.DELETE);
		set.add(ACCESS_TYPE.CHANGE_PERMISSIONS);
		return set;
	}
	
	private Set<ACCESS_TYPE> getOwnerAccessTypeSet() {
		Set<ACCESS_TYPE> set = new HashSet<ACCESS_TYPE>();		
		set.add(ACCESS_TYPE.CREATE);
		set.add(ACCESS_TYPE.READ);
		set.add(ACCESS_TYPE.UPDATE);
		set.add(ACCESS_TYPE.DELETE);
		set.add(ACCESS_TYPE.CHANGE_PERMISSIONS);
		set.add(ACCESS_TYPE.DOWNLOAD);
		return set;
	}

}

