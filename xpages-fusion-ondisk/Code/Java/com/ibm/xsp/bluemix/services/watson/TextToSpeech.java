package com.ibm.xsp.bluemix.services.watson;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Response;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.xsp.bluemix.util.BluemixContextUtil;
import com.ibm.xsp.bluemix.util.RestUtil;

/**
 * Utility bean for using the Watson Text to Speech service
 * 
 * @author Brian Gleeson - brian.gleeson@ie.ibm.com
 */
public class TextToSpeech implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final String SERVICE_NAME = "text_to_speech";
	private BluemixContextUtil bluemixUtil;
	private RestUtil rest;
	
	public TextToSpeech() {
		// Hardcoded Watson url/credentials for local testing
		String baseUrl  = "";
		String username = "";
		String password = "";
		bluemixUtil = new BluemixContextUtil(SERVICE_NAME, username, password, baseUrl);
		rest = new RestUtil();
	}
	
	/**
	 * Send POST request to Watson TextToSpeech service
	 * 
	 * @param text - The text to turn into speech
	 * @param voice - The chosen voice to use for the speech (e.g."US Male", "French Female" etc.)
	 * @return Response containing the returned audio file
	 */
	public Response getSpeech(String text, String voice) throws URISyntaxException, ClientProtocolException, IOException, JsonException {
		//Build the POST data
		JsonJavaObject postData = new JsonJavaObject();
		postData.put("text", text);
		
		//Send the POST request
		String postUrl = bluemixUtil.getBaseUrl() + "/v1/synthesize?voice=" + voice +"&accept=audio/ogg;codecs=opus";
		Response response = rest.post(postUrl, bluemixUtil.getAuthorizationHeader(), postData);
		
		return response;
	}
}
