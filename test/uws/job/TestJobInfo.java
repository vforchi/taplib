package uws.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import uws.job.JobInfo.InfoType;

public class TestJobInfo {

	@Before
	public void setUp() throws Exception{}

	@Test
	public void testJobInfoString(){
		// A null String MUST create an empty Job Info:
		JobInfo info = new JobInfo((String)null);
		assertNull(info.getType());
		assertNull(info.getContent());
		info = new JobInfo((String)null, false);
		assertNull(info.getType());
		assertNull(info.getContent());

		// Create a simple plain String Job Info:
		info = new JobInfo("blabla");
		assertEquals(InfoType.STRING, info.getType());
		assertEquals("blabla", info.getContent());
		info = new JobInfo("blabla", false);
		assertEquals(InfoType.STRING, info.getType());
		assertEquals("blabla", info.getContent());

		// Create an empty Job Info and set manually a String:
		info = new JobInfo();
		assertNull(info.getType());
		assertNull(info.getContent());
		info.setContent("blabla");
		assertEquals(InfoType.STRING, info.getType());
		assertEquals("blabla", info.getContent());

		// Check the JSON output:
		assertEquals("\"blabla\"", info.toJSON());
		// check the double quote escaping is working:
		info.setContent("it's a \"blabla\"!");
		assertEquals("\"it's a \\\"blabla\\\"!\"", info.toJSON());

		// Check the XML output:
		assertEquals("it's a \"blabla\"!", info.toXML(null));
		// indentation should not change anything in the case of an elementary content:
		assertEquals("it's a \"blabla\"!", info.toXML("\t"));
		// check special XML characters are escaped:
		info.setContent("foo < bar > thing<>>");
		assertEquals("foo &lt; bar &gt; thing&lt;&gt;&gt;", info.toXML(null));

		// Check the Backup output (should be the string content):
		assertEquals("foo < bar > thing<>>", info.getBackupContent());

		// Check the string output:
		assertEquals("foo < bar > thing<>>", info.toString());
	}

	@Test
	public void testJobInfoXML(){
		// A null String MUST create an empty Job Info:
		JobInfo info = new JobInfo((String)null, false);
		assertNull(info.getType());
		assertNull(info.getContent());
		info = new JobInfo((String)null, true);
		assertNull(info.getType());
		assertNull(info.getContent());

		// A String declared as NOT-XML should create a simple plain String Job Info:
		info = new JobInfo("blabla", false);
		assertEquals(InfoType.STRING, info.getType());
		assertEquals("blabla", info.getContent());

		// A String declared as XML should create an XML Job Info:
		info = new JobInfo("<blabla></blabla>", true);
		assertEquals(InfoType.XML, info.getType());
		assertEquals("<blabla></blabla>", info.getContent());
		// ...even if it is not an XML document:
		info = new JobInfo("blabla", true);
		assertEquals(InfoType.XML, info.getType());
		assertEquals("blabla", info.getContent());

		// Create an empty Job Info and set manually an XML:
		info = new JobInfo();
		assertNull(info.getType());
		assertNull(info.getContent());
		info.setContent("<blabla></blabla>");
		assertEquals(InfoType.XML, info.getType());
		assertEquals("<blabla></blabla>", info.getContent());
		info.setContent("  	<blabla></blabla>");
		assertEquals(InfoType.XML, info.getType());
		assertEquals("  	<blabla></blabla>", info.getContent());
		// ...but it does not work if the text does not start with a <
		info.setContent("blabla");
		assertEquals(InfoType.STRING, info.getType());
		info.setContent("  	blabla");
		assertEquals(InfoType.STRING, info.getType());

		// Check the JSON output:
		info.setContent("<blabla></blabla>");
		assertEquals("\"<blabla></blabla>\"", info.toJSON());
		// check the double quote escaping is working:
		info.setContent("<blabla>it's a \"blabla\"!</blabla>");
		assertEquals("\"<blabla>it's a \\\"blabla\\\"!</blabla>\"", info.toJSON());

		// Check the XML output:
		assertEquals("<blabla>it's a \"blabla\"!</blabla>", info.toXML(null));
		// indentation should not change anything in the case of an elementary content:
		assertEquals("<blabla>it's a \"blabla\"!</blabla>", info.toXML("\t"));

		// Check the Backup output (should be the string content):
		assertEquals("<blabla>it's a \"blabla\"!</blabla>", info.getBackupContent());

		// Check the string output:
		assertEquals("<blabla>it's a \"blabla\"!</blabla>", info.toString());
	}

	@Test
	public void testJobInfoDouble(){
		// A null Double MUST create an empty Job Info:
		JobInfo info = new JobInfo((Double)null);
		assertNull(info.getType());
		assertNull(info.getContent());

		// A Double value should create a Double Job Info content:
		info = new JobInfo(3.14);
		assertEquals(InfoType.DOUBLE, info.getType());
		assertEquals(3.14, info.getContent());
		info = new JobInfo(3.);
		assertEquals(InfoType.DOUBLE, info.getType());
		assertEquals(3., info.getContent());

		// Create an empty Job Info and set manually a Double:
		info = new JobInfo();
		assertNull(info.getType());
		assertNull(info.getContent());
		info.setContent(3.14);
		assertEquals(InfoType.DOUBLE, info.getType());
		assertEquals(3.14, info.getContent());

		// Check the JSON output:
		assertEquals("3.14", info.toJSON());

		// Check the XML output:
		assertEquals("3.14", info.toXML(null));
		// indentation should not change anything in the case of an elementary content:
		assertEquals("3.14", info.toXML("\t"));

		// Check the Backup output (should be the numeric content):
		assertEquals(3.14, info.getBackupContent());

		// Check the string output:
		assertEquals("3.14", info.toString());
	}

	@Test
	public void testJobInfoLong(){
		// A null Long MUST create an empty Job Info:
		JobInfo info = new JobInfo((Long)null);
		assertNull(info.getType());
		assertNull(info.getContent());

		// A Double value should create a Double Job Info content:
		info = new JobInfo(3l);
		assertEquals(InfoType.LONG, info.getType());
		assertEquals(3l, info.getContent());
		info = new JobInfo((long)3);
		assertEquals(InfoType.LONG, info.getType());
		assertEquals(3l, info.getContent());
		info = new JobInfo(new Long(3));
		assertEquals(InfoType.LONG, info.getType());
		assertEquals(new Long(3), info.getContent());

		// Create an empty Job Info and set manually a Long:
		info = new JobInfo();
		assertNull(info.getType());
		assertNull(info.getContent());
		info.setContent(3l);
		assertEquals(InfoType.LONG, info.getType());
		assertEquals(3l, info.getContent());
		info.setContent(3);
		assertEquals(InfoType.LONG, info.getType());
		assertEquals(3, info.getContent());

		// Check the JSON output:
		assertEquals("3", info.toJSON());

		// Check the XML output:
		assertEquals("3", info.toXML(null));
		// indentation should not change anything in the case of an elementary content:
		assertEquals("3", info.toXML("\t"));

		// Check the Backup output (should be the numeric content):
		assertEquals(3, info.getBackupContent());

		// Check the string output:
		assertEquals("3", info.toString());
	}

	@Test
	public void testJobInfoJSONArray(){
		// A null JSONArray MUST create an empty Job Info:
		JobInfo info = new JobInfo((JSONArray)null);
		assertNull(info.getType());
		assertNull(info.getContent());

		// Create a mock array:
		JSONArray array = new JSONArray();
		array.put("banana");
		array.put("orange");
		array.put("<appel>");

		// A JSONArray object should create an Array Job Info content:
		info = new JobInfo(array);
		assertEquals(InfoType.ARRAY, info.getType());
		assertEquals(array, info.getContent());

		// Create an empty Job Info and set manually an Array:
		info = new JobInfo();
		assertNull(info.getType());
		assertNull(info.getContent());
		info.setContent(array);
		assertEquals(InfoType.ARRAY, info.getType());
		assertEquals(array, info.getContent());

		// Check the JSON output:
		assertEquals(array.toString(), info.toJSON());
		// with an empty array:
		info.setContent(new JSONArray());
		assertEquals((new JSONArray()).toString(), info.toJSON());

		// Check the XML output:
		info.setContent(array);
		assertEquals("\n\t<item>banana</item>\n\t<item>orange</item>\n\t<item>&lt;appel&gt;</item>\n", info.toXML(null));
		// indentation should not change anything in the case of an elementary content:
		assertEquals("\n\t\t<item>banana</item>\n\t\t<item>orange</item>\n\t\t<item>&lt;appel&gt;</item>\n\t", info.toXML("\t"));
		// with an empty array:
		info.setContent(new JSONArray());
		assertEquals("\n", info.toXML(null));
		assertEquals("\n\t", info.toXML("\t"));

		// Check the Backup output (should be the array content):
		info.setContent(array);
		assertEquals(array, info.getBackupContent());

		// Check the string output:
		assertEquals(array.toString(), info.toString());
	}

	@Test
	public void testJobInfoJSONObject(){
		// A null JSONObject MUST create an empty Job Info:
		JobInfo info = new JobInfo((JSONObject)null);
		assertNull(info.getType());
		assertNull(info.getContent());

		// Create a mock object:
		JSONObject map = new JSONObject();
		try{
			map.put("banana", 3);
			map.put("orange", "Florida");
			map.put("appel", "Very <special> \"Appel\"!");
		}catch(JSONException je){
			je.printStackTrace();
			fail("No error should have occurred here: all the item names are not null!");
		}

		// A JSONObject object should create a Map Job Info content:
		info = new JobInfo(map);
		assertEquals(InfoType.MAP, info.getType());
		assertEquals(map, info.getContent());

		// Create an empty Job Info and set manually a Map:
		info = new JobInfo();
		assertNull(info.getType());
		assertNull(info.getContent());
		info.setContent(map);
		assertEquals(InfoType.MAP, info.getType());
		assertEquals(map, info.getContent());

		// Check the JSON output:
		assertEquals(map.toString(), info.toJSON());
		// with an empty object:
		info.setContent(new JSONObject());
		assertEquals((new JSONObject()).toString(), info.toJSON());

		// Check the XML output:
		info.setContent(map);
		String expected = "\n";
		for(String k : JSONObject.getNames(map)){
			if (k.equals("banana"))
				expected += "\t<banana>3</banana>\n";
			else if (k.equals("orange"))
				expected += "\t<orange>Florida</orange>\n";
			else
				expected += "\t<appel>Very &lt;special&gt; \"Appel\"!</appel>\n";
		}
		assertEquals(expected, info.toXML(null));
		// indentation should not change anything in the case of an elementary content:
		expected = "\n\t";
		for(String k : JSONObject.getNames(map)){
			if (k.equals("banana"))
				expected += "\t<banana>3</banana>\n\t";
			else if (k.equals("orange"))
				expected += "\t<orange>Florida</orange>\n\t";
			else
				expected += "\t<appel>Very &lt;special&gt; \"Appel\"!</appel>\n\t";
		}
		assertEquals(expected, info.toXML("\t"));
		// with an empty object:
		info.setContent(new JSONObject());
		assertEquals("\n", info.toXML(null));
		assertEquals("\n\t", info.toXML("\t"));

		// Check the Backup output (should be the content map):
		info.setContent(map);
		assertEquals(map, info.getBackupContent());

		// Check the string output:
		assertEquals(map.toString(), info.toString());
	}

	@Test
	public void testJobInfoCustom(){
		JobInfo info = new JobInfo();

		// Set a custom content should throw an exception (here a JobInfo instance, but it could be anything):
		Object obj = new JobInfo();
		try{
			info.setContent(obj);
		}catch(Exception ex){
			assertEquals(IllegalArgumentException.class, ex.getClass());
			assertEquals("Unsupported type of Job Info: " + obj.getClass() + "! You should extend uws.job.JobInfo in order to support this type.", ex.getMessage());
		}

		// Simulate an extension of JobInfo for a CUSTOM content:
		info.type = InfoType.CUSTOM;
		info.content = obj;

		// The JSON, XML and backup outputs are empty (but not NULL) by default:
		assertEquals(0, info.toJSON().length());
		assertEquals(0, info.toXML(null).length());
		assertEquals(0, info.toXML("\t").length());
		assertNull(info.getBackupContent());
		assertEquals(0, info.toString().length());
	}

	@Test
	public void testSetContent(){
		JobInfo info = new JobInfo(3.14);
		assertNotNull(info.getType());

		// Set NULL should empty the Job Info:
		info.setContent(null);
		assertNull(info.getType());
		assertNull(info.getContent());
	}

}
