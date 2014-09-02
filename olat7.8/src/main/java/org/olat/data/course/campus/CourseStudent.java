package org.olat.data.course.campus;

import javax.persistence.*;

@Entity
@IdClass(CourseStudentPK.class)
@Table(name = "ck_student_course")
public class CourseStudent {
    @Id
    private Course course;

    @Id
    private Student student;

    public Course getCourse() {
        return course;
    }

    public Student getStudent() {
        return student;
    }
}
