package ru.ifmo.rain.kochetkov.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Kochetkov Nikita M3234
 * Date: 25.02.2019
 */

public class StudentDB implements StudentQuery {

    private Comparator<Student> sortByRule = Comparator
            .comparing(Student::getLastName, String::compareTo)
            .thenComparing(Student::getFirstName, String::compareTo)
            .thenComparingInt(Student::getId);

    private List<String> collectToList(Stream<Student> stream, Function<Student, String> function) {
        return stream.map(function).collect(Collectors.toList());
    }

    private List<Student> collectSorted(Stream<Student> stream, Comparator<Student> comparator) {
        return stream.sorted(comparator).collect(Collectors.toList());
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return collectToList(students.stream(), Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return collectToList(students.stream(), Student::getLastName);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return collectToList(students.stream(), Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return collectToList(students.stream(), (student) -> student.getFirstName() + " " + student.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return students.stream()
                .map(Student::getFirstName)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return students.stream()
                .min(Student::compareTo)
                .map(Student::getFirstName)
                .orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return collectSorted(students.stream(), Student::compareTo);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return collectSorted(students.stream(), sortByRule);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return collectSorted(students.stream().filter(student -> name.equals(student.getFirstName())), sortByRule);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return collectSorted(students.stream().filter(student -> name.equals(student.getLastName())), sortByRule);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return collectSorted(students.stream().filter(student -> group.equals(student.getGroup())), sortByRule);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return students.stream()
                .filter(student -> group.equals(student.getGroup()))
                .collect(Collectors.toMap(Student::getLastName, Student::getFirstName, BinaryOperator.minBy(String::compareTo)));
    }

    public List<Group> getGroupsByName(Collection<Student> students) {
         return students.stream()
                 .collect(Collectors.groupingBy(Student::getGroup,  TreeMap::new, Collectors.toList()))
                 .entrySet()
                 .stream()
                 .map(elem -> new Group(elem.getKey(), sortStudentsByName(elem.getValue())))
                 .collect(Collectors.toList());
    }

    public List<Group> getGroupsById(Collection<Student> students) {
        return students.stream()
                .collect(Collectors.groupingBy(Student::getGroup,  TreeMap::new, Collectors.toList()))
                .entrySet()
                .stream()
                .map(elem -> new Group(elem.getKey(), sortStudentsById(elem.getValue())))
                .collect(Collectors.toList());
    }
}
