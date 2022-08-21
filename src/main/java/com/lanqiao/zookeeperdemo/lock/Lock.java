package com.lanqiao.zookeeperdemo.lock;

public interface Lock {
    //加锁
    boolean lock() throws Exception;
	//释放锁
    boolean unlock() throws Exception;

    String getLockPath();
}
