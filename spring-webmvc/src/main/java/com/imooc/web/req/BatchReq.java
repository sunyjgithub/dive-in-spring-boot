package com.imooc.web.req;

import java.util.List;

/**
 * @description:
 * @author: sunyingji
 * @create: 2019-08-29 17:48
 **/
public class BatchReq<T> {

    private String itemId;

    private T data;

    public BatchReq() {
    }

    public String getItemId() {
        return this.itemId;
    }

    public BatchReq<T> setItemId(String itemId) {
        this.itemId = itemId;
        return this;
    }

    public T getData() {
        return this.data;
    }

    public BatchReq<T> setData(T data) {
        this.data = data;
        return this;
    }

    public static <T> BatchReq<T> newBatchReq() {
        return new BatchReq();
    }

    public static <T> BatchReq<T> newBatchReq(T data) {
        BatchReq<T> batchReq = new BatchReq();
        batchReq.setData(data);
        return batchReq;
    }

    public static <T> List<BatchReq<T>> addToList(List<BatchReq<T>> list, BatchReq<T> batchReq) {
        list.add(batchReq);
        batchReq.setItemId(String.valueOf(list.size()));
        return list;
    }
}
