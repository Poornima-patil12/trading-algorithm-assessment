package codingblackfemales.sotw;

import codingblackfemales.service.MarketDataService;
import codingblackfemales.service.OrderService;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.sotw.marketdata.BidLevel;

import java.util.List;
import java.util.stream.Collectors;

public class SimpleAlgoStateImpl implements SimpleAlgoState {

    public final MarketDataService marketDataService;
    public final OrderService orderService;
    public List<ChildOrder> childOrders;

    public SimpleAlgoStateImpl(final MarketDataService marketDataService, final OrderService orderService, ChildOrder) {
        this.marketDataService = marketDataService;
        this.orderService = orderService;
        this.childOrders = new LinkedList<>(); 
    }
     // Overloaded constructor that accepts a list of ChildOrder objects
     public SimpleAlgoStateImpl(final MarketDataService marketDataService, final OrderService orderService, List<ChildOrder> childOrders) {
        this.marketDataService = marketDataService;
        this.orderService = orderService;
        this.childOrders = childOrders; // Initialize with a provided list of child orders
    }

    @Override
    public long getInstrumentId() {
        return marketDataService.getInstrumentId();
    }

    @Override
    public String getSymbol() {
        return null;
    }

    @Override
    public int getBidLevels() {
        return marketDataService.getBidLength();
    }

    @Override
    public int getAskLevels() {
        return marketDataService.getAskLength();
    }

    @Override
    public BidLevel getBidAt(int index) {
        return marketDataService.getBidLevel(index);
    }

    @Override
    public AskLevel getAskAt(int index) {
        return marketDataService.getAskLevel(index);
    }

    @Override
    public List<ChildOrder> getChildOrders() {
        return orderService.children();
    }
    @Override
    public ChildOrder getChildOrderById(long orderId) {
    for (ChildOrder childOrder : childOrders) {
        if (childOrder.getOrderId() == orderId) {
            return childOrder; // Return the actual ChildOrder object
        }
    }
    return null; // Return null if no matching child order is found
}
    @Override
    public List<ChildOrder> getChildOrders() {
    return childOrders; // Return the list of child orders
    }
    @Override
    public List<ChildOrder> getActiveChildOrders() {
        return orderService.children().stream().filter(order -> order.getState() != OrderState.CANCELLED).collect(Collectors.toList());
    }
}
