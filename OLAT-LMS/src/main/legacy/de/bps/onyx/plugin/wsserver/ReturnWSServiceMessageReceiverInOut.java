/**
 * ReturnWSServiceMessageReceiverInOut.java This file was auto-generated from WSDL by the Apache Axis2 version: 1.5 Built on : Apr 30, 2009 (06:07:24 EDT)
 */
package de.bps.onyx.plugin.wsserver;

/**
 * ReturnWSServiceMessageReceiverInOut message receiver
 */

public class ReturnWSServiceMessageReceiverInOut extends org.apache.axis2.receivers.AbstractInOutMessageReceiver {

	@Override
	public void invokeBusinessLogic(final org.apache.axis2.context.MessageContext msgContext, final org.apache.axis2.context.MessageContext newMsgContext)
			throws org.apache.axis2.AxisFault {

		try {

			// get the implementation class for the Web Service
			final Object obj = getTheImplementationObject(msgContext);

			final ReturnWSServiceSkeleton skel = (ReturnWSServiceSkeleton) obj;
			// Out Envelop
			org.apache.axiom.soap.SOAPEnvelope envelope = null;
			// Find the axisOperation that has been set by the Dispatch phase.
			final org.apache.axis2.description.AxisOperation op = msgContext.getOperationContext().getAxisOperation();
			if (op == null) { throw new org.apache.axis2.AxisFault(
					"Operation is not located, if this is doclit style the SOAP-ACTION should specified via the SOAP Action to use the RawXMLProvider"); }

			java.lang.String methodName;
			if ((op.getName() != null) && ((methodName = org.apache.axis2.util.JavaUtils.xmlNameToJavaIdentifier(op.getName().getLocalPart())) != null)) {

				if ("saveResultLocal".equals(methodName)) {

					de.bps.onyx.plugin.wsserver.SaveResultLocalResponse saveResultLocalResponse1 = null;
					final de.bps.onyx.plugin.wsserver.SaveResultLocal wrappedParam = (de.bps.onyx.plugin.wsserver.SaveResultLocal) fromOM(msgContext.getEnvelope()
							.getBody().getFirstElement(), de.bps.onyx.plugin.wsserver.SaveResultLocal.class, getEnvelopeNamespaces(msgContext.getEnvelope()));

					saveResultLocalResponse1 =

					skel.saveResultLocal(wrappedParam);

					envelope = toEnvelope(getSOAPFactory(msgContext), saveResultLocalResponse1, false);
				} else

				if ("saveResult".equals(methodName)) {

					de.bps.onyx.plugin.wsserver.SaveResultResponse saveResultResponse3 = null;
					final de.bps.onyx.plugin.wsserver.SaveResult wrappedParam = (de.bps.onyx.plugin.wsserver.SaveResult) fromOM(msgContext.getEnvelope().getBody()
							.getFirstElement(), de.bps.onyx.plugin.wsserver.SaveResult.class, getEnvelopeNamespaces(msgContext.getEnvelope()));

					saveResultResponse3 =

					skel.saveResult(wrappedParam);

					envelope = toEnvelope(getSOAPFactory(msgContext), saveResultResponse3, false);

				} else {
					throw new java.lang.RuntimeException("method not found");
				}

				newMsgContext.setEnvelope(envelope);
			}
		} catch (final java.lang.Exception e) {
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	//
	private org.apache.axiom.om.OMElement toOM(final de.bps.onyx.plugin.wsserver.SaveResultLocal param, final boolean optimizeContent) throws org.apache.axis2.AxisFault {

		try {
			return param.getOMElement(de.bps.onyx.plugin.wsserver.SaveResultLocal.MY_QNAME, org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		} catch (final org.apache.axis2.databinding.ADBException e) {
			throw org.apache.axis2.AxisFault.makeFault(e);
		}

	}

	private org.apache.axiom.om.OMElement toOM(final de.bps.onyx.plugin.wsserver.SaveResultLocalResponse param, final boolean optimizeContent)
			throws org.apache.axis2.AxisFault {

		try {
			return param.getOMElement(de.bps.onyx.plugin.wsserver.SaveResultLocalResponse.MY_QNAME, org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		} catch (final org.apache.axis2.databinding.ADBException e) {
			throw org.apache.axis2.AxisFault.makeFault(e);
		}

	}

	private org.apache.axiom.om.OMElement toOM(final de.bps.onyx.plugin.wsserver.SaveResult param, final boolean optimizeContent) throws org.apache.axis2.AxisFault {

		try {
			return param.getOMElement(de.bps.onyx.plugin.wsserver.SaveResult.MY_QNAME, org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		} catch (final org.apache.axis2.databinding.ADBException e) {
			throw org.apache.axis2.AxisFault.makeFault(e);
		}

	}

	private org.apache.axiom.om.OMElement toOM(final de.bps.onyx.plugin.wsserver.SaveResultResponse param, final boolean optimizeContent)
			throws org.apache.axis2.AxisFault {

		try {
			return param.getOMElement(de.bps.onyx.plugin.wsserver.SaveResultResponse.MY_QNAME, org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		} catch (final org.apache.axis2.databinding.ADBException e) {
			throw org.apache.axis2.AxisFault.makeFault(e);
		}

	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(final org.apache.axiom.soap.SOAPFactory factory,
			final de.bps.onyx.plugin.wsserver.SaveResultLocalResponse param, final boolean optimizeContent) throws org.apache.axis2.AxisFault {
		try {
			final org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();

			emptyEnvelope.getBody().addChild(param.getOMElement(de.bps.onyx.plugin.wsserver.SaveResultLocalResponse.MY_QNAME, factory));

			return emptyEnvelope;
		} catch (final org.apache.axis2.databinding.ADBException e) {
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private de.bps.onyx.plugin.wsserver.SaveResultLocalResponse wrapsaveResultLocal() {
		final de.bps.onyx.plugin.wsserver.SaveResultLocalResponse wrappedElement = new de.bps.onyx.plugin.wsserver.SaveResultLocalResponse();
		return wrappedElement;
	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(final org.apache.axiom.soap.SOAPFactory factory, final de.bps.onyx.plugin.wsserver.SaveResultResponse param,
			final boolean optimizeContent) throws org.apache.axis2.AxisFault {
		try {
			final org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();

			emptyEnvelope.getBody().addChild(param.getOMElement(de.bps.onyx.plugin.wsserver.SaveResultResponse.MY_QNAME, factory));

			return emptyEnvelope;
		} catch (final org.apache.axis2.databinding.ADBException e) {
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private de.bps.onyx.plugin.wsserver.SaveResultResponse wrapsaveResult() {
		final de.bps.onyx.plugin.wsserver.SaveResultResponse wrappedElement = new de.bps.onyx.plugin.wsserver.SaveResultResponse();
		return wrappedElement;
	}

	/**
	 * get the default envelope
	 */
	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(final org.apache.axiom.soap.SOAPFactory factory) {
		return factory.getDefaultEnvelope();
	}

	private java.lang.Object fromOM(final org.apache.axiom.om.OMElement param, final java.lang.Class type, final java.util.Map extraNamespaces)
			throws org.apache.axis2.AxisFault {

		try {

			if (de.bps.onyx.plugin.wsserver.SaveResultLocal.class.equals(type)) {

			return de.bps.onyx.plugin.wsserver.SaveResultLocal.Factory.parse(param.getXMLStreamReaderWithoutCaching());

			}

			if (de.bps.onyx.plugin.wsserver.SaveResultLocalResponse.class.equals(type)) {

			return de.bps.onyx.plugin.wsserver.SaveResultLocalResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());

			}

			if (de.bps.onyx.plugin.wsserver.SaveResult.class.equals(type)) {

			return de.bps.onyx.plugin.wsserver.SaveResult.Factory.parse(param.getXMLStreamReaderWithoutCaching());

			}

			if (de.bps.onyx.plugin.wsserver.SaveResultResponse.class.equals(type)) {

			return de.bps.onyx.plugin.wsserver.SaveResultResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());

			}

		} catch (final java.lang.Exception e) {
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
		return null;
	}

	/**
	 * A utility method that copies the namepaces from the SOAPEnvelope
	 */
	private java.util.Map getEnvelopeNamespaces(final org.apache.axiom.soap.SOAPEnvelope env) {
		final java.util.Map returnMap = new java.util.HashMap();
		final java.util.Iterator namespaceIterator = env.getAllDeclaredNamespaces();
		while (namespaceIterator.hasNext()) {
			final org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace) namespaceIterator.next();
			returnMap.put(ns.getPrefix(), ns.getNamespaceURI());
		}
		return returnMap;
	}

	private org.apache.axis2.AxisFault createAxisFault(final java.lang.Exception e) {
		org.apache.axis2.AxisFault f;
		final Throwable cause = e.getCause();
		if (cause != null) {
			f = new org.apache.axis2.AxisFault(e.getMessage(), cause);
		} else {
			f = new org.apache.axis2.AxisFault(e.getMessage());
		}

		return f;
	}

}// end of class
