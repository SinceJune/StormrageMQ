package com.ray.stormragemq.thread;

import com.ray.stormragemq.common.MessageTypeConstant;
import com.ray.stormragemq.constant.ConstantVariable;
import com.ray.stormragemq.dao.QueueMessageDao;
import com.ray.stormragemq.entity.QueueEntity;
import com.ray.stormragemq.entity.QueueMessageEntity;
import com.ray.stormragemq.netty.ClientChannel;
import com.ray.stormragemq.netty.service.GatewayService;
import com.ray.stormragemq.util.JsonUtil;
import com.ray.stormragemq.util.LogUtil;
import com.ray.stormragemq.util.RandomUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 将队列中的消息发送到消费者中
 * */
@Service
public class QueueThreadService {

    @Autowired
    private GatewayService gatewayService;

    @Autowired
    private Map<String, QueueEntity> queueMap;

    @Autowired
    private QueueMessageDao queueMessageDao;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 每个队列启动一个线程发送消息
     * */
    public void start(){
        queueMap.forEach((s, queueEntity) -> {

            startThread(queueEntity);

        });

    }


    /**
     * 开启线程
     * */
    private void startThread(QueueEntity queueEntity){
        new Thread(() -> {
            int modCount = ModCount.getModCount();
            Thread.currentThread().setName(queueEntity.getName() + "-" + modCount);

            BlockingQueue<QueueMessageEntity> q = queueEntity.getBlockingQueue();
            List<String> consumerUuidList = queueEntity.getConsumerUuidList();

            while (true){
                QueueMessageEntity queueMessage = null;

                //检查消费者是否存在
                if(consumerUuidList.size() > 0){
                    try {
                        queueMessage = q.poll(5, TimeUnit.SECONDS);
                        if(queueMessage != null){
                            String messageString = JsonUtil.toJson(queueMessage.getMessage());
                            ClientChannel clientChannel = gatewayService.getGatewayChannel(consumerUuidList.get(RandomUtil.getIntRandom(0, consumerUuidList.size() - 1)));
                            clientChannel.getSocketChannel().writeAndFlush(Unpooled.copiedBuffer(messageString, CharsetUtil.UTF_8));

                            //将 不重要 的消息直接把状态改为送达
                            //并将redis中的消息删除
                            if(queueMessage.getMessage() != null && MessageTypeConstant.NORMAL_MESSAGE_TYPE.equals(queueMessage.getMessage().getType())){
                                queueMessage.setReceived(true);
                                queueMessageDao.insertQueueMessage(queueMessage);
                                redisTemplate.opsForHash().delete(ConstantVariable.MESSAGE_QUEUE_KEY,  queueMessage.getId());
                            }

                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    //消费者不存在，暂停5秒
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                //检查modCount
                if(modCount != ModCount.getModCount()){
                    LogUtil.logInfo("ModCount被修改，线程结束，modCount = " + ModCount.getModCount());
                    break;
                }

            }
        }).start();

    }






}