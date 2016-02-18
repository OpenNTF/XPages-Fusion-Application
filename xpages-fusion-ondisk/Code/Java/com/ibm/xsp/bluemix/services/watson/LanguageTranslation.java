package com.ibm.xsp.bluemix.services.watson;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.xsp.bluemix.util.BluemixContextUtil;
import com.ibm.xsp.bluemix.util.RestUtil;

/**
 * Utility bean for using the Watson Language Translation service
 * 
 * @author Brian Gleeson - brian.gleeson@ie.ibm.com
 */
public class LanguageTranslation implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String SERVICE_NAME = "language_translation";
	private BluemixContextUtil bluemixUtil;
	private RestUtil rest;
	
	public LanguageTranslation() {
		// Hardcoded Watson url/credentials for local testing
		String baseUrl  = "";
		String username = "";
		String password = "";
		bluemixUtil = new BluemixContextUtil(SERVICE_NAME, username, password, baseUrl);
		rest = new RestUtil();
	}
	
	/**
	 * Send a POST request to the Watson service to translate some text
	 * 
	 * @param text - The text to be translated
	 * @param origin - The origin language of the text 
	 * @param target - The target translation language
	 * @return String of the translated text
	 * @throws JsonException 
	 */
	public String getTranslation(String text, String origin, String target) throws URISyntaxException, IOException, JsonException {
		//Build the POST data
		JsonJavaObject postData = new JsonJavaObject();
		postData.put("text", text);
		
		//Send the POST request
		String postUrl = bluemixUtil.getBaseUrl() + "/v2/translate?model_id=" + origin + "-" + target +"&source=" + origin + "&target=" + target;
		String responseStr = rest.post(postUrl, bluemixUtil.getAuthorizationHeader(), postData).returnContent().asString();
		
		//Decode the result
		String response =  URLDecoder.decode(responseStr, "UTF-8");
		return response;
	}
	
	/**
	 * Send a GET request to the Watson service to detect the language of some text
	 * 
	 * @param text - Detect the language of this text 
	 * @return String identifier of the detected language
	 */
	public String detectLanguage(String text) throws URISyntaxException, IOException {
		//Encode the text
		String utfText =  URLEncoder.encode(text, "UTF-8");
		
		//Send the GET request to detect the language
		String getUrl = bluemixUtil.getBaseUrl() + "/v2/identify?text=" + utfText;
		String responseStr = rest.get(getUrl, bluemixUtil.getAuthorizationHeader()).returnContent().asString();
		
		return responseStr;
	}
}
