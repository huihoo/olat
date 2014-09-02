package org.olat.data.course.campus;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

/**
 * Initial Date: 27.06.2012 <br>
 * 
 * @author aabouc
 */
@Entity
@NamedQueries({
        @NamedQuery(name = SapOlatUser.GET_SAP_OLAT_USERS_BY_SAP_IDS, query = "from  SapOlatUser m  " + " where m.sapUserId in (:" + SapOlatUser.SAP_IDS_PARAM + ")"),
        @NamedQuery(name = SapOlatUser.DELETE_SAP_OLAT_LECTURERS_BY_SAP_IDS, query = "delete from SapOlatUser m where m.sapUserType = 'LECTURER' and m.sapUserId in ( :sapIds) "),
        @NamedQuery(name = SapOlatUser.DELETE_SAP_OLAT_STUDENTS_BY_SAP_IDS, query = "delete from SapOlatUser m where m.sapUserType = 'STUDENT' and m.sapUserId in ( :sapIds) "),
        @NamedQuery(name = SapOlatUser.DELETE_SAP_OLAT_STUDENTS, query = "delete from SapOlatUser m where m.sapUserType = 'STUDENT' and m.sapUserId not in (select id from Student) "),
        @NamedQuery(name = SapOlatUser.DELETE_SAP_OLAT_LECTURERS, query = "delete from SapOlatUser m where m.sapUserType = 'LECTURER' and m.sapUserId not in (select id from Lecturer ) ") })
@Table(name = "ck_olat_user")
public class SapOlatUser {

    @Id
    @Column(name = "sap_user_id")
    private Long sapUserId;

    @Column(name = "olat_user_name")
    private String olatUserName;

    @Column(name = "kind_of_mapping")
    private String kindOfMapping = "AUTO";

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "mapping_timestamp")
    private Date mappingTimeStamp;

    public static enum SapUserType {
        STUDENT, LECTURER;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "sap_user_type")
    private SapUserType sapUserType;

    public static final String GET_SAP_OLAT_USERS_BY_SAP_IDS = "selectSapOlatUsersBySapIds";
    public static final String SAP_IDS_PARAM = "sapUserIds";
    public static final String DELETE_SAP_OLAT_LECTURERS_BY_SAP_IDS = "deleteSapOlatLecturersBySapIds";
    public static final String DELETE_SAP_OLAT_STUDENTS_BY_SAP_IDS = "deleteSapOlatStudentsBySapIds";

    public static final String DELETE_SAP_OLAT_LECTURERS = "deleteSapOlatLecturers";
    public static final String DELETE_SAP_OLAT_STUDENTS = "deleteSapOlatStudents";

    public SapOlatUser() {
    }

    public SapOlatUser(Long sapUserId, String olatUserName, SapUserType sapUserType) {
        this.sapUserId = sapUserId;
        this.olatUserName = olatUserName;
        this.sapUserType = sapUserType;
    }

    public Long getSapUserId() {
        return sapUserId;
    }

    public void setSapUserId(Long sapUserId) {
        this.sapUserId = sapUserId;
    }

    public String getOlatUserName() {
        return olatUserName;
    }

    public void setOlatUserName(String olatUserName) {
        this.olatUserName = olatUserName;
    }

    public SapUserType getSapUserType() {
        return sapUserType;
    }

    public void setSapUserType(SapUserType sapUserType) {
        this.sapUserType = sapUserType;
    }

    public String getKindOfMapping() {
        return kindOfMapping;
    }

    public void setKindOfMapping(String kindOfMapping) {
        this.kindOfMapping = kindOfMapping;
    }

    public Date getMappingTimeStamp() {
        return mappingTimeStamp;
    }

    public void setMappingTimeStamp(Date mappingTimeStamp) {
        this.mappingTimeStamp = mappingTimeStamp;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SapOlatUser))
            return false;
        SapOlatUser theOther = (SapOlatUser) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.getOlatUserName(), theOther.getOlatUserName());
        builder.append(this.getSapUserType(), theOther.getSapUserType());
        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(1239, 5475);
        builder.append(this.getOlatUserName());
        builder.append(this.getSapUserType());
        return builder.toHashCode();
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("sapUserId", getSapUserId());
        builder.append("sapUserType", getSapUserType());
        builder.append("olatUserName", getOlatUserName());
        return builder.toString();
    }

}
