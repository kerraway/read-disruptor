package com.github.kerraway.disruptor.quickstart;

import com.lmax.disruptor.RingBuffer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.nio.ByteBuffer;

/**
 * @author kerraway
 * @date 2019/3/10
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class OrderEventProducer {

  private final RingBuffer<OrderEvent> ringBuffer;

  void sendData(ByteBuffer data) {
    //1 在生产者发送消息时，首先需要从 ringBuffer 中获取一个可用的 sequence
    long sequence = ringBuffer.next();
    try {
      //2 根据这个 sequence，找到具体的 orderEvent 对象
      //注意：此时的 orderEvent 对象是一个空对象
      OrderEvent orderEvent = ringBuffer.get(sequence);
      //3 根据具体业务场景，为 orderEvent 对象赋值
      orderEvent.setId(data.getLong(0));
    } finally {
      //4 提交发布操作
      ringBuffer.publish(sequence);
    }
  }

}
