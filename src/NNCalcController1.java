import javax.swing.JOptionPane;

import components.map.Map;
import components.naturalnumber.NaturalNumber;
import components.queue.Queue;
import components.queue.Queue1L;

/**
 * Controller class.
 *
 * @author Zhengzhe Wei
 */
public final class NNCalcController1 implements NNCalcController {

    /**
     * Model object.
     */
    private final NNCalcModel model;
    private SerialManager hello;
    private Map<String, String> cmdLookUp;
    private Queue<String> idealReading;
    private Queue<String> adcInputCollection;
    /**
     * View object.
     */
    private final NNCalcView view;

    /**
     * Useful constants.
     */

    /**
     * Updates this.view to display this.model, and to allow only operations
     * that are legal given this.model.
     *
     * @param model
     *            the model
     * @param view
     *            the view
     * @ensures [view has been updated to be consistent with model]
     */
    private static void updateViewToMatchModel(NNCalcModel model,
            NNCalcView view) {

        NaturalNumber topN = model.top();
        NaturalNumber bottomN = model.bottom();
        view.updateTopDisplay(model.top());

    }

    /**
     * Constructor.
     *
     * @param model
     *            model to connect to
     * @param view
     *            view to connect to
     */
    public NNCalcController1(NNCalcModel model, NNCalcView view) {
        this.model = model;
        this.view = view;
        updateViewToMatchModel(model, view);

    }

    @Override
    public void processClearEvent() {
        /*
         * Get alias to bottom from model
         */
        NaturalNumber bottom = this.model.bottom();
        /*
         * Update model in response to this event
         */
        bottom.clear();
        /*
         * Update view to reflect changes in model
         */
        updateViewToMatchModel(this.model, this.view);
    }

    @Override
    public void processSwapEvent() {
        /*
         * Get aliases to top and bottom from model
         */
        NaturalNumber top = this.model.top();
        NaturalNumber bottom = this.model.bottom();
        /*
         * Update model in response to this event
         */
        NaturalNumber temp = top.newInstance();
        temp.transferFrom(top);
        top.transferFrom(bottom);
        bottom.transferFrom(temp);
        /*
         * Update view to reflect changes in model
         */
        updateViewToMatchModel(this.model, this.view);
    }

    @Override
    public void processEnterEvent(String portName) {
        System.out.println("Hello");
        // this.hello = new SerialManager("/dev/" + portName);
        this.hello = new SerialManager(portName);
        this.hello.open(); //
        this.view.setButtomMonotonicEnable();
        this.view.setConnectDisable();
        this.view.setPortSelectionDisable();
    }

    @Override
    public void processMonotonicEvent() {
        System.out.println("Hello");
        this.cmdLookUp = JSerialComm01.includeCommandInMap();
        this.idealReading = new Queue1L<>();
        this.adcInputCollection = new Queue1L<>();
        int maxMonotonicBits = JSerialComm01.findGuaranteedMonotonicBits(
                this.cmdLookUp, this.hello, this.idealReading,
                this.adcInputCollection);
        JOptionPane.showMessageDialog(null,
                "DAC under test can retain monotonicity at " + maxMonotonicBits
                        + " bits");
    }

    @Override
    public void processAddEvent() {

        NaturalNumber topN = this.model.top();
        NaturalNumber bottomN = this.model.bottom();
        bottomN.add(topN);
        topN.clear();
        updateViewToMatchModel(this.model, this.view);
    }

    @Override
    public void processSubtractEvent() {

        NaturalNumber topN = this.model.top();
        NaturalNumber bottomN = this.model.bottom();
        topN.subtract(bottomN);
        bottomN.transferFrom(topN);
        updateViewToMatchModel(this.model, this.view);

    }

    @Override
    public void processMultiplyEvent() {
        NaturalNumber topN = this.model.top();
        NaturalNumber bottomN = this.model.bottom();
        bottomN.multiply(topN);
        topN.clear();
        updateViewToMatchModel(this.model, this.view);

    }

    @Override
    public void processDivideEvent() {
        NaturalNumber topN = this.model.top();
        NaturalNumber bottomN = this.model.bottom();
        topN.divide(bottomN);
        bottomN.transferFrom(topN);
        updateViewToMatchModel(this.model, this.view);

    }

    @Override
    public void processPowerEvent() {

        NaturalNumber topN = this.model.top();
        NaturalNumber bottomN = this.model.bottom();
        topN.power(bottomN.toInt());
        bottomN.transferFrom(topN);
        updateViewToMatchModel(this.model, this.view);

    }

    @Override
    public void processRootEvent() {

        NaturalNumber topN = this.model.top();
        NaturalNumber bottomN = this.model.bottom();
        topN.root(bottomN.toInt());
        bottomN.transferFrom(topN);
        updateViewToMatchModel(this.model, this.view);

    }

    @Override
    public void processAddNewDigitEvent(int digit) {

        NaturalNumber n = this.model.bottom();
        n.multiplyBy10(digit);
        updateViewToMatchModel(this.model, this.view);
    }

    @Override
    public void processTestINL(String bitSelection) {
        System.out.println(bitSelection);
        if (bitSelection.equals("selectBits")) {
            JOptionPane.showMessageDialog(null,
                    "Please select number of bits to begin testing!");
        } else {
            this.cmdLookUp = JSerialComm01.includeCommandInMap();
            this.idealReading = new Queue1L<>();
            this.adcInputCollection = new Queue1L<>();

            JSerialComm01.generateDataFromShield(this.cmdLookUp, bitSelection,
                    this.hello, this.idealReading, this.adcInputCollection);

            JSerialComm01.createINLperformanceGraph(this.idealReading,
                    this.adcInputCollection);
        }
    }

    @Override
    public void processTestNonMonotonicRegion(String bitSelection) {
        System.out.println(bitSelection);
        if (bitSelection.equals("selectBits")) {
            JOptionPane.showMessageDialog(null,
                    "Please select number of bits to begin testing!");
        } else {
            this.cmdLookUp = JSerialComm01.includeCommandInMap();
            this.idealReading = new Queue1L<>();
            this.adcInputCollection = new Queue1L<>();

            JSerialComm01.generateIdealQueue(bitSelection, this.idealReading);

            Map<String, String> faultRegionMap = JSerialComm01
                    .mapNonMonotonicRegion(this.cmdLookUp, this.hello,
                            this.idealReading, this.adcInputCollection,
                            Integer.valueOf(bitSelection));
            JSerialComm01.NonMonotonicRegionGraph(faultRegionMap,
                    Double.valueOf(bitSelection));
        }

    }

}
