package eu.hansolo.fx.pi;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;


public class Main {
    private static final double THRESHOLD = 24.0;
    private SerialComm sensor;


    public Main() {
        sensor = new SerialComm();
        initSensor();
    }

    private void initSensor() {
        sensor.celsiusProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
                if (newValue.doubleValue() > THRESHOLD) {
                    System.out.println("Temperature (" + newValue.toString() + ") exceeds threshold of " + THRESHOLD + " °C !");
                }
            }
        });
    }

    public static void main(String[] args) {
        Main app = new Main();
    }
}
