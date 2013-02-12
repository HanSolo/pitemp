package eu.hansolo.fx.pi;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.TooManyListenersException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;


public class SerialComm {
    public static final String    PI_PORT      = "/dev/ttyUSB0";
    private static final int      TIME_OUT     = 5000;
    private static final int      DATA_RATE    = 9600;
    private static final Pattern  PATTERN      = Pattern.compile("-?[\\d\\.]+");
    private static final Matcher  MATCHER      = PATTERN.matcher("");
    private DoubleProperty        celsius;
    private CommPort              commPort;
    private InputStream           inputStream;
    private BufferedReader        portReader;


    // ******************** Constructor ***************************************
    public SerialComm() {
        celsius = new SimpleDoubleProperty(0);
        connect(PI_PORT);
    }


    // ******************** Methods *******************************************
    private void connect(final String PORT_NAME) {
        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(PORT_NAME);
            if (portIdentifier.isCurrentlyOwned()) {
                System.out.println("Error: Port is currently in use");
            } else {
                commPort = portIdentifier.open(getClass().getName(), TIME_OUT);
                if (commPort instanceof SerialPort) {
                    SerialPort serialPort = (SerialPort) commPort;
                    serialPort.setSerialPortParams(DATA_RATE,
                                                   SerialPort.DATABITS_8,
                                                   SerialPort.STOPBITS_1,
                                                   SerialPort.PARITY_NONE);

                    serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                    serialPort.setDTR(true);
                    serialPort.disableReceiveTimeout();
                    serialPort.enableReceiveThreshold(1);

                    inputStream = serialPort.getInputStream();
                    portReader  = new BufferedReader(new InputStreamReader(inputStream));
                    try {
                        serialPort.addEventListener(new SerialPortEventListener(){
                            @Override public void serialEvent(SerialPortEvent event) {
                                if (SerialPortEvent.DATA_AVAILABLE == event.getEventType()) {
                                    readSerial();
                                }
                            }
                        });
                        serialPort.notifyOnDataAvailable(true);
                    } catch (TooManyListenersException exception) {}

                }
            }
        } catch (gnu.io.NoSuchPortException | gnu.io.PortInUseException | gnu.io.UnsupportedCommOperationException | java.io.IOException exception) {
            System.out.println("Error connecting to serial port: " + exception);
        }
    }

    private void readSerial() {
        try {
            if (inputStream.available() > 0) {
                MATCHER.reset(portReader.readLine());
                while(MATCHER.find()) {
                    celsius.set(Double.parseDouble(MATCHER.group()));
                }
            }
        } catch (IOException exception) {}
    }

    public ReadOnlyDoubleProperty celsiusProperty() {
        return celsius;
    }
}
