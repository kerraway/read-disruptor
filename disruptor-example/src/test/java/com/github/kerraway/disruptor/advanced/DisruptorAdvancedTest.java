package com.github.kerraway.disruptor.advanced;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
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

      /**
       * 2.3.1 菱形操作写法一
       *          -> handler 1 ->
       *         /               \
       * start ->                 -> handler 3 -> end
       *         \               /
       *          -> handler 2 ->
       */
      case DIAMOND_I:
        disruptor
            .handleEventsWith(new TradeHandler1(), new TradeHandler2())
            .handleEventsWith(new TradeHandler3());
        break;

      /**
       * 2.3.2 菱形操作写法二
       *          -> handler 1 ->
       *         /               \
       * start ->                 -> handler 3 -> end
       *         \               /
       *          -> handler 2 ->
       */
      case DIAMOND_II:
        EventHandlerGroup<Trade> eventHandlerGroup
            = disruptor.handleEventsWith(new TradeHandler1(), new TradeHandler2());
        eventHandlerGroup.then(new TradeHandler3());
        break;

      /**
       * 2.4 六边形操作
       *          -> handler 1 -> handler 2 ->
       *         /                             \
       * start ->                               -> handler 3 -> end
       *         \                             /
       *          -> handler 4 -> handler 5 ->
       */
      case HEXAGON:
        handleEventsWithHexagon(disruptor);
        break;

      default:
    }
  }

  /**
   * 2.4 六边形操作
   * <pre>
   *          -> handler 1 -> handler 2 ->
   *         /                             \
   * start ->                               -> handler 3 -> end
   *         \                             /
   *          -> handler 4 -> handler 5 ->
   * </pre>
   * 注意：用于构建 {@link Disruptor#Disruptor(EventFactory, int, Executor, ProducerType, WaitStrategy)}
   * 的 {@link Executor} 的线程数，必须大于等于下面要用到的 handler 数量。
   * <p>
   * 原因分析：
   * <p>
   * 在 {@link Disruptor#start()} 被调用时，会遍历 {@link Disruptor#consumerRepository}
   * （有几个 handler，{@link Disruptor#consumerRepository} 中就会有几个 {@link ConsumerInfo} 对象），
   * 调用 {@link ConsumerInfo#start(Executor)} 方法，这里 {@link ConsumerInfo} 实际是 {@link EventProcessorInfo}，
   * 在 {@link EventProcessorInfo#start(Executor)} 方法中，会调用 {@link Executor#execute(Runnable)} 方法，
   * 将 {@link EventProcessor} 传进去，这里实际上是其子类 {@link BatchEventProcessor}，所以
   * {@link BatchEventProcessor#run()} 方法最终会被调用，该方法中，
   * 会具体调用 {@link EventHandler#onEvent(Object, long, boolean)} 方法，也就对应 handler 1 ~ 5 的具体方法。
   * <p>
   * 每一个 handler 都对应一个 {@link BatchEventProcessor}，故会占用一个线程，所以 {@link Executor} 的线程数，
   * 必须大于等于下面要用到的 handler 数量。
   */
  private void handleEventsWithHexagon(Disruptor<Trade> disruptor) {
    //sleep 1000 ms
    TradeHandler1 handler1 = new TradeHandler1();
    //sleep 1500 ms
    TradeHandler2 handler2 = new TradeHandler2();
    //sleep 0 ms
    TradeHandler3 handler3 = new TradeHandler3();
    //sleep 1000 ms
    TradeHandler4 handler4 = new TradeHandler4();
    //sleep 1500 ms
    TradeHandler5 handler5 = new TradeHandler5();
    //handler 1 和 handler 4 并行执行
    disruptor.handleEventsWith(handler1, handler4);
    //handler 1 执行完成之后，执行 handler 2
    disruptor.after(handler1).handleEventsWith(handler2);
    //handler 4 执行完成之后，执行 handler 5
    disruptor.after(handler4).handleEventsWith(handler5);
    //handler 2 和 handler 5 执行完成之后，执行 handler 3
    disruptor.after(handler2, handler5).handleEventsWith(handler3);
  }

}