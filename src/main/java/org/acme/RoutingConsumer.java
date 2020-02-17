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
import javax.enterprise.context.ApplicationScoped;
import org.acme.jsonObjectMapper.Message;
import org.eclipse.microprofile.reactive.messaging.Incoming;

/**
 *
 * @author Rasmu
 */
@ApplicationScoped
public class RoutingConsumer {

    private String url = "http://cis-x.convergens.dk:5984/routingslips/_design/RoutingSlip/_view/by_producerReference?key=";
    Gson gson = new Gson();

    @Incoming("routing")
    public void consume(String content) throws IOException, Exception {
        Message msg = gson.fromJson(content, Message.class);
        msg.startLog("routing");

        String producerRef = msg.getProducerReference();
        String conditionsStr = getJsonData(url + "%22" + producerRef + "%22");

        /*  Det gamle kode
        JsonObject conditionsJson = new JsonParser().parse(conditionsStr).getAsJsonObject();

        
        JsonElement conditionsElm = conditionsJson
                .getAsJsonArray("rows")
                .get(0)
                .getAsJsonObject()
                .get("value")
                .deepCopy();
        
        //gson.fromJson(conditionsElm, Message.class);
        
        
        //
        ObjectMapper objectMapper  = new ObjectMapper();
        ObjectReader objectReader = objectMapper.readerForUpdating(msg);
        Message newMsg = objectReader.readValue(conditionsStr);
        
        System.out.println("NEW MESSAGE: " + newMsg);
        //

         */
        
        // Ny kode med brug af Jackson. Den laver et nyt objekt med samme hashkode
        // Det er kun blevet testet i test-klassen, men det brude virke
        // Taget udgangspunkt i dette: https://www.logicbig.com/tutorials/misc/jackson/reader-for-updating.html 
        JsonObject conditionsJson = new JsonParser().parse(conditionsStr).getAsJsonObject();
        JsonElement conditionsElm = conditionsJson
                .getAsJsonArray("rows")
                .get(0)
                .getAsJsonObject()
                .get("value")
                .getAsJsonArray();

        String conditionssList = "{\"conditionsList\":" + conditionsElm + "}";
        System.out.println(conditionssList);

        msg.startLog("ReciverAPI");
        msg.setEntryTime(System.currentTimeMillis());
        msg.setProducerReference("testPR");
        msg.endLog();

        System.out.println("OLD MESSAGE: " + msg.toString());
        System.out.println(System.identityHashCode(msg));

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectReader objectReader = objectMapper.readerForUpdating(msg);
        Message newMsg = objectReader.readValue(conditionssList);

        System.out.println("New MESSAGE: " + newMsg.toString());
        System.out.println(System.identityHashCode(newMsg));

        newMsg.sendToKafkaQue();

    }

    public String getJsonData(String url) throws MalformedURLException, IOException {
        URL getUrl = new URL(url);
        Scanner scanner = new Scanner(getUrl.openStream());
        String response = scanner.useDelimiter("\\Z").next();
        scanner.close();

        return response;
    }

}
