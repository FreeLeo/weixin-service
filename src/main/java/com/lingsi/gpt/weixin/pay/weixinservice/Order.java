package com.lingsi.gpt.weixin.pay.weixinservice;

public class Order {
    private int amount;
    private String created_at;
    private String order_id;
    private String package_id;
    private String status;
    private String title;
    private String user_id;

    // 构造方法
    public Order(int amount, String created_at, String order_id, String package_id, String status, String title, String user_id) {
        this.amount = amount;
        this.created_at = created_at;
        this.order_id = order_id;
        this.package_id = package_id;
        this.status = status;
        this.title = title;
        this.user_id = user_id;
    }

    // Getter和Setter方法
    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getPackage_id() {
        return package_id;
    }

    public void setPackage_id(String package_id) {
        this.package_id = package_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    // toString方法（可选，用于调试和打印对象信息）
    @Override
    public String toString() {
        return "Order{" +
                "amount=" + amount +
                ", created_at=" + created_at +
                ", order_id='" + order_id + '\'' +
                ", package_id='" + package_id + '\'' +
                ", status='" + status + '\'' +
                ", title='" + title + '\'' +
                ", user_id='" + user_id + '\'' +
                '}';
    }
}
