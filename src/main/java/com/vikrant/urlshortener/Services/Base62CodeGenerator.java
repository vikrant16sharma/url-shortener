package com.vikrant.urlshortener.Services;
import org.springframework.stereotype.Component;
import java.security.SecureRandom;
@Component
public class Base62CodeGenerator implements CodeGenerator {
//    private static final String BASE62 =
//            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
//    @Override
//    public String generate(Long id) {
//
//        if (id == null || id <= 0) {
//            throw new IllegalArgumentException(
//                    "ID must be positive"
//            );
//        }
//
//        StringBuilder result = new StringBuilder();
//
//        long number = id;
//
//        while (number > 0) {
//
//            int digit = (int) (number % 62);
//
//            result.append(BASE62.charAt(digit));
//
//            number /= 62;
//        }
//
//        return result.reverse().toString();
//    }
private static final String BASE62 =
        "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final int CODE_LENGTH = 7;

    private final SecureRandom random = new SecureRandom();

    @Override
    public String generate() {

        StringBuilder code = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {

            int index = random.nextInt(BASE62.length());

            code.append(BASE62.charAt(index));
        }

        return code.toString();
    }
}
