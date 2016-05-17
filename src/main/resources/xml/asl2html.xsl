<?xml version="1.0" ?>

<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0"
>
        
    <xsl:output method="html" />
    <xsl:strip-space elements="*"/>
    <xsl:include href="agInspection.xsl" />
    
    <xsl:template match="agent">
        <html>
            <xsl:if test="string-length(@source) > 0"> 
            	<h1><xsl:value-of select="@source" /></h1>
           	</xsl:if>
            <xsl:apply-templates select="//beliefs" />
            <xsl:apply-templates select="//plans"/>
        </html>
    </xsl:template>

    <xsl:template match="beliefs">
        <xsl:if test="count(literal) > 0"> 
	        <h2>Beliefs</h2>
	        <xsl:for-each select="literal">
	            <span style="color: {$bc}">
	                <xsl:apply-templates select="." />
                    <xsl:text>.</xsl:text>
	                <br/>
	            </span>
	        </xsl:for-each>
       	</xsl:if>
        <xsl:if test="count(rule) > 0"> 
	        <h2>Rules</h2>
	        <xsl:for-each select="rule">
	            <span style="color: {$bc}">
	                <xsl:apply-templates select="." />
	                <br/>
	            </span>
	        </xsl:for-each>
       	</xsl:if>
        
    </xsl:template>
    
    <!-- do not show source(self)  [we need to show self annot in case of some rules] -->
    <xsl:template match="annotations">
        <!-- xsl:if test="count(list-term/literal/structure/arguments/literal/structure[@functor = 'self']) != count(list-term/literal)"-->
            <span style="color: rgb(0 ,190, 0)">
                <sub>
                    <xsl:apply-templates select="list-term" />
                </sub>
            </span>
        <!-- /xsl:if -->
    </xsl:template>    
    
    <xsl:template match="plans">
        <h2>Plans</h2>
        <xsl:for-each select="plan|new-set-of-plans">
            <xsl:apply-templates select="." />
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template match="new-set-of-plans">
        <br/>
    </xsl:template>
    
    <xsl:template match="body">
        <xsl:param name="in-plan" select="'false'" />
        <xsl:for-each select="body-literal">
            <xsl:choose>
                <xsl:when test="literal/@ia = 'true'">
                    <span style="color: {$iac}"><xsl:apply-templates />	</span>
                </xsl:when>
                <xsl:when test="string-length(@type) = 0">
                    <span style="color: {$ac}"><xsl:apply-templates />	</span>
                </xsl:when>
                <xsl:when test="@type = '?'">
                    <span style="color: {$tgc}">?<xsl:apply-templates />	</span>
                </xsl:when>
                <xsl:when test="@type = '!' or @type = '!!'">
                    <span style="color: {$agc}"><xsl:value-of select="@type"/><xsl:apply-templates />	</span>
                </xsl:when>
                <xsl:when test="@type = '+' or @type = '-'">
                    <span style="color: {$bc}"><xsl:value-of select="@type"/><xsl:apply-templates />	</span>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="@type"/><xsl:apply-templates />
                </xsl:otherwise>        		
            </xsl:choose>
            <xsl:if test="not(position()=last())"> 
                <xsl:text>; </xsl:text> 
                <xsl:if test="$in-plan='true'"> 
                    <br/>
                </xsl:if>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>
    
</xsl:stylesheet>
