package com.locify.client.net.browser;

/**
 * Internal class used when registering tag handlers.
 *
 * @see TagHandler
 */
class XHtmlTagHandlerKey {

    public String tagName;
    public String attributeName;
    public String attributeValue;

    public XHtmlTagHandlerKey(String tagName) {
        this.tagName = tagName;
    }

    public XHtmlTagHandlerKey(String tagName, String attributeName, String attributeValue) {
        this.tagName = tagName;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    public boolean equals(Object obj) {
        if (obj instanceof XHtmlTagHandlerKey) {
            XHtmlTagHandlerKey other = (XHtmlTagHandlerKey) obj;

            if (this.attributeName == null && other.attributeName == null) {
                return (this.tagName.equals(other.tagName));
            } else if (this.attributeName != null) {
                return (this.tagName.equals(other.tagName) && this.attributeName.equals(other.attributeName) && this.attributeValue.equals(other.attributeValue));
            } else {
                return false;
            }
        }

        return false;
    }

    public int hashCode() {
        if (this.attributeName == null) {
            return this.tagName.hashCode();
        }

        return (this.tagName.hashCode() ^ this.attributeName.hashCode() ^ this.attributeValue.hashCode());
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.tagName);
        sb.append(":");
        sb.append(this.attributeName);
        sb.append(":");
        sb.append(this.attributeValue);
        return sb.toString();
    }
}