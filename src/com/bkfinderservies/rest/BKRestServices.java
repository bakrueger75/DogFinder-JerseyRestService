package com.bkfinderservies.rest;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("services")
public class BKRestServices extends ResourceConfig {
   public BKRestServices() {
        packages("com.fasterxml.jackson.jaxrs.json");
        packages("com.bkfinderservices.rest");
   }
}