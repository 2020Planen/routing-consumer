/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.acme;

import classesToDelete.ConfigJsonObject;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;

/**
 *
 * @author Rasmu
 */
@ApplicationScoped
public class EntryConsumer {

    Gson gson = new Gson();

    public String getHttpData(String url) throws MalformedURLException, IOException {
        URL getUrl = new URL(url);
        Scanner scanner = new Scanner(getUrl.openStream());
        String response = scanner.useDelimiter("\\Z").next();
        scanner.close();

        return response;
    }

    @Incoming("routing")
    public void consumeEntry(String msg) throws IOException, Exception {
        System.out.println("Recieved data: " + msg);

        Map map = gson.fromJson(msg, Map.class);
        String pdref = (String) map.get("producerReference");
        System.out.println("PR: " + pdref);

        String json = getHttpData("http://cis-x.convergens.dk:5984/routingslips/_design/RoutingSlip/_view/by_producerReference?key=\"" + pdref + "\"");

        JsonObject jsonObj = new JsonParser().parse(json).getAsJsonObject();

        JsonElement testJson = jsonObj.getAsJsonArray("rows").get(0).getAsJsonObject().get("value").deepCopy();
        JsonObject finalJson = new JsonObject();
        finalJson.add("conditions", testJson);

        ConfigJsonObject cjObj = new ConfigJsonObject("routingConsumer");
        cjObj.convertJsonToEntity(finalJson.toString());

        System.out.println(cjObj.toString());
        cjObj.director();

    }
}
