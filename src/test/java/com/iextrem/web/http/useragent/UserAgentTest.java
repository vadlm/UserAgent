package com.iextrem.web.http.useragent;

import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * Created by vadim on 01.04.14.
 */
public class UserAgentTest {
    public static final String URL = "http://www.htmlcodetutorial.com/cgi-bin/mycgi.pl";
    public static final String[] PARAMETERS = {"test_key","test_value"};
    public static final String TEST_STRING_PARAMETERS_REQUEST = "test_value";
    public static final String TEST_STRING_EMPTY_REQUEST = "CGI";

    @Test
    public void testDoGetWithoutParameters() throws Exception {
        UserAgent userAgent = new UserAgent();
        HttpResponse httpResponse = userAgent.doGet(URL);
        assertTrue(httpResponse.getAsText().contains(TEST_STRING_EMPTY_REQUEST));
    }

    @Test
    public void testDoGetWithParameters() throws Exception {
        UserAgent userAgent = new UserAgent();
        HttpResponse httpResponse = userAgent.doGet(URL, PARAMETERS);
        assertTrue(httpResponse.getAsText().contains(TEST_STRING_PARAMETERS_REQUEST));
    }

    @Test
    public void testDoPostWithoutParameters() throws Exception {
        UserAgent userAgent = new UserAgent();
        HttpResponse httpResponse = userAgent.doPost(URL);
        assertTrue(httpResponse.getAsText().contains(TEST_STRING_EMPTY_REQUEST));
    }

    @Test
    public void testDoPostWithParameters() throws Exception {
        UserAgent userAgent = new UserAgent();
        HttpResponse httpResponse = userAgent.doPost(URL, PARAMETERS);
        assertTrue(httpResponse.getAsText().contains(TEST_STRING_PARAMETERS_REQUEST));


    }
}
