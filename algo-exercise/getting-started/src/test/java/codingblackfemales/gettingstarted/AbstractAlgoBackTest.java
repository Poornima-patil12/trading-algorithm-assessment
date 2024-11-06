package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.container.Actioner;
import codingblackfemales.container.AlgoContainer;
import codingblackfemales.container.RunTrigger;
import codingblackfemales.orderbook.OrderBook;
import codingblackfemales.orderbook.channel.MarketDataChannel;
import codingblackfemales.orderbook.channel.OrderChannel;
import codingblackfemales.orderbook.consumer.OrderBookInboundOrderConsumer;
import codingblackfemales.sequencer.DefaultSequencer;
import codingblackfemales.sequencer.Sequencer;
import codingblackfemales.sequencer.consumer.LoggingConsumer;
import codingblackfemales.sequencer.marketdata.SequencerTestCase;
import codingblackfemales.sequencer.net.TestNetwork;
import codingblackfemales.service.MarketDataService;
import codingblackfemales.service.OrderService;
import messages.marketdata.*;
import messages.marketdata.BookUpdateEncoder.AskBookEncoder;
import messages.marketdata.BookUpdateEncoder.BidBookEncoder;

import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;

public abstract class AbstractAlgoBackTest extends SequencerTestCase {

        protected AlgoContainer container;

        @Override
        public Sequencer getSequencer() {
                final TestNetwork network = new TestNetwork();
                final Sequencer sequencer = new DefaultSequencer(network);

                final RunTrigger runTrigger = new RunTrigger();
                final Actioner actioner = new Actioner(sequencer);

                final MarketDataChannel marketDataChannel = new MarketDataChannel(sequencer);
                final OrderChannel orderChannel = new OrderChannel(sequencer);
                final OrderBook book = new OrderBook(marketDataChannel, orderChannel);

                final OrderBookInboundOrderConsumer orderConsumer = new OrderBookInboundOrderConsumer(book);

                container = new AlgoContainer(new MarketDataService(runTrigger), new OrderService(runTrigger),
                                runTrigger,
                                actioner);
                // set my algo logic
                container.setLogic(createAlgoLogic());

                network.addConsumer(new LoggingConsumer());
                network.addConsumer(book);
                network.addConsumer(container.getMarketDataService());
                network.addConsumer(container.getOrderService());
                network.addConsumer(orderConsumer);
                network.addConsumer(container);

                return sequencer;
        }

        public abstract AlgoLogic createAlgoLogic();

        protected UnsafeBuffer createTick2() {
                final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
                final BookUpdateEncoder encoder = new BookUpdateEncoder();

                final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
                final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

                // write the encoded output to the direct buffer
                encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);

                // set the fields to desired values
                encoder.venue(Venue.XLON);
                encoder.instrumentId(123L);
                encoder.source(Source.STREAM);

                encoder.bidBookCount(3)
                                .next().price(98L).size(100L)
                                .next().price(95L).size(200L)
                                .next().price(91L).size(300L);

                encoder.askBookCount(3)
                                .next().price(100L).size(101L)
                                .next().price(110L).size(200L)
                                .next().price(115L).size(5000L);
                // .next().price(119L).size(5600L)

                encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);

                return directBuffer;
        }

        protected UnsafeBuffer createTickMore() {

                final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
                final BookUpdateEncoder encoder = new BookUpdateEncoder();

                final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(8192);
                final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

                // write the encoded output to the direct buffer
                encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);

                // set the fields to desired values
                encoder.venue(Venue.XLON);
                encoder.instrumentId(124L);

                encoder.askBookCount(4)
                                .next().price(100L).size(101L)
                                .next().price(110L).size(200L)
                                .next().price(115L).size(5000L)
                                .next().price(120L).size(7000L);

                encoder.bidBookCount(4)
                                .next().price(98L).size(100L)
                                .next().price(95L).size(200L)
                                .next().price(91L).size(300L)
                                .next().price(90L).size(400L);

                encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
                encoder.source(Source.STREAM);

                return directBuffer;
        }

        /*
         * protected UnsafeBuffer createTickmore3() {
         * 
         * final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
         * final BookUpdateEncoder encoder = new BookUpdateEncoder();
         * 
         * final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(8192);
         * final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);
         * 
         * // write the encoded output to the direct buffer
         * encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);
         * 
         * // set the common fields
         * encoder.venue(Venue.XLON);
         * encoder.instrumentId(124L);
         * 
         * // Example: Create 225 values for askBook and bidBook (can be dynamically
         * set)
         * int numberOfValues = 225;
         * 
         * // Ask book encoding with 225 values
         * encoder.askBookCount(numberOfValues); // Set the ask book count to 225
         * for (int i = 0; i < numberOfValues; i++) {
         * long price = 100L + i; // Example: increment price by 1 for each iteration
         * long size = 100L + (i * 10); // Example: increment size by 10 for each
         * iteration
         * 
         * // Assuming you have methods like setAskPrice and setAskSize for specific
         * // entries
         * // encoder.askBookCount(i).price(price).size(size); // Use the indexed method
         * // instead of next()
         * AskBookEncoder askEncoder = new AskBookEncoder(encoder); // Assuming it needs
         * a parent reference
         * askEncoder.wrap(directBuffer, i); // Wrap the direct buffer for the current
         * entry
         * askEncoder.price(price); // Set the price
         * askEncoder.size(size); // Set the size
         * }
         * 
         * // Bid book encoding with 225 values
         * encoder.bidBookCount(numberOfValues); // Set the bid book count to 225
         * for (int i = 0; i < numberOfValues; i++) {
         * long price = 98L - i; // Example: decrement price by 1 for each iteration
         * long size = 100L + (i * 10); // Example: increment size by 10 for each
         * iteration
         * 
         * // Assuming you have methods like setBidPrice and setBidSize for specific
         * // entries
         * // encoder.bidBookCount(i).price(price).size(size); // Use the indexed method
         * // instead of next()
         * BidBookEncoder bidEncoder = new BidBookEncoder(encoder); // Assuming it needs
         * a parent reference
         * bidEncoder.wrap(directBuffer, i); // Wrap the direct buffer for the current
         * entry
         * bidEncoder.price(price); // Set the price
         * bidEncoder.size(size); // Set the size
         * }
         * 
         * // Set instrument status and source
         * encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
         * encoder.source(Source.STREAM);
         * 
         * return directBuffer;
         * }
         */
}
