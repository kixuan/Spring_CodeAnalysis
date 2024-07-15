package com.itheima.a01;

import org.springframework.context.ApplicationEvent;

/**
 * 用户注册事件
 * 实现 ApplicationEvent 接口，表示这是一个事件
 */
public class UserRegisteredEvent extends ApplicationEvent {
    public UserRegisteredEvent(Object source) {
        super(source);
    }
}
