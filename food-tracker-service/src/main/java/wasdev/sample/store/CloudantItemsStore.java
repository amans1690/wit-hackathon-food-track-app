/******************************************************************************
 * Copyright (c) 2018 IBM Corp.                                               *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 *    http://www.apache.org/licenses/LICENSE-2.0                              *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 ******************************************************************************/
package wasdev.sample.store;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.google.gson.JsonObject;

import wasdev.sample.Item;

public class CloudantItemsStore implements ItemsStore {
    
    private Database db = null;
    private static final String databaseName = "food-tracker";
    
    public CloudantItemsStore(){
        CloudantClient cloudant = createClient();
        if(cloudant!=null){
         db = cloudant.database(databaseName, true);
        }
    }
    
    public Database getDB(){
        return db;
    }

    private static CloudantClient createClient() {
        
        String url;

        if (System.getenv("VCAP_SERVICES") != null) {
            // When running in IBM Cloud, the VCAP_SERVICES env var will have the credentials for all bound/connected services
            // Parse the VCAP JSON structure looking for cloudant.
            JsonObject cloudantCredentials = VCAPHelper.getCloudCredentials("cloudant");
            if(cloudantCredentials == null){
                System.out.println("No cloudant database service bound to this application");
                return null;
            }
            url = cloudantCredentials.get("url").getAsString();
        } else if (System.getenv("CLOUDANT_URL") != null) {
            url = System.getenv("CLOUDANT_URL");
        } else {
            System.out.println("Running locally. Looking for credentials in cloudant.properties");
            url = VCAPHelper.getLocalProperties("cloudant.properties").getProperty("cloudant_url");
            if(url == null || url.length()==0){
                System.out.println("To use a database, set the Cloudant url in src/main/resources/cloudant.properties");
                return null;
            }
        }

        try {
            System.out.println("Connecting to Cloudant");
            CloudantClient client = ClientBuilder.url(new URL(url)).build();
            return client;
        } catch (Exception e) {
            System.out.println("Unable to connect to database");
            //e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public Collection<Item> getAll(){
        List<Item> docs;
        try {
            docs = db.getAllDocsRequestBuilder().includeDocs(true).build().getResponse().getDocsAs(Item.class);
        } catch (IOException e) {
            return null;
        }
        return docs;
    }

    @Override
    public Item get(String id) {
        return db.find(Item.class, id);
    }


}
