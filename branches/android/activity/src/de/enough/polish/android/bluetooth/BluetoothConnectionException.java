//#condition polish.android
// generated by de.enough.doc2java.Doc2Java (www.enough.de) on Tue Mar 24 10:34:27 EET 2009
package de.enough.polish.android.bluetooth;

/**
 * 
 * This <code>BluetoothConnectionException</code> is thrown when
 * a Bluetooth connection (L2CAP, RFCOMM, or OBEX over RFCOMM)
 * cannot be established successfully. The fields in this exception
 * class indicate the cause of the exception. For example, an L2CAP
 * connection may fail due to a security problem.
 * This reason is passed on to the application through this class.
 * 
 * <DD>1.3</DD>
 * <HR>
 * 
 * <!-- =========== FIELD SUMMARY =========== -->
 * 
 * <A NAME="field_summary"><!-- --></A>
 * <TABLE BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
 * <TR BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
 * <TH ALIGN="left" COLSPAN="2"><FONT SIZE="+2">
 * <B>Field Summary</B></FONT></TH>
 * </TR>
 * <TR BGCOLOR="white" CLASS="TableRowColor">
 * <TD ALIGN="right" VALIGN="top" WIDTH="1%"><FONT SIZE="-1">
 * <CODE>static&nbsp;int</CODE></FONT></TD>
 * <TD><CODE><B><A HREF="BluetoothConnectionException.html#FAILED_NOINFO" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/bluetooth/BluetoothConnectionException.html#FAILED_NOINFO">FAILED_NOINFO</A></B></CODE>
 * 
 * <BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Indicates the connection to the server failed due to unknown reasons.</TD>
 * </TR>
 * <TR BGCOLOR="white" CLASS="TableRowColor">
 * <TD ALIGN="right" VALIGN="top" WIDTH="1%"><FONT SIZE="-1">
 * <CODE>static&nbsp;int</CODE></FONT></TD>
 * <TD><CODE><B><A HREF="BluetoothConnectionException.html#NO_RESOURCES" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/bluetooth/BluetoothConnectionException.html#NO_RESOURCES">NO_RESOURCES</A></B></CODE>
 * 
 * <BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Indicates the connection failed due to a lack of resources
 * either on the local device or on the remote device.</TD>
 * </TR>
 * <TR BGCOLOR="white" CLASS="TableRowColor">
 * <TD ALIGN="right" VALIGN="top" WIDTH="1%"><FONT SIZE="-1">
 * <CODE>static&nbsp;int</CODE></FONT></TD>
 * <TD><CODE><B><A HREF="BluetoothConnectionException.html#SECURITY_BLOCK" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/bluetooth/BluetoothConnectionException.html#SECURITY_BLOCK">SECURITY_BLOCK</A></B></CODE>
 * 
 * <BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Indicates the connection failed because the security
 * settings on the local device or the remote device were
 * incompatible with the request.</TD>
 * </TR>
 * <TR BGCOLOR="white" CLASS="TableRowColor">
 * <TD ALIGN="right" VALIGN="top" WIDTH="1%"><FONT SIZE="-1">
 * <CODE>static&nbsp;int</CODE></FONT></TD>
 * <TD><CODE><B><A HREF="BluetoothConnectionException.html#TIMEOUT" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/bluetooth/BluetoothConnectionException.html#TIMEOUT">TIMEOUT</A></B></CODE>
 * 
 * <BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Indicates the connection to the server failed due to a timeout.</TD>
 * </TR>
 * <TR BGCOLOR="white" CLASS="TableRowColor">
 * <TD ALIGN="right" VALIGN="top" WIDTH="1%"><FONT SIZE="-1">
 * <CODE>static&nbsp;int</CODE></FONT></TD>
 * <TD><CODE><B><A HREF="BluetoothConnectionException.html#UNACCEPTABLE_PARAMS" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/bluetooth/BluetoothConnectionException.html#UNACCEPTABLE_PARAMS">UNACCEPTABLE_PARAMS</A></B></CODE>
 * 
 * <BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Indicates the connection failed because the configuration
 * parameters provided were not acceptable to either the remote device or
 * the local device.</TD>
 * </TR>
 * <TR BGCOLOR="white" CLASS="TableRowColor">
 * <TD ALIGN="right" VALIGN="top" WIDTH="1%"><FONT SIZE="-1">
 * <CODE>static&nbsp;int</CODE></FONT></TD>
 * <TD><CODE><B><A HREF="BluetoothConnectionException.html#UNKNOWN_PSM" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/bluetooth/BluetoothConnectionException.html#UNKNOWN_PSM">UNKNOWN_PSM</A></B></CODE>
 * 
 * <BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Indicates the connection to the server failed because no service for
 * the given PSM was registered.</TD>
 * </TR>
 * </TABLE>
 * &nbsp;
 * <!-- ======== CONSTRUCTOR SUMMARY ======== -->
 * 
 * <A NAME="constructor_summary"><!-- --></A>
 * <TABLE BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
 * <TR BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
 * <TH ALIGN="left" COLSPAN="2"><FONT SIZE="+2">
 * <B>Constructor Summary</B></FONT></TH>
 * </TR>
 * <TR BGCOLOR="white" CLASS="TableRowColor">
 * <TD><CODE><B><A HREF="BluetoothConnectionException.html#BluetoothConnectionException(int)" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/bluetooth/BluetoothConnectionException.html#BluetoothConnectionException(int)">BluetoothConnectionException</A></B>(int&nbsp;error)</CODE>
 * 
 * <BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Creates a new <code>BluetoothConnectionException</code>
 * with the error indicator specified.</TD>
 * </TR>
 * <TR BGCOLOR="white" CLASS="TableRowColor">
 * <TD><CODE><B><A HREF="BluetoothConnectionException.html#BluetoothConnectionException(int, java.lang.String)" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/bluetooth/BluetoothConnectionException.html#BluetoothConnectionException(int, java.lang.String)">BluetoothConnectionException</A></B>(int&nbsp;error,
 * java.lang.String&nbsp;msg)</CODE>
 * 
 * <BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Creates a new <code>BluetoothConnectionException</code>
 * with the error indicator and message specified.</TD>
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
 * <TD><CODE><B><A HREF="BluetoothConnectionException.html#getStatus()" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/bluetooth/BluetoothConnectionException.html#getStatus()">getStatus</A></B>()</CODE>
 * 
 * <BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Gets the status set in the constructor that will indicate
 * the reason for the exception.</TD>
 * </TR>
 * </TABLE>
 * &nbsp;<A NAME="methods_inherited_from_class_java.lang.Throwable"><!-- --></A>
 * <TABLE BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
 * <TR BGCOLOR="#EEEEFF" CLASS="TableSubHeadingColor">
 * <TH ALIGN="left"><B>Methods inherited from class java.lang.Throwable</B></TH>
 * </TR>
 * <TR BGCOLOR="white" CLASS="TableRowColor">
 * <TD><CODE>fillInStackTrace, getCause, getLocalizedMessage, getMessage, getStackTrace, initCause, printStackTrace, printStackTrace, printStackTrace, setStackTrace, toString</CODE></TD>
 * </TR>
 * </TABLE>
 * &nbsp;<A NAME="methods_inherited_from_class_java.lang.Object"><!-- --></A>
 * <TABLE BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
 * <TR BGCOLOR="#EEEEFF" CLASS="TableSubHeadingColor">
 * <TH ALIGN="left"><B>Methods inherited from class java.lang.Object</B></TH>
 * </TR>
 * <TR BGCOLOR="white" CLASS="TableRowColor">
 * <TD><CODE>clone, equals, finalize, getClass, hashCode, notify, notifyAll, wait, wait, wait</CODE></TD>
 * </TR>
 * </TABLE>
 * &nbsp;
 * 
 * <!-- ============ FIELD DETAIL =========== -->
 * 
 * <A NAME="field_detail"><!-- --></A>
 * <TABLE BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
 * <TR BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
 * <TH ALIGN="left" COLSPAN="1"><FONT SIZE="+2">
 * <B>Field Detail</B></FONT></TH>
 * </TR>
 * </TABLE>
 * 
 * <A NAME="UNKNOWN_PSM"><!-- --></A><H3>
 * UNKNOWN_PSM</H3>
 * <PRE>
 * public static final int <B>UNKNOWN_PSM</B></PRE>
 * <DD>Indicates the connection to the server failed because no service for
 * the given PSM was registered.
 * The value for <code>UNKNOWN_PSM</code> is 0x0001 (1).
 * <HR>
 * 
 * <A NAME="SECURITY_BLOCK"><!-- --></A><H3>
 * SECURITY_BLOCK</H3>
 * <PRE>
 * public static final int <B>SECURITY_BLOCK</B></PRE>
 * <DD>Indicates the connection failed because the security
 * settings on the local device or the remote device were
 * incompatible with the request.
 * The value for <code>SECURITY_BLOCK</code> is 0x0002 (2).
 * <HR>
 * 
 * <A NAME="NO_RESOURCES"><!-- --></A><H3>
 * NO_RESOURCES</H3>
 * <PRE>
 * public static final int <B>NO_RESOURCES</B></PRE>
 * <DD>Indicates the connection failed due to a lack of resources
 * either on the local device or on the remote device.
 * The value for <code>NO_RESOURCES</code> is 0x0003 (3).
 * <HR>
 * 
 * <A NAME="FAILED_NOINFO"><!-- --></A><H3>
 * FAILED_NOINFO</H3>
 * <PRE>
 * public static final int <B>FAILED_NOINFO</B></PRE>
 * <DD>Indicates the connection to the server failed due to unknown reasons.
 * The value for <code>FAILED_NOINFO</code> is 0x0004 (4).
 * <HR>
 * 
 * <A NAME="TIMEOUT"><!-- --></A><H3>
 * TIMEOUT</H3>
 * <PRE>
 * public static final int <B>TIMEOUT</B></PRE>
 * <DD>Indicates the connection to the server failed due to a timeout.
 * The value for <code>TIMEOUT</code> is 0x0005 (5).
 * <HR>
 * 
 * <A NAME="UNACCEPTABLE_PARAMS"><!-- --></A><H3>
 * UNACCEPTABLE_PARAMS</H3>
 * <PRE>
 * public static final int <B>UNACCEPTABLE_PARAMS</B></PRE>
 * <DD>Indicates the connection failed because the configuration
 * parameters provided were not acceptable to either the remote device or
 * the local device.
 * The value for <code>UNACCEPTABLE_PARAMS</code> is 0x0006 (6).
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
 * <A NAME="BluetoothConnectionException(int)"><!-- --></A><H3>
 * BluetoothConnectionException</H3>
 * <PRE>
 * public <B>BluetoothConnectionException</B>(int&nbsp;error)</PRE>
 * <DD>Creates a new <code>BluetoothConnectionException</code>
 * with the error indicator specified.
 * constants described in this class
 * <DD><CODE>java.lang.IllegalArgumentException</CODE> - if the input value
 * is not one of the constants in this class</DL>
 * <HR>
 * 
 * <A NAME="BluetoothConnectionException(int, java.lang.String)"><!-- --></A><H3>
 * BluetoothConnectionException</H3>
 * <PRE>
 * public <B>BluetoothConnectionException</B>(int&nbsp;error,
 * java.lang.String&nbsp;msg)</PRE>
 * <DD>Creates a new <code>BluetoothConnectionException</code>
 * with the error indicator and message specified.
 * constants described in this class<DD><CODE>msg</CODE> - a description of the exception; may by <code>null</code>
 * <DD><CODE>java.lang.IllegalArgumentException</CODE> - if the input value
 * is not one of the constants in this class</DL>
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
 * <A NAME="getStatus()"><!-- --></A><H3>
 * getStatus</H3>
 * <PRE>
 * public int <B>getStatus</B>()</PRE>
 * <DD>Gets the status set in the constructor that will indicate
 * the reason for the exception.
 * 
 * in this class</DL>
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
 * &nbsp;PREV CLASS&nbsp;
 * &nbsp;<A HREF="BluetoothStateException.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/bluetooth/BluetoothStateException.html" title="class in javax.bluetooth"><B>NEXT CLASS</B></A></FONT></TD>
 * <TD BGCOLOR="white" CLASS="NavBarCell2"><FONT SIZE="-2">
 * <A HREF="../../index.html-javax-bluetooth-BluetoothConnectionException.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/index.html?javax/bluetooth/BluetoothConnectionException.html" target="_top"><B>FRAMES</B></A>  &nbsp;
 * &nbsp;<A HREF="BluetoothConnectionException.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/bluetooth/BluetoothConnectionException.html" target="_top"><B>NO FRAMES</B></A>  &nbsp;
 * &nbsp;<SCRIPT type="text/javascript">
 * <!--
 * if(window==top) {
 * document.writeln('<A HREF="../../allclasses-noframe.html"tpa=http://java.sun.com/javame/reference/apis/jsr082/allclasses-noframe.html><B>All Classes</B></A>');
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
 * SUMMARY:&nbsp;NESTED&nbsp;|&nbsp;<A HREF="#field_summary">FIELD</A>&nbsp;|&nbsp;<A HREF="#constructor_summary">CONSTR</A>&nbsp;|&nbsp;<A HREF="#method_summary">METHOD</A></FONT></TD>
 * <TD VALIGN="top" CLASS="NavBarCell3"><FONT SIZE="-2">
 * DETAIL:&nbsp;<A HREF="#field_detail">FIELD</A>&nbsp;|&nbsp;<A HREF="#constructor_detail">CONSTR</A>&nbsp;|&nbsp;<A HREF="#method_detail">METHOD</A></FONT></TD>
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
public class BluetoothConnectionException extends java.io.IOException
{
	/**
	 * Indicates the connection to the server failed because no service for
	 * the given PSM was registered.
	 * <P>
	 * The value for <code>UNKNOWN_PSM</code> is 0x0001 (1).
	 * <P>
	 * <DT><B>See Also:</B>
	 * 
	 */
	public static final int UNKNOWN_PSM = 0x0001;

	/**
	 * Indicates the connection failed because the security
	 * settings on the local device or the remote device were
	 * incompatible with the request.
	 * <P>
	 * The value for <code>SECURITY_BLOCK</code> is 0x0002 (2).
	 * <P>
	 * <DT><B>See Also:</B>
	 * 
	 */
	public static final int SECURITY_BLOCK =  0x0002;

	/**
	 * Indicates the connection failed due to a lack of resources
	 * either on the local device or on the remote device.
	 * <P>
	 * The value for <code>NO_RESOURCES</code> is 0x0003 (3).
	 * <P>
	 * <DT><B>See Also:</B>
	 * 
	 */
	public static final int NO_RESOURCES =  0x0003;

	/**
	 * Indicates the connection to the server failed due to unknown reasons.
	 * <P>
	 * The value for <code>FAILED_NOINFO</code> is 0x0004 (4).
	 * <P>
	 * <DT><B>See Also:</B>
	 * 
	 */
	public static final int FAILED_NOINFO = 0x0004;

	/**
	 * Indicates the connection to the server failed due to a timeout.
	 * <P>
	 * The value for <code>TIMEOUT</code> is 0x0005 (5).
	 * <P>
	 * <DT><B>See Also:</B>
	 * 
	 */
	public static final int TIMEOUT =  0x0005;

	/**
	 * Indicates the connection failed because the configuration
	 * parameters provided were not acceptable to either the remote device or
	 * the local device.
	 * <P>
	 * The value for <code>UNACCEPTABLE_PARAMS</code> is 0x0006 (6).
	 * <P>
	 * <DT><B>See Also:</B>
	 * 
	 * 
	 */
	public static final int UNACCEPTABLE_PARAMS = 0x0006;

	//following variables are implicitely defined by getter- or setter-methods:
	private int status;

	/**
	 * Creates a new <code>BluetoothConnectionException</code>
	 * with the error indicator specified.
	 * <P>
	 * 
	 * @param error - indicates the exception condition; must be one of the constants described in this class
	 * @throws java.lang.IllegalArgumentException - if the input value is not one of the constants in this class
	 */
	public BluetoothConnectionException(int error)
	{
		//TODO implement BluetoothConnectionException
	}

	/**
	 * Creates a new <code>BluetoothConnectionException</code>
	 * with the error indicator and message specified.
	 * <P>
	 * 
	 * @param error - indicates the exception condition; must be one of the constants described in this class
	 * @param msg - a description of the exception; may by null
	 * @throws java.lang.IllegalArgumentException - if the input value is not one of the constants in this class
	 */
	public BluetoothConnectionException(int error, java.lang.String msg)
	{
		//TODO implement BluetoothConnectionException
	}

	/**
	 * Gets the status set in the constructor that will indicate
	 * the reason for the exception.
	 * <P>
	 * 
	 * 
	 * @return cause for the exception; will be one of the constants defined in this class
	 */
	public int getStatus()
	{
		return this.status;
	}

}
