/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package SimulationAndReport;
import TheBusiness.ProductManagement.Product;
import java.io.*;
/**
 *
 * @author 70787
 */
public class ReportGenerator {
    public File generateFinalReport(SimulationResult r, String filePath) throws IOException {
        File out = new File(filePath);
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(out), "UTF-8"))) {
            pw.println("Product,OldPrice,NewPrice,RevenueBefore,RevenueAfter,Delta,Target,AboveTargetRate");
            for (Product p : r.oldPrice.keySet()) {
                double oldP = r.oldPrice.getOrDefault(p, 0.0);
                double newP = r.newPrice.getOrDefault(p, oldP);
                double before = r.revenueBefore.getOrDefault(p, 0.0);
                double after  = r.revenueAfter.getOrDefault(p, 0.0);
                double delta  = after - before;
                double target = p.getTargetPrice();
                double rate   = r.salesFreq.getOrDefault(p, new Frequency()).rateAbove();
                pw.printf("%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.4f%n",
                        p.toString(), oldP, newP, before, after, delta, target, rate);
            }
            pw.printf("TOTAL,, ,%.2f,%.2f,%.2f,,%n", r.totalBefore, r.totalAfter, r.totalAfter - r.totalBefore);
            if (r.mostImpactProduct != null) pw.printf("MOST_IMPACT,%s,,,%n", r.mostImpactProduct.toString());
        }
        return out;
    }
}
