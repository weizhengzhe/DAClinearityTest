/**
 * Natural Number Calculator application.
 *
 *
 * @author Bruce W. Weide
 *
 */
public final class NaturalNumberCalculator {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private NaturalNumberCalculator() {
    }

    /**
     * Main program that sets up main application window and starts user
     * interaction.
     *
     * @param args
     *            command-line arguments; not used
     */
    public static void main(String[] args) {

        NNCalcModel model = new NNCalcModel1();
        NNCalcView view = new NNCalcView1();
        NNCalcController controller = new NNCalcController1(model, view);

        view.registerObserver(controller);
    }

}
