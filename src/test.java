import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by Lin on 2015/7/18.
 */
public class test {
    static SSD1306Lib T1;
    public static void main(String[] args) throws InterruptedException {
        try
        {
            T1=new SSD1306Lib();
            /*T1.clear();
            T1.drawString("1234",0,0,true);*/
            /*T1.drawStringCentered("Hello World!", 1, true);
            T1.drawStringCentered("Hello World!", 25, true);
            T1.drawStringCentered("Hello World!", 49, true);

            T1.update();
            Thread.sleep(1000);
            T1.clear();
            System.out.println("Done");*/
            for(Integer i=0;i<8;i++) {
                for (Integer j = 0; j < 4; j++)
                    DispChinese(i * 16, j * 16, 16, 16,true);
            }
//            T1.drawChar((char)'A',32,32,true);
            T1.update();
            Thread.sleep(10000);
            T1.clear();
            for(Integer i=0;i<8;i++) {
                for (Integer j = 0; j < 4; j++)
                    DispChinese(i * 16, j * 16, 16, 16,false);
            }
            T1.update();
            Thread.sleep(10000);
            ReadFileByLine();
        }catch (Exception E)
        {

        }
    }

    public  static  void DispChinese(int StartX,int StartY,int FontWidth,int FongHeight,Boolean Invert)
    {
        byte FONT[] = {
               (byte)0x10,(byte)0x60,(byte)0x02,(byte)0x8C,(byte)0x00,(byte)0xFE,(byte)0x02,(byte)0xF2,
                (byte)0x02,(byte)0xFE,(byte)0x00,(byte)0xF8,(byte)0x00,(byte)0xFF,(byte)0x00,(byte)0x00,
                (byte)0x04,(byte)0x04,(byte)0x7E,(byte)0x01,(byte)0x80,(byte)0x47,(byte)0x30,(byte)0x0F,
                (byte)0x10,(byte)0x27,(byte)0x00,(byte)0x47,(byte)0x80,(byte)0x7F,(byte)0x00,(byte)0x00
        };

        for(int K = 0; K < FongHeight/8;K++) {
            for (int i = 0; i < FontWidth; ++i) {
//                int line = FONT[K*16+ i];
                int line = FONT[K*FongHeight+ i];


                for (int j = 0; j < 8; ++j) {
                    if(Invert!=true) {
                        if ((line & 0x01) > 0) {
                            T1.setPixel(StartX + i, StartY + j + (K * 8), true);
                        }

                    }else
                    {
                        if ((line & 0x01) == 0) {
                            T1.setPixel(StartX + i, StartY + j + (K * 8), true);
                        }
                    }

                    line >>= 1;
                }
            }
        }
    }
    public static byte[] hexToBytes(String hexString) {

        char[] hex = hexString.toCharArray();
        //轉rawData長度減半
        int length = hex.length / 2;
        byte[] rawData = new byte[length];
        for (int i = 0; i < length; i++) {
            //先將hex資料轉10進位數值
            int high = Character.digit(hex[i * 2], 16);
            int low = Character.digit(hex[i * 2 + 1], 16);
            //將第一個值的二進位值左平移4位,ex: 00001000 => 10000000 (8=>128)
            //然後與第二個值的二進位值作聯集ex: 10000000 | 00001100 => 10001100 (137)
            int value = (high << 4) | low;
            //與FFFFFFFF作補集
            if (value > 127)
                value -= 256;
            //最後轉回byte就OK
            rawData[i] = (byte) value;
        }
        return rawData;
    }
    private static void  ReadFileByLine()
    {

        try {
            List<String> testlines = Files.readAllLines(Paths.get("c:\\2.txt"),
                    StandardCharsets.UTF_8);
            for (String linestr : testlines) {
                System.out.println(linestr);
            }
        }catch (Exception E)
        {

        }

    }
}
