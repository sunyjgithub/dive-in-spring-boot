package com.imooc.web.resp;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @description:
 * @author: sunyingji
 * @create: 2019-08-29 17:53
 **/
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchResp<T> extends BaseResp {

    private T data;

    public BatchResp() {
        this.setMessage(new ValidResult());
    }

    public BatchResp(String itemId) {
        this.setItemId(itemId);
        this.setMessage(new ValidResult());
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }
}