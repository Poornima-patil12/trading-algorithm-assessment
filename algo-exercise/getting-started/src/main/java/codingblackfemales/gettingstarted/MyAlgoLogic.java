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
    protected AlgoContainer container;
    protected SimpleAlgoState state;

    @Override
    public Action evaluate(SimpleAlgoState state) {
        logger.info("[MYALGO] In Algo Logic....");

        var orderBookAsString = Util.orderBookToString(state);
        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);

        final BidLevel nearTouch = state.getBidAt(0); // top bid
        long bidQuantity = nearTouch.quantity;
        long bidPrice = nearTouch.price;

        // Get the top ask level (best price on the offer side)
        final AskLevel offerTouch = state.getAskAt(0); // top ask
        long offerQuantity = offerTouch.quantity;
        long offerPrice = offerTouch.price;

        // Manually creating and canceling child orders
        if (state.getChildOrders().size() < 3) {
            logger.info("[MYALGO] Have:" + state.getChildOrders().size()
                    + " children, need 3. Creating BUY child order with quantity: " + bidQuantity + " @ " + bidPrice);
            // Create new BUY child order
            return new CreateChildOrder(Side.BUY, bidQuantity, bidPrice);
        } else {
            logger.info("[MYALGO] Have 3 or more child orders. No need to create more.");
        }

        // Check if bidPrice is below the threshold (90) and manually cancel child orders
        if (shouldCancelOrders(bidPrice)) {
            logger.info("[MYALGO] Bid price is below 90, checking for child orders to cancel...");
            return cancelChildOrders(bidPrice);  // Prioritize canceling child orders
        }

        // If no action required, return NoAction
        logger.info("[MYALGO] No action required.");
        return NoAction.NoAction;
    }

    // Method to manually cancel child orders
    public Action cancelChildOrders(long bidPrice) {
        logger.info("[MYALGO] Manually canceling child orders...");

        // Loop through each child order and cancel based on criteria
        for (var childOrder : state.getActiveChildOrders()) {
            if (shouldCancelOrder(childOrder, bidPrice)) {
                logger.info("[MYALGO] Canceling child order with price: " + childOrder.getPrice());
                return new CancelChildOrder(childOrder); // Cancel each matching child order
            }
        }

        logger.info("[MYALGO] No child orders were canceled.");
        return NoAction.NoAction;  // Return NoAction if no cancellations were made
    }

    // Helper method to decide whether to cancel a specific child order
    private boolean shouldCancelOrder(ChildOrder order, long bidPrice) {
        return bidPrice < 90 && order.getPrice() > bidPrice;  // Cancel if bid price is below 90
    }

    // Helper method to check if the overall order cancellation condition is met
    private boolean shouldCancelOrders(long bidPrice) {
        return bidPrice < 90;
    }
}