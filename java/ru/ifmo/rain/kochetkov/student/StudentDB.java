package ru.ifmo.rain.kochetkov.student;

import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/**
 * @author Kochetkov Nikita M3234
 * Date: 25.02.2019
 */

public class StudentDB implements StudentQuery {
    /**
     * Returns student {@link Student#getFirstName() first names}.
     *
     * @param students
     */
    @Override
    public List<String> getFirstNames(List<Student> students) {
        return students.stream()
                .map(Student::getFirstName)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Returns student {@link Student#getLastName() last names}.
     *
     * @param students
     */
    @Override
    public List<String> getLastNames(List<Student> students) {
        return students.stream()
                .map(Student::getLastName)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Returns student {@link Student#getGroup() groups}.
     *
     * @param students
     */
    @Override
    public List<String> getGroups(List<Student> students) {
        return students.stream()
                .map(Student::getGroup)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Returns student {@link Student#getGroup() groups}.
     *
     * @param students
     */
    @Override
    public List<String> getFullNames(List<Student> students) {
        return students.stream()
                .map((student) -> student.getFirstName() + " " + student.getLastName())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Returns distinct student {@link Student#getFirstName() first names} in alphabetical order.
     *
     * @param students
     */
    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return students.stream()
                .map(Student::getFirstName)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Returns name of the student with minimal {@link Student#getId() id}.
     *
     * @param students
     */
    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return students.stream()
                .min(Student::compareTo)
                .map(Student::getFirstName)
                .orElse("");
    }

    //!!!!!!!!!!!!!!!!!!!!

    /**
     * Returns list of students sorted by {@link Student#getId() id}.
     *
     * @param students
     */
    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return students.stream()
                .sorted(Student::compareTo)
                .collect(Collectors.toList());
    }

    /**
     * Returns list of students sorted by name
     * (students are ordered by {@link Student#getLastName() lastName},
     * students with equal last names are ordered by {@link Student#getFirstName() firstName},
     * students having equal both last and first names are ordered by {@link Student#getId() id}.
     *
     * @param students
     */
    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return students.stream()
                .sorted(Comparator.comparing(Student::getLastName, String::compareTo)
                        .thenComparing(Student::getFirstName, String::compareTo)
                        .thenComparingInt(Student::getId))
                .collect(Collectors.toList());
    }

    /**
     * Returns list of students having specified first name. Students are ordered by name.
     *
     * @param students
     * @param name
     */
    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return students.stream()
                .filter(student -> name.equals(student.getFirstName()))
                .sorted(Comparator.comparing(Student::getLastName, String::compareTo)
                        .thenComparing(Student::getFirstName, String::compareTo)
                        .thenComparingInt(Student::getId))
                .collect(Collectors.toList());
    }

    /**
     * Returns list of students having specified last name. Students are ordered by name.
     *
     * @param students
     * @param name
     */
    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return students.stream()
                .filter(student -> name.equals(student.getLastName()))
                .sorted(Comparator.comparing(Student::getLastName, String::compareTo)
                        .thenComparing(Student::getFirstName, String::compareTo)
                        .thenComparingInt(Student::getId))
                .collect(Collectors.toList());
    }

    /**
     * Returns list of students having specified groups. Students are ordered by name.
     *
     * @param students
     * @param group
     */
    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return students.stream()
                .filter(student -> group.equals(student.getGroup()))
                .sorted(Comparator.comparing(Student::getLastName, String::compareTo)
                        .thenComparing(Student::getFirstName, String::compareTo)
                        .thenComparingInt(Student::getId))
                .collect(Collectors.toList());

    }

    /**
     * Returns map of group's student last names mapped to minimal first name.
     *
     * @param students
     * @param group
     */
    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return students.stream()
                .filter(student -> group.equals(student.getGroup()))
                .collect(Collectors.toMap(Student::getLastName, Student::getFirstName, BinaryOperator.minBy(String::compareTo)));
    }
}
