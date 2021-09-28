package com.defvortex.reflection;

import javax.print.attribute.standard.JobKOctets;
import java.io.Serializable;

@DbTable(name = "Students")
public class Student implements Serializable {

    @DbId
    Long id;

    @DbColumn
    @DbText
    String name;

    @DbColumn
    int age;

    @DbColumn
    int score;

    @DbColumn
    @DbText
    String surname;


    public Student() {
    }

    public Student(Long id, String name, int age, int score) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.score = score;
    }
    public Student(Long id, String name, String surname, int age, int score) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.score = score;
        this.surname = surname;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", score=" + score +
                ", surname='" + surname + '\'' +
                '}';
    }
}
