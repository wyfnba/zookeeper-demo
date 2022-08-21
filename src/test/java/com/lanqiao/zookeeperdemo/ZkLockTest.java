package com.lanqiao.zookeeperdemo;

import com.lanqiao.zookeeperdemo.lock.Lock;
import com.lanqiao.zookeeperdemo.lock.ZkLock;

public class ZkLockTest {

    public static void main(String[] args) throws Exception {
        System.out.println("开始测试ZK分布式锁...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                Lock zkLock = new ZkLock("/test/lock", 3L);
                System.out.println("线程1启动");
                try {
                    boolean lock = zkLock.lock();
                    if (lock) {
                        System.out.println("线程1获取到锁");
                        Thread.sleep(2000);
                        zkLock.unlock();
                        System.out.println("线程1释放锁");
                    } else {
                        System.out.println("线程1获取锁失败");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Lock zkLock = new ZkLock("/test/lock", 3L);
                System.out.println("线程2启动");
                try {
                    boolean lock = zkLock.lock();
                    if (lock) {
                        System.out.println("线程2获取到锁");
                        Thread.sleep(2000);
                        zkLock.unlock();
                        System.out.println("线程2释放锁");
                    } else {
                        System.out.println("线程2获取锁失败");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
