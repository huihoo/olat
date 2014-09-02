package org.olat.data.course.campus;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 */
@Entity
@Table(name = "ck_student_course")
@NamedQueries({
        @NamedQuery(name = StudentCourse.DELETE_STUDENT_BY_COURSE_ID, query = "delete from StudentCourse sc where sc.pk.courseId = :courseId "),
        @NamedQuery(name = StudentCourse.DELETE_STUDENTS_BY_COURSE_IDS, query = "delete from StudentCourse sc where sc.pk.courseId in ( :courseIds) "),
        @NamedQuery(name = StudentCourse.DELETE_STUDENT_BY_STUDENT_ID, query = "delete from StudentCourse sc where sc.pk.studentId = :studentId "),
        @NamedQuery(name = StudentCourse.DELETE_STUDENTS_BY_STUDENT_IDS, query = "delete from StudentCourse sc where sc.pk.studentId in ( :studentIds) "),
        @NamedQuery(name = StudentCourse.DELETE_ALL_NOT_UPDATED_SC_BOOKING, query = "delete from StudentCourse sc where sc.modifiedDate is not null and sc.modifiedDate < :lastImportDate") })
public class StudentCourse {
    @EmbeddedId
    private StudentCoursePK pk;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_date")
    private Date modifiedDate;

    public static final String DELETE_STUDENT_BY_COURSE_ID = "deleteStudentByCourseId";
    public static final String DELETE_STUDENTS_BY_COURSE_IDS = "deleteStudentsByCourseIds";

    public static final String DELETE_STUDENT_BY_STUDENT_ID = "deleteStudentByStudentId";
    public static final String DELETE_STUDENTS_BY_STUDENT_IDS = "deleteStudentsByStudentIds";
    public static final String DELETE_ALL_NOT_UPDATED_SC_BOOKING = "deleteAllNotUpdatedSCBooking";

    public StudentCourse() {
    }

    public StudentCourse(StudentCoursePK pk) {
        this.pk = pk;
    }

    public StudentCoursePK getPk() {
        return pk;
    }

    public void setPk(StudentCoursePK pk) {
        this.pk = pk;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("studentId", getPk().getStudentId());
        builder.append("courseId", getPk().getCourseId());
        return builder.toString();
    }

}
