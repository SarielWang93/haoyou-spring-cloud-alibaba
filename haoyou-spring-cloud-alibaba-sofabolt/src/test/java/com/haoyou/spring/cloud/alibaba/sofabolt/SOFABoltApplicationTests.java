package com.haoyou.spring.cloud.alibaba.sofabolt;


import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.lang.Console;
import com.alipay.remoting.Connection;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SOFABoltApplicationTests {

    @Before
    public void befor() {

    }

    @Test
    public void contextLoads() throws RemotingException, InterruptedException {

    }


    @Test
    public void contextLoads2() throws RemotingException, InterruptedException {

    }

    public static void main(String[] args) {
        FileReader fileReader = new FileReader("ShieldVoca.txt");
        List<String> lines = fileReader.readLines();

        for (String line : lines) {
            Console.log(line);
        }
    }
}
