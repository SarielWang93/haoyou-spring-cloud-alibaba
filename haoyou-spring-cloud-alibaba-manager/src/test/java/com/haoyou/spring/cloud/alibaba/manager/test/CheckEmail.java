package com.haoyou.spring.cloud.alibaba.manager.test;
import com.sun.mail.util.BASE64DecoderStream;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SearchTerm;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class CheckEmail {
    public static void main(String args[]){
        //邮件服务器参数
        String user = "582558220@qq.com";     //邮箱验证用户名，一般第三方的POP，smtp服务，用户名为发送邮箱地址
        String password = "fbhmteugohpvbfja";            //邮箱验证授权码
        String host = "pop.qq.com";                  //发送邮箱服务器
        //第三方POP服务器可以不用设置port参数

        //设置邮件服务器参数、服务器端口等参数
        Properties props = new Properties();
        props.put("mail.pop3.host",host);
        props.put("mail.pop3.auth", "true");
        props.put("mail.transport.protocol", "pop3");


        //设置Session对象，同时配置验证方法
        Session session = Session.getDefaultInstance(props,new javax.mail.Authenticator(){
            protected PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication(user,password);
            }
        });

        try {
            //创建本地储存对象，并进行配置
            Store store = session.getStore("pop3s");
            store.connect(host,user,password);

            //创建文件夹对象，用于读取本地邮件列表
            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);

            //从文件夹对象中获取每封邮件的Message对象

            SearchTerm searchTerm = new SearchTerm() {
                @Override
                public boolean match(Message msg) {
                    try {
                        return msg.getSubject().startsWith("SBD");
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            };

            Message[] messages = folder.search(searchTerm);
            System.out.println("The count of the Email is :"+messages.length);

            //输出所有邮件的信息
            int count = 1;
            for(Message message : messages){
                System.out.println("---------------------------------------");
                System.out.println("Email No."+ count++);
                System.out.println("Subject: "+ message.getSubject());
                System.out.println("From: "+ message.getFrom()[0]);
                System.out.println("Date: "+ message.getSentDate());
                System.out.println("Text: "+ message.getContent().toString());
                MimeMultipart mimeMultipart = (MimeMultipart)message.getContent();
                System.out.println("mimeMultipartCount: "+ mimeMultipart.getCount());
                if(mimeMultipart.getCount()>1){
                    BASE64DecoderStream content = (BASE64DecoderStream)mimeMultipart.getBodyPart(1).getContent();

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(content));

                    System.out.println("Text: "+ bufferedReader.readLine());
                }
            }

            //释放相关资源
            folder.close(false);
            store.close();

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
