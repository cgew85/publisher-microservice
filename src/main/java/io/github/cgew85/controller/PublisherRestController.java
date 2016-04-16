package io.github.cgew85.controller;

import io.github.cgew85.domain.Info;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;

/**
 * Created by cgew85 on 13.04.2016.
 */
@RestController
public class PublisherRestController {

    /**
     * Checks the availability of a given publishing server.
     *
     * @param address -   The address of the publishing server.
     * @param port    -   The port of the publishing server.
     * @return        -   A string object containing the http status code.
     */
    @RequestMapping(value = "/v0/available", method = RequestMethod.GET)
    public String available(@RequestParam(value = "address") String address, @RequestParam(value = "port") String port) {
        return getJsonObjectFromHttpGetRequest(new HttpGet("http://" + address + ":" + port + "/available")).toJSONString();
    }

    /**
     * Starts a pdf creation process with the given arguments.
     *
     * @param address       -   The address of the publishing server.
     * @param port          -   The port of the publishing server.
     * @param dataXml       -   The given data xml (base64 encoded).
     * @param layoutXml     -   The given layout xml (base64 encoded).
     * @return              -   Returns the id of the stated process, or in case of an error -1.
     */
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
            httpPost.setEntity(stringEntity);
        } catch (Exception e) {
            jsonObject.put("id", -1);
            return jsonObject.toJSONString();
        }
        try (CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost)) {
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
            final StringBuilder stringBuilder = new StringBuilder();
            String temp;
            while ((temp = bufferedReader.readLine()) != null) {
                stringBuilder.append(temp);
            }
            jsonObject.put("id", stringBuilder.toString());
            return jsonObject.toJSONString();
        } catch (Exception e) {
            jsonObject.put("id", -1);
            return jsonObject.toJSONString();
        }
    }

    /**
     * Returns information about the process published beforehand.
     *
     * @param address   -   The address of the publishing server.
     * @param port      -   The port of the publishing server.
     * @param id        -   The id of the process to get information about.
     * @return          -   Json formatted information about the process.
     */
    @RequestMapping(value = "/v0/info", method = RequestMethod.GET)
    public Info info(@RequestParam(value = "address") String address, @RequestParam(value = "port") String port,
                     @RequestParam(value = "id") String id) {
        // Create info object for the worst case, only to be rewritten in case of success
        final Info info = new Info("-1", "", "", "", "");
        final CloseableHttpClient closeableHttpClient = HttpClients.createMinimal();
        final HttpGet httpGet = new HttpGet("http://" + address + ":" + port + "/v0/status/" + id);
        try (final CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet)) {
            if (closeableHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
                final StringBuilder stringBuilder = new StringBuilder();
                String temp;
                while ((temp = bufferedReader.readLine()) != null) {
                    stringBuilder.append(temp);
                }
                final JSONParser jsonParser = new JSONParser();
                final JSONObject jsonObject = (JSONObject) jsonParser.parse(stringBuilder.toString());
                info.setProcessId(id);
                info.setErrorStatus((String) jsonObject.get("errorstatus"));
                info.setResult((String) jsonObject.get("result"));
                info.setMessage((String) jsonObject.get("message"));
                info.setFinished((String) jsonObject.get("finished"));

                return info;
            } else {
                return info;
            }
        } catch (Exception e) {
            return info;
        }
    }

    /**
     * Returns the pdf in a byte array.
     *
     * @param address   -   The address of the publishing server.
     * @param port      -   The port of the publishing server.
     * @param id        -   The id relating to the pdf that is to be downloaded.
     * @return          -   A byte array containing the pdf file.
     */
    @RequestMapping(value = "/v0/download", method = RequestMethod.GET, produces = "application/pdf")
    public ResponseEntity<byte[]> download(@RequestParam(value = "address") String address, @RequestParam(value = "port") String port,
                                           @RequestParam(value = "id") String id) {
        final CloseableHttpClient closeableHttpClient = HttpClients.createMinimal();
        final HttpGet httpGet = new HttpGet("http://" + address + ":" + port + "/v0/pdf/" + id);


        try (final CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet)) {

            // Setup no caching headers
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Cache-Control", "no-cache, no-store, must-revalidate");
            httpHeaders.add("Pragma", "no-cache");
            httpHeaders.add("Expires", "0");
            httpHeaders.setContentType(MediaType.parseMediaType("application/pdf"));

            if (closeableHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                int len;
                byte[] buffer = new byte[4096];
                while ((len = closeableHttpResponse.getEntity().getContent().read(buffer, 0, buffer.length)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, len);
                }
                return ResponseEntity
                        .ok()
                        .headers(httpHeaders)
                        .contentLength(closeableHttpResponse.getEntity().getContentLength())
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(byteArrayOutputStream.toByteArray());
            } else {
                return new ResponseEntity<>(org.springframework.http.HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(org.springframework.http.HttpStatus.I_AM_A_TEAPOT);
        }
    }

    /**
     * Deletes a certain folder for a given id on a given publishing server.
     *
     * @param address -   The address of the publisher.
     * @param port    -   The port of the publisher.
     * @param id      -   The id for the publishing process.
     * @return        -   A string object containing the http status code.
     */
    @RequestMapping(value = "/v0/delete", method = RequestMethod.GET)
    public String delete(@RequestParam(value = "address") String address, @RequestParam(value = "port") String port,
                         @RequestParam(value = "id") String id) {
        return getJsonObjectFromHttpGetRequest(new HttpGet("http://" + address + ":" + port + "/v0/delete/" + id)).toJSONString();
    }

    private JSONObject getJsonObjectFromHttpGetRequest(HttpGet httpGet) {
        final CloseableHttpClient closeableHttpClient = HttpClients.createMinimal();
        final JSONObject jsonObject = new JSONObject();
        try (CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet)) {
            jsonObject.put("httpStatusCode", closeableHttpResponse.getStatusLine().getStatusCode());
            return jsonObject;
        } catch (Exception e) {
            jsonObject.put("httpStatusCode", HttpStatus.SC_NOT_FOUND);
            return jsonObject;
        }
    }
}
