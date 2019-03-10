package com.github.kerraway.disruptor.quickstart;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author kerraway
 * @date 2019/3/10
 */
public class DisruptorQuickStartTest {

  @Test
  public void test() {
    //0 参数准备
    OrderEventFactory eventFactory = new OrderEventFactory();
    int ringBufferSize = 4;
    ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    //1 创建 disruptor 对象
    Disruptor<OrderEvent> disruptor = new Disruptor<>(
        //event 工厂对象
        eventFactory,
        //容器的长度
        ringBufferSize,
        //线程池（建议使用自定义线程池）
        executor,
        //单生产者 / 多生产者
        ProducerType.SINGLE,
        //等待策略
        new BlockingWaitStrategy()
    );

    //2 添加消费者的监听（构建 disruptor 与消费者的关联关系）
    disruptor.handleEventsWith(new OrderEventHandler());

    //3 启动 disruptor
    disruptor.start();

    //4 获取实际存储数据的容器 RingBuffer
    RingBuffer<OrderEvent> ringBuffer = disruptor.getRingBuffer();

    //5 构建生产者 + 生产数据
    OrderEventProducer producer = new OrderEventProducer(ringBuffer);
    ByteBuffer byteBuffer = ByteBuffer.allocate(8);
    for (long i = 0L; i < 5L; i++) {
      byteBuffer.putLong(0, i);
      producer.sendData(byteBuffer);
    }

    disruptor.shutdown();
    executor.shutdown();
  }

}