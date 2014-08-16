/**
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * Copyright (c) 2005-2009 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * All rights reserved.
 */
package de.bps.course.nodes.den;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.presentation.framework.core.translator.Translator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.notifications.NotificationsHandler;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.Publisher;
import org.olat.core.util.notifications.Subscriber;
import org.olat.core.util.notifications.SubscriptionInfo;
import org.olat.core.util.notifications.items.SubscriptionListItem;
import org.olat.core.util.notifications.items.TitleItem;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.Structure;
import org.olat.course.nodes.CourseNode;
import org.olat.notifications.NotificationsUpgradeHelper;
import org.olat.repository.RepositoryManager;

import de.bps.course.nodes.DENCourseNode;

/**
 * Description:<br>
 * Notification handler for date enrollment
 * <P>
 * Initial Date: 25.08.2008 <br>
 * 
 * @author bja
 */
public class DENCourseNodeNotificationHandler implements NotificationsHandler {
	private static final Logger log = LoggerHelper.getLogger();


	@Override
	public SubscriptionInfo createSubscriptionInfo(final Subscriber subscriber, final Locale locale, final Date compareDate) {
		SubscriptionInfo si = null;
		final Publisher p = subscriber.getPublisher();

		final Date latestNews = p.getLatestNewsDate();

		// do not try to create a subscription info if state is deleted - results in
		// exceptions, course
		// can't be loaded when already deleted
		try {
			if (NotificationsManager.getInstance().isPublisherValid(p) && compareDate.before(latestNews)) {
				final Long courseId = new Long(p.getData());
				final ICourse course = loadCourseFromId(courseId);
				if (course != null) {
					final List<DENCourseNode> denNodes = getCourseDENNodes(course);
					final Translator trans = Util.createPackageTranslator(DENCourseNodeNotificationHandler.class, locale);

					final String cssClass = new DENCourseNodeConfiguration().getIconCSSClass();
					si = new SubscriptionInfo(new TitleItem(trans.translate("notifications.header", new String[] { course.getCourseTitle() }), cssClass), null);
					SubscriptionListItem subListItem;

					for (final DENCourseNode denNode : denNodes) {
						final String changer = "";
						final String desc = trans.translate("notifications.entry", new String[] { denNode.getLongTitle(), changer });

						final Date modDate = new Date();
						subListItem = new SubscriptionListItem(desc, null, modDate, cssClass);
						si.addSubscriptionListItem(subListItem);
					}
				}
			} else {
				si = NotificationsManager.getInstance().getNoSubscriptionInfo();
			}
		} catch (final Exception e) {
			log.error("Error creating enrollment notifications for subscriber: " + subscriber.getKey(), e);
			checkPublisher(p);
			si = NotificationsManager.getInstance().getNoSubscriptionInfo();
		}

		return si;
	}

	private void checkPublisher(final Publisher p) {
		try {
			if (!NotificationsUpgradeHelper.checkCourse(p)) {
				log.info("deactivating publisher with key; " + p.getKey(), null);
				NotificationsManager.getInstance().deactivate(p);
			}
		} catch (final Exception e) {
			log.error("", e);
		}
	}

	/**
	 * @param courseId
	 * @return
	 */
	private ICourse loadCourseFromId(final Long courseId) {
		return CourseFactory.loadCourse(courseId);
	}

	/**
	 * @param course
	 * @return
	 */
	private List<DENCourseNode> getCourseDENNodes(final ICourse course) {
		final List<DENCourseNode> denNodes = new ArrayList<DENCourseNode>(10);

		final Structure courseStruct = course.getRunStructure();
		final CourseNode rootNode = courseStruct.getRootNode();

		getCourseDENNodes(rootNode, denNodes);
		return denNodes;
	}

	/**
	 * @param node
	 * @param result
	 */
	private void getCourseDENNodes(final INode node, final List<DENCourseNode> result) {
		if (node != null) {
			if (node instanceof DENCourseNode) {
				result.add((DENCourseNode) node);
			}

			for (int i = 0; i < node.getChildCount(); i++) {
				getCourseDENNodes(node.getChildAt(i), result);
			}
		}
	}

	@Override
	public String createTitleInfo(final Subscriber subscriber, final Locale locale) {
		try {
			final Long resId = subscriber.getPublisher().getResId();
			final String displayName = RepositoryManager.getInstance().lookupDisplayNameByOLATResourceableId(resId);
			final Translator trans = Util.createPackageTranslator(DENCourseNodeNotificationHandler.class, locale);
			return trans.translate("notifications.header", new String[] { displayName });
		} catch (final Exception e) {
			log.error("Error while creating assessment notifications for subscriber: " + subscriber.getKey(), e);
			checkPublisher(subscriber.getPublisher());
			return "-";
		}
	}

	@Override
	public String getType() {
		return "DENCourseNode";
	}
}
