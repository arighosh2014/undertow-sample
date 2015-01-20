package com.mastertheboss.undertow;

import io.undertow.testutils.DefaultServer;
import io.undertow.testutils.HttpClientUtils;
import io.undertow.testutils.TestHttpClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit test for simple App.
 */
@RunWith(DefaultServer.class)
public class AppTest
{
    /**
     * Rigourous Test :-)
     * @throws IOException 
     * @throws ClientProtocolException 
     */
	
	
	@BeforeClass
    public static void setup() throws IOException {
		DefaultServer.setRootHandler(App.getUndertowRouter());
    }
	
	@Test
	public void testAppSave() throws JSONException, ClientProtocolException, IOException{
		TestHttpClient client = new TestHttpClient();
		try{
			HttpPost post = new HttpPost(DefaultServer.getDefaultServerURL() + "/employees");
			JSONObject obj = new JSONObject();
			obj.put("id", "123");
			obj.put("name","Arindam");
			StringEntity content = new StringEntity(obj.toString(),"UTF-8");
			content.setContentType("application/json; charset=UTF-8");
            post.setEntity(content);
			client.execute(post);
			Assert.assertTrue(true);
			
		}finally{
			client.getConnectionManager().shutdown();
		}
	}
	
	@Test
    public void testApp() throws ClientProtocolException, IOException, JSONException
    {
    	TestHttpClient client = new TestHttpClient();
    	try{

            HttpGet get = new HttpGet(DefaultServer.getDefaultServerURL()+"/employees/123");
            HttpResponse result = client.execute(get);
            Assert.assertEquals(201, result.getStatusLine().getStatusCode());
            JSONObject obj = new JSONObject(HttpClientUtils.readResponse(result));
            Assert.assertEquals("Arindam", obj.get("name"));
    		
    	}finally{
    		client.getConnectionManager().shutdown();
    	}
    }
}
