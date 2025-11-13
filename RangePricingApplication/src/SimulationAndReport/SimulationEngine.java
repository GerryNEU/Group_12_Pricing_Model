/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package SimulationAndReport;
import java.util.*;
import TheBusiness.Business.Business;
import TheBusiness.OrderManagement.*;
import TheBusiness.ProductManagement.Product;
import TheBusiness.Supplier.Supplier;
/**
 *
 * @author 70787
 */
public class SimulationEngine {
    private final Business business;
    public SimulationEngine(Business b){ this.business = b; }

    public SimulationResult runSimulation() {
        SimulationResult r = new SimulationResult();
        r.business = business;

        for (Product p : getAllProducts()) {
            r.oldPrice.put(p, (double) p.getTargetPrice());
        }

        accumulate(r);

        for (Product p : getAllProducts()) {
            r.newPrice.put(p, (double) p.getTargetPrice());
        }

        double maxAbs = -1; Product best = null;
        for (Product p : getAllProducts()) {
            double before = r.revenueBefore.getOrDefault(p, 0.0);
            double after  = r.revenueAfter .getOrDefault(p, 0.0);
            double d = Math.abs(after - before);
            if (d > maxAbs) { maxAbs = d; best = p; }
        }
        r.mostImpactProduct = best;
        r.maxDelta = maxAbs;
        return r;
    }

    private void accumulate(SimulationResult r){
        MasterOrderList mol = business.getMasterOrderList();
        for (Order o : mol.getOrders()) {
            for (OrderItem li : o.getOrderItems()) {
                Product p = li.getSelectedProduct();
                int qty = li.getQuantity();
                double price = li.getActualPrice();

                Frequency f = r.salesFreq.computeIfAbsent(p, k -> new Frequency());
                if (price > p.getTargetPrice()) f.aboveTarget++;
                else f.atOrBelowTarget++;

                double rev = price * qty;
                r.revenueBefore.merge(p, rev, Double::sum);
                r.totalBefore += rev;

                r.revenueAfter.merge(p, rev, Double::sum);
                r.totalAfter += rev;
            }
        }
    }

    private List<Product> getAllProducts() {
        List<Product> res = new ArrayList<>();
        for (Supplier s : business.getSupplierDirectory().getSuplierList()) {
            res.addAll(s.getProductCatalog().getProductList());
        }
        return res;
    }
}
