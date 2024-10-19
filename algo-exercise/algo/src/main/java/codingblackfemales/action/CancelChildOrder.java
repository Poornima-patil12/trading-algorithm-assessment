package codingblackfemales.action;

import codingblackfemales.sequencer.Sequencer;
import codingblackfemales.sotw.ChildOrder;
import messages.order.CancelOrderEncoder;
import messages.order.MessageHeaderEncoder;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;

public class CancelChildOrder implements Action{

    private final ChildOrder orderToCancel;
    private final long orderId; 
    
    public CancelChildOrder(long orderId) {
        this.orderId = orderId;
    }
    public CancelChildOrder(ChildOrder orderToCancel) {
        this.orderId = orderToCancel.getOrderId();
    }

    @Override
    public String toString() {
        return "CancelChildOrder{id=" + orderToCancel + "}";
    }

    @Override
    public void apply(final Sequencer sequencer) {

        final CancelOrderEncoder encoder = new CancelOrderEncoder();
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);
        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();

        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);
        headerEncoder.schemaId(CancelOrderEncoder.SCHEMA_ID);
        headerEncoder.version(CancelOrderEncoder.SCHEMA_VERSION);

        encoder.orderId(orderToCancel.getOrderId());

        sequencer.onCommand(directBuffer);
    }

    private static final Logger logger = LoggerFactory.getLogger(CancelChildOrder.class);

    @Override
    public void execute(SimpleAlgoState state) {
        List<ChildOrder> childOrders = state.getChildOrders();

        if (childOrders.isEmpty()) {
            logger.info("[CancelAllChildOrders] No child orders to cancel.");
            return;
        }

        // Loop through each child order and cancel it
        for (ChildOrder childOrder : childOrders) {
            logger.info("[CancelAllChildOrders] Canceling child order: ID=" + childOrder.getOrderId() + ", Quantity=" + childOrder.getQuantity() + ", Price=" + childOrder.getPrice());
            
            // Create a CancelChildOrder action for each child order
            CancelChildOrder cancelAction = new CancelChildOrder(childOrder);
            
            // Apply the cancel action to the sequencer
            cancelAction.apply(state.getSequencer());
        }

        logger.info("[CancelAllChildOrders] All child orders have been processed for cancellation.");
    }
}
