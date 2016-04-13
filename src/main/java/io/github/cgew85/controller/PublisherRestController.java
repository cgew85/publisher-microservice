package io.github.cgew85.controller;

import io.github.cgew85.domain.Info;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by cgew85 on 13.04.2016.
 */
@RestController
public class PublisherRestController {

    @RequestMapping("/v0/available")
    public String available(@RequestParam(value="address") String address, @RequestParam(value="port") String port) {
        CloseableHttpClient closeableHttpClient = HttpClients.createMinimal();
        HttpGet httpGet = new HttpGet("http://" + address + ":" + port + "/available");
        JSONObject jsonObject = new JSONObject();
        try(CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet)) {
            jsonObject.put("httpStatusCode", closeableHttpResponse.getStatusLine().getStatusCode());
            return jsonObject.toJSONString();
        }catch(Exception e) {
            jsonObject.put("httpStatusCode", HttpStatus.SC_NOT_FOUND);
            return jsonObject.toJSONString();
        }
    }

    @RequestMapping("/v0/create")
    public String create() {
        return "not available right now";
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
    public String delete(@RequestParam(value="id") String id) {
        return "not available right now";
    }
}
