package com.lanqiao.zookeeperdemo;

import com.lanqiao.zookeeperdemo.factory.ClientFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

public class InterProcessMutexTest {

    public static void main(String[] args) {
        CuratorFramework zkClient = ClientFactory.getClient();
        InterProcessMutex zkMutex = new InterProcessMutex(zkClient, "/test/mutex");

        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("线程1启动");
                try {
                    zkMutex.acquire(); //阻塞等待，也可超时等待
                    System.out.println("线程1获取到锁");
                    Thread.sleep(2000);
                    zkMutex.release();
                    System.out.println("线程1释放锁");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("线程2启动");
                try {
                    zkMutex.acquire();
                    System.out.println("线程2获取到锁");
                    Thread.sleep(2000);
                    zkMutex.release();
                    System.out.println("线程2释放锁");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
