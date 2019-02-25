package ru.ifmo.rain.kochetkov.student;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Kochetkov Nikita M3234
 * Date: 25.02.2019
 */

public class StudentDB implements StudentGroupQuery {

    /**
     * Returns student groups, where both groups and students within a group are ordered by name.
     *
     * @param students
     */
    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return null;
    }

    /**
     * Returns student groups, where groups are ordered by name, and students within a group are ordered by id.
     *
     * @param students
     */
    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return null;
    }

    /**
     * Returns name of the group containing maximum number of students.
     * If there are more than one largest group, the one with smallest name is returned.
     *
     * @param students
     */
    @Override
    public String getLargestGroup(Collection<Student> students) {
        return null;
    }

    /**
     * Returns name of the group containing maximum number of students with distinct first names.
     * If there are more than one largest group, the one with smallest name is returned.
     *
     * @param students
     */
    @Override
    public String getLargestGroupFirstName(Collection<Student> students) {
        return null;
    }

    /**
     * Returns student {@link Student#getFirstName() first names}.
     *
     * @param students
     */
    @Override
    public List<String> getFirstNames(List<Student> students) {
        return null;
    }

    /**
     * Returns student {@link Student#getLastName() last names}.
     *
     * @param students
     */
    @Override
    public List<String> getLastNames(List<Student> students) {
        return null;
    }

    /**
     * Returns student {@link Student#getGroup() groups}.
     *
     * @param students
     */
    @Override
    public List<String> getGroups(List<Student> students) {
        return null;
    }

    /**
     * Returns student {@link Student#getGroup() groups}.
     *
     * @param students
     */
    @Override
    public List<String> getFullNames(List<Student> students) {
        return null;
    }

    /**
     * Returns distinct student {@link Student#getFirstName() first names} in alphabetical order.
     *
     * @param students
     */
    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return null;
    }

    /**
     * Returns name of the student with minimal {@link Student#getId() id}.
     *
     * @param students
     */
    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return null;
    }

    /**
     * Returns list of students sorted by {@link Student#getId() id}.
     *
     * @param students
     */
    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return null;
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
        return null;
    }

    /**
     * Returns list of students having specified first name. Students are ordered by name.
     *
     * @param students
     * @param name
     */
    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return null;
    }

    /**
     * Returns list of students having specified last name. Students are ordered by name.
     *
     * @param students
     * @param name
     */
    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return null;
    }

    /**
     * Returns list of students having specified groups. Students are ordered by name.
     *
     * @param students
     * @param group
     */
    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return null;
    }

    /**
     * Returns map of group's student last names mapped to minimal first name.
     *
     * @param students
     * @param group
     */
    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return null;
    }
}
