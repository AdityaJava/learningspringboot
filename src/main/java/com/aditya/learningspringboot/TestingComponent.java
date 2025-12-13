package com.aditya.learningspringboot;

import org.springframework.stereotype.Service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

@Service
public class TestingComponent {

    public String sayHello() {
        Class<?> myClass = TestClass.class;
        Annotation[] annotations = myClass.getAnnotations();
        Method[] methods = myClass.getMethods();

        Arrays.stream(annotations).forEach(annotation -> {
            System.out.println(annotation);
        });

//        Arrays.stream(methods).forEach(method -> {
//            System.out.println(method);
//        });


        return "Hey hello";
    }
}
