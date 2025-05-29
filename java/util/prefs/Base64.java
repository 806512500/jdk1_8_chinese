
/*
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.util.prefs;

/**
 * 静态方法用于将Base64编码的字符串转换为字节数组，反之亦然。
 *
 * @author  Josh Bloch
 * @see     Preferences
 * @since   1.4
 */
class Base64 {
    /**
     * 将指定的字节数组转换为Base64字符串，遵循Preferences.put(byte[])。
     */
    static String byteArrayToBase64(byte[] a) {
        return byteArrayToBase64(a, false);
    }

    /**
     * 将指定的字节数组转换为“替代表示”Base64字符串。这种非标准变体使用不包含大写字母的字母表，使其适用于发生大小写折叠的情况。
     */
    static String byteArrayToAltBase64(byte[] a) {
        return byteArrayToBase64(a, true);
    }

    private static String byteArrayToBase64(byte[] a, boolean alternate) {
        int aLen = a.length;
        int numFullGroups = aLen / 3;
        int numBytesInPartialGroup = aLen - 3 * numFullGroups;
        int resultLen = 4 * ((aLen + 2) / 3);
        StringBuffer result = new StringBuffer(resultLen);
        char[] intToAlpha = (alternate ? intToAltBase64 : intToBase64);

        // 将所有完整组从字节数组元素转换为Base64
        int inCursor = 0;
        for (int i = 0; i < numFullGroups; i++) {
            int byte0 = a[inCursor++] & 0xff;
            int byte1 = a[inCursor++] & 0xff;
            int byte2 = a[inCursor++] & 0xff;
            result.append(intToAlpha[byte0 >> 2]);
            result.append(intToAlpha[(byte0 << 4) & 0x3f | (byte1 >> 4)]);
            result.append(intToAlpha[(byte1 << 2) & 0x3f | (byte2 >> 6)]);
            result.append(intToAlpha[byte2 & 0x3f]);
        }

        // 如果存在部分组，则进行转换
        if (numBytesInPartialGroup != 0) {
            int byte0 = a[inCursor++] & 0xff;
            result.append(intToAlpha[byte0 >> 2]);
            if (numBytesInPartialGroup == 1) {
                result.append(intToAlpha[(byte0 << 4) & 0x3f]);
                result.append("==");
            } else {
                // assert numBytesInPartialGroup == 2;
                int byte1 = a[inCursor++] & 0xff;
                result.append(intToAlpha[(byte0 << 4) & 0x3f | (byte1 >> 4)]);
                result.append(intToAlpha[(byte1 << 2) & 0x3f]);
                result.append('=');
            }
        }
        // assert inCursor == a.length;
        // assert result.length() == resultLen;
        return result.toString();
    }

    /**
     * 该数组是一个查找表，将6位正整数索引值转换为RFC 2045表1中指定的“Base64字母表”等效值。
     */
    private static final char intToBase64[] = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };

    /**
     * 该数组是一个查找表，将6位正整数索引值转换为“替代Base64字母表”等效值。
     * 这并不是RFC 2045表1中规定的真正的Base64字母表。
     * 这个替代字母表不使用大写字母。它设计用于发生“大小写折叠”的环境。
     */
    private static final char intToAltBase64[] = {
        '!', '"', '#', '$', '%', '&', '\'', '(', ')', ',', '-', '.', ':',
        ';', '<', '>', '@', '[', ']', '^',  '`', '_', '{', '|', '}', '~',
        'a', 'b', 'c', 'd', 'e', 'f', 'g',  'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't',  'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6',  '7', '8', '9', '+', '?'
    };

    /**
     * 将指定的Base64字符串（遵循Preferences.get(byte[])）转换为字节数组。
     *
     * @throw IllegalArgumentException 如果<tt>s</tt>不是有效的Base64字符串。
     */
    static byte[] base64ToByteArray(String s) {
        return base64ToByteArray(s, false);
    }

    /**
     * 将指定的“替代表示”Base64字符串转换为字节数组。
     *
     * @throw IllegalArgumentException 或 ArrayOutOfBoundsException
     *        如果<tt>s</tt>不是有效的替代表示Base64字符串。
     */
    static byte[] altBase64ToByteArray(String s) {
        return base64ToByteArray(s, true);
    }

    private static byte[] base64ToByteArray(String s, boolean alternate) {
        byte[] alphaToInt = (alternate ?  altBase64ToInt : base64ToInt);
        int sLen = s.length();
        int numGroups = sLen / 4;
        if (4 * numGroups != sLen)
            throw new IllegalArgumentException(
                "字符串长度必须是四的倍数。");
        int missingBytesInLastGroup = 0;
        int numFullGroups = numGroups;
        if (sLen != 0) {
            if (s.charAt(sLen - 1) == '=') {
                missingBytesInLastGroup++;
                numFullGroups--;
            }
            if (s.charAt(sLen - 2) == '=')
                missingBytesInLastGroup++;
        }
        byte[] result = new byte[3 * numGroups - missingBytesInLastGroup];

        // 将所有完整组从Base64转换为字节数组元素
        int inCursor = 0, outCursor = 0;
        for (int i = 0; i < numFullGroups; i++) {
            int ch0 = base64toInt(s.charAt(inCursor++), alphaToInt);
            int ch1 = base64toInt(s.charAt(inCursor++), alphaToInt);
            int ch2 = base64toInt(s.charAt(inCursor++), alphaToInt);
            int ch3 = base64toInt(s.charAt(inCursor++), alphaToInt);
            result[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));
            result[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
            result[outCursor++] = (byte) ((ch2 << 6) | ch3);
        }


                    // 翻译部分组，如果存在
        if (missingBytesInLastGroup != 0) {
            int ch0 = base64toInt(s.charAt(inCursor++), alphaToInt);
            int ch1 = base64toInt(s.charAt(inCursor++), alphaToInt);
            result[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));

            if (missingBytesInLastGroup == 1) {
                int ch2 = base64toInt(s.charAt(inCursor++), alphaToInt);
                result[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
            }
        }
        // assert inCursor == s.length()-missingBytesInLastGroup;
        // assert outCursor == result.length;
        return result;
    }

    /**
     * 将指定字符（假设其在“Base 64 字母表”中）翻译为其等效的 6 位正整数。
     *
     * @throw IllegalArgumentException 或 ArrayOutOfBoundsException 如果
     *        c 不在 Base64 字母表中。
     */
    private static int base64toInt(char c, byte[] alphaToInt) {
        int result = alphaToInt[c];
        if (result < 0)
            throw new IllegalArgumentException("非法字符 " + c);
        return result;
    }

    /**
     * 该数组是一个查找表，将从“Base64 字母表”（如 RFC 2045 表 1 所指定）中提取的 Unicode 字符
     * 翻译为其 6 位正整数等效值。不在 Base64 字母表中的字符但落在数组范围内的字符
     * 被翻译为 -1。
     */
    private static final byte base64ToInt[] = {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54,
        55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4,
        5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
        24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34,
        35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
    };

    /**
     * 该数组是 base64ToInt 的类似物，但用于避免使用大写字母的非标准变体。
     */
    private static final byte altBase64ToInt[] = {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 1,
        2, 3, 4, 5, 6, 7, 8, -1, 62, 9, 10, 11, -1 , 52, 53, 54, 55, 56, 57,
        58, 59, 60, 61, 12, 13, 14, -1, 15, 63, 16, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, 17, -1, 18, 19, 21, 20, 26, 27, 28, 29, 30, 31, 32, 33,
        34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50,
        51, 22, 23, 24, 25
    };

    public static void main(String args[]) {
        int numRuns  = Integer.parseInt(args[0]);
        int numBytes = Integer.parseInt(args[1]);
        java.util.Random rnd = new java.util.Random();
        for (int i=0; i<numRuns; i++) {
            for (int j=0; j<numBytes; j++) {
                byte[] arr = new byte[j];
                for (int k=0; k<j; k++)
                    arr[k] = (byte)rnd.nextInt();

                String s = byteArrayToBase64(arr);
                byte [] b = base64ToByteArray(s);
                if (!java.util.Arrays.equals(arr, b))
                    System.out.println("惨败!");

                s = byteArrayToAltBase64(arr);
                b = altBase64ToByteArray(s);
                if (!java.util.Arrays.equals(arr, b))
                    System.out.println("备用惨败!");
            }
        }
    }
}
