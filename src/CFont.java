import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.util.HashMap;

/**
 * Created by Lin on 2015/7/24.
 */
public class CFont {



// Scrollingprivate static final ints
            private static final byte SSD1306_NORMALDISPLAY = (byte) 0xA6;
            private static final byte SSD1306_INVERTDISPLAY = (byte) 0xA7;
           private static final int SSD1306_ACTIVATE_SCROLL= 0x2F;
           private static final int SSD1306_DEACTIVATE_SCROLL= 0x2E;
           private static final int SSD1306_SET_VERTICAL_SCROLL_AREA= 0xA3;
           private static final int SSD1306_RIGHT_HORIZONTAL_SCROLL= 0x26;
           private static final int SSD1306_LEFT_HORIZONTAL_SCROLL= 0x27;
           private static final int SSD1306_VERTICAL_AND_RIGHT_HORIZONTAL_SCROLL= 0x29;
           private static final int SSD1306_VERTICAL_AND_LEFT_HORIZONTAL_SCROLL= 0x2A;
            private static final int Mnu_Music = 1;
            private static final int Mnu_Weather = 2;
            private static final int Mnu_Led = 3;
            private static final int Mnu_Led1 = 4;
            private static final int Mnu_Son = 5;
            private static final int Mnu_Exit = 6;

            private static SSD1306Lib SSD1;
            private static int PageIndex=1,MenuIndex=1;
            private static boolean RunMode=false;

    static GpioController gpio = GpioFactory.getInstance();
    private static HashMap<Integer,String> MenuHash=new HashMap<Integer,String>();
    public static void main(String[] args) throws Exception {
        SSD1= new SSD1306Lib();
        GpioPinDigitalInput[] pins = {
                gpio.provisionDigitalInputPin(RaspiPin.GPIO_13,"ENTER", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(RaspiPin.GPIO_12,"SEL", PinPullResistance.PULL_UP),

        };
        boolean DefaultInv=false;
        java.util.Scanner sc=new java.util.Scanner(System.in);
        SSD1.ReadFontFile("/home/pi/prog/2.TXT");
        MenuInit();
        MenuDisplay();

        // create GPIO listener
        GpioPinListenerDigital listener  = new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                // display pin state on console
                if(event.getState().toString()=="LOW") {
//                    System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState()+":"+event.getPin().getName() );

                    try {
                        if(event.getPin().getName()=="SEL"& RunMode==false){
                            MenuIndex++;

//                            System.out.println("MenuHashSize:"+MenuHash.size());
                            if(MenuIndex>MenuHash.size()) MenuIndex=1;
                            MenuDisplay();
                        }
                        if(event.getPin().getName()=="ENTER"){
                            if(RunMode==true) {
                                MenuDisplay();
                                RunMode=!RunMode;
                            }else {
                                MenuAction();
                            }
                        }



                    }
                    catch (Exception E)
                    {

                    }
                }
            }
        };
        gpio.addListener(listener, pins);

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
    private static void MenuAction()throws Exception {
        switch(MenuIndex)
        {
            case Mnu_Music:
                System.out.println( MenuHash.get(Mnu_Music));
                break;
            case Mnu_Weather:
                System.out.println( MenuHash.get(Mnu_Weather));
                break;
            case Mnu_Led:
                System.out.println( MenuHash.get(Mnu_Led));
                break;
            case Mnu_Led1:
                System.out.println(MenuHash.get(Mnu_Led1));
                SSD1.clear();
                SSD1.DispStr(32, 16, "11111", false);
                SSD1.update();
                RunMode=!RunMode;

                break;
            case Mnu_Son:
                System.out.println( MenuHash.get(Mnu_Son));
                break;
            case Mnu_Exit:
                System.out.println( MenuHash.get(Mnu_Exit));
                System.exit(1) ;
                break;

        }
    }
  private static void MenuInit()throws Exception {
        MenuHash.put(Mnu_Music,"Music");
        MenuHash.put(Mnu_Weather, "Weather");
        MenuHash.put(Mnu_Led, "Led");
        MenuHash.put(Mnu_Led1,"Led1");
      MenuHash.put(Mnu_Son,"林郁庭ABCD");
        MenuHash.put(Mnu_Exit, "Exit");


    }
    private static void MenuDisplay()  throws Exception
    {
        SSD1.clear();
        System.out.println(MenuHash.size() + MenuHash.get(1) + "MenuIndex:" + MenuIndex);
        Integer High=0;
        PageIndex=MenuIndex/5;
        for (Integer i = (PageIndex)*4+1; i <  (PageIndex)*4+5; i++) {
            if(i<=MenuHash.size()) {

                if (i == MenuIndex) {

                    SSD1.DispStr(16, High, MenuHash.get(i), true);
                } else {
                    SSD1.DispStr(16, High, MenuHash.get(i), false);
                }
            }
            High+=16;
        }
        SSD1.update();
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
