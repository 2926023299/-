package com.tanhua.server.interceptor;

import com.tanhua.model.domain.User;

/**
 * 工具类：实现向ThreadLocal中存储数据的方法
 */
public class UserHolder {
    private static ThreadLocal<User> t1 = new ThreadLocal<User>();

    //将用户对象，存入ThreadLocal中
    public static void set(User user) {
        t1.set(user);
    }

    public static User get() {
        return t1.get();
    }

    //从当前线程中获取用户对象的id
    public static Long getUserId() {
        return t1.get().getId();
    }

    //获取手机号码
    public static String getUserPhone() {
        return t1.get().getMobile();
    }

    public static void remove() {
        t1.remove();
    }
}
