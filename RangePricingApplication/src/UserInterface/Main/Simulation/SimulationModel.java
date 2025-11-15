/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UserInterface.Main.Simulation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author hug0_
 */
class SimulationResult {
    public int revenueBeforeAdjustment;
    public int revenueAfterAdjustment;
    public int profitBeforeAdjustment;
    public int profitAfterAdjustment;
    public double impactPercentage;
    public int productsAdjusted;
    public Date simulationTime;
    
    public SimulationResult() {
        this.simulationTime = new Date();
    }
    
    public String getImpactSummary() {
        return String.format(
            "收入变化: $%d → $%d (%.2f%%)\n利润变化: $%d → $%d",
            revenueBeforeAdjustment, revenueAfterAdjustment, impactPercentage,
            profitBeforeAdjustment, profitAfterAdjustment
        );
    }
}

/**
 * 优化结果类
 */
class OptimizationResult {
    public int totalIterations;
    public double initialProfitMargin;
    public double finalProfitMargin;
    public List<IterationRecord> iterationHistory;
    public boolean converged;
    
    public OptimizationResult() {
        this.iterationHistory = new ArrayList<>();
    }
    
    public void addIteration(int iteration, double profitMargin, int revenue) {
        iterationHistory.add(new IterationRecord(iteration, profitMargin, revenue));
    }
}

/**
 * 迭代记录类
 */
class IterationRecord {
    public int iterationNumber;
    public double profitMargin;
    public int revenue;
    public Date timestamp;
    
    public IterationRecord(int iteration, double margin, int rev) {
        this.iterationNumber = iteration;
        this.profitMargin = margin;
        this.revenue = rev;
        this.timestamp = new Date();
    }
}
