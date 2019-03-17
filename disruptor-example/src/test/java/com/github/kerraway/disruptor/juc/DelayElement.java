package com.github.kerraway.disruptor.juc;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Ref https://www.baeldung.com/java-delay-queue
 * <p>
 * Each element we want to put into the DelayQueue needs to implement the Delayed interface. Let’s say that
 * we want to create a DelayObject class. Instances of that class will be put into the DelayQueue.
 * <p>
 * We’ll pass the String data and delayInMilliseconds as and arguments to its constructor:
 * <pre>
 * public class DelayObject implements Delayed {
 *     private String data;
 *     private long startTime;
 *
 *     public DelayObject(String data, long delayInMilliseconds) {
 *         this.data = data;
 *         this.startTime = System.currentTimeMillis() + delayInMilliseconds;
 *     }
 * }
 * </pre>
 * We are defining a startTime – this is a time when the element should be consumed from the queue. Next, we
 * need to implement the getDelay() method – it should return the remaining delay associated with this object
 * in the given time unit.
 * <p>
 * Therefore, we need to use the TimeUnit.convert() method to return the remaining delay in the proper TimeUnit:
 * <pre>
 * public long getDelay(TimeUnit unit) {
 *     long diff = startTime - System.currentTimeMillis();
 *     return unit.convert(diff, TimeUnit.MILLISECONDS);
 * }
 * </pre>
 * When the consumer tries to take an element from the queue, the DelayQueue will execute getDelay() to find out
 * if that element is allowed to be returned from the queue. If the getDelay() method will return zero or a negative
 * number, it means that it could be retrieved from the queue.
 * <p>
 * We also need to implement the compareTo() method, because the elements in the DelayQueue will be sorted according
 * to the expiration time. The item that will expire first is kept at the head of the queue and the element with the
 * highest expiration time is kept at the tail of the queue:
 * <pre>
 * public int compareTo(Delayed o) {
 *     return Ints.saturatedCast(
 *       this.startTime - ((DelayObject) o).startTime);
 * }
 * </pre>
 *
 * @author kerraway
 * @date 2019/3/17
 */
@Data
public class DelayElement implements Delayed {

  private Integer id;
  @Setter(AccessLevel.NONE)
  private long startTime;

  public DelayElement(Integer id, long delayInMilliseconds) {
    this.id = id;
    this.startTime = System.currentTimeMillis() + delayInMilliseconds;
  }

  @Override
  public long getDelay(TimeUnit unit) {
    long diff = startTime - System.currentTimeMillis();
    return unit.convert(diff, TimeUnit.MILLISECONDS);
  }

  @Override
  public int compareTo(Delayed other) {
    if (other == this) {
      return 0;
    }
    long diff = (getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS));
    return (diff == 0) ? 0 : ((diff < 0) ? -1 : 1);
  }
}
