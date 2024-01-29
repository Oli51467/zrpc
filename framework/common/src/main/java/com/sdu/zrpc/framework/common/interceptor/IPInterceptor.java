package com.sdu.zrpc.framework.common.interceptor;

import com.sdu.zrpc.framework.common.entity.dto.RequestInfo;
import com.sdu.zrpc.framework.common.entity.holder.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class IPInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        RequestInfo requestInfo = UserContextHolder.get();
        if (null == requestInfo) requestInfo = new RequestInfo();
        requestInfo.setIp(request.getRemoteAddr());
        UserContextHolder.set(requestInfo);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContextHolder.remove();
    }
}
