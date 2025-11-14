/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package UserInterface.Main.Simulation;

import TheBusiness.Business.Business;
import java.awt.Color;
import java.awt.Font;
import java.text.SimpleDateFormat;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 *
 * @author hug0_
 */
public class SimulationJPanel extends javax.swing.JPanel {
    private Business business;
    private SimulationEngine simulationEngine;
    private ProfitOptimizer profitOptimizer;

    
     private boolean isRunning = false;
    /**
     * Creates new form SimulationJPanel
     */
    public SimulationJPanel() {
        initComponents();
    }
    
     public SimulationJPanel(Business business) {
        this.business = business;
        this.simulationEngine = new SimulationEngine(business);
        this.profitOptimizer = new ProfitOptimizer(business, simulationEngine);
        initComponents();
        customInit();
        
       }
     
      private void customInit() {
        // 设置初始状态
        txtCurrentRevenue.setText("$0");
        txtNewRevenue.setText("$0");
        txtImpactPercentage.setText("0.00%");
        
        // 设置字体
        txtCurrentRevenue.setFont(new Font("Arial", Font.BOLD, 14));
        txtNewRevenue.setFont(new Font("Arial", Font.BOLD, 14));
        txtImpactPercentage.setFont(new Font("Arial", Font.BOLD, 14));
        
        // 设置颜色
        txtCurrentRevenue.setForeground(Color.BLUE);
        txtNewRevenue.setForeground(new Color(0, 128, 0)); // 绿色
        
        // 设置文本区域
        jTextArea1.setFont(new Font("Monospaced", Font.PLAIN, 12));
        jTextArea1.setEditable(false);
        
        // 设置进度条
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        
        // 添加按钮事件监听器
        btnRunSimulation.addActionListener(e -> runSimulation());
        btnOptimizeProfit.addActionListener(e -> optimizeProfit());
        btnClear.addActionListener(e -> clearResults());
    }
      
      
       private void runSimulation() {
        if (isRunning) {
            JOptionPane.showMessageDialog(this, "模拟正在运行中，请稍候...");
            return;
        }
        
        // 使用SwingWorker在后台运行
        SwingWorker<SimulationResult, String> worker = new SwingWorker<SimulationResult, String>() {
            @Override
            protected SimulationResult doInBackground() throws Exception {
                isRunning = true;
                setButtonsEnabled(false);
                
                // 更新状态
                SwingUtilities.invokeLater(() -> {
                    lblStatusReady.setText("Status: Running Simulation...");
                    progressBar.setIndeterminate(true);
                });
                
                publish("正在运行模拟...\n");
                publish("=====================================\n");
                
                // 运行模拟
                SimulationResult result = simulationEngine.runSimulation();
                
                // 模拟详细输出
                publish(String.format("当前收入: $%,d\n", result.revenueBeforeAdjustment));
                publish(String.format("当前利润: $%,d\n", result.profitBeforeAdjustment));
                publish(String.format("调整了 %d 个产品的价格\n", result.productsAdjusted));
                publish(String.format("新收入: $%,d\n", result.revenueAfterAdjustment));
                publish(String.format("影响: %.2f%%\n", result.impactPercentage));
                
                return result;
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                for (String chunk : chunks) {
                    jTextArea1.append(chunk);
                }
                // 自动滚动到底部
                jTextArea1.setCaretPosition(jTextArea1.getDocument().getLength());
            }
            
            @Override
            protected void done() {
                try {
                    SimulationResult result = get();
                    displaySimulationResult(result);
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(SimulationJPanel.this, 
                        "模拟运行失败: " + e.getMessage(), 
                        "错误", 
                        JOptionPane.ERROR_MESSAGE);
                    jTextArea1.append("\n错误: " + e.getMessage() + "\n");
                } finally {
                    isRunning = false;
                    setButtonsEnabled(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                    lblStatusReady.setText("Status: Ready");
                }
            }
        };
        
        worker.execute();
    }
       
       private void optimizeProfit() {
        if (isRunning) {
            JOptionPane.showMessageDialog(this, "优化正在运行中，请稍候...");
            return;
        }
        
        // 使用SwingWorker在后台运行
        SwingWorker<OptimizationResult, String> worker = new SwingWorker<OptimizationResult, String>() {
            @Override
            protected OptimizationResult doInBackground() throws Exception {
                isRunning = true;
                setButtonsEnabled(false);
                
                // 更新状态
                SwingUtilities.invokeLater(() -> {
                    lblStatusReady.setText("Status: Optimizing...");
                    progressBar.setValue(0);
                });
                
                publish("\n开始利润优化...\n");
                publish("=====================================\n");
                
                // 创建一个自定义的优化器用于更新进度
                OptimizationResult result = new OptimizationResult();
                SimulationResult initialSim = simulationEngine.runSimulation();
                
                double lastProfitMargin = calculateProfitMargin(
                    initialSim.profitAfterAdjustment, 
                    initialSim.revenueAfterAdjustment
                );
                
                result.initialProfitMargin = lastProfitMargin;
                result.addIteration(0, lastProfitMargin, initialSim.revenueAfterAdjustment);
                
                publish(String.format("初始利润率: %.2f%%\n", lastProfitMargin));
                
                // 迭代优化
                final int MAX_ITERATIONS = 10;
                final double CONVERGENCE_THRESHOLD = 0.01;
                
                for (int i = 1; i <= MAX_ITERATIONS; i++) {
                    // 更新进度条
                    final int progress = i * 100 / MAX_ITERATIONS;
                    SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
                    
                    publish(String.format("\n--- 优化迭代 %d ---\n", i));
                    
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
                    publish(String.format("当前利润率: %.2f%%\n", currentProfitMargin));
                    publish(String.format("改善幅度: %.2f%%\n", improvement));
                    
                    // 检查是否收敛
                    if (improvement < CONVERGENCE_THRESHOLD) {
                        publish("✓ 优化收敛，达到最优状态\n");
                        result.converged = true;
                        break;
                    }
                    
                    lastProfitMargin = currentProfitMargin;
                    
                    // 模拟处理时间
                    Thread.sleep(500);
                }
                
                result.totalIterations = result.iterationHistory.size() - 1;
                result.finalProfitMargin = lastProfitMargin;
                
                return result;
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                for (String chunk : chunks) {
                    jTextArea1.append(chunk);
                }
                // 自动滚动到底部
                jTextArea1.setCaretPosition(jTextArea1.getDocument().getLength());
            }
            
            @Override
            protected void done() {
                try {
                    OptimizationResult result = get();
                    displayOptimizationResult(result);
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(SimulationJPanel.this, 
                        "优化运行失败: " + e.getMessage(), 
                        "错误", 
                        JOptionPane.ERROR_MESSAGE);
                    jTextArea1.append("\n错误: " + e.getMessage() + "\n");
                } finally {
                    isRunning = false;
                    setButtonsEnabled(true);
                    progressBar.setValue(100);
                    lblStatusReady.setText("Status: Ready");
                }
            }
        };
        
        worker.execute();
    }
       
        private double calculateProfitMargin(int profit, int revenue) {
        if (revenue == 0) return 0;
        return ((double)profit / revenue) * 100;
    }
    
    /**
     * 显示模拟结果
     */
    private void displaySimulationResult(SimulationResult result) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        // 更新状态标签
        txtCurrentRevenue.setText(String.format("$%,d", result.revenueBeforeAdjustment));
        txtNewRevenue.setText(String.format("$%,d", result.revenueAfterAdjustment));
        
        String impactText = String.format("%.2f%%", result.impactPercentage);
        txtImpactPercentage.setText(impactText);
        
        // 设置颜色
        if (result.impactPercentage > 0) {
            txtImpactPercentage.setForeground(new Color(0, 128, 0)); // 绿色
        } else if (result.impactPercentage < 0) {
            txtImpactPercentage.setForeground(Color.RED);
        } else {
            txtImpactPercentage.setForeground(Color.BLACK);
        }
        
        // 添加详细结果到文本区域
        jTextArea1.append("\n=== 模拟完成 ===\n");
        jTextArea1.append("时间: " + sdf.format(result.simulationTime) + "\n");
        jTextArea1.append("调整产品数: " + result.productsAdjusted + "\n");
        jTextArea1.append("收入变化: $" + String.format("%,d", result.revenueBeforeAdjustment) + 
                         " → $" + String.format("%,d", result.revenueAfterAdjustment) + "\n");
        jTextArea1.append("利润变化: $" + String.format("%,d", result.profitBeforeAdjustment) + 
                         " → $" + String.format("%,d", result.profitAfterAdjustment) + "\n");
        jTextArea1.append("影响: " + String.format("%.2f%%", result.impactPercentage) + "\n");
        
        // 评估性能
        String performance = profitOptimizer.evaluatePerformance();
        jTextArea1.append("性能评估: " + performance + "\n");
        jTextArea1.append("=====================================\n\n");
        
        // 滚动到底部
        jTextArea1.setCaretPosition(jTextArea1.getDocument().getLength());
    }
    
    /**
     * 显示优化结果
     */
    private void displayOptimizationResult(OptimizationResult result) {
        jTextArea1.append("\n=== 优化完成 ===\n");
        jTextArea1.append("总迭代次数: " + result.totalIterations + "\n");
        jTextArea1.append("初始利润率: " + String.format("%.2f%%", result.initialProfitMargin) + "\n");
        jTextArea1.append("最终利润率: " + String.format("%.2f%%", result.finalProfitMargin) + "\n");
        jTextArea1.append("利润率提升: " + String.format("%.2f%%", 
            result.finalProfitMargin - result.initialProfitMargin) + "\n");
        jTextArea1.append("是否收敛: " + (result.converged ? "是" : "否") + "\n");
        
        // 显示建议
        if (result.finalProfitMargin < 10) {
            jTextArea1.append("\n⚠️ 建议: 利润率仍然较低，建议进一步分析产品结构\n");
        } else if (result.finalProfitMargin < 20) {
            jTextArea1.append("\n✓ 建议: 利润率有所改善，继续监控\n");
        } else {
            jTextArea1.append("\n✅ 建议: 利润率表现良好！\n");
        }
        
        jTextArea1.append("=====================================\n\n");
        
        // 滚动到底部
        jTextArea1.setCaretPosition(jTextArea1.getDocument().getLength());
        
        // 如果有显著改善，更新收入显示
        if (result.iterationHistory.size() > 0) {
            IterationRecord lastIteration = result.iterationHistory.get(result.iterationHistory.size() - 1);
            txtNewRevenue.setText(String.format("$%,d", lastIteration.revenue));
        }
    }
    
    /**
     * 清除结果
     */
    private void clearResults() {
        jTextArea1.setText("");
        txtCurrentRevenue.setText("$0");
        txtNewRevenue.setText("$0");
        txtImpactPercentage.setText("0.00%");
        txtImpactPercentage.setForeground(Color.BLACK);
        progressBar.setValue(0);
        lblStatusReady.setText("Status: Ready");
    }
    
    /**
     * 设置按钮启用状态
     */
    private void setButtonsEnabled(boolean enabled) {
        btnRunSimulation.setEnabled(enabled);
        btnOptimizeProfit.setEnabled(enabled);
        btnClear.setEnabled(enabled);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lblSimulationAndOptimizationConsole = new javax.swing.JLabel();
        btnRunSimulation = new javax.swing.JButton();
        btnOptimizeProfit = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        txtCurrentRevenue = new javax.swing.JLabel();
        txtNewRevenue = new javax.swing.JLabel();
        txtImpactPercentage = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        ResultsScrollPane = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        lblStatusReady = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        lblSimulationAndOptimizationConsole.setFont(new java.awt.Font("Helvetica Neue", 0, 18)); // NOI18N
        lblSimulationAndOptimizationConsole.setText("Simulation & Optimization Console");

        btnRunSimulation.setText(" Run Simulation");

        btnOptimizeProfit.setText("Optimize Profit");

        btnClear.setText("Clear");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(137, 137, 137)
                .addComponent(btnRunSimulation)
                .addGap(148, 148, 148)
                .addComponent(btnOptimizeProfit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnClear)
                .addGap(111, 111, 111))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblSimulationAndOptimizationConsole)
                .addGap(298, 298, 298))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addComponent(lblSimulationAndOptimizationConsole)
                .addGap(36, 36, 36)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOptimizeProfit)
                    .addComponent(btnRunSimulation)
                    .addComponent(btnClear))
                .addContainerGap(36, Short.MAX_VALUE))
        );

        txtCurrentRevenue.setText("CurrentRevenue");

        txtNewRevenue.setText("NewRevenue");

        txtImpactPercentage.setText("Impact Percentage");

        lblStatus.setFont(new java.awt.Font("Helvetica Neue", 0, 18)); // NOI18N
        lblStatus.setText("Status");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(151, 151, 151)
                .addComponent(txtCurrentRevenue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 182, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblStatus)
                    .addComponent(txtNewRevenue))
                .addGap(218, 218, 218)
                .addComponent(txtImpactPercentage)
                .addGap(70, 70, 70))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(lblStatus)
                .addGap(41, 41, 41)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNewRevenue)
                    .addComponent(txtCurrentRevenue)
                    .addComponent(txtImpactPercentage))
                .addContainerGap(32, Short.MAX_VALUE))
        );

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        ResultsScrollPane.setViewportView(jTextArea1);

        lblStatusReady.setText("Status: Ready ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(ResultsScrollPane)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(101, 101, 101)
                        .addComponent(lblStatusReady)
                        .addGap(61, 61, 61)
                        .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 551, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(57, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(ResultsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 71, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblStatusReady, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(54, 54, 54))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane ResultsScrollPane;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnOptimizeProfit;
    private javax.swing.JButton btnRunSimulation;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JLabel lblSimulationAndOptimizationConsole;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblStatusReady;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel txtCurrentRevenue;
    private javax.swing.JLabel txtImpactPercentage;
    private javax.swing.JLabel txtNewRevenue;
    // End of variables declaration//GEN-END:variables
}
