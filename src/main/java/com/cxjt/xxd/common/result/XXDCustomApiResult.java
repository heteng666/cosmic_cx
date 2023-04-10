package com.cxjt.xxd.common.result;

import kd.bos.openapi.common.result.CustomApiResult;

public class XXDCustomApiResult<T>  extends CustomApiResult {


    public static <T> CustomApiResult<T> success(String errorCode,String message,T data) {
        CustomApiResult<T> result = new CustomApiResult();
        result.setErrorCode(errorCode);
        result.setMessage(message);
        result.setStatus(true);
        result.setData(data);
        return result;
    }

}
