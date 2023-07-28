package com.lingsi.gpt.weixin.pay.weixinservice;

/**
 * 全局统一返回code与msg
 * @author ouyangjun
 */
public enum CodeState {
 
    RESULT_CODE_SUCCESS(0, "SUCCESS"),
    RESULT_CODE_FAIL(400, "FAIL"),
    ;
	
    private int code;
    private String msg;
    CodeState(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
	
    public int getCode() {return code;}
    public void setCode(int code) {this.code = code;}
	
    public String getMsg() {return msg;}
    public void setMsg(String msg) {this.msg = msg;}
}