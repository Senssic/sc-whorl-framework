package sc.whorl.system.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.DispatcherType;
import javax.validation.executable.ExecutableValidator;

import lombok.extern.slf4j.Slf4j;
import sc.whorl.system.extend.validate.ValidateInterceptor;
import sc.whorl.system.extend.webhandler.handler.RequestAttrHandlerMethodArgumentResolver;
import sc.whorl.system.extend.webhandler.handler.RequestHeaderJsonHandlerMethodArgumentResolver;
import sc.whorl.system.extend.webhandler.handler.RequestJsonHandlerMethodArgumentResolver;
import sc.whorl.system.extend.webhandler.handler.SessionScopeMethodArgumentResolver;
import sc.whorl.system.extend.webhandler.wapper.RequestBodyThreadLocalInterceptor;
import sc.whorl.system.extend.webhandler.wapper.ResponseJsonAdvice;
import sc.whorl.system.extend.xss.XssFilter;
import sc.whorl.system.properties.ScAppProperties;
import sc.whorl.system.utils.SpringUtil;


@Slf4j
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@EnableConfigurationProperties({ScAppProperties.class})
public class CommonWebAutoConfiguration implements WebMvcConfigurer {


    private static final String CORS_MAPPING = "/**";
    private static final String ALLOWED_ORIGIN = "*";
    private static final String ALLOWED_METHODS = "*";
    private static final String ALLOWED_HEADERS = "*";
    private static final Long MAX_AGE = 168000L;

    @Value("${spring.http.multipart.location:/}")
    private String multipartLocation;

    @Autowired
    private ScAppProperties scAppProperties;

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.removeIf(x -> x instanceof StringHttpMessageConverter || x instanceof MappingJackson2HttpMessageConverter);
        converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
        converters.add(gsonHttpMessageConverter);
    }

    @Bean
    public FilterRegistrationBean<XssFilter> xssFilterRegistration() {
        FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<XssFilter>();
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setFilter(new XssFilter());
        registration.addUrlPatterns("/*");
        registration.setName("xssFilter");
        registration.setOrder(Ordered.LOWEST_PRECEDENCE);
        registration.setEnabled(scAppProperties.getXssFilterEnable());
        return registration;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(CORS_MAPPING)
                .allowedOrigins(ALLOWED_ORIGIN)
                .allowedMethods(ALLOWED_METHODS)
                .allowedHeaders(ALLOWED_HEADERS)
                .allowCredentials(true)
                //response
                .exposedHeaders(
                        "Access-Control-Allow-Headers",
                        "Access-Control-Allow-Origin",
                        "Access-Control-Allow-Methods")
                .maxAge(MAX_AGE);
    }


    @ConditionalOnClass(ExecutableValidator.class)
    @ConditionalOnResource(resources = "classpath:META-INF/services/javax.validation.spi.ValidationProvider")
    @Bean
    public ValidateInterceptor validateInterceptor() {
        return new ValidateInterceptor();
    }


    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new RequestJsonHandlerMethodArgumentResolver());
        argumentResolvers.add(new RequestAttrHandlerMethodArgumentResolver());
        argumentResolvers.add(new RequestHeaderJsonHandlerMethodArgumentResolver());
        argumentResolvers.add(new SessionScopeMethodArgumentResolver());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestBodyThreadLocalInterceptor()).addPathPatterns("/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/upload/**").addResourceLocations("file:" + multipartLocation + "/upload/");
        registry.addResourceHandler("/resoures/**").addResourceLocations("classpath:/resoures/");
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Bean
    public SpringUtil springUtils() {
        return new SpringUtil();
    }

    @Bean
    public ResponseJsonAdvice responseJsonAdvice() {
        return new ResponseJsonAdvice();
    }
}