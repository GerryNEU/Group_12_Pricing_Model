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
        System.out.println("=== 开始利润优化 ===");
        OptimizationResult result = new OptimizationResult();
        
        // 初始状态
        SimulationResult initialSim = simulationEngine.runSimulation();
        double lastProfitMargin = calculateProfitMargin(
            initialSim.profitAfterAdjustment, 
            initialSim.revenueAfterAdjustment
        );
        
        result.initialProfitMargin = lastProfitMargin;
        result.addIteration(0, lastProfitMargin, initialSim.revenueAfterAdjustment);
        
        System.out.println("初始利润率: " + String.format("%.2f%%", lastProfitMargin));
        
        // 迭代优化
        for (int i = 1; i <= MAX_ITERATIONS; i++) {
            System.out.println("\n--- 优化迭代 " + i + " ---");
            
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
            System.out.println("当前利润率: " + String.format("%.2f%%", currentProfitMargin));
            System.out.println("改善幅度: " + String.format("%.2f%%", improvement));
            
            // 检查是否收敛
            if (improvement < CONVERGENCE_THRESHOLD) {
                System.out.println("✓ 优化收敛，达到最优状态");
                result.converged = true;
                break;
            }
            
            lastProfitMargin = currentProfitMargin;
        }
        
        result.totalIterations = result.iterationHistory.size() - 1; // 减去初始状态
        result.finalProfitMargin = lastProfitMargin;
        
        System.out.println("\n=== 优化完成 ===");
        System.out.println("总迭代次数: " + result.totalIterations);
        System.out.println("最终利润率: " + String.format("%.2f%%", result.finalProfitMargin));
        System.out.println("利润率提升: " + String.format("%.2f%%", 
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
     * 评估当前性能
     */
    public String evaluatePerformance() {
        SimulationResult latest = simulationEngine.runSimulation();
        double margin = calculateProfitMargin(
            latest.profitAfterAdjustment,
            latest.revenueAfterAdjustment
        );
        
        if (margin < 10) return "差 - 需要立即优化";
        else if (margin < 20) return "一般 - 有改进空间";
        else if (margin < 30) return "良好 - 表现不错";
        else return "优秀 - 利润率很高";
    }
}
    

