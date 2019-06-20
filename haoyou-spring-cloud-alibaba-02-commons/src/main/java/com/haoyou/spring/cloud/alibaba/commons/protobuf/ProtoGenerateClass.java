package com.haoyou.spring.cloud.alibaba.commons.protobuf;

import cn.hutool.core.lang.Console;
import com.google.protobuf.ByteString;
import com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.FirstDemo;
import com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg;
import com.haoyou.spring.cloud.alibaba.commons.util.Cmd;

import java.util.ArrayList;
import java.util.List;
import java.io.*;

/**
 * @Author: wanghui
 * @Date: 2019/5/15 10:14
 * @Version 1.0
 * 实现protobuf的java生成
 */
public class ProtoGenerateClass {
    // protoc的目录
    private static final String PROTOC_FILE = "D:\\IDEAProject\\haoyou-spring-cloud-alibaba\\haoyou-spring-cloud-alibaba-commons\\src\\main\\resources\\protoc.exe";
    // 指定解析导入指令时要在其中查找.proto文件的目录
    private static final String IMPOR_TPROTO = "D:/IDEAProject/haoyou-spring-cloud-alibaba/haoyou-spring-cloud-alibaba-commons/src/main/resources/proto";
    // 生成java类输出目录
    private static final String JAVA_OUT = "D:/IDEAProject/haoyou-spring-cloud-alibaba/haoyou-spring-cloud-alibaba-commons/src/main/java";
    // 指定proto文件
    private static final String protos = "D:\\IDEAProject\\haoyou-spring-cloud-alibaba\\haoyou-spring-cloud-alibaba-commons\\src\\main\\resources\\proto\\LRequest.proto";

    public static void main(String[] args) throws IOException {
        List<String> lCommand = new ArrayList<String>();
        lCommand.add(PROTOC_FILE);
        lCommand.add("-I=" + IMPOR_TPROTO );
        lCommand.add("--java_out=" + JAVA_OUT);
        lCommand.add(protos);

        Cmd cmd = new Cmd();
        cmd.execute(lCommand);
    }
    public static void main0(String[] args) throws IOException {
        //模拟将对象转成byte[]，方便传输
        MyRequestMsg.MyRequest.Builder builder = MyRequestMsg.MyRequest.newBuilder();
        builder.setId(1);
        builder.setUseruid("lasfdjwiahf");
        builder.setMsg(ByteString.copyFrom("hryou0922@126.com".getBytes()));
        MyRequestMsg.MyRequest person = builder.build();
        System.out.println("before :"+ person.toString());

        System.out.println("===========Person Byte==========");
        for(byte b : person.toByteArray()){
            System.out.print(b);
        }
        System.out.println();
        System.out.println(person.toByteString());
        System.out.println("================================");

        //模拟接收Byte[]，反序列化成Person类
        byte[] byteArray =person.toByteArray();
        MyRequestMsg.MyRequest p2 = MyRequestMsg.MyRequest.parseFrom(byteArray);
        System.out.println("after :" +p2.toString());

    }



}
