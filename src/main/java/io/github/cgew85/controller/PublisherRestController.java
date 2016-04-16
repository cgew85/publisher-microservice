package io.github.cgew85.controller;

import io.github.cgew85.domain.Info;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;

/**
 * Created by cgew85 on 13.04.2016.
 */
@RestController
public class PublisherRestController {

    /**
     * Checks the availability of a given publishing server.
     *
     * @param address   -   The address of the publishing server.
     * @param port      -   The port of the publishing server.
     * @return          -   A string object containing the http status code.
     */
    @RequestMapping(value = "/v0/available", method = RequestMethod.GET)
    public String available(@RequestParam(value = "address") String address, @RequestParam(value = "port") String port) {
        return getJsonObjectFromHttpGetRequest(new HttpGet("http://" + address + ":" + port + "/available")).toJSONString();
    }

    @RequestMapping(value = "/v0/create", method = RequestMethod.POST)
    public String create(@RequestParam(value = "address") String address, @RequestParam(value = "port") String port,
                         @RequestParam(value = "dataXml") String dataXml, @RequestParam(value = "layoutXml") String layoutXml) {
        final CloseableHttpClient closeableHttpClient = HttpClients.createMinimal();
        final HttpPost httpPost = new HttpPost("http://" + address + ":" + port + "/v0/publish");
        final JSONObject jsonObject = new JSONObject();
        try {
            final StringEntity stringEntity = new StringEntity(
                    "{\"layout.xml\":\"" + layoutXml.replace(" ", "+")
                            + "\", \"data.xml\":\"" + new String(dataXml.replace(" ", "+").getBytes(), "UTF-8") + "\"}");
            stringEntity.setContentType("application/json");
            final StringWriter writer = new StringWriter();
            IOUtils.copy(stringEntity.getContent(), writer, "UTF-8");
            httpPost.setEntity(stringEntity);
        } catch(Exception e) {
            jsonObject.put("id", -1);
            return jsonObject.toJSONString();
        }
        try(CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost)) {
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
            final StringBuilder stringBuilder = new StringBuilder();
            String temp;
            while((temp = bufferedReader.readLine()) != null) {
                stringBuilder.append(temp);
            }
            jsonObject.put("id", stringBuilder.toString());
            return jsonObject.toJSONString();
        } catch(Exception e) {
            jsonObject.put("id", -1);
            return jsonObject.toJSONString();
        }
    }

    @RequestMapping(value = "/v0/info", method = RequestMethod.GET)
    public Info info(@RequestParam(value = "address") String address, @RequestParam(value = "port") String port,
                     @RequestParam(value="id") String id) {
        return new Info("processId", "errorStatus", "result", "message", "finished");
    }

    @RequestMapping(value = "/v0/download", method = RequestMethod.GET)
    public String download(@RequestParam(value = "address") String address, @RequestParam(value = "port") String port,
                           @RequestParam(value="id") String id) {
        return "not available right now";
    }

    /**
     * Deletes a certain folder for a given id on a given publishing server.
     *
     * @param address   -   The address of the publisher.
     * @param port      -   The port of the publisher.
     * @param id        -   The id for the publishing process.
     * @return          -   A string object containing the http status code.
     */
    @RequestMapping(value = "/v0/delete", method = RequestMethod.GET)
    public String delete(@RequestParam(value = "address") String address, @RequestParam(value = "port") String port,
                         @RequestParam(value="id") String id) {
        return getJsonObjectFromHttpGetRequest(new HttpGet("http://" + address + ":" + port + "/v0/delete/" + id)).toJSONString();
    }

    private JSONObject getJsonObjectFromHttpGetRequest(HttpGet httpGet) {
        final CloseableHttpClient closeableHttpClient = HttpClients.createMinimal();
        final JSONObject jsonObject = new JSONObject();
        try(CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet)) {
            jsonObject.put("httpStatusCode", closeableHttpResponse.getStatusLine().getStatusCode());
            return jsonObject;
        }catch(Exception e) {
            jsonObject.put("httpStatusCode", HttpStatus.SC_NOT_FOUND);
            return jsonObject;
        }
    }
}
