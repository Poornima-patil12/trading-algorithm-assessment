package codingblackfemales.gettingstarted;

//import static org.junit.Assert.assertEquals;
import codingblackfemales.algo.AlgoLogic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * This test is designed to check your algo behavior in isolation of the order
 * book.
 *
 * You can tick in market data messages by creating new versions of createTick()
 * (ex. createTick2, createTickMore etc..)
 *
 * You should then add behaviour to your algo to respond to that market data by
 * creating or cancelling child orders.
 *
 * When you are comfortable you algo does what you expect, then you can move on
 * to creating the MyAlgoBackTest.
 *
 */
public class MyAlgoTest extends AbstractAlgoTest {

    @Override
    public AlgoLogic createAlgoLogic() {
        // this adds your algo logic to the container classes
        return new MyAlgoLogic();
    }
    
    @Test
    public void testDispatchThroughSequencer() throws Exception {

        send(createTick());
        System.out.println("Child orders after createTick: " + container.getState().getChildOrders().size());
    System.out.println("Active child orders after createTick: " + container.getState().getActiveChildOrders().size());


        assertEquals(3, container.getState().getActiveChildOrders().size());
        send(createTick2());
        System.out.println("Child orders after createTick2: " + container.getState().getChildOrders().size());
        System.out.println("Active child orders after createTick2: " + container.getState().getActiveChildOrders().size());


        assertEquals(3, container.getState().getActiveChildOrders().size());
    }
    @Test
    public void testCancelChildOrders() throws Exception {
        // Step 1: Simulate creating 3 child orders with a market data tick
       // send(createTick());

        // Step 2: Verify that 3 child orders have been created
      //assertEquals(container.getState().getChildOrders().size(),3);

        // Step 3: Simulate a market tick where the bid price is below 90
        send(createCancelTriggerTick());

        System.out.println("Child orders after createCancelTriggerTick: " + container.getState().getChildOrders().size());
        System.out.println("Active child orders after createCancelTriggerTick: " + container.getState().getActiveChildOrders().size());

        // Step 4: Check that no child orders remain active after cancellation
        assertEquals(0,container.getState().getActiveChildOrders().size());
    }

}
