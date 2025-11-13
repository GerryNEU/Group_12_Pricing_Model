/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package SimulationAndReport;

/**
 *
 * @author 70787
 */
public class Frequency {
    public int aboveTarget;
    public int atOrBelowTarget;

    public double rateAbove() {
        int total = aboveTarget + atOrBelowTarget;
        return total == 0 ? 0.0 : (aboveTarget * 1.0 / total);
    }
}
