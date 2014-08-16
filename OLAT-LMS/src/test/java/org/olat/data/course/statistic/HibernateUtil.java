package org.olat.data.course.statistic;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

/**
 * From: http://www.theserverside.com/tt/articles/article.tss?l=UnitTesting
 * <P>
 * Initial Date: 02.03.2010 <br>
 * 
 * @author Stefan
 */
public class HibernateUtil {

    private static SessionFactory factory;

    public static synchronized Session getSession() {
        if (factory == null) {
            factory = new Configuration().configure().buildSessionFactory();
        }
        return factory.openSession();
    }

    public static void setSessionFactory(final SessionFactory factory) {
        HibernateUtil.factory = factory;
    }

    public static void save(final Object stat) {
        final Session session = getSession();
        final Transaction transaction = session.beginTransaction();
        try {
            session.save(stat);
            transaction.commit();
        } finally {
            session.close();
        }
    }
}
