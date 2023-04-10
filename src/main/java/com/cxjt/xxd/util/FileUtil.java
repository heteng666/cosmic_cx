package com.cxjt.xxd.util;

import kd.bos.cache.CacheFactory;
import kd.bos.cache.tempfile.TempFileCacheDownloadable;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class FileUtil {
    public static byte[] download(String fileUrl) {
        TempFileCacheDownloadable downLoad = (TempFileCacheDownloadable) CacheFactory.getCommonCacheFactory().getTempFileCache();
        int temp = 0;
        int len = 0;
        byte[] bytes = null;
        InputStream is = null;
        try {
            String[] queryParams = new URL(fileUrl).getQuery().split("&");
            Map<String, String> downloadFileParams = new HashMap<String, String>();
            for (String queryParam : queryParams) {
                String[] param = queryParam.split("=");
                downloadFileParams.put(param[0], param[1]);
            }
            TempFileCacheDownloadable.Content content = downLoad.get(downloadFileParams.get("configKey"), downloadFileParams.get("id"));
            is = content.getInputStream();
            bytes = new byte[(int) 1024 * 1024 * 5];
            while ((temp = is.read()) != -1) {
                bytes[len] = (byte) temp;
                len++;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return bytes;
    }

    public static String byte2Base64(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String getSuffixName(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }

    /**
     * @param fileName 带扩展名的文件名
     * @return 不带扩展名的文件名
     */
    public static String getNameWithOutExt(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return null;
        }
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    public static byte[] toByteArray(InputStream inputStream) {
        try {
            byte[] fileBytes = IOUtils.toByteArray(inputStream);
            return fileBytes;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据url下载文件流
     *
     * @param urlStr
     * @return
     */
    public static InputStream getInputStreamFromUrl(String urlStr) {
        InputStream inputStream = null;
        try {
            //url解码
            URL url = new URL(java.net.URLDecoder.decode(urlStr, "UTF-8"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //设置超时间为3秒
            conn.setConnectTimeout(3 * 1000);
            //防止屏蔽程序抓取而返回403错误
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            //得到输入流
            inputStream = conn.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return inputStream;
    }

    /**
     * @param attachmentFileUrl 苍穹文件服务器-文件地址
     * @return
     */
    public static String byte2Base64(String attachmentFileUrl) {
        InputStream inputStream = getInputStreamFromUrl(attachmentFileUrl);
        byte[] fileBytes = toByteArray(inputStream);
        //String content = new String(fileBytes);

        String base64Str = FileUtil.byte2Base64(fileBytes);

        return base64Str;
    }

}
