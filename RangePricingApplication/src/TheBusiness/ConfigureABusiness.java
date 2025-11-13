/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TheBusiness;


import java.util.Random;
import com.github.javafaker.Faker;


import MarketingManagement.MarketingPersonDirectory;
import MarketingManagement.MarketingPersonProfile;
import TheBusiness.Business.Business;
import TheBusiness.MarketModel.ChannelCatalog;
import TheBusiness.CustomerManagement.CustomerDirectory;
import TheBusiness.CustomerManagement.CustomerProfile;
import TheBusiness.MarketModel.Channel;
import TheBusiness.MarketModel.Market;
import TheBusiness.MarketModel.MarketCatalog;
import TheBusiness.MarketModel.MarketChannelAssignment;
import TheBusiness.MarketModel.MarketChannelComboCatalog;
import TheBusiness.MarketModel.SolutionOffer;
import TheBusiness.MarketModel.SolutionOfferCatalog;
import TheBusiness.OrderManagement.MasterOrderList;
import TheBusiness.SolutionOrders.MasterSolutionOrderList;
import TheBusiness.OrderManagement.Order;
import TheBusiness.OrderManagement.OrderItem;
import TheBusiness.Personnel.Person;
import TheBusiness.Personnel.PersonDirectory;
import TheBusiness.ProductManagement.Product;
import TheBusiness.ProductManagement.ProductSummary;
import TheBusiness.ProductManagement.ProductCatalog;
import TheBusiness.SalesManagement.SalesPersonDirectory;
import TheBusiness.SalesManagement.SalesPersonProfile;
import TheBusiness.SolutionOrders.SolutionOrder;
import TheBusiness.Supplier.Supplier;
import TheBusiness.Supplier.SupplierDirectory;
import TheBusiness.UserAccountManagement.UserAccount;
import TheBusiness.UserAccountManagement.UserAccountDirectory;
import java.util.ArrayList;

/**
 *
 * @author kal bugrara
 */
class ConfigureABusiness {

    static Business initialize() {
        Business business = new Business("Xerox");
        Faker faker = new Faker();        
        Random random = new Random();

        // Get directories from the business object
        PersonDirectory personDirectory = business.getPersonDirectory();
        UserAccountDirectory uad = business.getUserAccountDirectory();
        CustomerDirectory customerDirectory = business.getCustomerDirectory();
        SupplierDirectory supplierDirectory = business.getSupplierDirectory();
        MasterOrderList masterOrderList = business.getMasterOrderList();
        
        // --- 1. Create User Accounts for Login (Sales, Marketing) ---
        // We need these to log in and test the application's different workspaces.
        // Create Sales User
        Person salesPersonObj = personDirectory.newPerson("sales_user");
        SalesPersonProfile salesProfile = business.getSalesPersonDirectory().newSalesPersonProfile(salesPersonObj);
        uad.newUserAccount(salesProfile, "sales", "sales"); // User: sales, Pass: sales
        
        // Create Marketing User (for Pricing Team, as per PDF)
        Person marketingPersonObj = personDirectory.newPerson("mkt_user");
        MarketingPersonProfile marketingProfile = business.getMarketingPersonDirectory().newMarketingPersonProfile(marketingPersonObj);
        uad.newUserAccount(marketingProfile, "marketing", "marketing"); // User: marketing, Pass: marketing
                
        // --- 2. Generate Suppliers and Products ---
        // Requirement: 50 Suppliers, 50 Products each (Total 2500 products)       
        
         for (int i = 0; i < 50; i++) {
            String supplierName = faker.company().name(); // Use Faker for supplier name
            Supplier supplier = supplierDirectory.newSupplier(supplierName);
            ProductCatalog productCatalog = supplier.getProductCatalog();

            for (int j = 0; j < 50; j++) {
                String productName = faker.commerce().productName(); // Use Faker for product name

                // Generate prices based on PDF hint (Page 4) [cite: 3240-3242]
                int floorPrice = 100 + random.nextInt(200); // Random floor price (e.g., 100-299)
                int ceilingPrice = floorPrice + 50 + random.nextInt(200); // Ceiling is always above floor
                int targetPrice = floorPrice + (int) ((ceilingPrice - floorPrice) * 0.5); // Target is mid-point

                productCatalog.newProduct(productName, floorPrice, ceilingPrice, targetPrice);
            }
        }
       
        // --- 3. Generate Customers ---
        // Requirement: 300 Customers
        for (int i = 0; i < 300; i++) {
            String customerName = faker.name().fullName(); // Use Faker for customer name
            Person person = personDirectory.newPerson(customerName);
            customerDirectory.newCustomerProfile(person);
        }
        
        // --- 4. Simulate Order History (Market Data) ---
        // Requirement: 1-3 Orders per Customer, 1-10 Items per Order       
        
        // Get all suppliers and customers we just created
        ArrayList<Supplier> allSuppliers = supplierDirectory.getSuplierList();
        ArrayList<CustomerProfile> allCustomers = customerDirectory.getCustomerlist();   
        
        for (CustomerProfile customer : allCustomers) {
            int orderCount = 1 + random.nextInt(3); // Generates 1, 2, or 3 orders

            for (int o = 0; o < orderCount; o++) {
                // Create an order and assign it to our default salesperson (salesProfile)
                Order order = masterOrderList.newOrder(customer, salesProfile);

                int itemCount = 1 + random.nextInt(10); // 1 to 10 items per order
                for (int item = 0; item < itemCount; item++) {
                    
                    // Pick a random product from a random supplier
                    Supplier randomSupplier = allSuppliers.get(random.nextInt(allSuppliers.size()));
                    Product randomProduct = randomSupplier.getProductCatalog().getProductList().get(random.nextInt(randomSupplier.getProductCatalog().getProductList().size()));

                    // Requirement: Varying actual prices
                    // This simulates market behavior. Price is randomized between floor and ceiling.
                    int actualPrice = 0;
                    if (randomProduct.getCeilingPrice() > randomProduct.getFloorPrice()) {
                         actualPrice = randomProduct.getFloorPrice() + 
                                       random.nextInt(randomProduct.getCeilingPrice() - randomProduct.getFloorPrice() + 1); // +1 to make ceiling inclusive
                    } else {
                         actualPrice = randomProduct.getTargetPrice(); // Failsafe if prices are bad
                    }
                    
                    int quantity = 1 + random.nextInt(5); // Random quantity (1-5)
                    
                    // Create the order item, which links back to the product
                    order.newOrderItem(randomProduct, actualPrice, quantity);
                }
                order.Submit(); // Mark order as "Submitted" to simulate a completed sale
            }
        }
        
        System.out.println("Data Generation Complete: " + supplierDirectory.getSuplierList().size() + " suppliers, " + customerDirectory.getCustomerlist().size() + " customers.");
        return business;
    }

    static Business initializeMarkets() {
        
        Business business = new Business("Xerox-Markets");
        return business;
    }
    
}
