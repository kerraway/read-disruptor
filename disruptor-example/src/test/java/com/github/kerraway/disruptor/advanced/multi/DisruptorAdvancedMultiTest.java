package com.github.kerraway.disruptor.advanced.multi;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.*;

/**
 * @author kerraway
 * @date 2019/3/16
 */
@Slf4j
public class DisruptorAdvancedMultiTest {

  /**
   * 多消费者模式，当消费者数量 > 线程数时，程序可以正常执行，只是实际上只有与线程数相等个数的消费者会工作。
   * <p>
   * 原因分析：
   * <p>
   * 在 {@link WorkerPool#start(Executor)} 方法中，会遍历 {@link WorkerPool#workProcessors}，
   * 并将每一个 {@link WorkProcessor} 传递给 {@link Executor#execute(Runnable)} 方法，
   * 每一个 {@link WorkProcessor} 都持有一个 {@link WorkHandler} 的对象，也即用户自定义的消费者，
   * 这里是 {@link OrderConsumer}，在 {@link WorkProcessor#run()} 方法中，会具体调用
   * {@link WorkHandler#onEvent(Object)} 方法。
   * <p>
   * 因此，{@link Executor} 有几个工作线程，对应就会有多少个消费者工作。
   */
  @Test
  public void advancedMultiTest() throws InterruptedException {
    //1 构建 disruptor
    RingBuffer<Order> ringBuffer = RingBuffer.create(
        ProducerType.MULTI, Order::new, 1024 * 1024, new YieldingWaitStrategy());

    //2 通过 ringBuffer 创建一个屏障
    SequenceBarrier sequenceBarrier = ringBuffer.newBarrier();

    //3 创建多个消费者
    OrderConsumer[] consumers = new OrderConsumer[10];
    for (int i = 0; i < consumers.length; i++) {
      consumers[i] = new OrderConsumer("Order consumer " + i);
    }

    //4 创建多个消费者数组
    WorkerPool<Order> workerPool
        = new WorkerPool<>(ringBuffer, sequenceBarrier, new OrderExceptionHandler(), consumers);

    //5 设置多个消费者的 sequence 序号，用于单独统计消费进度，并设置到 ringBuffer 中
    ringBuffer.addGatingSequences(workerPool.getWorkerSequences());

    //6 启动 workPool
    ExecutorService executor4WorkPool = Executors.newFixedThreadPool(8);
    workerPool.start(executor4WorkPool);

    //7 创建多个生产者
    ExecutorService executor4Producer = Executors.newFixedThreadPool(100);
    CountDownLatch latch4Producer = new CountDownLatch(1);
    for (int i = 0; i < 100; i++) {
      OrderProducer producer = new OrderProducer(ringBuffer);
      executor4Producer.submit(() -> {
        try {
          //由主线程统一调度
          latch4Producer.await();
        } catch (InterruptedException e) {
          logger.error("InterruptedException occurred.", e);
        }
        for (int j = 0; j < 100; j++) {
          producer.sendData(UUID.randomUUID().toString());
        }
      });
    }

    //让生产者一起开始工作
    latch4Producer.countDown();

    //主线程睡一会儿
    TimeUnit.SECONDS.sleep(5);

    executor4WorkPool.shutdown();
    executor4Producer.shutdown();

    int sum = 0;
    for (OrderConsumer consumer : consumers) {
      logger.info("消费者 {} 处理了 {} 个订单", consumer.getConsumerId(), consumer.getCount());
      sum += consumer.getCount();
    }
    logger.info("所有消费者一共处理了 {} 个订单", sum);
  }

}
