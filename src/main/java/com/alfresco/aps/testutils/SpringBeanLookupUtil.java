package com.alfresco.aps.testutils;

import org.activiti.engine.test.ActivitiRule;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringBeanLookupUtil implements ApplicationContextAware {
 
    private static ApplicationContext applicationContext;
 
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) 
            throws BeansException {
        SpringBeanLookupUtil.applicationContext = applicationContext;
    }
    
    public static ActivitiRule getActivitiRule() {
        return applicationContext.getBean(ActivitiRule.class);
    }
 

}
