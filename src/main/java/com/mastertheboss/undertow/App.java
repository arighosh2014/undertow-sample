package com.mastertheboss.undertow;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.util.Headers;
import io.undertow.util.Methods;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;
import org.xnio.Pooled;

import redis.clients.jedis.Jedis;


/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(getUndertowRouter()).build();
        server.start();

        System.out.println("Started server at http://127.1:8080/  Hit ^C to stop");
    }
    
    public static RoutingHandler getUndertowRouter(){
    	RoutingHandler routingHandler = Handlers.routing().add(Methods.GET, "/employees/{id}", new HttpHandler() {
			@Override
			public void handleRequest(HttpServerExchange exchange) throws Exception {
				getEmployee(exchange);
			}
		});
    	routingHandler.add(Methods.POST, "/employees",new HttpHandler() {
			@Override
			public void handleRequest(HttpServerExchange exchange) throws Exception {
				saveEmployee(exchange);
			}
		});
    	return routingHandler;
    }
    
    private static void saveEmployee(HttpServerExchange exchange) throws JSONException, IOException{
    	String requestBody = getRequestBody(exchange);
		JSONObject jsonObj = new JSONObject(requestBody);
		saveEmployeeInRedis(jsonObj);
    }
    
    private static void getEmployee(HttpServerExchange exchange) throws JSONException{
    	exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
		exchange.setResponseCode(201);
		String employeeId = exchange.getQueryParameters().get("id").pop();
		String value = getEmployeeFromRedis(employeeId);
		if(value == null){
			exchange.setResponseCode(404);
			exchange.getResponseSender().send("No employees found");
		}else{
			exchange.getResponseSender().send(value);
		}
    }
    
    private static void saveEmployeeInRedis(JSONObject jsonObj) throws JSONException{
    	Jedis jedis = new Jedis("localhost");
		jedis.set(jsonObj.get("id").toString(), jsonObj.get("name").toString());
		jedis.close();
    }
    
    private static String getEmployeeFromRedis(String employeeId) throws JSONException{
    	Jedis jedis = new Jedis("localhost");
		String value = jedis.get(employeeId);
		if(value == null){
			jedis.close();
			return null;
		}
		JSONObject obj = new JSONObject();
		obj.put("id", employeeId);
		obj.put("name", value);
		jedis.close();
		return obj.toString();
    }
    
    private static String getRequestBody(HttpServerExchange exchange) throws IOException{
        Pooled<ByteBuffer> pooledByteBuffer = exchange.getConnection().getBufferPool().allocate();
        ByteBuffer byteBuffer = pooledByteBuffer.getResource();
        byteBuffer.clear();

        exchange.getRequestChannel().read(byteBuffer);
        int pos = byteBuffer.position();
        byteBuffer.rewind();
        byte[] bytes = new byte[pos];
        byteBuffer.get(bytes);

        String requestBody = new String(bytes, Charset.forName("UTF-8") );

        byteBuffer.clear();
        pooledByteBuffer.free();
		return requestBody;
    }
}
