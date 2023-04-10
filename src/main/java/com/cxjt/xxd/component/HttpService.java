package com.cxjt.xxd.component;

import java.io.IOException;
import java.net.URI;

import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONObject;

public class HttpService {

    private Log logger = LogFactory.getLog(this.getClass());

    private static final HttpService service = new HttpService();

    private HttpService() {
    }

    // 静态工厂方法
    public static HttpService getService() {
        return service;
    }

    /**
     * POST方式发送请求
     *
     * @param url
     * @param data
     * @return
     * @throws Exception
     */
    public String doPostByHttpClient(String url, String data) throws Exception {
        StringEntity se = new StringEntity(data, "UTF-8");
        se.setContentType("text/json");
        se.setContentEncoding(new BasicHeader("Content-Type", "application/json; charset=UTF-8"));
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");
        httpPost.setEntity(se);
        return doExecuteByHttpClient(httpPost, new ResponseHandler<String>() {
            @Override
            public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_CREATED) {
                    try {
                        throw new Exception("连接服务器发生错误！");
                    } catch (Exception e) {
                        logger.error("连接服务器发生错误！,异常信息如下:{}",e);
                        throw new RuntimeException(e);
                    }
                }
                return EntityUtils.toString(response.getEntity());
            }
        });
    }

    public <T> T doExecuteByHttpClient(HttpUriRequest httpPost, ResponseHandler<? extends T> responseHandler) throws Exception {
        try {
            CloseableHttpClient httpClient = HttpClientFactory.getHttpClient();
            T rtn = httpClient.execute(httpPost, responseHandler);
            return rtn;
        } catch (Exception e) {
            logger.error(" ===== doPostByHttpClient() ERROR =====信息如下:{} ", e);
            throw new Exception(e.getMessage());
        } finally {
            System.clearProperty("javax.net.debug");
        }
    }

    /**
     * GET方式发送请求
     *
     * @param url
     * @param param
     * @return
     * @throws Exception
     */
    public String doGetByHttpClient(String url, JSONObject param) throws Exception {
        URIBuilder builder = new URIBuilder(url);
        if (param != null) {
            builder.addParameter("body", param.toJSONString());
        }
        URI uri = builder.build();
        HttpGet httpGet = new HttpGet(uri);
        return doExecuteByHttpClient(httpGet, new ResponseHandler<String>() {
            @Override
            public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_CREATED) {
                    try {
                        throw new Exception("连接服务器发生错误！");
                    } catch (Exception e) {
                        logger.error(" ===== doGetByHttpClient() ERROR =====信息如下:{} ", e);
                        throw new RuntimeException(e);
                    }
                }
                return EntityUtils.toString(response.getEntity());
            }
        });
    }

}
