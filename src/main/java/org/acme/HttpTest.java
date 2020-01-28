/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.acme;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

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

    public static void main(String[] args) throws IOException {
        String param = "testParam";
        HttpTest httpTest = new HttpTest();
        String json = httpTest.getHttpData("http://cis-x.convergens.dk:5984/routingslips/_design/RoutingSlip/_view/by_producerReference?key=\"" + param + "\"");

        JsonObject jsonObj = new JsonParser().parse(json).getAsJsonObject();

        JsonElement testJson = jsonObj.getAsJsonArray("rows").get(0).getAsJsonObject().get("value").deepCopy();
        JsonObject finalJson = new JsonObject();
        finalJson.add("conditions", testJson);

        System.out.println(finalJson);

    }

}
