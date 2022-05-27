package com.hhm.myjob;

import lombok.SneakyThrows;

/**
 * @Author huanghm
 * @Date 2022/5/25
 */
public class RunnableTest {
    static long count = 0;

    public static void main(String[] args) {
        MyRunnable runnable = new MyRunnable();
        MyRunnable runnable2 = new MyRunnable();

        Thread t = new Thread(runnable);
        Thread t2 = new Thread(runnable);

        t.start();
        t2.start();
    }
    private static class MyRunnable implements Runnable{
        private static final Object lock = new Object();

        @SneakyThrows
        @Override
        public void run() {
            while (true) {
                synchronized (lock) {
                    System.out.println(Thread.currentThread() + "-" + (count++));
                    Thread.sleep(1000);
                }
            }
        }
    }
}
