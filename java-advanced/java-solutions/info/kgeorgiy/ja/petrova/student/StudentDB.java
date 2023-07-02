package info.kgeorgiy.ja.petrova.student;

import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StudentDB implements StudentQuery {
    Comparator<Student> comparator = Comparator.comparing(Student::getLastName).thenComparing(Student::getFirstName).reversed()
            .thenComparing(Student::getId);

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getValue(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getValue(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return getValue(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getValue(students, e -> e.getFirstName() + " " + e.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return students.stream()
                .map(Student::getFirstName)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream()
                .max(Comparator.comparing(Student::getId))
                .map(Student::getFirstName)
                .orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortStudents(students, Comparator.comparing(Student::getId));
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudents(students, comparator);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return getStudents(students, name, Student::getFirstName);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return getStudents(students, name, Student::getLastName);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return getStudents(students, group, Student::getGroup);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(
            Collection<Student> students, GroupName group) {
        return students.stream()
                .filter(e -> e.getGroup().equals(group))
                .collect(Collectors.toMap(
                        Student::getLastName, Student::getFirstName,
                        BinaryOperator.minBy(String::compareTo)
                ));
    }

    private <T> List<T> getValue(
            List<Student> students,
            Function<Student, T> key
    ) {
        return students.stream().map(key).
                toList();
    }

    private <T> List<Student> getStudents(
            Collection<Student> students,
            T key,
            Function<Student, T> func
    ) {
        return students.stream()
                .filter(e -> func.apply(e).equals(key))
                .sorted(comparator)
                .toList();
    }

    private <T> List<Student> sortStudents(
            Collection<Student> students,
            Comparator<Student> comp
    ) {
        return students.stream()
                .sorted(comp)
                .toList();
    }
}
