package com.example.tmdt_bookingmakeup_app.security;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordEncryption {
    public static String hashPassword(String password) {
        // Tạo salt và mã hóa mật khẩu
        String salt = BCrypt.gensalt(12); //cost factor
        return BCrypt.hashpw(password, salt);
    }

    //kiểm tra pass có hợp lệ không
    public static boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
}
