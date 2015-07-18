import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BasicGraphics {
    private static I2CBus bus;
    private static I2CDevice device;
    private static Constants SSDConst = new Constants();

    public static void main(String[] args) throws IOException {
        bus = I2CFactory.getInstance(I2CBus.BUS_1);
        device = bus.getDevice(Constants.SSD1306_I2C_ADDRESS);
        try {
//            Display disp = new Display(128, 64, GpioFactory.getInstance(),I2CFactory.getInstance(I2CBus.BUS_1), 0x3c);
            SSD1306Lib disp = new SSD1306Lib();
        /*public Display(int width, int height, GpioController gpio, I2CBus i2c, int address) throws ReflectiveOperationException, IOException {
            this(width, height, gpio, i2c, address, null);
        }*/
        /*public Display(int width, int height, GpioController gpio, I2CBus i2c, int address, Pin rstPin) throws ReflectiveOperationException, IOException {
            this(width, height, true, gpio, rstPin);  */
            /*Display disp = new Display(128, 64, GpioFactory.getInstance(),
                    SpiFactory.getInstance(SpiChannel.CS1, 8000000), RaspiPin.GPIO_03, RaspiPin.GPIO_04);*/
//        public Display(int width, int height, GpioController gpio, SpiDevice spi, Pin rstPin, Pin dcPin)
//        public Display(int width, int height, GpioController gpio, I2CBus i2c, int address, Pin rstPin)
            // Create 128x64 display on CE1 (change to SpiChannel.CS0 for using CE0) with D/C pin on WiringPi pin 04
        /*public Display(int width, int height, GpioController gpio, SpiDevice spi, Pin dcPin) {
            this(width, height, gpio, spi, null, dcPin);
        }*/

//        disp.begin();
            // Init the display

//        Image i = ImageIO.read(BasicGraphics.class.getResourceAsStream("Pict/lord.png"));
//            BufferedImage i = ImageIO.read("Pict/lord.png");
            BufferedImage i = ImageIO.read(new File("Pict/lord.png"));

        /*disp.getGraphics().setColor(Color.WHITE);
        disp.getGraphics().drawImage(i, 0, 0, null);
        disp.getGraphics().setFont(new Font("Monospaced", Font.PLAIN, 10));
        disp.getGraphics().drawString("Praise him", 64, 60);
        disp.drawRect(0, 0, disp.getWidth() - 1, disp.getHeight() - 1);
        // Deal with the image using AWT

        disp.displayImage();*/
            // Copy AWT image to an inner buffer and send to the display
            disp.drawImage(i, 0, 0);
            disp.update();
            Thread.sleep(5000);
            for (int x = 70; x < 90; x += 2) {
                for (int y = 10; y < 30; y += 2) {
                    disp.setPixel(x, y, true);
                }
            }
            // Set some pixels in the buffer manually

            disp.update();

            // Send the buffer to the display again, now with the modified pixels

            while (true) {
                Thread.sleep(1000);
            }
        } catch (Exception E) {

        }

    }
}
