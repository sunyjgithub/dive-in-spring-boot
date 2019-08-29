package com.imooc.web.resp;

/**
 * @description:
 * @author: sunyingji
 * @create: 2019-08-29 17:41
 **/
public abstract class BaseResp {


    private String itemId;

    private String resultCode;

    private ValidResult message = new ValidResult();

    public BaseResp() {
    }

    public String getItemId() {
        return this.itemId;
    }

    public BaseResp setItemId(String itemId) {
        this.itemId = itemId;
        return this;
    }

    public String getResultCode() {
        return this.resultCode;
    }

    public BaseResp setResultCode(String resultCode) {
        this.resultCode = resultCode;
        return this;
    }

    public ValidResult getMessage() {
        return this.message;
    }

    public BaseResp setMessage(ValidResult message) {
        this.message = message;
        return this;
    }
}
