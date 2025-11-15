/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UserInterface.PriceAdjustment;

import TheBusiness.Business.Business;
import TheBusiness.ProductManagement.Product;
import TheBusiness.ProductManagement.ProductCatalog;
import TheBusiness.Supplier.Supplier;
import TheBusiness.Supplier.SupplierDirectory;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ouyangkairui
 */


public class PriceAdjustmentEngine {
    
    // Helper method: Gets a single list of all products from all suppliers.
    private static List<Product> getAllProducts(Business business) {
        ArrayList<Product> allProducts = new ArrayList<>();
        SupplierDirectory supplierDirectory = business.getSupplierDirectory();
        
        for (Supplier supplier : supplierDirectory.getSuplierList()) {
            ProductCatalog productCatalog = supplier.getProductCatalog();
            allProducts.addAll(productCatalog.getProductList());
        }
        return allProducts;
    }
    
    
    // Function 2: Batch adjust prices down for all under-performing products.
    public static String adjustPricesDown(Business business) {
        StringBuilder log = new StringBuilder("--- Starting Price Reduction for Under-performing Products ---\n");
        int count = 0;
        List<Product> allProducts = getAllProducts(business);

        for (Product p : allProducts) {
            // Get sales figures from the Product object
            int below = p.getNumberOfProductSalesBelowTarget();
            int above = p.getNumberOfProductSalesAboveTarget();

            // Rule: Sales below target > Sales above target
            if (below > above) {
                int oldPrice = p.getTargetPrice();
                int newPrice = (int) (oldPrice * 0.95); // 5% reduction
                
                // The setTargetPrice method in Product.java already handles floor/ceiling validation
                p.setTargetPrice(newPrice); 
                
                int finalPrice = p.getTargetPrice(); // Get the price after validation
                if (oldPrice != finalPrice) {
                    log.append(String.format("Product '%s' price adjusted from %d to %d\n", p.toString(), oldPrice, finalPrice));
                    count++;
                }
            }
        }
        log.append(String.format("--- Finished: %d products were adjusted. ---\n", count));
        return log.toString();
    }
    
    
//    // Function 3: Batch adjust prices up for all over-performing products.
//    public static String adjustPricesUp(Business business) {
//        StringBuilder log = new StringBuilder("--- Starting Price Increase for Over-performing Products ---\n");
//        int count = 0;
//        List<Product> allProducts = getAllProducts(business);
//
//        for (Product p : allProducts) {
//            int below = p.getNumberOfProductSalesBelowTarget();
//            int above = p.getNumberOfProductSalesAboveTarget();
//
//            // Rule: Sales above target > (Sales below target * 2)
//            if (above > (below * 2)) {
//                int oldPrice = p.getTargetPrice();
//                int newPrice = (int) (oldPrice * 1.05); // 5% increase
//                
//                // The setTargetPrice method in Product.java already handles floor/ceiling validation
//                p.setTargetPrice(newPrice);
//                
//                int finalPrice = p.getTargetPrice(); // Get the price after validation
//                if (oldPrice != finalPrice) {
//                    log.append(String.format("Product '%s' price adjusted from %d to %d\n", p.toString(), oldPrice, finalPrice));
//                    count++;
//                }
//            }
//        }
//        log.append(String.format("--- Finished: %d products were adjusted. ---\n", count));
//        return log.toString();
//    }
//    
}


