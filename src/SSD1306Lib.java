/*
 * Copyright (C) 2015 Florian Frankenberger.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */


import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * A raspberry pi driver for the 128x64 pixel OLED display (i2c bus).
 * The supported kind of display uses the SSD1306 driver chip and
 * is connected to the raspberry's i2c bus (bus 1).
 * <p/>
 * Note that you need to enable i2c (using for example raspi-config).
 * Also note that it is possible to speed up the refresh rate of the
 * display up to ~60fps by adding the following to the config.txt of 
 * your raspberry: dtparam=i2c1_baudrate=1000000
 * <p/>
 * Sample usage:
 * <pre>
 * OLEDDisplay display = new OLEDDisplay();
 * display.drawStringCentered("Hello World!", 25, true);
 * display.update();
 * </pre>
 * <p/>
 * This class is basically a rough port of Adafruit's BSD licensed
 * SSD1306 library (https://github.com/adafruit/Adafruit_SSD1306)
 *
 * @author Florian Frankenberger
 */
public class SSD1306Lib {

    private static final Logger LOGGER = Logger.getLogger(SSD1306Lib.class.getCanonicalName());


    private static final int DEFAULT_I2C_BUS = I2CBus.BUS_1;
    private static final int DEFAULT_DISPLAY_ADDRESS = 0x3C;

    private static final int DISPLAY_WIDTH = 128;
    private static final int DISPLAY_HEIGHT = 64;

    private static final byte SSD1306_SETCONTRAST = (byte) 0x81;
    private static final byte SSD1306_DISPLAYALLON_RESUME = (byte) 0xA4;
    private static final byte SSD1306_DISPLAYALLON = (byte) 0xA5;
    private static final byte SSD1306_NORMALDISPLAY = (byte) 0xA6;
    private static final byte SSD1306_INVERTDISPLAY = (byte) 0xA7;
    private static final byte SSD1306_DISPLAYOFF = (byte) 0xAE;
    private static final byte SSD1306_DISPLAYON = (byte) 0xAF;

    private static final byte SSD1306_SETDISPLAYOFFSET = (byte) 0xD3;
    private static final byte SSD1306_SETCOMPINS = (byte) 0xDA;

    private static final byte SSD1306_SETVCOMDETECT = (byte) 0xDB;

    private static final byte SSD1306_SETDISPLAYCLOCKDIV = (byte) 0xD5;
    private static final byte SSD1306_SETPRECHARGE = (byte) 0xD9;

    private static final byte SSD1306_SETMULTIPLEX = (byte) 0xA8;

    private static final byte SSD1306_SETLOWCOLUMN = (byte) 0x00;
    private static final byte SSD1306_SETHIGHCOLUMN = (byte) 0x10;

    private static final byte SSD1306_SETSTARTLINE = (byte) 0x40;

    private static final byte SSD1306_MEMORYMODE = (byte) 0x20;
    private static final byte SSD1306_COLUMNADDR = (byte) 0x21;
    private static final byte SSD1306_PAGEADDR = (byte) 0x22;

    private static final byte SSD1306_COMSCANINC = (byte) 0xC0;
    private static final byte SSD1306_COMSCANDEC = (byte) 0xC8;

    private static final byte SSD1306_SEGREMAP = (byte) 0xA0;

    private static final byte SSD1306_CHARGEPUMP = (byte) 0x8D;

    private static final byte SSD1306_EXTERNALVCC = (byte) 0x1;
    private static final byte SSD1306_SWITCHCAPVCC = (byte) 0x2;

    private final I2CBus bus;
    private final I2CDevice device;


    private static final byte[] imageBuffer = new byte[(DISPLAY_WIDTH * DISPLAY_HEIGHT) / 8];
    private static HashMap<String, byte[]> FontHash=new HashMap<String, byte[]>();

//Migration Definition

    // Scrollingprivate static final ints
    private static final int SSD1306_ACTIVATE_SCROLL= 0x2F;
    private static final int SSD1306_DEACTIVATE_SCROLL= 0x2E;
    private static final int SSD1306_SET_VERTICAL_SCROLL_AREA= 0xA3;
    private static final int SSD1306_RIGHT_HORIZONTAL_SCROLL= 0x26;
    private static final int SSD1306_LEFT_HORIZONTAL_SCROLL = 0x27;
    private static final int SSD1306_VERTICAL_AND_RIGHT_HORIZONTAL_SCROLL= 0x29;
    private static final int SSD1306_VERTICAL_AND_LEFT_HORIZONTAL_SCROLL= 0x2A;

//    Chinese Font Display Function

    public void  ReadFontFile(String Filename)
    {
        List<String>  FontData;
        FontData=ReadStrFile(Filename);
        Integer line=3; //Start Line from 3
        String[] FontArray;
        while(FontData.get(line).length()>0)
        {
            FontArray=FontData.get(line).split(":");
            FontHash.put(FontArray[1], hexStringToBytes(FontArray[0]));
            line++;

        }
    }
   public static  void DispStr(Integer x,Integer y,String S1,Boolean invert1) throws UnsupportedEncodingException
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
                DispEng(x + X_, y + Y_, 16, 16, invert1, FontHash.get(new String(TempByte, CharPoint, 3)));
                X_=X_+16;
                CharPoint+=3;
            } else {
                DispEng(x + X_, y + Y_, 16, 8, invert1, FontHash.get(new String(TempByte, CharPoint, 1)));

                X_=X_+8;
                CharPoint+=1;
            }

        }

    }
    public  static  void DispEng(int StartX,int StartY,int FontHeight,int FontWidth,Boolean Invert,byte FONT[])
    {
        for (int i = 0; i < (FontHeight/8); i++) {
            for(int h=0;h< FontWidth;h++) {
                int line = FONT[h+i*FontWidth];
                for (int j = 0; j < 8; ++j) {
                    if (Invert != true) {
                        if ((line & 0x01) > 0) {
                            WritePixel(StartX + h, StartY + j + i * 8, true);
                        }

                    } else {
                        if ((line & 0x01) == 0) {
                            WritePixel(StartX + h, StartY +j+i*8, true);
                        }
                    }

                    line >>= 1;
                }
            }
        }
    }
    private static boolean isChinese(char c) {
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
    /**
     * creates an oled display object with default
     * i2c bus 1 and default display address of 0x3C
     *
     * @throws IOException
     */
    public SSD1306Lib() throws IOException {
        this(DEFAULT_I2C_BUS, DEFAULT_DISPLAY_ADDRESS);
    }

    /**
     * creates an oled display object with default
     * i2c bus 1 and the given display address
     *
     * @param displayAddress the i2c bus address of the display
     * @throws IOException
     */
    public SSD1306Lib(int displayAddress) throws IOException {
        this(DEFAULT_I2C_BUS, displayAddress);
    }

    /**
     * constructor with all parameters
     *
     * @param busNumber the i2c bus number (use constants from I2CBus)
     * @param displayAddress the i2c bus address of the display
     * @throws IOException
     */
    public SSD1306Lib(int busNumber, int displayAddress) throws IOException {
        bus = I2CFactory.getInstance(busNumber);
        device = bus.getDevice(displayAddress);

//        LOGGER.log(Level.FINE, "Opened i2c bus");

        clear();

        //add shutdown hook that clears the display
        //and closes the bus correctly when the software
        //if terminated.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });

        init();
//        System.out.println(busNumber);
    }

    public synchronized void clear() {
        Arrays.fill(imageBuffer, (byte) 0x00);
    }

    public int getWidth() {
        return DISPLAY_WIDTH;
    }

    public int getHeight() {
        return DISPLAY_HEIGHT;
    }

    public void writeCommand(byte command) throws IOException {
        device.write(0x00, command);
    }

    private void init() throws IOException {
        writeCommand(SSD1306_DISPLAYOFF);                    // 0xAE
        writeCommand(SSD1306_SETDISPLAYCLOCKDIV);            // 0xD5
        writeCommand((byte) 0x80);                           // the suggested ratio 0x80
        writeCommand(SSD1306_SETMULTIPLEX);                  // 0xA8
        writeCommand((byte) 0x3F);
        writeCommand(SSD1306_SETDISPLAYOFFSET);              // 0xD3
        writeCommand((byte) 0x0);                            // no offset
        writeCommand((byte) (SSD1306_SETSTARTLINE | 0x0));   // line #0
        writeCommand(SSD1306_CHARGEPUMP);                    // 0x8D
        writeCommand((byte) 0x14);
        writeCommand(SSD1306_MEMORYMODE);                    // 0x20
        writeCommand((byte) 0x00);                           // 0x0 act like ks0108
        writeCommand((byte) (SSD1306_SEGREMAP | 0x1));
        writeCommand(SSD1306_COMSCANDEC);
        writeCommand(SSD1306_SETCOMPINS);                    // 0xDA
        writeCommand((byte) 0x12);
        writeCommand(SSD1306_SETCONTRAST);                   // 0x81
        writeCommand((byte) 0xCF);
        writeCommand(SSD1306_SETPRECHARGE);                  // 0xd9
        writeCommand((byte) 0xF1);
        writeCommand(SSD1306_SETVCOMDETECT);                 // 0xDB
        writeCommand((byte) 0x40);
        writeCommand(SSD1306_DISPLAYALLON_RESUME);           // 0xA4
        writeCommand(SSD1306_NORMALDISPLAY);

        writeCommand(SSD1306_DISPLAYON);//--turn on oled panel
    }

    public synchronized static void WritePixel(int x, int y, boolean on) {
        final int pos = x + (y / 8) * DISPLAY_WIDTH;
        if (on) {
           imageBuffer[pos] |= (1 << (y & 0x07));
        } else {
            imageBuffer[pos] &= ~(1 << (y & 0x07));
        }
    }
    public synchronized  void setPixel(int x, int y, boolean on) {
        final int pos = x + (y / 8) * DISPLAY_WIDTH;

        if (on) {
            this.imageBuffer[pos] |= (1 << (y & 0x07));
        } else {
            this.imageBuffer[pos] &= ~(1 << (y & 0x07));
        }
    }

    public synchronized void drawString(String string, int x, int y, boolean on) {
        int posX = x;
        int posY = y;
        for (char c : string.toCharArray()) {
            if (c == '\n') {
                posY += 8;
                posX = x;
            }
            if (posX >= 0 && posX + 5 < this.getWidth()
                    && posY >= 0 && posY + 7 < this.getHeight()) {
                drawChar(c, posX, posY, on);
            }
            posX += 6;
        }
    }

    public synchronized void drawStringCentered(String string, int y, boolean on) {
        final int strSizeX = string.length() * 5 + string.length() - 1;
        final int x = (this.getWidth() - strSizeX) / 2;
        drawString(string, x, y, on);
    }

    public synchronized void clearRect(int x, int y, int width, int height, boolean on) {
        for (int posX = x; posX < x + width; ++posX) {
            for (int posY = y; posY < y + height; ++posY) {
                setPixel(posX, posY, on);
            }
        }
    }

    public synchronized void drawChar(char c, int x, int y, boolean on) {
        if (c > 255) {
            c = '?';
        }

        for (int i = 0; i < 5; ++i) {
            int line = FONT[(c * 5) + i];

            for (int j = 0; j < 8; ++j) {
                if ((line & 0x01) > 0) {
                    setPixel(x + i, y + j, on);
                }
                line >>= 1;
            }
        }
    }

    /**
     * draws the given image over the current image buffer. The image
     * is automatically converted to a binary image (if it not already
     * is).
     * <p/>
     * Note that the current buffer is not cleared before, so if you
     * want the image to completely overwrite the current display
     * content you need to call clear() before.
     *
     * @param image
     * @param x
     * @param y
     */
    public synchronized void drawImage(BufferedImage image, int x, int y) {
        BufferedImage tmpImage = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        tmpImage.getGraphics().drawImage(image, x, y, null);

        int index = 0;
        int pixelval;
        final byte[] pixels = ((DataBufferByte) tmpImage.getRaster().getDataBuffer()).getData();
        for (int posY = 0; posY < DISPLAY_HEIGHT; posY++) {
            for (int posX = 0; posX < DISPLAY_WIDTH / 8; posX++) {
                for (int bit = 0; bit < 8; bit++) {
                    pixelval = (byte) ((pixels[index/8] >>  (8 - bit)) & 0x01);
                    setPixel(posX * 8 + bit, posY, pixelval > 0);
                    index++;
                }
            }
        }
    }

    /**
     * sends the current buffer to the display
     * @throws IOException
     */
    public synchronized void update() throws IOException {
        writeCommand(SSD1306_COLUMNADDR);
        writeCommand((byte) 0);   // Column start address (0 = reset)
        writeCommand((byte) (DISPLAY_WIDTH - 1)); // Column end address (127 = reset)

        writeCommand(SSD1306_PAGEADDR);
        writeCommand((byte) 0); // Page start address (0 = reset)
        writeCommand((byte) 7); // Page end address

        for (int i = 0; i < ((DISPLAY_WIDTH * DISPLAY_HEIGHT / 8) / 16); i++) {
            // send a bunch of data in one xmission
            device.write((byte) 0x40, imageBuffer, i * 16, 16);
        }
    }

    private synchronized void shutdown() {
        try {
            //before we shut down we clear the display
            clear();
            update();

            //now we close the bus
            bus.close();
        } catch (IOException ex) {
//            LOGGER.log(Level.FINE, "Closing i2c bus");
        }
    }
    public static List<String> ReadStrFile(String Filename) {
        String line;
        List<String> testlines = new ArrayList<String>();
        try {



            InputStream fis = new FileInputStream(Filename);
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("GBK"));
            BufferedReader br = new BufferedReader(isr);

            while ((line = br.readLine()) != null) {
                // Deal with the line
//                System.out.println(line);
                testlines.add(line);
            }

        } catch (Exception E) {

        }
        return testlines;
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
    public void invertDisplay(boolean Inv)
    {
        try {
            if (Inv) {
                writeCommand((byte) SSD1306_INVERTDISPLAY);
            } else {
                writeCommand((byte) SSD1306_NORMALDISPLAY);
            }
        }catch (Exception E)
        {

        }

    }
    // startscrollright
// Activate a right handed scroll for rows start through stop
// Hint, the display is 16 rows tall. To scroll the whole display, run:
// display.scrollright(0x00, 0x0F)
    public  void startscrollright(int start,int endpix )
    {
        try {
           writeCommand((byte) SSD1306_RIGHT_HORIZONTAL_SCROLL);
           writeCommand((byte)0X00);
           writeCommand((byte)start);
           writeCommand((byte)0X00);
           writeCommand((byte)endpix);
           writeCommand((byte)0X01);
           writeCommand((byte)0XFF);
           writeCommand((byte)SSD1306_ACTIVATE_SCROLL);
        }catch (Exception E)
        {

        }

    }
    public  void startscrollleft(int start,int stop )
    {
        try {
            writeCommand((byte) SSD1306_LEFT_HORIZONTAL_SCROLL);
            writeCommand((byte)0X00);
            writeCommand((byte)start);
            writeCommand((byte)0X00);
            writeCommand((byte)stop);
            writeCommand((byte)0X01);
            writeCommand((byte)0XFF);
            writeCommand((byte)SSD1306_ACTIVATE_SCROLL);
        }catch (Exception E)
        {

        }


    }
    // startscrolldiagright
// Activate a diagonal scroll for rows start through stop
// Hint, the display is 16 rows tall. To scroll the whole display, run:
// display.scrollright(0x00, 0x0F)
    public void startscrolldiagright(int start, int stop)
    {
        try {
           writeCommand((byte) SSD1306_SET_VERTICAL_SCROLL_AREA);
           writeCommand((byte) 0X00);
           writeCommand((byte) 64);
           writeCommand((byte) SSD1306_VERTICAL_AND_RIGHT_HORIZONTAL_SCROLL);
           writeCommand((byte)0X00);
           writeCommand((byte) start);
           writeCommand((byte) 0X00);
           writeCommand((byte) stop);
           writeCommand((byte) 0X01);
           writeCommand((byte) SSD1306_ACTIVATE_SCROLL);
        }catch (Exception E)
        {

        }
    }
    public  void startscrolldiagleft(int start,int stop )
    {
        try {
            writeCommand((byte) SSD1306_SET_VERTICAL_SCROLL_AREA);
            writeCommand((byte) 0X00);
            writeCommand((byte) 64);
            writeCommand((byte) SSD1306_VERTICAL_AND_LEFT_HORIZONTAL_SCROLL);
            writeCommand((byte)0X00);
            writeCommand((byte) start);
            writeCommand((byte) 0X00);
            writeCommand((byte) stop);
            writeCommand((byte) 0X01);
            writeCommand((byte) SSD1306_ACTIVATE_SCROLL);
        }catch (Exception E)
        {

        }
    }
    public  void  stopscroll(){
        try {
            writeCommand((byte) SSD1306_DEACTIVATE_SCROLL);
        }catch (Exception E)
        {

        }
    }
    public static void waitForKeypress() {
        try {

            while (System.in.read() != (int) 'c') {
                Thread.sleep(100); //Let the CPU do more worthwhile things for 0.1 seconds ;)
            }
        }catch(Exception E)
        {

        }
    }
    public  void drawCircle(Integer x0, Integer y0, Integer r
    ) {
        Integer f = 1 - r;
        Integer ddF_x = 1;
        Integer ddF_y = -2 * r;
        Integer x = 0;
        Integer y = r;

        setPixel(x0, y0 + r, true);
        setPixel(x0, y0 - r,true);
        setPixel(x0 + r, y0,true  );
        setPixel(x0 - r, y0 ,true );

        while (x<y) {
            if (f >= 0) {
                y--;
                ddF_y += 2;
                f += ddF_y;
            }
            x++;
            ddF_x += 2;
            f += ddF_x;

         setPixel(x0 + x, y0 + y, true);
         setPixel(x0 - x, y0 + y, true);
         setPixel(x0 + x, y0 - y, true);
         setPixel(x0 - x, y0 - y, true);
         setPixel(x0 + y, y0 + x, true);
         setPixel(x0 - y, y0 + x, true);
         setPixel(x0 + y, y0 - x, true);
         setPixel(x0 - y, y0 - x, true);
        }
    }
    public  void drawLine(Integer x0, Integer y0,
                                Integer x1, Integer y1) {
        boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);
        Integer temp;
        if (steep) {
            temp=y0;
            y0=x0;
            x0=temp;
//            swap(x0, y0);
            temp=y1;
            y1=x1;
            x1=temp;
//            swap(x1, y1);
        }

        if (x0 > x1) {
            temp=x1;
            x1=x0;
            x0=temp;
//            swap(x0, x1);
            temp=y1;
            y1=y0;
            y0=temp;
//            swap(y0, y1);
        }

        Integer dx, dy;
        dx = x1 - x0;
        dy = Math.abs(y1 - y0);

        Integer err = dx / 2;
        Integer ystep;

        if (y0 < y1) {
            ystep = 1;
        } else {
            ystep = -1;
        }

        for (; x0<=x1; x0++) {
            if (steep) {
                setPixel(y0, x0, true);
            } else {
               setPixel(x0, y0, true);
            }
            err -= dy;
            if (err < 0) {
                y0 += ystep;
                err += dx;
            }
        }
    }
    //simple 7x5 font
    private static final byte FONT[] = {
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x3E, (byte) 0x5B, (byte) 0x4F, (byte) 0x5B, (byte) 0x3E,
            (byte) 0x3E, (byte) 0x6B, (byte) 0x4F, (byte) 0x6B, (byte) 0x3E,
            (byte) 0x1C, (byte) 0x3E, (byte) 0x7C, (byte) 0x3E, (byte) 0x1C,
            (byte) 0x18, (byte) 0x3C, (byte) 0x7E, (byte) 0x3C, (byte) 0x18,
            (byte) 0x1C, (byte) 0x57, (byte) 0x7D, (byte) 0x57, (byte) 0x1C,
            (byte) 0x1C, (byte) 0x5E, (byte) 0x7F, (byte) 0x5E, (byte) 0x1C,
            (byte) 0x00, (byte) 0x18, (byte) 0x3C, (byte) 0x18, (byte) 0x00,
            (byte) 0xFF, (byte) 0xE7, (byte) 0xC3, (byte) 0xE7, (byte) 0xFF,
            (byte) 0x00, (byte) 0x18, (byte) 0x24, (byte) 0x18, (byte) 0x00,
            (byte) 0xFF, (byte) 0xE7, (byte) 0xDB, (byte) 0xE7, (byte) 0xFF,
            (byte) 0x30, (byte) 0x48, (byte) 0x3A, (byte) 0x06, (byte) 0x0E,
            (byte) 0x26, (byte) 0x29, (byte) 0x79, (byte) 0x29, (byte) 0x26,
            (byte) 0x40, (byte) 0x7F, (byte) 0x05, (byte) 0x05, (byte) 0x07,
            (byte) 0x40, (byte) 0x7F, (byte) 0x05, (byte) 0x25, (byte) 0x3F,
            (byte) 0x5A, (byte) 0x3C, (byte) 0xE7, (byte) 0x3C, (byte) 0x5A,
            (byte) 0x7F, (byte) 0x3E, (byte) 0x1C, (byte) 0x1C, (byte) 0x08,
            (byte) 0x08, (byte) 0x1C, (byte) 0x1C, (byte) 0x3E, (byte) 0x7F,
            (byte) 0x14, (byte) 0x22, (byte) 0x7F, (byte) 0x22, (byte) 0x14,
            (byte) 0x5F, (byte) 0x5F, (byte) 0x00, (byte) 0x5F, (byte) 0x5F,
            (byte) 0x06, (byte) 0x09, (byte) 0x7F, (byte) 0x01, (byte) 0x7F,
            (byte) 0x00, (byte) 0x66, (byte) 0x89, (byte) 0x95, (byte) 0x6A,
            (byte) 0x60, (byte) 0x60, (byte) 0x60, (byte) 0x60, (byte) 0x60,
            (byte) 0x94, (byte) 0xA2, (byte) 0xFF, (byte) 0xA2, (byte) 0x94,
            (byte) 0x08, (byte) 0x04, (byte) 0x7E, (byte) 0x04, (byte) 0x08,
            (byte) 0x10, (byte) 0x20, (byte) 0x7E, (byte) 0x20, (byte) 0x10,
            (byte) 0x08, (byte) 0x08, (byte) 0x2A, (byte) 0x1C, (byte) 0x08,
            (byte) 0x08, (byte) 0x1C, (byte) 0x2A, (byte) 0x08, (byte) 0x08,
            (byte) 0x1E, (byte) 0x10, (byte) 0x10, (byte) 0x10, (byte) 0x10,
            (byte) 0x0C, (byte) 0x1E, (byte) 0x0C, (byte) 0x1E, (byte) 0x0C,
            (byte) 0x30, (byte) 0x38, (byte) 0x3E, (byte) 0x38, (byte) 0x30,
            (byte) 0x06, (byte) 0x0E, (byte) 0x3E, (byte) 0x0E, (byte) 0x06,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x5F, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x07, (byte) 0x00, (byte) 0x07, (byte) 0x00,
            (byte) 0x14, (byte) 0x7F, (byte) 0x14, (byte) 0x7F, (byte) 0x14,
            (byte) 0x24, (byte) 0x2A, (byte) 0x7F, (byte) 0x2A, (byte) 0x12,
            (byte) 0x23, (byte) 0x13, (byte) 0x08, (byte) 0x64, (byte) 0x62,
            (byte) 0x36, (byte) 0x49, (byte) 0x56, (byte) 0x20, (byte) 0x50,
            (byte) 0x00, (byte) 0x08, (byte) 0x07, (byte) 0x03, (byte) 0x00,
            (byte) 0x00, (byte) 0x1C, (byte) 0x22, (byte) 0x41, (byte) 0x00,
            (byte) 0x00, (byte) 0x41, (byte) 0x22, (byte) 0x1C, (byte) 0x00,
            (byte) 0x2A, (byte) 0x1C, (byte) 0x7F, (byte) 0x1C, (byte) 0x2A,
            (byte) 0x08, (byte) 0x08, (byte) 0x3E, (byte) 0x08, (byte) 0x08,
            (byte) 0x00, (byte) 0x80, (byte) 0x70, (byte) 0x30, (byte) 0x00,
            (byte) 0x08, (byte) 0x08, (byte) 0x08, (byte) 0x08, (byte) 0x08,
            (byte) 0x00, (byte) 0x00, (byte) 0x60, (byte) 0x60, (byte) 0x00,
            (byte) 0x20, (byte) 0x10, (byte) 0x08, (byte) 0x04, (byte) 0x02,
            (byte) 0x3E, (byte) 0x51, (byte) 0x49, (byte) 0x45, (byte) 0x3E,
            (byte) 0x00, (byte) 0x42, (byte) 0x7F, (byte) 0x40, (byte) 0x00,
            (byte) 0x72, (byte) 0x49, (byte) 0x49, (byte) 0x49, (byte) 0x46,
            (byte) 0x21, (byte) 0x41, (byte) 0x49, (byte) 0x4D, (byte) 0x33,
            (byte) 0x18, (byte) 0x14, (byte) 0x12, (byte) 0x7F, (byte) 0x10,
            (byte) 0x27, (byte) 0x45, (byte) 0x45, (byte) 0x45, (byte) 0x39,
            (byte) 0x3C, (byte) 0x4A, (byte) 0x49, (byte) 0x49, (byte) 0x31,
            (byte) 0x41, (byte) 0x21, (byte) 0x11, (byte) 0x09, (byte) 0x07,
            (byte) 0x36, (byte) 0x49, (byte) 0x49, (byte) 0x49, (byte) 0x36,
            (byte) 0x46, (byte) 0x49, (byte) 0x49, (byte) 0x29, (byte) 0x1E,
            (byte) 0x00, (byte) 0x00, (byte) 0x14, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x40, (byte) 0x34, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x08, (byte) 0x14, (byte) 0x22, (byte) 0x41,
            (byte) 0x14, (byte) 0x14, (byte) 0x14, (byte) 0x14, (byte) 0x14,
            (byte) 0x00, (byte) 0x41, (byte) 0x22, (byte) 0x14, (byte) 0x08,
            (byte) 0x02, (byte) 0x01, (byte) 0x59, (byte) 0x09, (byte) 0x06,
            (byte) 0x3E, (byte) 0x41, (byte) 0x5D, (byte) 0x59, (byte) 0x4E,
            (byte) 0x7C, (byte) 0x12, (byte) 0x11, (byte) 0x12, (byte) 0x7C,
            (byte) 0x7F, (byte) 0x49, (byte) 0x49, (byte) 0x49, (byte) 0x36,
            (byte) 0x3E, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x22,
            (byte) 0x7F, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x3E,
            (byte) 0x7F, (byte) 0x49, (byte) 0x49, (byte) 0x49, (byte) 0x41,
            (byte) 0x7F, (byte) 0x09, (byte) 0x09, (byte) 0x09, (byte) 0x01,
            (byte) 0x3E, (byte) 0x41, (byte) 0x41, (byte) 0x51, (byte) 0x73,
            (byte) 0x7F, (byte) 0x08, (byte) 0x08, (byte) 0x08, (byte) 0x7F,
            (byte) 0x00, (byte) 0x41, (byte) 0x7F, (byte) 0x41, (byte) 0x00,
            (byte) 0x20, (byte) 0x40, (byte) 0x41, (byte) 0x3F, (byte) 0x01,
            (byte) 0x7F, (byte) 0x08, (byte) 0x14, (byte) 0x22, (byte) 0x41,
            (byte) 0x7F, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40,
            (byte) 0x7F, (byte) 0x02, (byte) 0x1C, (byte) 0x02, (byte) 0x7F,
            (byte) 0x7F, (byte) 0x04, (byte) 0x08, (byte) 0x10, (byte) 0x7F,
            (byte) 0x3E, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x3E,
            (byte) 0x7F, (byte) 0x09, (byte) 0x09, (byte) 0x09, (byte) 0x06,
            (byte) 0x3E, (byte) 0x41, (byte) 0x51, (byte) 0x21, (byte) 0x5E,
            (byte) 0x7F, (byte) 0x09, (byte) 0x19, (byte) 0x29, (byte) 0x46,
            (byte) 0x26, (byte) 0x49, (byte) 0x49, (byte) 0x49, (byte) 0x32,
            (byte) 0x03, (byte) 0x01, (byte) 0x7F, (byte) 0x01, (byte) 0x03,
            (byte) 0x3F, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x3F,
            (byte) 0x1F, (byte) 0x20, (byte) 0x40, (byte) 0x20, (byte) 0x1F,
            (byte) 0x3F, (byte) 0x40, (byte) 0x38, (byte) 0x40, (byte) 0x3F,
            (byte) 0x63, (byte) 0x14, (byte) 0x08, (byte) 0x14, (byte) 0x63,
            (byte) 0x03, (byte) 0x04, (byte) 0x78, (byte) 0x04, (byte) 0x03,
            (byte) 0x61, (byte) 0x59, (byte) 0x49, (byte) 0x4D, (byte) 0x43,
            (byte) 0x00, (byte) 0x7F, (byte) 0x41, (byte) 0x41, (byte) 0x41,
            (byte) 0x02, (byte) 0x04, (byte) 0x08, (byte) 0x10, (byte) 0x20,
            (byte) 0x00, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x7F,
            (byte) 0x04, (byte) 0x02, (byte) 0x01, (byte) 0x02, (byte) 0x04,
            (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40,
            (byte) 0x00, (byte) 0x03, (byte) 0x07, (byte) 0x08, (byte) 0x00,
            (byte) 0x20, (byte) 0x54, (byte) 0x54, (byte) 0x78, (byte) 0x40,
            (byte) 0x7F, (byte) 0x28, (byte) 0x44, (byte) 0x44, (byte) 0x38,
            (byte) 0x38, (byte) 0x44, (byte) 0x44, (byte) 0x44, (byte) 0x28,
            (byte) 0x38, (byte) 0x44, (byte) 0x44, (byte) 0x28, (byte) 0x7F,
            (byte) 0x38, (byte) 0x54, (byte) 0x54, (byte) 0x54, (byte) 0x18,
            (byte) 0x00, (byte) 0x08, (byte) 0x7E, (byte) 0x09, (byte) 0x02,
            (byte) 0x18, (byte) 0xA4, (byte) 0xA4, (byte) 0x9C, (byte) 0x78,
            (byte) 0x7F, (byte) 0x08, (byte) 0x04, (byte) 0x04, (byte) 0x78,
            (byte) 0x00, (byte) 0x44, (byte) 0x7D, (byte) 0x40, (byte) 0x00,
            (byte) 0x20, (byte) 0x40, (byte) 0x40, (byte) 0x3D, (byte) 0x00,
            (byte) 0x7F, (byte) 0x10, (byte) 0x28, (byte) 0x44, (byte) 0x00,
            (byte) 0x00, (byte) 0x41, (byte) 0x7F, (byte) 0x40, (byte) 0x00,
            (byte) 0x7C, (byte) 0x04, (byte) 0x78, (byte) 0x04, (byte) 0x78,
            (byte) 0x7C, (byte) 0x08, (byte) 0x04, (byte) 0x04, (byte) 0x78,
            (byte) 0x38, (byte) 0x44, (byte) 0x44, (byte) 0x44, (byte) 0x38,
            (byte) 0xFC, (byte) 0x18, (byte) 0x24, (byte) 0x24, (byte) 0x18,
            (byte) 0x18, (byte) 0x24, (byte) 0x24, (byte) 0x18, (byte) 0xFC,
            (byte) 0x7C, (byte) 0x08, (byte) 0x04, (byte) 0x04, (byte) 0x08,
            (byte) 0x48, (byte) 0x54, (byte) 0x54, (byte) 0x54, (byte) 0x24,
            (byte) 0x04, (byte) 0x04, (byte) 0x3F, (byte) 0x44, (byte) 0x24,
            (byte) 0x3C, (byte) 0x40, (byte) 0x40, (byte) 0x20, (byte) 0x7C,
            (byte) 0x1C, (byte) 0x20, (byte) 0x40, (byte) 0x20, (byte) 0x1C,
            (byte) 0x3C, (byte) 0x40, (byte) 0x30, (byte) 0x40, (byte) 0x3C,
            (byte) 0x44, (byte) 0x28, (byte) 0x10, (byte) 0x28, (byte) 0x44,
            (byte) 0x4C, (byte) 0x90, (byte) 0x90, (byte) 0x90, (byte) 0x7C,
            (byte) 0x44, (byte) 0x64, (byte) 0x54, (byte) 0x4C, (byte) 0x44,
            (byte) 0x00, (byte) 0x08, (byte) 0x36, (byte) 0x41, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x77, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x41, (byte) 0x36, (byte) 0x08, (byte) 0x00,
            (byte) 0x02, (byte) 0x01, (byte) 0x02, (byte) 0x04, (byte) 0x02,
            (byte) 0x3C, (byte) 0x26, (byte) 0x23, (byte) 0x26, (byte) 0x3C,
            (byte) 0x1E, (byte) 0xA1, (byte) 0xA1, (byte) 0x61, (byte) 0x12,
            (byte) 0x3A, (byte) 0x40, (byte) 0x40, (byte) 0x20, (byte) 0x7A,
            (byte) 0x38, (byte) 0x54, (byte) 0x54, (byte) 0x55, (byte) 0x59,
            (byte) 0x21, (byte) 0x55, (byte) 0x55, (byte) 0x79, (byte) 0x41,
            (byte) 0x21, (byte) 0x54, (byte) 0x54, (byte) 0x78, (byte) 0x41,
            (byte) 0x21, (byte) 0x55, (byte) 0x54, (byte) 0x78, (byte) 0x40,
            (byte) 0x20, (byte) 0x54, (byte) 0x55, (byte) 0x79, (byte) 0x40,
            (byte) 0x0C, (byte) 0x1E, (byte) 0x52, (byte) 0x72, (byte) 0x12,
            (byte) 0x39, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x59,
            (byte) 0x39, (byte) 0x54, (byte) 0x54, (byte) 0x54, (byte) 0x59,
            (byte) 0x39, (byte) 0x55, (byte) 0x54, (byte) 0x54, (byte) 0x58,
            (byte) 0x00, (byte) 0x00, (byte) 0x45, (byte) 0x7C, (byte) 0x41,
            (byte) 0x00, (byte) 0x02, (byte) 0x45, (byte) 0x7D, (byte) 0x42,
            (byte) 0x00, (byte) 0x01, (byte) 0x45, (byte) 0x7C, (byte) 0x40,
            (byte) 0xF0, (byte) 0x29, (byte) 0x24, (byte) 0x29, (byte) 0xF0,
            (byte) 0xF0, (byte) 0x28, (byte) 0x25, (byte) 0x28, (byte) 0xF0,
            (byte) 0x7C, (byte) 0x54, (byte) 0x55, (byte) 0x45, (byte) 0x00,
            (byte) 0x20, (byte) 0x54, (byte) 0x54, (byte) 0x7C, (byte) 0x54,
            (byte) 0x7C, (byte) 0x0A, (byte) 0x09, (byte) 0x7F, (byte) 0x49,
            (byte) 0x32, (byte) 0x49, (byte) 0x49, (byte) 0x49, (byte) 0x32,
            (byte) 0x32, (byte) 0x48, (byte) 0x48, (byte) 0x48, (byte) 0x32,
            (byte) 0x32, (byte) 0x4A, (byte) 0x48, (byte) 0x48, (byte) 0x30,
            (byte) 0x3A, (byte) 0x41, (byte) 0x41, (byte) 0x21, (byte) 0x7A,
            (byte) 0x3A, (byte) 0x42, (byte) 0x40, (byte) 0x20, (byte) 0x78,
            (byte) 0x00, (byte) 0x9D, (byte) 0xA0, (byte) 0xA0, (byte) 0x7D,
            (byte) 0x39, (byte) 0x44, (byte) 0x44, (byte) 0x44, (byte) 0x39,
            (byte) 0x3D, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x3D,
            (byte) 0x3C, (byte) 0x24, (byte) 0xFF, (byte) 0x24, (byte) 0x24,
            (byte) 0x48, (byte) 0x7E, (byte) 0x49, (byte) 0x43, (byte) 0x66,
            (byte) 0x2B, (byte) 0x2F, (byte) 0xFC, (byte) 0x2F, (byte) 0x2B,
            (byte) 0xFF, (byte) 0x09, (byte) 0x29, (byte) 0xF6, (byte) 0x20,
            (byte) 0xC0, (byte) 0x88, (byte) 0x7E, (byte) 0x09, (byte) 0x03,
            (byte) 0x20, (byte) 0x54, (byte) 0x54, (byte) 0x79, (byte) 0x41,
            (byte) 0x00, (byte) 0x00, (byte) 0x44, (byte) 0x7D, (byte) 0x41,
            (byte) 0x30, (byte) 0x48, (byte) 0x48, (byte) 0x4A, (byte) 0x32,
            (byte) 0x38, (byte) 0x40, (byte) 0x40, (byte) 0x22, (byte) 0x7A,
            (byte) 0x00, (byte) 0x7A, (byte) 0x0A, (byte) 0x0A, (byte) 0x72,
            (byte) 0x7D, (byte) 0x0D, (byte) 0x19, (byte) 0x31, (byte) 0x7D,
            (byte) 0x26, (byte) 0x29, (byte) 0x29, (byte) 0x2F, (byte) 0x28,
            (byte) 0x26, (byte) 0x29, (byte) 0x29, (byte) 0x29, (byte) 0x26,
            (byte) 0x30, (byte) 0x48, (byte) 0x4D, (byte) 0x40, (byte) 0x20,
            (byte) 0x38, (byte) 0x08, (byte) 0x08, (byte) 0x08, (byte) 0x08,
            (byte) 0x08, (byte) 0x08, (byte) 0x08, (byte) 0x08, (byte) 0x38,
            (byte) 0x2F, (byte) 0x10, (byte) 0xC8, (byte) 0xAC, (byte) 0xBA,
            (byte) 0x2F, (byte) 0x10, (byte) 0x28, (byte) 0x34, (byte) 0xFA,
            (byte) 0x00, (byte) 0x00, (byte) 0x7B, (byte) 0x00, (byte) 0x00,
            (byte) 0x08, (byte) 0x14, (byte) 0x2A, (byte) 0x14, (byte) 0x22,
            (byte) 0x22, (byte) 0x14, (byte) 0x2A, (byte) 0x14, (byte) 0x08,
            (byte) 0xAA, (byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0xAA,
            (byte) 0xAA, (byte) 0x55, (byte) 0xAA, (byte) 0x55, (byte) 0xAA,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00,
            (byte) 0x10, (byte) 0x10, (byte) 0x10, (byte) 0xFF, (byte) 0x00,
            (byte) 0x14, (byte) 0x14, (byte) 0x14, (byte) 0xFF, (byte) 0x00,
            (byte) 0x10, (byte) 0x10, (byte) 0xFF, (byte) 0x00, (byte) 0xFF,
            (byte) 0x10, (byte) 0x10, (byte) 0xF0, (byte) 0x10, (byte) 0xF0,
            (byte) 0x14, (byte) 0x14, (byte) 0x14, (byte) 0xFC, (byte) 0x00,
            (byte) 0x14, (byte) 0x14, (byte) 0xF7, (byte) 0x00, (byte) 0xFF,
            (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0xFF,
            (byte) 0x14, (byte) 0x14, (byte) 0xF4, (byte) 0x04, (byte) 0xFC,
            (byte) 0x14, (byte) 0x14, (byte) 0x17, (byte) 0x10, (byte) 0x1F,
            (byte) 0x10, (byte) 0x10, (byte) 0x1F, (byte) 0x10, (byte) 0x1F,
            (byte) 0x14, (byte) 0x14, (byte) 0x14, (byte) 0x1F, (byte) 0x00,
            (byte) 0x10, (byte) 0x10, (byte) 0x10, (byte) 0xF0, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1F, (byte) 0x10,
            (byte) 0x10, (byte) 0x10, (byte) 0x10, (byte) 0x1F, (byte) 0x10,
            (byte) 0x10, (byte) 0x10, (byte) 0x10, (byte) 0xF0, (byte) 0x10,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x10,
            (byte) 0x10, (byte) 0x10, (byte) 0x10, (byte) 0x10, (byte) 0x10,
            (byte) 0x10, (byte) 0x10, (byte) 0x10, (byte) 0xFF, (byte) 0x10,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x14,
            (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0xFF,
            (byte) 0x00, (byte) 0x00, (byte) 0x1F, (byte) 0x10, (byte) 0x17,
            (byte) 0x00, (byte) 0x00, (byte) 0xFC, (byte) 0x04, (byte) 0xF4,
            (byte) 0x14, (byte) 0x14, (byte) 0x17, (byte) 0x10, (byte) 0x17,
            (byte) 0x14, (byte) 0x14, (byte) 0xF4, (byte) 0x04, (byte) 0xF4,
            (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0xF7,
            (byte) 0x14, (byte) 0x14, (byte) 0x14, (byte) 0x14, (byte) 0x14,
            (byte) 0x14, (byte) 0x14, (byte) 0xF7, (byte) 0x00, (byte) 0xF7,
            (byte) 0x14, (byte) 0x14, (byte) 0x14, (byte) 0x17, (byte) 0x14,
            (byte) 0x10, (byte) 0x10, (byte) 0x1F, (byte) 0x10, (byte) 0x1F,
            (byte) 0x14, (byte) 0x14, (byte) 0x14, (byte) 0xF4, (byte) 0x14,
            (byte) 0x10, (byte) 0x10, (byte) 0xF0, (byte) 0x10, (byte) 0xF0,
            (byte) 0x00, (byte) 0x00, (byte) 0x1F, (byte) 0x10, (byte) 0x1F,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1F, (byte) 0x14,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFC, (byte) 0x14,
            (byte) 0x00, (byte) 0x00, (byte) 0xF0, (byte) 0x10, (byte) 0xF0,
            (byte) 0x10, (byte) 0x10, (byte) 0xFF, (byte) 0x10, (byte) 0xFF,
            (byte) 0x14, (byte) 0x14, (byte) 0x14, (byte) 0xFF, (byte) 0x14,
            (byte) 0x10, (byte) 0x10, (byte) 0x10, (byte) 0x1F, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xF0, (byte) 0x10,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xF0, (byte) 0xF0, (byte) 0xF0, (byte) 0xF0, (byte) 0xF0,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF,
            (byte) 0x0F, (byte) 0x0F, (byte) 0x0F, (byte) 0x0F, (byte) 0x0F,
            (byte) 0x38, (byte) 0x44, (byte) 0x44, (byte) 0x38, (byte) 0x44,
            (byte) 0x7C, (byte) 0x2A, (byte) 0x2A, (byte) 0x3E, (byte) 0x14,
            (byte) 0x7E, (byte) 0x02, (byte) 0x02, (byte) 0x06, (byte) 0x06,
            (byte) 0x02, (byte) 0x7E, (byte) 0x02, (byte) 0x7E, (byte) 0x02,
            (byte) 0x63, (byte) 0x55, (byte) 0x49, (byte) 0x41, (byte) 0x63,
            (byte) 0x38, (byte) 0x44, (byte) 0x44, (byte) 0x3C, (byte) 0x04,
            (byte) 0x40, (byte) 0x7E, (byte) 0x20, (byte) 0x1E, (byte) 0x20,
            (byte) 0x06, (byte) 0x02, (byte) 0x7E, (byte) 0x02, (byte) 0x02,
            (byte) 0x99, (byte) 0xA5, (byte) 0xE7, (byte) 0xA5, (byte) 0x99,
            (byte) 0x1C, (byte) 0x2A, (byte) 0x49, (byte) 0x2A, (byte) 0x1C,
            (byte) 0x4C, (byte) 0x72, (byte) 0x01, (byte) 0x72, (byte) 0x4C,
            (byte) 0x30, (byte) 0x4A, (byte) 0x4D, (byte) 0x4D, (byte) 0x30,
            (byte) 0x30, (byte) 0x48, (byte) 0x78, (byte) 0x48, (byte) 0x30,
            (byte) 0xBC, (byte) 0x62, (byte) 0x5A, (byte) 0x46, (byte) 0x3D,
            (byte) 0x3E, (byte) 0x49, (byte) 0x49, (byte) 0x49, (byte) 0x00,
            (byte) 0x7E, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x7E,
            (byte) 0x2A, (byte) 0x2A, (byte) 0x2A, (byte) 0x2A, (byte) 0x2A,
            (byte) 0x44, (byte) 0x44, (byte) 0x5F, (byte) 0x44, (byte) 0x44,
            (byte) 0x40, (byte) 0x51, (byte) 0x4A, (byte) 0x44, (byte) 0x40,
            (byte) 0x40, (byte) 0x44, (byte) 0x4A, (byte) 0x51, (byte) 0x40,
            (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x01, (byte) 0x03,
            (byte) 0xE0, (byte) 0x80, (byte) 0xFF, (byte) 0x00, (byte) 0x00,
            (byte) 0x08, (byte) 0x08, (byte) 0x6B, (byte) 0x6B, (byte) 0x08,
            (byte) 0x36, (byte) 0x12, (byte) 0x36, (byte) 0x24, (byte) 0x36,
            (byte) 0x06, (byte) 0x0F, (byte) 0x09, (byte) 0x0F, (byte) 0x06,
            (byte) 0x00, (byte) 0x00, (byte) 0x18, (byte) 0x18, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x10, (byte) 0x10, (byte) 0x00,
            (byte) 0x30, (byte) 0x40, (byte) 0xFF, (byte) 0x01, (byte) 0x01,
            (byte) 0x00, (byte) 0x1F, (byte) 0x01, (byte) 0x01, (byte) 0x1E,
            (byte) 0x00, (byte) 0x19, (byte) 0x1D, (byte) 0x17, (byte) 0x12,
            (byte) 0x00, (byte) 0x3C, (byte) 0x3C, (byte) 0x3C, (byte) 0x3C,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
    };
}

