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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);
    private static final long BUY_THRESHOLD = 90L;
    private static final long SELL_THRESHOLD = 110L;

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

        // Check current count of active BUY and SELL orders
        long buyOrderCount = state.getChildOrders().stream().filter(order -> order.getSide() == Side.BUY).count();

        // Buy condition: Place BUY order if bid price meets threshold and count is below limit
        if (bidPrice >= BUY_THRESHOLD && buyOrderCount < 3) {

            final long spread = Math.abs(askPrice - bidPrice); // added 5/10/2024 to get absolute value of a number
            final long spreadThreshold = 5L;

            // Define the buy quantity
            bidQuantity = 200L;
            if (spread <= spreadThreshold) {

                logger.info("[MYALGO] BUY CONDITIONS - Best Bid Qty " + bidQuantity + " units, " + "BestBid " + bidPrice);
                logger.info("[MYALGO] BUY CONDITIONS - Best Ask Qty " + askQuantity + " units, " + "BestAsk " + askPrice);
                logger.info("[BuySellAlgo] Placing BUY order - Bid Price: " + bidPrice + ", Quantity: " + bidQuantity);
                return new CreateChildOrder(Side.BUY, bidQuantity, bidPrice);
            }
            else {
                // Log if the spread does not meet the threshold
                logger.info("[MYALGO] BUY CONDITIONS - Spread is " + spread + " points.");
                logger.info("[MYALGO] BUY CONDITIONS - Spread is above the buying threshold. No buy order created.");
            }
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