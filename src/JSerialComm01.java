
//package jserialcomm01;

import java.io.IOException;
import java.io.InputStream;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.fazecast.jSerialComm.SerialPort;

import components.map.Map;
import components.map.Map1L;
import components.queue.Queue;
import components.queue.Queue1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;

/**
 *
 * @author Zhengzhe Wei
 */
public class JSerialComm01 {

    public static String devicePortName = "IOUSBHostDevice";
    public static SerialPort arduinoPort = null;
    public static InputStream arduinoStream = null;
    public static int PACKET_SIZE_IN_BYTES = 8;

    public static Map<String, String> includeCommandInMap() {
        Map<String, String> cmdLookUp = new Map1L<>();
        cmdLookUp.add("16", "a");
        cmdLookUp.add("15", "b");
        cmdLookUp.add("14", "c");
        cmdLookUp.add("13", "d");
        cmdLookUp.add("12", "e");
        cmdLookUp.add("11", "f");
        cmdLookUp.add("10", "g");
        cmdLookUp.add("9", "h");
        cmdLookUp.add("8", "i");
        return cmdLookUp;
    }

    public static void generateIdealQueue(String bitsUnderTest,
            Queue<String> idealReading) {
        long numOfLevel = 65535;
        double i = 0;
        double DUTlevels = Math.pow(2, Double.parseDouble(bitsUnderTest));
        double incrementInterval = 65536.0 / DUTlevels;
        while (i <= numOfLevel) {

            idealReading.enqueue(Double.toString(i));
            i = i + incrementInterval;
        }
    }

    public static void generateDataFromShield(Map<String, String> cmdLookUp,
            String bitsUnderTest, SerialManager hello,
            Queue<String> idealReading, Queue<String> adcInputCollection) {
        long numOfLevel = 65535;
        double i = 0;
        double DUTlevels = Math.pow(2, Double.parseDouble(bitsUnderTest));
        double incrementInterval = 65536.0 / DUTlevels;
        while (i <= numOfLevel) {

            idealReading.enqueue(Double.toString(i));
            i = i + incrementInterval;
        }
        hello.writeToArduino(cmdLookUp.value(bitsUnderTest));

        String inputEntry = hello.getReply();
        System.out.println(inputEntry);
        boolean notEOS = true;
        while (notEOS) {

            adcInputCollection.enqueue(inputEntry);
            inputEntry = hello.getReply();
            System.out.println(inputEntry);
            if (inputEntry.charAt(0) == 'f') {
                //  hello.closeSerial();
                notEOS = false;
            }
        }
    }

    ////////////
    public static Map<String, String> mapNonMonotonicRegion(
            Map<String, String> cmdLookUp, SerialManager hello,
            Queue<String> idealReadingMasterTape,
            Queue<String> adcInputCollectionMasterTape, int bits) {

        Queue<String> idealReading = idealReadingMasterTape.newInstance();
        Queue<String> adcInputCollection = adcInputCollectionMasterTape
                .newInstance();
        Queue<String> nonMonotonicLog = idealReadingMasterTape.newInstance();
        idealReading.transferFrom(idealReadingMasterTape);
        adcInputCollection.transferFrom(adcInputCollectionMasterTape);
        boolean monotonic = true;
        Map<String, String> faultRegionMap = new Map1L<>();
        generateDataFromShield(cmdLookUp, String.valueOf(bits), hello,
                idealReading, adcInputCollection);
        while (adcInputCollection.length() >= 2) {
            String firstValue = adcInputCollection.dequeue();
            String secondValue = adcInputCollection.dequeue();
            adcInputCollectionMasterTape.enqueue(firstValue);
            adcInputCollectionMasterTape.enqueue(secondValue);

            String firstIdealValue = idealReading.dequeue();
            String secondIdealValue = idealReading.dequeue();
            idealReadingMasterTape.enqueue(firstIdealValue);
            idealReadingMasterTape.enqueue(secondIdealValue);

            if (Double.parseDouble(firstValue) > Double
                    .parseDouble(secondValue)) {
                monotonic = false;
                System.out.println("hhhhhh " + firstValue);
                nonMonotonicLog.enqueue(firstValue);
                faultRegionMap.add(firstIdealValue, firstValue);
            }
        }
        return faultRegionMap;
    }

    public static void NonMonotonicRegionGraph(
            Map<String, String> faultRegionMap, double bits) {
        DefaultCategoryDataset objDataset = new DefaultCategoryDataset();
        double numOfLevels = Math.pow(2, bits);
        for (double i = 0; i < numOfLevels; i++) {
            if (faultRegionMap.hasKey(String.valueOf(i))) {
                objDataset.setValue(50, "Analog Shield", String.valueOf(i));
            } else {
                objDataset.setValue(0, "Analog Shield", String.valueOf(i));
            }

        }
        JFreeChart objChart = ChartFactory.createBarChart(
                "Non-monotonic region at " + (new Double(bits)).longValue()
                        + "bits", //Chart title
                "Bitword from 0 to " + (new Double(numOfLevels)).longValue(), //Domain axis label
                "Line indicating non-monotonicity", //Range axis label
                objDataset, //Chart Data
                PlotOrientation.VERTICAL, // orientation
                true, // include legend?
                true, // include tooltips?
                false // include URLs?
        );

        ChartFrame frame = new ChartFrame("Demo", objChart);
        frame.pack();
        frame.setVisible(true);

    }

    public static int findGuaranteedMonotonicBits(Map<String, String> cmdLookUp,
            SerialManager hello, Queue<String> idealReadingMasterTape,
            Queue<String> adcInputCollectionMasterTape) {

        Queue<String> idealReading = idealReadingMasterTape.newInstance();
        Queue<String> adcInputCollection = adcInputCollectionMasterTape
                .newInstance();
        idealReading.transferFrom(idealReadingMasterTape);
        adcInputCollection.transferFrom(adcInputCollectionMasterTape);
        boolean monotonic = true;
        int monotonicENOB = 0;
        for (int i = 8; i <= 16; i++) {
            if (monotonic) {
                generateDataFromShield(cmdLookUp, String.valueOf(i), hello,
                        idealReading, adcInputCollection);
                while (adcInputCollection.length() >= 2) {
                    String firstValue = adcInputCollection.dequeue();
                    String secondValue = adcInputCollection.dequeue();
                    adcInputCollectionMasterTape.enqueue(firstValue);
                    adcInputCollectionMasterTape.enqueue(secondValue);

                    if (Double.parseDouble(firstValue) > Double
                            .parseDouble(secondValue)) {
                        monotonic = false;
                        monotonicENOB = i - 1;
                        System.out
                                .println("This DAC can retain monotonicity at "
                                        + monotonicENOB + " bits");
                    }
                }
            }
        }

        return monotonicENOB;
    }

    public static void createINLperformanceGraph(
            Queue<String> idealReadingMasterTape,
            Queue<String> adcInputCollectionMasterTape) {
        Queue<String> idealReading = idealReadingMasterTape.newInstance();
        Queue<String> adcInputCollection = adcInputCollectionMasterTape
                .newInstance();
        idealReading.transferFrom(idealReadingMasterTape);
        adcInputCollection.transferFrom(adcInputCollectionMasterTape);
        XYSeries realDac = new XYSeries("Real-world DAC");
        XYSeries idealDac = new XYSeries("Ideal DAC");
        XYSeriesCollection dataset = new XYSeriesCollection();

        double counter = 0;
        while (adcInputCollection.length() != 0) {
            String middleMan = adcInputCollection.dequeue();
            realDac.add(counter, Double.parseDouble(middleMan));
            adcInputCollectionMasterTape.enqueue(middleMan);
            counter++;
        }
        double counter2 = 0;
        while (idealReading.length() != 0) {
            String middleWoman = idealReading.dequeue();
            idealDac.add(counter2, Double.parseDouble(middleWoman));
            idealReadingMasterTape.enqueue(middleWoman);
            counter2++;
        }
        dataset.addSeries(idealDac);
        dataset.addSeries(realDac);
        JFreeChart objChart = ChartFactory.createXYLineChart("INL of DAC",
                "BitWord", "ADC Reading", dataset);
        ChartFrame frame = new ChartFrame("Trusted electronics-ADC/DAC",
                objChart);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) throws IOException {

        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();

        SerialPort ports[] = SerialPort.getCommPorts();
        System.out.println("Select a port:");
        for (int i = 0; i < ports.length; i++) {
            out.println(i + " " + ports[i].getSystemPortName());
        }
        out.println("Type your port: ");
        int portNum = in.nextInteger();
        //open port, handshake with Arduino
        SerialManager hello = new SerialManager(
                "/dev/" + ports[portNum].getSystemPortName());
        hello.open(); //

        Map<String, String> cmdLookUp = includeCommandInMap();

        out.print("Test as how many number of bits? ");
        String bitsUnderTest = in.nextLine();

        Queue<String> idealReading = new Queue1L<>();
        Queue<String> adcInputCollection = new Queue1L<>();

        generateDataFromShield(cmdLookUp, bitsUnderTest, hello, idealReading,
                adcInputCollection);

//        out.print(adcInputCollection);
//        out.print(idealReading);

        createINLperformanceGraph(idealReading, adcInputCollection);
        //findGuaranteedMonotonicBits

//        out.println(findGuaranteedMonotonicBits(cmdLookUp, hello, idealReading,
//                adcInputCollection));
        Map<String, String> faultRegionMap = mapNonMonotonicRegion(cmdLookUp,
                hello, idealReading, adcInputCollection, 13);
        out.println(faultRegionMap.hasKey("1"));
        NonMonotonicRegionGraph(faultRegionMap, 13);

    }

}
