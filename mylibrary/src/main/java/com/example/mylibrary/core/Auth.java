package com.example.mylibrary.core;

/**
 * 认证
 */
public class Auth {
    private static final Auth auth = new Auth();

    private Auth() {

    }

    public static Auth getInstance() {
        return auth;
    }

    private boolean isAuth = false;

    public boolean isAuth() {
        return isAuth;
    }

    public void setAuth(boolean auth) {
        this.isAuth = auth;
    }
}
