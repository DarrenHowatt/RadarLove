package ca.radarlove.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

public class HttpClient {
	
	public static void SendHttpPost(String URL, JSONObject jsonObjSend){

		HttpPost httpPostRequest = new HttpPost(URL);		
		httpPostRequest.setHeader("Accept", "application/json");
		httpPostRequest.setHeader("Content-type", "application/json");
		httpPostRequest.setHeader("User-Agent","Acme Device");	
		
		try{
			StringEntity entity = new StringEntity(jsonObjSend.toString());
			httpPostRequest.setEntity(entity);
			
			DefaultHttpClient httpclient = new DefaultHttpClient();
			@SuppressWarnings("unused")
			HttpResponse response        = (HttpResponse) httpclient.execute(httpPostRequest);

		}
		catch(Exception ex){
			ex.printStackTrace();
		}

	}
	
	public static String SendHttpPut(String URL, JSONObject jsonObjSend){
		
		String getResults = "";
		
		HttpPut httpPutRequest = new HttpPut(URL);
		httpPutRequest.setHeader("Content-type", "application/json");
		httpPutRequest.setHeader("User-Agent","Acme Device");
		
		
		try {
			StringEntity entity = new StringEntity(jsonObjSend.toString());
			httpPutRequest.setEntity(entity);
			
			DefaultHttpClient httpclient = new DefaultHttpClient();
			
			HttpResponse response        = (HttpResponse) httpclient.execute(httpPutRequest);
			
			InputStream responseStream   = response.getEntity().getContent();
			getResults                   = ConvertStreamToString(responseStream);
					
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		return getResults;
	}
	
	public static String SendHttpGet(String URL){
		
		String getResults = "";
			
		try{
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpGet httpGetRequest       = new HttpGet(URL);
			
			httpGetRequest.setHeader("Content-type", "application/json");
			httpGetRequest.setHeader("User-Agent", "Acme Device");
			
			HttpResponse response = httpclient.execute(httpGetRequest);
			
			InputStream result    = response.getEntity().getContent();
			getResults            = ConvertStreamToString(result);

		}
		catch(Exception ex){
				getResults = ex.getMessage(); 
				ex.printStackTrace();
		}
		
		return getResults;
	}

	private static String ConvertStreamToString(InputStream stream)
	{
		String line            = "";
		String completedResult = "";
		StringBuilder builder  = new StringBuilder();
		BufferedReader reader  = new BufferedReader(new InputStreamReader(stream));
		
		
		try {
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			reader.close();
		} catch (IOException e) {
			// TODO - fix exception reporting
			e.printStackTrace();
		}
		completedResult = builder.toString();		
		return completedResult;
	}


}
