import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lin on 2015/7/17.
 */
public class ReadFontTest {
    private static HashMap<String, byte[]> FontHash;
    private static HashMap<String, String> T1;
    static SSD1306Lib SSD;
    public static void main(String[] args) throws Exception {
        String s1;
        List<String>  FontData;
        byte[] hexStrngToBytes;
        FontHash  = new HashMap<String, byte[]>();
        T1 = new HashMap<String, String>();
        SSD=new SSD1306Lib();

        FontData=SSD.ReadStrFile("/home/pi/prog/2.TXT");
//        System.out.println("Size Array"+FontData.size());

        Integer P=3;
        String[] FontArray;
        String Temp=null;
        while(FontData.get(P).length()>0)
        {
//            System.out.println(FontData.get(P));
            FontArray=FontData.get(P).split(":");
//            System.out.println(FontArray[0] + ":" + FontArray[1]);
            FontHash.put(FontArray[1], hexStringToBytes(FontArray[0]));
/*
            SSD.clear();
            DispChinese(0, 0, 16, 16, false, hexStringToBytes(FontArray[0]));
            SSD.update();
            Thread.sleep(500);
*/
            P++;

        }
//        DispChinese(0,0,16,16,false,FontHash.get("�?"));
//        DispChineseString(0,0,"abc");
//        DispChineseString(0, 0, "林测试庭12");
        DispChineseString(0, 0, "测[]1ABCD郁试");
        SSD.update();
        Thread.sleep(7000);
       /* SSD.clear();
        Thread.sleep(500);
        SSD.update();
        DispChinese(0,0,16,16,false,FontHash.get("�?"));
        SSD.update();
        Thread.sleep(1000);*/
    }
    public static  void DispChineseString(Integer x,Integer y,String S1) throws UnsupportedEncodingException
    {
        Integer X_=0,Y_=0;
        Integer CharPoint=0;
        ChineseStr CHI=new ChineseStr();
        byte TempByte[]=S1.getBytes("UTF-8");
        char ch;

        while(CharPoint<S1.getBytes("UTF-8").length)
        {
            ch=(char) TempByte[CharPoint];
            if(isChinese(ch)){
                DispChinese(x + X_, y + Y_, 16, 16, false, FontHash.get(new String(TempByte, CharPoint, 3)));
                X_=X_+16;
                CharPoint+=3;
            } else {
                DispEng(x + X_, y + Y_, 16, 8, false, FontHash.get(new String(TempByte, CharPoint, 1)));

                X_=X_+8;
                CharPoint+=1;
            }

        }
//        ChineseStr.CalNumb(S1);
//        DispChinese(x, y, 16, 16, false, FontHash.get(ChineseStr.substring(S1,0, 3, "UTF-8")));
//        DispChinese(10, 0, 16, 16, false, FontHash.get(ChineseStr.substring(S1, 3, "UTF-8")));

    }
    public static  void DispChineseString1(Integer x,Integer y,String S1) throws UnsupportedEncodingException
    {
        Integer X_=0,Y_=0;
        Integer CharPoint=0;
        ChineseStr CHI=new ChineseStr();
        byte TempByte[]=S1.getBytes("UTF-8");
        char ch;
       /* System.out.println("Length:"+ S1.getBytes("UTF-8").length);
        System.out.println("Index:" + ChineseStr.substring(S1, 9, "UTF-8"));
        System.out.println("CharLen:"+TempByte.length);*/
//        for(int i=0;i<S1.length();i++){
        while(CharPoint<S1.getBytes("UTF-8").length)
        {
            ch=(char) TempByte[CharPoint];
//            System.out.println("Charpoint:"+CharPoint);
            if(ch>='0'&&ch<='9'){ //数字
//                digitCount++;
//                CharPoint++;
//                SSD.drawString(Character.toString(ch),x+X_,y,true);
                /*String StrTemp=new String(TempByte, CharPoint, 1);
                System.out.println(StrTemp+":"+convertToHex(FontHash.get(StrTemp)));
//                System.out.println(new String(TempByte, CharPoint, 1));
//                drawChar(x, y,true, FontHash.get(StrTemp));
                System.out.println("Char X:"+x+" Char _X:"+X_);*/
                DispEng(x + X_, y + Y_, 16, 8, false, FontHash.get(new String(TempByte, CharPoint, 1)));

                X_=X_+8;
                CharPoint+=1;
            }else if((ch>='a'&&ch<'z')||(ch>='A'&&ch<'Z')){
//                englishCount++;
//                System.out.println(new String(TempByte,CharPoint,1));
//                SSD.drawString(Character.toString(ch),x+X_,y+Y_,true);
                DispEng(x + X_, y + Y_, 16, 8, false, FontHash.get(new String(TempByte, CharPoint, 1)));
                X_=X_+8;
                CharPoint+=1;;
            }
            else{
//                chineseCount++;
//                String TestStr=ChineseStr.substring(S1,3, "UTF-8");
//                System.out.println(new String(TempChar, CharPoint,3));
//                System.out.println(new String(TempChar, 0,3));

//                System.out.println(new String(TempByte,CharPoint,3));
//                System.out.println("Char X:"+x+" Char _X:"+X_);
                DispChinese(x + X_, y + Y_, 16, 16, false, FontHash.get(new String(TempByte, CharPoint, 3)));
                X_=X_+16;
                CharPoint+=3;

            }
        }
//        ChineseStr.CalNumb(S1);
//        DispChinese(x, y, 16, 16, false, FontHash.get(ChineseStr.substring(S1,0, 3, "UTF-8")));
//        DispChinese(10, 0, 16, 16, false, FontHash.get(ChineseStr.substring(S1, 3, "UTF-8")));

    }


    public static boolean isContainChinese(String str) {

        Pattern p=Pattern.compile("[u4e00-u9fa5]");
        Matcher m=p.matcher(str);
        if(m.find())
        {
            return true;
        }
        return false;
    }
    public static boolean isChinese(char c) {
           Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
           if (   ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
               || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
               || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
               || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
               || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
               || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
               )
               //|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION)
           {
               return true;
       }
           return false;
       }

    static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('A' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }


    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static byte[] hexToBytes(String hexString) {

        char[] hex = hexString.toCharArray();
        //??rawData?????b
        int length = hex.length / 2;
        byte[] rawData = new byte[length];
        for (int i = 0; i < length; i++) {
            //???Nhex?????10?i????
            int high = Character.digit(hex[i * 2], 16);
            int low = Character.digit(hex[i * 2 + 1], 16);
            //?N??@?????G?i????????4??,ex: 00001000 => 10000000 (8=>128)
            //?M??P??G?????G?i???@?p??ex: 10000000 | 00001100 => 10001100 (137)
            int value = (high << 4) | low;
            //?PFFFFFFFF?@???
            if (value > 127)
                value -= 256;
            //?????^byte?NOK
            rawData[i] = (byte) value;
        }
        return rawData;
    }

    private static void ReadFileByLine() {

        try {
            List<String> testlines = Files.readAllLines(Paths.get("c:\\2.txt"),
                    StandardCharsets.UTF_8);
            for (String linestr : testlines) {
//                System.out.println(linestr);
            }
        } catch (Exception E) {

        }

    }
//    int StartX,int StartY,int FontHeight,int FontWidth,Boolean Invert,byte FONT[])
//    public void drawChar(char c, int x, int y, boolean on) {
public static void drawChar(int x, int y, boolean on,byte FONT[]) {
for(Integer k=0;k<2;k++) {
    for (int i = 0; i < 8; ++i) {
        int line = FONT[k*8+ i];
        for (int j = 0; j < 8; ++j) {

            if ((line & 0x01) > 0) {
                SSD.setPixel(x + i, y + j, on);
            }
            line >>= 1;
        }
    }
}
}
    public  static  void DispEng(int StartX,int StartY,int FontHeight,int FontWidth,Boolean Invert,byte FONT[])
    {
//        System.out.println("FontWidth:"+FontWidth+" FontHeight-"+FontHeight);
            for (int i = 0; i < (FontHeight/8); i++) {
                for(int h=0;h< FontWidth;h++) {
//                int line = FONT[K*16+ i];
//                    System.out.println("Code:"+"K:"+k+"-I:"+i);
//                    int line = FONT[h+(FontHeight/8)*i];
                    int line = FONT[h+i*FontWidth];
//                    FONT[K*FontHeight+ i];
//                convertToHex(FontHash.get(StrTemp))

                   /* byte s2[] = new byte[1];
                    s2[0] = FONT[i];
                    System.out.println("I:" + i + "-h:" + h + "-Byte:" + convertToHex(s2) + "-Index:" + (h * FontHeight + i));
*/
                    for (int j = 0; j < 8; ++j) {
                        if (Invert != true) {
                            if ((line & 0x01) > 0) {
                                SSD.setPixel(StartX + h, StartY + j+i*8, true);
//                                System.out.println("(line & 0x01) > 0）StartX:" + (StartX + i) + "--StartY:" + (StartY + j + (h * 8)));
                            }

                        } else {
                            if ((line & 0x01) == 0) {
                                SSD.setPixel(StartX + h, StartY +j+i*8, true);
//                                System.out.println("(line & 0x01) == 0） StartX:" + (StartX + i) + "--StartY:" + (StartY + j + (h * 8)));
                            }
                        }

                        line >>= 1;
                    }
                }
        }
    }

    public  static  void DispChinese(int StartX,int StartY,int FontHeight,int FontWidth,Boolean Invert,byte FONT[])
    {


        for(int K = 0; K < (FontWidth/8);K++) {
            for (int i = 0; i < FontHeight; ++i) {
//                int line = FONT[K*16+ i];
                int line = FONT[K*FontHeight+ i];
//                convertToHex(FontHash.get(StrTemp))
               /* byte s2[]=new byte[1];
                s2[0]=FONT[K*FontHeight+i];
                System.out.println("I:"+i+"-K:"+K+"-Byte:"+convertToHex(s2));
*/
                for (int j = 0; j < 8; ++j) {
                    if(Invert!=true) {
                        if ((line & 0x01) > 0) {
                            SSD.setPixel(StartX + i, StartY + j + (K * 8), true);
//                            System.out.println("StartX:"+(StartX + i)+"--StartY:"+(StartY + j + (K * 8)));
                        }

                    }else
                    {
                        if ((line & 0x01) == 0) {
                            SSD.setPixel(StartX + i, StartY + j + (K * 8), true);
                        }
                    }

                    line >>= 1;
                }
            }
        }
    }
}