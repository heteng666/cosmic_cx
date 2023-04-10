package com.cxjt.xxd.helper;

public class PageHelper {

    //每页取20条
    public static final int DEFAULT_PAGE_SIZE = 20;

    public static int getPageCount(int dataCount, int pageSize) {
        int pageCount = dataCount % pageSize == 0 ? dataCount / pageSize : dataCount / pageSize + 1;
        return pageCount;
    }

    public static int getPageCount(int dataCount) {
        int pageCount = dataCount % DEFAULT_PAGE_SIZE == 0 ? dataCount / DEFAULT_PAGE_SIZE : dataCount / DEFAULT_PAGE_SIZE + 1;
        return pageCount;
    }
}
