package module2.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// (Section 4.2.3) Behavioral Pattern
// Observer Pattern - Subscriber Interfaces
// The interface that receiving inventory update notifications
interface InventoryObserver{
    void update(String productID);
}

// Publisher operations for broadcasting notifications
interface InventorySubject{
    void attach(InventoryObserver observer);
    void detach(InventoryObserver observer);
    void notifyObservers(String productID);
}

// (Section 4.2.2) Structural Pattern
// Composite Pattern - The base structural component
// Top abstraction layer for all inventory components
// that allows individual items and bundles to be treated the same way
abstract class AbstractStockComponent{
    // shared attributes for both leaves and composites
    protected String componentID;
    protected double basePrice;

    public AbstractStockComponent(String componentID, double basePrice){
        this.componentID = componentID;
        this.basePrice = basePrice;
    }
    public String getComponentID(){
        return componentID;
    }
    // for uniform polymorphic execution
    public abstract boolean checkAvailability();
    public abstract void deductStock(int qty);
}

// Leaf Node - represents a single product in the inventory
class SingleItem extends AbstractStockComponent{
    private int stockQty;
    private String category;

    public SingleItem(String componentID, double basePrice, int stockQty, String category){
        super(componentID, basePrice);
        this.stockQty = stockQty;
        this.category = category;
    }

    @Override
    public boolean checkAvailability(){
        return stockQty > 0;
    }

    @Override
    public void deductStock(int qty){
        if(stockQty >= qty){
            this.stockQty -= qty;
        } else {
            throw new IllegalStateException("Insufficient stock for item: " + componentID);
        }
    }
    public String getCategory(){
        return category;
    }
}

// Composite Node - represents a bundle of products
class PromoBundle extends AbstractStockComponent{
    private List<AbstractStockComponent> components = new ArrayList<>();
    private double bundleDiscount;

    public PromoBundle(String componentID, double basePrice, double bundleDiscount){
        super(componentID, basePrice);
        this.bundleDiscount = bundleDiscount;
    }
    public void add(AbstractStockComponent component){
        components.add(component);
    }
    public void remove(AbstractStockComponent component){
        components.remove(component);
    }
    @Override
    public boolean checkAvailability(){
        for(AbstractStockComponent component : components){
            if(!component.checkAvailability()){
                return false;
            }
        }
        return !components.isEmpty();
    }
    @Override
    public void deductStock(int qty){
        if (!checkAvailability()){
            throw new IllegalStateException("Bundle " + componentID + " is not available in sufficient quantity.");
        }
        for(AbstractStockComponent component : components){
            component.deductStock(qty);
        }
    }
    public double getBundleDiscount(){
        return bundleDiscount;
    }
}

// Singleton Unified Inventory Gateway
// Main inventory manager of the system.
public class InventoryModule implements InventorySubject{
    
    // (Section 4.2.1) Creational Pattern
    // Singleton Pattern - Singleton instance declaration
    private static InventoryModule instance;
    private String inventoryID;
    private int stockLevel;

    // Composite that interacts via high-level abstract interface
    private Map<String, AbstractStockComponent> stockRecords;
    // Observer list that tracks runtime subscriber instances for background broadcast alerts
    private List<InventoryObserver> observers;
    // Singleton private constructor 
    private InventoryModule(){
        stockRecords = new HashMap<>();
        observers = new ArrayList<>();
        this.inventoryID = "inv-001"; // Example inventory ID
        this.stockLevel = 0; // Initialize stock level
    }
    
    // Singleton pattern - synchronized global access point
    public static synchronized InventoryModule getInstance(){
        if(instance == null){
            instance = new InventoryModule();
        }
        return instance;
    }
    // Simulating inventory validation and processing during checkout
    // triggers the observer notification mechanism to alert subscribers of inventory changes
    public boolean verifyAndDeduct(String componentID, int qty){
        AbstractStockComponent stockElement = stockRecords.get(componentID);
        if(stockElement != null && stockElement.checkAvailability()){
            stockElement.deductStock(qty);

            // Update overall stock level for single items (aggregated)
            if(stockElement instanceof SingleItem){
                this.stockLevel -= qty; 
            }

            // Out of stock notification trigger for observers
            if(!stockElement.checkAvailability()){
                notifyObservers(componentID); 
            }
            return true;
        }
        return false;
    }
    

    public void addStockRecord(AbstractStockComponent component){
        stockRecords.put(component.getComponentID(), component);
    }

    // (Section 4.2.3) 
    // Observer Pattern Implementation
    @Override
    public void attach(InventoryObserver observer){
        observers.add(observer);
    }
    @Override
    public void detach(InventoryObserver observer){
        observers.remove(observer);
    }
    @Override
    public void notifyObservers(String productID){
        for(InventoryObserver observer : observers){
            observer.update(productID);
        }
    }
}

// Observer that notifies vendors when a product goes out of stock
class VendorNotifier implements InventoryObserver{
    @Override
    public void update(String productID){
        System.out.println("Vendor Notification: Product " + productID + " is out of stock. Please restock soon.");
    }
}
// Observer that updates search indexes when products become unavailable
class SearchEngineAdapter implements InventoryObserver{
    @Override
    public void update(String productID){
        System.out.println("Search Engine Update: Product " + productID + " is now out of stock. Updating search results.");
    }
}