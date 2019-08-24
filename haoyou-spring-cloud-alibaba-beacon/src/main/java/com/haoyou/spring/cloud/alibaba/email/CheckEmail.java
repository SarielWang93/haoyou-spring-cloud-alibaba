package com.haoyou.spring.cloud.alibaba.email;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Protocol;
import com.haoyou.spring.cloud.alibaba.commons.mapper.ProtocolMapper;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.sun.mail.util.BASE64DecoderStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SearchTerm;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Service
public class CheckEmail {
    private final static Logger logger = LoggerFactory.getLogger(CheckEmail.class);
    //发送邮箱服务器
    final static public String QQ_HOST = "pop.qq.com";
    final static public String OUTLOOK_HOST = "outlook.office365.com";
    final static public String GMAIL_HOST = "pop.gmail.com";


    final static public long SECONDS = 60 * 60 * 24 * 30;


    //邮件服务器参数
    public static String user = "582558220@qq.com";     //邮箱验证用户名，一般第三方的POP，smtp服务，用户名为发送邮箱地址
    public static String password = "fbhmteugohpvbfja";            //邮箱验证授权码


    public static String user1 = "wh19930320@gmail.com";
    public static String password1 = "wangHUI930228";

    public static String user2 = "wh19930320@outlook.com";
    public static String password2 = "wangHUI930228";


    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Autowired
    private ProtocolMapper protocolMapper;

    public static void main(String args[]) {
        new CheckEmail().getEmails(user, password, QQ_HOST);
    }

    public void getEmails(String user, String password, String host) {

        //第三方POP服务器可以不用设置port参数

        //设置邮件服务器参数、服务器端口等参数
        Properties props = new Properties();
        props.put("mail.pop3.host", host);
        props.put("mail.pop3.auth", "true");
        props.put("mail.transport.Protocol", "pop3");


        //设置Session对象，同时配置验证方法
        Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        try {
            //创建本地储存对象，并进行配置
            Store store = session.getStore("pop3s");
            store.connect(host, user, password);

            //创建文件夹对象，用于读取本地邮件列表
            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_WRITE);
            //从文件夹对象中获取每封邮件的Message对象

            SearchTerm searchTerm = new SearchTerm() {
                @Override
                public boolean match(Message msg) {
                    try {
                        if (msg.getSubject().startsWith("SBD")) {
                            MimeMultipart mimeMultipart = (MimeMultipart) msg.getContent();
                            if (mimeMultipart.getCount() > 1) {
                                return true;
                            }
                        }
                        //删除邮件
                        msg.setFlag(Flags.Flag.DELETED, true);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            };

            Message[] messages = folder.search(searchTerm);
//            Console.log("The count of the Email is :" + messages.length);

            //输出所有邮件的信息
            int count = 1;
            for (Message message : messages) {
                //设置为已读
                message.setFlag(Flags.Flag.SEEN, true);


//                Console.log("---------------------------------------");
//                Console.log("Email No." + count++);

                String subject = message.getSubject();
                String[] split = subject.split(":");

//                Console.log("Subject: " + split[0].trim());

//                Console.log("ID: " + split[1].trim());


//                Console.log("From: " + message.getFrom()[0]);
                DateTime date = DateUtil.date(message.getSentDate());
//                Console.log("Date: " + date.toString());
                MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();


                MimeBodyPart bodyPart = (MimeBodyPart) mimeMultipart.getBodyPart(0);
                BufferedReader textReader = new BufferedReader(new InputStreamReader(bodyPart.getInputStream()));
                String line = null;
                StringBuilder text = StrUtil.builder();
//                Console.log("Text: ");
                while ((line = textReader.readLine()) != null) {
                    text.append(line);
                    text.append("\r");
                    text.append("\n");
                }
//                Console.log(text.toString());


                BASE64DecoderStream content = (BASE64DecoderStream) mimeMultipart.getBodyPart(1).getContent();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(content));
                //$B,3,190811053701,3904.14975,N,11701.50519,E,*
                String msg = bufferedReader.readLine().trim();
//                Console.log("FileText: " + msg);
                if(msg.startsWith("$B")){
                    Protocol protocol = new Protocol();
                    protocol.setDeviceIdNum(split[1].trim());
                    protocol.setText(msg);
                    List<Protocol> select = protocolMapper.select(protocol);


                    protocol.analysis(msg);
                    if (protocol.getSendDate() == null) {
                        protocol.setSendDate(date);
                    }


                    if (select == null || select.size() == 0) {
                        logger.info(String.format("新收到信息：%s", msg));
                        protocolMapper.insertSelective(protocol);
                        redisObjectUtil.save(RedisKeyUtil.getKey(RedisKey.BEACON, protocol.getDeviceIdNum(), protocol.getId().toString()), protocol, SECONDS);
                    }
                }

                //删除邮件
                message.setFlag(Flags.Flag.DELETED, true);

            }

            //释放相关资源
            folder.close(true);
            store.close();

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
