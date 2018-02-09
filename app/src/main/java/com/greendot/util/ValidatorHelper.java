package com.greendot.util;

import android.net.Uri;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidatorHelper {

    private static String phonePattern = "^(13[0-9]|147|15[0-9]|17[0-9]|18[0-9])\\d{8}$";

    private static String passwordPattern = "^[A-Za-z0-9_]{6,30}$";

    private static String codePattern = "^[0-9_]{4}$";

    private static String inviteCodePattern = "^[A-Za-z0-9_]{4}$";

    public static boolean PhoneValidator(String phone){
        return getValidatorResult(phonePattern, phone);
    }

    public static boolean PasswordValidator(String password){
        return getValidatorResult(passwordPattern, password);
    }

    public static boolean CodeValidator(String code){
        return getValidatorResult(codePattern, code);
    }

    public static boolean InviteCodeValidator(String code){
        return getValidatorResult(inviteCodePattern, code);
    }

    public static boolean ProxyUrlValidator(String url){
        try {
            if (url == null || url.isEmpty())
                return false;

            if (url.startsWith("ss://")) {
                return true;
            } else {
                Uri uri = Uri.parse(url);
                if (!"http".equals(uri.getScheme()) && !"https".equals(uri.getScheme()))
                    return false;
                if (uri.getHost() == null)
                    return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean getValidatorResult(String phonePattern, String str){
        Pattern pattern = Pattern.compile(phonePattern);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }
}
