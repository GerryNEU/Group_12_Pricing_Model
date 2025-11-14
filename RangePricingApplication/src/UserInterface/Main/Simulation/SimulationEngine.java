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
import java.util.List;
import java.util.Random;

/**
 *
 * @author hug0_
 */
public class SimulationEngine {
    private Business business;
    private List<SimulationResult> simulationHistory;
    private Random random = new Random();
    
    public SimulationEngine(Business business) {
        this.business = business;
        this.simulationHistory = new ArrayList<>();
    }
    
    /**
     * 运行一次完整的模拟
     */
    public SimulationResult runSimulation() {
        System.out.println("=== 开始运行模拟 ===");
        SimulationResult result = new SimulationResult();
        
        // 步骤1: 计算调整前的指标
        result.revenueBeforeAdjustment = calculateTotalRevenue();
        result.profitBeforeAdjustment = calculateTotalProfit();
        System.out.println("当前收入: $" + result.revenueBeforeAdjustment);
        System.out.println("当前利润: $" + result.profitBeforeAdjustment);
        
        // 步骤2: 执行价格调整
        int adjustedCount = performPriceAdjustments();
        result.productsAdjusted = adjustedCount;
        System.out.println("调整了 " + adjustedCount + " 个产品的价格");
        
        // 步骤3: 计算调整后的指标
        result.revenueAfterAdjustment = calculateTotalRevenue();
        result.profitAfterAdjustment = calculateTotalProfit();
        
        // 步骤4: 计算影响
        if (result.revenueBeforeAdjustment > 0) {
            result.impactPercentage = 
                ((double)(result.revenueAfterAdjustment - result.revenueBeforeAdjustment) 
                / result.revenueBeforeAdjustment) * 100;
        }
        
        System.out.println("新收入: $" + result.revenueAfterAdjustment);
        System.out.println("影响: " + String.format("%.2f%%", result.impactPercentage));
        
        // 保存历史记录
        simulationHistory.add(result);
        
        return result;
    }
    
    /**
     * 计算总收入
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
     * 计算总利润
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
     * 执行价格调整（临时实现）
     */
    private int performPriceAdjustments() {
        int adjustedCount = 0;
        
        ArrayList<Supplier> suppliers = business.getSupplierDirectory().getSuplierList();
        for (Supplier supplier : suppliers) {
            ArrayList<Product> products = supplier.getProductCatalog().getProductList();
            for (Product product : products) {
                ProductSummary summary = new ProductSummary(product);
                
                boolean adjusted = false;
                int currentTarget = product.getTargetPrice();
                int newTarget = currentTarget;
                
                // 判断是否需要调整
                if (summary.getNumberBelowTarget() > summary.getNumberAboveTarget()) {
                    // 销售不佳，降低价格
                    newTarget = (int)(currentTarget * 0.95);
                    newTarget = Math.max(newTarget, product.getFloorPrice());
                    adjusted = true;
                } else if (summary.getNumberAboveTarget() > summary.getNumberBelowTarget() * 2) {
                    // 销售很好，提高价格
                    newTarget = (int)(currentTarget * 1.05);
                    newTarget = Math.min(newTarget, product.getCeilingPrice());
                    adjusted = true;
                }
                
                // 更新价格
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
     * 获取最高收入影响的产品
     */
    public List<Product> getTopImpactProducts(int count) {
        List<Product> allProducts = new ArrayList<>();
        
        ArrayList<Supplier> suppliers = business.getSupplierDirectory().getSuplierList();
        for (Supplier supplier : suppliers) {
            allProducts.addAll(supplier.getProductCatalog().getProductList());
        }
        
        // 按收入排序
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
}