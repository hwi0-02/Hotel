package com.example.backend.authlogin.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class MailConfig {

    /**
     * JavaMailSender 빈이 자동 설정되지 않은 경우 기본 구현을 제공.
     * 실제 메일 전송은 불가능하지만 애플리케이션 구동 시 오류 방지.
     */
    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender javaMailSender() {
        log.warn("⚠️  JavaMailSender 자동 설정 실패. 기본 구현으로 대체합니다. 메일 전송 불가능할 수 있습니다.");
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("smtp.gmail.com");
        sender.setPort(587);
        sender.setUsername("noreply@yourapp.com");
        sender.setPassword("noreply-password");
        sender.setDefaultEncoding("UTF-8");
        
        java.util.Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");
        
        log.info("✅ 기본 JavaMailSender 빈 등록됨 (개발/테스트 환경용)");
        return sender;
    }
}
