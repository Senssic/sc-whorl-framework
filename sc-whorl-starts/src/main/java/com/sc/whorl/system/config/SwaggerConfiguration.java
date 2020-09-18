package com.sc.whorl.system.config;

import com.github.xiaoymin.swaggerbootstrapui.annotations.EnableSwaggerBootstrapUI;
import com.sc.whorl.system.properties.ScAppProperties;
import com.sc.whorl.system.utils.StringPool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@ConditionalOnClass(EnableSwagger2.class)
@EnableSwagger2
@EnableSwaggerBootstrapUI
@Profile({"dev", "test"})
public class SwaggerConfiguration {
    @Autowired
    private ScAppProperties scAppProperties;

    @Bean(value = "defaultSwaggerApi")
    public Docket defaultSwaggerApi() {
        ParameterBuilder tokenPar = new ParameterBuilder();
        List<Parameter> pars = new ArrayList<Parameter>();
        tokenPar.name(StringPool.AUTH).description("携带令牌").modelRef(new ModelRef("string")).parameterType(StringPool.HEADER).required(false).build();
        pars.add(tokenPar.build());
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo()).select()
                .apis(RequestHandlerSelectors.basePackage(scAppProperties.getSwaggerBasePackage()))
                .paths(PathSelectors.any())
                .build().globalOperationParameters(pars);
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("SC WHORL FRAMEWORK RESTFUL APIS")
                .description("#sc-whorl framework RESTful APIs")
                .termsOfServiceUrl("http://#/")
                .version("1.0")
                .build();
    }

}