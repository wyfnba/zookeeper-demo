package com.lanqiao.zookeeperdemo.lock;

import com.lanqiao.zookeeperdemo.factory.ClientFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ZkLock implements Lock{
    
    private String zkPath;  //分布式锁节点，如"/test/lock"
    private String lockPrefix;  //子节点前缀，如"/test/lock/seq-"
    private long waitTime;  //超时等待
    CuratorFramework zkClient;  //ZK客户端
    private Thread thread;  //当前线程
    private String lockPath;  //当前加锁节点
    private String waitPath;  //前一个等待节点
    final AtomicInteger lockCount = new AtomicInteger(0);  //重入计数器

    public String getLockPath() {
        return lockPath;
    }

    public ZkLock(String zkPath) throws Exception {
        this.zkPath = zkPath;
        this.lockPrefix = zkPath + "/seq-";
        this.waitTime = 0L;
        this.zkClient = ClientFactory.getClient();
        try {
            if (zkClient.checkExists().forPath(zkPath) == null) {
                zkClient.create().creatingParentsIfNeeded().forPath(zkPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public ZkLock(String zkPath, long waitTime) {
        this.zkPath = zkPath;
        this.lockPrefix = zkPath + "/seq-";
        this.waitTime = waitTime;
        this.zkClient = ClientFactory.getClient();
        try {
            if (zkClient.checkExists().forPath(zkPath) == null) {
                zkClient.create().creatingParentsIfNeeded().forPath(zkPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加锁
     */
    @Override
    public boolean lock() throws Exception {
        //可重入
        synchronized (this) {
            if (lockCount.get() == 0) {
                thread = Thread.currentThread();
                lockCount.incrementAndGet();
            } else {
                if (!thread.equals(Thread.currentThread())) {
                    return false;
                }
                lockCount.incrementAndGet();
                return true;
            }
        }
        return tryLock();
    }

    /**
     * 尝试获取锁
     */
    private boolean tryLock() throws Exception {
        lockPath = zkClient.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(lockPrefix);
        List<String> childList = zkClient.getChildren().forPath(zkPath);
        if (childList.size() == 1) {
            return true;
        } else {
            Collections.sort(childList);
            String curNode = lockPath.substring(zkPath.length() + 1);
            int index = childList.indexOf(curNode);
            if (index < 0) {
                throw new Exception("加锁异常");
            } else if (index == 0) {
                //第一个节点，加锁成功
                return true;
            } else {
                //监听前一个节点
                waitPath = zkPath + "/" + childList.get(index - 1);
                final CountDownLatch waitLatch = new CountDownLatch(1);
                Watcher w = new Watcher() {
                    @Override
                    public void process(WatchedEvent watchedEvent) {
                        if (watchedEvent.getType() == Event.EventType.NodeDeleted &&
                            watchedEvent.getPath().equals(waitPath)) {
                            System.out.println("监听到节点删除事件:" + watchedEvent);
                            waitLatch.countDown();
                        }
                    }
                };
                zkClient.getData().usingWatcher(w).forPath(waitPath);
                if (waitTime == 0L) {
                    waitLatch.await();
                    return true;
                } else {
                    return waitLatch.await(waitTime, TimeUnit.SECONDS);
                }
            }
        }
    }

    /**
     * 释放锁
     */
    @Override
    public boolean unlock() throws Exception {
        if (!thread.equals(Thread.currentThread())) {
            return false;
        }
        int newLockCount = lockCount.decrementAndGet();
        if (newLockCount < 0) {
            throw new Exception("解锁异常");
        } else if (newLockCount > 0) {
            return true;
        } else {
            try {
                if (zkClient.checkExists().forPath(lockPath) != null) {
                    zkClient.delete().forPath(lockPath);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }
}
