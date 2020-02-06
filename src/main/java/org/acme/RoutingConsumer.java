/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.acme;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import javax.enterprise.context.ApplicationScoped;
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
        String conditionsStr = getJsonData(url + producerRef + "");
        JsonObject conditionsJson = new JsonParser().parse(conditionsStr).getAsJsonObject();

        
        JsonElement conditionsElm = conditionsJson
                .getAsJsonArray("rows")
                .get(0)
                .getAsJsonObject()
                .get("value")
                .deepCopy();

        gson.fromJson(conditionsElm, Message.class);
        
        
        msg.sendToKafkaQue();

    }

    public String getJsonData(String url) throws MalformedURLException, IOException {
        URL getUrl = new URL(url);
        Scanner scanner = new Scanner(getUrl.openStream());
        String response = scanner.useDelimiter("\\Z").next();
        scanner.close();

        return response;
    }

}
