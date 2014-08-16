/**
 * Onyx_reporterStub.java This file was auto-generated from WSDL by the Apache Axis2 version: 1.4.1 Built on : Aug 13, 2008 (05:03:35 LKT)
 */
package de.bps.webservices.clients.onyxreporter.stubs;

import de.bps.webservices.WebServiceModule;

/*
 * Onyx_reporterStub java implementation
 */

public class OnyxReporterStub extends org.apache.axis2.client.Stub {
	protected org.apache.axis2.description.AxisOperation[] _operations;

	// hashmaps to keep the fault mapping
	private final java.util.HashMap faultExceptionNameMap = new java.util.HashMap();
	private final java.util.HashMap faultExceptionClassNameMap = new java.util.HashMap();
	private final java.util.HashMap faultMessageMap = new java.util.HashMap();

	private static int counter = 0;

	// private static final String SERVICE_PATH = ServiceConfigurationImporter.getInstance().getServiceConfiguration().getCompleteLink();
	private static final String SERVICE_PATH = WebServiceModule.getService("OnyxReporterWebserviceClient").getAddress();

	private static synchronized String getUniqueSuffix() {
		// reset the counter if it is greater than 99999
		if (counter > 99999) {
			counter = 0;
		}
		counter = counter + 1;
		return Long.toString(System.currentTimeMillis()) + "_" + counter;
	}

	private void populateAxisService() throws org.apache.axis2.AxisFault {

		// creating the Service with a unique name
		_service = new org.apache.axis2.description.AxisService("Onyx_reporter" + getUniqueSuffix());
		addAnonymousOperations();

		// creating the operations
		org.apache.axis2.description.AxisOperation __operation;

		_operations = new org.apache.axis2.description.AxisOperation[5];

		__operation = new org.apache.axis2.description.OutInAxisOperation();

		__operation.setName(new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/", "resultValues"));
		_service.addOperation(__operation);

		_operations[0] = __operation;

		__operation = new org.apache.axis2.description.OutInAxisOperation();

		__operation.setName(new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/", "disarmSite"));
		_service.addOperation(__operation);

		_operations[1] = __operation;

		__operation = new org.apache.axis2.description.OutInAxisOperation();

		__operation.setName(new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/", "initiateSite"));
		_service.addOperation(__operation);

		_operations[2] = __operation;

		__operation = new org.apache.axis2.description.OutInAxisOperation();

		__operation.setName(new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/", "armSite"));
		_service.addOperation(__operation);

		_operations[3] = __operation;

		__operation = new org.apache.axis2.description.OutInAxisOperation();

		__operation.setName(new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/", "resultVariableCandidates"));
		_service.addOperation(__operation);

		_operations[4] = __operation;

	}

	// populates the faults
	private void populateFaults() {

	}

	/**
	 * Constructor that takes in a configContext
	 */

	public OnyxReporterStub(final org.apache.axis2.context.ConfigurationContext configurationContext, final java.lang.String targetEndpoint)
			throws org.apache.axis2.AxisFault {
		this(configurationContext, targetEndpoint, false);
	}

	/**
	 * Constructor that takes in a configContext and useseperate listner
	 */
	public OnyxReporterStub(final org.apache.axis2.context.ConfigurationContext configurationContext, final java.lang.String targetEndpoint,
			final boolean useSeparateListener) throws org.apache.axis2.AxisFault {
		// To populate AxisService
		populateAxisService();
		populateFaults();

		_serviceClient = new org.apache.axis2.client.ServiceClient(configurationContext, _service);

		_serviceClient.getOptions().setTo(new org.apache.axis2.addressing.EndpointReference(targetEndpoint));
		_serviceClient.getOptions().setUseSeparateListener(useSeparateListener);

	}

	/**
	 * Default Constructor
	 */
	public OnyxReporterStub(final org.apache.axis2.context.ConfigurationContext configurationContext) throws org.apache.axis2.AxisFault {

		this(configurationContext, SERVICE_PATH);

	}

	/**
	 * Default Constructor
	 */
	public OnyxReporterStub() throws org.apache.axis2.AxisFault {

		this(SERVICE_PATH);

	}

	/**
	 * Constructor taking the target endpoint
	 */
	public OnyxReporterStub(final java.lang.String targetEndpoint) throws org.apache.axis2.AxisFault {
		this(null, targetEndpoint);
	}

	/**
	 * Auto generated method signature
	 * 
	 * @see localhost.axis2_tester.services.Onyx_reporter#resultValues
	 * @param resultValuesInput
	 */

	public de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ResultValuesResponse resultValues(

	final de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ResultValuesInputE resultValuesInput)

	throws java.rmi.RemoteException

	{
		org.apache.axis2.context.MessageContext _messageContext = null;
		try {
			final org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[0].getName());
			_operationClient.getOptions().setAction(SERVICE_PATH + "/resultValues");
			_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

			addPropertyToOperationClient(_operationClient, org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR, "&");

			// create a message context
			_messageContext = new org.apache.axis2.context.MessageContext();

			// create SOAP envelope with that payload
			org.apache.axiom.soap.SOAPEnvelope env = null;

			env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()), resultValuesInput, optimizeContent(new javax.xml.namespace.QName(
					"http://localhost:8080/axis2_tester/services/", "resultValues")));

			// adding SOAP soap_headers
			_serviceClient.addHeadersToEnvelope(env);
			// set the message context with that soap envelope
			_messageContext.setEnvelope(env);

			// add the message contxt to the operation client
			_operationClient.addMessageContext(_messageContext);

			// execute the operation client
			_operationClient.execute(true);

			final org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient
					.getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			final org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();

			final java.lang.Object object = fromOM(_returnEnv.getBody().getFirstElement(),
					de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ResultValuesResponse.class, getEnvelopeNamespaces(_returnEnv));

			return (de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ResultValuesResponse) object;

		} catch (final org.apache.axis2.AxisFault f) {

			final org.apache.axiom.om.OMElement faultElt = f.getDetail();
			if (faultElt != null) {
				if (faultExceptionNameMap.containsKey(faultElt.getQName())) {
					// make the fault by reflection
					try {
						final java.lang.String exceptionClassName = (java.lang.String) faultExceptionClassNameMap.get(faultElt.getQName());
						final java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
						final java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
						// message class
						final java.lang.String messageClassName = (java.lang.String) faultMessageMap.get(faultElt.getQName());
						final java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
						final java.lang.Object messageObject = fromOM(faultElt, messageClass, null);
						final java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage", new java.lang.Class[] { messageClass });
						m.invoke(ex, new java.lang.Object[] { messageObject });

						throw new java.rmi.RemoteException(ex.getMessage(), ex);
					} catch (final java.lang.ClassCastException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (final java.lang.ClassNotFoundException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (final java.lang.NoSuchMethodException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (final java.lang.reflect.InvocationTargetException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (final java.lang.IllegalAccessException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (final java.lang.InstantiationException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					}
				} else {
					throw f;
				}
			} else {
				throw f;
			}
		} finally {
			_messageContext.getTransportOut().getSender().cleanup(_messageContext);
		}
	}

	/**
	 * Auto generated method signature
	 * 
	 * @see localhost.axis2_tester.services.Onyx_reporter#resultVariableCandidates
	 * @param resultVariableCandidatesInput
	 */

	public de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ResultVariableCandidatesResponse resultVariableCandidates(

	final de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ResultVariableCandidatesInput resultVariableCandidatesInput)

	throws java.rmi.RemoteException

	{
		org.apache.axis2.context.MessageContext _messageContext = null;
		try {
			final org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[0].getName());
			_operationClient.getOptions().setAction(SERVICE_PATH + "/resultVariableCandidates");
			_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

			addPropertyToOperationClient(_operationClient, org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR, "&");

			// create a message context
			_messageContext = new org.apache.axis2.context.MessageContext();

			// create SOAP envelope with that payload
			org.apache.axiom.soap.SOAPEnvelope env = null;

			env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()), resultVariableCandidatesInput, optimizeContent(new javax.xml.namespace.QName(
					"http://localhost:8080/axis2_tester/services/", "resultVariableCandidates")));

			// adding SOAP soap_headers
			_serviceClient.addHeadersToEnvelope(env);
			// set the message context with that soap envelope
			_messageContext.setEnvelope(env);

			// add the message contxt to the operation client
			_operationClient.addMessageContext(_messageContext);

			// execute the operation client
			_operationClient.execute(true);

			final org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient
					.getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			final org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();

			final java.lang.Object object = fromOM(_returnEnv.getBody().getFirstElement(),
					de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ResultVariableCandidatesResponse.class, getEnvelopeNamespaces(_returnEnv));

			return (de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ResultVariableCandidatesResponse) object;

		} catch (final org.apache.axis2.AxisFault f) {

			final org.apache.axiom.om.OMElement faultElt = f.getDetail();
			if (faultElt != null) {
				if (faultExceptionNameMap.containsKey(faultElt.getQName())) {
					// make the fault by reflection
					try {
						final java.lang.String exceptionClassName = (java.lang.String) faultExceptionClassNameMap.get(faultElt.getQName());
						final java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
						final java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
						// message class
						final java.lang.String messageClassName = (java.lang.String) faultMessageMap.get(faultElt.getQName());
						final java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
						final java.lang.Object messageObject = fromOM(faultElt, messageClass, null);
						final java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage", new java.lang.Class[] { messageClass });
						m.invoke(ex, new java.lang.Object[] { messageObject });

						throw new java.rmi.RemoteException(ex.getMessage(), ex);
					} catch (final java.lang.ClassCastException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (final java.lang.ClassNotFoundException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (final java.lang.NoSuchMethodException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (final java.lang.reflect.InvocationTargetException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (final java.lang.IllegalAccessException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (final java.lang.InstantiationException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					}
				} else {
					throw f;
				}
			} else {
				throw f;
			}
		} finally {
			_messageContext.getTransportOut().getSender().cleanup(_messageContext);
		}
	}

	public static class ResultVariableCandidatesResponse implements org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/",
				"resultVariableCandidatesResponse", "ns1");

		private static java.lang.String generatePrefix(final java.lang.String namespace) {
			if (namespace.equals("http://localhost:8080/axis2_tester/services/")) { return "ns1"; }
			return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
		}

		/**
		 * field for ResultVariableCandidatesResponse
		 */

		protected ResultValuesOutput localResultVariableCandidatesResponse;

		/**
		 * Auto generated getter method
		 * 
		 * @return ResultValuesOutput
		 */
		public ResultValuesOutput getResultVariableCandidatesResponse() {
			return localResultVariableCandidatesResponse;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param ResultVariableCandidatesResponse
		 */
		public void setResultVariableCandidatesResponse(final ResultValuesOutput param) {

			this.localResultVariableCandidatesResponse = param;

		}

		/**
		 * isReaderMTOMAware
		 * 
		 * @return true if the reader supports MTOM
		 */
		public static boolean isReaderMTOMAware(final javax.xml.stream.XMLStreamReader reader) {
			boolean isReaderMTOMAware = false;

			try {
				isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
			} catch (final java.lang.IllegalArgumentException e) {
				isReaderMTOMAware = false;
			}
			return isReaderMTOMAware;
		}

		/**
		 * @param parentQName
		 * @param factory
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getOMElement(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory)
				throws org.apache.axis2.databinding.ADBException {

			final org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this, MY_QNAME) {

				public void serialize(final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
					ResultVariableCandidatesResponse.this.serialize(MY_QNAME, factory, xmlWriter);
				}
			};
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(MY_QNAME, factory, dataSource);

		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException,
				org.apache.axis2.databinding.ADBException {
			serialize(parentQName, factory, xmlWriter, false);
		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter, final boolean serializeType)
				throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

			// We can safely assume an element has only one type associated with it

			if (localResultVariableCandidatesResponse == null) { throw new org.apache.axis2.databinding.ADBException("Property cannot be null!"); }
			localResultVariableCandidatesResponse.serialize(MY_QNAME, factory, xmlWriter);

		}

		/**
		 * Util method to write an attribute with the ns prefix
		 */
		private void writeAttribute(final java.lang.String prefix, final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (xmlWriter.getPrefix(namespace) == null) {
				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);

			}

			xmlWriter.writeAttribute(namespace, attName, attValue);

		}

		/**
		 * Util method to write an attribute without the ns prefix
		 */
		private void writeAttribute(final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (namespace.equals("")) {
				xmlWriter.writeAttribute(attName, attValue);
			} else {
				registerPrefix(xmlWriter, namespace);
				xmlWriter.writeAttribute(namespace, attName, attValue);
			}
		}

		/**
		 * Util method to write an attribute without the ns prefix
		 */
		private void writeQNameAttribute(final java.lang.String namespace, final java.lang.String attName, final javax.xml.namespace.QName qname,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

			final java.lang.String attributeNamespace = qname.getNamespaceURI();
			java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
			if (attributePrefix == null) {
				attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
			}
			java.lang.String attributeValue;
			if (attributePrefix.trim().length() > 0) {
				attributeValue = attributePrefix + ":" + qname.getLocalPart();
			} else {
				attributeValue = qname.getLocalPart();
			}

			if (namespace.equals("")) {
				xmlWriter.writeAttribute(attName, attributeValue);
			} else {
				registerPrefix(xmlWriter, namespace);
				xmlWriter.writeAttribute(namespace, attName, attributeValue);
			}
		}

		/**
		 * method to handle Qnames
		 */

		private void writeQName(final javax.xml.namespace.QName qname, final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			final java.lang.String namespaceURI = qname.getNamespaceURI();
			if (namespaceURI != null) {
				java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
				if (prefix == null) {
					prefix = generatePrefix(namespaceURI);
					xmlWriter.writeNamespace(prefix, namespaceURI);
					xmlWriter.setPrefix(prefix, namespaceURI);
				}

				if (prefix.trim().length() > 0) {
					xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
				} else {
					// i.e this is the default namespace
					xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
				}

			} else {
				xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
			}
		}

		private void writeQNames(final javax.xml.namespace.QName[] qnames, final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

			if (qnames != null) {
				// we have to store this data until last moment since it is not possible to write any
				// namespace data after writing the charactor data
				final java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
				java.lang.String namespaceURI = null;
				java.lang.String prefix = null;

				for (int i = 0; i < qnames.length; i++) {
					if (i > 0) {
						stringToWrite.append(" ");
					}
					namespaceURI = qnames[i].getNamespaceURI();
					if (namespaceURI != null) {
						prefix = xmlWriter.getPrefix(namespaceURI);
						if ((prefix == null) || (prefix.length() == 0)) {
							prefix = generatePrefix(namespaceURI);
							xmlWriter.writeNamespace(prefix, namespaceURI);
							xmlWriter.setPrefix(prefix, namespaceURI);
						}

						if (prefix.trim().length() > 0) {
							stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
						} else {
							stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
						}
					} else {
						stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
					}
				}
				xmlWriter.writeCharacters(stringToWrite.toString());
			}

		}

		/**
		 * Register a namespace prefix
		 */
		private java.lang.String registerPrefix(final javax.xml.stream.XMLStreamWriter xmlWriter, final java.lang.String namespace)
				throws javax.xml.stream.XMLStreamException {
			java.lang.String prefix = xmlWriter.getPrefix(namespace);

			if (prefix == null) {
				prefix = generatePrefix(namespace);

				while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
					prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
				}

				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);
			}

			return prefix;
		}

		/**
		 * databinding method to get an XML representation of this object
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(final javax.xml.namespace.QName qName) throws org.apache.axis2.databinding.ADBException {

			// We can safely assume an element has only one type associated with it
			return localResultVariableCandidatesResponse.getPullParser(MY_QNAME);

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening
			 * reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
			 * Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the
			 * end element of its outer element
			 */
			public static ResultVariableCandidatesResponse parse(final javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
				final ResultVariableCandidatesResponse object = new ResultVariableCandidatesResponse();

				final int event;
				final java.lang.String nillableValue = null;
				final java.lang.String prefix = "";
				final java.lang.String namespaceuri = "";
				try {

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					// Note all attributes that were handled. Used to differ normal attributes
					// from anyAttributes.
					final java.util.Vector handledAttributes = new java.util.Vector();

					while (!reader.isEndElement()) {
						if (reader.isStartElement()) {

							if (reader.isStartElement()
									&& new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/", "resultVariableCandidatesResponse").equals(reader
											.getName())) {

								object.setResultVariableCandidatesResponse(ResultValuesOutput.Factory.parse(reader));

							} // End of if for expected property start element

							else {
								// A start element we are not expecting indicates an invalid parameter was passed
								throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
							}

						} else {
							reader.next();
						}
					} // end of while loop

				} catch (final javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class ResultVariableCandidatesInput implements org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/",
				"resultVariableCandidatesInput", "ns1");

		private static java.lang.String generatePrefix(final java.lang.String namespace) {
			if (namespace.equals("http://localhost:8080/axis2_tester/services/")) { return "ns1"; }
			return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
		}

		/**
		 * field for ResultVariableCandidatesInput
		 */

		protected ContentPackage localResultVariableCandidatesInput;

		/**
		 * Auto generated getter method
		 * 
		 * @return ContentPackage
		 */
		public ContentPackage getResultVariableCandidatesInput() {
			return localResultVariableCandidatesInput;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param ResultVariableCandidatesInput
		 */
		public void setResultVariableCandidatesInput(final ContentPackage param) {

			this.localResultVariableCandidatesInput = param;

		}

		/**
		 * isReaderMTOMAware
		 * 
		 * @return true if the reader supports MTOM
		 */
		public static boolean isReaderMTOMAware(final javax.xml.stream.XMLStreamReader reader) {
			boolean isReaderMTOMAware = false;

			try {
				isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
			} catch (final java.lang.IllegalArgumentException e) {
				isReaderMTOMAware = false;
			}
			return isReaderMTOMAware;
		}

		/**
		 * @param parentQName
		 * @param factory
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getOMElement(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory)
				throws org.apache.axis2.databinding.ADBException {

			final org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this, MY_QNAME) {

				public void serialize(final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
					ResultVariableCandidatesInput.this.serialize(MY_QNAME, factory, xmlWriter);
				}
			};
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(MY_QNAME, factory, dataSource);

		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException,
				org.apache.axis2.databinding.ADBException {
			serialize(parentQName, factory, xmlWriter, false);
		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter, final boolean serializeType)
				throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

			// We can safely assume an element has only one type associated with it

			if (localResultVariableCandidatesInput == null) { throw new org.apache.axis2.databinding.ADBException("Property cannot be null!"); }
			localResultVariableCandidatesInput.serialize(MY_QNAME, factory, xmlWriter);

		}

		/**
		 * Util method to write an attribute with the ns prefix
		 */
		private void writeAttribute(final java.lang.String prefix, final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (xmlWriter.getPrefix(namespace) == null) {
				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);

			}

			xmlWriter.writeAttribute(namespace, attName, attValue);

		}

		/**
		 * Util method to write an attribute without the ns prefix
		 */
		private void writeAttribute(final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (namespace.equals("")) {
				xmlWriter.writeAttribute(attName, attValue);
			} else {
				registerPrefix(xmlWriter, namespace);
				xmlWriter.writeAttribute(namespace, attName, attValue);
			}
		}

		/**
		 * Util method to write an attribute without the ns prefix
		 */
		private void writeQNameAttribute(final java.lang.String namespace, final java.lang.String attName, final javax.xml.namespace.QName qname,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

			final java.lang.String attributeNamespace = qname.getNamespaceURI();
			java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
			if (attributePrefix == null) {
				attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
			}
			java.lang.String attributeValue;
			if (attributePrefix.trim().length() > 0) {
				attributeValue = attributePrefix + ":" + qname.getLocalPart();
			} else {
				attributeValue = qname.getLocalPart();
			}

			if (namespace.equals("")) {
				xmlWriter.writeAttribute(attName, attributeValue);
			} else {
				registerPrefix(xmlWriter, namespace);
				xmlWriter.writeAttribute(namespace, attName, attributeValue);
			}
		}

		/**
		 * method to handle Qnames
		 */

		private void writeQName(final javax.xml.namespace.QName qname, final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			final java.lang.String namespaceURI = qname.getNamespaceURI();
			if (namespaceURI != null) {
				java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
				if (prefix == null) {
					prefix = generatePrefix(namespaceURI);
					xmlWriter.writeNamespace(prefix, namespaceURI);
					xmlWriter.setPrefix(prefix, namespaceURI);
				}

				if (prefix.trim().length() > 0) {
					xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
				} else {
					// i.e this is the default namespace
					xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
				}

			} else {
				xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
			}
		}

		private void writeQNames(final javax.xml.namespace.QName[] qnames, final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

			if (qnames != null) {
				// we have to store this data until last moment since it is not possible to write any
				// namespace data after writing the charactor data
				final java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
				java.lang.String namespaceURI = null;
				java.lang.String prefix = null;

				for (int i = 0; i < qnames.length; i++) {
					if (i > 0) {
						stringToWrite.append(" ");
					}
					namespaceURI = qnames[i].getNamespaceURI();
					if (namespaceURI != null) {
						prefix = xmlWriter.getPrefix(namespaceURI);
						if ((prefix == null) || (prefix.length() == 0)) {
							prefix = generatePrefix(namespaceURI);
							xmlWriter.writeNamespace(prefix, namespaceURI);
							xmlWriter.setPrefix(prefix, namespaceURI);
						}

						if (prefix.trim().length() > 0) {
							stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
						} else {
							stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
						}
					} else {
						stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
					}
				}
				xmlWriter.writeCharacters(stringToWrite.toString());
			}

		}

		/**
		 * Register a namespace prefix
		 */
		private java.lang.String registerPrefix(final javax.xml.stream.XMLStreamWriter xmlWriter, final java.lang.String namespace)
				throws javax.xml.stream.XMLStreamException {
			java.lang.String prefix = xmlWriter.getPrefix(namespace);

			if (prefix == null) {
				prefix = generatePrefix(namespace);

				while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
					prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
				}

				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);
			}

			return prefix;
		}

		/**
		 * databinding method to get an XML representation of this object
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(final javax.xml.namespace.QName qName) throws org.apache.axis2.databinding.ADBException {

			// We can safely assume an element has only one type associated with it
			return localResultVariableCandidatesInput.getPullParser(MY_QNAME);

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening
			 * reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
			 * Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the
			 * end element of its outer element
			 */
			public static ResultVariableCandidatesInput parse(final javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
				final ResultVariableCandidatesInput object = new ResultVariableCandidatesInput();

				final int event;
				final java.lang.String nillableValue = null;
				final java.lang.String prefix = "";
				final java.lang.String namespaceuri = "";
				try {

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					// Note all attributes that were handled. Used to differ normal attributes
					// from anyAttributes.
					final java.util.Vector handledAttributes = new java.util.Vector();

					while (!reader.isEndElement()) {
						if (reader.isStartElement()) {

							if (reader.isStartElement()
									&& new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/", "resultVariableCandidatesInput").equals(reader
											.getName())) {

								object.setResultVariableCandidatesInput(ContentPackage.Factory.parse(reader));

							} // End of if for expected property start element

							else {
								// A start element we are not expecting indicates an invalid parameter was passed
								throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
							}

						} else {
							reader.next();
						}
					} // end of while loop

				} catch (final javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(final org.apache.axiom.soap.SOAPFactory factory,
			final de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ResultVariableCandidatesInput param, final boolean optimizeContent)
			throws org.apache.axis2.AxisFault {

		try {

			final org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
			emptyEnvelope.getBody().addChild(
					param.getOMElement(de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ResultVariableCandidatesInput.MY_QNAME, factory));
			return emptyEnvelope;
		} catch (final org.apache.axis2.databinding.ADBException e) {
			throw org.apache.axis2.AxisFault.makeFault(e);
		}

	}

	public static class ContentPackage implements org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/", "contentPackage", "ns1");

		private static java.lang.String generatePrefix(final java.lang.String namespace) {
			if (namespace.equals("http://localhost:8080/axis2_tester/services/")) { return "ns1"; }
			return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
		}

		/**
		 * field for ContentPackage
		 */

		protected javax.activation.DataHandler localContentPackage;

		/**
		 * Auto generated getter method
		 * 
		 * @return javax.activation.DataHandler
		 */
		public javax.activation.DataHandler getContentPackage() {
			return localContentPackage;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param ContentPackage
		 */
		public void setContentPackage(final javax.activation.DataHandler param) {

			this.localContentPackage = param;

		}

		public java.lang.String toString() {

			return localContentPackage.toString();

		}

		/**
		 * isReaderMTOMAware
		 * 
		 * @return true if the reader supports MTOM
		 */
		public static boolean isReaderMTOMAware(final javax.xml.stream.XMLStreamReader reader) {
			boolean isReaderMTOMAware = false;

			try {
				isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
			} catch (final java.lang.IllegalArgumentException e) {
				isReaderMTOMAware = false;
			}
			return isReaderMTOMAware;
		}

		/**
		 * @param parentQName
		 * @param factory
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getOMElement(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory)
				throws org.apache.axis2.databinding.ADBException {

			final org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this, MY_QNAME) {

				public void serialize(final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
					ContentPackage.this.serialize(MY_QNAME, factory, xmlWriter);
				}
			};
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(MY_QNAME, factory, dataSource);

		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException,
				org.apache.axis2.databinding.ADBException {
			serialize(parentQName, factory, xmlWriter, false);
		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter, final boolean serializeType)
				throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

			// We can safely assume an element has only one type associated with it

			final java.lang.String namespace = parentQName.getNamespaceURI();
			final java.lang.String localName = parentQName.getLocalPart();

			if (!namespace.equals("")) {
				java.lang.String prefix = xmlWriter.getPrefix(namespace);

				if (prefix == null) {
					prefix = generatePrefix(namespace);

					xmlWriter.writeStartElement(prefix, localName, namespace);
					xmlWriter.writeNamespace(prefix, namespace);
					xmlWriter.setPrefix(prefix, namespace);

				} else {
					xmlWriter.writeStartElement(namespace, localName);
				}

			} else {
				xmlWriter.writeStartElement(localName);
			}

			// add the type details if this is used in a simple type
			if (serializeType) {
				final java.lang.String namespacePrefix = registerPrefix(xmlWriter, "http://localhost:8080/axis2_tester/services/");
				if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)) {
					writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix + ":contentPackage", xmlWriter);
				} else {
					writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "contentPackage", xmlWriter);
				}
			}

			if (localContentPackage == null) {

				throw new org.apache.axis2.databinding.ADBException("Value cannot be null !!");

			} else {

				xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localContentPackage));

			}

			xmlWriter.writeEndElement();

		}

		/**
		 * Util method to write an attribute with the ns prefix
		 */
		private void writeAttribute(final java.lang.String prefix, final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (xmlWriter.getPrefix(namespace) == null) {
				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);

			}

			xmlWriter.writeAttribute(namespace, attName, attValue);

		}

		/**
		 * Util method to write an attribute without the ns prefix
		 */
		private void writeAttribute(final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (namespace.equals("")) {
				xmlWriter.writeAttribute(attName, attValue);
			} else {
				registerPrefix(xmlWriter, namespace);
				xmlWriter.writeAttribute(namespace, attName, attValue);
			}
		}

		/**
		 * Util method to write an attribute without the ns prefix
		 */
		private void writeQNameAttribute(final java.lang.String namespace, final java.lang.String attName, final javax.xml.namespace.QName qname,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

			final java.lang.String attributeNamespace = qname.getNamespaceURI();
			java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
			if (attributePrefix == null) {
				attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
			}
			java.lang.String attributeValue;
			if (attributePrefix.trim().length() > 0) {
				attributeValue = attributePrefix + ":" + qname.getLocalPart();
			} else {
				attributeValue = qname.getLocalPart();
			}

			if (namespace.equals("")) {
				xmlWriter.writeAttribute(attName, attributeValue);
			} else {
				registerPrefix(xmlWriter, namespace);
				xmlWriter.writeAttribute(namespace, attName, attributeValue);
			}
		}

		/**
		 * method to handle Qnames
		 */

		private void writeQName(final javax.xml.namespace.QName qname, final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			final java.lang.String namespaceURI = qname.getNamespaceURI();
			if (namespaceURI != null) {
				java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
				if (prefix == null) {
					prefix = generatePrefix(namespaceURI);
					xmlWriter.writeNamespace(prefix, namespaceURI);
					xmlWriter.setPrefix(prefix, namespaceURI);
				}

				if (prefix.trim().length() > 0) {
					xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
				} else {
					// i.e this is the default namespace
					xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
				}

			} else {
				xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
			}
		}

		private void writeQNames(final javax.xml.namespace.QName[] qnames, final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

			if (qnames != null) {
				// we have to store this data until last moment since it is not possible to write any
				// namespace data after writing the charactor data
				final java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
				java.lang.String namespaceURI = null;
				java.lang.String prefix = null;

				for (int i = 0; i < qnames.length; i++) {
					if (i > 0) {
						stringToWrite.append(" ");
					}
					namespaceURI = qnames[i].getNamespaceURI();
					if (namespaceURI != null) {
						prefix = xmlWriter.getPrefix(namespaceURI);
						if ((prefix == null) || (prefix.length() == 0)) {
							prefix = generatePrefix(namespaceURI);
							xmlWriter.writeNamespace(prefix, namespaceURI);
							xmlWriter.setPrefix(prefix, namespaceURI);
						}

						if (prefix.trim().length() > 0) {
							stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
						} else {
							stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
						}
					} else {
						stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
					}
				}
				xmlWriter.writeCharacters(stringToWrite.toString());
			}

		}

		/**
		 * Register a namespace prefix
		 */
		private java.lang.String registerPrefix(final javax.xml.stream.XMLStreamWriter xmlWriter, final java.lang.String namespace)
				throws javax.xml.stream.XMLStreamException {
			java.lang.String prefix = xmlWriter.getPrefix(namespace);

			if (prefix == null) {
				prefix = generatePrefix(namespace);

				while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
					prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
				}

				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);
			}

			return prefix;
		}

		/**
		 * databinding method to get an XML representation of this object
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(final javax.xml.namespace.QName qName) throws org.apache.axis2.databinding.ADBException {

			// We can safely assume an element has only one type associated with it
			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(MY_QNAME, new java.lang.Object[] {
					org.apache.axis2.databinding.utils.reader.ADBXMLStreamReader.ELEMENT_TEXT,
					org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localContentPackage) }, null);

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			public static ContentPackage fromString(final java.lang.String value, final java.lang.String namespaceURI) {
				final ContentPackage returnValue = new ContentPackage();

				returnValue.setContentPackage(org.apache.axis2.databinding.utils.ConverterUtil.convertToBase64Binary(value));

				return returnValue;
			}

			public static ContentPackage fromString(final javax.xml.stream.XMLStreamReader xmlStreamReader, final java.lang.String content) {
				if (content.indexOf(":") > -1) {
					final java.lang.String prefix = content.substring(0, content.indexOf(":"));
					final java.lang.String namespaceUri = xmlStreamReader.getNamespaceContext().getNamespaceURI(prefix);
					return ContentPackage.Factory.fromString(content, namespaceUri);
				} else {
					return ContentPackage.Factory.fromString(content, "");
				}
			}

			/**
			 * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening
			 * reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
			 * Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the
			 * end element of its outer element
			 */
			public static ContentPackage parse(final javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
				final ContentPackage object = new ContentPackage();

				final int event;
				final java.lang.String nillableValue = null;
				final java.lang.String prefix = "";
				final java.lang.String namespaceuri = "";
				try {

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					// Note all attributes that were handled. Used to differ normal attributes
					// from anyAttributes.
					final java.util.Vector handledAttributes = new java.util.Vector();

					while (!reader.isEndElement()) {
						if (reader.isStartElement() || reader.hasText()) {

							if (reader.isStartElement() || reader.hasText()) {

								final java.lang.String content = reader.getElementText();

								object.setContentPackage(org.apache.axis2.databinding.utils.ConverterUtil.convertToBase64Binary(content));

							} // End of if for expected property start element

							else {
								// A start element we are not expecting indicates an invalid parameter was passed
								throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
							}

						} else {
							reader.next();
						}
					} // end of while loop

				} catch (final javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	/**
	 * Auto generated method signature
	 * 
	 * @see localhost.axis2_tester.services.Onyx_reporter#disarmSite
	 * @param disarmInputElement
	 */

	public de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.DisarmOutputElement disarmSite(

	final de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.DisarmInputElement disarmInputElement)

	throws java.rmi.RemoteException

	{
		org.apache.axis2.context.MessageContext _messageContext = null;
		try {
			final org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[1].getName());
			_operationClient.getOptions().setAction(SERVICE_PATH + "/disarmSite");
			_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

			addPropertyToOperationClient(_operationClient, org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR, "&");

			// create a message context
			_messageContext = new org.apache.axis2.context.MessageContext();

			// create SOAP envelope with that payload
			org.apache.axiom.soap.SOAPEnvelope env = null;

			env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()), disarmInputElement, optimizeContent(new javax.xml.namespace.QName(
					"http://localhost:8080/axis2_tester/services/", "disarmSite")));

			// adding SOAP soap_headers
			_serviceClient.addHeadersToEnvelope(env);
			// set the message context with that soap envelope
			_messageContext.setEnvelope(env);

			// add the message contxt to the operation client
			_operationClient.addMessageContext(_messageContext);

			// execute the operation client
			_operationClient.execute(true);

			final org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient
					.getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			final org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();

			final java.lang.Object object = fromOM(_returnEnv.getBody().getFirstElement(),
					de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.DisarmOutputElement.class, getEnvelopeNamespaces(_returnEnv));

			return (de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.DisarmOutputElement) object;

		} catch (final org.apache.axis2.AxisFault f) {

			final org.apache.axiom.om.OMElement faultElt = f.getDetail();
			if (faultElt != null) {
				if (faultExceptionNameMap.containsKey(faultElt.getQName())) {
					// make the fault by reflection
					try {
						final java.lang.String exceptionClassName = (java.lang.String) faultExceptionClassNameMap.get(faultElt.getQName());
						final java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
						final java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
						// message class
						final java.lang.String messageClassName = (java.lang.String) faultMessageMap.get(faultElt.getQName());
						final java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
						final java.lang.Object messageObject = fromOM(faultElt, messageClass, null);
						final java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage", new java.lang.Class[] { messageClass });
						m.invoke(ex, new java.lang.Object[] { messageObject });

						throw new java.rmi.RemoteException(ex.getMessage(), ex);
					} catch (final java.lang.ClassCastException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (final java.lang.ClassNotFoundException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (final java.lang.NoSuchMethodException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (final java.lang.reflect.InvocationTargetException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (final java.lang.IllegalAccessException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (final java.lang.InstantiationException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					}
				} else {
					throw f;
				}
			} else {
				throw f;
			}
		} finally {
			_messageContext.getTransportOut().getSender().cleanup(_messageContext);
		}
	}

	/**
	 * Auto generated method signature
	 * 
	 * @see localhost.axis2_tester.services.Onyx_reporter#initiateSite
	 * @param inputPartElement
	 */

	public de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.OutputPartElement initiateSite(

	final de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.InputPartElement inputPartElement)

	throws java.rmi.RemoteException

	{
		org.apache.axis2.context.MessageContext _messageContext = null;
		try {
			final org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[2].getName());
			_operationClient.getOptions().setAction(SERVICE_PATH + "/initiateSite");
			_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

			addPropertyToOperationClient(_operationClient, org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR, "&");

			// create a message context
			_messageContext = new org.apache.axis2.context.MessageContext();

			// create SOAP envelope with that payload
			org.apache.axiom.soap.SOAPEnvelope env = null;

			env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()), inputPartElement, optimizeContent(new javax.xml.namespace.QName(
					"http://localhost:8080/axis2_tester/services/", "initiateSite")));

			// adding SOAP soap_headers
			_serviceClient.addHeadersToEnvelope(env);
			// set the message context with that soap envelope
			_messageContext.setEnvelope(env);

			// add the message contxt to the operation client
			_operationClient.addMessageContext(_messageContext);

			// execute the operation client
			_operationClient.execute(true);

			final org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient
					.getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			final org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();

			final java.lang.Object object = fromOM(_returnEnv.getBody().getFirstElement(),
					de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.OutputPartElement.class, getEnvelopeNamespaces(_returnEnv));

			return (de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.OutputPartElement) object;

		} catch (final org.apache.axis2.AxisFault f) {

			final org.apache.axiom.om.OMElement faultElt = f.getDetail();
			if (faultElt != null) {
				if (faultExceptionNameMap.containsKey(faultElt.getQName())) {
					// make the fault by reflection
					try {
						final java.lang.String exceptionClassName = (java.lang.String) faultExceptionClassNameMap.get(faultElt.getQName());
						final java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
						final java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
						// message class
						final java.lang.String messageClassName = (java.lang.String) faultMessageMap.get(faultElt.getQName());
						final java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
						final java.lang.Object messageObject = fromOM(faultElt, messageClass, null);
						final java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage", new java.lang.Class[] { messageClass });
						m.invoke(ex, new java.lang.Object[] { messageObject });

						throw new java.rmi.RemoteException(ex.getMessage(), ex);
					} catch (final java.lang.ClassCastException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (final java.lang.ClassNotFoundException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (final java.lang.NoSuchMethodException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (final java.lang.reflect.InvocationTargetException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (final java.lang.IllegalAccessException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (final java.lang.InstantiationException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					}
				} else {
					throw f;
				}
			} else {
				throw f;
			}
		} finally {
			_messageContext.getTransportOut().getSender().cleanup(_messageContext);
		}
	}

	/**
	 * Auto generated method signature
	 * 
	 * @see localhost.axis2_tester.services.Onyx_reporter#armSite
	 * @param armInputElement
	 */

	public de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ArmOutputElement armSite(

	final de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ArmInputElement armInputElement)

	throws java.rmi.RemoteException

	{
		org.apache.axis2.context.MessageContext _messageContext = null;
		try {
			final org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[3].getName());
			_operationClient.getOptions().setAction(SERVICE_PATH + "/armSite");
			_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

			addPropertyToOperationClient(_operationClient, org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR, "&");

			// create a message context
			_messageContext = new org.apache.axis2.context.MessageContext();

			// create SOAP envelope with that payload
			org.apache.axiom.soap.SOAPEnvelope env = null;

			env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()), armInputElement, optimizeContent(new javax.xml.namespace.QName(
					"http://localhost:8080/axis2_tester/services/", "armSite")));

			// adding SOAP soap_headers
			_serviceClient.addHeadersToEnvelope(env);
			// set the message context with that soap envelope
			_messageContext.setEnvelope(env);

			// add the message contxt to the operation client
			_operationClient.addMessageContext(_messageContext);

			// execute the operation client
			_operationClient.execute(true);

			final org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient
					.getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			final org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();

			final java.lang.Object object = fromOM(_returnEnv.getBody().getFirstElement(),
					de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ArmOutputElement.class, getEnvelopeNamespaces(_returnEnv));

			return (de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ArmOutputElement) object;

		} catch (final org.apache.axis2.AxisFault f) {

			final org.apache.axiom.om.OMElement faultElt = f.getDetail();
			if (faultElt != null) {
				if (faultExceptionNameMap.containsKey(faultElt.getQName())) {
					// make the fault by reflection
					try {
						final java.lang.String exceptionClassName = (java.lang.String) faultExceptionClassNameMap.get(faultElt.getQName());
						final java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
						final java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
						// message class
						final java.lang.String messageClassName = (java.lang.String) faultMessageMap.get(faultElt.getQName());
						final java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
						final java.lang.Object messageObject = fromOM(faultElt, messageClass, null);
						final java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage", new java.lang.Class[] { messageClass });
						m.invoke(ex, new java.lang.Object[] { messageObject });

						throw new java.rmi.RemoteException(ex.getMessage(), ex);
					} catch (final java.lang.ClassCastException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (final java.lang.ClassNotFoundException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (final java.lang.NoSuchMethodException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (final java.lang.reflect.InvocationTargetException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (final java.lang.IllegalAccessException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (final java.lang.InstantiationException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					}
				} else {
					throw f;
				}
			} else {
				throw f;
			}
		} finally {
			_messageContext.getTransportOut().getSender().cleanup(_messageContext);
		}
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

	private boolean optimizeContent(final javax.xml.namespace.QName opName) {

		return false;

	}

	// http://localhost:8989/qti_renderer_servlet/services/onyx_reporter/
	public static class UserDataPayload implements org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name = userDataPayload Namespace URI = http://localhost:8080/axis2_tester/services/ Namespace Prefix
		 * = ns1
		 */

		private static java.lang.String generatePrefix(final java.lang.String namespace) {
			if (namespace.equals("http://localhost:8080/axis2_tester/services/")) { return "ns1"; }
			return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
		}

		/**
		 * field for UserSessionId
		 */

		protected java.lang.String localUserSessionId;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getUserSessionId() {
			return localUserSessionId;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param UserSessionId
		 */
		public void setUserSessionId(final java.lang.String param) {

			this.localUserSessionId = param;

		}

		/**
		 * field for SharedSecret
		 */

		protected java.lang.String localSharedSecret;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getSharedSecret() {
			return localSharedSecret;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param SharedSecret
		 */
		public void setSharedSecret(final java.lang.String param) {

			this.localSharedSecret = param;

		}

		/**
		 * isReaderMTOMAware
		 * 
		 * @return true if the reader supports MTOM
		 */
		public static boolean isReaderMTOMAware(final javax.xml.stream.XMLStreamReader reader) {
			boolean isReaderMTOMAware = false;

			try {
				isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
			} catch (final java.lang.IllegalArgumentException e) {
				isReaderMTOMAware = false;
			}
			return isReaderMTOMAware;
		}

		/**
		 * @param parentQName
		 * @param factory
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getOMElement(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory)
				throws org.apache.axis2.databinding.ADBException {

			final org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this, parentQName) {

				public void serialize(final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
					UserDataPayload.this.serialize(parentQName, factory, xmlWriter);
				}
			};
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(parentQName, factory, dataSource);

		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException,
				org.apache.axis2.databinding.ADBException {
			serialize(parentQName, factory, xmlWriter, false);
		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter, final boolean serializeType)
				throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

			java.lang.String prefix = null;
			java.lang.String namespace = null;

			prefix = parentQName.getPrefix();
			namespace = parentQName.getNamespaceURI();

			if ((namespace != null) && (namespace.trim().length() > 0)) {
				final java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
				if (writerPrefix != null) {
					xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
				} else {
					if (prefix == null) {
						prefix = generatePrefix(namespace);
					}

					xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
					xmlWriter.writeNamespace(prefix, namespace);
					xmlWriter.setPrefix(prefix, namespace);
				}
			} else {
				xmlWriter.writeStartElement(parentQName.getLocalPart());
			}

			if (serializeType) {

				final java.lang.String namespacePrefix = registerPrefix(xmlWriter, "http://localhost:8080/axis2_tester/services/");
				if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)) {
					writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix + ":userDataPayload", xmlWriter);
				} else {
					writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "userDataPayload", xmlWriter);
				}

			}

			namespace = "";
			if (!namespace.equals("")) {
				prefix = xmlWriter.getPrefix(namespace);

				if (prefix == null) {
					prefix = generatePrefix(namespace);

					xmlWriter.writeStartElement(prefix, "userSessionId", namespace);
					xmlWriter.writeNamespace(prefix, namespace);
					xmlWriter.setPrefix(prefix, namespace);

				} else {
					xmlWriter.writeStartElement(namespace, "userSessionId");
				}

			} else {
				xmlWriter.writeStartElement("userSessionId");
			}

			if (localUserSessionId == null) {
				// write the nil attribute

				throw new org.apache.axis2.databinding.ADBException("userSessionId cannot be null!!");

			} else {

				xmlWriter.writeCharacters(localUserSessionId);

			}

			xmlWriter.writeEndElement();

			namespace = "";
			if (!namespace.equals("")) {
				prefix = xmlWriter.getPrefix(namespace);

				if (prefix == null) {
					prefix = generatePrefix(namespace);

					xmlWriter.writeStartElement(prefix, "sharedSecret", namespace);
					xmlWriter.writeNamespace(prefix, namespace);
					xmlWriter.setPrefix(prefix, namespace);

				} else {
					xmlWriter.writeStartElement(namespace, "sharedSecret");
				}

			} else {
				xmlWriter.writeStartElement("sharedSecret");
			}

			if (localSharedSecret == null) {
				// write the nil attribute

				throw new org.apache.axis2.databinding.ADBException("sharedSecret cannot be null!!");

			} else {

				xmlWriter.writeCharacters(localSharedSecret);

			}

			xmlWriter.writeEndElement();

			xmlWriter.writeEndElement();

		}

		/**
		 * Util method to write an attribute with the ns prefix
		 */
		private void writeAttribute(final java.lang.String prefix, final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (xmlWriter.getPrefix(namespace) == null) {
				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);

			}

			xmlWriter.writeAttribute(namespace, attName, attValue);

		}

		/**
		 * Register a namespace prefix
		 */
		private java.lang.String registerPrefix(final javax.xml.stream.XMLStreamWriter xmlWriter, final java.lang.String namespace)
				throws javax.xml.stream.XMLStreamException {
			java.lang.String prefix = xmlWriter.getPrefix(namespace);

			if (prefix == null) {
				prefix = generatePrefix(namespace);

				while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
					prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
				}

				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);
			}

			return prefix;
		}

		/**
		 * databinding method to get an XML representation of this object
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(final javax.xml.namespace.QName qName) throws org.apache.axis2.databinding.ADBException {

			final java.util.ArrayList elementList = new java.util.ArrayList();
			final java.util.ArrayList attribList = new java.util.ArrayList();

			elementList.add(new javax.xml.namespace.QName("", "userSessionId"));

			if (localUserSessionId != null) {
				elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUserSessionId));
			} else {
				throw new org.apache.axis2.databinding.ADBException("userSessionId cannot be null!!");
			}

			elementList.add(new javax.xml.namespace.QName("", "sharedSecret"));

			if (localSharedSecret != null) {
				elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSharedSecret));
			} else {
				throw new org.apache.axis2.databinding.ADBException("sharedSecret cannot be null!!");
			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening
			 * reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
			 * Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the
			 * end element of its outer element
			 */
			public static UserDataPayload parse(final javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
				final UserDataPayload object = new UserDataPayload();

				final int event;
				final java.lang.String nillableValue = null;
				final java.lang.String prefix = "";
				final java.lang.String namespaceuri = "";
				try {

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type") != null) {
						final java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = null;
							if (fullTypeName.indexOf(":") > -1) {
								nsPrefix = fullTypeName.substring(0, fullTypeName.indexOf(":"));
							}
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							final java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":") + 1);

							if (!"userDataPayload".equals(type)) {
								// find namespace for the prefix
								final java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
								return (UserDataPayload) ExtensionMapper.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ normal attributes
					// from anyAttributes.

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "userSessionId").equals(reader.getName())) {

						final java.lang.String content = reader.getElementText();

						object.setUserSessionId(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an invalid parameter was passed
						throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "sharedSecret").equals(reader.getName())) {

						final java.lang.String content = reader.getElementText();

						object.setSharedSecret(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an invalid parameter was passed
						throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement()) {
						// A start element we are not expecting indicates a trailing invalid property
						throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
					}

				} catch (final javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class OutputPartElement implements org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/", "outputPartElement", "ns1");

		private static java.lang.String generatePrefix(final java.lang.String namespace) {
			if (namespace.equals("http://localhost:8080/axis2_tester/services/")) { return "ns1"; }
			return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
		}

		/**
		 * field for OutputPartElement
		 */

		protected ReturnLinkPayload localOutputPartElement;

		/**
		 * Auto generated getter method
		 * 
		 * @return ReturnLinkPayload
		 */
		public ReturnLinkPayload getOutputPartElement() {
			return localOutputPartElement;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param OutputPartElement
		 */
		public void setOutputPartElement(final ReturnLinkPayload param) {

			this.localOutputPartElement = param;

		}

		/**
		 * isReaderMTOMAware
		 * 
		 * @return true if the reader supports MTOM
		 */
		public static boolean isReaderMTOMAware(final javax.xml.stream.XMLStreamReader reader) {
			boolean isReaderMTOMAware = false;

			try {
				isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
			} catch (final java.lang.IllegalArgumentException e) {
				isReaderMTOMAware = false;
			}
			return isReaderMTOMAware;
		}

		/**
		 * @param parentQName
		 * @param factory
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getOMElement(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory)
				throws org.apache.axis2.databinding.ADBException {

			final org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this, MY_QNAME) {

				public void serialize(final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
					OutputPartElement.this.serialize(MY_QNAME, factory, xmlWriter);
				}
			};
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(MY_QNAME, factory, dataSource);

		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException,
				org.apache.axis2.databinding.ADBException {
			serialize(parentQName, factory, xmlWriter, false);
		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter, final boolean serializeType)
				throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

			// We can safely assume an element has only one type associated with it

			if (localOutputPartElement == null) { throw new org.apache.axis2.databinding.ADBException("Property cannot be null!"); }
			localOutputPartElement.serialize(MY_QNAME, factory, xmlWriter);

		}

		/**
		 * Util method to write an attribute with the ns prefix
		 */
		private void writeAttribute(final java.lang.String prefix, final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (xmlWriter.getPrefix(namespace) == null) {
				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);

			}

			xmlWriter.writeAttribute(namespace, attName, attValue);

		}

		/**
		 * Util method to write an attribute without the ns prefix
		 */
		private void writeAttribute(final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (namespace.equals("")) {
				xmlWriter.writeAttribute(attName, attValue);
			} else {
				registerPrefix(xmlWriter, namespace);
				xmlWriter.writeAttribute(namespace, attName, attValue);
			}
		}

		/**
		 * Util method to write an attribute without the ns prefix
		 */
		private void writeQNameAttribute(final java.lang.String namespace, final java.lang.String attName, final javax.xml.namespace.QName qname,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

			final java.lang.String attributeNamespace = qname.getNamespaceURI();
			java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
			if (attributePrefix == null) {
				attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
			}
			java.lang.String attributeValue;
			if (attributePrefix.trim().length() > 0) {
				attributeValue = attributePrefix + ":" + qname.getLocalPart();
			} else {
				attributeValue = qname.getLocalPart();
			}

			if (namespace.equals("")) {
				xmlWriter.writeAttribute(attName, attributeValue);
			} else {
				registerPrefix(xmlWriter, namespace);
				xmlWriter.writeAttribute(namespace, attName, attributeValue);
			}
		}

		/**
		 * method to handle Qnames
		 */

		private void writeQName(final javax.xml.namespace.QName qname, final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			final java.lang.String namespaceURI = qname.getNamespaceURI();
			if (namespaceURI != null) {
				java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
				if (prefix == null) {
					prefix = generatePrefix(namespaceURI);
					xmlWriter.writeNamespace(prefix, namespaceURI);
					xmlWriter.setPrefix(prefix, namespaceURI);
				}

				if (prefix.trim().length() > 0) {
					xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
				} else {
					// i.e this is the default namespace
					xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
				}

			} else {
				xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
			}
		}

		private void writeQNames(final javax.xml.namespace.QName[] qnames, final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

			if (qnames != null) {
				// we have to store this data until last moment since it is not possible to write any
				// namespace data after writing the charactor data
				final java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
				java.lang.String namespaceURI = null;
				java.lang.String prefix = null;

				for (int i = 0; i < qnames.length; i++) {
					if (i > 0) {
						stringToWrite.append(" ");
					}
					namespaceURI = qnames[i].getNamespaceURI();
					if (namespaceURI != null) {
						prefix = xmlWriter.getPrefix(namespaceURI);
						if ((prefix == null) || (prefix.length() == 0)) {
							prefix = generatePrefix(namespaceURI);
							xmlWriter.writeNamespace(prefix, namespaceURI);
							xmlWriter.setPrefix(prefix, namespaceURI);
						}

						if (prefix.trim().length() > 0) {
							stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
						} else {
							stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
						}
					} else {
						stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
					}
				}
				xmlWriter.writeCharacters(stringToWrite.toString());
			}

		}

		/**
		 * Register a namespace prefix
		 */
		private java.lang.String registerPrefix(final javax.xml.stream.XMLStreamWriter xmlWriter, final java.lang.String namespace)
				throws javax.xml.stream.XMLStreamException {
			java.lang.String prefix = xmlWriter.getPrefix(namespace);

			if (prefix == null) {
				prefix = generatePrefix(namespace);

				while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
					prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
				}

				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);
			}

			return prefix;
		}

		/**
		 * databinding method to get an XML representation of this object
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(final javax.xml.namespace.QName qName) throws org.apache.axis2.databinding.ADBException {

			// We can safely assume an element has only one type associated with it
			return localOutputPartElement.getPullParser(MY_QNAME);

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening
			 * reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
			 * Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the
			 * end element of its outer element
			 */
			public static OutputPartElement parse(final javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
				final OutputPartElement object = new OutputPartElement();

				final int event;
				final java.lang.String nillableValue = null;
				final java.lang.String prefix = "";
				final java.lang.String namespaceuri = "";
				try {

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					// Note all attributes that were handled. Used to differ normal attributes
					// from anyAttributes.

					while (!reader.isEndElement()) {
						if (reader.isStartElement()) {

							if (reader.isStartElement()
									&& new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/", "outputPartElement").equals(reader.getName())) {

								object.setOutputPartElement(ReturnLinkPayload.Factory.parse(reader));

							} // End of if for expected property start element

							else {
								// A start element we are not expecting indicates an invalid parameter was passed
								throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
							}

						} else {
							reader.next();
						}
					} // end of while loop

				} catch (final javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class PostGet_type0 implements org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName("", "postGet_type0", "");

		private static java.lang.String generatePrefix(final java.lang.String namespace) {
			if (namespace.equals("")) { return ""; }
			return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
		}

		/**
		 * field for PostGet_type0
		 */

		protected java.lang.String localPostGet_type0;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getPostGet_type0() {
			return localPostGet_type0;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param PostGet_type0
		 */
		public void setPostGet_type0(final java.lang.String param) {

			if (org.apache.axis2.databinding.utils.ConverterUtil.convertToString(param).matches("post|get")) {
				this.localPostGet_type0 = param;
			} else {
				throw new java.lang.RuntimeException();
			}

		}

		public java.lang.String toString() {

			return localPostGet_type0.toString();

		}

		/**
		 * isReaderMTOMAware
		 * 
		 * @return true if the reader supports MTOM
		 */
		public static boolean isReaderMTOMAware(final javax.xml.stream.XMLStreamReader reader) {
			boolean isReaderMTOMAware = false;

			try {
				isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
			} catch (final java.lang.IllegalArgumentException e) {
				isReaderMTOMAware = false;
			}
			return isReaderMTOMAware;
		}

		/**
		 * @param parentQName
		 * @param factory
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getOMElement(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory)
				throws org.apache.axis2.databinding.ADBException {

			final org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this, MY_QNAME) {

				public void serialize(final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
					PostGet_type0.this.serialize(MY_QNAME, factory, xmlWriter);
				}
			};
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(MY_QNAME, factory, dataSource);

		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException,
				org.apache.axis2.databinding.ADBException {
			serialize(parentQName, factory, xmlWriter, false);
		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter, final boolean serializeType)
				throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

			// We can safely assume an element has only one type associated with it

			final java.lang.String namespace = parentQName.getNamespaceURI();
			final java.lang.String localName = parentQName.getLocalPart();

			if (!namespace.equals("")) {
				java.lang.String prefix = xmlWriter.getPrefix(namespace);

				if (prefix == null) {
					prefix = generatePrefix(namespace);

					xmlWriter.writeStartElement(prefix, localName, namespace);
					xmlWriter.writeNamespace(prefix, namespace);
					xmlWriter.setPrefix(prefix, namespace);

				} else {
					xmlWriter.writeStartElement(namespace, localName);
				}

			} else {
				xmlWriter.writeStartElement(localName);
			}

			// add the type details if this is used in a simple type
			if (serializeType) {
				final java.lang.String namespacePrefix = registerPrefix(xmlWriter, "");
				if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)) {
					writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix + ":postGet_type0", xmlWriter);
				} else {
					writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "postGet_type0", xmlWriter);
				}
			}

			if (localPostGet_type0 == null) {

				throw new org.apache.axis2.databinding.ADBException("Value cannot be null !!");

			} else {

				xmlWriter.writeCharacters(localPostGet_type0);

			}

			xmlWriter.writeEndElement();

		}

		/**
		 * Util method to write an attribute with the ns prefix
		 */
		private void writeAttribute(final java.lang.String prefix, final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (xmlWriter.getPrefix(namespace) == null) {
				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);

			}

			xmlWriter.writeAttribute(namespace, attName, attValue);

		}

		/**
		 * Register a namespace prefix
		 */
		private java.lang.String registerPrefix(final javax.xml.stream.XMLStreamWriter xmlWriter, final java.lang.String namespace)
				throws javax.xml.stream.XMLStreamException {
			java.lang.String prefix = xmlWriter.getPrefix(namespace);

			if (prefix == null) {
				prefix = generatePrefix(namespace);

				while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
					prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
				}

				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);
			}

			return prefix;
		}

		/**
		 * databinding method to get an XML representation of this object
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(final javax.xml.namespace.QName qName) throws org.apache.axis2.databinding.ADBException {

			// We can safely assume an element has only one type associated with it
			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(MY_QNAME, new java.lang.Object[] {
					org.apache.axis2.databinding.utils.reader.ADBXMLStreamReader.ELEMENT_TEXT,
					org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPostGet_type0) }, null);

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			public static PostGet_type0 fromString(final java.lang.String value, final java.lang.String namespaceURI) {
				final PostGet_type0 returnValue = new PostGet_type0();

				returnValue.setPostGet_type0(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(value));

				return returnValue;
			}

			public static PostGet_type0 fromString(final javax.xml.stream.XMLStreamReader xmlStreamReader, final java.lang.String content) {
				if (content.indexOf(":") > -1) {
					final java.lang.String prefix = content.substring(0, content.indexOf(":"));
					final java.lang.String namespaceUri = xmlStreamReader.getNamespaceContext().getNamespaceURI(prefix);
					return PostGet_type0.Factory.fromString(content, namespaceUri);
				} else {
					return PostGet_type0.Factory.fromString(content, "");
				}
			}

			/**
			 * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening
			 * reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
			 * Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the
			 * end element of its outer element
			 */
			public static PostGet_type0 parse(final javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
				final PostGet_type0 object = new PostGet_type0();

				final int event;
				final java.lang.String nillableValue = null;
				final java.lang.String prefix = "";
				final java.lang.String namespaceuri = "";
				try {

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					// Note all attributes that were handled. Used to differ normal attributes
					// from anyAttributes.

					while (!reader.isEndElement()) {
						if (reader.isStartElement() || reader.hasText()) {

							if (reader.isStartElement() || reader.hasText()) {

								final java.lang.String content = reader.getElementText();

								object.setPostGet_type0(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

							} // End of if for expected property start element

							else {
								// A start element we are not expecting indicates an invalid parameter was passed
								throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
							}

						} else {
							reader.next();
						}
					} // end of while loop

				} catch (final javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class ArmSitePayload implements org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name = armSitePayload Namespace URI = http://localhost:8080/axis2_tester/services/ Namespace Prefix =
		 * ns1
		 */

		private static java.lang.String generatePrefix(final java.lang.String namespace) {
			if (namespace.equals("http://localhost:8080/axis2_tester/services/")) { return "ns1"; }
			return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
		}

		/**
		 * field for UserId
		 */

		protected java.lang.String localUserId;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getUserId() {
			return localUserId;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param UserId
		 */
		public void setUserId(final java.lang.String param) {

			this.localUserId = param;

		}

		/**
		 * field for Role
		 */

		protected java.lang.String localRole;

		/*
		 * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this
		 * field in the serialized XML
		 */
		protected boolean localRoleTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getRole() {
			return localRole;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param Role
		 */
		public void setRole(final java.lang.String param) {

			if (param != null) {
				// update the setting tracker
				localRoleTracker = true;
			} else {
				localRoleTracker = false;

			}

			this.localRole = param;

		}

		/**
		 * field for SecretToShare
		 */

		protected java.lang.String localSecretToShare;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getSecretToShare() {
			return localSecretToShare;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param SecretToShare
		 */
		public void setSecretToShare(final java.lang.String param) {

			this.localSecretToShare = param;

		}

		/**
		 * field for UserLastName
		 */

		protected java.lang.String localUserLastName;

		/*
		 * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this
		 * field in the serialized XML
		 */
		protected boolean localUserLastNameTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getUserLastName() {
			return localUserLastName;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param UserLastName
		 */
		public void setUserLastName(final java.lang.String param) {

			if (param != null) {
				// update the setting tracker
				localUserLastNameTracker = true;
			} else {
				localUserLastNameTracker = false;

			}

			this.localUserLastName = param;

		}

		/**
		 * field for UserFirstName
		 */

		protected java.lang.String localUserFirstName;

		/*
		 * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this
		 * field in the serialized XML
		 */
		protected boolean localUserFirstNameTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getUserFirstName() {
			return localUserFirstName;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param UserFirstName
		 */
		public void setUserFirstName(final java.lang.String param) {

			if (param != null) {
				// update the setting tracker
				localUserFirstNameTracker = true;
			} else {
				localUserFirstNameTracker = false;

			}

			this.localUserFirstName = param;

		}

		/**
		 * isReaderMTOMAware
		 * 
		 * @return true if the reader supports MTOM
		 */
		public static boolean isReaderMTOMAware(final javax.xml.stream.XMLStreamReader reader) {
			boolean isReaderMTOMAware = false;

			try {
				isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
			} catch (final java.lang.IllegalArgumentException e) {
				isReaderMTOMAware = false;
			}
			return isReaderMTOMAware;
		}

		/**
		 * @param parentQName
		 * @param factory
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getOMElement(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory)
				throws org.apache.axis2.databinding.ADBException {

			final org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this, parentQName) {

				public void serialize(final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
					ArmSitePayload.this.serialize(parentQName, factory, xmlWriter);
				}
			};
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(parentQName, factory, dataSource);

		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException,
				org.apache.axis2.databinding.ADBException {
			serialize(parentQName, factory, xmlWriter, false);
		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter, final boolean serializeType)
				throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

			java.lang.String prefix = null;
			java.lang.String namespace = null;

			prefix = parentQName.getPrefix();
			namespace = parentQName.getNamespaceURI();

			if ((namespace != null) && (namespace.trim().length() > 0)) {
				final java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
				if (writerPrefix != null) {
					xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
				} else {
					if (prefix == null) {
						prefix = generatePrefix(namespace);
					}

					xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
					xmlWriter.writeNamespace(prefix, namespace);
					xmlWriter.setPrefix(prefix, namespace);
				}
			} else {
				xmlWriter.writeStartElement(parentQName.getLocalPart());
			}

			if (serializeType) {

				final java.lang.String namespacePrefix = registerPrefix(xmlWriter, "http://localhost:8080/axis2_tester/services/");
				if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)) {
					writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix + ":armSitePayload", xmlWriter);
				} else {
					writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "armSitePayload", xmlWriter);
				}

			}

			namespace = "";
			if (!namespace.equals("")) {
				prefix = xmlWriter.getPrefix(namespace);

				if (prefix == null) {
					prefix = generatePrefix(namespace);

					xmlWriter.writeStartElement(prefix, "userId", namespace);
					xmlWriter.writeNamespace(prefix, namespace);
					xmlWriter.setPrefix(prefix, namespace);

				} else {
					xmlWriter.writeStartElement(namespace, "userId");
				}

			} else {
				xmlWriter.writeStartElement("userId");
			}

			if (localUserId == null) {
				// write the nil attribute

				throw new org.apache.axis2.databinding.ADBException("userId cannot be null!!");

			} else {

				xmlWriter.writeCharacters(localUserId);

			}

			xmlWriter.writeEndElement();
			if (localRoleTracker) {
				namespace = "";
				if (!namespace.equals("")) {
					prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = generatePrefix(namespace);

						xmlWriter.writeStartElement(prefix, "role", namespace);
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					} else {
						xmlWriter.writeStartElement(namespace, "role");
					}

				} else {
					xmlWriter.writeStartElement("role");
				}

				if (localRole == null) {
					// write the nil attribute

					throw new org.apache.axis2.databinding.ADBException("role cannot be null!!");

				} else {

					xmlWriter.writeCharacters(localRole);

				}

				xmlWriter.writeEndElement();
			}
			namespace = "";
			if (!namespace.equals("")) {
				prefix = xmlWriter.getPrefix(namespace);

				if (prefix == null) {
					prefix = generatePrefix(namespace);

					xmlWriter.writeStartElement(prefix, "secretToShare", namespace);
					xmlWriter.writeNamespace(prefix, namespace);
					xmlWriter.setPrefix(prefix, namespace);

				} else {
					xmlWriter.writeStartElement(namespace, "secretToShare");
				}

			} else {
				xmlWriter.writeStartElement("secretToShare");
			}

			if (localSecretToShare == null) {
				// write the nil attribute

				throw new org.apache.axis2.databinding.ADBException("secretToShare cannot be null!!");

			} else {

				xmlWriter.writeCharacters(localSecretToShare);

			}

			xmlWriter.writeEndElement();
			if (localUserLastNameTracker) {
				namespace = "";
				if (!namespace.equals("")) {
					prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = generatePrefix(namespace);

						xmlWriter.writeStartElement(prefix, "userLastName", namespace);
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					} else {
						xmlWriter.writeStartElement(namespace, "userLastName");
					}

				} else {
					xmlWriter.writeStartElement("userLastName");
				}

				if (localUserLastName == null) {
					// write the nil attribute

					throw new org.apache.axis2.databinding.ADBException("userLastName cannot be null!!");

				} else {

					xmlWriter.writeCharacters(localUserLastName);

				}

				xmlWriter.writeEndElement();
			}
			if (localUserFirstNameTracker) {
				namespace = "";
				if (!namespace.equals("")) {
					prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = generatePrefix(namespace);

						xmlWriter.writeStartElement(prefix, "userFirstName", namespace);
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					} else {
						xmlWriter.writeStartElement(namespace, "userFirstName");
					}

				} else {
					xmlWriter.writeStartElement("userFirstName");
				}

				if (localUserFirstName == null) {
					// write the nil attribute

					throw new org.apache.axis2.databinding.ADBException("userFirstName cannot be null!!");

				} else {

					xmlWriter.writeCharacters(localUserFirstName);

				}

				xmlWriter.writeEndElement();
			}
			xmlWriter.writeEndElement();

		}

		/**
		 * Util method to write an attribute with the ns prefix
		 */
		private void writeAttribute(final java.lang.String prefix, final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (xmlWriter.getPrefix(namespace) == null) {
				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);

			}

			xmlWriter.writeAttribute(namespace, attName, attValue);

		}

		/**
		 * Register a namespace prefix
		 */
		private java.lang.String registerPrefix(final javax.xml.stream.XMLStreamWriter xmlWriter, final java.lang.String namespace)
				throws javax.xml.stream.XMLStreamException {
			java.lang.String prefix = xmlWriter.getPrefix(namespace);

			if (prefix == null) {
				prefix = generatePrefix(namespace);

				while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
					prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
				}

				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);
			}

			return prefix;
		}

		/**
		 * databinding method to get an XML representation of this object
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(final javax.xml.namespace.QName qName) throws org.apache.axis2.databinding.ADBException {

			final java.util.ArrayList elementList = new java.util.ArrayList();
			final java.util.ArrayList attribList = new java.util.ArrayList();

			elementList.add(new javax.xml.namespace.QName("", "userId"));

			if (localUserId != null) {
				elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUserId));
			} else {
				throw new org.apache.axis2.databinding.ADBException("userId cannot be null!!");
			}
			if (localRoleTracker) {
				elementList.add(new javax.xml.namespace.QName("", "role"));

				if (localRole != null) {
					elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRole));
				} else {
					throw new org.apache.axis2.databinding.ADBException("role cannot be null!!");
				}
			}
			elementList.add(new javax.xml.namespace.QName("", "secretToShare"));

			if (localSecretToShare != null) {
				elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSecretToShare));
			} else {
				throw new org.apache.axis2.databinding.ADBException("secretToShare cannot be null!!");
			}
			if (localUserLastNameTracker) {
				elementList.add(new javax.xml.namespace.QName("", "userLastName"));

				if (localUserLastName != null) {
					elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUserLastName));
				} else {
					throw new org.apache.axis2.databinding.ADBException("userLastName cannot be null!!");
				}
			}
			if (localUserFirstNameTracker) {
				elementList.add(new javax.xml.namespace.QName("", "userFirstName"));

				if (localUserFirstName != null) {
					elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUserFirstName));
				} else {
					throw new org.apache.axis2.databinding.ADBException("userFirstName cannot be null!!");
				}
			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening
			 * reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
			 * Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the
			 * end element of its outer element
			 */
			public static ArmSitePayload parse(final javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
				final ArmSitePayload object = new ArmSitePayload();

				final int event;
				final java.lang.String nillableValue = null;
				final java.lang.String prefix = "";
				final java.lang.String namespaceuri = "";
				try {

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type") != null) {
						final java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = null;
							if (fullTypeName.indexOf(":") > -1) {
								nsPrefix = fullTypeName.substring(0, fullTypeName.indexOf(":"));
							}
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							final java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":") + 1);

							if (!"armSitePayload".equals(type)) {
								// find namespace for the prefix
								final java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
								return (ArmSitePayload) ExtensionMapper.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ normal attributes
					// from anyAttributes.

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "userId").equals(reader.getName())) {

						final java.lang.String content = reader.getElementText();

						object.setUserId(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an invalid parameter was passed
						throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "role").equals(reader.getName())) {

						final java.lang.String content = reader.getElementText();

						object.setRole(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					else {

					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "secretToShare").equals(reader.getName())) {

						final java.lang.String content = reader.getElementText();

						object.setSecretToShare(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an invalid parameter was passed
						throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "userLastName").equals(reader.getName())) {

						final java.lang.String content = reader.getElementText();

						object.setUserLastName(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					else {

					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "userFirstName").equals(reader.getName())) {

						final java.lang.String content = reader.getElementText();

						object.setUserFirstName(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					else {

					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement()) {
						// A start element we are not expecting indicates a trailing invalid property
						throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
					}

				} catch (final javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class DisarmOutPutPayload implements org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name = disarmOutPutPayload Namespace URI = http://localhost:8080/axis2_tester/services/ Namespace
		 * Prefix = ns1
		 */

		private static java.lang.String generatePrefix(final java.lang.String namespace) {
			if (namespace.equals("http://localhost:8080/axis2_tester/services/")) { return "ns1"; }
			return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
		}

		/**
		 * field for Successfull
		 */

		protected boolean localSuccessfull;

		/**
		 * Auto generated getter method
		 * 
		 * @return boolean
		 */
		public boolean getSuccessfull() {
			return localSuccessfull;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param Successfull
		 */
		public void setSuccessfull(final boolean param) {

			this.localSuccessfull = param;

		}

		/**
		 * field for AdditionalParameters This was an Array!
		 */

		protected PostGetParams[] localAdditionalParameters;

		/*
		 * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this
		 * field in the serialized XML
		 */
		protected boolean localAdditionalParametersTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return PostGetParams[]
		 */
		public PostGetParams[] getAdditionalParameters() {
			return localAdditionalParameters;
		}

		/**
		 * validate the array for AdditionalParameters
		 */
		protected void validateAdditionalParameters(final PostGetParams[] param) {

		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param AdditionalParameters
		 */
		public void setAdditionalParameters(final PostGetParams[] param) {

			validateAdditionalParameters(param);

			if (param != null) {
				// update the setting tracker
				localAdditionalParametersTracker = true;
			} else {
				localAdditionalParametersTracker = false;

			}

			this.localAdditionalParameters = param;
		}

		/**
		 * Auto generated add method for the array for convenience
		 * 
		 * @param param PostGetParams
		 */
		public void addAdditionalParameters(final PostGetParams param) {
			if (localAdditionalParameters == null) {
				localAdditionalParameters = new PostGetParams[] {};
			}

			// update the setting tracker
			localAdditionalParametersTracker = true;

			final java.util.List list = org.apache.axis2.databinding.utils.ConverterUtil.toList(localAdditionalParameters);
			list.add(param);
			this.localAdditionalParameters = (PostGetParams[]) list.toArray(new PostGetParams[list.size()]);

		}

		/**
		 * isReaderMTOMAware
		 * 
		 * @return true if the reader supports MTOM
		 */
		public static boolean isReaderMTOMAware(final javax.xml.stream.XMLStreamReader reader) {
			boolean isReaderMTOMAware = false;

			try {
				isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
			} catch (final java.lang.IllegalArgumentException e) {
				isReaderMTOMAware = false;
			}
			return isReaderMTOMAware;
		}

		/**
		 * @param parentQName
		 * @param factory
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getOMElement(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory)
				throws org.apache.axis2.databinding.ADBException {

			final org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this, parentQName) {

				public void serialize(final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
					DisarmOutPutPayload.this.serialize(parentQName, factory, xmlWriter);
				}
			};
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(parentQName, factory, dataSource);

		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException,
				org.apache.axis2.databinding.ADBException {
			serialize(parentQName, factory, xmlWriter, false);
		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter, final boolean serializeType)
				throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

			java.lang.String prefix = null;
			java.lang.String namespace = null;

			prefix = parentQName.getPrefix();
			namespace = parentQName.getNamespaceURI();

			if ((namespace != null) && (namespace.trim().length() > 0)) {
				final java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
				if (writerPrefix != null) {
					xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
				} else {
					if (prefix == null) {
						prefix = generatePrefix(namespace);
					}

					xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
					xmlWriter.writeNamespace(prefix, namespace);
					xmlWriter.setPrefix(prefix, namespace);
				}
			} else {
				xmlWriter.writeStartElement(parentQName.getLocalPart());
			}

			if (serializeType) {

				final java.lang.String namespacePrefix = registerPrefix(xmlWriter, "http://localhost:8080/axis2_tester/services/");
				if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)) {
					writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix + ":disarmOutPutPayload", xmlWriter);
				} else {
					writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "disarmOutPutPayload", xmlWriter);
				}

			}

			namespace = "";
			if (!namespace.equals("")) {
				prefix = xmlWriter.getPrefix(namespace);

				if (prefix == null) {
					prefix = generatePrefix(namespace);

					xmlWriter.writeStartElement(prefix, "successfull", namespace);
					xmlWriter.writeNamespace(prefix, namespace);
					xmlWriter.setPrefix(prefix, namespace);

				} else {
					xmlWriter.writeStartElement(namespace, "successfull");
				}

			} else {
				xmlWriter.writeStartElement("successfull");
			}

			if (false) {

				throw new org.apache.axis2.databinding.ADBException("successfull cannot be null!!");

			} else {
				xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSuccessfull));
			}

			xmlWriter.writeEndElement();
			if (localAdditionalParametersTracker) {
				if (localAdditionalParameters != null) {
					for (int i = 0; i < localAdditionalParameters.length; i++) {
						if (localAdditionalParameters[i] != null) {
							localAdditionalParameters[i].serialize(new javax.xml.namespace.QName("", "additionalParameters"), factory, xmlWriter);
						} else {

							// we don't have to do any thing since minOccures is zero

						}

					}
				} else {

					throw new org.apache.axis2.databinding.ADBException("additionalParameters cannot be null!!");

				}
			}
			xmlWriter.writeEndElement();

		}

		/**
		 * Util method to write an attribute with the ns prefix
		 */
		private void writeAttribute(final java.lang.String prefix, final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (xmlWriter.getPrefix(namespace) == null) {
				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);

			}

			xmlWriter.writeAttribute(namespace, attName, attValue);

		}

		/**
		 * Register a namespace prefix
		 */
		private java.lang.String registerPrefix(final javax.xml.stream.XMLStreamWriter xmlWriter, final java.lang.String namespace)
				throws javax.xml.stream.XMLStreamException {
			java.lang.String prefix = xmlWriter.getPrefix(namespace);

			if (prefix == null) {
				prefix = generatePrefix(namespace);

				while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
					prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
				}

				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);
			}

			return prefix;
		}

		/**
		 * databinding method to get an XML representation of this object
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(final javax.xml.namespace.QName qName) throws org.apache.axis2.databinding.ADBException {

			final java.util.ArrayList elementList = new java.util.ArrayList();
			final java.util.ArrayList attribList = new java.util.ArrayList();

			elementList.add(new javax.xml.namespace.QName("", "successfull"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSuccessfull));
			if (localAdditionalParametersTracker) {
				if (localAdditionalParameters != null) {
					for (int i = 0; i < localAdditionalParameters.length; i++) {

						if (localAdditionalParameters[i] != null) {
							elementList.add(new javax.xml.namespace.QName("", "additionalParameters"));
							elementList.add(localAdditionalParameters[i]);
						} else {

							// nothing to do

						}

					}
				} else {

					throw new org.apache.axis2.databinding.ADBException("additionalParameters cannot be null!!");

				}

			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening
			 * reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
			 * Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the
			 * end element of its outer element
			 */
			public static DisarmOutPutPayload parse(final javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
				final DisarmOutPutPayload object = new DisarmOutPutPayload();

				final int event;
				final java.lang.String nillableValue = null;
				final java.lang.String prefix = "";
				final java.lang.String namespaceuri = "";
				try {

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type") != null) {
						final java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = null;
							if (fullTypeName.indexOf(":") > -1) {
								nsPrefix = fullTypeName.substring(0, fullTypeName.indexOf(":"));
							}
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							final java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":") + 1);

							if (!"disarmOutPutPayload".equals(type)) {
								// find namespace for the prefix
								final java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
								return (DisarmOutPutPayload) ExtensionMapper.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ normal attributes
					// from anyAttributes.

					reader.next();

					final java.util.ArrayList list2 = new java.util.ArrayList();

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "successfull").equals(reader.getName())) {

						final java.lang.String content = reader.getElementText();

						object.setSuccessfull(org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an invalid parameter was passed
						throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "additionalParameters").equals(reader.getName())) {

						// Process the array and step past its final element's end.
						list2.add(PostGetParams.Factory.parse(reader));

						// loop until we find a start element that is not part of this array
						boolean loopDone2 = false;
						while (!loopDone2) {
							// We should be at the end element, but make sure
							while (!reader.isEndElement()) {
								reader.next();
							}
							// Step out of this element
							reader.next();
							// Step to next element event.
							while (!reader.isStartElement() && !reader.isEndElement()) {
								reader.next();
							}
							if (reader.isEndElement()) {
								// two continuous end elements means we are exiting the xml structure
								loopDone2 = true;
							} else {
								if (new javax.xml.namespace.QName("", "additionalParameters").equals(reader.getName())) {
									list2.add(PostGetParams.Factory.parse(reader));

								} else {
									loopDone2 = true;
								}
							}
						}
						// call the converter utility to convert and set the array

						object.setAdditionalParameters((PostGetParams[]) org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(PostGetParams.class, list2));

					} // End of if for expected property start element

					else {

					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement()) {
						// A start element we are not expecting indicates a trailing invalid property
						throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
					}

				} catch (final javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class InputPartPayload implements org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name = inputPartPayload Namespace URI = http://localhost:8080/axis2_tester/services/ Namespace Prefix
		 * = ns1
		 */

		private static java.lang.String generatePrefix(final java.lang.String namespace) {
			if (namespace.equals("http://localhost:8080/axis2_tester/services/")) { return "ns1"; }
			return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
		}

		/**
		 * field for ContentPackage
		 */

		protected javax.activation.DataHandler localContentPackage;

		/**
		 * Auto generated getter method
		 * 
		 * @return javax.activation.DataHandler
		 */
		public javax.activation.DataHandler getContentPackage() {
			return localContentPackage;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param ContentPackage
		 */
		public void setContentPackage(final javax.activation.DataHandler param) {

			this.localContentPackage = param;

		}

		/**
		 * field for Students This was an Array!
		 */

		protected Student[] localStudents;

		/**
		 * Auto generated getter method
		 * 
		 * @return Student[]
		 */
		public Student[] getStudents() {
			return localStudents;
		}

		/**
		 * validate the array for Students
		 */
		protected void validateStudents(final Student[] param) {

			if ((param != null) && (param.length < 1)) { throw new java.lang.RuntimeException(); }

		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param Students
		 */
		public void setStudents(final Student[] param) {

			validateStudents(param);

			this.localStudents = param;
		}

		/**
		 * Auto generated add method for the array for convenience
		 * 
		 * @param param Student
		 */
		public void addStudents(final Student param) {
			if (localStudents == null) {
				localStudents = new Student[] {};
			}

			final java.util.List list = org.apache.axis2.databinding.utils.ConverterUtil.toList(localStudents);
			list.add(param);
			this.localStudents = (Student[]) list.toArray(new Student[list.size()]);

		}

		/**
		 * field for UserData
		 */

		protected UserDataPayload localUserData;

		/**
		 * Auto generated getter method
		 * 
		 * @return UserDataPayload
		 */
		public UserDataPayload getUserData() {
			return localUserData;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param UserData
		 */
		public void setUserData(final UserDataPayload param) {

			this.localUserData = param;

		}

		/**
		 * isReaderMTOMAware
		 * 
		 * @return true if the reader supports MTOM
		 */
		public static boolean isReaderMTOMAware(final javax.xml.stream.XMLStreamReader reader) {
			boolean isReaderMTOMAware = false;

			try {
				isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
			} catch (final java.lang.IllegalArgumentException e) {
				isReaderMTOMAware = false;
			}
			return isReaderMTOMAware;
		}

		/**
		 * @param parentQName
		 * @param factory
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getOMElement(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory)
				throws org.apache.axis2.databinding.ADBException {

			final org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this, parentQName) {

				public void serialize(final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
					InputPartPayload.this.serialize(parentQName, factory, xmlWriter);
				}
			};
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(parentQName, factory, dataSource);

		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException,
				org.apache.axis2.databinding.ADBException {
			serialize(parentQName, factory, xmlWriter, false);
		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter, final boolean serializeType)
				throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

			java.lang.String prefix = null;
			java.lang.String namespace = null;

			prefix = parentQName.getPrefix();
			namespace = parentQName.getNamespaceURI();

			if ((namespace != null) && (namespace.trim().length() > 0)) {
				final java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
				if (writerPrefix != null) {
					xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
				} else {
					if (prefix == null) {
						prefix = generatePrefix(namespace);
					}

					xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
					xmlWriter.writeNamespace(prefix, namespace);
					xmlWriter.setPrefix(prefix, namespace);
				}
			} else {
				xmlWriter.writeStartElement(parentQName.getLocalPart());
			}

			if (serializeType) {

				final java.lang.String namespacePrefix = registerPrefix(xmlWriter, "http://localhost:8080/axis2_tester/services/");
				if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)) {
					writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix + ":inputPartPayload", xmlWriter);
				} else {
					writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "inputPartPayload", xmlWriter);
				}

			}

			namespace = "";
			if (!namespace.equals("")) {
				prefix = xmlWriter.getPrefix(namespace);

				if (prefix == null) {
					prefix = generatePrefix(namespace);

					xmlWriter.writeStartElement(prefix, "contentPackage", namespace);
					xmlWriter.writeNamespace(prefix, namespace);
					xmlWriter.setPrefix(prefix, namespace);

				} else {
					xmlWriter.writeStartElement(namespace, "contentPackage");
				}

			} else {
				xmlWriter.writeStartElement("contentPackage");
			}

			if (localContentPackage != null) {
				xmlWriter.writeDataHandler(localContentPackage);
			}

			xmlWriter.writeEndElement();

			if (localStudents != null) {
				for (int i = 0; i < localStudents.length; i++) {
					if (localStudents[i] != null) {
						localStudents[i].serialize(new javax.xml.namespace.QName("", "students"), factory, xmlWriter);
					} else {

						throw new org.apache.axis2.databinding.ADBException("students cannot be null!!");

					}

				}
			} else {

				throw new org.apache.axis2.databinding.ADBException("students cannot be null!!");

			}

			if (localUserData == null) { throw new org.apache.axis2.databinding.ADBException("userData cannot be null!!"); }
			localUserData.serialize(new javax.xml.namespace.QName("", "userData"), factory, xmlWriter);

			xmlWriter.writeEndElement();

		}

		/**
		 * Util method to write an attribute with the ns prefix
		 */
		private void writeAttribute(final java.lang.String prefix, final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (xmlWriter.getPrefix(namespace) == null) {
				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);

			}

			xmlWriter.writeAttribute(namespace, attName, attValue);

		}

		/**
		 * Register a namespace prefix
		 */
		private java.lang.String registerPrefix(final javax.xml.stream.XMLStreamWriter xmlWriter, final java.lang.String namespace)
				throws javax.xml.stream.XMLStreamException {
			java.lang.String prefix = xmlWriter.getPrefix(namespace);

			if (prefix == null) {
				prefix = generatePrefix(namespace);

				while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
					prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
				}

				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);
			}

			return prefix;
		}

		/**
		 * databinding method to get an XML representation of this object
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(final javax.xml.namespace.QName qName) throws org.apache.axis2.databinding.ADBException {

			final java.util.ArrayList elementList = new java.util.ArrayList();
			final java.util.ArrayList attribList = new java.util.ArrayList();

			elementList.add(new javax.xml.namespace.QName("", "contentPackage"));

			elementList.add(localContentPackage);

			if (localStudents != null) {
				for (int i = 0; i < localStudents.length; i++) {

					if (localStudents[i] != null) {
						elementList.add(new javax.xml.namespace.QName("", "students"));
						elementList.add(localStudents[i]);
					} else {

						throw new org.apache.axis2.databinding.ADBException("students cannot be null !!");

					}

				}
			} else {

				throw new org.apache.axis2.databinding.ADBException("students cannot be null!!");

			}

			elementList.add(new javax.xml.namespace.QName("", "userData"));

			if (localUserData == null) { throw new org.apache.axis2.databinding.ADBException("userData cannot be null!!"); }
			elementList.add(localUserData);

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening
			 * reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
			 * Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the
			 * end element of its outer element
			 */
			public static InputPartPayload parse(final javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
				final InputPartPayload object = new InputPartPayload();

				final int event;
				final java.lang.String nillableValue = null;
				final java.lang.String prefix = "";
				final java.lang.String namespaceuri = "";
				try {

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type") != null) {
						final java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = null;
							if (fullTypeName.indexOf(":") > -1) {
								nsPrefix = fullTypeName.substring(0, fullTypeName.indexOf(":"));
							}
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							final java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":") + 1);

							if (!"inputPartPayload".equals(type)) {
								// find namespace for the prefix
								final java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
								return (InputPartPayload) ExtensionMapper.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ normal attributes
					// from anyAttributes.

					reader.next();

					final java.util.ArrayList list2 = new java.util.ArrayList();

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "contentPackage").equals(reader.getName())) {
						reader.next();
						if (isReaderMTOMAware(reader) && java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_BINARY))) {
							// MTOM aware reader - get the datahandler directly and put it in the object
							object.setContentPackage((javax.activation.DataHandler) reader.getProperty(org.apache.axiom.om.OMConstants.DATA_HANDLER));
						} else {
							if (reader.getEventType() == javax.xml.stream.XMLStreamConstants.START_ELEMENT
									&& reader.getName().equals(
											new javax.xml.namespace.QName(org.apache.axiom.om.impl.MTOMConstants.XOP_NAMESPACE_URI,
													org.apache.axiom.om.impl.MTOMConstants.XOP_INCLUDE))) {
								final java.lang.String id = org.apache.axiom.om.util.ElementHelper.getContentID(reader, "UTF-8");
								object.setContentPackage(((org.apache.axiom.soap.impl.builder.MTOMStAXSOAPModelBuilder) ((org.apache.axiom.om.impl.llom.OMStAXWrapper) reader)
										.getBuilder()).getDataHandler(id));
								reader.next();

								reader.next();

							} else if (reader.hasText()) {
								// Do the usual conversion
								final java.lang.String content = reader.getText();
								object.setContentPackage(org.apache.axis2.databinding.utils.ConverterUtil.convertToBase64Binary(content));

								reader.next();

							}
						}

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an invalid parameter was passed
						throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "students").equals(reader.getName())) {

						// Process the array and step past its final element's end.
						list2.add(Student.Factory.parse(reader));

						// loop until we find a start element that is not part of this array
						boolean loopDone2 = false;
						while (!loopDone2) {
							// We should be at the end element, but make sure
							while (!reader.isEndElement()) {
								reader.next();
							}
							// Step out of this element
							reader.next();
							// Step to next element event.
							while (!reader.isStartElement() && !reader.isEndElement()) {
								reader.next();
							}
							if (reader.isEndElement()) {
								// two continuous end elements means we are exiting the xml structure
								loopDone2 = true;
							} else {
								if (new javax.xml.namespace.QName("", "students").equals(reader.getName())) {
									list2.add(Student.Factory.parse(reader));

								} else {
									loopDone2 = true;
								}
							}
						}
						// call the converter utility to convert and set the array

						object.setStudents((Student[]) org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(Student.class, list2));

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an invalid parameter was passed
						throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "userData").equals(reader.getName())) {

						object.setUserData(UserDataPayload.Factory.parse(reader));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an invalid parameter was passed
						throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement()) {
						// A start element we are not expecting indicates a trailing invalid property
						throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
					}

				} catch (final javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class DisarmOutputElement implements org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/", "disarmOutputElement",
				"ns1");

		private static java.lang.String generatePrefix(final java.lang.String namespace) {
			if (namespace.equals("http://localhost:8080/axis2_tester/services/")) { return "ns1"; }
			return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
		}

		/**
		 * field for DisarmOutputElement
		 */

		protected DisarmOutPutPayload localDisarmOutputElement;

		/**
		 * Auto generated getter method
		 * 
		 * @return DisarmOutPutPayload
		 */
		public DisarmOutPutPayload getDisarmOutputElement() {
			return localDisarmOutputElement;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param DisarmOutputElement
		 */
		public void setDisarmOutputElement(final DisarmOutPutPayload param) {

			this.localDisarmOutputElement = param;

		}

		/**
		 * isReaderMTOMAware
		 * 
		 * @return true if the reader supports MTOM
		 */
		public static boolean isReaderMTOMAware(final javax.xml.stream.XMLStreamReader reader) {
			boolean isReaderMTOMAware = false;

			try {
				isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
			} catch (final java.lang.IllegalArgumentException e) {
				isReaderMTOMAware = false;
			}
			return isReaderMTOMAware;
		}

		/**
		 * @param parentQName
		 * @param factory
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getOMElement(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory)
				throws org.apache.axis2.databinding.ADBException {

			final org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this, MY_QNAME) {

				public void serialize(final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
					DisarmOutputElement.this.serialize(MY_QNAME, factory, xmlWriter);
				}
			};
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(MY_QNAME, factory, dataSource);

		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException,
				org.apache.axis2.databinding.ADBException {
			serialize(parentQName, factory, xmlWriter, false);
		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter, final boolean serializeType)
				throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

			// We can safely assume an element has only one type associated with it

			if (localDisarmOutputElement == null) { throw new org.apache.axis2.databinding.ADBException("Property cannot be null!"); }
			localDisarmOutputElement.serialize(MY_QNAME, factory, xmlWriter);

		}

		/**
		 * Util method to write an attribute with the ns prefix
		 */
		private void writeAttribute(final java.lang.String prefix, final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (xmlWriter.getPrefix(namespace) == null) {
				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);

			}

			xmlWriter.writeAttribute(namespace, attName, attValue);

		}

		/**
		 * Util method to write an attribute without the ns prefix
		 */
		private void writeAttribute(final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (namespace.equals("")) {
				xmlWriter.writeAttribute(attName, attValue);
			} else {
				registerPrefix(xmlWriter, namespace);
				xmlWriter.writeAttribute(namespace, attName, attValue);
			}
		}

		/**
		 * Util method to write an attribute without the ns prefix
		 */
		private void writeQNameAttribute(final java.lang.String namespace, final java.lang.String attName, final javax.xml.namespace.QName qname,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

			final java.lang.String attributeNamespace = qname.getNamespaceURI();
			java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
			if (attributePrefix == null) {
				attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
			}
			java.lang.String attributeValue;
			if (attributePrefix.trim().length() > 0) {
				attributeValue = attributePrefix + ":" + qname.getLocalPart();
			} else {
				attributeValue = qname.getLocalPart();
			}

			if (namespace.equals("")) {
				xmlWriter.writeAttribute(attName, attributeValue);
			} else {
				registerPrefix(xmlWriter, namespace);
				xmlWriter.writeAttribute(namespace, attName, attributeValue);
			}
		}

		/**
		 * method to handle Qnames
		 */

		private void writeQName(final javax.xml.namespace.QName qname, final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			final java.lang.String namespaceURI = qname.getNamespaceURI();
			if (namespaceURI != null) {
				java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
				if (prefix == null) {
					prefix = generatePrefix(namespaceURI);
					xmlWriter.writeNamespace(prefix, namespaceURI);
					xmlWriter.setPrefix(prefix, namespaceURI);
				}

				if (prefix.trim().length() > 0) {
					xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
				} else {
					// i.e this is the default namespace
					xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
				}

			} else {
				xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
			}
		}

		private void writeQNames(final javax.xml.namespace.QName[] qnames, final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

			if (qnames != null) {
				// we have to store this data until last moment since it is not possible to write any
				// namespace data after writing the charactor data
				final java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
				java.lang.String namespaceURI = null;
				java.lang.String prefix = null;

				for (int i = 0; i < qnames.length; i++) {
					if (i > 0) {
						stringToWrite.append(" ");
					}
					namespaceURI = qnames[i].getNamespaceURI();
					if (namespaceURI != null) {
						prefix = xmlWriter.getPrefix(namespaceURI);
						if ((prefix == null) || (prefix.length() == 0)) {
							prefix = generatePrefix(namespaceURI);
							xmlWriter.writeNamespace(prefix, namespaceURI);
							xmlWriter.setPrefix(prefix, namespaceURI);
						}

						if (prefix.trim().length() > 0) {
							stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
						} else {
							stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
						}
					} else {
						stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
					}
				}
				xmlWriter.writeCharacters(stringToWrite.toString());
			}

		}

		/**
		 * Register a namespace prefix
		 */
		private java.lang.String registerPrefix(final javax.xml.stream.XMLStreamWriter xmlWriter, final java.lang.String namespace)
				throws javax.xml.stream.XMLStreamException {
			java.lang.String prefix = xmlWriter.getPrefix(namespace);

			if (prefix == null) {
				prefix = generatePrefix(namespace);

				while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
					prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
				}

				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);
			}

			return prefix;
		}

		/**
		 * databinding method to get an XML representation of this object
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(final javax.xml.namespace.QName qName) throws org.apache.axis2.databinding.ADBException {

			// We can safely assume an element has only one type associated with it
			return localDisarmOutputElement.getPullParser(MY_QNAME);

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening
			 * reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
			 * Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the
			 * end element of its outer element
			 */
			public static DisarmOutputElement parse(final javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
				final DisarmOutputElement object = new DisarmOutputElement();

				final int event;
				final java.lang.String nillableValue = null;
				final java.lang.String prefix = "";
				final java.lang.String namespaceuri = "";
				try {

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					// Note all attributes that were handled. Used to differ normal attributes
					// from anyAttributes.

					while (!reader.isEndElement()) {
						if (reader.isStartElement()) {

							if (reader.isStartElement()
									&& new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/", "disarmOutputElement").equals(reader.getName())) {

								object.setDisarmOutputElement(DisarmOutPutPayload.Factory.parse(reader));

							} // End of if for expected property start element

							else {
								// A start element we are not expecting indicates an invalid parameter was passed
								throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
							}

						} else {
							reader.next();
						}
					} // end of while loop

				} catch (final javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class Student implements org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name = student Namespace URI = http://localhost:8080/axis2_tester/services/ Namespace Prefix = ns1
		 */

		private static java.lang.String generatePrefix(final java.lang.String namespace) {
			if (namespace.equals("http://localhost:8080/axis2_tester/services/")) { return "ns1"; }
			return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
		}

		/**
		 * field for StudentId
		 */

		protected java.lang.String localStudentId;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getStudentId() {
			return localStudentId;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param StudentId
		 */
		public void setStudentId(final java.lang.String param) {

			this.localStudentId = param;

		}

		/**
		 * field for StudentFirstName
		 */

		protected java.lang.String localStudentFirstName;

		/*
		 * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this
		 * field in the serialized XML
		 */
		protected boolean localStudentFirstNameTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getStudentFirstName() {
			return localStudentFirstName;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param StudentFirstName
		 */
		public void setStudentFirstName(final java.lang.String param) {

			if (param != null) {
				// update the setting tracker
				localStudentFirstNameTracker = true;
			} else {
				localStudentFirstNameTracker = false;

			}

			this.localStudentFirstName = param;

		}

		/**
		 * field for StudentSureName
		 */

		protected java.lang.String localStudentSureName;

		/*
		 * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this
		 * field in the serialized XML
		 */
		protected boolean localStudentSureNameTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getStudentSureName() {
			return localStudentSureName;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param StudentSureName
		 */
		public void setStudentSureName(final java.lang.String param) {

			if (param != null) {
				// update the setting tracker
				localStudentSureNameTracker = true;
			} else {
				localStudentSureNameTracker = false;

			}

			this.localStudentSureName = param;

		}

		/**
		 * field for GroupName
		 */

		protected java.lang.String localGroupName;

		/*
		 * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this
		 * field in the serialized XML
		 */
		protected boolean localGroupNameTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getGroupName() {
			return localGroupName;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param GroupName
		 */
		public void setGroupName(final java.lang.String param) {

			if (param != null) {
				// update the setting tracker
				localGroupNameTracker = true;
			} else {
				localGroupNameTracker = false;

			}

			this.localGroupName = param;

		}

		/**
		 * field for TutorName
		 */

		protected java.lang.String localTutorName;

		/*
		 * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this
		 * field in the serialized XML
		 */
		protected boolean localTutorNameTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getTutorName() {
			return localTutorName;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param TutorName
		 */
		public void setTutorName(final java.lang.String param) {

			if (param != null) {
				// update the setting tracker
				localTutorNameTracker = true;
			} else {
				localTutorNameTracker = false;

			}

			this.localTutorName = param;

		}

		/**
		 * field for ResultFile
		 */

		protected javax.activation.DataHandler localResultFile;

		/**
		 * Auto generated getter method
		 * 
		 * @return javax.activation.DataHandler
		 */
		public javax.activation.DataHandler getResultFile() {
			return localResultFile;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param ResultFile
		 */
		public void setResultFile(final javax.activation.DataHandler param) {

			this.localResultFile = param;

		}

		/**
		 * isReaderMTOMAware
		 * 
		 * @return true if the reader supports MTOM
		 */
		public static boolean isReaderMTOMAware(final javax.xml.stream.XMLStreamReader reader) {
			boolean isReaderMTOMAware = false;

			try {
				isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
			} catch (final java.lang.IllegalArgumentException e) {
				isReaderMTOMAware = false;
			}
			return isReaderMTOMAware;
		}

		/**
		 * @param parentQName
		 * @param factory
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getOMElement(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory)
				throws org.apache.axis2.databinding.ADBException {

			final org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this, parentQName) {

				public void serialize(final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
					Student.this.serialize(parentQName, factory, xmlWriter);
				}
			};
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(parentQName, factory, dataSource);

		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException,
				org.apache.axis2.databinding.ADBException {
			serialize(parentQName, factory, xmlWriter, false);
		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter, final boolean serializeType)
				throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

			java.lang.String prefix = null;
			java.lang.String namespace = null;

			prefix = parentQName.getPrefix();
			namespace = parentQName.getNamespaceURI();

			if ((namespace != null) && (namespace.trim().length() > 0)) {
				final java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
				if (writerPrefix != null) {
					xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
				} else {
					if (prefix == null) {
						prefix = generatePrefix(namespace);
					}

					xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
					xmlWriter.writeNamespace(prefix, namespace);
					xmlWriter.setPrefix(prefix, namespace);
				}
			} else {
				xmlWriter.writeStartElement(parentQName.getLocalPart());
			}

			if (serializeType) {

				final java.lang.String namespacePrefix = registerPrefix(xmlWriter, "http://localhost:8080/axis2_tester/services/");
				if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)) {
					writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix + ":student", xmlWriter);
				} else {
					writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "student", xmlWriter);
				}

			}

			namespace = "";
			if (!namespace.equals("")) {
				prefix = xmlWriter.getPrefix(namespace);

				if (prefix == null) {
					prefix = generatePrefix(namespace);

					xmlWriter.writeStartElement(prefix, "studentId", namespace);
					xmlWriter.writeNamespace(prefix, namespace);
					xmlWriter.setPrefix(prefix, namespace);

				} else {
					xmlWriter.writeStartElement(namespace, "studentId");
				}

			} else {
				xmlWriter.writeStartElement("studentId");
			}

			if (localStudentId == null) {
				// write the nil attribute

				throw new org.apache.axis2.databinding.ADBException("studentId cannot be null!!");

			} else {

				xmlWriter.writeCharacters(localStudentId);

			}

			xmlWriter.writeEndElement();
			if (localStudentFirstNameTracker) {
				namespace = "";
				if (!namespace.equals("")) {
					prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = generatePrefix(namespace);

						xmlWriter.writeStartElement(prefix, "studentFirstName", namespace);
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					} else {
						xmlWriter.writeStartElement(namespace, "studentFirstName");
					}

				} else {
					xmlWriter.writeStartElement("studentFirstName");
				}

				if (localStudentFirstName == null) {
					// write the nil attribute

					throw new org.apache.axis2.databinding.ADBException("studentFirstName cannot be null!!");

				} else {

					xmlWriter.writeCharacters(localStudentFirstName);

				}

				xmlWriter.writeEndElement();
			}
			if (localStudentSureNameTracker) {
				namespace = "";
				if (!namespace.equals("")) {
					prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = generatePrefix(namespace);

						xmlWriter.writeStartElement(prefix, "studentSureName", namespace);
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					} else {
						xmlWriter.writeStartElement(namespace, "studentSureName");
					}

				} else {
					xmlWriter.writeStartElement("studentSureName");
				}

				if (localStudentSureName == null) {
					// write the nil attribute

					throw new org.apache.axis2.databinding.ADBException("studentSureName cannot be null!!");

				} else {

					xmlWriter.writeCharacters(localStudentSureName);

				}

				xmlWriter.writeEndElement();
			}
			if (localGroupNameTracker) {
				namespace = "";
				if (!namespace.equals("")) {
					prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = generatePrefix(namespace);

						xmlWriter.writeStartElement(prefix, "groupName", namespace);
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					} else {
						xmlWriter.writeStartElement(namespace, "groupName");
					}

				} else {
					xmlWriter.writeStartElement("groupName");
				}

				if (localGroupName == null) {
					// write the nil attribute

					throw new org.apache.axis2.databinding.ADBException("groupName cannot be null!!");

				} else {

					xmlWriter.writeCharacters(localGroupName);

				}

				xmlWriter.writeEndElement();
			}
			if (localTutorNameTracker) {
				namespace = "";
				if (!namespace.equals("")) {
					prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = generatePrefix(namespace);

						xmlWriter.writeStartElement(prefix, "tutorName", namespace);
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					} else {
						xmlWriter.writeStartElement(namespace, "tutorName");
					}

				} else {
					xmlWriter.writeStartElement("tutorName");
				}

				if (localTutorName == null) {
					// write the nil attribute

					throw new org.apache.axis2.databinding.ADBException("tutorName cannot be null!!");

				} else {

					xmlWriter.writeCharacters(localTutorName);

				}

				xmlWriter.writeEndElement();
			}
			namespace = "";
			if (!namespace.equals("")) {
				prefix = xmlWriter.getPrefix(namespace);

				if (prefix == null) {
					prefix = generatePrefix(namespace);

					xmlWriter.writeStartElement(prefix, "resultFile", namespace);
					xmlWriter.writeNamespace(prefix, namespace);
					xmlWriter.setPrefix(prefix, namespace);

				} else {
					xmlWriter.writeStartElement(namespace, "resultFile");
				}

			} else {
				xmlWriter.writeStartElement("resultFile");
			}

			if (localResultFile != null) {
				xmlWriter.writeDataHandler(localResultFile);
			}

			xmlWriter.writeEndElement();

			xmlWriter.writeEndElement();

		}

		/**
		 * Util method to write an attribute with the ns prefix
		 */
		private void writeAttribute(final java.lang.String prefix, final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (xmlWriter.getPrefix(namespace) == null) {
				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);

			}

			xmlWriter.writeAttribute(namespace, attName, attValue);

		}

		/**
		 * Register a namespace prefix
		 */
		private java.lang.String registerPrefix(final javax.xml.stream.XMLStreamWriter xmlWriter, final java.lang.String namespace)
				throws javax.xml.stream.XMLStreamException {
			java.lang.String prefix = xmlWriter.getPrefix(namespace);

			if (prefix == null) {
				prefix = generatePrefix(namespace);

				while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
					prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
				}

				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);
			}

			return prefix;
		}

		/**
		 * databinding method to get an XML representation of this object
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(final javax.xml.namespace.QName qName) throws org.apache.axis2.databinding.ADBException {

			final java.util.ArrayList elementList = new java.util.ArrayList();
			final java.util.ArrayList attribList = new java.util.ArrayList();

			elementList.add(new javax.xml.namespace.QName("", "studentId"));

			if (localStudentId != null) {
				elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStudentId));
			} else {
				throw new org.apache.axis2.databinding.ADBException("studentId cannot be null!!");
			}
			if (localStudentFirstNameTracker) {
				elementList.add(new javax.xml.namespace.QName("", "studentFirstName"));

				if (localStudentFirstName != null) {
					elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStudentFirstName));
				} else {
					throw new org.apache.axis2.databinding.ADBException("studentFirstName cannot be null!!");
				}
			}
			if (localStudentSureNameTracker) {
				elementList.add(new javax.xml.namespace.QName("", "studentSureName"));

				if (localStudentSureName != null) {
					elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStudentSureName));
				} else {
					throw new org.apache.axis2.databinding.ADBException("studentSureName cannot be null!!");
				}
			}
			if (localGroupNameTracker) {
				elementList.add(new javax.xml.namespace.QName("", "groupName"));

				if (localGroupName != null) {
					elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localGroupName));
				} else {
					throw new org.apache.axis2.databinding.ADBException("groupName cannot be null!!");
				}
			}
			if (localTutorNameTracker) {
				elementList.add(new javax.xml.namespace.QName("", "tutorName"));

				if (localTutorName != null) {
					elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localTutorName));
				} else {
					throw new org.apache.axis2.databinding.ADBException("tutorName cannot be null!!");
				}
			}
			elementList.add(new javax.xml.namespace.QName("", "resultFile"));

			elementList.add(localResultFile);

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening
			 * reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
			 * Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the
			 * end element of its outer element
			 */
			public static Student parse(final javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
				final Student object = new Student();

				final int event;
				final java.lang.String nillableValue = null;
				final java.lang.String prefix = "";
				final java.lang.String namespaceuri = "";
				try {

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type") != null) {
						final java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = null;
							if (fullTypeName.indexOf(":") > -1) {
								nsPrefix = fullTypeName.substring(0, fullTypeName.indexOf(":"));
							}
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							final java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":") + 1);

							if (!"student".equals(type)) {
								// find namespace for the prefix
								final java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
								return (Student) ExtensionMapper.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ normal attributes
					// from anyAttributes.

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "studentId").equals(reader.getName())) {

						final java.lang.String content = reader.getElementText();

						object.setStudentId(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an invalid parameter was passed
						throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "studentFirstName").equals(reader.getName())) {

						final java.lang.String content = reader.getElementText();

						object.setStudentFirstName(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					else {

					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "studentSureName").equals(reader.getName())) {

						final java.lang.String content = reader.getElementText();

						object.setStudentSureName(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					else {

					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "groupName").equals(reader.getName())) {

						final java.lang.String content = reader.getElementText();

						object.setGroupName(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					else {

					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "tutorName").equals(reader.getName())) {

						final java.lang.String content = reader.getElementText();

						object.setTutorName(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					else {

					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "resultFile").equals(reader.getName())) {
						reader.next();
						if (isReaderMTOMAware(reader) && java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_BINARY))) {
							// MTOM aware reader - get the datahandler directly and put it in the object
							object.setResultFile((javax.activation.DataHandler) reader.getProperty(org.apache.axiom.om.OMConstants.DATA_HANDLER));
						} else {
							if (reader.getEventType() == javax.xml.stream.XMLStreamConstants.START_ELEMENT
									&& reader.getName().equals(
											new javax.xml.namespace.QName(org.apache.axiom.om.impl.MTOMConstants.XOP_NAMESPACE_URI,
													org.apache.axiom.om.impl.MTOMConstants.XOP_INCLUDE))) {
								final java.lang.String id = org.apache.axiom.om.util.ElementHelper.getContentID(reader, "UTF-8");
								object.setResultFile(((org.apache.axiom.soap.impl.builder.MTOMStAXSOAPModelBuilder) ((org.apache.axiom.om.impl.llom.OMStAXWrapper) reader)
										.getBuilder()).getDataHandler(id));
								reader.next();

								reader.next();

							} else if (reader.hasText()) {
								// Do the usual conversion
								final java.lang.String content = reader.getText();
								object.setResultFile(org.apache.axis2.databinding.utils.ConverterUtil.convertToBase64Binary(content));

								reader.next();

							}
						}

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an invalid parameter was passed
						throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement()) {
						// A start element we are not expecting indicates a trailing invalid property
						throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
					}

				} catch (final javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class ResultValuesInput implements org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name = resultValuesInput Namespace URI = http://localhost:8080/axis2_tester/services/ Namespace
		 * Prefix = ns1
		 */

		private static java.lang.String generatePrefix(final java.lang.String namespace) {
			if (namespace.equals("http://localhost:8080/axis2_tester/services/")) { return "ns1"; }
			return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
		}

		/**
		 * field for UserData
		 */

		protected UserDataPayload localUserData;

		/**
		 * Auto generated getter method
		 * 
		 * @return UserDataPayload
		 */
		public UserDataPayload getUserData() {
			return localUserData;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param UserData
		 */
		public void setUserData(final UserDataPayload param) {

			this.localUserData = param;

		}

		/**
		 * field for AdditionalParameters This was an Array!
		 */

		protected PostGetParams[] localAdditionalParameters;

		/*
		 * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this
		 * field in the serialized XML
		 */
		protected boolean localAdditionalParametersTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return PostGetParams[]
		 */
		public PostGetParams[] getAdditionalParameters() {
			return localAdditionalParameters;
		}

		/**
		 * validate the array for AdditionalParameters
		 */
		protected void validateAdditionalParameters(final PostGetParams[] param) {

		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param AdditionalParameters
		 */
		public void setAdditionalParameters(final PostGetParams[] param) {

			validateAdditionalParameters(param);

			if (param != null) {
				// update the setting tracker
				localAdditionalParametersTracker = true;
			} else {
				localAdditionalParametersTracker = false;

			}

			this.localAdditionalParameters = param;
		}

		/**
		 * Auto generated add method for the array for convenience
		 * 
		 * @param param PostGetParams
		 */
		public void addAdditionalParameters(final PostGetParams param) {
			if (localAdditionalParameters == null) {
				localAdditionalParameters = new PostGetParams[] {};
			}

			// update the setting tracker
			localAdditionalParametersTracker = true;

			final java.util.List list = org.apache.axis2.databinding.utils.ConverterUtil.toList(localAdditionalParameters);
			list.add(param);
			this.localAdditionalParameters = (PostGetParams[]) list.toArray(new PostGetParams[list.size()]);

		}

		/**
		 * isReaderMTOMAware
		 * 
		 * @return true if the reader supports MTOM
		 */
		public static boolean isReaderMTOMAware(final javax.xml.stream.XMLStreamReader reader) {
			boolean isReaderMTOMAware = false;

			try {
				isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
			} catch (final java.lang.IllegalArgumentException e) {
				isReaderMTOMAware = false;
			}
			return isReaderMTOMAware;
		}

		/**
		 * @param parentQName
		 * @param factory
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getOMElement(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory)
				throws org.apache.axis2.databinding.ADBException {

			final org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this, parentQName) {

				public void serialize(final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
					ResultValuesInput.this.serialize(parentQName, factory, xmlWriter);
				}
			};
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(parentQName, factory, dataSource);

		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException,
				org.apache.axis2.databinding.ADBException {
			serialize(parentQName, factory, xmlWriter, false);
		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter, final boolean serializeType)
				throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

			java.lang.String prefix = null;
			java.lang.String namespace = null;

			prefix = parentQName.getPrefix();
			namespace = parentQName.getNamespaceURI();

			if ((namespace != null) && (namespace.trim().length() > 0)) {
				final java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
				if (writerPrefix != null) {
					xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
				} else {
					if (prefix == null) {
						prefix = generatePrefix(namespace);
					}

					xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
					xmlWriter.writeNamespace(prefix, namespace);
					xmlWriter.setPrefix(prefix, namespace);
				}
			} else {
				xmlWriter.writeStartElement(parentQName.getLocalPart());
			}

			if (serializeType) {

				final java.lang.String namespacePrefix = registerPrefix(xmlWriter, "http://localhost:8080/axis2_tester/services/");
				if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)) {
					writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix + ":resultValuesInput", xmlWriter);
				} else {
					writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "resultValuesInput", xmlWriter);
				}

			}

			if (localUserData == null) { throw new org.apache.axis2.databinding.ADBException("userData cannot be null!!"); }
			localUserData.serialize(new javax.xml.namespace.QName("", "userData"), factory, xmlWriter);
			if (localAdditionalParametersTracker) {
				if (localAdditionalParameters != null) {
					for (int i = 0; i < localAdditionalParameters.length; i++) {
						if (localAdditionalParameters[i] != null) {
							localAdditionalParameters[i].serialize(new javax.xml.namespace.QName("", "additionalParameters"), factory, xmlWriter);
						} else {

							// we don't have to do any thing since minOccures is zero

						}

					}
				} else {

					throw new org.apache.axis2.databinding.ADBException("additionalParameters cannot be null!!");

				}
			}
			xmlWriter.writeEndElement();

		}

		/**
		 * Util method to write an attribute with the ns prefix
		 */
		private void writeAttribute(final java.lang.String prefix, final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (xmlWriter.getPrefix(namespace) == null) {
				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);

			}

			xmlWriter.writeAttribute(namespace, attName, attValue);

		}

		/**
		 * Register a namespace prefix
		 */
		private java.lang.String registerPrefix(final javax.xml.stream.XMLStreamWriter xmlWriter, final java.lang.String namespace)
				throws javax.xml.stream.XMLStreamException {
			java.lang.String prefix = xmlWriter.getPrefix(namespace);

			if (prefix == null) {
				prefix = generatePrefix(namespace);

				while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
					prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
				}

				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);
			}

			return prefix;
		}

		/**
		 * databinding method to get an XML representation of this object
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(final javax.xml.namespace.QName qName) throws org.apache.axis2.databinding.ADBException {

			final java.util.ArrayList elementList = new java.util.ArrayList();
			final java.util.ArrayList attribList = new java.util.ArrayList();

			elementList.add(new javax.xml.namespace.QName("", "userData"));

			if (localUserData == null) { throw new org.apache.axis2.databinding.ADBException("userData cannot be null!!"); }
			elementList.add(localUserData);
			if (localAdditionalParametersTracker) {
				if (localAdditionalParameters != null) {
					for (int i = 0; i < localAdditionalParameters.length; i++) {

						if (localAdditionalParameters[i] != null) {
							elementList.add(new javax.xml.namespace.QName("", "additionalParameters"));
							elementList.add(localAdditionalParameters[i]);
						} else {

							// nothing to do

						}

					}
				} else {

					throw new org.apache.axis2.databinding.ADBException("additionalParameters cannot be null!!");

				}

			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening
			 * reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
			 * Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the
			 * end element of its outer element
			 */
			public static ResultValuesInput parse(final javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
				final ResultValuesInput object = new ResultValuesInput();

				final int event;
				final java.lang.String nillableValue = null;
				final java.lang.String prefix = "";
				final java.lang.String namespaceuri = "";
				try {

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type") != null) {
						final java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = null;
							if (fullTypeName.indexOf(":") > -1) {
								nsPrefix = fullTypeName.substring(0, fullTypeName.indexOf(":"));
							}
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							final java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":") + 1);

							if (!"resultValuesInput".equals(type)) {
								// find namespace for the prefix
								final java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
								return (ResultValuesInput) ExtensionMapper.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ normal attributes
					// from anyAttributes.

					reader.next();

					final java.util.ArrayList list2 = new java.util.ArrayList();

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "userData").equals(reader.getName())) {

						object.setUserData(UserDataPayload.Factory.parse(reader));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an invalid parameter was passed
						throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "additionalParameters").equals(reader.getName())) {

						// Process the array and step past its final element's end.
						list2.add(PostGetParams.Factory.parse(reader));

						// loop until we find a start element that is not part of this array
						boolean loopDone2 = false;
						while (!loopDone2) {
							// We should be at the end element, but make sure
							while (!reader.isEndElement()) {
								reader.next();
							}
							// Step out of this element
							reader.next();
							// Step to next element event.
							while (!reader.isStartElement() && !reader.isEndElement()) {
								reader.next();
							}
							if (reader.isEndElement()) {
								// two continuous end elements means we are exiting the xml structure
								loopDone2 = true;
							} else {
								if (new javax.xml.namespace.QName("", "additionalParameters").equals(reader.getName())) {
									list2.add(PostGetParams.Factory.parse(reader));

								} else {
									loopDone2 = true;
								}
							}
						}
						// call the converter utility to convert and set the array

						object.setAdditionalParameters((PostGetParams[]) org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(PostGetParams.class, list2));

					} // End of if for expected property start element

					else {

					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement()) {
						// A start element we are not expecting indicates a trailing invalid property
						throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
					}

				} catch (final javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class ReturnLinkPayload implements org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name = returnLinkPayload Namespace URI = http://localhost:8080/axis2_tester/services/ Namespace
		 * Prefix = ns1
		 */

		private static java.lang.String generatePrefix(final java.lang.String namespace) {
			if (namespace.equals("http://localhost:8080/axis2_tester/services/")) { return "ns1"; }
			return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
		}

		/**
		 * field for Link
		 */

		protected java.lang.String localLink;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getLink() {
			return localLink;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param Link
		 */
		public void setLink(final java.lang.String param) {

			this.localLink = param;

		}

		/**
		 * field for AdditionalParameters This was an Array!
		 */

		protected PostGetParams[] localAdditionalParameters;

		/*
		 * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this
		 * field in the serialized XML
		 */
		protected boolean localAdditionalParametersTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return PostGetParams[]
		 */
		public PostGetParams[] getAdditionalParameters() {
			return localAdditionalParameters;
		}

		/**
		 * validate the array for AdditionalParameters
		 */
		protected void validateAdditionalParameters(final PostGetParams[] param) {

		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param AdditionalParameters
		 */
		public void setAdditionalParameters(final PostGetParams[] param) {

			validateAdditionalParameters(param);

			if (param != null) {
				// update the setting tracker
				localAdditionalParametersTracker = true;
			} else {
				localAdditionalParametersTracker = false;

			}

			this.localAdditionalParameters = param;
		}

		/**
		 * Auto generated add method for the array for convenience
		 * 
		 * @param param PostGetParams
		 */
		public void addAdditionalParameters(final PostGetParams param) {
			if (localAdditionalParameters == null) {
				localAdditionalParameters = new PostGetParams[] {};
			}

			// update the setting tracker
			localAdditionalParametersTracker = true;

			final java.util.List list = org.apache.axis2.databinding.utils.ConverterUtil.toList(localAdditionalParameters);
			list.add(param);
			this.localAdditionalParameters = (PostGetParams[]) list.toArray(new PostGetParams[list.size()]);

		}

		/**
		 * isReaderMTOMAware
		 * 
		 * @return true if the reader supports MTOM
		 */
		public static boolean isReaderMTOMAware(final javax.xml.stream.XMLStreamReader reader) {
			boolean isReaderMTOMAware = false;

			try {
				isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
			} catch (final java.lang.IllegalArgumentException e) {
				isReaderMTOMAware = false;
			}
			return isReaderMTOMAware;
		}

		/**
		 * @param parentQName
		 * @param factory
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getOMElement(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory)
				throws org.apache.axis2.databinding.ADBException {

			final org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this, parentQName) {

				public void serialize(final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
					ReturnLinkPayload.this.serialize(parentQName, factory, xmlWriter);
				}
			};
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(parentQName, factory, dataSource);

		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException,
				org.apache.axis2.databinding.ADBException {
			serialize(parentQName, factory, xmlWriter, false);
		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter, final boolean serializeType)
				throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

			java.lang.String prefix = null;
			java.lang.String namespace = null;

			prefix = parentQName.getPrefix();
			namespace = parentQName.getNamespaceURI();

			if ((namespace != null) && (namespace.trim().length() > 0)) {
				final java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
				if (writerPrefix != null) {
					xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
				} else {
					if (prefix == null) {
						prefix = generatePrefix(namespace);
					}

					xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
					xmlWriter.writeNamespace(prefix, namespace);
					xmlWriter.setPrefix(prefix, namespace);
				}
			} else {
				xmlWriter.writeStartElement(parentQName.getLocalPart());
			}

			if (serializeType) {

				final java.lang.String namespacePrefix = registerPrefix(xmlWriter, "http://localhost:8080/axis2_tester/services/");
				if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)) {
					writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix + ":returnLinkPayload", xmlWriter);
				} else {
					writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "returnLinkPayload", xmlWriter);
				}

			}

			namespace = "";
			if (!namespace.equals("")) {
				prefix = xmlWriter.getPrefix(namespace);

				if (prefix == null) {
					prefix = generatePrefix(namespace);

					xmlWriter.writeStartElement(prefix, "link", namespace);
					xmlWriter.writeNamespace(prefix, namespace);
					xmlWriter.setPrefix(prefix, namespace);

				} else {
					xmlWriter.writeStartElement(namespace, "link");
				}

			} else {
				xmlWriter.writeStartElement("link");
			}

			if (localLink == null) {
				// write the nil attribute

				throw new org.apache.axis2.databinding.ADBException("link cannot be null!!");

			} else {

				xmlWriter.writeCharacters(localLink);

			}

			xmlWriter.writeEndElement();
			if (localAdditionalParametersTracker) {
				if (localAdditionalParameters != null) {
					for (int i = 0; i < localAdditionalParameters.length; i++) {
						if (localAdditionalParameters[i] != null) {
							localAdditionalParameters[i].serialize(new javax.xml.namespace.QName("", "additionalParameters"), factory, xmlWriter);
						} else {

							// we don't have to do any thing since minOccures is zero

						}

					}
				} else {

					throw new org.apache.axis2.databinding.ADBException("additionalParameters cannot be null!!");

				}
			}
			xmlWriter.writeEndElement();

		}

		/**
		 * Util method to write an attribute with the ns prefix
		 */
		private void writeAttribute(final java.lang.String prefix, final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (xmlWriter.getPrefix(namespace) == null) {
				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);

			}

			xmlWriter.writeAttribute(namespace, attName, attValue);

		}

		/**
		 * Util method to write an attribute without the ns prefix
		 */
		private void writeQNameAttribute(final java.lang.String namespace, final java.lang.String attName, final javax.xml.namespace.QName qname,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

			final java.lang.String attributeNamespace = qname.getNamespaceURI();
			java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
			if (attributePrefix == null) {
				attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
			}
			java.lang.String attributeValue;
			if (attributePrefix.trim().length() > 0) {
				attributeValue = attributePrefix + ":" + qname.getLocalPart();
			} else {
				attributeValue = qname.getLocalPart();
			}

			if (namespace.equals("")) {
				xmlWriter.writeAttribute(attName, attributeValue);
			} else {
				registerPrefix(xmlWriter, namespace);
				xmlWriter.writeAttribute(namespace, attName, attributeValue);
			}
		}

		/**
		 * method to handle Qnames
		 */

		private void writeQName(final javax.xml.namespace.QName qname, final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			final java.lang.String namespaceURI = qname.getNamespaceURI();
			if (namespaceURI != null) {
				java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
				if (prefix == null) {
					prefix = generatePrefix(namespaceURI);
					xmlWriter.writeNamespace(prefix, namespaceURI);
					xmlWriter.setPrefix(prefix, namespaceURI);
				}

				if (prefix.trim().length() > 0) {
					xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
				} else {
					// i.e this is the default namespace
					xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
				}

			} else {
				xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
			}
		}

		private void writeQNames(final javax.xml.namespace.QName[] qnames, final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

			if (qnames != null) {
				// we have to store this data until last moment since it is not possible to write any
				// namespace data after writing the charactor data
				final java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
				java.lang.String namespaceURI = null;
				java.lang.String prefix = null;

				for (int i = 0; i < qnames.length; i++) {
					if (i > 0) {
						stringToWrite.append(" ");
					}
					namespaceURI = qnames[i].getNamespaceURI();
					if (namespaceURI != null) {
						prefix = xmlWriter.getPrefix(namespaceURI);
						if ((prefix == null) || (prefix.length() == 0)) {
							prefix = generatePrefix(namespaceURI);
							xmlWriter.writeNamespace(prefix, namespaceURI);
							xmlWriter.setPrefix(prefix, namespaceURI);
						}

						if (prefix.trim().length() > 0) {
							stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
						} else {
							stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
						}
					} else {
						stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
					}
				}
				xmlWriter.writeCharacters(stringToWrite.toString());
			}

		}

		/**
		 * Register a namespace prefix
		 */
		private java.lang.String registerPrefix(final javax.xml.stream.XMLStreamWriter xmlWriter, final java.lang.String namespace)
				throws javax.xml.stream.XMLStreamException {
			java.lang.String prefix = xmlWriter.getPrefix(namespace);

			if (prefix == null) {
				prefix = generatePrefix(namespace);

				while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
					prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
				}

				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);
			}

			return prefix;
		}

		/**
		 * databinding method to get an XML representation of this object
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(final javax.xml.namespace.QName qName) throws org.apache.axis2.databinding.ADBException {

			final java.util.ArrayList elementList = new java.util.ArrayList();
			final java.util.ArrayList attribList = new java.util.ArrayList();

			elementList.add(new javax.xml.namespace.QName("", "link"));

			if (localLink != null) {
				elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localLink));
			} else {
				throw new org.apache.axis2.databinding.ADBException("link cannot be null!!");
			}
			if (localAdditionalParametersTracker) {
				if (localAdditionalParameters != null) {
					for (int i = 0; i < localAdditionalParameters.length; i++) {

						if (localAdditionalParameters[i] != null) {
							elementList.add(new javax.xml.namespace.QName("", "additionalParameters"));
							elementList.add(localAdditionalParameters[i]);
						} else {

							// nothing to do

						}

					}
				} else {

					throw new org.apache.axis2.databinding.ADBException("additionalParameters cannot be null!!");

				}

			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening
			 * reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
			 * Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the
			 * end element of its outer element
			 */
			public static ReturnLinkPayload parse(final javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
				final ReturnLinkPayload object = new ReturnLinkPayload();

				final int event;
				final java.lang.String nillableValue = null;
				final java.lang.String prefix = "";
				final java.lang.String namespaceuri = "";
				try {

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type") != null) {
						final java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = null;
							if (fullTypeName.indexOf(":") > -1) {
								nsPrefix = fullTypeName.substring(0, fullTypeName.indexOf(":"));
							}
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							final java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":") + 1);

							if (!"returnLinkPayload".equals(type)) {
								// find namespace for the prefix
								final java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
								return (ReturnLinkPayload) ExtensionMapper.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ normal attributes
					// from anyAttributes.

					reader.next();

					final java.util.ArrayList list2 = new java.util.ArrayList();

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "link").equals(reader.getName())) {

						final java.lang.String content = reader.getElementText();

						object.setLink(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an invalid parameter was passed
						throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "additionalParameters").equals(reader.getName())) {

						// Process the array and step past its final element's end.
						list2.add(PostGetParams.Factory.parse(reader));

						// loop until we find a start element that is not part of this array
						boolean loopDone2 = false;
						while (!loopDone2) {
							// We should be at the end element, but make sure
							while (!reader.isEndElement()) {
								reader.next();
							}
							// Step out of this element
							reader.next();
							// Step to next element event.
							while (!reader.isStartElement() && !reader.isEndElement()) {
								reader.next();
							}
							if (reader.isEndElement()) {
								// two continuous end elements means we are exiting the xml structure
								loopDone2 = true;
							} else {
								if (new javax.xml.namespace.QName("", "additionalParameters").equals(reader.getName())) {
									list2.add(PostGetParams.Factory.parse(reader));

								} else {
									loopDone2 = true;
								}
							}
						}
						// call the converter utility to convert and set the array

						object.setAdditionalParameters((PostGetParams[]) org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(PostGetParams.class, list2));

					} // End of if for expected property start element

					else {

					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement()) {
						// A start element we are not expecting indicates a trailing invalid property
						throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
					}

				} catch (final javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class DisarmInputElement implements org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/", "disarmInputElement",
				"ns1");

		private static java.lang.String generatePrefix(final java.lang.String namespace) {
			if (namespace.equals("http://localhost:8080/axis2_tester/services/")) { return "ns1"; }
			return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
		}

		/**
		 * field for DisarmInputElement
		 */

		protected UserDataPayload localDisarmInputElement;

		/**
		 * Auto generated getter method
		 * 
		 * @return UserDataPayload
		 */
		public UserDataPayload getDisarmInputElement() {
			return localDisarmInputElement;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param DisarmInputElement
		 */
		public void setDisarmInputElement(final UserDataPayload param) {

			this.localDisarmInputElement = param;

		}

		/**
		 * isReaderMTOMAware
		 * 
		 * @return true if the reader supports MTOM
		 */
		public static boolean isReaderMTOMAware(final javax.xml.stream.XMLStreamReader reader) {
			boolean isReaderMTOMAware = false;

			try {
				isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
			} catch (final java.lang.IllegalArgumentException e) {
				isReaderMTOMAware = false;
			}
			return isReaderMTOMAware;
		}

		/**
		 * @param parentQName
		 * @param factory
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getOMElement(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory)
				throws org.apache.axis2.databinding.ADBException {

			final org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this, MY_QNAME) {

				public void serialize(final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
					DisarmInputElement.this.serialize(MY_QNAME, factory, xmlWriter);
				}
			};
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(MY_QNAME, factory, dataSource);

		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException,
				org.apache.axis2.databinding.ADBException {
			serialize(parentQName, factory, xmlWriter, false);
		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter, final boolean serializeType)
				throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

			// We can safely assume an element has only one type associated with it

			if (localDisarmInputElement == null) { throw new org.apache.axis2.databinding.ADBException("Property cannot be null!"); }
			localDisarmInputElement.serialize(MY_QNAME, factory, xmlWriter);

		}

		/**
		 * Util method to write an attribute with the ns prefix
		 */
		private void writeAttribute(final java.lang.String prefix, final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (xmlWriter.getPrefix(namespace) == null) {
				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);

			}

			xmlWriter.writeAttribute(namespace, attName, attValue);

		}

		/**
		 * Util method to write an attribute without the ns prefix
		 */
		private void writeAttribute(final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (namespace.equals("")) {
				xmlWriter.writeAttribute(attName, attValue);
			} else {
				registerPrefix(xmlWriter, namespace);
				xmlWriter.writeAttribute(namespace, attName, attValue);
			}
		}

		/**
		 * Util method to write an attribute without the ns prefix
		 */
		private void writeQNameAttribute(final java.lang.String namespace, final java.lang.String attName, final javax.xml.namespace.QName qname,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

			final java.lang.String attributeNamespace = qname.getNamespaceURI();
			java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
			if (attributePrefix == null) {
				attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
			}
			java.lang.String attributeValue;
			if (attributePrefix.trim().length() > 0) {
				attributeValue = attributePrefix + ":" + qname.getLocalPart();
			} else {
				attributeValue = qname.getLocalPart();
			}

			if (namespace.equals("")) {
				xmlWriter.writeAttribute(attName, attributeValue);
			} else {
				registerPrefix(xmlWriter, namespace);
				xmlWriter.writeAttribute(namespace, attName, attributeValue);
			}
		}

		/**
		 * method to handle Qnames
		 */

		private void writeQName(final javax.xml.namespace.QName qname, final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			final java.lang.String namespaceURI = qname.getNamespaceURI();
			if (namespaceURI != null) {
				java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
				if (prefix == null) {
					prefix = generatePrefix(namespaceURI);
					xmlWriter.writeNamespace(prefix, namespaceURI);
					xmlWriter.setPrefix(prefix, namespaceURI);
				}

				if (prefix.trim().length() > 0) {
					xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
				} else {
					// i.e this is the default namespace
					xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
				}

			} else {
				xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
			}
		}

		private void writeQNames(final javax.xml.namespace.QName[] qnames, final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

			if (qnames != null) {
				// we have to store this data until last moment since it is not possible to write any
				// namespace data after writing the charactor data
				final java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
				java.lang.String namespaceURI = null;
				java.lang.String prefix = null;

				for (int i = 0; i < qnames.length; i++) {
					if (i > 0) {
						stringToWrite.append(" ");
					}
					namespaceURI = qnames[i].getNamespaceURI();
					if (namespaceURI != null) {
						prefix = xmlWriter.getPrefix(namespaceURI);
						if ((prefix == null) || (prefix.length() == 0)) {
							prefix = generatePrefix(namespaceURI);
							xmlWriter.writeNamespace(prefix, namespaceURI);
							xmlWriter.setPrefix(prefix, namespaceURI);
						}

						if (prefix.trim().length() > 0) {
							stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
						} else {
							stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
						}
					} else {
						stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
					}
				}
				xmlWriter.writeCharacters(stringToWrite.toString());
			}

		}

		/**
		 * Register a namespace prefix
		 */
		private java.lang.String registerPrefix(final javax.xml.stream.XMLStreamWriter xmlWriter, final java.lang.String namespace)
				throws javax.xml.stream.XMLStreamException {
			java.lang.String prefix = xmlWriter.getPrefix(namespace);

			if (prefix == null) {
				prefix = generatePrefix(namespace);

				while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
					prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
				}

				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);
			}

			return prefix;
		}

		/**
		 * databinding method to get an XML representation of this object
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(final javax.xml.namespace.QName qName) throws org.apache.axis2.databinding.ADBException {

			// We can safely assume an element has only one type associated with it
			return localDisarmInputElement.getPullParser(MY_QNAME);

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening
			 * reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
			 * Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the
			 * end element of its outer element
			 */
			public static DisarmInputElement parse(final javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
				final DisarmInputElement object = new DisarmInputElement();

				final int event;
				final java.lang.String nillableValue = null;
				final java.lang.String prefix = "";
				final java.lang.String namespaceuri = "";
				try {

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					// Note all attributes that were handled. Used to differ normal attributes
					// from anyAttributes.

					while (!reader.isEndElement()) {
						if (reader.isStartElement()) {

							if (reader.isStartElement()
									&& new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/", "disarmInputElement").equals(reader.getName())) {

								object.setDisarmInputElement(UserDataPayload.Factory.parse(reader));

							} // End of if for expected property start element

							else {
								// A start element we are not expecting indicates an invalid parameter was passed
								throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
							}

						} else {
							reader.next();
						}
					} // end of while loop

				} catch (final javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class ExtensionMapper {

		public static java.lang.Object getTypeObject(final java.lang.String namespaceURI, final java.lang.String typeName, final javax.xml.stream.XMLStreamReader reader)
				throws java.lang.Exception {

			if ("http://localhost:8080/axis2_tester/services/".equals(namespaceURI) && "postGet_type1".equals(typeName)) {

			return PostGet_type1.Factory.parse(reader);

			}

			if ("http://localhost:8080/axis2_tester/services/".equals(namespaceURI) && "postGetParams".equals(typeName)) {

			return PostGetParams.Factory.parse(reader);

			}

			if ("http://localhost:8080/axis2_tester/services/".equals(namespaceURI) && "postGet_type1".equals(typeName)) {

			return PostGet_type1.Factory.parse(reader);

			}

			if ("http://localhost:8080/axis2_tester/services/".equals(namespaceURI) && "returnLinkPayload".equals(typeName)) {

			return ReturnLinkPayload.Factory.parse(reader);

			}

			if ("http://localhost:8080/axis2_tester/services/".equals(namespaceURI) && "resultValuesInput".equals(typeName)) {

			return ResultValuesInput.Factory.parse(reader);

			}

			if ("http://localhost:8080/axis2_tester/services/".equals(namespaceURI) && "userDataPayload".equals(typeName)) {

			return UserDataPayload.Factory.parse(reader);

			}

			if ("http://localhost:8080/axis2_tester/services/".equals(namespaceURI) && "armSiteResponsePayload".equals(typeName)) {

			return ArmSiteResponsePayload.Factory.parse(reader);

			}

			if ("http://localhost:8080/axis2_tester/services/".equals(namespaceURI) && "inputPartPayload".equals(typeName)) {

			return InputPartPayload.Factory.parse(reader);

			}

			if ("http://localhost:8080/axis2_tester/services/".equals(namespaceURI) && "student".equals(typeName)) {

			return Student.Factory.parse(reader);

			}

			if ("http://localhost:8080/axis2_tester/services/".equals(namespaceURI) && "disarmOutPutPayload".equals(typeName)) {

			return DisarmOutPutPayload.Factory.parse(reader);

			}

			if ("http://localhost:8080/axis2_tester/services/".equals(namespaceURI) && "armSitePayload".equals(typeName)) {

			return ArmSitePayload.Factory.parse(reader);

			}

			if ("http://localhost:8080/axis2_tester/services/".equals(namespaceURI) && "resultValuesOutput".equals(typeName)) {

			return ResultValuesOutput.Factory.parse(reader);

			}

			throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
		}

	}

	public static class PostGet_type1 implements org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/", "postGet_type1", "ns1");

		private static java.lang.String generatePrefix(final java.lang.String namespace) {
			if (namespace.equals("http://localhost:8080/axis2_tester/services/")) { return "ns1"; }
			return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
		}

		/**
		 * field for PostGet_type0
		 */

		protected java.lang.String localPostGet_type0;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getPostGet_type0() {
			return localPostGet_type0;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param PostGet_type0
		 */
		public void setPostGet_type0(final java.lang.String param) {

			if (org.apache.axis2.databinding.utils.ConverterUtil.convertToString(param).matches("post|get")) {
				this.localPostGet_type0 = param;
			} else {
				throw new java.lang.RuntimeException();
			}

		}

		public java.lang.String toString() {

			return localPostGet_type0.toString();

		}

		/**
		 * isReaderMTOMAware
		 * 
		 * @return true if the reader supports MTOM
		 */
		public static boolean isReaderMTOMAware(final javax.xml.stream.XMLStreamReader reader) {
			boolean isReaderMTOMAware = false;

			try {
				isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
			} catch (final java.lang.IllegalArgumentException e) {
				isReaderMTOMAware = false;
			}
			return isReaderMTOMAware;
		}

		/**
		 * @param parentQName
		 * @param factory
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getOMElement(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory)
				throws org.apache.axis2.databinding.ADBException {

			final org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this, MY_QNAME) {

				public void serialize(final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
					PostGet_type1.this.serialize(MY_QNAME, factory, xmlWriter);
				}
			};
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(MY_QNAME, factory, dataSource);

		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException,
				org.apache.axis2.databinding.ADBException {
			serialize(parentQName, factory, xmlWriter, false);
		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter, final boolean serializeType)
				throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

			// We can safely assume an element has only one type associated with it

			final java.lang.String namespace = parentQName.getNamespaceURI();
			final java.lang.String localName = parentQName.getLocalPart();

			if (!namespace.equals("")) {
				java.lang.String prefix = xmlWriter.getPrefix(namespace);

				if (prefix == null) {
					prefix = generatePrefix(namespace);

					xmlWriter.writeStartElement(prefix, localName, namespace);
					xmlWriter.writeNamespace(prefix, namespace);
					xmlWriter.setPrefix(prefix, namespace);

				} else {
					xmlWriter.writeStartElement(namespace, localName);
				}

			} else {
				xmlWriter.writeStartElement(localName);
			}

			// add the type details if this is used in a simple type
			if (serializeType) {
				final java.lang.String namespacePrefix = registerPrefix(xmlWriter, "http://localhost:8080/axis2_tester/services/");
				if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)) {
					writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix + ":postGet_type1", xmlWriter);
				} else {
					writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "postGet_type1", xmlWriter);
				}
			}

			if (localPostGet_type0 == null) {

				throw new org.apache.axis2.databinding.ADBException("Value cannot be null !!");

			} else {

				xmlWriter.writeCharacters(localPostGet_type0);

			}

			xmlWriter.writeEndElement();

		}

		/**
		 * Util method to write an attribute with the ns prefix
		 */
		private void writeAttribute(final java.lang.String prefix, final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (xmlWriter.getPrefix(namespace) == null) {
				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);

			}

			xmlWriter.writeAttribute(namespace, attName, attValue);

		}

		/**
		 * Register a namespace prefix
		 */
		private java.lang.String registerPrefix(final javax.xml.stream.XMLStreamWriter xmlWriter, final java.lang.String namespace)
				throws javax.xml.stream.XMLStreamException {
			java.lang.String prefix = xmlWriter.getPrefix(namespace);

			if (prefix == null) {
				prefix = generatePrefix(namespace);

				while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
					prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
				}

				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);
			}

			return prefix;
		}

		/**
		 * databinding method to get an XML representation of this object
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(final javax.xml.namespace.QName qName) throws org.apache.axis2.databinding.ADBException {

			// We can safely assume an element has only one type associated with it
			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(MY_QNAME, new java.lang.Object[] {
					org.apache.axis2.databinding.utils.reader.ADBXMLStreamReader.ELEMENT_TEXT,
					org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPostGet_type0) }, null);

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			public static PostGet_type1 fromString(final java.lang.String value, final java.lang.String namespaceURI) {
				final PostGet_type1 returnValue = new PostGet_type1();

				returnValue.setPostGet_type0(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(value));

				return returnValue;
			}

			public static PostGet_type1 fromString(final javax.xml.stream.XMLStreamReader xmlStreamReader, final java.lang.String content) {
				if (content.indexOf(":") > -1) {
					final java.lang.String prefix = content.substring(0, content.indexOf(":"));
					final java.lang.String namespaceUri = xmlStreamReader.getNamespaceContext().getNamespaceURI(prefix);
					return PostGet_type1.Factory.fromString(content, namespaceUri);
				} else {
					return PostGet_type1.Factory.fromString(content, "");
				}
			}

			/**
			 * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening
			 * reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
			 * Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the
			 * end element of its outer element
			 */
			public static PostGet_type1 parse(final javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
				final PostGet_type1 object = new PostGet_type1();

				final int event;
				final java.lang.String nillableValue = null;
				final java.lang.String prefix = "";
				final java.lang.String namespaceuri = "";
				try {

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					// Note all attributes that were handled. Used to differ normal attributes
					// from anyAttributes.

					while (!reader.isEndElement()) {
						if (reader.isStartElement() || reader.hasText()) {

							if (reader.isStartElement() || reader.hasText()) {

								final java.lang.String content = reader.getElementText();

								object.setPostGet_type0(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

							} // End of if for expected property start element

							else {
								// A start element we are not expecting indicates an invalid parameter was passed
								throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
							}

						} else {
							reader.next();
						}
					} // end of while loop

				} catch (final javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class ArmInputElement implements org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/", "armInputElement", "ns1");

		private static java.lang.String generatePrefix(final java.lang.String namespace) {
			if (namespace.equals("http://localhost:8080/axis2_tester/services/")) { return "ns1"; }
			return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
		}

		/**
		 * field for ArmInputElement
		 */

		protected ArmSitePayload localArmInputElement;

		/**
		 * Auto generated getter method
		 * 
		 * @return ArmSitePayload
		 */
		public ArmSitePayload getArmInputElement() {
			return localArmInputElement;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param ArmInputElement
		 */
		public void setArmInputElement(final ArmSitePayload param) {

			this.localArmInputElement = param;

		}

		/**
		 * isReaderMTOMAware
		 * 
		 * @return true if the reader supports MTOM
		 */
		public static boolean isReaderMTOMAware(final javax.xml.stream.XMLStreamReader reader) {
			boolean isReaderMTOMAware = false;

			try {
				isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
			} catch (final java.lang.IllegalArgumentException e) {
				isReaderMTOMAware = false;
			}
			return isReaderMTOMAware;
		}

		/**
		 * @param parentQName
		 * @param factory
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getOMElement(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory)
				throws org.apache.axis2.databinding.ADBException {

			final org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this, MY_QNAME) {

				public void serialize(final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
					ArmInputElement.this.serialize(MY_QNAME, factory, xmlWriter);
				}
			};
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(MY_QNAME, factory, dataSource);

		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException,
				org.apache.axis2.databinding.ADBException {
			serialize(parentQName, factory, xmlWriter, false);
		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter, final boolean serializeType)
				throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

			// We can safely assume an element has only one type associated with it

			if (localArmInputElement == null) { throw new org.apache.axis2.databinding.ADBException("Property cannot be null!"); }
			localArmInputElement.serialize(MY_QNAME, factory, xmlWriter);

		}

		/**
		 * Util method to write an attribute with the ns prefix
		 */
		private void writeAttribute(final java.lang.String prefix, final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (xmlWriter.getPrefix(namespace) == null) {
				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);

			}

			xmlWriter.writeAttribute(namespace, attName, attValue);

		}

		/**
		 * Util method to write an attribute without the ns prefix
		 */
		private void writeAttribute(final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (namespace.equals("")) {
				xmlWriter.writeAttribute(attName, attValue);
			} else {
				registerPrefix(xmlWriter, namespace);
				xmlWriter.writeAttribute(namespace, attName, attValue);
			}
		}

		/**
		 * Util method to write an attribute without the ns prefix
		 */
		private void writeQNameAttribute(final java.lang.String namespace, final java.lang.String attName, final javax.xml.namespace.QName qname,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

			final java.lang.String attributeNamespace = qname.getNamespaceURI();
			java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
			if (attributePrefix == null) {
				attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
			}
			java.lang.String attributeValue;
			if (attributePrefix.trim().length() > 0) {
				attributeValue = attributePrefix + ":" + qname.getLocalPart();
			} else {
				attributeValue = qname.getLocalPart();
			}

			if (namespace.equals("")) {
				xmlWriter.writeAttribute(attName, attributeValue);
			} else {
				registerPrefix(xmlWriter, namespace);
				xmlWriter.writeAttribute(namespace, attName, attributeValue);
			}
		}

		/**
		 * method to handle Qnames
		 */

		private void writeQName(final javax.xml.namespace.QName qname, final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			final java.lang.String namespaceURI = qname.getNamespaceURI();
			if (namespaceURI != null) {
				java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
				if (prefix == null) {
					prefix = generatePrefix(namespaceURI);
					xmlWriter.writeNamespace(prefix, namespaceURI);
					xmlWriter.setPrefix(prefix, namespaceURI);
				}

				if (prefix.trim().length() > 0) {
					xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
				} else {
					// i.e this is the default namespace
					xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
				}

			} else {
				xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
			}
		}

		private void writeQNames(final javax.xml.namespace.QName[] qnames, final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

			if (qnames != null) {
				// we have to store this data until last moment since it is not possible to write any
				// namespace data after writing the charactor data
				final java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
				java.lang.String namespaceURI = null;
				java.lang.String prefix = null;

				for (int i = 0; i < qnames.length; i++) {
					if (i > 0) {
						stringToWrite.append(" ");
					}
					namespaceURI = qnames[i].getNamespaceURI();
					if (namespaceURI != null) {
						prefix = xmlWriter.getPrefix(namespaceURI);
						if ((prefix == null) || (prefix.length() == 0)) {
							prefix = generatePrefix(namespaceURI);
							xmlWriter.writeNamespace(prefix, namespaceURI);
							xmlWriter.setPrefix(prefix, namespaceURI);
						}

						if (prefix.trim().length() > 0) {
							stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
						} else {
							stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
						}
					} else {
						stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
					}
				}
				xmlWriter.writeCharacters(stringToWrite.toString());
			}

		}

		/**
		 * Register a namespace prefix
		 */
		private java.lang.String registerPrefix(final javax.xml.stream.XMLStreamWriter xmlWriter, final java.lang.String namespace)
				throws javax.xml.stream.XMLStreamException {
			java.lang.String prefix = xmlWriter.getPrefix(namespace);

			if (prefix == null) {
				prefix = generatePrefix(namespace);

				while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
					prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
				}

				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);
			}

			return prefix;
		}

		/**
		 * databinding method to get an XML representation of this object
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(final javax.xml.namespace.QName qName) throws org.apache.axis2.databinding.ADBException {

			// We can safely assume an element has only one type associated with it
			return localArmInputElement.getPullParser(MY_QNAME);

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening
			 * reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
			 * Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the
			 * end element of its outer element
			 */
			public static ArmInputElement parse(final javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
				final ArmInputElement object = new ArmInputElement();

				final int event;
				final java.lang.String nillableValue = null;
				final java.lang.String prefix = "";
				final java.lang.String namespaceuri = "";
				try {

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					// Note all attributes that were handled. Used to differ normal attributes
					// from anyAttributes.

					while (!reader.isEndElement()) {
						if (reader.isStartElement()) {

							if (reader.isStartElement()
									&& new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/", "armInputElement").equals(reader.getName())) {

								object.setArmInputElement(ArmSitePayload.Factory.parse(reader));

							} // End of if for expected property start element

							else {
								// A start element we are not expecting indicates an invalid parameter was passed
								throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
							}

						} else {
							reader.next();
						}
					} // end of while loop

				} catch (final javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class ArmOutputElement implements org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/", "armOutputElement", "ns1");

		private static java.lang.String generatePrefix(final java.lang.String namespace) {
			if (namespace.equals("http://localhost:8080/axis2_tester/services/")) { return "ns1"; }
			return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
		}

		/**
		 * field for ArmOutputElement
		 */

		protected ArmSiteResponsePayload localArmOutputElement;

		/**
		 * Auto generated getter method
		 * 
		 * @return ArmSiteResponsePayload
		 */
		public ArmSiteResponsePayload getArmOutputElement() {
			return localArmOutputElement;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param ArmOutputElement
		 */
		public void setArmOutputElement(final ArmSiteResponsePayload param) {

			this.localArmOutputElement = param;

		}

		/**
		 * isReaderMTOMAware
		 * 
		 * @return true if the reader supports MTOM
		 */
		public static boolean isReaderMTOMAware(final javax.xml.stream.XMLStreamReader reader) {
			boolean isReaderMTOMAware = false;

			try {
				isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
			} catch (final java.lang.IllegalArgumentException e) {
				isReaderMTOMAware = false;
			}
			return isReaderMTOMAware;
		}

		/**
		 * @param parentQName
		 * @param factory
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getOMElement(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory)
				throws org.apache.axis2.databinding.ADBException {

			final org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this, MY_QNAME) {

				public void serialize(final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
					ArmOutputElement.this.serialize(MY_QNAME, factory, xmlWriter);
				}
			};
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(MY_QNAME, factory, dataSource);

		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException,
				org.apache.axis2.databinding.ADBException {
			serialize(parentQName, factory, xmlWriter, false);
		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter, final boolean serializeType)
				throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

			// We can safely assume an element has only one type associated with it

			if (localArmOutputElement == null) { throw new org.apache.axis2.databinding.ADBException("Property cannot be null!"); }
			localArmOutputElement.serialize(MY_QNAME, factory, xmlWriter);

		}

		/**
		 * Util method to write an attribute with the ns prefix
		 */
		private void writeAttribute(final java.lang.String prefix, final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (xmlWriter.getPrefix(namespace) == null) {
				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);

			}

			xmlWriter.writeAttribute(namespace, attName, attValue);

		}

		/**
		 * Util method to write an attribute without the ns prefix
		 */
		private void writeAttribute(final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (namespace.equals("")) {
				xmlWriter.writeAttribute(attName, attValue);
			} else {
				registerPrefix(xmlWriter, namespace);
				xmlWriter.writeAttribute(namespace, attName, attValue);
			}
		}

		/**
		 * Util method to write an attribute without the ns prefix
		 */
		private void writeQNameAttribute(final java.lang.String namespace, final java.lang.String attName, final javax.xml.namespace.QName qname,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

			final java.lang.String attributeNamespace = qname.getNamespaceURI();
			java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
			if (attributePrefix == null) {
				attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
			}
			java.lang.String attributeValue;
			if (attributePrefix.trim().length() > 0) {
				attributeValue = attributePrefix + ":" + qname.getLocalPart();
			} else {
				attributeValue = qname.getLocalPart();
			}

			if (namespace.equals("")) {
				xmlWriter.writeAttribute(attName, attributeValue);
			} else {
				registerPrefix(xmlWriter, namespace);
				xmlWriter.writeAttribute(namespace, attName, attributeValue);
			}
		}

		/**
		 * method to handle Qnames
		 */

		private void writeQName(final javax.xml.namespace.QName qname, final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			final java.lang.String namespaceURI = qname.getNamespaceURI();
			if (namespaceURI != null) {
				java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
				if (prefix == null) {
					prefix = generatePrefix(namespaceURI);
					xmlWriter.writeNamespace(prefix, namespaceURI);
					xmlWriter.setPrefix(prefix, namespaceURI);
				}

				if (prefix.trim().length() > 0) {
					xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
				} else {
					// i.e this is the default namespace
					xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
				}

			} else {
				xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
			}
		}

		private void writeQNames(final javax.xml.namespace.QName[] qnames, final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

			if (qnames != null) {
				// we have to store this data until last moment since it is not possible to write any
				// namespace data after writing the charactor data
				final java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
				java.lang.String namespaceURI = null;
				java.lang.String prefix = null;

				for (int i = 0; i < qnames.length; i++) {
					if (i > 0) {
						stringToWrite.append(" ");
					}
					namespaceURI = qnames[i].getNamespaceURI();
					if (namespaceURI != null) {
						prefix = xmlWriter.getPrefix(namespaceURI);
						if ((prefix == null) || (prefix.length() == 0)) {
							prefix = generatePrefix(namespaceURI);
							xmlWriter.writeNamespace(prefix, namespaceURI);
							xmlWriter.setPrefix(prefix, namespaceURI);
						}

						if (prefix.trim().length() > 0) {
							stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
						} else {
							stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
						}
					} else {
						stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
					}
				}
				xmlWriter.writeCharacters(stringToWrite.toString());
			}

		}

		/**
		 * Register a namespace prefix
		 */
		private java.lang.String registerPrefix(final javax.xml.stream.XMLStreamWriter xmlWriter, final java.lang.String namespace)
				throws javax.xml.stream.XMLStreamException {
			java.lang.String prefix = xmlWriter.getPrefix(namespace);

			if (prefix == null) {
				prefix = generatePrefix(namespace);

				while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
					prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
				}

				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);
			}

			return prefix;
		}

		/**
		 * databinding method to get an XML representation of this object
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(final javax.xml.namespace.QName qName) throws org.apache.axis2.databinding.ADBException {

			// We can safely assume an element has only one type associated with it
			return localArmOutputElement.getPullParser(MY_QNAME);

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening
			 * reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
			 * Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the
			 * end element of its outer element
			 */
			public static ArmOutputElement parse(final javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
				final ArmOutputElement object = new ArmOutputElement();

				final int event;
				final java.lang.String nillableValue = null;
				final java.lang.String prefix = "";
				final java.lang.String namespaceuri = "";
				try {

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					// Note all attributes that were handled. Used to differ normal attributes
					// from anyAttributes.

					while (!reader.isEndElement()) {
						if (reader.isStartElement()) {

							if (reader.isStartElement()
									&& new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/", "armOutputElement").equals(reader.getName())) {

								object.setArmOutputElement(ArmSiteResponsePayload.Factory.parse(reader));

							} // End of if for expected property start element

							else {
								// A start element we are not expecting indicates an invalid parameter was passed
								throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
							}

						} else {
							reader.next();
						}
					} // end of while loop

				} catch (final javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class ResultValuesResponse implements org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/", "resultValuesResponse",
				"ns1");

		private static java.lang.String generatePrefix(final java.lang.String namespace) {
			if (namespace.equals("http://localhost:8080/axis2_tester/services/")) { return "ns1"; }
			return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
		}

		/**
		 * field for ResultValuesResponse
		 */

		protected ResultValuesOutput localResultValuesResponse;

		/**
		 * Auto generated getter method
		 * 
		 * @return ResultValuesOutput
		 */
		public ResultValuesOutput getResultValuesResponse() {
			return localResultValuesResponse;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param ResultValuesResponse
		 */
		public void setResultValuesResponse(final ResultValuesOutput param) {

			this.localResultValuesResponse = param;

		}

		/**
		 * isReaderMTOMAware
		 * 
		 * @return true if the reader supports MTOM
		 */
		public static boolean isReaderMTOMAware(final javax.xml.stream.XMLStreamReader reader) {
			boolean isReaderMTOMAware = false;

			try {
				isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
			} catch (final java.lang.IllegalArgumentException e) {
				isReaderMTOMAware = false;
			}
			return isReaderMTOMAware;
		}

		/**
		 * @param parentQName
		 * @param factory
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getOMElement(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory)
				throws org.apache.axis2.databinding.ADBException {

			final org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this, MY_QNAME) {

				public void serialize(final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
					ResultValuesResponse.this.serialize(MY_QNAME, factory, xmlWriter);
				}
			};
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(MY_QNAME, factory, dataSource);

		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException,
				org.apache.axis2.databinding.ADBException {
			serialize(parentQName, factory, xmlWriter, false);
		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter, final boolean serializeType)
				throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

			// We can safely assume an element has only one type associated with it

			if (localResultValuesResponse == null) { throw new org.apache.axis2.databinding.ADBException("Property cannot be null!"); }
			localResultValuesResponse.serialize(MY_QNAME, factory, xmlWriter);

		}

		/**
		 * Util method to write an attribute with the ns prefix
		 */
		private void writeAttribute(final java.lang.String prefix, final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (xmlWriter.getPrefix(namespace) == null) {
				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);

			}

			xmlWriter.writeAttribute(namespace, attName, attValue);

		}

		/**
		 * Util method to write an attribute without the ns prefix
		 */
		private void writeAttribute(final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (namespace.equals("")) {
				xmlWriter.writeAttribute(attName, attValue);
			} else {
				registerPrefix(xmlWriter, namespace);
				xmlWriter.writeAttribute(namespace, attName, attValue);
			}
		}

		/**
		 * Util method to write an attribute without the ns prefix
		 */
		private void writeQNameAttribute(final java.lang.String namespace, final java.lang.String attName, final javax.xml.namespace.QName qname,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

			final java.lang.String attributeNamespace = qname.getNamespaceURI();
			java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
			if (attributePrefix == null) {
				attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
			}
			java.lang.String attributeValue;
			if (attributePrefix.trim().length() > 0) {
				attributeValue = attributePrefix + ":" + qname.getLocalPart();
			} else {
				attributeValue = qname.getLocalPart();
			}

			if (namespace.equals("")) {
				xmlWriter.writeAttribute(attName, attributeValue);
			} else {
				registerPrefix(xmlWriter, namespace);
				xmlWriter.writeAttribute(namespace, attName, attributeValue);
			}
		}

		/**
		 * method to handle Qnames
		 */

		private void writeQName(final javax.xml.namespace.QName qname, final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			final java.lang.String namespaceURI = qname.getNamespaceURI();
			if (namespaceURI != null) {
				java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
				if (prefix == null) {
					prefix = generatePrefix(namespaceURI);
					xmlWriter.writeNamespace(prefix, namespaceURI);
					xmlWriter.setPrefix(prefix, namespaceURI);
				}

				if (prefix.trim().length() > 0) {
					xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
				} else {
					// i.e this is the default namespace
					xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
				}

			} else {
				xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
			}
		}

		private void writeQNames(final javax.xml.namespace.QName[] qnames, final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

			if (qnames != null) {
				// we have to store this data until last moment since it is not possible to write any
				// namespace data after writing the charactor data
				final java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
				java.lang.String namespaceURI = null;
				java.lang.String prefix = null;

				for (int i = 0; i < qnames.length; i++) {
					if (i > 0) {
						stringToWrite.append(" ");
					}
					namespaceURI = qnames[i].getNamespaceURI();
					if (namespaceURI != null) {
						prefix = xmlWriter.getPrefix(namespaceURI);
						if ((prefix == null) || (prefix.length() == 0)) {
							prefix = generatePrefix(namespaceURI);
							xmlWriter.writeNamespace(prefix, namespaceURI);
							xmlWriter.setPrefix(prefix, namespaceURI);
						}

						if (prefix.trim().length() > 0) {
							stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
						} else {
							stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
						}
					} else {
						stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
					}
				}
				xmlWriter.writeCharacters(stringToWrite.toString());
			}

		}

		/**
		 * Register a namespace prefix
		 */
		private java.lang.String registerPrefix(final javax.xml.stream.XMLStreamWriter xmlWriter, final java.lang.String namespace)
				throws javax.xml.stream.XMLStreamException {
			java.lang.String prefix = xmlWriter.getPrefix(namespace);

			if (prefix == null) {
				prefix = generatePrefix(namespace);

				while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
					prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
				}

				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);
			}

			return prefix;
		}

		/**
		 * databinding method to get an XML representation of this object
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(final javax.xml.namespace.QName qName) throws org.apache.axis2.databinding.ADBException {

			// We can safely assume an element has only one type associated with it
			return localResultValuesResponse.getPullParser(MY_QNAME);

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening
			 * reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
			 * Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the
			 * end element of its outer element
			 */
			public static ResultValuesResponse parse(final javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
				final ResultValuesResponse object = new ResultValuesResponse();

				final int event;
				final java.lang.String nillableValue = null;
				final java.lang.String prefix = "";
				final java.lang.String namespaceuri = "";
				try {

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					// Note all attributes that were handled. Used to differ normal attributes
					// from anyAttributes.

					while (!reader.isEndElement()) {
						if (reader.isStartElement()) {

							if (reader.isStartElement()
									&& new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/", "resultValuesResponse").equals(reader.getName())) {

								object.setResultValuesResponse(ResultValuesOutput.Factory.parse(reader));

							} // End of if for expected property start element

							else {
								// A start element we are not expecting indicates an invalid parameter was passed
								throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
							}

						} else {
							reader.next();
						}
					} // end of while loop

				} catch (final javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class InputPartElement implements org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/", "inputPartElement", "ns1");

		private static java.lang.String generatePrefix(final java.lang.String namespace) {
			if (namespace.equals("http://localhost:8080/axis2_tester/services/")) { return "ns1"; }
			return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
		}

		/**
		 * field for InputPartElement
		 */

		protected InputPartPayload localInputPartElement;

		/**
		 * Auto generated getter method
		 * 
		 * @return InputPartPayload
		 */
		public InputPartPayload getInputPartElement() {
			return localInputPartElement;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param InputPartElement
		 */
		public void setInputPartElement(final InputPartPayload param) {

			this.localInputPartElement = param;

		}

		/**
		 * isReaderMTOMAware
		 * 
		 * @return true if the reader supports MTOM
		 */
		public static boolean isReaderMTOMAware(final javax.xml.stream.XMLStreamReader reader) {
			boolean isReaderMTOMAware = false;

			try {
				isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
			} catch (final java.lang.IllegalArgumentException e) {
				isReaderMTOMAware = false;
			}
			return isReaderMTOMAware;
		}

		/**
		 * @param parentQName
		 * @param factory
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getOMElement(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory)
				throws org.apache.axis2.databinding.ADBException {

			final org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this, MY_QNAME) {

				public void serialize(final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
					InputPartElement.this.serialize(MY_QNAME, factory, xmlWriter);
				}
			};
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(MY_QNAME, factory, dataSource);

		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException,
				org.apache.axis2.databinding.ADBException {
			serialize(parentQName, factory, xmlWriter, false);
		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter, final boolean serializeType)
				throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

			// We can safely assume an element has only one type associated with it

			if (localInputPartElement == null) { throw new org.apache.axis2.databinding.ADBException("Property cannot be null!"); }
			localInputPartElement.serialize(MY_QNAME, factory, xmlWriter);

		}

		/**
		 * Util method to write an attribute with the ns prefix
		 */
		private void writeAttribute(final java.lang.String prefix, final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (xmlWriter.getPrefix(namespace) == null) {
				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);

			}

			xmlWriter.writeAttribute(namespace, attName, attValue);

		}

		/**
		 * Util method to write an attribute without the ns prefix
		 */
		private void writeAttribute(final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (namespace.equals("")) {
				xmlWriter.writeAttribute(attName, attValue);
			} else {
				registerPrefix(xmlWriter, namespace);
				xmlWriter.writeAttribute(namespace, attName, attValue);
			}
		}

		/**
		 * Util method to write an attribute without the ns prefix
		 */
		private void writeQNameAttribute(final java.lang.String namespace, final java.lang.String attName, final javax.xml.namespace.QName qname,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

			final java.lang.String attributeNamespace = qname.getNamespaceURI();
			java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
			if (attributePrefix == null) {
				attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
			}
			java.lang.String attributeValue;
			if (attributePrefix.trim().length() > 0) {
				attributeValue = attributePrefix + ":" + qname.getLocalPart();
			} else {
				attributeValue = qname.getLocalPart();
			}

			if (namespace.equals("")) {
				xmlWriter.writeAttribute(attName, attributeValue);
			} else {
				registerPrefix(xmlWriter, namespace);
				xmlWriter.writeAttribute(namespace, attName, attributeValue);
			}
		}

		/**
		 * method to handle Qnames
		 */

		private void writeQName(final javax.xml.namespace.QName qname, final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			final java.lang.String namespaceURI = qname.getNamespaceURI();
			if (namespaceURI != null) {
				java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
				if (prefix == null) {
					prefix = generatePrefix(namespaceURI);
					xmlWriter.writeNamespace(prefix, namespaceURI);
					xmlWriter.setPrefix(prefix, namespaceURI);
				}

				if (prefix.trim().length() > 0) {
					xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
				} else {
					// i.e this is the default namespace
					xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
				}

			} else {
				xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
			}
		}

		private void writeQNames(final javax.xml.namespace.QName[] qnames, final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

			if (qnames != null) {
				// we have to store this data until last moment since it is not possible to write any
				// namespace data after writing the charactor data
				final java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
				java.lang.String namespaceURI = null;
				java.lang.String prefix = null;

				for (int i = 0; i < qnames.length; i++) {
					if (i > 0) {
						stringToWrite.append(" ");
					}
					namespaceURI = qnames[i].getNamespaceURI();
					if (namespaceURI != null) {
						prefix = xmlWriter.getPrefix(namespaceURI);
						if ((prefix == null) || (prefix.length() == 0)) {
							prefix = generatePrefix(namespaceURI);
							xmlWriter.writeNamespace(prefix, namespaceURI);
							xmlWriter.setPrefix(prefix, namespaceURI);
						}

						if (prefix.trim().length() > 0) {
							stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
						} else {
							stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
						}
					} else {
						stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
					}
				}
				xmlWriter.writeCharacters(stringToWrite.toString());
			}

		}

		/**
		 * Register a namespace prefix
		 */
		private java.lang.String registerPrefix(final javax.xml.stream.XMLStreamWriter xmlWriter, final java.lang.String namespace)
				throws javax.xml.stream.XMLStreamException {
			java.lang.String prefix = xmlWriter.getPrefix(namespace);

			if (prefix == null) {
				prefix = generatePrefix(namespace);

				while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
					prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
				}

				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);
			}

			return prefix;
		}

		/**
		 * databinding method to get an XML representation of this object
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(final javax.xml.namespace.QName qName) throws org.apache.axis2.databinding.ADBException {

			// We can safely assume an element has only one type associated with it
			return localInputPartElement.getPullParser(MY_QNAME);

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening
			 * reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
			 * Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the
			 * end element of its outer element
			 */
			public static InputPartElement parse(final javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
				final InputPartElement object = new InputPartElement();

				final int event;
				final java.lang.String nillableValue = null;
				final java.lang.String prefix = "";
				final java.lang.String namespaceuri = "";
				try {

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					// Note all attributes that were handled. Used to differ normal attributes
					// from anyAttributes.

					while (!reader.isEndElement()) {
						if (reader.isStartElement()) {

							if (reader.isStartElement()
									&& new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/", "inputPartElement").equals(reader.getName())) {

								object.setInputPartElement(InputPartPayload.Factory.parse(reader));

							} // End of if for expected property start element

							else {
								// A start element we are not expecting indicates an invalid parameter was passed
								throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
							}

						} else {
							reader.next();
						}
					} // end of while loop

				} catch (final javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class ArmSiteResponsePayload implements org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name = armSiteResponsePayload Namespace URI = http://localhost:8080/axis2_tester/services/ Namespace
		 * Prefix = ns1
		 */

		private static java.lang.String generatePrefix(final java.lang.String namespace) {
			if (namespace.equals("http://localhost:8080/axis2_tester/services/")) { return "ns1"; }
			return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
		}

		/**
		 * field for UserSessionId
		 */

		protected java.lang.String localUserSessionId;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getUserSessionId() {
			return localUserSessionId;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param UserSessionId
		 */
		public void setUserSessionId(final java.lang.String param) {

			this.localUserSessionId = param;

		}

		/**
		 * isReaderMTOMAware
		 * 
		 * @return true if the reader supports MTOM
		 */
		public static boolean isReaderMTOMAware(final javax.xml.stream.XMLStreamReader reader) {
			boolean isReaderMTOMAware = false;

			try {
				isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
			} catch (final java.lang.IllegalArgumentException e) {
				isReaderMTOMAware = false;
			}
			return isReaderMTOMAware;
		}

		/**
		 * @param parentQName
		 * @param factory
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getOMElement(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory)
				throws org.apache.axis2.databinding.ADBException {

			final org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this, parentQName) {

				public void serialize(final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
					ArmSiteResponsePayload.this.serialize(parentQName, factory, xmlWriter);
				}
			};
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(parentQName, factory, dataSource);

		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException,
				org.apache.axis2.databinding.ADBException {
			serialize(parentQName, factory, xmlWriter, false);
		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter, final boolean serializeType)
				throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

			java.lang.String prefix = null;
			java.lang.String namespace = null;

			prefix = parentQName.getPrefix();
			namespace = parentQName.getNamespaceURI();

			if ((namespace != null) && (namespace.trim().length() > 0)) {
				final java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
				if (writerPrefix != null) {
					xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
				} else {
					if (prefix == null) {
						prefix = generatePrefix(namespace);
					}

					xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
					xmlWriter.writeNamespace(prefix, namespace);
					xmlWriter.setPrefix(prefix, namespace);
				}
			} else {
				xmlWriter.writeStartElement(parentQName.getLocalPart());
			}

			if (serializeType) {

				final java.lang.String namespacePrefix = registerPrefix(xmlWriter, "http://localhost:8080/axis2_tester/services/");
				if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)) {
					writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix + ":armSiteResponsePayload", xmlWriter);
				} else {
					writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "armSiteResponsePayload", xmlWriter);
				}

			}

			namespace = "";
			if (!namespace.equals("")) {
				prefix = xmlWriter.getPrefix(namespace);

				if (prefix == null) {
					prefix = generatePrefix(namespace);

					xmlWriter.writeStartElement(prefix, "userSessionId", namespace);
					xmlWriter.writeNamespace(prefix, namespace);
					xmlWriter.setPrefix(prefix, namespace);

				} else {
					xmlWriter.writeStartElement(namespace, "userSessionId");
				}

			} else {
				xmlWriter.writeStartElement("userSessionId");
			}

			if (localUserSessionId == null) {
				// write the nil attribute

				throw new org.apache.axis2.databinding.ADBException("userSessionId cannot be null!!");

			} else {

				xmlWriter.writeCharacters(localUserSessionId);

			}

			xmlWriter.writeEndElement();

			xmlWriter.writeEndElement();

		}

		/**
		 * Util method to write an attribute with the ns prefix
		 */
		private void writeAttribute(final java.lang.String prefix, final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (xmlWriter.getPrefix(namespace) == null) {
				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);

			}

			xmlWriter.writeAttribute(namespace, attName, attValue);

		}

		/**
		 * Register a namespace prefix
		 */
		private java.lang.String registerPrefix(final javax.xml.stream.XMLStreamWriter xmlWriter, final java.lang.String namespace)
				throws javax.xml.stream.XMLStreamException {
			java.lang.String prefix = xmlWriter.getPrefix(namespace);

			if (prefix == null) {
				prefix = generatePrefix(namespace);

				while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
					prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
				}

				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);
			}

			return prefix;
		}

		/**
		 * databinding method to get an XML representation of this object
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(final javax.xml.namespace.QName qName) throws org.apache.axis2.databinding.ADBException {

			final java.util.ArrayList elementList = new java.util.ArrayList();
			final java.util.ArrayList attribList = new java.util.ArrayList();

			elementList.add(new javax.xml.namespace.QName("", "userSessionId"));

			if (localUserSessionId != null) {
				elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUserSessionId));
			} else {
				throw new org.apache.axis2.databinding.ADBException("userSessionId cannot be null!!");
			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening
			 * reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
			 * Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the
			 * end element of its outer element
			 */
			public static ArmSiteResponsePayload parse(final javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
				final ArmSiteResponsePayload object = new ArmSiteResponsePayload();

				final int event;
				final java.lang.String nillableValue = null;
				final java.lang.String prefix = "";
				final java.lang.String namespaceuri = "";
				try {

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type") != null) {
						final java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = null;
							if (fullTypeName.indexOf(":") > -1) {
								nsPrefix = fullTypeName.substring(0, fullTypeName.indexOf(":"));
							}
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							final java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":") + 1);

							if (!"armSiteResponsePayload".equals(type)) {
								// find namespace for the prefix
								final java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
								return (ArmSiteResponsePayload) ExtensionMapper.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ normal attributes
					// from anyAttributes.

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "userSessionId").equals(reader.getName())) {

						final java.lang.String content = reader.getElementText();

						object.setUserSessionId(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an invalid parameter was passed
						throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement()) {
						// A start element we are not expecting indicates a trailing invalid property
						throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
					}

				} catch (final javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class ResultValuesInputE implements org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/", "resultValuesInput", "ns1");

		private static java.lang.String generatePrefix(final java.lang.String namespace) {
			if (namespace.equals("http://localhost:8080/axis2_tester/services/")) { return "ns1"; }
			return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
		}

		/**
		 * field for ResultValuesInput
		 */

		protected ResultValuesInput localResultValuesInput;

		/**
		 * Auto generated getter method
		 * 
		 * @return ResultValuesInput
		 */
		public ResultValuesInput getResultValuesInput() {
			return localResultValuesInput;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param ResultValuesInput
		 */
		public void setResultValuesInput(final ResultValuesInput param) {

			this.localResultValuesInput = param;

		}

		/**
		 * isReaderMTOMAware
		 * 
		 * @return true if the reader supports MTOM
		 */
		public static boolean isReaderMTOMAware(final javax.xml.stream.XMLStreamReader reader) {
			boolean isReaderMTOMAware = false;

			try {
				isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
			} catch (final java.lang.IllegalArgumentException e) {
				isReaderMTOMAware = false;
			}
			return isReaderMTOMAware;
		}

		/**
		 * @param parentQName
		 * @param factory
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getOMElement(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory)
				throws org.apache.axis2.databinding.ADBException {

			final org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this, MY_QNAME) {

				public void serialize(final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
					ResultValuesInputE.this.serialize(MY_QNAME, factory, xmlWriter);
				}
			};
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(MY_QNAME, factory, dataSource);

		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException,
				org.apache.axis2.databinding.ADBException {
			serialize(parentQName, factory, xmlWriter, false);
		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter, final boolean serializeType)
				throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

			// We can safely assume an element has only one type associated with it

			if (localResultValuesInput == null) { throw new org.apache.axis2.databinding.ADBException("Property cannot be null!"); }
			localResultValuesInput.serialize(MY_QNAME, factory, xmlWriter);

		}

		/**
		 * Util method to write an attribute with the ns prefix
		 */
		private void writeAttribute(final java.lang.String prefix, final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (xmlWriter.getPrefix(namespace) == null) {
				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);

			}

			xmlWriter.writeAttribute(namespace, attName, attValue);

		}

		/**
		 * Util method to write an attribute without the ns prefix
		 */
		private void writeAttribute(final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (namespace.equals("")) {
				xmlWriter.writeAttribute(attName, attValue);
			} else {
				registerPrefix(xmlWriter, namespace);
				xmlWriter.writeAttribute(namespace, attName, attValue);
			}
		}

		/**
		 * Util method to write an attribute without the ns prefix
		 */
		private void writeQNameAttribute(final java.lang.String namespace, final java.lang.String attName, final javax.xml.namespace.QName qname,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

			final java.lang.String attributeNamespace = qname.getNamespaceURI();
			java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
			if (attributePrefix == null) {
				attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
			}
			java.lang.String attributeValue;
			if (attributePrefix.trim().length() > 0) {
				attributeValue = attributePrefix + ":" + qname.getLocalPart();
			} else {
				attributeValue = qname.getLocalPart();
			}

			if (namespace.equals("")) {
				xmlWriter.writeAttribute(attName, attributeValue);
			} else {
				registerPrefix(xmlWriter, namespace);
				xmlWriter.writeAttribute(namespace, attName, attributeValue);
			}
		}

		/**
		 * method to handle Qnames
		 */

		private void writeQName(final javax.xml.namespace.QName qname, final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			final java.lang.String namespaceURI = qname.getNamespaceURI();
			if (namespaceURI != null) {
				java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
				if (prefix == null) {
					prefix = generatePrefix(namespaceURI);
					xmlWriter.writeNamespace(prefix, namespaceURI);
					xmlWriter.setPrefix(prefix, namespaceURI);
				}

				if (prefix.trim().length() > 0) {
					xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
				} else {
					// i.e this is the default namespace
					xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
				}

			} else {
				xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
			}
		}

		private void writeQNames(final javax.xml.namespace.QName[] qnames, final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

			if (qnames != null) {
				// we have to store this data until last moment since it is not possible to write any
				// namespace data after writing the charactor data
				final java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
				java.lang.String namespaceURI = null;
				java.lang.String prefix = null;

				for (int i = 0; i < qnames.length; i++) {
					if (i > 0) {
						stringToWrite.append(" ");
					}
					namespaceURI = qnames[i].getNamespaceURI();
					if (namespaceURI != null) {
						prefix = xmlWriter.getPrefix(namespaceURI);
						if ((prefix == null) || (prefix.length() == 0)) {
							prefix = generatePrefix(namespaceURI);
							xmlWriter.writeNamespace(prefix, namespaceURI);
							xmlWriter.setPrefix(prefix, namespaceURI);
						}

						if (prefix.trim().length() > 0) {
							stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
						} else {
							stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
						}
					} else {
						stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
					}
				}
				xmlWriter.writeCharacters(stringToWrite.toString());
			}

		}

		/**
		 * Register a namespace prefix
		 */
		private java.lang.String registerPrefix(final javax.xml.stream.XMLStreamWriter xmlWriter, final java.lang.String namespace)
				throws javax.xml.stream.XMLStreamException {
			java.lang.String prefix = xmlWriter.getPrefix(namespace);

			if (prefix == null) {
				prefix = generatePrefix(namespace);

				while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
					prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
				}

				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);
			}

			return prefix;
		}

		/**
		 * databinding method to get an XML representation of this object
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(final javax.xml.namespace.QName qName) throws org.apache.axis2.databinding.ADBException {

			// We can safely assume an element has only one type associated with it
			return localResultValuesInput.getPullParser(MY_QNAME);

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening
			 * reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
			 * Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the
			 * end element of its outer element
			 */
			public static ResultValuesInputE parse(final javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
				final ResultValuesInputE object = new ResultValuesInputE();

				final int event;
				final java.lang.String nillableValue = null;
				final java.lang.String prefix = "";
				final java.lang.String namespaceuri = "";
				try {

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					// Note all attributes that were handled. Used to differ normal attributes
					// from anyAttributes

					while (!reader.isEndElement()) {
						if (reader.isStartElement()) {

							if (reader.isStartElement()
									&& new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/", "resultValuesInput").equals(reader.getName())) {

								object.setResultValuesInput(ResultValuesInput.Factory.parse(reader));

							} // End of if for expected property start element

							else {
								// A start element we are not expecting indicates an invalid parameter was passed
								throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
							}

						} else {
							reader.next();
						}
					} // end of while loop

				} catch (final javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class ResultValuesOutput implements org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name = resultValuesOutput Namespace URI = http://localhost:8080/axis2_tester/services/ Namespace
		 * Prefix = ns1
		 */

		private static java.lang.String generatePrefix(final java.lang.String namespace) {
			if (namespace.equals("http://localhost:8080/axis2_tester/services/")) { return "ns1"; }
			return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
		}

		/**
		 * field for ReturnValues This was an Array!
		 */

		protected PostGetParams[] localReturnValues;

		/**
		 * Auto generated getter method
		 * 
		 * @return PostGetParams[]
		 */
		public PostGetParams[] getReturnValues() {
			return localReturnValues;
		}

		/**
		 * validate the array for ReturnValues
		 */
		protected void validateReturnValues(final PostGetParams[] param) {

			if ((param != null) && (param.length < 1)) { throw new java.lang.RuntimeException(); }

		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param ReturnValues
		 */
		public void setReturnValues(final PostGetParams[] param) {

			validateReturnValues(param);

			this.localReturnValues = param;
		}

		/**
		 * Auto generated add method for the array for convenience
		 * 
		 * @param param PostGetParams
		 */
		public void addReturnValues(final PostGetParams param) {
			if (localReturnValues == null) {
				localReturnValues = new PostGetParams[] {};
			}

			final java.util.List list = org.apache.axis2.databinding.utils.ConverterUtil.toList(localReturnValues);
			list.add(param);
			this.localReturnValues = (PostGetParams[]) list.toArray(new PostGetParams[list.size()]);

		}

		/**
		 * isReaderMTOMAware
		 * 
		 * @return true if the reader supports MTOM
		 */
		public static boolean isReaderMTOMAware(final javax.xml.stream.XMLStreamReader reader) {
			boolean isReaderMTOMAware = false;

			try {
				isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
			} catch (final java.lang.IllegalArgumentException e) {
				isReaderMTOMAware = false;
			}
			return isReaderMTOMAware;
		}

		/**
		 * @param parentQName
		 * @param factory
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getOMElement(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory)
				throws org.apache.axis2.databinding.ADBException {

			final org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this, parentQName) {

				public void serialize(final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
					ResultValuesOutput.this.serialize(parentQName, factory, xmlWriter);
				}
			};
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(parentQName, factory, dataSource);

		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException,
				org.apache.axis2.databinding.ADBException {
			serialize(parentQName, factory, xmlWriter, false);
		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter, final boolean serializeType)
				throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

			java.lang.String prefix = null;
			java.lang.String namespace = null;

			prefix = parentQName.getPrefix();
			namespace = parentQName.getNamespaceURI();

			if ((namespace != null) && (namespace.trim().length() > 0)) {
				final java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
				if (writerPrefix != null) {
					xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
				} else {
					if (prefix == null) {
						prefix = generatePrefix(namespace);
					}

					xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
					xmlWriter.writeNamespace(prefix, namespace);
					xmlWriter.setPrefix(prefix, namespace);
				}
			} else {
				xmlWriter.writeStartElement(parentQName.getLocalPart());
			}

			if (serializeType) {

				final java.lang.String namespacePrefix = registerPrefix(xmlWriter, "http://localhost:8080/axis2_tester/services/");
				if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)) {
					writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix + ":resultValuesOutput", xmlWriter);
				} else {
					writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "resultValuesOutput", xmlWriter);
				}

			}

			if (localReturnValues != null) {
				for (int i = 0; i < localReturnValues.length; i++) {
					if (localReturnValues[i] != null) {
						localReturnValues[i].serialize(new javax.xml.namespace.QName("", "returnValues"), factory, xmlWriter);
					} else {

						throw new org.apache.axis2.databinding.ADBException("returnValues cannot be null!!");

					}

				}
			} else {

				throw new org.apache.axis2.databinding.ADBException("returnValues cannot be null!!");

			}

			xmlWriter.writeEndElement();

		}

		/**
		 * Util method to write an attribute with the ns prefix
		 */
		private void writeAttribute(final java.lang.String prefix, final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (xmlWriter.getPrefix(namespace) == null) {
				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);

			}

			xmlWriter.writeAttribute(namespace, attName, attValue);

		}

		/**
		 * Register a namespace prefix
		 */
		private java.lang.String registerPrefix(final javax.xml.stream.XMLStreamWriter xmlWriter, final java.lang.String namespace)
				throws javax.xml.stream.XMLStreamException {
			java.lang.String prefix = xmlWriter.getPrefix(namespace);

			if (prefix == null) {
				prefix = generatePrefix(namespace);

				while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
					prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
				}

				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);
			}

			return prefix;
		}

		/**
		 * databinding method to get an XML representation of this object
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(final javax.xml.namespace.QName qName) throws org.apache.axis2.databinding.ADBException {

			final java.util.ArrayList elementList = new java.util.ArrayList();
			final java.util.ArrayList attribList = new java.util.ArrayList();

			if (localReturnValues != null) {
				for (int i = 0; i < localReturnValues.length; i++) {

					if (localReturnValues[i] != null) {
						elementList.add(new javax.xml.namespace.QName("", "returnValues"));
						elementList.add(localReturnValues[i]);
					} else {

						throw new org.apache.axis2.databinding.ADBException("returnValues cannot be null !!");

					}

				}
			} else {

				throw new org.apache.axis2.databinding.ADBException("returnValues cannot be null!!");

			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening
			 * reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
			 * Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the
			 * end element of its outer element
			 */
			public static ResultValuesOutput parse(final javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
				final ResultValuesOutput object = new ResultValuesOutput();

				final int event;
				final java.lang.String nillableValue = null;
				final java.lang.String prefix = "";
				final java.lang.String namespaceuri = "";
				try {

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type") != null) {
						final java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = null;
							if (fullTypeName.indexOf(":") > -1) {
								nsPrefix = fullTypeName.substring(0, fullTypeName.indexOf(":"));
							}
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							final java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":") + 1);

							if (!"resultValuesOutput".equals(type)) {
								// find namespace for the prefix
								final java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
								return (ResultValuesOutput) ExtensionMapper.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ normal attributes
					// from anyAttributes.

					reader.next();

					final java.util.ArrayList list1 = new java.util.ArrayList();

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "returnValues").equals(reader.getName())) {

						// Process the array and step past its final element's end.
						list1.add(PostGetParams.Factory.parse(reader));

						// loop until we find a start element that is not part of this array
						boolean loopDone1 = false;
						while (!loopDone1) {
							// We should be at the end element, but make sure
							while (!reader.isEndElement()) {
								reader.next();
							}
							// Step out of this element
							reader.next();
							// Step to next element event.
							while (!reader.isStartElement() && !reader.isEndElement()) {
								reader.next();
							}
							if (reader.isEndElement()) {
								// two continuous end elements means we are exiting the xml structure
								loopDone1 = true;
							} else {
								if (new javax.xml.namespace.QName("", "returnValues").equals(reader.getName())) {
									list1.add(PostGetParams.Factory.parse(reader));

								} else {
									loopDone1 = true;
								}
							}
						}
						// call the converter utility to convert and set the array

						object.setReturnValues((PostGetParams[]) org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(PostGetParams.class, list1));

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an invalid parameter was passed
						throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement()) {
						// A start element we are not expecting indicates a trailing invalid property
						throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
					}

				} catch (final javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class PostGetParams implements org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name = postGetParams Namespace URI = http://localhost:8080/axis2_tester/services/ Namespace Prefix =
		 * ns1
		 */

		private static java.lang.String generatePrefix(final java.lang.String namespace) {
			if (namespace.equals("http://localhost:8080/axis2_tester/services/")) { return "ns1"; }
			return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
		}

		/**
		 * field for ParamName
		 */

		protected java.lang.String localParamName;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getParamName() {
			return localParamName;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param ParamName
		 */
		public void setParamName(final java.lang.String param) {

			this.localParamName = param;

		}

		/**
		 * field for ParamValue
		 */

		protected java.lang.String localParamValue;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getParamValue() {
			return localParamValue;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param ParamValue
		 */
		public void setParamValue(final java.lang.String param) {

			this.localParamValue = param;

		}

		/**
		 * field for PostGet
		 */

		protected PostGet_type1 localPostGet;

		/*
		 * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this
		 * field in the serialized XML
		 */
		protected boolean localPostGetTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return PostGet_type1
		 */
		public PostGet_type1 getPostGet() {
			return localPostGet;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param PostGet
		 */
		public void setPostGet(final PostGet_type1 param) {

			if (param != null) {
				// update the setting tracker
				localPostGetTracker = true;
			} else {
				localPostGetTracker = false;

			}

			this.localPostGet = param;

		}

		/**
		 * isReaderMTOMAware
		 * 
		 * @return true if the reader supports MTOM
		 */
		public static boolean isReaderMTOMAware(final javax.xml.stream.XMLStreamReader reader) {
			boolean isReaderMTOMAware = false;

			try {
				isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
			} catch (final java.lang.IllegalArgumentException e) {
				isReaderMTOMAware = false;
			}
			return isReaderMTOMAware;
		}

		/**
		 * @param parentQName
		 * @param factory
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getOMElement(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory)
				throws org.apache.axis2.databinding.ADBException {

			final org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this, parentQName) {

				public void serialize(final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
					PostGetParams.this.serialize(parentQName, factory, xmlWriter);
				}
			};
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(parentQName, factory, dataSource);

		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException,
				org.apache.axis2.databinding.ADBException {
			serialize(parentQName, factory, xmlWriter, false);
		}

		public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
				final org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter, final boolean serializeType)
				throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

			java.lang.String prefix = null;
			java.lang.String namespace = null;

			prefix = parentQName.getPrefix();
			namespace = parentQName.getNamespaceURI();

			if ((namespace != null) && (namespace.trim().length() > 0)) {
				final java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
				if (writerPrefix != null) {
					xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
				} else {
					if (prefix == null) {
						prefix = generatePrefix(namespace);
					}

					xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
					xmlWriter.writeNamespace(prefix, namespace);
					xmlWriter.setPrefix(prefix, namespace);
				}
			} else {
				xmlWriter.writeStartElement(parentQName.getLocalPart());
			}

			if (serializeType) {

				final java.lang.String namespacePrefix = registerPrefix(xmlWriter, "http://localhost:8080/axis2_tester/services/");
				if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)) {
					writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix + ":postGetParams", xmlWriter);
				} else {
					writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "postGetParams", xmlWriter);
				}

			}

			namespace = "";
			if (!namespace.equals("")) {
				prefix = xmlWriter.getPrefix(namespace);

				if (prefix == null) {
					prefix = generatePrefix(namespace);

					xmlWriter.writeStartElement(prefix, "paramName", namespace);
					xmlWriter.writeNamespace(prefix, namespace);
					xmlWriter.setPrefix(prefix, namespace);

				} else {
					xmlWriter.writeStartElement(namespace, "paramName");
				}

			} else {
				xmlWriter.writeStartElement("paramName");
			}

			if (localParamName == null) {
				// write the nil attribute

				throw new org.apache.axis2.databinding.ADBException("paramName cannot be null!!");

			} else {

				xmlWriter.writeCharacters(localParamName);

			}

			xmlWriter.writeEndElement();

			namespace = "";
			if (!namespace.equals("")) {
				prefix = xmlWriter.getPrefix(namespace);

				if (prefix == null) {
					prefix = generatePrefix(namespace);

					xmlWriter.writeStartElement(prefix, "paramValue", namespace);
					xmlWriter.writeNamespace(prefix, namespace);
					xmlWriter.setPrefix(prefix, namespace);

				} else {
					xmlWriter.writeStartElement(namespace, "paramValue");
				}

			} else {
				xmlWriter.writeStartElement("paramValue");
			}

			if (localParamValue == null) {
				// write the nil attribute

				throw new org.apache.axis2.databinding.ADBException("paramValue cannot be null!!");

			} else {

				xmlWriter.writeCharacters(localParamValue);

			}

			xmlWriter.writeEndElement();
			if (localPostGetTracker) {
				if (localPostGet == null) { throw new org.apache.axis2.databinding.ADBException("postGet cannot be null!!"); }
				localPostGet.serialize(new javax.xml.namespace.QName("", "postGet"), factory, xmlWriter);
			}
			xmlWriter.writeEndElement();

		}

		/**
		 * Util method to write an attribute with the ns prefix
		 */
		private void writeAttribute(final java.lang.String prefix, final java.lang.String namespace, final java.lang.String attName, final java.lang.String attValue,
				final javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
			if (xmlWriter.getPrefix(namespace) == null) {
				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);

			}

			xmlWriter.writeAttribute(namespace, attName, attValue);

		}

		/**
		 * Register a namespace prefix
		 */
		private java.lang.String registerPrefix(final javax.xml.stream.XMLStreamWriter xmlWriter, final java.lang.String namespace)
				throws javax.xml.stream.XMLStreamException {
			java.lang.String prefix = xmlWriter.getPrefix(namespace);

			if (prefix == null) {
				prefix = generatePrefix(namespace);

				while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
					prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
				}

				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);
			}

			return prefix;
		}

		/**
		 * databinding method to get an XML representation of this object
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(final javax.xml.namespace.QName qName) throws org.apache.axis2.databinding.ADBException {

			final java.util.ArrayList elementList = new java.util.ArrayList();
			final java.util.ArrayList attribList = new java.util.ArrayList();

			elementList.add(new javax.xml.namespace.QName("", "paramName"));

			if (localParamName != null) {
				elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localParamName));
			} else {
				throw new org.apache.axis2.databinding.ADBException("paramName cannot be null!!");
			}

			elementList.add(new javax.xml.namespace.QName("", "paramValue"));

			if (localParamValue != null) {
				elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localParamValue));
			} else {
				throw new org.apache.axis2.databinding.ADBException("paramValue cannot be null!!");
			}
			if (localPostGetTracker) {
				elementList.add(new javax.xml.namespace.QName("", "postGet"));

				if (localPostGet == null) { throw new org.apache.axis2.databinding.ADBException("postGet cannot be null!!"); }
				elementList.add(localPostGet);
			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening
			 * reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
			 * Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the
			 * end element of its outer element
			 */
			public static PostGetParams parse(final javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
				final PostGetParams object = new PostGetParams();

				final int event;
				final java.lang.String nillableValue = null;
				final java.lang.String prefix = "";
				final java.lang.String namespaceuri = "";
				try {

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type") != null) {
						final java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = null;
							if (fullTypeName.indexOf(":") > -1) {
								nsPrefix = fullTypeName.substring(0, fullTypeName.indexOf(":"));
							}
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							final java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":") + 1);

							if (!"postGetParams".equals(type)) {
								// find namespace for the prefix
								final java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
								return (PostGetParams) ExtensionMapper.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ normal attributes
					// from anyAttributes.

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "paramName").equals(reader.getName())) {

						final java.lang.String content = reader.getElementText();

						object.setParamName(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an invalid parameter was passed
						throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "paramValue").equals(reader.getName())) {

						final java.lang.String content = reader.getElementText();

						object.setParamValue(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an invalid parameter was passed
						throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement() && new javax.xml.namespace.QName("", "postGet").equals(reader.getName())) {

						object.setPostGet(PostGet_type1.Factory.parse(reader));

						reader.next();

					} // End of if for expected property start element

					else {

					}

					while (!reader.isStartElement() && !reader.isEndElement()) {
						reader.next();
					}

					if (reader.isStartElement()) {
						// A start element we are not expecting indicates a trailing invalid property
						throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
					}

				} catch (final javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	private org.apache.axiom.om.OMElement toOM(final de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ResultValuesInputE param,
			final boolean optimizeContent) throws org.apache.axis2.AxisFault {

		try {
			return param.getOMElement(de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ResultValuesInputE.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		} catch (final org.apache.axis2.databinding.ADBException e) {
			throw org.apache.axis2.AxisFault.makeFault(e);
		}

	}

	private org.apache.axiom.om.OMElement toOM(final de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ResultValuesResponse param,
			final boolean optimizeContent) throws org.apache.axis2.AxisFault {

		try {
			return param.getOMElement(de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ResultValuesResponse.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		} catch (final org.apache.axis2.databinding.ADBException e) {
			throw org.apache.axis2.AxisFault.makeFault(e);
		}

	}

	private org.apache.axiom.om.OMElement toOM(final de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.DisarmInputElement param,
			final boolean optimizeContent) throws org.apache.axis2.AxisFault {

		try {
			return param.getOMElement(de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.DisarmInputElement.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		} catch (final org.apache.axis2.databinding.ADBException e) {
			throw org.apache.axis2.AxisFault.makeFault(e);
		}

	}

	private org.apache.axiom.om.OMElement toOM(final de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.DisarmOutputElement param,
			final boolean optimizeContent) throws org.apache.axis2.AxisFault {

		try {
			return param.getOMElement(de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.DisarmOutputElement.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		} catch (final org.apache.axis2.databinding.ADBException e) {
			throw org.apache.axis2.AxisFault.makeFault(e);
		}

	}

	private org.apache.axiom.om.OMElement toOM(final de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.InputPartElement param, final boolean optimizeContent)
			throws org.apache.axis2.AxisFault {

		try {
			return param.getOMElement(de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.InputPartElement.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		} catch (final org.apache.axis2.databinding.ADBException e) {
			throw org.apache.axis2.AxisFault.makeFault(e);
		}

	}

	private org.apache.axiom.om.OMElement toOM(final de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.OutputPartElement param, final boolean optimizeContent)
			throws org.apache.axis2.AxisFault {

		try {
			return param.getOMElement(de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.OutputPartElement.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		} catch (final org.apache.axis2.databinding.ADBException e) {
			throw org.apache.axis2.AxisFault.makeFault(e);
		}

	}

	private org.apache.axiom.om.OMElement toOM(final de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ArmInputElement param, final boolean optimizeContent)
			throws org.apache.axis2.AxisFault {

		try {
			return param.getOMElement(de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ArmInputElement.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		} catch (final org.apache.axis2.databinding.ADBException e) {
			throw org.apache.axis2.AxisFault.makeFault(e);
		}

	}

	private org.apache.axiom.om.OMElement toOM(final de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ArmOutputElement param, final boolean optimizeContent)
			throws org.apache.axis2.AxisFault {

		try {
			return param.getOMElement(de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ArmOutputElement.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		} catch (final org.apache.axis2.databinding.ADBException e) {
			throw org.apache.axis2.AxisFault.makeFault(e);
		}

	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(final org.apache.axiom.soap.SOAPFactory factory,
			final de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ResultValuesInputE param, final boolean optimizeContent)
			throws org.apache.axis2.AxisFault {

		try {

			final org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
			emptyEnvelope.getBody().addChild(param.getOMElement(de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ResultValuesInputE.MY_QNAME, factory));
			return emptyEnvelope;
		} catch (final org.apache.axis2.databinding.ADBException e) {
			throw org.apache.axis2.AxisFault.makeFault(e);
		}

	}

	/* methods to provide back word compatibility */

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(final org.apache.axiom.soap.SOAPFactory factory,
			final de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.DisarmInputElement param, final boolean optimizeContent)
			throws org.apache.axis2.AxisFault {

		try {

			final org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
			emptyEnvelope.getBody().addChild(param.getOMElement(de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.DisarmInputElement.MY_QNAME, factory));
			return emptyEnvelope;
		} catch (final org.apache.axis2.databinding.ADBException e) {
			throw org.apache.axis2.AxisFault.makeFault(e);
		}

	}

	/* methods to provide back word compatibility */

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(final org.apache.axiom.soap.SOAPFactory factory,
			final de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.InputPartElement param, final boolean optimizeContent) throws org.apache.axis2.AxisFault {

		try {

			final org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
			emptyEnvelope.getBody().addChild(param.getOMElement(de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.InputPartElement.MY_QNAME, factory));
			return emptyEnvelope;
		} catch (final org.apache.axis2.databinding.ADBException e) {
			throw org.apache.axis2.AxisFault.makeFault(e);
		}

	}

	/* methods to provide back word compatibility */

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(final org.apache.axiom.soap.SOAPFactory factory,
			final de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ArmInputElement param, final boolean optimizeContent) throws org.apache.axis2.AxisFault {

		try {

			final org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
			emptyEnvelope.getBody().addChild(param.getOMElement(de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ArmInputElement.MY_QNAME, factory));
			return emptyEnvelope;
		} catch (final org.apache.axis2.databinding.ADBException e) {
			throw org.apache.axis2.AxisFault.makeFault(e);
		}

	}

	/* methods to provide back word compatibility */

	private java.lang.Object fromOM(final org.apache.axiom.om.OMElement param, final java.lang.Class type, final java.util.Map extraNamespaces)
			throws org.apache.axis2.AxisFault {

		try {

			if (de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ResultValuesInputE.class.equals(type)) {

			return de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ResultValuesInputE.Factory.parse(param.getXMLStreamReaderWithoutCaching());

			}

			if (de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ResultValuesResponse.class.equals(type)) {

			return de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ResultValuesResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());

			}

			if (de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.DisarmInputElement.class.equals(type)) {

			return de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.DisarmInputElement.Factory.parse(param.getXMLStreamReaderWithoutCaching());

			}

			if (de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.DisarmOutputElement.class.equals(type)) {

			return de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.DisarmOutputElement.Factory.parse(param.getXMLStreamReaderWithoutCaching());

			}

			if (de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.InputPartElement.class.equals(type)) {

			return de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.InputPartElement.Factory.parse(param.getXMLStreamReaderWithoutCaching());

			}

			if (de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.OutputPartElement.class.equals(type)) {

			return de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.OutputPartElement.Factory.parse(param.getXMLStreamReaderWithoutCaching());

			}

			if (de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ArmInputElement.class.equals(type)) {

			return de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ArmInputElement.Factory.parse(param.getXMLStreamReaderWithoutCaching());

			}

			if (de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ArmOutputElement.class.equals(type)) {

			return de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ArmOutputElement.Factory.parse(param.getXMLStreamReaderWithoutCaching());

			}

			if (de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ResultVariableCandidatesResponse.class.equals(type)) {

			return de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub.ResultVariableCandidatesResponse.Factory
					.parse(param.getXMLStreamReaderWithoutCaching());

			}

		} catch (final java.lang.Exception e) {
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
		return null;
	}

}
