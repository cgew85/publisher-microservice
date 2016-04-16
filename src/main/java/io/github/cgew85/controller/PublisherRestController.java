package io.github.cgew85.controller;

import elemental.json.JsonString;
import io.github.cgew85.domain.Info;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by cgew85 on 13.04.2016.
 */
@RestController
public class PublisherRestController {

    @RequestMapping("/v0/available")
    public String available(@RequestParam(value = "address") String address, @RequestParam(value = "port") String port) {
        return getJsonObjectFromHttpGetRequest(new HttpGet("http://" + address + ":" + port + "/available")).toJSONString();
    }

    @RequestMapping("/v0/create")
    public String create(@RequestParam(value = "address") String address, @RequestParam(value = "port") String port,
                         @RequestParam(value = "dataXml") JsonString dataXml, @RequestParam(value = "layoutXml") JsonString layoutXml) {
        final CloseableHttpClient closeableHttpClient = HttpClients.createMinimal();
        final HttpPost httpPost = new HttpPost("http://" + address + ":" + port + "/v0/publish");
        httpPost.addHeader("data.xml", dataXml.getString());
        httpPost.addHeader("layout.xml", layoutXml.getString());
        final JSONObject jsonObject = new JSONObject();
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

    @RequestMapping("/v0/info")
    public Info info(@RequestParam(value="id") String id) {
        return new Info("processId", "errorStatus", "result", "message", "finished");
    }

    @RequestMapping("/v0/download")
    public String download(@RequestParam(value="id") String id) {
        return "not available right now";
    }

    @RequestMapping("/v0/delete")
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
