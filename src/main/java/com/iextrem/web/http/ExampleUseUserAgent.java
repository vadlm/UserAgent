package com.iextrem.web.http;

import com.iextrem.web.http.useragent.HttpResponse;
import com.iextrem.web.http.useragent.UserAgent;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;

/**
 * Show example how to use UserAgent
 * Created by vadim on 06.02.14.
 */
public class ExampleUseUserAgent {
    private static final int BUFFER_SIZE = 4096;

    public static void main(String... args) {
        String url = "http://google.com/";
//        url = "http://usa.gov/";
        if (args.length > 0) {
            url = args[0];
        } else {
            url = "http://google.com";
//            url = "http://www.squid-cache.org/Images/img4.jpg";
//            url = "http://z-oleg.com/avz4.zip";
//            url = "http://bellib.net/";
//            url = "http://talk.by";
        }
        printPage(url);
    }

    private static void printPage(String url) {
        UserAgent userAgent = new UserAgent();
        userAgent.clearAllRequestProperties();
        userAgent.setRequestProperty("User-Agent", UserAgent.USER_AGENT_MOZILLA_LINUX);
        userAgent.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        userAgent.setRequestProperty("Accept-Language", "en-us,en;q=0.5");
        userAgent.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
        userAgent.setRequestProperty("Keep-Alive", "300");
        userAgent.setRequestProperty("Referer", "http://google.com");
        userAgent.setRequestProperty("Accept-Encoding", "gzip, deflate");
        userAgent.enableCookies();
        userAgent.enableRefererAutoset();
//        userAgent.setResponseSizeMax(5024);
//        userAgent.setConnectTimeout(50);
        userAgent.setAutoRedirect(true);
//        userAgent.setProxyAuthentication("192.168.1.1", 3128, "login", "password");
//        userAgent.setProxyAnonymous("192.168.1.1", 3128);
        userAgent.setProxyNone();

        HttpResponse httpResponse = userAgent.doGet(url);

        String responseText = "";
        try {
            responseText = httpResponse.getAsText();
            //            responseText = httpResponse.getAsText("windows-1251");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println(responseText);
//        writeResponseToFile("./tmp.txt", httpResponse);

        System.out.println("-------------------------------------------------------");
        System.out.println("url = " + httpResponse.getUrl());
        System.out.println("status = " + httpResponse.getStatus());
        System.out.println("code = " + httpResponse.getCode());
        System.out.println("message = " + httpResponse.getMessage());

        System.out.println("--------- Header --------------------------------------");
        System.out.println(httpResponse.headerToString());

        System.out.println("--------- Cookies -------------------------------------");
        CookieManager cookieManager = userAgent.getCookieManager();
        CookieStore cookieStore = cookieManager.getCookieStore();
        for (HttpCookie httpCookie : cookieStore.getCookies()) {
            printCookie(httpCookie);
        }
    }

    private static void writeResponseToFile(String file, HttpResponse httpResponse) {
        int buffer_size = httpResponse.getContentLength();
        byte[] buffer = new byte[buffer_size];
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file, false);
            httpResponse.readContent(buffer, 0, buffer_size);
            out.write(buffer, 0 ,buffer_size);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void printCookie(HttpCookie cookie) {
        System.out.print(cookie.getName() + "=" + cookie.getValue());
        System.out.print("; path=" + cookie.getPath() + "; domain=" + cookie.getDomain());
        System.out.print("; maxAge=" + cookie.getMaxAge());
        System.out.println("; discard=" + cookie.getDiscard());
    }
}

