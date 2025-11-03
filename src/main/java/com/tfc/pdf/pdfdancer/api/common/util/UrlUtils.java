package com.tfc.pdf.pdfdancer.api.common.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class UrlUtils {

    /**
     * Builds a new URL using the same protocol, host, and (if non-standard) port
     * from the given URL, but with a new path.
     *
     * @param originalUrl the original URL
     * @param newPath     the new path (should start with '/')
     * @return a string like protocol://host[:port_if_non_standard]/newPath
     * @throws URISyntaxException if URL components are invalid
     */
    public static URL buildUrlWithPath(URL originalUrl, String newPath) throws URISyntaxException, MalformedURLException {
        String protocol = originalUrl.getProtocol();
        String host = originalUrl.getHost();
        int port = originalUrl.getPort();

        // Only include the port if it’s non-standard for the protocol
        boolean includePort = (port != -1 &&
                !((protocol.equals("http") && port == 80) ||
                        (protocol.equals("https") && port == 443)));

        // Build URI manually
        URI newUri = new URI(
                protocol,
                null,
                host,
                includePort ? port : -1,
                newPath,
                null,
                null
        );

        return newUri.toURL();
    }

    public static URL getRootUrl(URL originalUrl) throws URISyntaxException, MalformedURLException {
        return buildUrlWithPath(originalUrl, "/");
    }

    public static URL getRootUrl(String originalUrl) throws URISyntaxException, MalformedURLException {
        return buildUrlWithPath(new URI(originalUrl).toURL(), "/");
    }
}