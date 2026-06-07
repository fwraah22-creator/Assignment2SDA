import java.util.ArrayList;
import java.util.List;


// 1. BEHAVIORAL PATTERN: OBSERVER INTERFACES & SUBJECT

// Observer interface for receiving payment status updates
interface PaymentObserver {
    void update(String orderID, String status);
}

// Subject interface for managing observers and broadcasting notifications
interface PaymentSubject {
    void attach(PaymentObserver o);
    void detach(PaymentObserver o);
}

// 2. CORE INTERFACES

// Target interface expected by the core order processing system
interface PaymentMethod {
    void pay();
    void statusPayment(String type);
    void totalPayment(String type);
}

// Adaptee interface representing external financial processing systems
interface FinancialSystems {
    void payMethod();
    void displayStatusPayment(String type);
    void displayTotalPayment(String type);
}


// 3. ADAPTEE CLASSES 

// Concrete implementation for Cash on Delivery payment processing
class COD implements FinancialSystems {
    private String paymentAddress;

    public COD(String paymentAddress) {
        this.paymentAddress = paymentAddress;
    }

    @Override
    public void payMethod() {
        System.out.println("Executing Cash on Delivery transactional logic...");
        takePayment();
    }

    @Override
    public void displayStatusPayment(String type) {
        System.out.println("COD Status [" + type + "]: Awaiting delivery verification.");
    }

    @Override
    public void displayTotalPayment(String type) {
        System.out.println("COD Total computed for type: " + type);
    }

    public void takePayment() {
        System.out.println("Collection scheduled at destination address: " + paymentAddress);
    }
}

// Concrete implementation for Bank Transfer payment processing
class BankAcc implements FinancialSystems {
    private String accountNo;
    private String bankName;

    public BankAcc(String accountNo, String bankName) {
        this.accountNo = accountNo;
        this.bankName = bankName;
    }

    @Override
    public void payMethod() {
        System.out.println("Initializing secure Bank Transfer gateway via " + bankName + "...");
        save();
    }

    @Override
    public void displayStatusPayment(String type) {
        System.out.println("Bank Transfer Status [" + type + "]: Processing institutional clearance.");
    }

    @Override
    public void displayTotalPayment(String type) {
        System.out.println("Bank Transfer Total settled for type: " + type);
    }

    public void addAccNo() {
        System.out.println("Account mapping verified for trace identifier: " + accountNo);
    }

    public void save() {
        System.out.println("Transaction payload committed securely to institutional financial ledger.");
    }
}

// Concrete implementation for Credit Card payment processing
class CreditCard implements FinancialSystems {
    private String number;
   
    public CreditCard(String number) {
        this.number = number;
    }

    @Override
    public void payMethod() {
        System.out.println("Processing credit instrument transaction authorization sequence...");
        if (authorized()) {
            System.out.println("Credit clearing network accepted payment token successfully.");
        }
    }

    @Override
    public void displayStatusPayment(String type) {
        System.out.println("Credit Card Status [" + type + "]: Merchant settlement authorized.");
    }

    @Override
    public void displayTotalPayment(String type) {
        System.out.println("Credit Instrument Total cleared for category: " + type);
    }

    public boolean authorized() {
        System.out.println("Validating safety parameters for token sequence: " + number.substring(0, 4) + "-XXXX-XXXX");
        return true;
    }
}

// 4. STRUCTURAL PATTERN: THE ADAPTER IMPLEMENTATION

// Adapter class that bridges the core order processing system with external financial systems
class PaymentGatewayAdapter implements PaymentMethod {
    private String gatewayName;
    private FinancialSystems financialSystem;

    public PaymentGatewayAdapter(String gatewayName, FinancialSystems financialSystem) {
        this.gatewayName = gatewayName;
        this.financialSystem = financialSystem;
    }

    @Override
    public void pay() {
        System.out.println("\n[Adapter Context] Delegating core platform execution to gateway: " + gatewayName);
        financialSystem.payMethod();
    }

    @Override
    public void statusPayment(String type) {
        financialSystem.displayStatusPayment(type);
    }

    @Override
    public void totalPayment(String type) {
        financialSystem.displayTotalPayment(type);
    }
}

// 5. CREATIONAL PATTERN: FACTORY METHOD WITH PROXY 

// Abstract Factory defining the contract for creating payment method instances
abstract class PaymentProcessorFactory {
    protected String paymentID;
    protected String paymentStatus;
    protected double amount;
    protected String paymentMethod;

    public abstract PaymentMethod createPaymentMethod(String type);

    public void displayPaymentMethod(String type) {
        PaymentMethod method = createPaymentMethod(type);
        System.out.println("Factory Context initialized operational contract for type: " + type);
        method.statusPayment("INITIALIZED");
    }
}

// Proxy class that controls access to the payment method creation process, 
// adding an additional layer of validation and logging
class PaymentGatewayProxy extends PaymentProcessorFactory {
    private String gatewayName;

    public PaymentGatewayProxy(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    @Override
    public PaymentMethod createPaymentMethod(String type) {
        if (type == null) return null;
        
        if (type.equalsIgnoreCase("COD")) {
            COD externalCod = new COD("123, Jalan Sultan Ismail, Kuala Lumpur");
            return new PaymentGatewayAdapter("Shopee-COD-Bridge", externalCod);
        } else if (type.equalsIgnoreCase("BANK")) {
            // Constructor call
            BankAcc externalBank = new BankAcc("A24MJ5057-991", "Maybank2u");
            return new PaymentGatewayAdapter("Shopee-Maybank-API", externalBank);
        } else if (type.equalsIgnoreCase("CREDIT")) {
            // Constructor call
            CreditCard externalCard = new CreditCard("4321-5678-9012-3456");
            return new PaymentGatewayAdapter("Shopee-Visa-CyberSource", externalCard);
        }
        
        throw new IllegalArgumentException("Unknown financial processing instrument type: " + type);
    }

    public void validateTransaction() {
        System.out.println("[Proxy Validation] Verifying network request headers for proxy handler: " + gatewayName);
    }

    public String returnStatus() {
        return "PROXY_READY";
    }
}

// 6. CONCRETE SUBJECT & CONCRETE OBSERVERS 

// Concrete Subject that manages order processing and notifies observers of payment status changes
class OrderController implements PaymentSubject {
    private List<PaymentObserver> observers;
    private String orderID;
    private String status;
    private double totalAmount;

    public OrderController(String orderID, double totalAmount) {
        this.observers = new ArrayList<>();
        this.orderID = orderID;
        this.totalAmount = totalAmount;
        this.status = "PENDING";
    }

    @Override
    public void attach(PaymentObserver o) {
        if (o != null && !observers.contains(o)) {
            observers.add(o);
        }
    }

    @Override
    public void detach(PaymentObserver o) {
        if (o != null) {
            observers.remove(o);
        }
    }

    protected void notifyObservers(String orderID, String status) {
        for (PaymentObserver observer : observers) {
            observer.update(orderID, status);
        }
    }

    public void confirmOrder(String clientSelectedPaymentType) {
        System.out.println("\n=======================================================");
        System.out.println("Processing Checkout Order Framework for Request ID: " + orderID);
        System.out.println("=======================================================");
        
        PaymentGatewayProxy proxy = new PaymentGatewayProxy("MY_MAIN_GATEWAY_PROXY");
        proxy.validateTransaction();
        
        PaymentMethod processingInstrument = proxy.createPaymentMethod(clientSelectedPaymentType);
        processingInstrument.pay();
        
        this.status = "SUCCESS";
        System.out.println("\n[Order Status Mutation] Mutation finalized. System Status updated to: " + this.status);
        
        notifyObservers(this.orderID, this.status);
    }

    public void generateReceipt() {
        System.out.println("System Receipt printed for transaction aggregate value: RM " + totalAmount);
    }
}

// Observer that manages client-side cart operations and cache management upon payment status updates
class CartManager implements PaymentObserver {
    private String cartID;

    public CartManager(String cartID) {
        this.cartID = cartID;
    }

    @Override
    public void update(String orderID, String status) {
        if (status.equalsIgnoreCase("SUCCESS")) {
            System.out.println("CartManager Context [ID: " + cartID + "] intercepted event. Order reference: " + orderID);
            System.out.println(" -> Clearing active client database memory cache records. Status: CLEAR.");
        }
    }
}

// Observer that synchronizes logistics operations with order fulfillment status updates
class LogisticsPartner implements PaymentObserver {
    private String partnerID;

    public LogisticsPartner(String partnerID) {
        this.partnerID = partnerID;
    }

    public void confirmOrder() {
        System.out.println("Logistics dispatch schedule synchronized with core order fulfillment registry.");
    }

    @Override
    public void update(String orderID, String status) {
        if (status.equalsIgnoreCase("SUCCESS")) {
            System.out.println("LogisticsPartner [ID: " + partnerID + "] intercepted event notification for Order: " + orderID);
            confirmOrder();
            System.out.println(" -> Generating courier payload markers. Dispatched to warehouse fulfillment queue.");
        }
    }
}

// 7. MAIN DEMONSTRATION RUNTIME

// Main class to demonstrate the integrated functionality of the Order Payment Module with 
// Observer, Adapter, Factory, and Proxy patterns
class Main {
    public static void main(String[] args) {
        OrderController orderSystem = new OrderController("ORD-2026-99542", 249.50);

        CartManager shoppingCart = new CartManager("CART-MY-User8821");
        LogisticsPartner courierService = new LogisticsPartner("LOGI-J&T-Express");

        orderSystem.attach(shoppingCart);
        orderSystem.attach(courierService);

        orderSystem.confirmOrder("CREDIT");
        orderSystem.generateReceipt();
    }
}