package org.myframe.https.certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;


/**
 * @date 2015/12/28  14:47
 * 主机名验证的基接口
 */
public class HttpsHostnameVerifier implements HostnameVerifier {
    /**
     * 
    * @Description 返回为true即可
    * @see javax.net.ssl.HostnameVerifier#verify(java.lang.String, javax.net.ssl.SSLSession) 
    * @date 2016年1月4日 下午5:13:50
     */
    public boolean verify(String hostName, SSLSession session) {
        return true;
    }
}
