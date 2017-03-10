package com.ibm.xsp.bluemix.services.watson;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.client.fluent.Response;
import org.apache.http.util.EntityUtils;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaArray;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.xsp.bluemix.util.BluemixContextUtil;
import com.ibm.xsp.bluemix.util.RestUtil;

public class ImageRecognition implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String SERVICE_NAME = "watson_vision_combined";
	public static final String CLASSIFY_API = "/v3/classify";
	public static final String VERSION = "2016-05-20";
	
	private BluemixContextUtil bluemixUtil;
	private RestUtil rest;

	public ImageRecognition() {
		// Hardcoded Watson url/credentials for local testing
		String baseUrl  = "";
		String apikey   = "";
		
		bluemixUtil = new BluemixContextUtil(SERVICE_NAME, apikey, baseUrl);
		rest = new RestUtil();
	}

	public ArrayList<String[]> getVisualRecog(String imageUrl) throws JsonException, URISyntaxException, IOException {
		String apiKey = bluemixUtil.getApiKey();
		
		String getUrl = bluemixUtil.getBaseUrl().replace("https:", "http:") + CLASSIFY_API + "?url=" + imageUrl + "&api_key=" + apiKey + "&version=" + VERSION;
		Response response = rest.get(getUrl);
		
		//Convert the response into JSON data
		String content = EntityUtils.toString(response.returnResponse().getEntity());
		JsonJavaObject jsonData = rest.parse(content);
		
		//Retrieve the list of highest matching classifiers and associated confidences
		ArrayList<String[]> tags = getSuggestedTags(jsonData);
		if(tags != null && tags.size() > 0) {
			return tags;
		}
		return null;
	}
	
	public ArrayList<String[]> getSuggestedTags(JsonJavaObject data) {
		//ArrayList that will contain the set of classifiers and scores
		ArrayList<String[]> tags = new ArrayList<String[]>();
		
		//The data is returned as JSON with a root element of 'images'
		JsonJavaArray images = data.getAsArray("images");
		if (images != null) {
			//For each image, there is a set of scores in the JSON
			JsonJavaArray classifiers = images.getAsObject(0).getAsArray("classifiers");
			
			if(classifiers != null){
				JsonJavaArray classes = classifiers.getAsObject(0).getAsArray("classes");
				if(classes != null){
					//From the scores, get the 'name' of the classifier and it's associated 'score'
					for (int i = 0; i < classes.size(); i++) {
						JsonJavaObject jsonObj = classes.getAsObject(i);
						String tagName = jsonObj.getAsString("class");
						double score = jsonObj.getAsDouble("score");
						
						//Filter out classifiers with a match < 90%
						//if (score > 0.90) {
							//Add the classifier name and score to the ArrayList
							String[] tagInfo = new String[2];
							tagInfo[0] = tagName;
							tagInfo[1] = "" + score;
							tags.add(tagInfo);
						//}
					}
				}
			}
		}
		return tags;
	}
}
