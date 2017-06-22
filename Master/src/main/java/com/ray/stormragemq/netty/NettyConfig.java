package com.ray.stormragemq.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ray on 2017/6/22.
 * 配置Netty Bean
 */

@Configuration
@PropertySource("classpath:netty.properties")
public class NettyConfig {

    @Value("${tcp.port}")
    private int port;

    @Value("${boss.thread.count}")
    private int bossCount;

    @Value("${so.keepAlive}")
    private boolean keepAlive;

    @Value("${so.backlog}")
    private int backlog;

    @Value("${tcp.remoteHost}")
    private String remoteHost;

    private final MasterChannelInitializer masterChannelInitializer;

    @Autowired
    public NettyConfig(MasterChannelInitializer masterChannelInitializer) {
        this.masterChannelInitializer = masterChannelInitializer;
    }


    @Bean(name = "group", destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup group(){
        return new NioEventLoopGroup(bossCount);
    }

    //配置TCP
    @Bean(name = "tcpChannelOptions")
    public Map<ChannelOption, Object> tcpChannelOptions() {
        Map<ChannelOption, Object> options = new HashMap<>();
        options.put(ChannelOption.SO_KEEPALIVE, keepAlive);
        options.put(ChannelOption.SO_BACKLOG, backlog);
        return options;
    }

    //配置bootstrap
    @Bean(name = "bootstrap")
    public Bootstrap bootstrap(){
        Bootstrap b = new Bootstrap();
        b.group(group())
                .channel(NioSocketChannel.class)
                .remoteAddress(tcpAddress())
                .handler(masterChannelInitializer);

        return b;
    }

    //配置端口
    @Bean(name = "tcpSocketAddress")
    public InetSocketAddress tcpAddress(){
        return new InetSocketAddress(remoteHost, port);
    }



}
