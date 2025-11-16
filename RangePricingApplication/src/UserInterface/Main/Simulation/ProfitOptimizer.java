/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UserInterface.Main.Simulation;

import TheBusiness.Business.Business;

/**
 *
 * @author hug0_
 */
public class ProfitOptimizer {
    private Business business;
    private SimulationEngine simulationEngine;
    private static final int MAX_ITERATIONS = 10;
    private static final double CONVERGENCE_THRESHOLD = 0.01; // 1%
    
    public ProfitOptimizer(Business business, SimulationEngine engine) {
        this.business = business;
        this.simulationEngine = engine;
    }
    
    /**
     * 持续优化直到利润最大化
     */
    public OptimizationResult optimizeUntilMaxProfit() {
        System.out.println("=== Starting Profit Optimization ===");
        OptimizationResult result = new OptimizationResult();
        
        // 初始状态
        SimulationResult initialSim = simulationEngine.runSimulation();
        double lastProfitMargin = calculateProfitMargin(
            initialSim.profitAfterAdjustment, 
            initialSim.revenueAfterAdjustment
        );
        
        result.initialProfitMargin = lastProfitMargin;
        result.addIteration(0, lastProfitMargin, initialSim.revenueAfterAdjustment);
        
        System.out.println("Initial profit margin: " + String.format("%.2f%%", lastProfitMargin));
        
        // 迭代优化
        for (int i = 1; i <= MAX_ITERATIONS; i++) {
            System.out.println("\n--- Optimization Iteration " + i + " ---");
            
            // 运行模拟
            SimulationResult simResult = simulationEngine.runSimulation();
            
            // 计算新的利润率
            double currentProfitMargin = calculateProfitMargin(
                simResult.profitAfterAdjustment,
                simResult.revenueAfterAdjustment
            );
            
            result.addIteration(i, currentProfitMargin, simResult.revenueAfterAdjustment);
            
            // 计算改善程度
            double improvement = Math.abs(currentProfitMargin - lastProfitMargin);
            System.out.println("Current profit margin: " + String.format("%.2f%%", currentProfitMargin));
            System.out.println("Improvement: " + String.format("%.2f%%", improvement));
            
            // 检查是否收敛
            if (improvement < CONVERGENCE_THRESHOLD) {
                System.out.println("✓ Optimization converged, reached optimal state");
                result.converged = true;
                break;
            }
            
            lastProfitMargin = currentProfitMargin;
        }
        
        result.totalIterations = result.iterationHistory.size() - 1; // 减去初始状态
        result.finalProfitMargin = lastProfitMargin;
        
        System.out.println("\n=== Optimization Complete ===");
        System.out.println("Total iterations: " + result.totalIterations);
        System.out.println("Final profit margin: " + String.format("%.2f%%", result.finalProfitMargin));
        System.out.println("Profit margin improvement: " + String.format("%.2f%%", 
            result.finalProfitMargin - result.initialProfitMargin));
        
        return result;
    }
    
    /**
     * 计算利润率
     */
    private double calculateProfitMargin(int profit, int revenue) {
        if (revenue == 0) return 0;
        return ((double)profit / revenue) * 100;
    }
    
    /**
     * 评估当前性能 - 返回英文评价
     */
    public String evaluatePerformance() {
        SimulationResult latest = simulationEngine.runSimulation();
        double margin = calculateProfitMargin(
            latest.profitAfterAdjustment,
            latest.revenueAfterAdjustment
        );
        
        if (margin < 10) return "Poor - Needs immediate optimization";
        else if (margin < 20) return "Fair - Room for improvement";
        else if (margin < 30) return "Good - Performing well";
        else return "Excellent - High profit margin";
    }
}

    

