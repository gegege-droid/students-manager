package com.grade.common;

/**
 * 业务异常：携带《概要设计说明书》§6.5 定义的业务码。
 */
public class ServiceException extends RuntimeException {

    private final String businessCode;

    public ServiceException(String businessCode, String message) {
        super(message);
        this.businessCode = businessCode;
    }

    public String getBusinessCode() {
        return businessCode;
    }
}
