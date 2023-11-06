package com.sdu.arrow.middleware.delayer.model;

import cn.hutool.core.lang.UUID;
import com.sdu.arrow.middleware.delayer.listener.EventExecutableInvokerListener;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.time.Clock;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@ToString
@Data
public class ExecuteInvokerEvent<T> implements Serializable {

    /**
     * 业务主键编码
     */
    private String bizId;

    /**
     * 业务类型：可以是类名称
     */
    private String bizGroup;

    /**
     * 创建事件事件-可以作为基准值
     */
    private Long createTime;

    /**
     * 延时时间
     */
    private Long delayedTime;

    /**
     * 延时时间戳
     */
    private TimeUnit timeUnit;

    /**
     * 预先计划执行事件操作 被动生产，最后会通过延时时间计算出来，此处用于备份冗余字段
     */
    private Date firedTime;

    /**
     * 数据执行模型体操作机制
     */
    private T dataModel;

    /**
     * 是否重试机制 次数 默认 0
     */
    private int retry;

    /**
     * 异步
     */
    private boolean async;


    public ExecuteInvokerEvent() {
    }

    /**
     * 构造器
     */
    public ExecuteInvokerEvent(String bizId, String bizGroup) {
        this.bizId = bizId;
        this.bizGroup = bizGroup;
    }


    /**
     * 构造器
     */
    public ExecuteInvokerEvent(String bizGroup) {
        this.bizGroup = bizGroup;
    }

    /**
     * 检验操作机制 判断是否合法
     */
    boolean isNotNull(Object param) {
        return Objects.nonNull(param);
    }

    /**
     * 检验操作机制 判断是否合法
     *
     */
    boolean isValidate(ExecuteInvokerEvent<T> param) {
        return isNotNull(param);
    }

    /**
     * 预先初始化机制操作
     */
    public void preCondition(ExecuteInvokerEvent<T> param) {
        //如果校验通过了
        if (isValidate(param)) {
            param.setBizId(StringUtils.defaultIfBlank(param.getBizId(), UUID.fastUUID().toString()));
            long currentTime = Clock.system(ZoneId.systemDefault()).millis();
            param.setCreateTime(currentTime);
            //默认时间单位
            param.setTimeUnit(Optional.ofNullable(param.getTimeUnit()).orElse(EventExecutableInvokerListener.DEFAULT_DELAYED_TIMEUNIT));
            //默认时间偏移量
            param.setDelayedTime(Optional.ofNullable(param.getDelayedTime()).orElse(EventExecutableInvokerListener.DEFAULT_DELAYED_OSFFET));
            //计算相关的
            param.setAsync(EventExecutableInvokerListener.DEFAULT_IS_ASYNC_FLAG);
            // 重试次数
            param.setRetry(EventExecutableInvokerListener.DEFAULT_RETRY_NUM);
            // 执行组类型
            param.setBizGroup(StringUtils.isEmpty(param.getBizGroup()) ? EventExecutableInvokerListener.DEFAULT_BIZ_GROUP : param.getBizGroup());
            //计算触发时间值
            long triggerTime = currentTime + param.getTimeUnit().toMillis(param.getDelayedTime());
            //计算预先触发时间
            param.setFiredTime(new Date(triggerTime));
            return;
        }
        throw new IllegalArgumentException("传输的参数出现异常，非法参数，请检查传参！");
    }

}
