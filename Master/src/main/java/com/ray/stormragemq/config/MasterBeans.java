package com.ray.stormragemq.config;

import com.ray.stormragemq.netty.TCPClient;
import io.netty.util.internal.ConcurrentSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Ray on 2017/6/25.
 */
@Configuration
public class MasterBeans {

    //存放mq服务器名称与Netty Channel的对应关系
    @Bean(name = "mqMap")
    public ConcurrentHashMap<String, TCPClient> mqMap(){
        return new ConcurrentHashMap<>();
    }

    //存放mq服务器和消息队列名称的对应关系,初始化一个空队列
    @Bean(name = "mqNameMap")
    public Map<String, Set<String>> mqNameMap() {
        Map<String, Set<String>> map = new ConcurrentHashMap<>();
        return map;
    }
}
