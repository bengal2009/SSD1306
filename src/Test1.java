

import java.io.UnsupportedEncodingException;

public class Test1 {
    /**
     * @param text
     *            目标字符串
     * @param length
     *            截取长度
     * @param encode
     *            采用的编码方式
     * @return
     * @throws UnsupportedEncodingException
     */

    public static String substring(String text, int length, String encode)
            throws UnsupportedEncodingException {
        if (text == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int currentLength = 0;
        for (char c : text.toCharArray()) {
            currentLength += String.valueOf(c).getBytes(encode).length;
            if (currentLength <= length) {
                sb.append(c);
            } else {
                break;
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        String text = "我ABC汉DEF";
        int length1 = 3;
        int length2 = 6;
        String[] encodes = new String[] { "GB2312", "GBK", "GB18030", "CP936",
                "CNS11643", "UTF-8" };

        for (String encode : encodes) {
            System.out.println(new StringBuilder().append("用").append(encode)
                    .append("编码截取字符串 -- 【").append(text).append("】")
                    .append(length1).append("个字节的结果是【")
                    .append(substring(text, length1, encode)).append("】")
                    .toString());
            System.out.println(new StringBuilder().append("用").append(encode)
                    .append("编码截取字符串 -- 【").append(text).append("】")
                    .append(length2).append("个字节的结果是【")
                    .append(substring(text, length2, encode)).append("】")
                    .toString());
        }

    }
}