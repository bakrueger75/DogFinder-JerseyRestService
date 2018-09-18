package com.bk.webservices;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class WebServiceJSONAdapter {
	
	public static final String CLASSNAME = WebServiceJSONAdapter.class.getName();
	
    static Logger log = Logger.getLogger(CLASSNAME);   

    private static WebServiceJSONAdapter _instance;
	private JSONParser parser;
	
	public static WebServiceJSONAdapter getInstance() throws Exception{
		if(_instance == null)
			_instance = new WebServiceJSONAdapter();
		return _instance;
	}
	
	
	private WebServiceJSONAdapter() throws Exception {
		super();
		parser = new JSONParser();	
	}
	
	/**
	 * This method will make a POST call to the web service, and return its response in json format
	 * @param url - the web service url
	 * @return responseJSON - the response from the web service in json format
	 * @throws Exception
	 */
	public JSONObject postJSONRequest(String url)throws Exception{
		JSONObject responseJSON = null;
		
		String responseString = executeRequest(url);
		responseJSON = parseJSONResponse(responseString);
		
        return responseJSON;

	}

	/**
	 * This method is responsible for executing the POST request to the web service.
	 * @param url - the url of the web service
	 * @return responseString - the web service json response in string format
	 * @throws Exception
	 */
	private String executeRequest(String url) throws Exception {
		//final String METHODNAME = "executeRequest";
		String responseString = null;
		
		HttpURLConnection conn = null;
		try{
			if(url != null){
				URL jsonURL = new URL(url);
				conn = (HttpURLConnection)jsonURL.openConnection();
				conn.setConnectTimeout(2000);
				conn.setReadTimeout(2000);
				conn.setRequestMethod("POST");
				
				InputStream in = new BufferedInputStream(conn.getInputStream());
				responseString = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
			}
		}
		catch(IOException io){
			io.printStackTrace();
			log.severe("Failed calling json service IO Error: " + url + " - " + io.getMessage());
		}
		catch(Exception e){
			e.printStackTrace();
			log.severe("Failed calling json service: " + url + " - " + e.getMessage());
		}
		finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		
		return responseString;
	}
	
	/**
	 * This method is responsible for parsing the web service response into a JSON object
	 * @param responseString - the web service json response in string format
	 * @return responseJSON - the web service response in json format
	 * @throws Exception
	 */
	private JSONObject parseJSONResponse(String responseString) throws Exception {
		JSONObject responseJSON = null;
		
		try {
			if(responseString != null){
				responseJSON = (JSONObject)parser.parse(responseString);
			}
		} catch (ParseException pe) {
			pe.printStackTrace();
			log.severe("Exception when parsing json response: " + pe.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			log.severe("Exception when parsing json response: " + e.getMessage());
		}
		
		return responseJSON;
	}


	public JSONObject performDogSearch(String searchTerm, boolean breedSearch) throws Exception {
		JSONObject dogResults = new JSONObject();
		if (searchTerm != null && searchTerm.length() > 0) {
			try {
				// The service URL should in a properties file instead of in the code.
				JSONObject jsonResults = this.postJSONRequest("https://dog.ceo/api/breeds/list/all");
				if (jsonResults != null) {
					JSONArray dogMatches = new JSONArray();
					JSONObject dogList = (JSONObject)jsonResults.get("message");
					Iterator dogBreeds = dogList.keySet().iterator();
					boolean searchMatch = false;
					boolean subBreedMatch = false;
					JSONArray subBreedsMatched = null;
					int matchCount = 0;
					String[] searchTerms = URLDecoder.decode(searchTerm, "UTF-8").split(" ");
					int searchTermMatches = 0;
					/*
					 * Loop through the dog breeds from the rest service and look for a match on the selected breed name or search term
					 * in the master breed list or the sub breeds.
					 */
					while (dogBreeds.hasNext()) {
						searchTermMatches = 0;
						searchMatch = false;
						subBreedMatch = false;
						subBreedsMatched = new JSONArray();
						String dogBreed = (String)dogBreeds.next();
						// Look for a search term match for the master breed
						for (int j=0; j < searchTerms.length; j++) {
							if ((breedSearch && dogBreed.equals(searchTerms[j])) || dogBreed.indexOf(searchTerms[j]) >= 0) {
								searchTermMatches++;
							}
						}
						if (searchTermMatches == searchTerms.length) {
							matchCount++;
							searchMatch = true;
						}
						if (!breedSearch) {
							// If the customer is using a search term, look through the sub breeds for additional matches.
							JSONArray subBreeds = (JSONArray)dogList.get(dogBreed);
							if (subBreeds != null && subBreeds.size() > 0) {
								for (int i=0; i< subBreeds.size(); i++) {
									searchTermMatches = 0;
									String subBreed = (String)subBreeds.get(i);
									String fullBreedName = dogBreed + " " + subBreed; 
									if (!searchMatch) {
										for (int k=0; k < searchTerms.length; k++) {
											if (fullBreedName.indexOf(searchTerms[k]) >= 0) {
												searchTermMatches++;
											}
										}
									}
									if (searchMatch || searchTermMatches == searchTerms.length) {
										subBreedMatch = true;
										// Add the searchTerm matched subBreed to the list of subBreedMatches, 
										// these will be added to the search results below.
										subBreedsMatched.add(subBreed);
										matchCount++;
									}
								}
							}
						}
						if (searchMatch || subBreedMatch) {
							JSONObject dogMatch;
							if (searchMatch) {
								// If the master breed search found a match, get a random image for the dog breed.
								
								// The service URL should in a properties file instead of in the code.
								JSONObject imageResults = this.postJSONRequest("https://dog.ceo/api/breed/"+dogBreed+"/images/random");
								String dogImage = null;
								if (imageResults != null) {
									dogImage = (String)imageResults.get("message");
								}
	
								//  Add the matching breed to the search results.
								dogMatch = new JSONObject();
								dogMatch.put("breedName", dogBreed);
								dogMatch.put("subBreeds", dogList.get(dogBreed));
								if (dogImage != null) {
									dogMatch.put("image", dogImage);
								}
								dogMatches.add(dogMatch);
							}
							
							if (breedSearch || subBreedMatch) {
								// If the customer is doing a breed search or the searchTerm matched a subbreed, 
								// pull the corresponding subbreeds and add them to the results.
								JSONArray subBreeds = null;
								if (breedSearch) {
									// Use any subbreeds from the master breed
									subBreeds= (JSONArray)dogList.get(dogBreed);
								} else {
									// Use the subbreeds that matched the searchTerm
									subBreeds= subBreedsMatched;
								}
								if (subBreeds != null && subBreeds.size() > 0) {
									for (int i=0; i< subBreeds.size(); i++) {
										// Find a random sub breed image.
										String subBreed = (String)subBreeds.get(i);
										// The service URL should in a properties file instead of in the code.
										JSONObject imageResults = this.postJSONRequest("https://dog.ceo/api/breed/"+dogBreed+"/"+subBreed+"/images/random");
										String dogImage = null;
										if (imageResults != null) {
											dogImage = (String)imageResults.get("message");
										}
		
										//  Add the matching subbreed to the search results.
										dogMatch = new JSONObject();
										dogMatch.put("breedName", subBreed + " " + dogBreed);
										if (dogImage != null) {
											dogMatch.put("image", dogImage);
										}
										dogMatches.add(dogMatch);
										
										if (breedSearch) matchCount++;
									}
								}
							}
						}
					}
					dogResults.put("dogCount", "" + matchCount);
					if (dogMatches.size() > 0) {
						// Sort the dog results alphabetically by breedName
						Collections.sort(dogMatches,new Comparator(){
							   public int compare(final Object a, Object b) {
								   JSONObject jsonA = (JSONObject)a;
								   JSONObject jsonB = (JSONObject)b;
								   return ((String)jsonA.get("breedName")).compareTo((String)jsonB.get("breedName"));
							     }
							 });
						dogResults.put("dogResults", dogMatches);
					} else {
						dogResults.put("dogResults", "");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.severe("Failed performing dog search: " + e.getMessage());
			} finally {
				if (dogResults == null || (dogResults != null && !dogResults.containsKey("dogResults"))) {
					dogResults.put("ERROR", "A FAILURE OCCURRED");
				}
			}
		}
		
		return dogResults;
	}


	public JSONObject getDogList() throws Exception {
		JSONObject dogList = new JSONObject();
		try {
			// The service URL should in a properties file instead of in the code.
			JSONObject jsonResults = this.postJSONRequest("https://dog.ceo/api/breeds/list");
			if (jsonResults != null) {
				dogList.put("breedList", jsonResults.get("message"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.severe("Failed getting dog breed list: " + e.getMessage());
		}
		
		return dogList;
	}

	
}

