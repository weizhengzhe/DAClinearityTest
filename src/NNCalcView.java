import java.awt.event.ActionListener;

import components.naturalnumber.NaturalNumber;

/**
 * View interface.
 *
 * @author Bruce W. Weide
 */
public interface NNCalcView extends ActionListener {

    /**
     * Register argument as observer/listener of this; this must be done first,
     * before any other methods of this class are called.
     *
     * @param controller
     *            controller to register
     */
    void registerObserver(NNCalcController controller);

    /**
     * Updates top operand display based on NaturalNumber provided as argument.
     *
     * @param n
     *            new value of top operand display
     */
    void updateTopDisplay(NaturalNumber n);

    /**
     * Updates top operand display based on NaturalNumber provided as argument.
     *
     *
     * new value of top operand display
     */
    void setButtomMonotonicEnable();

    /**
     * Updates top operand display based on NaturalNumber provided as argument.
     *
     *
     * new value of top operand display
     */
    void setConnectDisable();

    /**
     * Updates top operand display based on NaturalNumber provided as argument.
     *
     * new value of top operand display
     */
    void setPortSelectionDisable();

}
