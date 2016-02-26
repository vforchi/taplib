package uws.job;

/*
 * This file is part of UWSLibrary.
 * 
 * UWSLibrary is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * UWSLibrary is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with UWSLibrary.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2016 - Astronomisches Rechen Institut (ARI)
 */

import org.json.JSONArray;
import org.json.JSONObject;

import uws.job.serializer.XMLSerializer;

/**
 * <p>Additional free piece of information of a job.
 * This object is never checked by the UWS library. It is free and only
 * dedicated to the UWS service users or for job processing.</p>
 * 
 * <h3>Types supported by default</h3>
 * 
 * <p>By default {@link JobInfo} supports the following basic types:</p>
 * <ul>
 * 	<li>XML</li>
 * 	<li>String (not a document, just a simple plain text)</li>
 * 	<li>Double (and with implicit cast Float as well)</li>
 * 	<li>Long (and with implicit cast Short and Integer as well)</li>
 * 	<li>JSONArray to store an array so that it can be easily
 * 	    and automatically backuped and restored by the UWS service</li>
 * 	<li>JSONObject to store a map so that it can be easily
 * 	    and automatically backuped and restored by the UWS service</li>
 * </ul>
 * 
 * <h3>Custom {@link JobInfo}</h3>
 * 
 * <p>
 * 	It is therefore possible to extend this class to support more complex data.
 * 	In such case, the type {@link InfoType#CUSTOM CUSTOM} should be set. If not
 * 	set to this type, the library should be able to detect the custom type anyway
 * 	and adapt the backup and restoration, but it is recommended to rely on this
 * 	smart behavior.
 * </p>
 * 
 * <p><i>Note: If the type of the {@link JobInfo} or its content is <code>null</code>,
 * this job info will be considered as empty and won't be backuped.</i></p>
 * 
 * <p>
 * 	If this class is extended, in addition of setting the {@link InfoType#CUSTOM CUSTOM}
 * 	type, the functions {@link #setContent(Object)}, {@link #toJSON()}, {@link #toXML(String)}
 * 	and {@link #getBackupContent()} should be overwritten in order to avoid unexpected behavior
 * 	while performing the backup or the restoration.
 * </p>
 * 
 * <p>A custom {@link JobInfo} is always restored using the same algorithm:</p>
 * <ol>
 * 	<li>Create the custom {@link JobInfo} using its empty constructor</li>
 * 	<li>Call {@link #setContent(Object)} with the object got from the JSON backup file.</li>
 * </ol>
 * 
 * <p>
 * 	Consequently, when extending {@link JobInfo} you <b>MUST</b> ensure there is
 * 	an empty constructor and that {@link #setContent(Object)}, {@link #toJSON()},
 * 	{@link #toXML(String)} and {@link #getBackupContent()} are correctly overwritten.
 * </p>
 * 
 * @author Gr&eacute;gory Mantelet (ARI)
 * @version 4.2 (02/2016)
 * @since 4.2
 */
public class JobInfo {

	/**
	 * <p>Type of an additional job info.</p>
	 * 
	 * <p>
	 * 	{@link #CUSTOM} should be used for an extended instance of {@link JobInfo}.
	 * 	Thus the UWS library is able to write a more precise backup and
	 * 	to ensure a better restoration of the info.
	 * </p>
	 * 
	 * @author Gr&eacute;gory Mantelet (ARI)
	 * @version 4.2 (02/2016)
	 * @since 4.2
	 */
	public static enum InfoType{
		XML, STRING, DOUBLE, LONG, MAP, ARRAY, CUSTOM;
	}

	/** <p>Type of this Job Info.</p>
	 * <p><i>Note: If <code>null</code>, this {@link JobInfo} will
	 * be considered as empty and won't be backuped.</i></p> */
	protected InfoType type = null;

	/** <p>Content of this Job Info.</p>
	 * <p><i>Note: If <code>null</code>, this {@link JobInfo} will
	 * be considered as empty and won't be backuped.</i></p> */
	protected Object content = null;

	/**
	 * Create an empty Job Info.
	 */
	public JobInfo(){}

	/**
	 * <p>Create a Job Info containing a simple plain text.</p>
	 * 
	 * <p>
	 * 	In the given text, double quotes will be escaped for the JSON and backup output,
	 * 	AND special XML characters (e.g. &lt;, &gt;, ...) will be escaped for the XML output.
	 * </p>
	 * 
	 * @param text	Textual content of the Job Info.
	 * 
	 * @see #JobInfo(String, boolean)
	 */
	public JobInfo(final String text){
		this(text, false);
	}

	/**
	 * <p>Create a Job Info containing either an XML document or a simple plain text.</p>
	 * 
	 * <p>
	 * 	If the given text is declared as an XML document, double quotes will be escaped
	 * 	for the JSON and backup output.
	 * </p>
	 * 
	 * <p>
	 * 	If the given text is declared as a plain text, double quotes will be escaped
	 * 	for the JSON and backup output, AND special XML characters (e.g. &lt;, &gt;, ...)
	 * 	will be escaped for the XML output.
	 * </p> 
	 * 
	 * @param text		Textual content of the Job Info.
	 *            		<i>If <code>null</code> or an empty String object, this Job Info will stay empty.</i>
	 * @param xmlDoc	<code>true</code> if the given text is an XML document,
	 *              	<code>false</code> for a simple plain text.
	 */
	public JobInfo(final String text, final boolean xmlDoc){
		if (text != null && text.trim().length() > 0){
			type = (xmlDoc ? InfoType.XML : InfoType.STRING);
			content = text;
		}
	}

	/**
	 * Create a Job Info containing just a floating point number.
	 * 
	 * @param value	Numerical content of the Job Info.
	 */
	public JobInfo(final Double value){
		if (value != null){
			type = InfoType.DOUBLE;
			content = value;
		}
	}

	/**
	 * Create a Job Info containing just a long number.
	 * 
	 * @param value	Numerical content of the Job Info.
	 */
	public JobInfo(final Long value){
		if (value != null){
			type = InfoType.LONG;
			content = value;
		}
	}

	/**
	 * Create a Job Info containing an array.
	 * 
	 * @param array	Array content of the Job Info.
	 */
	public JobInfo(final JSONArray array){
		if (array != null){
			type = InfoType.ARRAY;
			content = array;
		}
	}

	/**
	 * <p>Create a Job Info containing an associative map.</p>
	 * 
	 * <p><i>Note: it is allowed that the given map contains values of different type.</i></p> 
	 * 
	 * @param array	Map content of the Job Info.
	 */
	public JobInfo(final JSONObject map){
		if (map != null){
			type = InfoType.MAP;
			content = map;
		}
	}

	/**
	 * Get the type of this Job Info.
	 * 
	 * @return	Job Info type. <i>NULL if this info is empty.</i>
	 */
	public final InfoType getType(){
		return type;
	}

	/**
	 * Get the content of this Job Info.
	 * 
	 * @return	Job Info content. <i>NULL if this info is empty.</i>
	 */
	public final Object getContent(){
		return content;
	}

	/**
	 * <p>Change the content of this Job Info.</p>
	 * 
	 * <p>If <code>null</code> or an empty String object is given, the Job Info will become empty.</p>
	 * 
	 * <p>This function is able to detect automatically the type of the content among:</p>
	 * <ul>
	 * 	<li>String && starting with &lt; => {@link InfoType#XML}</li>
	 * 	<li>String => {@link InfoType#STRING}</li>
	 * 	<li>Short, Integer, Long => {@link InfoType#LONG}</li>
	 * 	<li>Float, Double => {@link InfoType#DOUBLE}</li>
	 * 	<li>{@link JSONArray} => {@link InfoType#ARRAY}</li>
	 * 	<li>{@link JSONObject} => {@link InfoType#MAP}</li>
	 * </li>
	 * 
	 * <p><b>WARNING:</b>
	 * 	Any other type of content will throw an exception by default.
	 * 	This function should be overwritten if inside an extension of {@link JobInfo} aiming
	 * 	to support a special type of content.</b></p>  
	 * 
	 * @param newContent	New content of this Job Info.
	 *                  	<i><code>null</code> or an empty String object will empty this Job Info.</i>
	 */
	public void setContent(final Object newContent) throws IllegalArgumentException{
		if (newContent == null || (newContent instanceof String && newContent.toString().trim().length() == 0)){
			type = null;
			content = null;
		}else{
			content = newContent;
			if (content instanceof String) // XML Doc if starting with an <, otherwise just STRING.
				type = (content.toString().trim().charAt(0) == '<' ? InfoType.XML : InfoType.STRING);
			else if (content instanceof Short || content instanceof Integer || content instanceof Long)
				type = InfoType.LONG;
			else if (content instanceof Float || content instanceof Double)
				type = InfoType.DOUBLE;
			else if (content instanceof JSONArray)
				type = InfoType.ARRAY;
			else if (content instanceof JSONObject)
				type = InfoType.MAP;
			else
				throw new IllegalArgumentException("Unsupported type of Job Info: " + newContent.getClass() + "! You should extend uws.job.JobInfo in order to support this type.");
		}
	}

	/**
	 * <p>Transform this Job Info into a JSON expression.</p>
	 * 
	 * <p>
	 * 	This function is used only when the job description is asked by a service user.
	 * 	<b>Thus, it should not contain information not intended to be seen by users.</b>
	 * </b>
	 * 
	 * <p><i>Note 1:
	 * 	Double quotes will be escaped for {@link InfoType#XML} and {@link InfoType#STRING}
	 * 	contents. The final string will be surrounded by double quotes, accordingly with the
	 * 	JSON format.</i></p>
	 * 
	 * <p><i>Note 2:
	 * 	This function should be overwritten if inside an extension of {@link JobInfo} aiming
	 * 	to support a special type of content.</i></p>
	 * 
	 * @return	JSON expression corresponding to this Job Info.
	 */
	public String toJSON(){
		switch(type){
			case XML:
			case STRING:
				return "\"" + content.toString().replaceAll("\"", "\\\\\"") + "\"";
			case LONG:
			case DOUBLE:
				return content.toString();
			case ARRAY:
				return ((JSONArray)content).toString();
			case MAP:
				return ((JSONObject)content).toString();
			default:
				return "";
		}
	}

	/**
	 * <p>Build a JSON expression representing this Job Info
	 * and which can be used to re-create this Job Info in the
	 * same state at the UWS Service restoration.</p>
	 * 
	 * <p>By default, {@link #toJSON()} is called.</p>
	 * 
	 * <p><i>Note:
	 * 	This function should be overwritten if inside an extension of {@link JobInfo} aiming
	 * 	to support a special type of content. Otherwise, {@link InfoType#CUSTOM} contents
	 * 	will never be backup-ed.</i></p>
	 * 
	 * @return	A JSON expression for backup.
	 *        	<i>If <code>null</code> no job info will be backup-ed.</i>
	 */
	public String getBackupContent(){
		return toJSON();
	}

	/**
	 * <p>Transform this Job Info into the XML content of the node <code>jobInfo</code>
	 * of the Job XML description. No parent XML node should be created in the returned XML string.</p>
	 * 
	 * <p>
	 * 	This function is used only when the job description is asked by a service user.
	 * 	<b>Thus, it should not contain information not intended to be seen by users.</b>
	 * </b>
	 * 
	 * <p><i>Note 1:
	 * 	Special XML characters will be escaped for all types of content, except
	 * 	for {@link InfoType#XML}.</i></p>
	 * 
	 * <p><i>Note 2:
	 * 	This function should be overwritten if inside an extension of {@link JobInfo} aiming
	 * 	to support a special type of content.</i></p>
	 * 
	 * <p><b>WARNING:
	 * 	Item names in the {@link JSONObject}s is neither checked nor escaped. You must ensure
	 * 	that these names are valid XML node names (e.g. no &lt;, &gt;, &amp;, punctuation and not
	 * 	starting with a digit).</b></p>
	 * 
	 * @param indent	Indentation to apply for each written row.
	 *              	<i><code>null</code> if no indentation must be applied ;
	 *              	indentation is not applied for elementary content such as numbers and Strings.</i>
	 * 
	 * @return	XML description of this Job Info ; the returned XML will be embedded inside the
	 *        	<code>jobInfo</code> node of the XML job description (so, no need to create a parent XML node).
	 */
	public String toXML(String indent){
		if (indent == null)
			indent = "";

		StringBuffer buf;
		switch(type){
			case XML:
				return content.toString();
			case STRING:
				return XMLSerializer.escapeXMLData(content.toString());
			case LONG:
			case DOUBLE:
				return content.toString();
			case ARRAY:
				buf = new StringBuffer("\n" + indent);
				JSONArray array = (JSONArray)content;
				for(int i = 0; i < array.length(); i++){
					if (array.opt(i) == null)
						buf.append("\t<item></item>\n").append(indent);
					else
						buf.append("\t<item>").append(XMLSerializer.escapeXMLData(array.opt(i).toString())).append("</item>\n").append(indent);
				}
				return buf.toString();
			case MAP:
				buf = new StringBuffer("\n" + indent);
				JSONObject map = (JSONObject)content;
				String[] keys = JSONObject.getNames(map);
				if (keys == null)
					return buf.toString();
				else{
					for(String k : keys){
						if (map.opt(k) == null)
							buf.append("\t<").append(k).append("></").append(k).append(">\n").append(indent);
						else
							buf.append("\t<").append(k).append('>').append(XMLSerializer.escapeXMLData(map.opt(k).toString())).append("</").append(k).append(">\n").append(indent);
					}
					return buf.toString();
				}
			default:
				return "";
		}
	}

	public final static void main(final String[] args) throws Throwable{
		System.out.println("to\"to".replaceAll("\"", "\\\\\""));
	}

	@Override
	public String toString(){
		return (content == null) ? "" : content.toString();
	}
}
