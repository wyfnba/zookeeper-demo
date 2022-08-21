package com.lanqiao.zookeeperdemo.controller;

//import org.redisson.Redisson;
//import org.redisson.api.RLock;
import com.lanqiao.zookeeperdemo.lock.Lock;
import com.lanqiao.zookeeperdemo.lock.ZkLock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class BiSheController {

    // 分布式锁
//    @Autowired
//    private Redisson redisson;
//    private Lock zkLock=null;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


//    @RequestMapping(value = "/login")
//    public String login(String username, HttpSession session, HttpServletRequest request) {
//        // 登录成功后，把用户的信息放入session
//        session.setAttribute("username", username);
//        return username + ",端口：" + request.getLocalPort();
//    }
//
//    @RequestMapping("/getUser")
//    public String getUser(HttpSession session, HttpServletRequest request) {
//        String username = (String) session.getAttribute("username");
//        return session.getId() + "，" + username + "，端口：" + request.getLocalPort();
//    }
//
    @PostMapping("/checkTeacher")
    public String checkTeacher(String teacherName) throws Exception {
        // SpringBoot操作Redis
        String lockKey = "lockKey";

        //RLock redissonLock = null;
        Lock zkLock = new ZkLock("/test2/lock", 5L);

        String num = "";
        try {
            //redissonLock = redisson.getLock("lockKey");//加锁
            boolean lock = zkLock.lock();

            //redissonLock.tryLock(30, TimeUnit.SECONDS);//超时时间：每间隔10秒（1/3）
            if (lock){
                //System.out.println("线程获取到锁");
                num = stringRedisTemplate.opsForValue().get(teacherName);
                int n = Integer.parseInt(num);
                if (n > 0) {
                    n = n - 1;
                    stringRedisTemplate.opsForValue().set("lkz", n + "");
                    System.out.println("当前名额：" + n);
                    //正常选择老师
                } else {
                    System.out.println("名额已满");
                    return "名额已满";
                }
            }else {
                //System.out.println("线程获取锁失败");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            //redissonLock.unlock();//释放锁

            zkLock.unlock();
            //System.out.println("释放锁" +zkLock.getLockPath());
        }
        return num;
    }
}
