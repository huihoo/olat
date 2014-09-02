package org.olat.data.course.campus;

import javax.persistence.*;

@Entity
@IdClass(CourseLecturerPK.class)
@Table(name = "ck_lecturer_course")
public class CourseLecturer {
    @Id
    private Course course;

    @Id
    private Lecturer lecturer;

    public Course getCourse() {
        return course;
    }

    public Lecturer getLecturer() {
        return lecturer;
    }

}
