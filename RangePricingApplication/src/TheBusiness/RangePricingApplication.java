/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TheBusiness;

import javax.swing.SwingUtilities;
import TheBusiness.Business.Business;
import SimulationAndReport.MainUI;
/**
 *
 * @author kal bugrara
 */
public class RangePricingApplication {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Business business = ConfigureABusiness.initialize();

        SwingUtilities.invokeLater(() -> {
            new MainUI(business).setVisible(true);
        });
    }

}

