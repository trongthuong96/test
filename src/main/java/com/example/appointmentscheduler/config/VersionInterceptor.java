package com.example.appointmentscheduler.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
//@Bean
@ConditionalOnResource(resources = "${spring.info.build.location:classpath:META-INF/build-info.properties}")
@ConditionalOnMissingBean(BuildProperties.class)
public class VersionInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private BuildProperties buildProperties;

    public VersionInterceptor(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Override
    public void postHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler,
                           final ModelAndView modelAndView) throws Exception {

        if (modelAndView != null) {
            modelAndView.getModelMap().addAttribute("currentVersion", buildProperties.getVersion());
        }
    }
}