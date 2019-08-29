package com.imooc.web.req;

import java.util.List;

/**
 * @description:
 * @author: sunyingji
 * @create: 2019-08-29 17:47
 **/
public class MicroReq<T> {

    private boolean isSingleTrans = false;
    private String itemId;
    private List<BatchReq<T>> data;

    public MicroReq() {
    }

    public String getItemId() {
        return this.itemId;
    }

    public MicroReq<T> setItemId(String itemId) {
        this.itemId = itemId;
        return this;
    }

    public List<BatchReq<T>> getData() {
        return this.data;
    }

    public void setData(List<BatchReq<T>> data) {
        this.data = data;
    }

    public boolean isSingleTrans() {
        return this.isSingleTrans;
    }

    public void setSingleTrans(boolean singleTrans) {
        this.isSingleTrans = singleTrans;
    }
}
