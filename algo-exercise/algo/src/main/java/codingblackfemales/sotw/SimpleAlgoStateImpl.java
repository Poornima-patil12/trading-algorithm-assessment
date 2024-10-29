package codingblackfemales.sotw;

import codingblackfemales.container.Actioner;
import codingblackfemales.container.RunTrigger;
import codingblackfemales.service.MarketDataService;
import codingblackfemales.service.OrderService;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.sequencer.Sequencer;
import codingblackfemales.sequencer.DefaultSequencer;
import codingblackfemales.sequencer.consumer.LoggingConsumer;
import codingblackfemales.container.AlgoContainer;
import codingblackfemales.sequencer.net.TestNetwork;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleAlgoStateImpl implements SimpleAlgoState {

    public final MarketDataService marketDataService;
    public final OrderService orderService;
//    public List<ChildOrder> childOrders;

    public SimpleAlgoStateImpl(final MarketDataService marketDataService, final OrderService orderService) {
        this.marketDataService = marketDataService;
        this.orderService = orderService;
//        this.childOrders = new LinkedList<>();
    }

//    public SimpleAlgoStateImpl(final MarketDataService marketDataService, final OrderService orderService, ChildOrder) {
//        this.marketDataService = marketDataService;
//        this.orderService = orderService;
//        this.childOrders = new LinkedList<>();
//    }

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
//    @Override
//    public ChildOrder getChildOrderById(long orderId) {
//    for (ChildOrder childOrder : childOrders) {
//        if (childOrder.getOrderId() == orderId) {
//            return childOrder; // Return the actual ChildOrder object
//        }
//    }
//    return null; // Return null if no matching child order is found
//}
//    @Override
//    public List<ChildOrder> getChildOrders() {
//    return childOrders; // Return the list of child orders
//    }
    @Override
    public List<ChildOrder> getActiveChildOrders() {
        return orderService.children().stream().filter(order -> order.getState() != OrderState.CANCELLED).collect(Collectors.toList());
    }

//    @Override
//    public Sequencer getSequencer() {
//            final TestNetwork network = new TestNetwork();
//            final Sequencer sequencer = new DefaultSequencer(network);
//
//    final RunTrigger runTrigger = new RunTrigger();
//    final Actioner actioner = new Actioner(sequencer);
//
//        AlgoContainer container = new AlgoContainer(new MarketDataService(runTrigger), new OrderService(runTrigger),
//                runTrigger,
//                actioner);
//            // set my algo logic
//            container.setLogic(createAlgoLogic());
//
//            network.addConsumer(new LoggingConsumer());
//            network.addConsumer(container.getMarketDataService());
//            network.addConsumer(container.getOrderService());
//            network.addConsumer(container);
//
//            return sequencer;
//    }
}
