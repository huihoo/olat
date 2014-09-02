package org.olat.lms.security.authentication.shibboleth;

public class SwitchShibbolethAuthenticationConfigurator {

    private String wayfSPEntityID;
    private String wayfSPHandlerURL;
    private String wayfSPSamlDSURL;
    private String wayfReturnUrl;
    private String additionalIDPs;

    /**
	 * 
	 */
    protected SwitchShibbolethAuthenticationConfigurator() {
        // used by spring
    }

    public String getWayfSPEntityID() {
        return wrapWithQuotes(wayfSPEntityID);
    }

    public void setWayfSPEntityID(final String wayfSPEntityID) {
        this.wayfSPEntityID = wayfSPEntityID;
    }

    /**
     * Adds a double quotes at the beginning and at the end of the aString if not already there.
     * 
     * @param aString
     * @return
     */
    private String wrapWithQuotes(String aString) {
        if (aString == null) {
            return aString;
        }
        if (!aString.startsWith("\"")) {
            aString = "\"" + aString;
        }
        if (!aString.endsWith("\"")) {
            aString += "\"";
        }
        return aString;
    }

    public String getWayfSPHandlerURL() {
        return wrapWithQuotes(wayfSPHandlerURL);
    }

    public void setWayfSPHandlerURL(final String wayfSPHandlerURL) {
        this.wayfSPHandlerURL = wayfSPHandlerURL;
    }

    public String getWayfSPSamlDSURL() {
        return wrapWithQuotes(wayfSPSamlDSURL);
    }

    public void setWayfSPSamlDSURL(final String wayfSPSamlDSURL) {
        this.wayfSPSamlDSURL = wayfSPSamlDSURL;
    }

    public String getWayfReturnUrl() {
        return wrapWithQuotes(wayfReturnUrl);
    }

    public void setWayfReturnUrl(final String wayfReturnUrl) {
        this.wayfReturnUrl = wayfReturnUrl;
    }

    public void setAdditionalIdentityProviders(final String additionalIDPs) {
        this.additionalIDPs = additionalIDPs;
    }

    public String getAdditionalIdentityProviders() {
        return additionalIDPs;
    }
}
