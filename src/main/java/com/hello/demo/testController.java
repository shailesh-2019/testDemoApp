package com.hello.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.boot.json.JsonParser;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


/*@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}*/

@RestController
public class testController {
                private SFAuthenticationResponse auth;
                private String createdContactId;
                
                public testController() {
                                auth = login();
                                createdContactId = "";
                                System.out.println("####..AuthenticationResponse" + auth);
                }
                @PostMapping("/echo")
                public String echo(@RequestBody Map<String, Object> request) {
                                System.out.println("@@@@1..Request=" + request);
                                
                                JsonRequest requestObject = getRequestObject(request);
                                String responseText = "";
                                if(requestObject.currentIntentName.equalsIgnoreCase("WelcomeIntent")) {
                                                responseText = handleWelcomeIntent(requestObject.echoText);
                                } else if(requestObject.currentIntentName.equalsIgnoreCase("EchoIntent")) {
                                                responseText = handleEchoIntent(requestObject.echoText);
                                } else if(requestObject.currentIntentName.equalsIgnoreCase("CreateRecord")) {
                                                responseText = handleCreateIntent(requestObject.echoText);
                                } else if(requestObject.currentIntentName.equalsIgnoreCase("UpdateRecord")) {
                                                responseText = handleUpdateIntent(requestObject.echoText);
                                } else {
                                                responseText = "Intent not recognized!! - " + requestObject.currentIntentName;
                                }
                                
                                System.out.println("####..AuthenticationResponse" + login());
                                
                                
                                return getResponseJsonString(responseText);
                }
                
                public class JsonRequest {
                                public String echoText;
                                public String currentIntentName;
                                
                                public JsonRequest() {
                                                echoText = null;
                                                currentIntentName = null;
                                }
                                
                                public JsonRequest(String echoText, String currentIntentName) {
                                                this.echoText = echoText;
                                                this.currentIntentName = currentIntentName;
                                }
                }
                
                public JsonRequest getRequestObject(Map<String, Object> requestBody) {
                                Map<String, Object> queryResult = (Map<String, Object>)requestBody.get("queryResult");
                                Map<String, Object> parameters = (Map<String, Object>)queryResult.get("parameters");
                                System.out.println("@@@@.. Intent=" + parameters.get("currentIntentName"));
                                System.out.println("@@@@.. echoText=" + parameters.get("echoText"));
                                return new JsonRequest(parameters.get("echoText").toString(), parameters.get("currentIntentName").toString());                             
                }
                
                public String getResponseJsonString(String text) {
                                return "{" +
                                                                  "\"payload\": {" + 
                                                                  "\"google\": {" +
                                                                  "\"expectUserResponse\": true," +
                                                                  "\"richResponse\": {" +
                                                                  "\"items\": [" +
                                                                                          "{" +
                                                                                          "\"simpleResponse\": {" +
                                                                                          "\"textToSpeech\": \"" + text + "\"" +
                                                                                            "}" +
                                                                                          "}" +
                                                                                        "]" +
                                                                                      "}" +
                                                                                    "}" +
                                                                                  "}," +
                                                                                  "\"fulfillmentText\": \"" + text + "\"," +
                                                                                  "\"speech\": \"" + text + "\"," +
                                                                                  "\"displayText\": \"" + text + "\"," +
                                                                                  "\"source\": \"webhook-echo-sample\"" +
                                                                                "}";
                }
                
                public SFAuthenticationResponse login(){
                                HttpHeaders headers = new HttpHeaders();
                                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                                MultiValueMap<String, String> params= new LinkedMultiValueMap<String, String>();
                                
                                params.add("username", "pk@einstein.service.pkbyot");
                                params.add("password", "byotEinstein@Amazon2hgIb4Ks4WuGt6wxkvh1epfPr");
                                params.add("client_secret", "6C400CB2F1502495D74ECEE4F912238658F1CA34C21CDCD9CE030F9107BF4617");
                                params.add("client_id", "3MVG9jBOyAOWY5bWVkEaJwy7oSGqN24J46L3QZTRoynr7KePIOwDTWEqIbfuttDRKNSs7ZbjL6oEkVBVXgGGs");
                                params.add("grant_type","password");
                                
                                HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(params, headers);
                                
                                RestTemplate restTemplate = new RestTemplate();
                                ResponseEntity response = restTemplate.postForEntity("https://test.salesforce.com/services/oauth2/token", request, SFAuthenticationResponse.class);
                                return (SFAuthenticationResponse)response.getBody();
                }
                
                public String handleWelcomeIntent(String text) {
                                // Phone number hard-coded for now. But, will be available in paid-version of Google
                                // IMPORTANT: We might have to remove +1 from the phone number before performing SOSL. Otherwise,
                                // it throws a weird error
                                String phoneNumber = "8586039792";
                                try {
                                                
                                                HttpClient httpClient = HttpClientBuilder.create().build();
                                                String uri = auth.getInstance_url() + "/services/data/v53.0/search/?q=" +
                                                                                URLEncoder.encode("FIND {" + phoneNumber + "} IN ALL FIELDS RETURNING Contact(FirstName)", "UTF-8");
                                                System.out.println("xxxxxxxx.." + uri);
                                                HttpGet httpGet = new HttpGet(uri);
                                                httpGet.setHeader("Authorization", "Bearer " + auth.getAccess_token());
                                                HttpResponse queryResponse = httpClient.execute(httpGet);
                                                String response_string = EntityUtils.toString(queryResponse.getEntity());
                                                System.out.println("222222.." + response_string);
                                                
                                                JSONObject json = new JSONObject(response_string);
            System.out.println("JSON result of Query:\n" + json.toString(1));
            //JSONArray j = json.getJSONArray("records");
            String firstName = json.getJSONArray("searchRecords").getJSONObject(0).getString("FirstName");
            System.out.println("xxxxxxxx.." + firstName);
            return "Hi " + firstName + ", thanks for calling. Say something.";            
                                } catch(Exception e) {
                                                e.printStackTrace();
                                }
                                return "Still Testing Welcome Intent";
                }
                
                public String handleCreateIntent(String text) {
                                String uri = auth.getInstance_url() + "/services/data/v53.0/sobjects/Contact/";
                                
                                try {
                                                JSONObject contact = new JSONObject();
                                                contact.put("FirstName", "Jane");
                                                contact.put("LastName", "Doe");
                                                
                                                HttpClient httpClient = HttpClientBuilder.create().build();
                                                
            HttpPost httpPost = new HttpPost(uri);
            httpPost.setHeader("Authorization", "Bearer " + auth.getAccess_token());
            // The message we are going to post
            StringEntity body = new StringEntity(contact.toString(1));
            body.setContentType("application/json");
            httpPost.setEntity(body);

            //Make the request
            HttpResponse response = httpClient.execute(httpPost);

            //Process the results
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 201) {
                String response_string = EntityUtils.toString(response.getEntity());
                JSONObject json = new JSONObject(response_string);
                // Store the retrieved lead id to use when we update the lead.
                createdContactId = json.getString("id");
                System.out.println("New Contact id from response: " + createdContactId);
                return "Jane Doe was added as a contact.";
            } else {                    
                System.out.println("Insertion unsuccessful. Status code returned is " + statusCode);
                System.out.println("ERROR......" + EntityUtils.toString(response.getEntity()));
                return "Insertion unsuccessful.";
            }
                                } catch(Exception e) {
                                                e.printStackTrace();
                                }
                                
                                return "Still Testing Create Intent";
                }
                
                public String handleUpdateIntent(String text) {
                                String uri = auth.getInstance_url() + "/services/data/v53.0/sobjects/Contact/" + createdContactId;
                                
                                try {
                                                JSONObject contact = new JSONObject();
                                                contact.put("FirstName", "Jason");                                           
                                                
                                                HttpClient httpClient = HttpClientBuilder.create().build();
                                                
                                                HttpPatch httpPatch = new HttpPatch(uri);
                                                httpPatch.setHeader("Authorization", "Bearer " + auth.getAccess_token());
            // The message we are going to post
            StringEntity body = new StringEntity(contact.toString(1));
            body.setContentType("application/json");
            httpPatch.setEntity(body);

            //Make the request
            HttpResponse response = httpClient.execute(httpPatch);

            //Process the results
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 204) {
                return "Jane's name was successfully updated to Jason.";
            } else {                    
                System.out.println("Update unsuccessful. Status code returned is " + statusCode);
                System.out.println("ERROR......" + EntityUtils.toString(response.getEntity()));
                return "Update unsuccessful.";
            }
                                } catch(Exception e) {
                                                e.printStackTrace();
                                }
                                
                                return "Still Testing Update Intent";
                }
                
                public String handleEchoIntent(String text) {
                                return "Ha Ha, " + text;
                }

}
