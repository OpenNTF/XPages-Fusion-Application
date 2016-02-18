package com.ibm.xsp.bluemix.services.watson;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.fluent.Response;
import org.apache.http.util.EntityUtils;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaArray;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.xsp.bluemix.util.BluemixContextUtil;
import com.ibm.xsp.bluemix.util.RestUtil;
import com.ibm.xsp.component.UIFileuploadEx.UploadedFile;

public class ImageRecognition implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String SERVICE_NAME = "visual_recognition";
	String classifyApi = "v2/classify";
	String version = "2015-11-24";
	
	private BluemixContextUtil bluemixUtil;
	private RestUtil rest;

	public ImageRecognition() {
		// Hardcoded Watson url/credentials for local testing
		String baseUrl  = "";
		String username = "";
		String password = "";
		
		bluemixUtil = new BluemixContextUtil(SERVICE_NAME, username, password, baseUrl);
		rest = new RestUtil();
	}

	public ArrayList<String[]> getVisualRecog(UploadedFile imageFile) {
		//Get the server file object and pass it along
		File fileObj = imageFile.getUploadedFile().getServerFile();
		return getVisualRecog(fileObj);
	}
	
	public ArrayList<String[]> getVisualRecog(String imageFilePath, String imageFileName) throws IOException, URISyntaxException {
		File imageFile = null;
		if(imageFilePath.contains("cloudant")) {
			//Handle a cloudant database document image attachment
			
			//Create a temporary local file that can be uploaded to Watson for analysis
			String tempFileName = new SimpleDateFormat("MMddhhmmss").format(new Date()) + "_" + imageFileName;
			imageFile = new File(System.getProperty("java.io.tmpdir") + tempFileName);
			imageFile.deleteOnExit();
			
			//Send a GET request to cloudant to get the image attachment
			Response resp = rest.get(imageFilePath);
			//Copy the response input stream into the temp file
			FileUtils.copyInputStreamToFile(resp.returnContent().asStream(), imageFile);
		}else if(imageFilePath.contains("http")) {
			//Handle a URL image
			
			//Create a temporary local file that can be uploaded to Watson for analysis
			String tempFileName = new SimpleDateFormat("MMddhhmmss").format(new Date()) + "_" + imageFileName;
			imageFile = new File(System.getProperty("java.io.tmpdir") + tempFileName);
			imageFile.deleteOnExit();
			
			//Copy the image into the temporary file
			URL imageURL = new URL(imageFilePath);
			FileUtils.copyURLToFile(imageURL, imageFile);
		}else{
			//Handle file contained in the NSF, e.g. "/boston.jpg"
			imageFile = new File(imageFilePath);
		}
		return getVisualRecog(imageFile);
	}
	
	public ArrayList<String[]> getVisualRecog(File imageFile) {
		//Build the URL used to POST to Watson
		String postUrl = bluemixUtil.getBaseUrl() + "/" + classifyApi + "?version=" + version;
		
		try{
			//Specify the set of classifiers to use, or specify none to use all the default classifiers
			//JsonJavaObject postData = getClassifiers();
			
			//Send the POST request to Watson
			//Response response = rest.post(postUrl, bluemixUtil.getAuthorizationHeader(), postData, imageFile);
			Response response = rest.post(postUrl, bluemixUtil.getAuthorizationHeader(), null, imageFile);
			
			//Convert the response into JSON data
			String content = EntityUtils.toString(response.returnResponse().getEntity());
			JsonJavaObject jsonData = rest.parse(content);
			
			//Retrieve the list of highest matching classifiers and associated confidences
			ArrayList<String[]> tags = getSuggestedTags(jsonData);
			if(tags != null && tags.size() > 0) {
				return tags;
			}
		}catch(IOException e) {
			e.printStackTrace();
		} catch (JsonException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
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
			JsonJavaArray scores = images.getAsObject(0).getAsArray("scores");
			
			if(scores !=null){
				//From the scores, get the 'name' of the classifier and it's associated 'score'
				for (int i = 0; i < scores.size(); i++) {
					JsonJavaObject jsonObj = scores.getAsObject(i);
					String tagName = jsonObj.getAsString("name");
					double score = jsonObj.getAsDouble("score");
					
					//Filter out classifiers with a match < 66%
					if (score > 0.65) {
						//Add the classifier name and score to the ArrayList
						String[] tagInfo = new String[2];
						tagInfo[0] = tagName;
						tagInfo[1] = "" + score;
						tags.add(tagInfo);
					}
				}
			}
		}
		return tags;
	}
	
	public JsonJavaObject getClassifiers() {
		//A refined list of classifiers that can be customised
		//See here for the full default list...
		//https://watson-api-explorer.mybluemix.net/apis/visual-recognition-v2-beta?cm_mc_uid=79875180721914509895351&cm_mc_sid_50200000=1450989535#!/visual-recognition/getClassifiersService
		JsonJavaObject classifiers = new JsonJavaObject();
		JsonJavaArray classifier_ids = new JsonJavaArray();
		
		classifier_ids.add("Black");
		classifier_ids.add("Blue");
		classifier_ids.add("Brown");
		classifier_ids.add("Cyan");
		classifier_ids.add("Green");
		classifier_ids.add("Magenta");
		classifier_ids.add("Mixed_Color");
		classifier_ids.add("Orange");
		classifier_ids.add("Red");
		classifier_ids.add("Violet");
		classifier_ids.add("White");
		classifier_ids.add("Yellow");
		classifier_ids.add("Black_and_white");
		classifier_ids.add("Color");
		classifier_ids.add("Gray");
		classifier_ids.add("Chart");
		classifier_ids.add("Graphics");
		classifier_ids.add("Photo");
		classifier_ids.add("Activity");
		classifier_ids.add("Camping");
		classifier_ids.add("Banners");
		classifier_ids.add("Eating");
		classifier_ids.add("Leisure_Swimming");
		classifier_ids.add("Meeting");
		classifier_ids.add("People_Activity");
		classifier_ids.add("Adventure_Sport");
		classifier_ids.add("Climbing");
		classifier_ids.add("Ice_Climbing");
		classifier_ids.add("Land_Sailing");
		classifier_ids.add("Spelunking");
		classifier_ids.add("Air_Sport");
		classifier_ids.add("Ballooning");
		classifier_ids.add("Base_Jumping");
		classifier_ids.add("Bungee_Jumping");
		classifier_ids.add("Hang_Gliding");
		classifier_ids.add("Parachuting");
		classifier_ids.add("Parasailing");
		classifier_ids.add("Sky_Diving");
		classifier_ids.add("Animal_Sport");
		classifier_ids.add("Polo");
		classifier_ids.add("Rodeo");
		classifier_ids.add("Club_Sport");
		classifier_ids.add("Croquet");
		classifier_ids.add("Golf");
		classifier_ids.add("Combat_Sport");
		classifier_ids.add("Boxing");
		classifier_ids.add("Fist_Fighting");
		classifier_ids.add("Kickboxing");
		classifier_ids.add("Judo");
		classifier_ids.add("Karate");
		classifier_ids.add("Martial_Art");
		classifier_ids.add("Tae_Kwando");
		classifier_ids.add("BMX");
		classifier_ids.add("Cycling");
		classifier_ids.add("Velodrome");
		classifier_ids.add("Fishing");
		classifier_ids.add("Gymnastics");
		classifier_ids.add("Hunting");
		classifier_ids.add("Archery");
		classifier_ids.add("Leisure_Activity");
		classifier_ids.add("Rollerskating");
		classifier_ids.add("Skateboarding");
		classifier_ids.add("Tennis");
		classifier_ids.add("Running");
		classifier_ids.add("Sports");
		classifier_ids.add("Baseball");
		classifier_ids.add("Bat_Sport");
		classifier_ids.add("Cricket");
		classifier_ids.add("Softball");
		classifier_ids.add("American_Football");
		classifier_ids.add("Football");
		classifier_ids.add("Rugby");
		classifier_ids.add("Soccer");
		classifier_ids.add("Field_Hockey");
		classifier_ids.add("Stick_Sport");
		classifier_ids.add("Team_Field_Sport");
		classifier_ids.add("Basketball");
		classifier_ids.add("Indoor_Volleyball");
		classifier_ids.add("Team_Indoor_Sport");
		classifier_ids.add("Team_Sport");
		classifier_ids.add("High_Jump");
		classifier_ids.add("Hurdles");
		classifier_ids.add("Javelin");
		classifier_ids.add("Long_Jump");
		classifier_ids.add("Pole_Vault");
		classifier_ids.add("Shot_Put");
		classifier_ids.add("Track");
		classifier_ids.add("Track_and_Field");
		classifier_ids.add("Scuba");
		classifier_ids.add("Snorkeling");
		classifier_ids.add("Underwater_Sport");
		classifier_ids.add("Boating");
		classifier_ids.add("Jet_Skiing");
		classifier_ids.add("Kayaking");
		classifier_ids.add("Rowing");
		classifier_ids.add("Sailing");
		classifier_ids.add("Windsurfing");
		classifier_ids.add("Surface_Water_Sport");
		classifier_ids.add("Surfing");
		classifier_ids.add("Waterskiing");
		classifier_ids.add("Speed_Swimming");
		classifier_ids.add("Swimming");
		classifier_ids.add("Synchronized_Swimming");
		classifier_ids.add("Water_Polo");
		classifier_ids.add("Water_Sport");
		classifier_ids.add("Skiing");
		classifier_ids.add("Snowboarding");
		classifier_ids.add("Snow_Sport");
		classifier_ids.add("Winter_Sport");
		classifier_ids.add("Clothing");
		classifier_ids.add("Coat");
		classifier_ids.add("Dress");
		classifier_ids.add("Hat");
		classifier_ids.add("Shoes");
		classifier_ids.add("Skirt");
		classifier_ids.add("Swimsuit");
		classifier_ids.add("Trouser");
		classifier_ids.add("Underwear");
		classifier_ids.add("Belt");
		classifier_ids.add("Bracelet");
		classifier_ids.add("Earrings");
		classifier_ids.add("Eyeglasses");
		classifier_ids.add("Fashion_Accessory");
		classifier_ids.add("Handbag");
		classifier_ids.add("Headphones");
		classifier_ids.add("Jewel");
		classifier_ids.add("Neckless");
		classifier_ids.add("Sunglasses");
		classifier_ids.add("Wallet");
		classifier_ids.add("Watch");
		classifier_ids.add("Beverage");
		classifier_ids.add("Bottle");
		classifier_ids.add("Object");
		classifier_ids.add("People");
		classifier_ids.add("Face_(closeup)");
		classifier_ids.add("Face");
		classifier_ids.add("Head_and_Shoulders");
		classifier_ids.add("Full_Body");
		classifier_ids.add("Hands");
		classifier_ids.add("Person_View");
		classifier_ids.add("Air_Vehicle");
		classifier_ids.add("Airplane");
		classifier_ids.add("Commercial_Plane");
		classifier_ids.add("Military_Plane");
		classifier_ids.add("Small_Plane");
		classifier_ids.add("Helicopter");
		classifier_ids.add("Hot_Air_Balloon");
		classifier_ids.add("Bicycle");
		classifier_ids.add("Bike");
		classifier_ids.add("Motorcycle");
		classifier_ids.add("Car");
		classifier_ids.add("Race_Car");
		classifier_ids.add("Sedan");
		classifier_ids.add("SUV");
		classifier_ids.add("Land_Vehicle");
		classifier_ids.add("Sled");
		classifier_ids.add("Snow_Vehicle");
		classifier_ids.add("Vehicle");
		classifier_ids.add("Boat");
		classifier_ids.add("Jet_Ski");
		classifier_ids.add("Sailboat");
		classifier_ids.add("Surfboard");
		classifier_ids.add("Sailing_Ship");
		classifier_ids.add("Ship");
		classifier_ids.add("Water_Vehicle");
		classifier_ids.add("Big_Group");
		classifier_ids.add("Crowd");
		classifier_ids.add("Group_of_People");
		classifier_ids.add("Small_Group");
		classifier_ids.add("Two_People");
		classifier_ids.add("Human");
		classifier_ids.add("Adult");
		classifier_ids.add("Female_Adult");
		classifier_ids.add("Male_Adult");
		classifier_ids.add("Baby");
		classifier_ids.add("Child");
		classifier_ids.add("Girl");
		classifier_ids.add("Person");
		classifier_ids.add("Indoors");
		classifier_ids.add("Locker_Room");
		classifier_ids.add("Corridor");
		classifier_ids.add("Passageway");
		classifier_ids.add("Auditorium");
		classifier_ids.add("Closet");
		classifier_ids.add("Closet_or_Cupboard");
		classifier_ids.add("Residential_Space");
		classifier_ids.add("Clothing_Store");
		classifier_ids.add("Drugstore");
		classifier_ids.add("Store");
		classifier_ids.add("Supermarket");
		classifier_ids.add("Warehouse");
		classifier_ids.add("Activity_Facility");
		classifier_ids.add("Golf_Course");
		classifier_ids.add("Sports_Field");
		classifier_ids.add("Swimming_Pool");
		classifier_ids.add("Track_Scene");
		classifier_ids.add("Campsite");
		classifier_ids.add("Boathouse");
		classifier_ids.add("Skyline_(day)");
		classifier_ids.add("Skyline_(night)");
		classifier_ids.add("Skyline");
		classifier_ids.add("Crosswalk");
		classifier_ids.add("Market");
		classifier_ids.add("Street_Scene");
		classifier_ids.add("Urban_Scene");
		classifier_ids.add("Water_Structure");
		classifier_ids.add("Cliff");
		classifier_ids.add("Desert");
		classifier_ids.add("Corn_Field");
		classifier_ids.add("Field");
		classifier_ids.add("Hayfield");
		classifier_ids.add("Forest");
		classifier_ids.add("Greenery");
		classifier_ids.add("Canyons_and_Rock_Formations");
		classifier_ids.add("Mountain_Scene");
		classifier_ids.add("Mountains");
		classifier_ids.add("Rock_Arch");
		classifier_ids.add("Nature_Scene");
		classifier_ids.add("Park");
		classifier_ids.add("Coral_Reef");
		classifier_ids.add("Underwater");
		classifier_ids.add("Volcano");
		classifier_ids.add("Bayou");
		classifier_ids.add("Beach");
		classifier_ids.add("Creek");
		classifier_ids.add("Harbor");
		classifier_ids.add("Ocean_(waves)");
		classifier_ids.add("Urban_Canal");
		classifier_ids.add("Water_Scene");
		classifier_ids.add("Waterfall");
		classifier_ids.add("Waterscape");
		classifier_ids.add("Snow_Scene");
		classifier_ids.add("Snowy_Mountains");
		classifier_ids.add("Winter_Scene");
		classifier_ids.add("Outdoors");
		classifier_ids.add("Scene");
		classifier_ids.add("Blue_Sky");
		classifier_ids.add("Gray_Sky");
		classifier_ids.add("Lightning");
		classifier_ids.add("Night_Sky");
		classifier_ids.add("Sky_Scene");
		classifier_ids.add("Sunset");
		
		classifiers.putArray("classifier_ids", classifier_ids);

		return classifiers;
	}
}
