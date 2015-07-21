import java.io.UnsupportedEncodingException;

/**
 * Created by Lin on 2015/7/21.
 */
public class ChineseStr {

    public static boolean isLetter(char c) {
        int k = 0x80;
        return c / k == 0 ? true : false;
    }

    public static void CalNumb(String str) {
        int englishCount=0;
        int chineseCount=0;
        int digitCount=0;

//        String str="你好啊nihaoa9999999";
        for(int i=0;i<str.length();i++){
            char ch=str.charAt(i);
            if(ch>='0'&&ch<='9'){ //数字
                digitCount++;
            }else if((ch>='a'&&ch<'z')||(ch>='A'&&ch<'Z')){
                englishCount++;
            }
            else{
                chineseCount++;
            }
        }

        System.out.println("中文字符的数量: "+chineseCount);
        System.out.println("英文字符的数量: "+englishCount);
        System.out.println("数字字符的数量: "+digitCount);
    }
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

    public static int lengths(String s) {
        if (s == null)
            return 0;
        char[] c = s.toCharArray();
        int len = 0;
        for (int i = 0; i < c.length; i++) {
            len++;
            if (!isLetter(c[i])) {
                len++;
            }
        }
        return len;
    }


}
