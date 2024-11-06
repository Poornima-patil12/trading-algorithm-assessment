package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.container.AlgoContainer;
import codingblackfemales.sequencer.net.Consumer;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;
import messages.order.Side;
import codingblackfemales.algo.AddCancelAlgoLogic;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);
    private static final long BUY_THRESHOLD = 90L;
    private static final long SELL_THRESHOLD = 110L;
    
    private static final long SPREAD_COMPRESSION_THRESHOLD = 5L;
    private static final int MA_PERIOD = 10; // Short-term moving average period

    // To track recent bid prices for moving average calculation
    private final Queue<Long> recentBidPrices = new LinkedList<>();

    @Override
    public Action evaluate(SimpleAlgoState state) {
        logger.info("[BuySellAlgo] Evaluating market conditions...");

        // Obtain top bid and ask levels
        final BidLevel topBid = state.getBidAt(0);
        final AskLevel topAsk = state.getAskAt(0);

        long bidPrice = topBid.price;
        long bidQuantity = topBid.quantity;
        long askPrice = topAsk.price;
        long askQuantity = topAsk.quantity;

        // Track recent bid prices for moving average
        if (recentBidPrices.size() >= MA_PERIOD) {
            recentBidPrices.poll(); // Remove oldest price
        }
        recentBidPrices.add(bidPrice);

        // Calculate moving average of bid prices
        long movingAverage = recentBidPrices.stream().mapToLong(Long::longValue).sum() / recentBidPrices.size();

        // Check current count of active BUY and SELL orders
        long buyOrderCount = state.getChildOrders().stream().filter(order -> order.getSide() == Side.BUY).count();

        // Buy condition 1: Moving average strategy
        if (bidPrice > movingAverage && buyOrderCount < 3) {
            logger.info("[BuySellAlgo] Moving average strategy: Placing BUY order - Bid Price: " + bidPrice + ", Quantity: " + bidQuantity);
            return new CreateChildOrder(Side.BUY, bidQuantity, bidPrice);
        }

       // Buy condition 3: Breakout strategy
        long recentHigh = recentBidPrices.stream().mapToLong(Long::longValue).max().orElse(bidPrice);
        if (bidPrice > recentHigh && buyOrderCount < 3) {
            logger.info("[BuySellAlgo] Breakout strategy: Placing BUY order - Bid Price: " + bidPrice + ", Quantity: " + bidQuantity);
            return new CreateChildOrder(Side.BUY, bidQuantity, bidPrice);
        }

        // Buy condition 4: Spread compression
        long spread = Math.abs(askPrice - bidPrice);
        if (spread <= SPREAD_COMPRESSION_THRESHOLD && buyOrderCount < 3) {
            logger.info("[BuySellAlgo] Spread compression: Placing BUY order - Bid Price: " + bidPrice + ", Quantity: " + bidQuantity);
            return new CreateChildOrder(Side.BUY, bidQuantity, bidPrice);
        }


        // Cancel BUY orders if bid price falls below threshold
        if (bidPrice < BUY_THRESHOLD && buyOrderCount > 0) {
            logger.info("[BuySellAlgo] Bid price below threshold. Attempting to cancel BUY orders...");
            return cancelBuyOrders(state);
        }

        // Cancel SELL orders if ask price rises above threshold


        logger.info("[BuySellAlgo] No action required.");
        return NoAction.NoAction;
    }



    // Cancels existing BUY orders if conditions for canceling are met
    private Action cancelBuyOrders(SimpleAlgoState state) {
        for (ChildOrder order : state.getActiveChildOrders()) {
            if (order.getSide() == Side.BUY) {
                logger.info("[BuySellAlgo] Canceling BUY order at price: " + order.getPrice());
                return new CancelChildOrder(order);
            }
        }
        return NoAction.NoAction;
    }
}