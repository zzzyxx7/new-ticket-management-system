package com.fuzhou.common.utils;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Random;

/**
 * 发送邮件
 */
@Component
@RequiredArgsConstructor
public class MailUtils {
    /**
     * 注入邮件工具类
     */
    @Autowired
    private JavaMailSenderImpl javaMailSender;

    @Value("${spring.mail.username}")
    private String sendMailer;


    public String sendEmail(String email) {
        try {
            //true 代表支持复杂的类型
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(javaMailSender.createMimeMessage(),true);
            //邮件发信人
            mimeMessageHelper.setFrom("大麦平台 <" + sendMailer + ">");
            //邮件收信人
            mimeMessageHelper.setTo(email);
            //邮件主题
            mimeMessageHelper.setSubject("DaMai");

            Random random = new Random();
            StringBuilder code = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                // 生成0 - 9的随机数字并转换为字符串添加到StringBuilder中
                code.append(random.nextInt(10));
            }
            //邮件内容
            mimeMessageHelper.setText("尊敬的用户，您本次获取的验证码:"+ code.toString()+",5 分钟内有效，请尽快使用。此验证码是您进行注册的重要凭证，请务必注意保密，不要随意告知他人。");
            //邮件发送时间
            mimeMessageHelper.setSentDate(new Date());
            //发送邮件
            javaMailSender.send(mimeMessageHelper.getMimeMessage());
            System.out.println("发送邮件成功：" +sendMailer+"===>"+email);
             return code.toString();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("发送邮件失败："+e.getMessage());
            return null;
        }
    }

    /**
     * 发送自定义主题和正文的邮件（纯文本）
     * 用于抢票成功、订单通知等
     */
    public boolean sendTextEmail(String toEmail, String subject, String textContent) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            return false;
        }
        try {
            MimeMessageHelper helper = new MimeMessageHelper(javaMailSender.createMimeMessage(), true);
            helper.setFrom("大麦平台 <" + sendMailer + ">");
            helper.setTo(toEmail.trim());
            helper.setSubject(subject != null ? subject : "大麦平台通知");
            helper.setText(textContent != null ? textContent : "", false);
            helper.setSentDate(new Date());
            javaMailSender.send(helper.getMimeMessage());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

