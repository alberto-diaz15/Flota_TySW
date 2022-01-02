package edu.uclm.esi.tys2122.tienda;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import edu.uclm.esi.tys2122.http.PSController;

public class PSProxy {

	private String clave, url;
	
	private PSProxy() {
		this.clave = "T17SUK34U6SBA7HK4N9E74KLZCXFVNGC";
		this.url = "http://pshop05.esi.uclm.es/api";
	}
	
	private static class PSProxyHolder{
		static PSProxy singleton = new PSProxy();
	}
	
	public static PSProxy get() {
		return PSProxyHolder.singleton;

	}
	
	public String getProductos() throws ClientProtocolException, IOException {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet get = new HttpGet("https://"+ this.clave + "@"+ this.url +"?output_format=JSON");
		CloseableHttpResponse response = client.execute(get);
		HttpEntity entity = response.getEntity();
		String result = EntityUtils.toString(entity);
		client.close();
		return result;
		
	}
	
	

}
