package com.defvortex.reflection;

import java.util.List;

public class MainApp {
    public static void main(String[] args) throws Exception {
        ReflectionRepository<Student> reflectionRepository = new ReflectionRepository<>(Student.class);

        Student student1 = new Student(null,"Leha","Bubnov",1,5);
        Student student2 = new Student(null,"Sergei","Bulkin",1,5);

        //        reflectionRepository.save(student1);
//        reflectionRepository.save(student2);

//        System.out.println(reflectionRepository.findById(15L));

//        reflectionRepository.deleteById(17L);

//        reflectionRepository.deleteAllFields();

//        List<Student> ls = reflectionRepository.findAllObjects();
//        for (Student s : ls) {
//            System.out.println(s);
//        }
    }
}
