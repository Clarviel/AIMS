package worker.aims.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class PasswordUtil {

    // BCrypt 盐值轮数
    private static final int SALT_ROUNDS = 12;
    private final BCryptPasswordEncoder encoder;

    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*";
    private static final Random RANDOM = new SecureRandom();

    // 构造器注入 BCryptPasswordEncoder
    public PasswordUtil() {
        this.encoder = new BCryptPasswordEncoder(SALT_ROUNDS);
    }

    /**
         * 验证密码强度
         * 至少8位，包含大小写字母、数字
         */
    public ValidationResult validatePasswordStrength(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.length() < 8) {
            errors.add("密码至少需要8个字符");
        }
        if (!password.matches(".*[a-z].*")) {
            errors.add("密码必须包含至少一个小写字母");
        }
        if (!password.matches(".*[A-Z].*")) {
            errors.add("密码必须包含至少一个大写字母");
        }
        if (!password.matches(".*\\d.*")) {
            errors.add("密码必须包含至少一个数字");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * 哈希密码
     */
    public String hashPassword(String password) {
        return encoder.encode(password);
    }

    /**
     * 验证密码
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return encoder.matches(plainPassword, hashedPassword);
    }

    /**
     * 生成随机密码
     */
    public String generateRandomPassword(int length) {
        String allChars = LOWERCASE + UPPERCASE + NUMBERS + SYMBOLS;
        StringBuilder password = new StringBuilder();

        // 确保至少包含一个大写字母、小写字母、数字
        password.append(LOWERCASE.charAt(RANDOM.nextInt(LOWERCASE.length())));
        password.append(UPPERCASE.charAt(RANDOM.nextInt(UPPERCASE.length())));
        password.append(NUMBERS.charAt(RANDOM.nextInt(NUMBERS.length())));

        // 填充剩余长度
        for (int i = 3; i < length; i++) {
            password.append(allChars.charAt(RANDOM.nextInt(allChars.length())));
        }

        // 打乱顺序
        List<Character> chars = new ArrayList<>();
        for (char c : password.toString().toCharArray()) {
            chars.add(c);
        }
        java.util.Collections.shuffle(chars, RANDOM);

        StringBuilder shuffled = new StringBuilder();
        for (char c : chars) {
            shuffled.append(c);
        }
        return shuffled.toString();
    }

    /**
     * 生成 6 位数字安全码
     */
    public String generateSecurityCode() {
        int code = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * 密码验证结果
     */
    public class ValidationResult {
        private final boolean isValid;
        private final List<String> errors;

        public ValidationResult(boolean isValid, List<String> errors) {
            this.isValid = isValid;
            this.errors = errors;
        }

        public boolean isValid() {
            return isValid;
        }

        public List<String> getErrors() {
            return errors;
        }
    }
}

