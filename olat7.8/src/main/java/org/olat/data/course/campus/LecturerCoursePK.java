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
public class LecturerCoursePK implements Serializable {
    @Basic(optional = false)
    @Column(name = "lecturer_id")
    private long lecturerId;
    @Basic(optional = false)
    @Column(name = "course_id")
    private long courseId;

    public long getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(long lecturerId) {
        this.lecturerId = lecturerId;
    }

    public long getCourseId() {
        return courseId;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

}
