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
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 */
@Entity
@Table(name = "ck_text")
@NamedQueries({ @NamedQuery(name = Text.DELETE_ALL_TEXTS, query = "delete from Text"),
        @NamedQuery(name = Text.GET_TEXTS, query = "select t from Text t where t.courseId = :courseId and t.type = :type order by t.id, t.lineSeq asc"),
        @NamedQuery(name = Text.DELETE_TEXTS_BY_COURSE_ID, query = "delete from Text t where t.courseId = :courseId"),
        @NamedQuery(name = Text.DELETE_TEXTS_BY_COURSE_IDS, query = "delete from Text t where t.courseId in ( :courseIds)") })
public class Text {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "hilo")
    private Long id;

    @Column(name = "course_id")
    private long courseId;
    @Column(name = "type")
    private String type;
    @Column(name = "line_seq")
    private int lineSeq;
    @Column(name = "line")
    private String line;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_date")
    private Date modifiedDate;

    @ManyToOne()
    @JoinColumn(name = "course_id", nullable = false, insertable = false, updatable = false)
    private Course course;

    public static final String CONTENTS = "Veranstaltungsinhalt";
    public static final String INFOS = "Hinweise";
    public static final String MATERIALS = "Unterrichtsmaterialien";
    public static final String BREAK_TAG = "<br>";

    public static final String DELETE_ALL_TEXTS = "deleteAllTexts";
    public static final String DELETE_TEXTS_BY_COURSE_ID = "deleteTextsByCourseId";
    public static final String DELETE_TEXTS_BY_COURSE_IDS = "deleteTextsByCourseIds";
    public static final String GET_TEXTS = "getTexts";

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLineSeq() {
        return lineSeq;
    }

    public void setLineSeq(int lineSeq) {
        this.lineSeq = lineSeq;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
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
        builder.append("type", getType());
        builder.append("lineSeq", getLineSeq());
        builder.append("line", getLine());
        return builder.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Text))
            return false;
        Text theOther = (Text) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.getCourseId(), theOther.getCourseId());
        builder.append(this.getType(), theOther.getType());
        builder.append(this.getLineSeq(), theOther.getLineSeq());
        builder.append(this.getLine(), theOther.getLine());
        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(1239, 5475);
        builder.append(this.getCourseId());
        builder.append(this.getType());
        builder.append(this.getLineSeq());
        builder.append(this.getLine());
        return builder.toHashCode();
    }

}
