package com.bkfinderservies.rest;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.logging.Logger;

//import java.util.*;
//import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
//import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
//import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.bk.webservices.WebServiceJSONAdapter;

@Path("bkdogfinderservice")
public class BKDogFinderRestService {

	public static final String CLASSNAME = BKDogFinderRestService.class.getName();
	
    static Logger log = Logger.getLogger(CLASSNAME);   

		public BKDogFinderRestService() {
	    }

	    @GET
	    @Path("searchByTerm/{searchTerm}")
	    @Produces(MediaType.APPLICATION_JSON)
	    public Response searchByTerm(@PathParam("searchTerm") String searchTerm) {
	    	JSONObject searchResults = null;
	    	try {
				WebServiceJSONAdapter jsonAdapter = WebServiceJSONAdapter.getInstance();
				searchResults = jsonAdapter.performDogSearch(searchTerm, false);
			} catch (Exception e) {
				e.printStackTrace();
				log.severe("Failed performing dog search: " + e.getMessage());
			} finally {
				if (searchResults == null) {
					searchResults = new JSONObject();
					searchResults.put("ERROR", "AN ERROR OCCURRED");
				}
			}

			return Response.ok()
					.entity(searchResults)
					.header("Access-Control-Allow-Origin",  "*")
					.header("Access-Control-Allow-Methods",  "POST, GET, PUT, UPDATE, OPTIONS")
					.header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
					.build();
	    }

	    @GET
	    @Path("searchByBreed/{breedName}")
	    @Produces(MediaType.APPLICATION_JSON)
	    public Response searchByBreed(@PathParam("breedName") String breedName) {
	    	JSONObject searchResults = null;
			try {
				WebServiceJSONAdapter jsonAdapter = WebServiceJSONAdapter.getInstance();
				searchResults = jsonAdapter.performDogSearch(breedName, true);
			} catch (Exception e) {
				e.printStackTrace();
				log.severe("Failed performing dog search: " + e.getMessage());
			} finally {
				if (searchResults == null) {
					searchResults = new JSONObject();
					searchResults.put("ERROR", "AN ERROR OCCURRED");
				}
			}
			return Response.ok()
					.entity(searchResults)
					.header("Access-Control-Allow-Origin",  "*")
					.header("Access-Control-Allow-Methods",  "POST, GET, PUT, UPDATE, OPTIONS")
					.header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
					.build();
	    }


	    @GET
	    @Path("getDogList")
	    @Produces(MediaType.APPLICATION_JSON)
	    public Response getDogList() {
	    	JSONObject searchResults = null;
			try {
				WebServiceJSONAdapter jsonAdapter = WebServiceJSONAdapter.getInstance();
				searchResults = jsonAdapter.getDogList();
			} catch (Exception e) {
				e.printStackTrace();
				log.severe("Failed performing dog search: " + e.getMessage());
			} finally {
				if (searchResults == null) {
					searchResults = new JSONObject();
					searchResults.put("ERROR", "AN ERROR OCCURRED");
				}
			}

			return Response.ok()
					.entity(searchResults)
					.header("Access-Control-Allow-Origin",  "*")
					.header("Access-Control-Allow-Methods",  "POST, GET, PUT, UPDATE, OPTIONS")
					.header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
					.build();
	    }
}