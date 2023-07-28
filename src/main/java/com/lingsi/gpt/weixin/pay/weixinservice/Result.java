package com.lingsi.gpt.weixin.pay.weixinservice;

import java.io.Serializable;
import java.util.Date;
 
/**
 * 全局返回Result
 * @author ouyangjun
 */
public class Result<T> implements Serializable {
	
    private static final long serialVersionUID = 1L;
 
    private int code;
    private String msg;
    private T data;
    private Date time;
	
    public Result() {}
    public Result(CodeState codeenum, T data) {
        this.code = codeenum.getCode();
        this.msg = codeenum.getMsg();
        this.data = data;
        this.time = new Date();
    }
	
    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.time = new Date();
    }
	
    public int getCode() {return code;}
    public void setCode(int code) {this.code = code;}
	
    public String getMsg() {return msg;}
    public void setMsg(String msg) {this.msg = msg;}
	
    public T getData() {return data;}
    public void setData(T data) {this.data = data;}
	
    public Date getTime() {return time;}
    public void setTime(Date time) {this.time = time;}
}