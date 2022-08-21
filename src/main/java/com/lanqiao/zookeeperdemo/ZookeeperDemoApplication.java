package com.lanqiao.zookeeperdemo;

import io.lettuce.core.RedisClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


@SpringBootApplication
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 600)
public class ZookeeperDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZookeeperDemoApplication.class, args);
		RedisClient r;
	}

	// 传统的XML的bean的配置
//	@Bean
//	public Redisson redission() {
//		Config config = new Config();
//		config.useSingleServer().setAddress("redis://localhost:6379").setDatabase(0);
//		return (Redisson) Redisson.create(config);
//	}
}