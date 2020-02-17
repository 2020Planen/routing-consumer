/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.acme;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import org.acme.jsonObjectMapper.Message;

/**
 *
 * @author Rasmu
 */
public class HttpTest {

    public String getHttpData(String url) throws MalformedURLException, IOException {
        URL getUrl = new URL(url);
        Scanner scanner = new Scanner(getUrl.openStream());
        String response = scanner.useDelimiter("\\Z").next();
        scanner.close();

        return response;
    }

    Gson gson = new Gson();

    public static void main(String[] args) throws IOException {

        String url = "http://cis-x.convergens.dk:5984/routingslips/_design/RoutingSlip/_view/by_producerReference?key=";
        HttpTest httpTest = new HttpTest();
        String conditionsStr = httpTest.getHttpData(url + "%22" + "address" + "%22");
        
        JsonObject conditionsJson = new JsonParser().parse(conditionsStr).getAsJsonObject();
        JsonElement conditionsElm = conditionsJson
                .getAsJsonArray("rows")
                .get(0)
                .getAsJsonObject()
                .get("value")
                .getAsJsonArray();
        
        String conditionssList = "{\"conditionsList\":" + conditionsElm+ "}";
        System.out.println(conditionssList);
        
        
        
        
        
        Message message = new Message();
        message.startLog("ReciverAPI");
        message.setEntryTime(System.currentTimeMillis());
        message.setProducerReference("testPR");
        message.endLog();
        
        
        
        System.out.println("OLD MESSAGE: " + message.toString());
        System.out.println(System.identityHashCode(message));

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectReader objectReader = objectMapper.readerForUpdating(message);
        Message newMsg = objectReader.readValue(conditionssList);

        System.out.println("New MESSAGE: " + newMsg.toString());   
        System.out.println(System.identityHashCode(newMsg));
    }

}
