package com.cxjt.xxd.util;

import com.alibaba.fastjson.JSONObject;
import com.cxjt.xxd.constants.FormConstant;
import kd.bos.cache.CacheFactory;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.TypesContainer;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.MainEntityType;
import kd.bos.entity.datamodel.IAttachmentModel;
import kd.bos.fileservice.FileServiceFactory;
import kd.bos.fileservice.extension.FileServiceExtFactory;
import kd.bos.form.control.AttachmentPanel;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.metadata.dao.MetaCategory;
import kd.bos.metadata.dao.MetadataDao;
import kd.bos.metadata.form.FormMetadata;
import kd.bos.servicehelper.AttachmentServiceHelper;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.attachment.AttachmentFieldServiceHelper;
import kd.bos.servicehelper.operation.DeleteServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.session.EncreptSessionUtils;
import kd.bos.url.UrlService;
import kd.bos.util.StringUtils;
import sun.misc.BASE64Decoder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class XXDUtils {
    private final static Log logger = LogFactory.getLog(XXDUtils.class);

    /**
     * 根据url上传附件至服务器，并返回附件id
     * @param url 文件url
     * @param name 文件名称,文件扩展名必录
     * @param type  文件类型
     * 附件字段赋值方式：
     * DynamicObjectCollection ukwoAttachmentfield = (DynamicObjectCollection) this.getModel().getValue("ukwo_attachmentfield");
     * DynamicObject dynamicObject = ukwoAttachmentfield.addNew();
     * dynamicObject.set("fbasedataid_id", longs.get(0));*/
    public static Long buildAttachmentDataFromEdit(String url,String name,String type) {
        long currUserId = RequestContext.get().getCurrUserId();
        InputStream inputStreamFromUrl = XXDUtils.getInputStreamFromUrl(url);
        int size = 10;
        try {
            size = inputStreamFromUrl.available();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        String saveUrl = CacheFactory.getCommonCacheFactory().getTempFileCache().saveAsUrl(name, new BufferedInputStream(inputStreamFromUrl), 2*3600);
        String pathCode = AttachmentFieldServiceHelper.saveTempToFileService(saveUrl, "", name);
//        String pathCode = AttachmentServiceHelper.saveTempToFileService(saveUrl, "ukwo_demo_ht", "ukwo_purreq_ht", name, "360777.pdf");
        //pathCode = UrlService.getAttachmentFullUrl(pathCode);
        List<Map<String, Object>> attachDataList = new ArrayList<>();
        Map<String, Object> attachMap = new HashMap<>();
        attachMap.put("description", "");
        attachMap.put("type", type);
        //url
        attachMap.put("url", pathCode);
        //uid
        attachMap.put("uid", XXDUtils.getUid().toString());
        //name
        attachMap.put("name", name);
        //size
        attachMap.put("size", size);
        attachMap.put("fattachmentpanel", "attachmentpanel");
        //lastModified
        attachMap.put("lastModified", new Date().getTime());
        attachMap.put("status", "success");
        //client
        attachMap.put("client", null);
        attachMap.put("filesource", 1);
        attachDataList.add(attachMap);
        List<DynamicObject> dynamicObjects = AttachmentFieldServiceHelper.saveAttachments("", "", attachDataList);
        return dynamicObjects.get(0).getLong("id");

//        DynamicObject bdAttachment = BusinessDataServiceHelper.newDynamicObject("bd_attachment");
//        bdAttachment.set("number",saveUrl.substring(saveUrl.length()-36,saveUrl.length()-1));
//        bdAttachment.set("name",name);
//        bdAttachment.set("size",size);
//        bdAttachment.set("uid",XXDUtils.getUid().toString());
//        bdAttachment.set("url",pathCode);
//        bdAttachment.set("type",type);
//        bdAttachment.set("creator",currUserId);
//        bdAttachment.set("createtime",new Date());
//        bdAttachment.set("modifytime",new Date());
//        bdAttachment.set("status","B");
//        bdAttachment.set("filesource",1);
//        SaveServiceHelper.save(new DynamicObject[]{bdAttachment});
//        return bdAttachment.getLong("id");
    }

    /**
     * 根据url上传附件至服务器并绑定单据附件面板
     * @param url 文件url
     * @param name 文件名称,文件扩展名必录
     * @param type  文件类型
     * @param billId 单据id
     *
    */
    public static void buildAttachmentDataFromPanel(String url,String name,String type,Object billId) {
//        List<Map<String, Object>> attachmentData3 = XXDUtils.buildAttachmentData("ukwo_purreq_ht", url, name, type, billId);
        List<Map<String, Object>> attachmentData3 = XXDUtils.buildAttachmentData(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME, url, name, type, billId);
        //根据附件字段数据构造附件面板数据
        Map<String, Object> attachemnts = new HashMap<>();
        attachemnts.put("attachmentpanel", attachmentData3);
//        AttachmentServiceHelper.saveTempAttachments("ukwo_purreq_ht", billId, "ukwo_demo_ht", attachemnts);
        AttachmentServiceHelper.saveTempAttachments(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME, billId, FormConstant.APP_ID, attachemnts);
    }

    /**
     *
     * @param
     * @return
     */
    private static List<Map<String, Object>> buildAttachmentData(String entityNum,String url,String name,String type,Object billId) {
        List<Map<String, Object>> attachDataList = new ArrayList<>();
        Map<String, Object> attachMap = new HashMap<>();
        //description
        attachMap.put("description", "");
        attachMap.put("type", type);

        InputStream inputStreamFromUrl = XXDUtils.getInputStreamFromUrl(url);
        int size = 10;
        try {
            size = inputStreamFromUrl.available();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        String saveUrl = CacheFactory.getCommonCacheFactory().getTempFileCache().saveAsFullUrl(name, new BufferedInputStream(inputStreamFromUrl), 2*3600);
        //url
        attachMap.put("url", saveUrl);
        //uid
        attachMap.put("uid", XXDUtils.getUid().toString());
        //name
        attachMap.put("name", name);
        //size
        attachMap.put("size", size);
        attachMap.put("fattachmentpanel", "attachmentpanel");
        //entityNum
        attachMap.put("entityNum", entityNum);
        attachMap.put("billPkId", billId);
        //lastModified
        attachMap.put("lastModified", new Date().getTime());
        attachMap.put("status", "success");
        //client
        attachMap.put("client", null);
        attachDataList.add(attachMap);
        return attachDataList;
    }

    /**
     * 根据url下载文件流
     * @param urlStr
     * @return
     */
    public static InputStream getInputStreamFromUrl(String urlStr) {
        InputStream inputStream=null;
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
     * 根据附件字段数据构造附件面板数据
     * @param sourceAttachCol
     * @return
     */
    public static List<Map<String, Object>> buildAttachmentDataFromEdit(JSONObject sourceAttachCol, String entityId, String fid) {
        List<Map<String, Object>> attachDataList = new ArrayList<>();
        JSONObject attachObj = sourceAttachCol.getJSONObject("fbasedataid");
        Map<String, Object> attachMap = new HashMap<>();
        //description
        attachMap.put("description", attachObj.getJSONObject("description").getString("zh_CN"));
        attachMap.put("type", attachObj.getString("type"));

        //获取附件inputstream上传到缓存服务
        String url = attachObj.getString("url");
        InputStream inputStream = XXDUtils.getInputStreamFromUrl(url);
//        InputStream inputStream = FileServiceFactory.getAttachmentFileService().getInputStream(url);
        String saveUrl = CacheFactory.getCommonCacheFactory().getTempFileCache().saveAsFullUrl(attachObj.getString("name"), new BufferedInputStream(inputStream), 2*3600);
        //url
        attachMap.put("url", saveUrl);
//        IAttachmentModel panel= (IAttachmentModel) TypesContainer.createInstance("kd.bos.mvc.list.AttachmentModel");
//        saveUrl = saveUrl.substring(saveUrl.indexOf("tempfile"),saveUrl.length());
//        String previewUrl = panel.getTempFilePreviewUrl(saveUrl);
        attachMap.put("previewurl", attachObj.getString("previewurl"));
        //uid
        attachMap.put("uid", getUid());
        //name
        attachMap.put("name", attachObj.getJSONObject("name").getString("zh_CN"));
        //size
        attachMap.put("size", attachObj.get("size"));
        attachMap.put("fattachmentpanel", "attachmentpanel");
        //entityNum
        attachMap.put("entityNum", entityId);
        attachMap.put("billPkId", fid);
        //lastModified
        attachMap.put("lastModified", new Date().getTime());
        attachMap.put("status", "success");
        //client
        attachMap.put("client", null);
        attachDataList.add(attachMap);
        return attachDataList;
    }
    /**
     * 获取附件id集合*/
    public static List<Long> getAttchIdSet(DynamicObjectCollection sourceAttachCol) {
        List<Long> attchIdSet = new ArrayList<>();
        sourceAttachCol.forEach(attach -> {
            attchIdSet.add(attach.getDynamicObject("fbasedataId").getLong("id"));
        });
        return attchIdSet;
    }

    /**
     * 获取附件uid
     * @return
     */
    public static StringBuffer getUid() {
        StringBuffer uid = new StringBuffer("rc-upload-");
        uid.append((new Date()).getTime());
        uid.append("-");
        int index = (int)(1.0D + Math.random() * 10.0D);
        uid.append(index);
        return uid;
    }

    /**
     * 时间格式化
     * @return
     */
    public static String getFormatDate(Date date) {
        if(date==null){
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        stringBuffer.append(instance.get(Calendar.YEAR));
        stringBuffer.append("年");
        stringBuffer.append(instance.get(Calendar.MONTH)+1);
        stringBuffer.append("月");
        stringBuffer.append(instance.get(Calendar.DATE));
        stringBuffer.append("日");
        stringBuffer.append(instance.get(Calendar.HOUR_OF_DAY));
        stringBuffer.append(":");
        stringBuffer.append(instance.get(Calendar.MINUTE));
        stringBuffer.append(":");
        stringBuffer.append(instance.get(Calendar.SECOND));
        return stringBuffer.toString();
    }

    /**
     * 数字字符串格式化
     * @param str 需格式的字符串
     * @param point 保留小数位数
     * @return
     */
    public static String getFormatString(String str,int point) {
        if(StringUtils.isNotEmpty(str)){
            int i = str.lastIndexOf(".");
            if(i+point+1>str.length() || i < 0){
                return str;
            }
            return str.substring(0,i+point+1);
        }
        return str;
    }
    /**
     * 流程节点显示名称转换
     * @param str 原名称
     * @return 转换后名称
     */
    public static String transferNodeName(String str) {
        if(StringUtils.isNotEmpty(str)){

        }
        return str;
    }


    /**
     * 获取远程文件
     *
     * @param inputStreamFromUrl 文件输入流
     * @param localFilePath  本地文件路径
     */
    public static void downloadFile(InputStream inputStreamFromUrl, String localFilePath) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        File f = new File(localFilePath);
        try {
            bis = new BufferedInputStream(inputStreamFromUrl);
            bos = new BufferedOutputStream(new FileOutputStream(f));
            int len = 2048;
            byte[] b = new byte[len];
            while ((len = bis.read(b)) != -1) {
                bos.write(b, 0, len);
            }
            bos.flush();
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bis.close();
                bos.close();
            } catch (IOException e) {
               // e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 将inputstream转为Base64
     *
     * @param is
     * @return
     * @throws Exception
     */
    public static String inputStream2Base64(InputStream is) throws Exception {
        byte[] data = null;
        try {
            ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
            byte[] buff = new byte[100];
            int rc = 0;
            while ((rc = is.read(buff, 0, 100)) > 0) {
                swapStream.write(buff, 0, rc);
            }
            data = swapStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    throw new Exception("输入流关闭异常");
                }
            }
        }

        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * base64转inputStream
     *
     * @param base64string
     * @return
     */
    public static InputStream base2InputStream(String base64string) {
        ByteArrayInputStream stream = null;
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            byte[] bytes1 = decoder.decodeBuffer(base64string);
            stream = new ByteArrayInputStream(bytes1);
        } catch (Exception e) {
            //e.printStackTrace();
            throw new RuntimeException(e);
        }
        return stream;
    }


    /**
    * 获取单据名称
    * @param key 单据标识
    * */
    public static String getEntityNameKey(String key){
        //根据表单编码获取表单id
        String id = MetadataDao.getIdByNumber(key, MetaCategory.Form);
        //获取表单元数据
        FormMetadata formMeta = (FormMetadata) MetadataDao.readRuntimeMeta(id, MetaCategory.Form);
        return formMeta.getName().getLocaleValue();
    }

    public static String getNewUrl(String url) {
//        if (!url.contains(".do")) {
//            url = FileServiceExtFactory.getAttachFileServiceExt().getRealPath(url);
//            url = UrlService.getAttachmentFullUrl(url);
//        }
        if (!url.contains(".do")) {
            url = UrlService.getAttachmentFullUrl(url);
        }

        url = url.contains("&kdedcba") ? url :
                EncreptSessionUtils.encryptSession(url);

        return url;
    }

    public static void deleteAtts(Object[] ids){
        MainEntityType entityType = EntityMetadataCache.getDataEntityType("bd_attachment");
        DeleteServiceHelper.delete(entityType, ids);
    }
}
