package codingblackfemales.gettingstarted;

import static org.junit.Assert.assertEquals;

import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;

import org.junit.Test;

/**
 * This test plugs together all of the infrastructure, including the order book
 * (which you can trade against)
 * and the market data feed.
 *
 * If your algo adds orders to the book, they will reflect in your market data
 * coming back from the order book.
 *
 * If you cross the srpead (i.e. you BUY an order with a price which is == or >
 * askPrice()) you will match, and receive
 * a fill back into your order from the order book (visible from the algo in the
 * childOrders of the state object.
 *
 * If you cancel the order your child order will show the order status as
 * cancelled in the childOrders of the state object.
 *
 */
public class MyAlgoBackTest extends AbstractAlgoBackTest {

    @Override
    public AlgoLogic createAlgoLogic() {
        return new MyAlgoLogic();
    }

    @Test
    public void testExampleBackTest() throws Exception {
        // create a sample market data tick....
        send(createTick2());

        // ADD asserts when you have implemented your algo logic
        assertEquals(container.getState().getChildOrders().size(), 3);

        send(createTick2());
        send(createTickMore());

        // Step 3: Retrieve the current state and calculate filled quantity
        var state = container.getState();
        long filledQuantity = state.getChildOrders()
                .stream()
                .mapToLong(ChildOrder::getFilledQuantity)
                .sum();
        assertEquals("Expected total filled quantity of 700", 700, filledQuantity);

        // Step 4: Cancel unfilled orders
        state.getChildOrders().forEach(order -> {
            if (order.getUnfilledQuantity() > 0) {
                new CancelChildOrder(order);
            }
        });

        // Step 5: Send another tick to confirm cancellations and state
        send(createTickMore());

        // Assert that the active child orders count is as expected after cancellations
        assertEquals("Expected 3 active child orders after canceling unfilled orders", 3,
                container.getState().getActiveChildOrders().size());

    }

}