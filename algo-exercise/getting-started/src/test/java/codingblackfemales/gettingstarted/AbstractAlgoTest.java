package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.container.Actioner;
import codingblackfemales.container.AlgoContainer;
import codingblackfemales.container.RunTrigger;
import codingblackfemales.sequencer.DefaultSequencer;
import codingblackfemales.sequencer.Sequencer;
import codingblackfemales.sequencer.consumer.LoggingConsumer;
import codingblackfemales.sequencer.marketdata.SequencerTestCase;
import codingblackfemales.sequencer.net.TestNetwork;
import codingblackfemales.service.MarketDataService;
import codingblackfemales.service.OrderService;
import messages.marketdata.*;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;

public abstract class AbstractAlgoTest extends SequencerTestCase {

        protected AlgoContainer container;

        @Override
        public Sequencer getSequencer() {
                final TestNetwork network = new TestNetwork();
                final Sequencer sequencer = new DefaultSequencer(network);

        final RunTrigger runTrigger = new RunTrigger();
        final Actioner actioner = new Actioner(sequencer);

                container = new AlgoContainer(new MarketDataService(runTrigger), new OrderService(runTrigger),
                                runTrigger,
                                actioner);
                // set my algo logic
                container.setLogic(createAlgoLogic());

                network.addConsumer(new LoggingConsumer());
                network.addConsumer(container.getMarketDataService());
                network.addConsumer(container.getOrderService());
                network.addConsumer(container);

                return sequencer;
        }

        public abstract AlgoLogic createAlgoLogic();

        protected UnsafeBuffer createTick() {

                final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
                final BookUpdateEncoder encoder = new BookUpdateEncoder();

                final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
                final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

                // Wrap the encoder with the direct buffer
    encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);

    // Set the venue and instrument ID
    encoder.venue(Venue.XLON);
    encoder.instrumentId(123L);

    // Set up ask book
    encoder.askBookCount(3)
            .next().price(100L).size(101L)
            .next().price(110L).size(200L)
            .next().price(115L).size(5000L);

    // Set up bid book (prices > 90, no cancel trigger)
    encoder.bidBookCount(3)
            .next().price(98L).size(100L)
            .next().price(95L).size(200L)
            .next().price(91L).size(300L);

    // Set instrument status and data source
    encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
    encoder.source(Source.STREAM);

    return directBuffer;
}
protected UnsafeBuffer createTick2() {
        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();
    
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);
encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);

// Set the venue and instrument ID
encoder.venue(Venue.XLON);
encoder.instrumentId(123L);

// Set up ask book with different prices
encoder.askBookCount(4)
        .next().price(98L).size(501L)
        .next().price(101L).size(200L)
        .next().price(110L).size(5000L)
        .next().price(119L).size(5600L);

// Set up bid book (still > 90)
encoder.bidBookCount(3)
        .next().price(95L).size(100L)
        .next().price(93L).size(200L)
        .next().price(91L).size(300L);

// Set instrument status and data source
encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
encoder.source(Source.STREAM);

return directBuffer;
}
        protected UnsafeBuffer createCancelTriggerTick() {

                final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
                final BookUpdateEncoder encoder = new BookUpdateEncoder();

                final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
                final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

                encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);

                // Set the venue and instrument ID
                encoder.venue(Venue.XLON);
                encoder.instrumentId(123L);
            
                // Set up ask book (no change)
                encoder.askBookCount(3)
                        .next().price(100L).size(101L)
                        .next().price(110L).size(200L)
                        .next().price(115L).size(5000L);
            
                // Set up bid book (with bid price < 90, triggering cancellation)
                encoder.bidBookCount(3)
                        .next().price(89L).size(100L)  // Bid price < 90
                        .next().price(88L).size(200L)
                        .next().price(85L).size(300L);
            
                // Set instrument status and data source
                encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
                encoder.source(Source.STREAM);
            
                return directBuffer;
            }
            protected UnsafeBuffer createTickForMovingAverage() {
                final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
                final BookUpdateEncoder encoder = new BookUpdateEncoder();
            
                final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
                final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);
            
                // Wrap the encoder with the direct buffer
                encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);
            
                // Set the venue and instrument ID
                encoder.venue(Venue.XLON);
                encoder.instrumentId(123L);
            
                // Set up ask book with a rising trend in prices (simulates a bullish crossover condition)
                encoder.askBookCount(3)
                    .next().price(102L).size(150L)   // Increased ask price and volume
                    .next().price(105L).size(300L)
                    .next().price(108L).size(600L);
            
                // Set up bid book with corresponding prices to support a bullish signal
                encoder.bidBookCount(3)
                    .next().price(101L).size(150L)
                    .next().price(104L).size(300L)
                    .next().price(107L).size(600L);
            
                // Set instrument status and data source
                encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
                encoder.source(Source.STREAM);
            
                return directBuffer;
            }
        }