package com.github.daweizhou89.modulea;

/**
 * Created by zhoudawei on 2018/12/20.
 */
public class ModelA {

    public int a;

    public String b;

    public ModelA(int a, String b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public String toString() {
        return "ModelA{" +
                "a=" + a +
                ", b='" + b + '\'' +
                '}';
    }
}
