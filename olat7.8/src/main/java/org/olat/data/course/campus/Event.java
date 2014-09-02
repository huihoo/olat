package org.olat.data.course.campus;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

/**
 * 
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 */
@Entity
@Table(name = "ck_event")
@NamedQueries({ @NamedQuery(name = Event.DELETE_ALL_EVENTS, query = "delete from Event"),
        @NamedQuery(name = Event.DELETE_EVENTS_BY_COURSE_ID, query = "delete from Event e where e.courseId = :courseId"),
        @NamedQuery(name = Event.DELETE_EVENTS_BY_COURSE_IDS, query = "delete from Event e where e.courseId in( :courseIds)") })
public class Event {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "hilo")
    private Long id;

    @Column(name = "course_id")
    private long courseId;
    @Column(name = "date")
    private Date date;
    @Column(name = "start")
    private String start;
    @Column(name = "end")
    private String end;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_date")
    private Date modifiedDate;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false, insertable = false, updatable = false)
    private Course course;

    public static final String DELETE_ALL_EVENTS = "deleteAllEvents";
    public static final String DELETE_EVENTS_BY_COURSE_ID = "deleteEventsByCourseId";
    public static final String DELETE_EVENTS_BY_COURSE_IDS = "deleteEventsByCourseIds";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getCourseId() {
        return courseId;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = new Date();
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("id", getId());
        builder.append("courseId", getCourseId());
        builder.append("date", getDate());
        builder.append("start", getStart());
        builder.append("end", getEnd());
        return builder.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Event))
            return false;
        Event theOther = (Event) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.getCourseId(), theOther.getCourseId());
        builder.append(this.getDate(), theOther.getDate());
        builder.append(this.getStart(), theOther.getStart());
        builder.append(this.getEnd(), theOther.getEnd());
        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(1239, 5475);
        builder.append(this.getCourseId());
        builder.append(this.getDate());
        builder.append(this.getStart());
        builder.append(this.getEnd());
        return builder.toHashCode();
    }

}
