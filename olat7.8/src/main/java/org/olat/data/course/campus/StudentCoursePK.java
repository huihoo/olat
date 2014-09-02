package org.olat.data.course.campus;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 */
@SuppressWarnings("serial")
@Embeddable
public class StudentCoursePK implements Serializable {
    @Basic(optional = false)
    @Column(name = "student_id")
    private long studentId;
    @Basic(optional = false)
    @Column(name = "course_id")
    private long courseId;

    public StudentCoursePK() {
    }

    public StudentCoursePK(long studentId, long courseId) {
        this.studentId = studentId;
        this.courseId = courseId;
    }

    public long getStudentId() {
        return studentId;
    }

    public void setStudentId(long studentId) {
        this.studentId = studentId;
    }

    public long getCourseId() {
        return courseId;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

}
