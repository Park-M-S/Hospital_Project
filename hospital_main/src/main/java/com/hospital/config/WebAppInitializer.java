package com.hospital.config;

import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
import org.springframework.web.filter.CharacterEncodingFilter;

public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {


    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[] { AppConfig.class };
    }


    @Override
    protected Class<?>[] getServletConfigClasses() {
        return null;
    }


    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }

    // web.xml의 <filter> 역할을 대신하며, 필터와 비동기 지원을 등록
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        // 기존 WebAppInitializer의 onStartup() 메서드를 호출하여 기본 서블릿 등록을 진행
        super.onStartup(servletContext);

        // CharacterEncodingFilter를 등록
        FilterRegistration.Dynamic encodingFilter = servletContext.addFilter("encodingFilter", CharacterEncodingFilter.class);

        // web.xml의 <init-param>을 설정
        encodingFilter.setInitParameter("encoding", "UTF-8");
        encodingFilter.setInitParameter("forceEncoding", "true");

        // web.xml의 <async-supported>true</async-supported>
        encodingFilter.setAsyncSupported(true);

        // web.xml의 <filter-mapping> 
        encodingFilter.addMappingForUrlPatterns(null, true, "/*");
    }
}

