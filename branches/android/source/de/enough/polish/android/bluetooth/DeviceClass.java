//#condition polish.android
// generated by de.enough.doc2java.Doc2Java (www.enough.de) on Tue Mar 24 10:34:27 EET 2009
package de.enough.polish.android.bluetooth;

/**
 * 
 * The <code>DeviceClass</code> class represents the class of device (CoD)
 * record as defined by the Bluetooth specification.  This record is defined in
 * the Bluetooth Assigned Numbers document
 * and contains information on the type of the device and the type of services
 * available on the device.
 * The Bluetooth Assigned Numbers document
 * (<A HREF="http://www.bluetooth.org/assigned-numbers/baseband.htm">
 * http://www.bluetooth.org/assigned-numbers/baseband.htm</A>)
 * defines the service class, major device class, and minor device class.  The
 * table below provides some examples of possible return values and their
 * meaning:
 * <TABLE>
 * <TR><TH>Method</TH><TH>Return Value</TH><TH>Class of Device</TH></TR>
 * <TR><TD><code>getServiceClasses()</code></TD>
 * <TD>0x22000</TD>
 * <TD>Networking and Limited Discoverable Major Service Classes</TD></TR>
 * <TR><TD><code>getServiceClasses()</code></TD>
 * <TD>0x100000</TD>
 * <TD>Object Transfer Major Service Class</TD></TR>
 * <TR><TD><code>getMajorDeviceClass()</code></TD>
 * <TD>0x00</TD>
 * <TD>Miscellaneous Major Device Class</TD></TR>
 * <TR><TD><code>getMajorDeviceClass()</code></TD>
 * <TD>0x200</TD>
 * <TD>Phone Major Device Class</TD></TR>
 * <TR><TD><code>getMinorDeviceClass()</code></TD>
 * <TD>0x0C</TD><TD>With a Computer Major Device Class,
 * Laptop Minor Device Class</TD></TR>
 * <TR><TD><code>getMinorDeviceClass()</code></TD>
 * <TD>0x04</TD><TD>With a Phone Major Device Class,
 * Cellular Minor Device Class</TD></TR>
 * </TABLE>
 * 
 * <DD>1.4</DD>
 * <HR>
 * 
 * 
 * <!-- ======== CONSTRUCTOR SUMMARY ======== -->
 * 
 * <A NAME="constructor_summary"><!-- --></A>
 * <TABLE BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
 * <TR BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
 * <TH ALIGN="left" COLSPAN="2"><FONT SIZE="+2">
 * <B>Constructor Summary</B></FONT></TH>
 * </TR>
 * <TR BGCOLOR="white" CLASS="TableRowColor">
 * <TD><CODE><B><A HREF="DeviceClass.html#DeviceClass(int)" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/bluetooth/DeviceClass.html#DeviceClass(int)">DeviceClass</A></B>(int&nbsp;record)</CODE>
 * 
 * <BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Creates a <code>DeviceClass</code> from the class of device record
 * provided.</TD>
 * </TR>
 * </TABLE>
 * &nbsp;
 * <!-- ========== METHOD SUMMARY =========== -->
 * 
 * <A NAME="method_summary"><!-- --></A>
 * <TABLE BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
 * <TR BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
 * <TH ALIGN="left" COLSPAN="2"><FONT SIZE="+2">
 * <B>Method Summary</B></FONT></TH>
 * </TR>
 * <TR BGCOLOR="white" CLASS="TableRowColor">
 * <TD ALIGN="right" VALIGN="top" WIDTH="1%"><FONT SIZE="-1">
 * <CODE>&nbsp;int</CODE></FONT></TD>
 * <TD><CODE><B><A HREF="DeviceClass.html#getMajorDeviceClass()" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/bluetooth/DeviceClass.html#getMajorDeviceClass()">getMajorDeviceClass</A></B>()</CODE>
 * 
 * <BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Retrieves the major device class.</TD>
 * </TR>
 * <TR BGCOLOR="white" CLASS="TableRowColor">
 * <TD ALIGN="right" VALIGN="top" WIDTH="1%"><FONT SIZE="-1">
 * <CODE>&nbsp;int</CODE></FONT></TD>
 * <TD><CODE><B><A HREF="DeviceClass.html#getMinorDeviceClass()" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/bluetooth/DeviceClass.html#getMinorDeviceClass()">getMinorDeviceClass</A></B>()</CODE>
 * 
 * <BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Retrieves the minor device class.</TD>
 * </TR>
 * <TR BGCOLOR="white" CLASS="TableRowColor">
 * <TD ALIGN="right" VALIGN="top" WIDTH="1%"><FONT SIZE="-1">
 * <CODE>&nbsp;int</CODE></FONT></TD>
 * <TD><CODE><B><A HREF="DeviceClass.html#getServiceClasses()" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/bluetooth/DeviceClass.html#getServiceClasses()">getServiceClasses</A></B>()</CODE>
 * 
 * <BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Retrieves the major service classes.</TD>
 * </TR>
 * </TABLE>
 * &nbsp;<A NAME="methods_inherited_from_class_java.lang.Object"><!-- --></A>
 * <TABLE BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
 * <TR BGCOLOR="#EEEEFF" CLASS="TableSubHeadingColor">
 * <TH ALIGN="left"><B>Methods inherited from class java.lang.Object</B></TH>
 * </TR>
 * <TR BGCOLOR="white" CLASS="TableRowColor">
 * <TD><CODE>clone, equals, finalize, getClass, hashCode, notify, notifyAll, toString, wait, wait, wait</CODE></TD>
 * </TR>
 * </TABLE>
 * &nbsp;
 * 
 * <!-- ========= CONSTRUCTOR DETAIL ======== -->
 * 
 * <A NAME="constructor_detail"><!-- --></A>
 * <TABLE BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
 * <TR BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
 * <TH ALIGN="left" COLSPAN="1"><FONT SIZE="+2">
 * <B>Constructor Detail</B></FONT></TH>
 * </TR>
 * </TABLE>
 * 
 * <A NAME="DeviceClass(int)"><!-- --></A><H3>
 * DeviceClass</H3>
 * <PRE>
 * public <B>DeviceClass</B>(int&nbsp;record)</PRE>
 * <DD>Creates a <code>DeviceClass</code> from the class of device record
 * provided.  <code>record</code> must follow the format of the
 * class of device record in the Bluetooth specification.
 * <DD><CODE>java.lang.IllegalArgumentException</CODE> - if <code>record</code> has any bits
 * between 24 and 31 set</DL>
 * 
 * <!-- ============ METHOD DETAIL ========== -->
 * 
 * <A NAME="method_detail"><!-- --></A>
 * <TABLE BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
 * <TR BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
 * <TH ALIGN="left" COLSPAN="1"><FONT SIZE="+2">
 * <B>Method Detail</B></FONT></TH>
 * </TR>
 * </TABLE>
 * 
 * <A NAME="getServiceClasses()"><!-- --></A><H3>
 * getServiceClasses</H3>
 * <PRE>
 * public int <B>getServiceClasses</B>()</PRE>
 * <DD>Retrieves the major service classes.  A device may have multiple major
 * service classes.  When this occurs, the major service classes are
 * bitwise OR'ed together.
 * 
 * <HR>
 * 
 * <A NAME="getMajorDeviceClass()"><!-- --></A><H3>
 * getMajorDeviceClass</H3>
 * <PRE>
 * public int <B>getMajorDeviceClass</B>()</PRE>
 * <DD>Retrieves the major device class.  A device may have only a single major
 * device class.
 * 
 * <HR>
 * 
 * <A NAME="getMinorDeviceClass()"><!-- --></A><H3>
 * getMinorDeviceClass</H3>
 * <PRE>
 * public int <B>getMinorDeviceClass</B>()</PRE>
 * <DD>Retrieves the minor device class.
 * 
 * <!-- ========= END OF CLASS DATA ========= -->
 * <HR>
 * 
 * 
 * <!-- ======= START OF BOTTOM NAVBAR ====== -->
 * <A NAME="navbar_bottom"><!-- --></A>
 * <A HREF="#skip-navbar_bottom" title="Skip navigation links"></A>
 * <TABLE BORDER="0" WIDTH="100%" CELLPADDING="1" CELLSPACING="0" SUMMARY="">
 * <TR>
 * <TD COLSPAN=2 BGCOLOR="#EEEEFF" CLASS="NavBarCell1">
 * <A NAME="navbar_bottom_firstrow"><!-- --></A>
 * <TABLE BORDER="0" CELLPADDING="0" CELLSPACING="3" SUMMARY="">
 * <TR ALIGN="center" VALIGN="top">
 * <TD BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    <A HREF="../../overview-summary.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/overview-summary.html"><FONT CLASS="NavBarFont1"><B>Overview</B></FONT></A>&nbsp;</TD>
 * <TD BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    <A HREF="package-summary.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/bluetooth/package-summary.html"><FONT CLASS="NavBarFont1"><B>Package</B></FONT></A>&nbsp;</TD>
 * <TD BGCOLOR="#FFFFFF" CLASS="NavBarCell1Rev"> &nbsp;<FONT CLASS="NavBarFont1Rev"><B>Class</B></FONT>&nbsp;</TD>
 * <TD BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    <A HREF="package-tree.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/bluetooth/package-tree.html"><FONT CLASS="NavBarFont1"><B>Tree</B></FONT></A>&nbsp;</TD>
 * <TD BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    <A HREF="../../deprecated-list.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/deprecated-list.html"><FONT CLASS="NavBarFont1"><B>Deprecated</B></FONT></A>&nbsp;</TD>
 * <TD BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    <A HREF="../../index-all.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/index-all.html"><FONT CLASS="NavBarFont1"><B>Index</B></FONT></A>&nbsp;</TD>
 * <TD BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    <A HREF="../../help-doc.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/help-doc.html"><FONT CLASS="NavBarFont1"><B>Help</B></FONT></A>&nbsp;</TD>
 * </TR>
 * </TABLE>
 * </TD>
 * <TD ALIGN="right" VALIGN="top" ROWSPAN=3><EM>
 * <b>JSR 82</b></EM>
 * </TD>
 * </TR>
 * 
 * <TR>
 * <TD BGCOLOR="white" CLASS="NavBarCell2"><FONT SIZE="-2">
 * &nbsp;<A HREF="DataElement.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/bluetooth/DataElement.html" title="class in javax.bluetooth"><B>PREV CLASS</B></A>&nbsp;
 * &nbsp;<A HREF="DiscoveryAgent.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/bluetooth/DiscoveryAgent.html" title="class in javax.bluetooth"><B>NEXT CLASS</B></A></FONT></TD>
 * <TD BGCOLOR="white" CLASS="NavBarCell2"><FONT SIZE="-2">
 * <A HREF="../../index.html-javax-bluetooth-DeviceClass.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/index.html?javax/bluetooth/DeviceClass.html" target="_top"><B>FRAMES</B></A>  &nbsp;
 * &nbsp;<A HREF="DeviceClass.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/bluetooth/DeviceClass.html" target="_top"><B>NO FRAMES</B></A>  &nbsp;
 * &nbsp;<SCRIPT type="text/javascript">
 * <!--
 * if(window==top) {
 * document.writeln('<A HREF="../../allclasses-noframe.html"/tpa=http://java.sun.com/javame/reference/apis/jsr082/allclasses-noframe.html><B>All Classes</B></A>');
 * }
 * //-->
 * </SCRIPT>
 * <NOSCRIPT>
 * <A HREF="../../allclasses-noframe.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/allclasses-noframe.html"><B>All Classes</B></A>
 * </NOSCRIPT>
 * 
 * 
 * </FONT></TD>
 * </TR>
 * <TR>
 * <TD VALIGN="top" CLASS="NavBarCell3"><FONT SIZE="-2">
 * SUMMARY:&nbsp;NESTED&nbsp;|&nbsp;FIELD&nbsp;|&nbsp;<A HREF="#constructor_summary">CONSTR</A>&nbsp;|&nbsp;<A HREF="#method_summary">METHOD</A></FONT></TD>
 * <TD VALIGN="top" CLASS="NavBarCell3"><FONT SIZE="-2">
 * DETAIL:&nbsp;FIELD&nbsp;|&nbsp;<A HREF="#constructor_detail">CONSTR</A>&nbsp;|&nbsp;<A HREF="#method_detail">METHOD</A></FONT></TD>
 * </TR>
 * </TABLE>
 * <A NAME="skip-navbar_bottom"></A>
 * <!-- ======== END OF BOTTOM NAVBAR ======= -->
 * 
 * <HR>
 * <small>Copyright � 2006 Sun Microsystems, Inc. All rights reserved. <b>Use is subject to <a href="http://java.sun.com/javame/reference/apis/license.html" target="_top">License Terms</a>.</b> Your use of this web site or any of its content or software indicates your agreement to be bound by these License Terms.<br><br>For more information, please consult the <a href="http://jcp.org/en/jsr/detail?id=82" target="_top">JSR 82 specification.</a></small>
 * </BODY>
 * <script language="JavaScript" src="../../../../../../js/omi/jsc/s_code_remote.js" tppabs="http://java.sun.com/js/omi/jsc/s_code_remote.js"></script></HTML>
 * 
 */
public class DeviceClass extends java.lang.Object
{
	//following variables are implicitely defined by getter- or setter-methods:
	private int serviceClasses;
	private int majorDeviceClass;
	private int minorDeviceClass;

	/**
	 * Creates a <code>DeviceClass</code> from the class of device record
	 * provided.  <code>record</code> must follow the format of the
	 * class of device record in the Bluetooth specification.
	 * <P>
	 * 
	 * @param record - describes the classes of a device
	 * @throws java.lang.IllegalArgumentException - if record has any bits between 24 and 31 set
	 */
	public DeviceClass(int record)
	{
		//TODO implement DeviceClass
	}

	/**
	 * Retrieves the major service classes.  A device may have multiple major
	 * service classes.  When this occurs, the major service classes are
	 * bitwise OR'ed together.
	 * <P>
	 * 
	 * 
	 * @return the major service classes
	 */
	public int getServiceClasses()
	{
		return this.serviceClasses;
	}

	/**
	 * Retrieves the major device class.  A device may have only a single major
	 * device class.
	 * <P>
	 * 
	 * 
	 * @return the major device class
	 */
	public int getMajorDeviceClass()
	{
		return this.majorDeviceClass;
	}

	/**
	 * Retrieves the minor device class.
	 * <P>
	 * 
	 * 
	 * @return the minor device class
	 */
	public int getMinorDeviceClass()
	{
		return this.minorDeviceClass;
	}

}
