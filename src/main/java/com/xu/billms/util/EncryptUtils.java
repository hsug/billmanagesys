package com.xu.billms.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 对密码加密
 *
 * @author xu
 * @date 24/9/2019 下午4:34
 */
public class EncryptUtils {
    private static final String SALT = "qazxcdew~`+qwe0";

    /**
     * 对密码进行加密,返回加密后的密码
     *
     * @param code 输入的密码
     * @return 加密后一串字符
     */
    public static String getPwd(String code) {
        // 使用MD5 hash算法
        try {
            MessageDigest MD5 = MessageDigest.getInstance("MD5");
            byte[] digest1 = MD5.digest((code + SALT).getBytes());
            // 把加密后的字节转换成字符串
            String sha1 = Base64.getEncoder().encodeToString(digest1);

            MessageDigest SHA = MessageDigest.getInstance("SHA-256");
            byte[] digest2 = SHA.digest((sha1).getBytes());
            System.out.println(Base64.getEncoder().encodeToString(digest2));
            return Base64.getEncoder().encodeToString(digest2);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
