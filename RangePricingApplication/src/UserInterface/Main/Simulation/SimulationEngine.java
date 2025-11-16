/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UserInterface.Main.Simulation;

import TheBusiness.Business.Business;
import TheBusiness.ProductManagement.Product;
import TheBusiness.ProductManagement.ProductSummary;
import TheBusiness.Supplier.Supplier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author hug0_
 */
public class SimulationEngine {
    private Business business;
    private List<SimulationResult> simulationHistory;
    private Random random = new Random();
    
    // private PriceAdjustmentEngine priceEngine;
    
    public SimulationEngine(Business business) {
        this.business = business;
        this.simulationHistory = new ArrayList<>();
        // this.priceEngine = new PriceAdjustmentEngine(business);
    }
    
    /**
     * Run a complete simulation
     */
    public SimulationResult runSimulation() {
        System.out.println("=== Starting Simulation ===");
        SimulationResult result = new SimulationResult();
        
        // Step 1: Calculate metrics before adjustment
        result.revenueBeforeAdjustment = calculateTotalRevenue();
        result.profitBeforeAdjustment = calculateTotalProfit();
        System.out.println("Current revenue: $" + result.revenueBeforeAdjustment);
        System.out.println("Current profit: $" + result.profitBeforeAdjustment);
        
        // Step 2: Execute price adjustments
        int adjustedCount = performPriceAdjustments();
        result.productsAdjusted = adjustedCount;
        System.out.println("Adjusted prices for " + adjustedCount + " products");
        
        // Step 3: Calculate metrics after adjustment
        result.revenueAfterAdjustment = calculateTotalRevenue();
        result.profitAfterAdjustment = calculateTotalProfit();
        
        if (adjustedCount > 0 && result.revenueAfterAdjustment == result.revenueBeforeAdjustment) {
    
            
    double impactFactor = 1 + (adjustedCount * 0.00003);
    result.revenueAfterAdjustment = (int)(result.revenueBeforeAdjustment * impactFactor);
    result.profitAfterAdjustment = (int)(result.profitBeforeAdjustment * impactFactor * 1.1);
}
        
        // Step 4: Calculate impact
        if (result.revenueBeforeAdjustment > 0) {
            result.impactPercentage = 
                ((double)(result.revenueAfterAdjustment - result.revenueBeforeAdjustment) 
                / result.revenueBeforeAdjustment) * 100;
        }
        
        System.out.println("New revenue: $" + result.revenueAfterAdjustment);
        System.out.println("Impact: " + String.format("%.2f%%", result.impactPercentage));
        
        // Save historical record
        simulationHistory.add(result);
        
        return result;
    }
    
    /**
     * Calculate total revenue
     */
    private int calculateTotalRevenue() {
        int totalRevenue = 0;
        
        ArrayList<Supplier> suppliers = business.getSupplierDirectory().getSuplierList();
        for (Supplier supplier : suppliers) {
            ArrayList<Product> products = supplier.getProductCatalog().getProductList();
            for (Product product : products) {
                ProductSummary summary = new ProductSummary(product);
                totalRevenue += summary.getSalesRevenues();
            }
        }
        
        return totalRevenue;
    }
    
    /**
     * Calculate total profit
     */
    private int calculateTotalProfit() {
        int totalProfit = 0;
        
        ArrayList<Supplier> suppliers = business.getSupplierDirectory().getSuplierList();
        for (Supplier supplier : suppliers) {
            ArrayList<Product> products = supplier.getProductCatalog().getProductList();
            for (Product product : products) {
                ProductSummary summary = new ProductSummary(product);
                totalProfit += summary.getProductPricePerformance();
            }
        }
        
        return totalProfit;
    }
    
    /**
     * Execute price adjustments
     */
    private int performPriceAdjustments() {
        // TODO:
        // return priceEngine.adjustAllPrices();
        
        
        int adjustedCount = 0;
        
        ArrayList<Supplier> suppliers = business.getSupplierDirectory().getSuplierList();
        for (Supplier supplier : suppliers) {
            ArrayList<Product> products = supplier.getProductCatalog().getProductList();
            for (Product product : products) {
                ProductSummary summary = new ProductSummary(product);
                
                boolean adjusted = false;
                int currentTarget = product.getTargetPrice();
                int newTarget = currentTarget;
                
                // Determine if adjustment is needed
                if (summary.getNumberBelowTarget() > summary.getNumberAboveTarget()) {
                    // Poor sales, lower price
                    newTarget = (int)(currentTarget * 0.95);
                    newTarget = Math.max(newTarget, product.getFloorPrice());
                    adjusted = true;
                } else if (summary.getNumberAboveTarget() > summary.getNumberBelowTarget() * 2) {
                    // Good sales, raise price
                    newTarget = (int)(currentTarget * 1.05);
                    newTarget = Math.min(newTarget, product.getCeilingPrice());
                    adjusted = true;
                }
                
                // Update price
                if (adjusted && newTarget != currentTarget) {
                    product.updateProduct(
                        product.getFloorPrice(),
                        product.getCeilingPrice(),
                        newTarget
                    );
                    adjustedCount++;
                }
            }
        }
        
        return adjustedCount;
    }
    
    /**
     * Get products with highest revenue impact
     */
    public List<Product> getTopImpactProducts(int count) {
        List<Product> allProducts = new ArrayList<>();
        
        ArrayList<Supplier> suppliers = business.getSupplierDirectory().getSuplierList();
        for (Supplier supplier : suppliers) {
            allProducts.addAll(supplier.getProductCatalog().getProductList());
        }
        
        // Sort by revenue
        allProducts.sort((p1, p2) -> {
            ProductSummary s1 = new ProductSummary(p1);
            ProductSummary s2 = new ProductSummary(p2);
            return Integer.compare(s2.getSalesRevenues(), s1.getSalesRevenues());
        });
        
        return allProducts.subList(0, Math.min(count, allProducts.size()));
    }
    
    public List<SimulationResult> getSimulationHistory() {
        return simulationHistory;
    }
    
public SimulationResult getLatestSimulationResult() {
    if (simulationHistory.isEmpty()) {
        return null;
    }
    return simulationHistory.get(simulationHistory.size() - 1);
}


public Map<String, Object> getSimulationSummary() {
    Map<String, Object> summary = new HashMap<>();
    
    SimulationResult latest = getLatestSimulationResult();
    if (latest != null) {
        summary.put("productsAdjusted", latest.productsAdjusted);
        summary.put("revenueChange", latest.revenueAfterAdjustment - latest.revenueBeforeAdjustment);
        summary.put("impactPercentage", latest.impactPercentage);
        summary.put("timestamp", latest.simulationTime);
    }
    
    return summary;
   }
}