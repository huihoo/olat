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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.olat.presentation.framework.core.media.MediaResource;

/**
 * Description:<br>
 * TODO: Mike Stock Class Description for JFreeChartMediaResource
 * <P>
 * Initial Date: 18.04.2006 <br>
 * 
 * @author Mike Stock
 */
public class JFreeChartMediaResource implements MediaResource {

	private static final String CONTENT_TYPE = "image/png";
	private static final Long UNDEF_SIZE = new Long(-1);
	private static final Long UNDEF_LAST_MODIFIED = new Long(-1);

	private final JFreeChart chart;
	private final Long width, height;

	public JFreeChartMediaResource(final JFreeChart chart, final Long width, final Long height) {
		this.chart = chart;
		this.width = width;
		this.height = height;
	}

	/**
	 * @see org.olat.presentation.framework.media.MediaResource#getContentType()
	 */
	@Override
	public String getContentType() {
		return CONTENT_TYPE;
	}

	/**
	 * @see org.olat.presentation.framework.media.MediaResource#getSize()
	 */
	@Override
	public Long getSize() {
		return UNDEF_SIZE;
	}

	/**
	 * @see org.olat.presentation.framework.media.MediaResource#getInputStream()
	 */
	@Override
	public InputStream getInputStream() {
		ByteArrayInputStream pIn = null;
		try {
			final ByteArrayOutputStream pOut = new ByteArrayOutputStream();
			ChartUtilities.writeChartAsPNG(pOut, chart, width.intValue(), height.intValue());
			pIn = new ByteArrayInputStream(pOut.toByteArray());
		} catch (final IOException e) {
			// bummer...
		}
		return pIn;
	}

	/**
	 * @see org.olat.presentation.framework.media.MediaResource#getLastModified()
	 */
	@Override
	public Long getLastModified() {
		return UNDEF_LAST_MODIFIED;
	}

	/**
	 * @see org.olat.presentation.framework.media.MediaResource#prepare(javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void prepare(final HttpServletResponse hres) {
		// nothing to do...
	}

	/**
	 * @see org.olat.presentation.framework.media.MediaResource#release()
	 */
	@Override
	public void release() {
		// nothing to do...
	}

}
