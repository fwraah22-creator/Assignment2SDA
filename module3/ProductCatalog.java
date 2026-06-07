import java.util.Map;
import java.util.ArrayList;
import java.util.List;

// =========================================================================
// 1. CORE DOMAIN ABSTRACTIONS & INVENTORY INTERFACES
// =========================================================================

// TARGET INTERFACE: Product
// Defines the functional contract for all product variants within the platform catalog.
interface Product {
    String getDetails();
    void updateStockQty(int qty);
}

// =========================================================================
// 2. CREATIONAL PATTERN: FACTORY METHOD IMPLEMENTATION
// =========================================================================

// CONCRETE PRODUCT 1: ElectronicProduct
// Specializes Product with hardware-specific structural data.
class ElectronicProduct implements Product {
    // Structural domain attributes established in Assignment 1 baseline
    private String productId;
    private String productName;
    private double price;
    private int stockQty; 
    
    // Factory Method Pattern Specific Attributes mapped in UML
    private int warrantyPeriod;
    private String modelNumber;

    public ElectronicProduct(String id, String name, double price, int qty, int warranty, String model) {
        this.productId = id;
        this.productName = name;
        this.price = price;
        this.stockQty = qty;
        this.warrantyPeriod = warranty;
        this.modelNumber = model;
    }

    @Override
    public String getDetails() {
        return "[Electronics] " + productName + " (Model: " + modelNumber + ", Warranty: " + warrantyPeriod + " months) - RM" + price;
    }

    @Override
    public void updateStockQty(int qty) {
        this.stockQty = qty; // Triggers internal inventory update flow
    }
}

// CONCRETE PRODUCT 2: ClothingProduct
// Specializes Product with garment-specific structural data.
class ClothingProduct implements Product {
    // Structural domain attributes established in Assignment 1 baseline
    private String productId;
    private String productName;
    private double price;
    private int stockQty;
    
    // Factory Method Pattern Specific Attributes mapped in UML
    private String size;
    private String material;

    public ClothingProduct(String id, String name, double price, int qty, String size, String material) {
        this.productId = id;
        this.productName = name;
        this.price = price;
        this.stockQty = qty;
        this.size = size;
        this.material = material;
    }

    @Override
    public String getDetails() {
        return "[Clothing] " + productName + " (Size: " + size + ", Material: " + material + ") - RM" + price;
    }

    @Override
    public void updateStockQty(int qty) {
        this.stockQty = qty; // Triggers internal inventory update flow
    }
}

// ABSTRACT CREATOR: AbstractCatalogCreator
// Declares the Factory Method template that concrete sub-factories override.
abstract class AbstractCatalogCreator {
    public abstract Product factoryMethod();

    //  Operational template method executing object lifecycle processes.
    public String catalogTheProduct(String productId, String name, double price, int qty) {
        // Step 1: Call the polymorphic factory method to instantiate a product type cleanly
        Product product = factoryMethod();
        
        // Step 2: Perform baseline inventory configurations
        product.updateStockQty(qty);
        
        System.out.println("Registration Log: Successfully instantiated catalog entry via Factory Method: " + product.getDetails());
        return "SUCCESS";
    }
}

// CONCRETE CREATOR 1: ElectronicsCatalogCreator
// Overrides the factory method to isolate specialized Electronic product configuration.
class ElectronicsCatalogCreator extends AbstractCatalogCreator {
    @Override
    public Product factoryMethod() {
        // Safely encapsulates specific instantiation data without polluting core modules
        return new ElectronicProduct("PROD-E101", "Wireless Noise-Canceling Earbuds", 149.00, 50, 12, "TWS-2026");
    }
}

// CONCRETE CREATOR 2: ClothingCatalogCreator
// Overrides the factory method to isolate specialized Clothing product configuration.
class ClothingCatalogCreator extends AbstractCatalogCreator {
    @Override
    public Product factoryMethod() {
        return new ClothingProduct("PROD-C202", "Oversized Vintage Cotton Hoodie", 79.90, 120, "XL", "100% Terry Cotton");
    }
}

// =========================================================================
// 3. STRUCTURAL PATTERN: ADAPTER IMPLEMENTATION
// =========================================================================

// TARGET INTERFACE: CatalogSyncTarget
// Expected target interface utilized by internal catalog subsystems.
interface CatalogSyncTarget {
    Product syncVendorProduct(Map<String, String> vendorData);
}

// ADAPTEE CLASS: SupplierERPAPI
// Simulates a complex external vendor supplier legacy management system API.
class SupplierERPAPI {
    // Legacy method displaying an incompatible method signature and return data model
    public LegacyStockRecord fetchLegacyStockItem(int skuId) {
        System.out.println("External ERP Connection: Extracting raw structural row payload for SKU: " + skuId);
        return new LegacyStockRecord(skuId, "Premium Leather Shoulder Bag", 189.50, 45, "BROWN", "GENUINE_COWHIDE");
    }
}

// ADAPTEE DATA CONTAINER: LegacyStockRecord
// The legacy data format exported by the third-party partner application.
class LegacyStockRecord {
    public int itemSkuCode;
    public String label;
    public double unitCost;
    public int availableCount;
    public String colorAttribute;
    public String fabricType;

    public LegacyStockRecord(int sku, String label, double cost, int qty, String color, String fabric) {
        this.itemSkuCode = sku;
        this.label = label;
        this.unitCost = cost;
        this.availableCount = qty;
        this.colorAttribute = color;
        this.fabricType = fabric;
    }
}

// ADAPTER CLASS: LegacyVendorAdapter
// Adapts legacy supplier payload interfaces to match standard internal systems.
class LegacyVendorAdapter implements CatalogSyncTarget {
    // Composition hook referencing the target Adaptee class.
    private SupplierERPAPI legacySupplierApi;

    public LegacyVendorAdapter() {
        this.legacySupplierApi = new SupplierERPAPI();
    }

    // Performs interface conversion and dynamic parameter translations.
    @Override
    public Product syncVendorProduct(Map<String, String> vendorData) {
        // Step 1: Safely parse context markers passed down from internal clients
        int targetSku = Integer.parseInt(vendorData.get("targetSkuId"));
        
        // Step 2: Fetch raw payload from the incompatible external dependency library via delegation
        LegacyStockRecord externalRecord = legacySupplierApi.fetchLegacyStockItem(targetSku);
        
        // Step 3: Map, transform, and normalize data schemas to fit internal fields
        String standardizedId = "VND-SYNC-" + externalRecord.itemSkuCode;
        String standardizedName = externalRecord.label;
        double standardizedPrice = externalRecord.unitCost + 15.50; // Injects dynamic profit-margin markups safely
        int standardizedQty = externalRecord.availableCount;
        
        System.out.println("Adapter Layer: Data structure translated cleanly. Mapping fields to Shopee framework entities.");
        
        // Step 4: Wrap the normalized variables back into a valid polymorphic Product instance
        return new ClothingProduct(
            standardizedId, 
            standardizedName, 
            standardizedPrice, 
            standardizedQty, 
            externalRecord.colorAttribute, 
            externalRecord.fabricType
        );
    }
}

// =========================================================================
// 4. BEHAVIORAL PATTERN: OBSERVER IMPLEMENTATION
// =========================================================================

// OBSERVER INTERFACE: CatalogObserver
// Defines the uniform entrance contract for objects watching catalog adjustments.
interface CatalogObserver {
    void update(Product product);
}

// SUBJECT BASE CLASS: CatalogSubject
// Manages runtime registration handles and dispatches change signals.
abstract class CatalogSubject {
    // Holds active subscribers.
    private final List<CatalogObserver> observers = new ArrayList<>();

    public void attach(CatalogObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void detach(CatalogObserver observer) {
        if (observer != null) {
            observers.remove(observer);
        }
    }

    public void notifyObservers(Product product) {
        for (CatalogObserver observer : observers) {
            // Polymorphically alerts all independent observer sub-modules
            observer.update(product);
        }
    }
}

// CONCRETE SUBJECT: ProductCatalog
// Concrete tracking system emitting event broad-signals when listings are updated.
class ProductCatalog extends CatalogSubject {
    // Modifies listing metrics and acts as the trigger for pattern updates.
    public void updateProductCatalogPrice(Product product, double adjustments) {
        System.out.println("\nCatalog Core State Mutation: Listing price points updated.");
        
        // Execute business operation logic... (Price adjusted)
        
        // Broadcast state modification triggers out to all attached subscribers
        notifyObservers(product);
    }
}

// CONCRETE OBSERVER 1: SearchIndexingObserver
// Real-time indexing engine listener.
class SearchIndexingObserver implements CatalogObserver {
    @Override
    public void update(Product product) {
        // Leverages standard methods defined on the Product interface to safely access details
        System.out.println("Search Engine Sync Event: Re-indexing text markers for item: " + product.getDetails());
        // Invokes search ranking engine optimization subroutines from Assignment 1
    }
}

// CONCRETE OBSERVER 2: WishlistAlertObserver
// Customer outreach alerts dispatcher.
class WishlistAlertObserver implements CatalogObserver {
    @Override
    public void update(Product product) {
        System.out.println("Wishlist Monitoring Service: Running matching data queries for tracking users...");
        System.out.println("Notification Engine: Dispatched immediate notification alert to watching customers.");
    }
}