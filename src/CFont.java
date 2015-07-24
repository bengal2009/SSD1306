/**
 * Created by Lin on 2015/7/24.
 */
public class CFont {

   private static final int SSD1306_SETCONTRAST =0x81;
    private static final int SSD1306_DISPLAYALLON_RESUME= 0xA4;
           private static final int SSD1306_DISPLAYALLON= 0xA5;
           private static final int SSD1306_NORMALDISPLAY= 0xA6;
           private static final int SSD1306_INVERTDISPLAY= 0xA7;
           private static final int SSD1306_DISPLAYOFF= 0xAE;
           private static final int SSD1306_DISPLAYON= 0xAF;

           private static final int SSD1306_SETDISPLAYOFFSET= 0xD3;
           private static final int SSD1306_SETCOMPINS= 0xDA;

           private static final int SSD1306_SETVCOMDETECT= 0xDB;

           private static final int SSD1306_SETDISPLAYCLOCKDIV= 0xD5;
           private static final int SSD1306_SETPRECHARGE= 0xD9;

           private static final int SSD1306_SETMULTIPLEX =0xA8;

           private static final int SSD1306_SETLOWCOLUMN= 0x00;
           private static final int SSD1306_SETHIGHCOLUMN =0x10;

           private static final int SSD1306_SETSTARTLINE =0x40;

           private static final int SSD1306_MEMORYMODE =0x20;

           private static final int SSD1306_COMSCANINC= 0xC0;
           private static final int SSD1306_COMSCANDEC =0xC8;

           private static final int SSD1306_SEGREMAP= 0xA0;

           private static final int SSD1306_CHARGEPUMP= 0x8D;

           private static final int SSD1306_EXTERNALVCC= 0x1;
           private static final int SSD1306_SWITCHCAPVCC= 0x2;

// Scrollingprivate static final ints
           private static final int SSD1306_ACTIVATE_SCROLL= 0x2F;
           private static final int SSD1306_DEACTIVATE_SCROLL= 0x2E;
           private static final int SSD1306_SET_VERTICAL_SCROLL_AREA= 0xA3;
           private static final int SSD1306_RIGHT_HORIZONTAL_SCROLL= 0x26;
           private static final int SSD1306_LEFT_HORIZONTAL_SCROLL= 0x27;
           private static final int SSD1306_VERTICAL_AND_RIGHT_HORIZONTAL_SCROLL= 0x29;
           private static final int SSD1306_VERTICAL_AND_LEFT_HORIZONTAL_SCROLL= 0x2A;
    private static SSD1306Lib SSD1;
    public static void main(String[] args) throws Exception {
        SSD1= new SSD1306Lib();
        boolean DefaultInv=false;
        java.util.Scanner sc=new java.util.Scanner(System.in);
        SSD1.ReadFontFile("/home/pi/prog/2.TXT");
        SSD1.clear();
        for (Integer i = 1; i < 5; i++) {
            if(i==1){
                SSD1.DispStr(16, (i - 1) * 16, i.toString() + ".林郁庭ABCD",true);
            }
            else{
            SSD1.DispStr(16, (i - 1) * 16, i.toString() + ".林郁庭ABCD",false);
            }
        }
        SSD1.update();
        /*for(Integer j=0;j<8;j++) {
            SSD1.clear();
            for (Integer i = 1; i < 5; i++) {
                SSD1.DispStr(16, (i - 1) * 16, i + ".林郁庭ABCD");
            }
            SSD1.update();
            Thread.sleep(2000);
            startscrollright(0x00, j);
            System.out.println("J:"+j);
            Thread.sleep(2000);
            stopscroll();
        }*/
        /*drawCircle(50, 50, 10);
        drawLine(0,0,100,30);
        SSD1.update();
        startscrollright(0x00, 0x0f);
        Thread.sleep(2000);
        stopscroll();
        *//*startscrollleft(0x00, 0x0A);
        Thread.sleep(7000);*//*
        startscrolldiagright(0x00, 0x0f);
        Thread.sleep(5000);
        stopscroll();
        System.out.println("Scroll down!");
        invertDisplay(true);
        Thread.sleep(5000);
        invertDisplay(false);*/
        char ch = (char) System.in.read();

//        waitForKeypress();

    }
    public static void invertDisplay(boolean Inv)
    {
        try {
            if (Inv) {
                SSD1.writeCommand((byte) SSD1306_INVERTDISPLAY);
            } else {
                SSD1.writeCommand((byte) SSD1306_NORMALDISPLAY);
            }
        }catch (Exception E)
        {

        }

    }
    // startscrollright
// Activate a right handed scroll for rows start through stop
// Hint, the display is 16 rows tall. To scroll the whole display, run:
// display.scrollright(0x00, 0x0F)
    public static void startscrollright(int start,int endpix )
    {
        try {
            SSD1.writeCommand((byte) SSD1306_RIGHT_HORIZONTAL_SCROLL);
            SSD1.writeCommand((byte)0X00);
            SSD1.writeCommand((byte)start);
            SSD1.writeCommand((byte)0X00);
            SSD1.writeCommand((byte)endpix);
            SSD1.writeCommand((byte)0X01);
            SSD1.writeCommand((byte)0XFF);
            SSD1.writeCommand((byte)SSD1306_ACTIVATE_SCROLL);
        }catch (Exception E)
        {

        }

    }
    public static void startscrollleft(int start,int stop )
    {
        try {
            SSD1.writeCommand((byte) SSD1306_LEFT_HORIZONTAL_SCROLL);
            SSD1.writeCommand((byte)0X00);
            SSD1.writeCommand((byte)start);
            SSD1.writeCommand((byte)0X00);
            SSD1.writeCommand((byte)stop);
            SSD1.writeCommand((byte)0X01);
            SSD1.writeCommand((byte)0XFF);
            SSD1.writeCommand((byte)SSD1306_ACTIVATE_SCROLL);
        }catch (Exception E)
        {

        }


    }
    // startscrolldiagright
// Activate a diagonal scroll for rows start through stop
// Hint, the display is 16 rows tall. To scroll the whole display, run:
// display.scrollright(0x00, 0x0F)
    public static void startscrolldiagright(int start,int stop )
    {
        try {
            SSD1.writeCommand((byte) SSD1306_SET_VERTICAL_SCROLL_AREA);
            SSD1.writeCommand((byte) 0X00);
            SSD1.writeCommand((byte) 64);
            SSD1.writeCommand((byte) SSD1306_VERTICAL_AND_RIGHT_HORIZONTAL_SCROLL);
            SSD1.writeCommand((byte)0X00);
            SSD1.writeCommand((byte) start);
            SSD1.writeCommand((byte) 0X00);
            SSD1.writeCommand((byte) stop);
            SSD1.writeCommand((byte) 0X01);
            SSD1.writeCommand((byte) SSD1306_ACTIVATE_SCROLL);
        }catch (Exception E)
        {

        }
    }
    public static void startscrolldiagleft(int start,int stop )
    {
        try {
            SSD1.writeCommand((byte) SSD1306_SET_VERTICAL_SCROLL_AREA);
            SSD1.writeCommand((byte) 0X00);
            SSD1.writeCommand((byte) 64);
            SSD1.writeCommand((byte) SSD1306_VERTICAL_AND_LEFT_HORIZONTAL_SCROLL);
            SSD1.writeCommand((byte)0X00);
            SSD1.writeCommand((byte) start);
            SSD1.writeCommand((byte) 0X00);
            SSD1.writeCommand((byte) stop);
            SSD1.writeCommand((byte) 0X01);
            SSD1.writeCommand((byte) SSD1306_ACTIVATE_SCROLL);
        }catch (Exception E)
        {

        }
    }
    public static void  stopscroll(){
        try {
            SSD1.writeCommand((byte) SSD1306_DEACTIVATE_SCROLL);
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
    public static void drawCircle(Integer x0, Integer y0, Integer r
              ) {
        Integer f = 1 - r;
        Integer ddF_x = 1;
        Integer ddF_y = -2 * r;
        Integer x = 0;
        Integer y = r;

        SSD1.setPixel(x0, y0 + r, true);
        SSD1.setPixel(x0, y0 - r,true);
        SSD1.setPixel(x0 + r, y0,true  );
        SSD1.setPixel(x0 - r, y0 ,true );

        while (x<y) {
            if (f >= 0) {
                y--;
                ddF_y += 2;
                f += ddF_y;
            }
            x++;
            ddF_x += 2;
            f += ddF_x;

            SSD1.setPixel(x0 + x, y0 + y,true);
            SSD1.setPixel(x0 - x, y0 + y,true);
            SSD1.setPixel(x0 + x, y0 - y,true);
            SSD1.setPixel(x0 - x, y0 - y,true);
            SSD1.setPixel(x0 + y, y0 + x,true);
            SSD1.setPixel(x0 - y, y0 + x,true);
            SSD1.setPixel(x0 + y, y0 - x,true);
            SSD1.setPixel(x0 - y, y0 - x,true);
        }
    }
    public static void drawLine(Integer x0, Integer y0,
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
                SSD1.setPixel(y0, x0, true);
            } else {
                SSD1.setPixel(x0, y0, true);
            }
            err -= dy;
            if (err < 0) {
                y0 += ystep;
                err += dx;
            }
        }
    }


}
