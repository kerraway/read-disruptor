package com.github.kerraway.disruptor.advanced;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author kerraway
 * @date 2019/3/11
 */
@Slf4j
public class DisruptorAdvancedTest {

  @Test
  public void advancedTest() throws InterruptedException {
    for (DisruptorAdvancedOption advancedOption : DisruptorAdvancedOption.values()) {
      logger.info("{}", advancedOption.getDesc());
      advancedTest(advancedOption);
      logger.info("");
    }
  }

  private void advancedTest(DisruptorAdvancedOption advancedOption) throws InterruptedException {
    ExecutorService executor4Disruptor = Executors.newFixedThreadPool(5);
    //1 构建 disruptor
    Disruptor<Trade> disruptor = new Disruptor<>(
        //trade event factory
        Trade::new,
        //ring buffer size
        1024 * 1024,
        //executor
        executor4Disruptor,
        //MULTI / SINGLE
        ProducerType.SINGLE,
        //wait strategy
        new BusySpinWaitStrategy()
    );

    //2 把消费者设置到 disruptor 中去
    handleEventsWith(advancedOption, disruptor);

    //3 启动 disruptor
    RingBuffer<Trade> ringBuffer = disruptor.start();
    CountDownLatch countDownLatch = new CountDownLatch(1);

    Instant start = Instant.now();
    ExecutorService executor4Publisher = Executors.newFixedThreadPool(1);
    executor4Publisher.submit(new TradePublisher(disruptor, countDownLatch, 1));

    countDownLatch.await();

    disruptor.shutdown();
    executor4Disruptor.shutdown();
    executor4Publisher.shutdown();

    logger.info("End, use {} ms", Duration.between(start, Instant.now()).toMillis());
  }

  private void handleEventsWith(DisruptorAdvancedOption advancedOption, Disruptor<Trade> disruptor) {
    switch (advancedOption) {
      //2.1 串行操作
      case SERIAL:
        disruptor
            .handleEventsWith(new TradeHandler1())
            .handleEventsWith(new TradeHandler2())
            .handleEventsWith(new TradeHandler3());
        break;

      //2.2.1 并行操作写法一
      case PARALLEL_I:
        disruptor.handleEventsWith(new TradeHandler1(), new TradeHandler2(), new TradeHandler3());
        break;

      //2.2.2 并行操作写法二
      case PARALLEL_II:
        disruptor.handleEventsWith(new TradeHandler1());
        disruptor.handleEventsWith(new TradeHandler2());
        disruptor.handleEventsWith(new TradeHandler3());
        break;

      //2.3.1 菱形操作写法一
      case DIAMOND_I:
        disruptor
            .handleEventsWith(new TradeHandler1(), new TradeHandler2())
            .handleEventsWith(new TradeHandler3());
        break;

      //2.3.1 菱形操作写法一
      case DIAMOND_II:
        EventHandlerGroup<Trade> eventHandlerGroup
            = disruptor.handleEventsWith(new TradeHandler1(), new TradeHandler2());
        eventHandlerGroup.then(new TradeHandler3());
        break;

      //2.4 六边形操作
      case HEXAGON:
        TradeHandler1 handler1 = new TradeHandler1();
        TradeHandler2 handler2 = new TradeHandler2();
        TradeHandler3 handler3 = new TradeHandler3();
        TradeHandler4 handler4 = new TradeHandler4();
        TradeHandler5 handler5 = new TradeHandler5();
        disruptor.handleEventsWith(handler1, handler4);
        disruptor.after(handler1).handleEventsWith(handler2);
        disruptor.after(handler4).handleEventsWith(handler5);
        disruptor.after(handler2, handler5).handleEventsWith(handler3);
        break;

      default:
    }
  }

}