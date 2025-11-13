/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package SimulationAndReport;
import java.util.*;
import TheBusiness.Business.Business;
import TheBusiness.ProductManagement.Product;
/**
 *
 * @author 70787
 */
public class SimulationResult {
    public Business business;
    public Map<Product, Double> revenueBefore = new LinkedHashMap<>();
    public Map<Product, Double> revenueAfter  = new LinkedHashMap<>();
    public Map<Product, Frequency> salesFreq  = new LinkedHashMap<>();
    public Map<Product, Double> oldPrice      = new LinkedHashMap<>();
    public Map<Product, Double> newPrice      = new LinkedHashMap<>();
    public double totalBefore;
    public double totalAfter;
    public Product mostImpactProduct;
    public double maxDelta;
}
