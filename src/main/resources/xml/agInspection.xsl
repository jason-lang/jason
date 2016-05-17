<?xml version="1.0" encoding="ISO-8859-1" ?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
    <xsl:param name="h-style"  select="'color: red; font-family: arial;'" />
    <xsl:param name="th-style" select="'text-align: left; vertical-align: top;  color: #330099;'" />
    <xsl:param name="th-style2" select="'text-align: left; color: blue;'" />
    <xsl:param name="td-style" select="'text-align: left; vertical-align: top;'" />
    <xsl:param name="td-style2" select="'text-align: center; vertical-align: top;'" />
    <xsl:param name="trh-style" select="'font-family: arial; vertical-align: top;'" />
    <xsl:param name="tr-style" select="'background-color: #ece7e6; font-family: arial;'" />
    <!-- border-top: 2px solid black;  -->
	
    <xsl:param name="bc"     select="'rgb(0 ,170, 0)'" />
    <xsl:param name="tec"    select="'rgb(200, 0, 0)'" />
    <xsl:param name="ac"     select="'rgb(80, 40, 20)'" />
    <xsl:param name="tgc"    select="'rgb(0, 0, 170)'" />
    <xsl:param name="agc"    select="'rgb(0, 0, 120)'" />
    <xsl:param name="iac"    select="'rgb(100, 70, 30)'" />
    <xsl:param name="var"    select="'rgb(0, 0, 200)'" />
    <xsl:param name="string" select="'rgb(0, 0, 250)'" />
    
    <xsl:param name="show-bels"    select="'true'" />
    <xsl:param name="show-annots"  select="'true'" />
    <xsl:param name="show-rules"   select="'true'" />
    <xsl:param name="show-evt"     select="'true'" />
    <xsl:param name="show-mb"      select="'true'" />
    <xsl:param name="show-plan"    select="'true'" />
    <xsl:param name="show-int"     select="'true'" />
    <xsl:param name="show-plan-details"   select="'true'" />
    <xsl:param name="show-int-details"    select="'true'" />
    
    <xsl:output method="html" />
    <xsl:strip-space elements="*" />
    
    <xsl:template match="agent">
        <html>
            <span style="{$h-style}"><font size="+2">
                    Inspection of agent <b><xsl:value-of select="@name"/></b> 
                    <xsl:if test="@cycle != 0">
                        (cycle #<xsl:value-of select="@cycle"/>)
                    </xsl:if>
            </font></span>
            
            <table border="0" cellspacing="3" cellpadding="6" >
                
                <xsl:apply-templates select="beliefs" />
                <xsl:apply-templates select="circumstance/mailbox" />
                <xsl:apply-templates select="circumstance/events" />
                <xsl:apply-templates select="circumstance/options" />
                <xsl:apply-templates select="circumstance/intentions" />
                <xsl:apply-templates select="circumstance/actions" />
                
                <!-- xsl:apply-templates select="plans" /-->
                <tr style="{$trh-style}">
	                <xsl:call-template name="hideshow">
	                    <xsl:with-param name="show" select="$show-annots" />
	                    <xsl:with-param name="item" select="'annots'" />
	                    <xsl:with-param name="ds" select="'Annotations'" />
	                </xsl:call-template>
	                <th/>
	            </tr>
            </table>
        </html>
    </xsl:template>
    
    <!-- create de +/- buttom -->
    <xsl:template name="hideshow">
        <xsl:param name="show" select="'false'" />
        <xsl:param name="item" select="'none'" />
        <xsl:param name="ds"   select="'none'" />
        <xsl:if test="$show='true'">       
            <th valign="top" style="{$th-style}">
                <hr/>
                <a href="hide?{$item}" style="text-decoration: none">
                    <font size="+1">-</font>
                    <xsl:text> </xsl:text>
                </a>
                <xsl:value-of select="$ds" />
            </th>
        </xsl:if>
        <xsl:if test="$show='false'">
            <th valign="top" style="{$th-style}">
                <hr/>
                <a href="show?{$item}" style="text-decoration: none">
                    <font size="+1">+</font>
                    <xsl:text> </xsl:text>
                </a>
                <xsl:value-of select="$ds" />
            </th>
            <td style="{$td-style}"></td>
        </xsl:if> 
    </xsl:template>
    
    <xsl:template match="beliefs">
        <xsl:if test="count(literal) > 0" >
            <tr style="{$trh-style}">
                <xsl:call-template name="hideshow">
                    <xsl:with-param name="show" select="$show-bels" />
                    <xsl:with-param name="item" select="'bels'" />
                    <xsl:with-param name="ds" select="'Beliefs'" />
                </xsl:call-template>
                <xsl:if test="$show-bels='true'">
	                    <td style="{$td-style}">
	                        <hr/>
	                        <table cellspacing="0" cellpadding="2">
			                    <xsl:for-each select="namespaces/namespace">
    		                        <xsl:variable name="nsId" select="@id" />
		                            <xsl:for-each select="../../literal[@namespace=$nsId]">
		                                <xsl:sort select="structure/@functor" />
		                                <tr style="{$trh-style}">
		                                    <td style="text-align: left">
			                                    <xsl:if test="$nsId != 'default' and position()=1">
			                                       <br/><b><xsl:value-of select="$nsId" /><xsl:text>::</xsl:text></b> <br/>
			                                    </xsl:if>
		                                        <span style="color: {$bc}">
		                                            <xsl:apply-templates select="." />
		                                        </span>
		                                        <xsl:text>.</xsl:text>
		                                    </td>
		                                </tr>
		                            </xsl:for-each>
                                </xsl:for-each>
	                        </table>
	                    </td>
                </xsl:if>
            </tr>
        </xsl:if>
        
        <!-- Rules -->
        <xsl:if test="count(rule) > 0" >
            <tr style="{$trh-style}">
                <xsl:call-template name="hideshow">
                    <xsl:with-param name="show" select="$show-rules" />
                    <xsl:with-param name="item" select="'rules'" />
                    <xsl:with-param name="ds" select="'Rules'" />
                </xsl:call-template>
                <xsl:if test="$show-rules='true'">
                    <td style="{$td-style}">
                        <hr/>
                        <table cellspacing="0" cellpadding="2">
                            <xsl:for-each select="rule">
                                <tr style="{$trh-style}">
                                    <td style="text-align: left">
                                        <span style="color: {$bc}">
                                            <xsl:apply-templates select="." />
                                        </span>
                                    </td>
                                </tr>
                            </xsl:for-each>
                        </table>
                    </td>
                </xsl:if>
            </tr>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="mailbox">
        <tr style="{$trh-style}">
            <xsl:call-template name="hideshow">
                <xsl:with-param name="show" select="$show-mb" />
                <xsl:with-param name="item" select="'mb'" />
                <xsl:with-param name="ds" select="'MailBox'" />
            </xsl:call-template>
            <xsl:if test="$show-mb='true'">
                <td style="{$td-style}"><hr/>
                    <table cellspacing="0" cellpadding="3">
                        <xsl:apply-templates select="message" />
                    </table>
                </td>
            </xsl:if>
        </tr>
    </xsl:template>
    
    
    <xsl:template match="message">
        <tr style="{$trh-style}">
            <td style="text-align: left">
                <xsl:apply-templates />
            </td>
        </tr>
    </xsl:template>
    
    <xsl:template match="events">
        <tr style="{$trh-style}">
            <xsl:call-template name="hideshow">
                <xsl:with-param name="show" select="$show-evt" />
                <xsl:with-param name="item" select="'evt'" />
                <xsl:with-param name="ds" select="'Events'" />
            </xsl:call-template>
            <xsl:if test="$show-evt='true'">
                <td style="{$td-style}"><hr/>
                    <table cellspacing="0" cellpadding="3">
                        <tr style="{$trh-style}">
                            <th valign="top" style="{$th-style2}">Sel</th>
                            <th valign="top" style="{$th-style2}">Trigger</th>
                            <th valign="top" style="{$th-style2}">Intention</th>
                        </tr>
                        <xsl:apply-templates />
                    </table>
                </td>
            </xsl:if>
        </tr>
    </xsl:template>
    
    <xsl:template match="event">
        <tr style="{$trh-style}">
            <td valign="top" style="{$td-style2}">
                <xsl:if test="@selected='true'">
                    <b>X</b>
                </xsl:if>
                <xsl:value-of select="@pending" />
            </td>
            
            <td valign="top" style="{$td-style}">
                <span style="color: {$tec}">
                    <xsl:apply-templates />
                </span>
            </td>
            <td valign="top" style="{$td-style2}">
                <xsl:value-of select="@intention" />
            </td>
        </tr>
    </xsl:template>
    
    <xsl:template match="intentions">
        <tr style="{$trh-style}">
            <xsl:call-template name="hideshow">
                <xsl:with-param name="show" select="$show-int" />
                <xsl:with-param name="item" select="'int'" />
                <xsl:with-param name="ds" select="'Intentions'" />
            </xsl:call-template>
            <xsl:if test="$show-int='true'">
                <td valign="top" style="{$td-style}"><hr/>
                    <table cellspacing="0" cellpadding="5">
                        <tr style="{$trh-style}">
                            <th valign="top" style="{$th-style2}">Sel</th>
                            <th valign="top" style="{$th-style2}">Id</th>
                            <th valign="top" style="{$th-style2}">Pen</th>
                            <th valign="top" style="{$th-style2}">Intended Means Stack
                                <xsl:if test="$show-int-details='true'">                                    
                                    <a href="hide?int-details" style="text-decoration: none">
                                        <xsl:text>(hide details)</xsl:text>
                                    </a>
                                </xsl:if>
                                <xsl:if test="$show-int-details='false'">
                                    <a href="show?int-details" style="text-decoration: none">
                                        <xsl:text>(show details)</xsl:text>
                                    </a>
                                </xsl:if>                            
                            </th>
                        </tr>
                        <xsl:apply-templates />
                    </table>
                </td>
            </xsl:if>
        </tr>
    </xsl:template>
    
    <xsl:template match="intention">
        <tr style="{$trh-style}">
            <td valign="top" style="{$td-style2}">
                <xsl:if test="@selected='true'">
                    <b>X</b>
                </xsl:if>
            </td>
            
            <td valign="top" style="{$td-style2}">
                <xsl:value-of select="@id" />
            </td>
            
            <td valign="top" style="{$td-style2}">
                <xsl:if test="string-length(@pending) > 0">
                    <b><xsl:value-of select="@pending" /></b>
                </xsl:if>
                <xsl:if test="string-length(@suspended) > 0 and @suspended='true' and not(starts-with(@pending,'suspen'))">
                    <xsl:text> (suspended) </xsl:text>
                </xsl:if>
            </td>
            
            <td valign="top">
                <table cellspacing="0" cellpadding="2">
                    <xsl:apply-templates />
                    <xsl:if test="@finished = 'true'">
                        <b> (finished)</b>
                    </xsl:if>
                </table>
                <hr/>
            </td>
        </tr>
    </xsl:template>
    <xsl:template match="intended-means">
        <tr>
            <td valign="top" style="{$td-style}">
	        <xsl:apply-templates select="@trigger"/>
            <xsl:if test="$show-int-details='true'">
                <!-- td valign="top" style="{$td-style}" -->
                <br/>                
                <pre>     &lt;- ... <xsl:apply-templates select="body"/> </pre>
                <!-- /td -->
            </xsl:if>
            </td>
            
            <xsl:if test="$show-int-details='true'">
                <td valign="top" style="{$td-style}">
                    <br/>
                    <xsl:apply-templates select="unifier"/>
                </td>
            </xsl:if>
        </tr>
    </xsl:template>
    
    
    <xsl:template match="actions">
        <tr style="{$trh-style}">
            <th valign="top" style="{$th-style}"><hr/>
            Actions</th>
            <td valign="top" style="{$td-style}"><hr/>
                <table ellspacing="0" cellpadding="3">
                    <tr style="{$trh-style}">
                        <th valign="top" style="{$th-style2}">Pend</th>
                        <th valign="top" style="{$th-style2}">Feed</th>
                        <th valign="top" style="{$th-style2}">Sel</th>
                        <th valign="top" style="{$th-style2}">Term</th>
                        <th valign="top" style="{$th-style2}">Result</th>
                        <th valign="top" style="{$th-style2}">Intention</th>
                    </tr>
                    <xsl:apply-templates select="action"/>
                </table>
            </td>
        </tr>
    </xsl:template>
    
    <xsl:template match="action">
        <tr style="{$trh-style}">
            
            <td valign="top" style="{$td-style2}">
                <xsl:if test="@pending='true'">
                    X
                </xsl:if>
            </td>
            
            <td valign="top" style="{$td-style2}">
                <xsl:if test="@feedback='true'">
                    X
                </xsl:if>
            </td>
            
            <td valign="top" style="{$td-style2}">
                <xsl:if test="@selected='true'">
                    <b>X</b>
                </xsl:if>
            </td>
            
            <td valign="top" style="{$td-style}">
                <xsl:value-of select="@term" /> 
            </td>
            <td valign="top" style="{$td-style2}">
                <xsl:value-of select="@result" />
            </td>
            <td valign="top" style="{$td-style2}">
                <xsl:value-of select="@intention" />
            </td>
        </tr>
    </xsl:template>
    
    <xsl:template match="options">
        <tr style="{$trh-style}">
            <xsl:call-template name="hideshow">
                <xsl:with-param name="show" select="$show-plan" />
                <xsl:with-param name="item" select="'plan'" />
                <xsl:with-param name="ds" select="'Options'" />
            </xsl:call-template>
            <xsl:if test="$show-plan='true'">
                <td><hr/>
                    <table ellspacing="0" cellpadding="3">
                        <tr style="{$trh-style}">
                            <!--th valign="top" style="{$th-style2}">Rel</th-->
                            <th valign="top" style="{$th-style2}">App</th>
                            <th valign="top" style="{$th-style2}">Sel</th>
                            <th valign="top" style="{$th-style2}">Plan
                                <xsl:if test="$show-plan-details='true'">
                                    <a href="hide?plan-details" style="text-decoration: none">
                                        <xsl:text>(hide details)</xsl:text>
                                    </a>
                                </xsl:if>
                                <xsl:if test="$show-plan-details='false'">
                                    <a href="show?plan-details" style="text-decoration: none">
                                        <xsl:text>(show details)</xsl:text>
                                    </a>
                                </xsl:if>                            
                            </th>
                            <th valign="top" style="{$th-style2}">Unifier</th>
                        </tr>
                        <xsl:apply-templates />
                    </table>
                </td>
            </xsl:if>
        </tr>
    </xsl:template>
    
    <xsl:template match="option">
        <tr style="{$trh-style}">
            
            <td valign="top" style="{$td-style2}">
                <xsl:if test="@applicable='true'">
                    X
                </xsl:if>
            </td>
            
            <td valign="top" style="{$td-style2}">
                <xsl:if test="@selected='true'">
                    <b>X</b>
                </xsl:if>
            </td>
            
            <td valign="top" style="{$td-style}">
                <xsl:if test="$show-plan-details='true'">
                    <xsl:apply-templates select="plan"/>
                </xsl:if>
                <xsl:if test="$show-plan-details='false'">
                    <xsl:apply-templates select="plan/trigger"/>
                </xsl:if>                            
            </td>
            <td valign="top" style="{$td-style}">
                <xsl:apply-templates select="unifier"/>
            </td>
        </tr>
    </xsl:template>
    
    <xsl:template match="unifier">
        <xsl:if test="count(map) > 0">
            {
            <xsl:for-each select="map">
                <xsl:apply-templates select="var-term"/>
                =
                <xsl:apply-templates select="value"/>
                <xsl:if test="not(position()=last())">, </xsl:if>
            </xsl:for-each>
            }
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="plan">
        <xsl:if test="count(label) > 0 and not(starts-with(label/literal/structure/@functor,'l__'))">
            <span style="color: rgb(51, 51, 51);">
                @<xsl:apply-templates select="label" />
            </span><br/>
        </xsl:if>
        
        <span style="color: {$tec};">
            <b><xsl:apply-templates select="trigger" /></b>
        </span>
        
        <xsl:if test="count(context) > 0">
            <table>
                <tr>
                    <td width="20" />
                    <td width="20" style="vertical-align: top"><b>:</b></td>
                    <td><xsl:apply-templates select="context" /></td> 
                </tr>
            </table>
        </xsl:if>
        
        <xsl:if test="count(body/body-literal) > 0">
            <table>
                <tr>
                    <td width="20" />
                    <td width="20" style="vertical-align: top"><b>&lt;-</b></td>
                    <td>
                       <xsl:apply-templates select="body">
                         <xsl:with-param name="in-plan" select="'true'" />
                       </xsl:apply-templates>
                    </td>
                </tr>
            </table>
        </xsl:if>
        <xsl:text>.</xsl:text>
    </xsl:template>
    
    
    <xsl:template match="context">
        <span style="color: {$bc}">
            <xsl:apply-templates />
        </span>
    </xsl:template>

    <xsl:template match="expression">
        <span style="color: black">(</span>
        <xsl:apply-templates select="left" />
        <span style="color: black">
            <xsl:value-of select="@operator" />
        </span>
        <xsl:apply-templates select="right" />
        <span style="color: black">)</span>
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
            <xsl:if test="not(position()=last())">; </xsl:if>
        </xsl:for-each>
    </xsl:template>
    
    
    <xsl:template match="trigger">
        <xsl:value-of select="@operator"/>
        <xsl:value-of select="@type"/>
        <xsl:apply-templates />
    </xsl:template>
    
    <xsl:template match="rule">
        <xsl:apply-templates select="head"/>
        <font color="black"><b><xsl:text> :- </xsl:text></b></font>
        <table>
            <tr>
                <td width="20" />
                <td><xsl:apply-templates select="context" />.</td> 
            </tr>
        </table>
    </xsl:template>

    <xsl:template match="literal">
        <xsl:if test="@negated = 'true'">
            <b><xsl:text>~</xsl:text></b>
        </xsl:if>
        <xsl:if test="count(@cyclic-var) > 0">
            <b><xsl:text>...</xsl:text></b>
        </xsl:if>
        <xsl:apply-templates  />
    </xsl:template>
    
    <xsl:template match="structure">
        <xsl:value-of select="@functor"/>
        <xsl:if test="count(arguments) > 0">
            <xsl:text>(</xsl:text>
            <xsl:for-each select="arguments/*">
                <xsl:apply-templates select="." />
                <xsl:if test="not(position()=last())"><xsl:text>,</xsl:text></xsl:if>
            </xsl:for-each>
            <xsl:text>)</xsl:text>
        </xsl:if>
        <xsl:if test="count(annotations) > 0">
            <xsl:apply-templates select="annotations" />
        </xsl:if>
    </xsl:template>

    <xsl:template match="annotations">
        <xsl:if test="$show-annots='true'">
            <span style="color: rgb(0 ,190, 0)">
                <sub>
                    <xsl:apply-templates />
                </sub>
            </span>            
        </xsl:if>
        <xsl:if test="$show-annots='false'">
            <xsl:if test="count(list-term) > 0">
                <sub>
                    <span style="color: rgb(0 ,0, 200)">
                    <a href="show?annots" style="text-decoration: none">
                    <xsl:text>[...]</xsl:text>
                    </a>
                    </span>                
                </sub>                
            </xsl:if>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="var-term">
        <span style="color: {$var}">
            <xsl:value-of select="@functor"/>
            <xsl:if test="count(annotations) > 0">
                <xsl:apply-templates select="annotations" />
            </xsl:if>
        </span>
    </xsl:template>
    
    <xsl:template match="number-term">
        <i><xsl:value-of select="text()"/></i>
    </xsl:template>
    <xsl:template match="string-term">
        <span style="color: {$string}">
            <xsl:value-of select="text()"/>
        </span>
    </xsl:template>
    <xsl:template match="list-term">
        <xsl:text>[</xsl:text>
        <xsl:for-each select="*">
            <xsl:value-of select="@sep"/>
            <xsl:apply-templates select="." />
        </xsl:for-each>
        <xsl:text>]</xsl:text>
    </xsl:template>

</xsl:stylesheet> 
