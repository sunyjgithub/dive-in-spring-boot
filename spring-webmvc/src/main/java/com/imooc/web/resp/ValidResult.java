package com.imooc.web.resp;

import java.util.List;

/**
 * @description:
 * @author: sunyingji
 * @create: 2019-08-29 17:42
 **/
public class ValidResult {


    private String description;

    private List<CheckError> checkErrors;

    public ValidResult() {
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ValidResult.CheckError> getCheckErrors() {
        return this.checkErrors;
    }

    public void setCheckErrors(List<ValidResult.CheckError> checkErrors) {
        this.checkErrors = checkErrors;
    }

    public String toString() {
        return "Message [" + (this.description != null ? "description=" + this.description + ", " : "") + (this.checkErrors != null ? "checkErrors=" + this.checkErrors : "") + "]";
    }


    public static class CheckError {

        private String errorColum;

        private String errorDetail;

        public CheckError() {
        }

        public CheckError(String errorColum, String errorDetail) {
            this.errorColum = errorColum;
            this.errorDetail = errorDetail;
        }

        public String getErrorColum() {
            return this.errorColum;
        }

        public void setErrorColum(String errorColum) {
            this.errorColum = errorColum;
        }

        public String getErrorDetail() {
            return this.errorDetail;
        }

        public void setErrorDetail(String errorDetail) {
            this.errorDetail = errorDetail;
        }

        public String toString() {
            return "CheckError [" + (this.errorColum != null ? "errorColum=" + this.errorColum + ", " : "") + (this.errorDetail != null ? "errorDetail=" + this.errorDetail : "") + "]";
        }
    }
}
