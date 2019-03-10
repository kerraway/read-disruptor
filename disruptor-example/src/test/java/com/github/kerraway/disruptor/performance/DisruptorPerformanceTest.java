package com.github.kerraway.disruptor.performance;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author kerraway
 * @date 2019/3/10
 */
@Slf4j
public class DisruptorPerformanceTest {

  private static final int ONE_MILLION = 1000000;

  /**
   * Total            Disruptor     ArrayBlockingQueue
   * One million      418 ms        534 ms
   * Five million     796 ms        985 ms
   * Ten million      1389 ms       1870 ms
   *
   * @throws InterruptedException
   */
  @Test
  public void performanceTest() throws InterruptedException {
    int[] totals = {ONE_MILLION, 5 * ONE_MILLION, 10 * ONE_MILLION};
    for (final int total : totals) {
      //test Disruptor
      performanceTest4Disruptor(total);
      //test ArrayBlockingQueue
      performanceTest4ArrayBlockingQueue(total);
    }
    //main thread wait a while
    TimeUnit.SECONDS.sleep(40);
  }

  /**
   * Performance test for {@link Disruptor}.
   *
   * @param total
   */
  private void performanceTest4Disruptor(final int total) {
    //2^16 = 65536
    int ringBufferSize = (int) Math.pow(2, 16);
    final Disruptor<Data> disruptor = new Disruptor<>(
        new DataFactory(),
        ringBufferSize,
        Executors.newSingleThreadExecutor(),
        ProducerType.SINGLE,
        new BlockingWaitStrategy()
    );

    //消费数据
    DataConsumer consumer = new DataConsumer(total);
    disruptor.handleEventsWith(consumer);
    disruptor.start();

    //生产数据
    new Thread(() -> {
      RingBuffer<Data> ringBuffer = disruptor.getRingBuffer();
      for (int i = 0; i < total; i++) {
        long sequence = ringBuffer.next();
        Data data = ringBuffer.get(sequence);
        data.setId(i);
        data.setName("name-" + i);
        ringBuffer.publish(sequence);
      }
    }).start();
  }

  /**
   * Performance test for {@link ArrayBlockingQueue}.
   *
   * @param total
   */
  private void performanceTest4ArrayBlockingQueue(final int total) {
    final ArrayBlockingQueue<Data> queue = new ArrayBlockingQueue<>(total);
    final Instant start = Instant.now();

    //向队列中添加数据
    new Thread(() -> {
      for (int i = 0; i < total; i++) {
        Data data = new Data(i, "name-" + i);
        try {
          queue.put(data);
        } catch (InterruptedException e) {
          logger.error("InterruptedException occurred.", e);
        }
      }
    }).start();

    //从队列中取出数据
    new Thread(() -> {
      for (int i = 0; i < total; i++) {
        try {
          queue.take();
        } catch (InterruptedException e) {
          logger.error("InterruptedException occurred.", e);
        }
      }
      Instant end = Instant.now();
      logger.info("ArrayBlockingQueue, total: {}, cost: {} ms.", total, Duration.between(start, end).toMillis());
    }).start();
  }

}