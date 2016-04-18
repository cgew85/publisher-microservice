package io.github.cgew85.controller;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by cgew85 on 13.04.2016.
 */
public class PublisherRestControllerTest {

    CloseableHttpClient closeableHttpClient;

    @Before
    public void setUp() throws Exception {
        closeableHttpClient = HttpClients.createMinimal();
    }

    @After
    public void tearDown() throws Exception {
        closeableHttpClient.close();
    }

    @Test
    public void testPublisherAvailable() throws Exception {
        HttpGet httpGet = new HttpGet("http://localhost:5266/available");
        CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet);
        Assert.assertEquals(200, closeableHttpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testCreate() throws Exception {

    }

    @Test
    public void testInfo() throws Exception {

    }

    @Test
    public void testDownload() throws Exception {

    }

    @Test
    public void testDelete() throws Exception {

    }
}