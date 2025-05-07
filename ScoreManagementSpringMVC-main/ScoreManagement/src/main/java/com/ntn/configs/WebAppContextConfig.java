/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.configs;

import com.ntn.filters.AuthenticatedLocaleInterceptor;
import com.ntn.formatters.ClassFormatter;
import com.ntn.formatters.DateFormatter;
import com.ntn.formatters.DepartmentFormatter;
import com.ntn.formatters.MajorFormatter;
import com.ntn.formatters.SchoolYearFormatter;
import com.ntn.formatters.StudentFormatter;
import com.ntn.formatters.SubjectFormatter;
import com.ntn.formatters.SubjectTeacherFormatter;
import com.ntn.formatters.TeacherFormatter;
import com.ntn.formatters.TrainingTypeFormatter;
import com.ntn.formatters.UserFormatter;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.format.FormatterRegistry;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

@Configuration
@EnableWebMvc
@EnableTransactionManagement
@ComponentScan(basePackages = {
    "com.ntn.controllers",
    "com.ntn.repository",
    "com.ntn.service",
    "com.ntn.formatters"
})

public class WebAppContextConfig implements WebMvcConfigurer {

    @Autowired
    private ClassFormatter classFormatter;

    @Autowired
    private DateFormatter dateFormatter;

    @Autowired
    private DepartmentFormatter departmentFormatter;

    @Autowired
    private MajorFormatter majorFormatter;

    @Autowired
    private SchoolYearFormatter schoolYearFormatter;

    @Autowired
    private StudentFormatter studentFormatter;

    @Autowired
    private SubjectFormatter subjectFormatter;

    @Autowired
    private SubjectTeacherFormatter subjectTeacherFormatter;

    @Autowired
    private TeacherFormatter teacherFormatter;

    @Autowired
    private TrainingTypeFormatter trainingTypeFormatter;

    @Autowired
    private UserFormatter userFormatter;

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
    }

    @Bean(name = "validator")
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean bean
                = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource());
        return bean;
    }

    @Bean
    public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
        return new HandlerMappingIntrospector();
    }

    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatter(classFormatter);
        registry.addFormatter(dateFormatter);
        registry.addFormatter(departmentFormatter);
        registry.addFormatter(majorFormatter);
        registry.addFormatter(schoolYearFormatter);
        registry.addFormatter(studentFormatter);
        registry.addFormatter(subjectFormatter);
        registry.addFormatter(subjectTeacherFormatter);
        registry.addFormatter(teacherFormatter);
        registry.addFormatter(trainingTypeFormatter);
        registry.addFormatter(userFormatter);
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(false);
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(new Locale("vi"));
        return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
        registry.addInterceptor(authenticatedLocaleInterceptor());
    }

    @Bean
    public AuthenticatedLocaleInterceptor authenticatedLocaleInterceptor() {
        return new AuthenticatedLocaleInterceptor();
    }
}
