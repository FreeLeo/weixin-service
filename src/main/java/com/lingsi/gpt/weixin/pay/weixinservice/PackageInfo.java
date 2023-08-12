package com.lingsi.gpt.weixin.pay.weixinservice;

import java.util.ArrayList;
import java.util.List;

public class PackageInfo {
    public String id;
    public String title;
    public int basic_chat_limit;
    public int advanced_chat_limit;
    public int price;
    public int expiration;

    public PackageInfo(String id, String title, int basic_chat_limit, int advanced_chat_limit, int price, int expiration) {
        this.id = id;
        this.title = title;
        this.basic_chat_limit = basic_chat_limit;
        this.advanced_chat_limit = advanced_chat_limit;
        this.price = price;
        this.expiration = expiration;
    }

    public static List<PackageInfo> getPackages() {
        List<PackageInfo> packages = new ArrayList<>();
        packages.add(new PackageInfo("1", "尝鲜套餐", 50, 0, 1, 30 * 24 * 60 * 60));
        packages.add(new PackageInfo("2", "无限制月套餐", -1, 0, 1, 30 * 24 * 60 * 60));
        packages.add(new PackageInfo("3", "无限制季套餐", -1, 0, 1, 90 * 24 * 60 * 60));
        packages.add(new PackageInfo("4", "无限制年套餐", -1, 0, 1, 365 * 24 * 60 * 60));
        return packages;
    }
}

