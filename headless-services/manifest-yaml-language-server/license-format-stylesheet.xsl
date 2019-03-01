<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="text"/> 
 
  <xsl:template match="/">
	  Welcome to the formatted license file
       <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="/licenseSummary/dependencies/dependency/groupId">
    groupId: <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="/licenseSummary/dependencies/dependency/artifactId">
    artifactId: <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="/licenseSummary/dependencies/dependency/version">
    version: <xsl:apply-templates/>
  </xsl:template>

</xsl:stylesheet>