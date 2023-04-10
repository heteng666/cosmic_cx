package com.cxjt.xxd.component;

import java.util.concurrent.TimeUnit;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

/*
 * 高并发HttpClient
 */
public class HttpClientFactory {

	private static CloseableHttpClient httpClient ;
	
	public static CloseableHttpClient getHttpClient() throws InstantiationException, IllegalAccessException 
	{
		if(httpClient == null ) {
			synchronized (HttpClientFactory.class) 
			{
				if(httpClient == null) {
					httpClient = create();
				}
			}
		}
		return httpClient;	
	}
	
	private static CloseableHttpClient create() {
		ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {
		    @Override
		    public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
		        HeaderElementIterator it = new BasicHeaderElementIterator
		            (response.headerIterator(HTTP.CONN_KEEP_ALIVE));
		        while (it.hasNext()) {
		            HeaderElement he = it.nextElement();
		            String param = he.getName();
		            String value = he.getValue();
		            if (value != null && param.equalsIgnoreCase
		               ("timeout")) {
		                return Long.parseLong(value) * 1000;
		            }
		        }
		        return 60 * 1000;//如果没有约定，则默认定义时长为60s
		    }			
		};
		// 配置一个PoolingHttpClientConnectionManager
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(500);
		connectionManager.setDefaultMaxPerRoute(50);//例如默认每路由最高50并发，具体依据业务来定
		
		//设置一个线程隔一段时间关闭超时连接
		IdleConnectionMonitorThread thread  = new IdleConnectionMonitorThread(connectionManager);
		thread.start();
		CloseableHttpClient	httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setKeepAliveStrategy(myStrategy)
                .setDefaultRequestConfig(RequestConfig.custom().setStaleConnectionCheckEnabled(true).build())
                .build();
		HttpParams params = new BasicHttpParams();
		//设置连接超时时间
		Integer CONNECTION_TIMEOUT = 2 * 1000; //设置请求超时2秒钟 根据业务调整
		Integer SO_TIMEOUT = 2 * 1000; //设置等待数据超时时间2秒钟 根据业务调整

		//定义了当从ClientConnectionManager中检索ManagedClientConnection实例时使用的毫秒级的超时时间
		//这个参数期望得到一个java.lang.Long类型的值。如果这个参数没有被设置，默认等于CONNECTION_TIMEOUT，因此一定要设置。
		Long CONN_MANAGER_TIMEOUT = 500L; //在httpclient4.2.3中我记得它被改成了一个对象导致直接用long会报错，后来又改回来了
		 
		params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
		params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, SO_TIMEOUT);
		params.setLongParameter(ClientPNames.CONN_MANAGER_TIMEOUT, CONN_MANAGER_TIMEOUT);
		//在提交请求之前 测试连接是否可用
		params.setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, true);
		 
		//另外设置http client的重试次数，默认是3次；当前是禁用掉（如果项目量不到，这个默认即可）
//		 httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
		return httpClient;
	}
	
	public static class IdleConnectionMonitorThread extends Thread {	    
	    private final HttpClientConnectionManager connMgr;
	    private volatile boolean shutdown;
	    
	    public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
	        super();
	        this.connMgr = connMgr;
	    }
	    @Override
	    public void run() {
	        try {
	            while (!shutdown) {
	                synchronized (this) {
	                    wait(5000);
	                    // Close expired connections
	                    connMgr.closeExpiredConnections();
	                    // Optionally, close connections
	                    // that have been idle longer than 30 sec
	                    connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
	                }
	            }
	        } catch (InterruptedException ex) {
	            // terminate
	        }
	    }
	    
	    public void shutdown() {
	        shutdown = true;
	        synchronized (this) {
	            notifyAll();
	        }
	    }
	    
	}
}
