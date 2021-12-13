package com.sshift.sqwatch

import org.slf4j.Logger

class URLFetcher {
    private Logger logger

    URLFetcher(Logger logger) {
        this.logger = logger
    }

    URLFetcher() {
    }

    Object getJsonFromURL(String url, String userpass) {
        def authorizationInfo = userpass.getBytes().encodeBase64().toString()
        logger?.info("Query " + url)
        def conn = url.toURL().openConnection()
        conn.setRequestProperty("Authorization", "Basic ${authorizationInfo}")
        conn.content.text
    }

    void postToURL(String url, String userpass, Map<String, String> params) {
        def authorizationInfo = userpass.getBytes().encodeBase64().toString()
        logger?.info("Post ${url} ${params}")
        def conn = url.toURL().openConnection()
        conn.setRequestProperty("Authorization", "Basic ${authorizationInfo}")
        conn.setDoOutput(true)
        def writer = new OutputStreamWriter(conn.getOutputStream())
        params.each { writer.write("${it.key}=${it.value}&") }
        writer.flush()
        String line
        def reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))
        while ((line = reader.readLine()) != null) {
            logger?.info(url + "-> " + line)
        }
        writer.close()
        reader.close()
    }
}
