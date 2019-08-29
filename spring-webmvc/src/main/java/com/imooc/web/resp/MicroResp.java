package com.imooc.web.resp;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * @description:
 * @author: sunyingji
 * @create: 2019-08-29 17:52
 **/
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MicroResp<T> extends BaseResp {
    private List<BatchResp<T>> data;

    public MicroResp() {
        this.setMessage(new ValidResult());
    }

    public MicroResp(String itemId) {
        this.setItemId(itemId);
        this.setMessage(new ValidResult());
    }

    public List<BatchResp<T>> getData() {
        return this.data;
    }

    public void setData(List<BatchResp<T>> data) {
        this.data = data;
    }
}
