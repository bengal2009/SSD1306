/**
 * Created by Lin on 2015/7/18.
 */
public class test {
    public static void main(String[] args) throws InterruptedException {
        try
        {
            SSD1306Lib T1=new SSD1306Lib();
            /*T1.clear();
            T1.drawString("1234",0,0,true);*/
            T1.drawStringCentered("Hello World!", 1, true);
            T1.drawStringCentered("Hello World!", 25, true);
            T1.drawStringCentered("Hello World!", 49, true);
            T1.update();
            Thread.sleep(10000);
        }catch (Exception E)
        {

        }
    }
}
