package yuan.thymeleaf.demo.utils;

import org.apache.commons.lang3.StringUtils;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import java.util.Properties;

public class CommonUtils {

    public static int getNowTime(){

        return (int)(System.currentTimeMillis() / 1000);
    }

    /**
     * 发送qq邮箱
     */
    public static void sendMail(String orderId) {
        try{
            if(StringUtils.isEmpty(orderId) || "0".equals(orderId)){
                return;
            }
            //创建Properties 类用于记录邮箱的一些属性
            Properties props = new Properties();
            //表示SMTP发送邮件，必须进行身份验证
            props.put("mail.smtp.auth", "true");
            //此处填写SMTP服务器
            props.put("mail.smtp.host", "smtp.qq.com");
            //端口号，QQ邮箱端口587
            props.put("mail.smtp.port", "587");
            //此处填写，写信人的账号
            props.put("mail.user", "1687042168@qq.com");
            //此处填写16位STMP口令
            props.put("mail.password", "mhgobjxihfqscjjj");
            //构建授权信息，用于进行SMTP进行身份验证
            Authenticator authenticator = new Authenticator() {

                protected PasswordAuthentication getPasswordAuthentication() {
                    // 用户名、密码
                    String userName = props.getProperty("mail.user");
                    String password = props.getProperty("mail.password");
                    return new PasswordAuthentication(userName, password);
                }
            };
            // 使用环境属性和授权信息，创建邮件会话
            Session mailSession = Session.getInstance(props, authenticator);
            // 创建邮件消息
            MimeMessage message = new MimeMessage(mailSession);
            // 设置发件人
            InternetAddress form = new InternetAddress(props.getProperty("mail.user"));
            message.setFrom(form);

            // 设置收件人的邮箱
            InternetAddress to = new InternetAddress("724590161@qq.com");
            message.setRecipient(RecipientType.TO, to);
            // 设置邮件标题
            message.setSubject(orderId + "狗东可能产生了个订单,抓紧时间看看");
            // 设置邮件的内容体
            message.setContent("orderId:" + orderId, "text/html;charset=UTF-8");
            // 最后当然就是发送邮件啦
            Transport.send(message);
        }catch (Exception e){
            e.getMessage();
        }
    }
}
