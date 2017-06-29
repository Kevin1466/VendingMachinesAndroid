package org.myframe.https.certificate;

import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * @Description 509协议证书
 * @date 2015/12/28  14:47
 */
public class HttpsX509TrustManager implements X509TrustManager {
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) {

    }

    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}
