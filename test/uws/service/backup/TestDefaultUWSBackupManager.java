package uws.service.backup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import uws.UWSException;
import uws.job.JobInfo;
import uws.job.JobInfo.InfoType;

public class TestDefaultUWSBackupManager {

	@Before
	public void setUp() throws Exception{}

	/* ********************************************************************* */
	/* TEST RESTORATION OF CUSTOM JOB_INFO                                   */

	/**
	 * Invalid because NO EMPTY CONSTRUCTOR.
	 * 
	 * In addition, the content type is incorrect...it should be {@link InfoType#CUSTOM}.
	 */
	public static class InvalidJobInfo extends JobInfo {
		public InvalidJobInfo(final String foo){
			type = InfoType.LONG;
		}
	}

	/**
	 * Valid because there is an empty constructor,
	 * the content type is {@link InfoType#CUSTOM}
	 * and the functions required for the restoration
	 * are overwritten.
	 */
	public static class ValidJobInfo extends JobInfo {

		public ValidJobInfo(){
			super();
			type = InfoType.CUSTOM;
		}

		@Override
		public void setContent(final Object obj) throws IllegalArgumentException{
			content = obj;
		}

	}

	@Test
	public void testCreateCustomJobInfo(){
		DefaultUWSBackupManager backupManager = new DefaultUWSBackupManager(null);

		// Wrong class name => NULL
		assertNull(backupManager.createCustomJobInfo("foo.bar"));

		// Invalid JobInfo extension => NULL
		assertNull(backupManager.createCustomJobInfo("uws.service.backup.TestDefaultUWSBackupManager$InvalidJobInfo"));

		// Valid JobInfo extension => OK
		assertNotNull(backupManager.createCustomJobInfo("uws.service.backup.TestDefaultUWSBackupManager$ValidJobInfo"));
	}

	/* ********************************************************************* */
	/* TEST RESTORATION OF JOB INFO                                          */

	@Test
	public void testGetJobInfo(){
		DefaultUWSBackupManager backupManager = new DefaultUWSBackupManager(null);

		// No parameter => NULL
		try{
			assertNull(backupManager.getJobInfo(null));
		}catch(UWSException ue){
			ue.printStackTrace();
			fail("A NULL parameter MUST NOT throw an error. The function MUST return NULL instead.");
		}

		// Create a mock JSON object:
		JSONObject obj = new JSONObject();
		try{
			obj.put("type", "uws.service.backup.TestDefaultUWSBackupManager$InvalidJobInfo");
			obj.put("content", "youpi!");
		}catch(JSONException je){
			; // can't happen ; the map items have all a NOT NULL name.
		}

		// Invalid JobInfo extension => NULL
		try{
			assertNull(backupManager.getJobInfo(obj));
		}catch(UWSException ue){
			ue.printStackTrace();
			fail("In case of an invalid custom JobInfo, NULL must be returned.");
		}

		// Fix the mock JSON object:
		try{
			obj.put("type", "uws.service.backup.TestDefaultUWSBackupManager$ValidJobInfo");
		}catch(JSONException je){
			; // can't happen ; the map items have all a NOT NULL name.
		}

		// Valid JobInfo extension => OK
		try{
			JobInfo info = backupManager.getJobInfo(obj);
			assertNotNull(info);
			assertEquals(InfoType.CUSTOM, info.getType());
			assertEquals("youpi!", info.getContent());
		}catch(UWSException ue){
			ue.printStackTrace();
			fail("The specified extension of JobInfo is valid. No error should have occurred.");
		}
	}

}
