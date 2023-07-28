package com.lingsi.gpt.weixin.examples;

public class UserRequest {
    private String name;
    private int age;

    // 构造函数
    public UserRequest() {
    }

    // 构造函数
    public UserRequest(String name, int age) {
        this.name = name;
        this.age = age;
    }

    // Getter和Setter方法
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}

