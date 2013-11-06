<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">
    <xsl:output method="html" encoding="UTF-8" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
                doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" indent="yes"/>
    <xsl:preserve-space elements="*"/>

    <xsl:template match="report">
        Report
        <xsl:apply-templates select="test"/>
    </xsl:template>

    <xsl:template match="test">
        Name: <xsl:value-of select="name"/>
    </xsl:template>

    <xsl:template match="text()|@*">
        
    </xsl:template>

    <xsl:template match="*">
        <xsl:message terminate="no">
            WARNING: Unmatched element: <xsl:value-of select="name()"/>
        </xsl:message>
        <xsl:apply-templates/>
    </xsl:template>


</xsl:stylesheet>