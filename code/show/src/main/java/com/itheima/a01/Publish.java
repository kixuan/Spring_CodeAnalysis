package com.itheima.a01;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * ApplicationEventPublisher
 * 事件发布器
 */
@Component
public class Publish {

    private static final Logger log = LoggerFactory.getLogger(Publish.class);

    @Autowired
    private ApplicationEventPublisher context;

    public void register() {
        log.debug("用户注册");
        // 使用发布事件进行解耦
        context.publishEvent(new UserRegisteredEvent(this));
    }

}
