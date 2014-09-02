/**
 * JGS goodsolutions GmbH<br>
 * http://www.goodsolutions.ch
 * <p>
 * This software is protected by the goodsolutions software license.<br>
 * <p>
 * Copyright (c) 2005-2006 by JGS goodsolutions GmbH, Switzerland.<br>
 * All rights reserved.
 * <p>
 */
package ch.goodsolutions.olat.jfreechart;

import org.jfree.chart.JFreeChart;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.image.ImageComponent;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.Event;
import org.olat.presentation.framework.core.control.WindowControl;

/**
 * Description:<br>
 * This is a simple Controller wrapper to JFreeChart charts. Drop in a JFreeChart chart, set its width and height and the Controller's initial component will render the
 * chart as a PNG. For JFreeChart reference please see http://www.jfree.org/jfreechart/index.php
 * <P>
 * Initial Date: 18.04.2006 <br>
 * 
 * @author Mike Stock
 */
public class JFreeChartController extends DefaultController {

	ImageComponent imgComponent;

	/**
	 * @param wControl
	 */
	public JFreeChartController(final JFreeChart chart, final Long height, final Long width, final WindowControl wControl) {
		super(wControl);
		imgComponent = new ImageComponent("jfreechartwrapper");
		imgComponent.setWidth(width);
		imgComponent.setHeight(height);
		imgComponent.setMediaResource(new JFreeChartMediaResource(chart, width, height));
		setInitialComponent(imgComponent);
	}

	/**
	 * @see org.olat.presentation.framework.control.DefaultController#event(org.olat.presentation.framework.UserRequest, org.olat.presentation.framework.components.Component, org.olat.presentation.framework.control.Event)
	 */
	@Override
	public void event(final UserRequest ureq, final Component source, final Event event) {
		// nothing to do...

	}

	/**
	 * @see org.olat.presentation.framework.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		// nothing to do...
	}

}
