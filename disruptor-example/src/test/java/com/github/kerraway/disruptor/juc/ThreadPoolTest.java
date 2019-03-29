package com.github.kerraway.disruptor.juc;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.*;

/**
 * @author kerraway
 * @date 2019/3/17
 */
@Slf4j
public class ThreadPoolTest {

  /**
   * 参考：https://blog.csdn.net/zhouhl_cn/article/details/7392607
   * <p>
   * <h3>参数介绍</h3>
   * <p>
   * corePoolSize
   * <p>
   * 核心线程数，核心线程会一直存活，即使没有任务需要处理。当线程数小于核心线程数时，即使现有的线程空闲，线程池也会优先创建新线程来处理任务，
   * 而不是直接交给现有的线程处理。核心线程在 allowCoreThreadTimeout 被设置为 true 时会超时退出，默认情况下不会退出。
   * <p>
   * maxPoolSize
   * <p>
   * 当线程数大于或等于核心线程，且任务队列已满时，线程池会创建新的线程，直到线程数量达到 maxPoolSize。如果线程数已等于maxPoolSize，且
   * 任务队列已满，则已超出线程池的处理能力，线程池会拒绝处理任务而抛出异常。
   * <p>
   * keepAliveTime
   * <p>
   * 当线程空闲时间达到 keepAliveTime，该线程会退出，直到线程数量等于 corePoolSize。如果 allowCoreThreadTimeout 设置为 true，则所
   * 有线程均会退出直到线程数量为 0。
   * <p>
   * allowCoreThreadTimeout
   * <p>
   * 是否允许核心线程空闲退出，默认值为 false。
   * <p>
   * queueCapacity
   * <p>
   * 任务队列容量。从 maxPoolSize 的描述上可以看出，任务队列的容量会影响到线程的变化，因此任务队列的长度也需要恰当的设置。
   *
   * <b>线程池按以下行为执行任务</b>
   * <p>
   * 1. 当线程数小于核心线程数时，创建线程。<br/>
   * 2. 当线程数大于等于核心线程数，且任务队列未满时，将任务放入任务队列。<br/>
   * 3. 当线程数大于等于核心线程数，且任务队列已满，若线程数小于最大线程数，创建线程；若线程数等于最大线程数，抛出异常，拒绝任务。<br/>
   * <p>
   * <h3>系统负载</h3>
   * <p>
   * 参数的设置跟系统的负载有直接的关系，下面为系统负载的相关参数：
   * <p>
   * tasks：每秒需要处理的最大任务数量<br/>
   * tasktime：处理第个任务所需要的时间<br/>
   * responsetime：系统允许任务最大的响应时间，比如每个任务的响应时间不得超过2秒<br/>
   * <p>
   * <h3>参数设置</h3>
   * <p>
   * corePoolSize
   * <p>
   * 每个任务需要 tasktime 秒处理，则每个线程每秒可处理 1/tasktime 个任务。系统每秒有 tasks 个任务需要处理，则需要的线程数为
   * tasks / (1 / tasktime)，即 tasks * tasktime 个线程数。假设系统每秒任务数为 100~1000，每个任务耗时 0.1 秒，则需要
   * 100 * 0.1 至 1000 * 0.1，即 10~100 个线程。那么 corePoolSize 应该设置为大于 10，具体数字最好根据 8020 原则，即
   * 80% 情况下系统每秒任务数，若系统 80% 的情况下第秒任务数小于 200，最多时为 1000，则 corePoolSize 可设置为 20。
   * <p>
   * queueCapacity
   * <p>
   * 任务队列的长度要根据核心线程数，以及系统对任务响应时间的要求有关。队列长度可以设置为 (corePoolSize / tasktime) * responsetime，
   * 上面的例子就是 (20 / 0.1) * 2 = 400，即队列长度可设置为 400。队列长度设置过大，会导致任务响应时间过长，切忌使用
   * LinkedBlockingQueue queue = new LinkedBlockingQueue() ——这实际上是将队列长度设置为 {@link Integer#MAX_VALUE} ，将会
   * 导致线程数量永远为 corePoolSize，再也不会增加，当任务数量陡增时，任务响应时间也将随之陡增。
   * <p>
   * maxPoolSize
   * <p>
   * 当系统负载达到最大值时，核心线程数已无法按时处理完所有任务，这时就需要增加线程。每秒 200 个任务需要 20 个线程，那么当每秒达到 1000 个
   * 任务时，则需要(1000 - queueCapacity) * (20 / 200)，即 60 个线程，可将 maxPoolSize 设置为 60。
   * <p>
   * keepAliveTime
   * <p>
   * 线程数量只增加不减少也不行。当负载降低时，可减少线程数量，如果一个线程空闲时间达到 keepAliveTiime，该线程就退出。
   * 默认情况下线程池最少会保持 corePoolSize 个线程。
   * <p>
   * allowCoreThreadTimeout
   * <p>
   * 默认情况下核心线程不会退出，可通过将该参数设置为 true，让核心线程也退出。
   * <p>
   * 以上关于线程数量的计算并没有考虑 CPU 的情况。若结合 CPU 的情况，比如，当线程数量达到 50 时，CPU 达到 100%，则将 maxPoolSize 设置为
   * 60 也不合适，此时若系统负载长时间维持在每秒 1000 个任务，则超出线程池处理能力，应设法降低每个任务的处理时间(tasktime)。
   */
  @Test
  public void test() throws InterruptedException {
    /**
     * {@link Executors#newCachedThreadPool()} 源码如下：
     * <pre>
     *    new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());
     * </pre>
     * 由于使用了 {@link SynchronousQueue}，当有任务进来时，除非有之前已经创建的空闲的线程可以使用，否则就会创建新的线程，而
     * 最大线程数是 {@link Integer#MAX_VALUE}，所以当有大量任务进来时，就会创建大量的工作线程，有可能会产生 OOM。
     * <p>
     * 故在实际开发中，不能使用 {@link Executors#newCachedThreadPool()}。
     */
    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    /**
     * {@link Executors#newFixedThreadPool(int)} 源码如下：
     * <pre>
     *   new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
     * </pre>
     * 由于 workQueue 使用的是 <code>new LinkedBlockingQueue<Runnable>()</code>，其容量是 {@link Integer#MAX_VALUE}，当有
     * 大量任务进来时，就会堆积在 workQueue 中，有可能会产生 OOM。
     * <p>
     * 故在实际开发中，不能使用 {@link Executors#newFixedThreadPool(int)}。
     */
    ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);

    /**
     * 自定义线程池
     */
    ThreadPoolExecutor orderThreadPool = new ThreadPoolExecutor(
        //corePoolSize 核心线程数，核心线程会一直存活，即使没有任务需要处理
        20,
        //maximumPoolSize 最大线程数，当线程数大于或等于核心线程，且任务队列已满时，线程池会创建新的线程，直到线程数量达到 maxPoolSize
        60,
        //keepAliveTime 当线程数超过核心线程数，且线程空闲时间达到 keepAliveTime，该线程会退出，直到线程数等于核心线程数
        60L, TimeUnit.SECONDS,
        //workQueue 使用有界队列
        new ArrayBlockingQueue<>(400),
        //threadFactory 线程工厂，可以自定义线程创建方法
        new ThreadFactory() {
          @Override
          public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName("order-thread-" + t.getId());
            return t;
          }
        },
        //handler 拒绝策略，当线程数等于最大线程数，任务队列已满时，执行该策略
        //这里可以打一些日志，做一些补偿，也就是一些兜底方案
        new RejectedExecutionHandler() {
          @Override
          public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            logger.warn("拒绝策略，任务：{}", r);
          }
        });
    CountDownLatch latch = new CountDownLatch(1);
    //最大线程数 60，工作队列数 400，故放入 461 个任务的时候，会有一个被拒绝掉
    for (int i = 0; i < 461; i++) {
      orderThreadPool.submit(() -> {
        try {
          latch.await();
        } catch (InterruptedException e) {
          logger.error("InterruptedException occurred.", e);
        }
        logger.debug("Process order.");
      });
    }

    latch.countDown();

    TimeUnit.SECONDS.sleep(2);
    orderThreadPool.shutdown();
    logger.info("Main thread end.");
  }

}
