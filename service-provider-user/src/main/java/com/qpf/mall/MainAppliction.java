package com.qpf.mall;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class MainAppliction {
    public static void main(String[] args) throws IOException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("provider.xml");
        context.start();
        System.in.read();
    }
}
