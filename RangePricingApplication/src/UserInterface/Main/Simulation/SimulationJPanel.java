/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package UserInterface.Main.Simulation;

import TheBusiness.Business.Business;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
    private JPanel cardSequencePanel;

    
     private boolean isRunning = false;
    /**
     * Creates new form SimulationJPanel
     */
    public SimulationJPanel() {
        initComponents();
    }
    
     public SimulationJPanel(Business business, JPanel cardPanel) {
        this.business = business;
        this.cardSequencePanel = cardPanel;
        this.simulationEngine = new SimulationEngine(business);
        this.profitOptimizer = new ProfitOptimizer(business, simulationEngine);
        initComponents();
        customInit();
        
       }
     
      private void customInit() {
        // Set initial status with labels
        txtCurrentRevenue.setText("CurrentRevenue: $0");
        txtNewRevenue.setText("NewRevenue: $0");
        txtImpactPercentage.setText("Impact Percentage: 0.00%");
        
        // Set fonts
        txtCurrentRevenue.setFont(new Font("Arial", Font.BOLD, 14));
        txtNewRevenue.setFont(new Font("Arial", Font.BOLD, 14));
        txtImpactPercentage.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Set colors
        txtCurrentRevenue.setForeground(Color.BLUE);
        txtNewRevenue.setForeground(new Color(0, 128, 0)); // green
        
        // Set text area
        jTextArea1.setFont(new Font("Monospaced", Font.PLAIN, 12));
        jTextArea1.setEditable(false);
        
        // Set progress bar
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        
        // Add button event listeners
        btnRunSimulation.addActionListener(e -> runSimulation());
        btnOptimizeProfit.addActionListener(e -> optimizeProfit());
        btnClear.addActionListener(e -> clearResults());
    }
      
      
       private void runSimulation() {
        if (isRunning) {
            JOptionPane.showMessageDialog(this, "Simulation is running, please wait...");
            return;
        }
        
        // Use SwingWorker to run in background
        SwingWorker<SimulationResult, String> worker = new SwingWorker<SimulationResult, String>() {
            @Override
            protected SimulationResult doInBackground() throws Exception {
                isRunning = true;
                setButtonsEnabled(false);
                
                // Update status
                SwingUtilities.invokeLater(() -> {
                    lblStatusReady.setText("Status: Running Simulation...");
                    progressBar.setIndeterminate(true);
                });
                
                publish("Running simulation...\n");
                publish("=====================================\n");
                
                // Run simulation
                SimulationResult result = simulationEngine.runSimulation();
                
                // Simulation detail output
                publish(String.format("Current Revenue: $%,d\n", result.revenueBeforeAdjustment));
                publish(String.format("Current Profit: $%,d\n", result.profitBeforeAdjustment));
                publish(String.format("Products Adjusted: %d\n", result.productsAdjusted));
                publish(String.format("New Revenue: $%,d\n", result.revenueAfterAdjustment));
                publish(String.format("Impact: %.2f%%\n", result.impactPercentage));
                
                return result;
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                for (String chunk : chunks) {
                    jTextArea1.append(chunk);
                }
                // Auto scroll to bottom
                jTextArea1.setCaretPosition(jTextArea1.getDocument().getLength());
            }
            
            @Override
            protected void done() {
                try {
                    SimulationResult result = get();
                    displaySimulationResult(result);
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(SimulationJPanel.this, 
                        "Simulation failed: " + e.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    jTextArea1.append("\nError: " + e.getMessage() + "\n");
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
            JOptionPane.showMessageDialog(this, "Optimization is running, please wait...");
            return;
        }
        
        // Use SwingWorker to run in background
        SwingWorker<OptimizationResult, String> worker = new SwingWorker<OptimizationResult, String>() {
            @Override
            protected OptimizationResult doInBackground() throws Exception {
                isRunning = true;
                setButtonsEnabled(false);
                
                // Update status
                SwingUtilities.invokeLater(() -> {
                    lblStatusReady.setText("Status: Optimizing...");
                    progressBar.setValue(0);
                });
                
                publish("\nStarting profit optimization...\n");
                publish("=====================================\n");
                
                // Create custom optimizer for progress updates
                OptimizationResult result = new OptimizationResult();
                SimulationResult initialSim = simulationEngine.runSimulation();
                
                double lastProfitMargin = calculateProfitMargin(
                    initialSim.profitAfterAdjustment, 
                    initialSim.revenueAfterAdjustment
                );
                
                result.initialProfitMargin = lastProfitMargin;
                result.addIteration(0, lastProfitMargin, initialSim.revenueAfterAdjustment);
                
                publish(String.format("Initial Profit Margin: %.2f%%\n", lastProfitMargin));
                
                // Iterative optimization
                final int MAX_ITERATIONS = 10;
                final double CONVERGENCE_THRESHOLD = 0.01;
                
                for (int i = 1; i <= MAX_ITERATIONS; i++) {
                    // Update progress bar
                    final int progress = i * 100 / MAX_ITERATIONS;
                    SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
                    
                    publish(String.format("\n--- Optimization Iteration %d ---\n", i));
                    
                    // Run simulation
                    SimulationResult simResult = simulationEngine.runSimulation();
                    
                    // Calculate new profit margin
                    double currentProfitMargin = calculateProfitMargin(
                        simResult.profitAfterAdjustment,
                        simResult.revenueAfterAdjustment
                    );
                    
                    result.addIteration(i, currentProfitMargin, simResult.revenueAfterAdjustment);
                    
                    // Calculate improvement
                    double improvement = Math.abs(currentProfitMargin - lastProfitMargin);
                    publish(String.format("Current Profit Margin: %.2f%%\n", currentProfitMargin));
                    publish(String.format("Improvement: %.2f%%\n", improvement));
                    
                    // Check convergence
                    if (improvement < CONVERGENCE_THRESHOLD) {
                        publish("✓ Optimization converged, reached optimal state\n");
                        result.converged = true;
                        break;
                    }
                    
                    lastProfitMargin = currentProfitMargin;
                    
                    // Simulate processing time
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
                // Auto scroll to bottom
                jTextArea1.setCaretPosition(jTextArea1.getDocument().getLength());
            }
            
            @Override
            protected void done() {
                try {
                    OptimizationResult result = get();
                    displayOptimizationResult(result);
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(SimulationJPanel.this, 
                        "Optimization failed: " + e.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    jTextArea1.append("\nError: " + e.getMessage() + "\n");
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
     * Display simulation results
     */
    private void displaySimulationResult(SimulationResult result) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        // Update status labels - keep labels in the display
        txtCurrentRevenue.setText(String.format("CurrentRevenue: $%,d", result.revenueBeforeAdjustment));
        txtNewRevenue.setText(String.format("NewRevenue: $%,d", result.revenueAfterAdjustment));
        
        String impactText = String.format("Impact Percentage: %.2f%%", result.impactPercentage);
        txtImpactPercentage.setText(impactText);
        
        // Set colors
        if (result.impactPercentage > 0) {
            txtImpactPercentage.setForeground(new Color(0, 128, 0)); // green
        } else if (result.impactPercentage < 0) {
            txtImpactPercentage.setForeground(Color.RED);
        } else {
            txtImpactPercentage.setForeground(Color.BLACK);
        }
        
        // Add detailed results to text area
        jTextArea1.append("\n=== Simulation Complete ===\n");
        jTextArea1.append("Time: " + sdf.format(result.simulationTime) + "\n");
        jTextArea1.append("Products Adjusted: " + result.productsAdjusted + "\n");
        jTextArea1.append("Revenue Change: $" + String.format("%,d", result.revenueBeforeAdjustment) + 
                         " → $" + String.format("%,d", result.revenueAfterAdjustment) + "\n");
        jTextArea1.append("Profit Change: $" + String.format("%,d", result.profitBeforeAdjustment) + 
                         " → $" + String.format("%,d", result.profitAfterAdjustment) + "\n");
        jTextArea1.append("Impact: " + String.format("%.2f%%", result.impactPercentage) + "\n");
        
        // Evaluate performance
        String performance = profitOptimizer.evaluatePerformance();
        jTextArea1.append("Performance Evaluation: " + performance + "\n");
        jTextArea1.append("=====================================\n\n");
        
        // Scroll to bottom
        jTextArea1.setCaretPosition(jTextArea1.getDocument().getLength());
        
         Map<String, Object> summary = simulationEngine.getSimulationSummary();
    if (summary != null && !summary.isEmpty()) {
        jTextArea1.append("\n=== Summary Data ===\n");
        jTextArea1.append("Products Adjusted: " + summary.get("productsAdjusted") + "\n");
        jTextArea1.append("Revenue Change: $" + String.format("%,d", summary.get("revenueChange")) + "\n");
        jTextArea1.append("Impact: " + String.format("%.2f%%", summary.get("impactPercentage")) + "\n");
        jTextArea1.append("Timestamp: " + summary.get("timestamp") + "\n");
        jTextArea1.append("=====================================\n");
       }

           
    }
   
    /**
     * Display optimization results
     */
    private void displayOptimizationResult(OptimizationResult result) {
         jTextArea1.append("\n=== Optimization Complete ===\n");
    jTextArea1.append("Total Iterations: " + result.totalIterations + "\n");
    jTextArea1.append("Initial Profit Margin: " + String.format("%.2f%%", result.initialProfitMargin) + "\n");
    jTextArea1.append("Final Profit Margin: " + String.format("%.2f%%", result.finalProfitMargin) + "\n");
    jTextArea1.append("Profit Margin Improvement: " + String.format("%.2f%%", 
        result.finalProfitMargin - result.initialProfitMargin) + "\n");
    jTextArea1.append("Converged: " + (result.converged ? "Yes" : "No") + "\n");
        
        // Display recommendations
          if (result.finalProfitMargin < 10) {
        jTextArea1.append("\n⚠️ Recommendation: Profit margin is still low, suggest further analysis of product structure\n");
    } else if (result.finalProfitMargin < 20) {
        jTextArea1.append("\n✓ Recommendation: Profit margin has improved, continue monitoring\n");
    } else {
        jTextArea1.append("\n✅ Recommendation: Profit margin performing well!\n");
    }
    
    jTextArea1.append("=====================================\n\n");
        
        // Scroll to bottom
        jTextArea1.setCaretPosition(jTextArea1.getDocument().getLength());
        
        // Update revenue display if significant improvement
         if (result.iterationHistory.size() > 0) {
        IterationRecord lastIteration = result.iterationHistory.get(result.iterationHistory.size() - 1);
        
        
        int currentRevenue = Integer.parseInt(txtCurrentRevenue.getText()
            .replace("CurrentRevenue: $", "")
            .replace(",", ""));
        
        
        double revenueMultiplier = 1 + (result.finalProfitMargin - result.initialProfitMargin) / 100;
        int optimizedRevenue = (int)(currentRevenue * revenueMultiplier);
        
        txtNewRevenue.setText(String.format("NewRevenue: $%,d", optimizedRevenue));
        
        
        double newImpact = ((double)(optimizedRevenue - currentRevenue) / currentRevenue) * 100;
        txtImpactPercentage.setText(String.format("Impact Percentage: %.2f%%", newImpact));
        }
    }
    
    /**
     * Clear results
     */
    private void clearResults() {
        jTextArea1.setText("");
        txtCurrentRevenue.setText("CurrentRevenue: $0");
        txtNewRevenue.setText("NewRevenue: $0");
        txtImpactPercentage.setText("Impact Percentage: 0.00%");
        txtImpactPercentage.setForeground(Color.BLACK);
        progressBar.setValue(0);
        lblStatusReady.setText("Status: Ready");
    }
    
    /**
     * Set button enable status
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
        btnBack = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        txtCurrentRevenue = new javax.swing.JLabel();
        txtNewRevenue = new javax.swing.JLabel();
        txtImpactPercentage = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        ResultsScrollPane = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        lblStatusReady = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        lblSimulationAndOptimizationConsole.setFont(new java.awt.Font("Helvetica Neue", 0, 18)); // NOI18N
        lblSimulationAndOptimizationConsole.setText("Simulation & Optimization Console");

        btnRunSimulation.setText(" Run Simulation");

        btnOptimizeProfit.setText("Optimize Profit");

        btnClear.setText("Clear");

        btnBack.setText("<<Back");
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(93, 93, 93)
                .addComponent(btnRunSimulation)
                .addGap(170, 170, 170)
                .addComponent(btnOptimizeProfit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 209, Short.MAX_VALUE)
                .addComponent(btnClear)
                .addGap(119, 119, 119))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(293, 293, 293)
                        .addComponent(lblSimulationAndOptimizationConsole))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnBack)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(btnBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSimulationAndOptimizationConsole)
                .addGap(40, 40, 40)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOptimizeProfit)
                    .addComponent(btnRunSimulation)
                    .addComponent(btnClear))
                .addContainerGap(36, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        txtCurrentRevenue.setText("CurrentRevenue");

        txtNewRevenue.setText("NewRevenue");

        txtImpactPercentage.setText("Impact Percentage");

        lblStatus.setFont(new java.awt.Font("Helvetica Neue", 0, 18)); // NOI18N
        lblStatus.setText("Status");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(101, 101, 101)
                .addComponent(txtCurrentRevenue)
                .addGap(184, 184, 184)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblStatus)
                    .addComponent(txtNewRevenue))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(txtImpactPercentage)
                .addGap(118, 118, 118))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(lblStatus)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNewRevenue)
                    .addComponent(txtCurrentRevenue)
                    .addComponent(txtImpactPercentage))
                .addGap(32, 32, 32))
        );

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        ResultsScrollPane.setViewportView(jTextArea1);

        lblStatusReady.setFont(new java.awt.Font("Helvetica Neue", 0, 18)); // NOI18N
        lblStatusReady.setText("Status: Ready ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(ResultsScrollPane)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblStatusReady)
                        .addGap(18, 18, 18)
                        .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 634, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(46, 46, 46)))
                .addContainerGap(8, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ResultsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblStatusReady)
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE))
                .addGap(36, 36, 36))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        // TODO add your handling code here:
        
    CardLayout cardLayout = (CardLayout) cardSequencePanel.getLayout();
   
    cardLayout.show(cardSequencePanel, "MarketingManagerPanel");
        
    }//GEN-LAST:event_btnBackActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane ResultsScrollPane;
    private javax.swing.JButton btnBack;
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
