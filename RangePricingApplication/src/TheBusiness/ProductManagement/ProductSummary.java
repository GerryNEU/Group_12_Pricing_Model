/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TheBusiness.ProductManagement;

/**
 *
 * @author kal bugrara
 */
//this class will extract summary data from the product
public class ProductSummary {

    Product subjectproduct;
    int numberofsalesabovetarget;
    int numberofsalesbelowtarget;
    int productpriceperformance; //total profit above target --could be negative too
    int totalQuantitySold; // total number of units sold
    int acutalsalesvolume;
    int rank; // will be done later

    public ProductSummary(Product p) {
        
        subjectproduct = p; //keeps track of the product itself not as well;
        numberofsalesabovetarget = p.getNumberOfProductSalesAboveTarget();
        productpriceperformance = p.getOrderPricePerformance();
        acutalsalesvolume = p.getSalesVolume();
        numberofsalesbelowtarget = p.getNumberOfProductSalesBelowTarget();
        totalQuantitySold = p.getTotalQuantitySold();
    }

    public Product getSubjectProduct(){
        return subjectproduct;
    }

    // Returns total revenue
    public int getSalesRevenues() {
        return acutalsalesvolume;
    }
    
    // Returns total units sold
    public int getTotalQuantitySold() {
        return totalQuantitySold;
    }

    // Returns count of sales ABOVE target
    public int getNumberAboveTarget() {
        return numberofsalesabovetarget;
    }

    // Returns the total profit margin difference (can be negative)
    public int getProductPricePerformance() {
        return productpriceperformance;
    }

    // Returns count of sales BELOW target
    public int getNumberBelowTarget() {
        return numberofsalesbelowtarget;
    }

    public boolean isProductAlwaysAboveTarget() {
        return subjectproduct.isProductAlwaysAboveTarget();
    }
}
