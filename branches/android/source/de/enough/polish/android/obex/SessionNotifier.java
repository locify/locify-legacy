//#condition polish.android
// generated by de.enough.doc2java.Doc2Java (www.enough.de) on Tue Mar 24 10:34:27 EET 2009
package de.enough.polish.android.obex;

import de.enough.polish.android.io.Connection;

/**
 * 
 * The <code>SessionNotifier</code> interface defines a connection notifier for
 * server-side OBEX connections.  When a <code>SessionNotifier</code> is
 * created and calls  <code>acceptAndOpen()</code>, it will begin listening for
 * clients to create a connection at the transport layer.  When the transport
 * layer connection is received, the <code>acceptAndOpen()</code> method will
 * return a  <code>javax.microedition.io.Connection</code> that is the
 * connection to the client.  The <code>acceptAndOpen()</code> method also takes
 * a <code>ServerRequestHandler</code> argument that will process the requests
 * from the client that connects to the server.
 * 
 * <DD>1.2</DD>
 * <HR>
 * 
 * 
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
 * <CODE>&nbsp;Connection</CODE></FONT></TD>
 * <TD><CODE><B><A HREF="SessionNotifier.html#acceptAndOpen(javax.obex.ServerRequestHandler)" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/obex/SessionNotifier.html#acceptAndOpen(javax.obex.ServerRequestHandler)">acceptAndOpen</A></B>(<A HREF="ServerRequestHandler.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/obex/ServerRequestHandler.html" title="class in javax.obex">ServerRequestHandler</A>&nbsp;handler)</CODE>
 * 
 * <BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Waits for a transport layer connection to be established and specifies
 * the handler to handle the requests from the client.</TD>
 * </TR>
 * <TR BGCOLOR="white" CLASS="TableRowColor">
 * <TD ALIGN="right" VALIGN="top" WIDTH="1%"><FONT SIZE="-1">
 * <CODE>&nbsp;Connection</CODE></FONT></TD>
 * <TD><CODE><B><A HREF="SessionNotifier.html#acceptAndOpen(javax.obex.ServerRequestHandler, javax.obex.Authenticator)" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/obex/SessionNotifier.html#acceptAndOpen(javax.obex.ServerRequestHandler, javax.obex.Authenticator)">acceptAndOpen</A></B>(<A HREF="ServerRequestHandler.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/obex/ServerRequestHandler.html" title="class in javax.obex">ServerRequestHandler</A>&nbsp;handler,
 * <A HREF="Authenticator.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/obex/Authenticator.html" title="interface in javax.obex">Authenticator</A>&nbsp;auth)</CODE>
 * 
 * <BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Waits for a transport layer connection to be established and specifies
 * the handler to handle the requests from the client and the
 * <code>Authenticator</code> to use to respond to authentication challenge
 * and authentication response headers.</TD>
 * </TR>
 * </TABLE>
 * &nbsp;
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
 * <A NAME="acceptAndOpen(javax.obex.ServerRequestHandler)"><!-- --></A><H3>
 * acceptAndOpen</H3>
 * <PRE>
 * Connection <B>acceptAndOpen</B>(<A HREF="ServerRequestHandler.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/obex/ServerRequestHandler.html" title="class in javax.obex">ServerRequestHandler</A>&nbsp;handler)
 * throws java.io.IOException</PRE>
 * <DD>Waits for a transport layer connection to be established and specifies
 * the handler to handle the requests from the client.  No authenticator
 * is associated with this connection, therefore, it is implementation
 * dependent as to how an authentication challenge and authentication
 * response header will be received and processed.
 * <H4>Additional Note for OBEX over Bluetooth</H4>
 * If this method is called on a <code>SessionNotifier</code> object that
 * does not have a <code>ServiceRecord</code> in the SDDB, the
 * <code>ServiceRecord</code> for this object will be added to the SDDB.
 * This method requests the BCC to put the
 * local device in connectable mode so that it will respond to
 * connection attempts by clients.
 * The following checks are done to verify that the service record
 * provided is valid. If any of these checks fail, then a
 * <code>ServiceRegistrationException</code> is thrown.
 * <UL>
 * <LI>ServiceClassIDList and ProtocolDescriptorList, the mandatory
 * service attributes for a <code>btgoep</code> service record, must be
 * present in the <code>ServiceRecord</code> associated with this notifier.
 * <LI>L2CAP, RFCOMM and OBEX must all be in the ProtocolDescriptorList
 * <LI>The <code>ServiceRecord</code> associated with this notifier must
 * not have changed the RFCOMM server channel number
 * </UL>
 * This method will not ensure that <code>ServiceRecord</code> associated
 * with this notifier is a completely
 * valid service record. It is the responsibility of the application to
 * ensure that the service record follows all of the applicable
 * syntactic and semantic rules for service record correctness.
 * <DD><CODE>java.io.IOException</CODE> - if an error occurs in the transport layer
 * <DD><CODE>java.lang.NullPointerException</CODE> - if <code>handler</code> is
 * <code>null</code>
 * <DD><CODE><A HREF="../bluetooth/ServiceRegistrationException.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/bluetooth/ServiceRegistrationException.html" title="class in javax.bluetooth">ServiceRegistrationException</A></CODE> - if the structure
 * of the associated service record is invalid or if the service record
 * could not be added successfully to the local SDDB.  The
 * structure of service record is invalid if the service
 * record is missing any mandatory service attributes, or has
 * changed any of the values described above which are fixed and
 * cannot be changed. Failures to add the record to the SDDB could
 * be due to insufficient disk space, database locks, etc.
 * <DD><CODE><A HREF="../bluetooth/BluetoothStateException.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/bluetooth/BluetoothStateException.html" title="class in javax.bluetooth">BluetoothStateException</A></CODE> - if the server device
 * could not be placed in connectable mode because the device user has
 * configured the device to be non-connectable</DL>
 * <HR>
 * 
 * <A NAME="acceptAndOpen(javax.obex.ServerRequestHandler, javax.obex.Authenticator)"><!-- --></A><H3>
 * acceptAndOpen</H3>
 * <PRE>
 * Connection <B>acceptAndOpen</B>(<A HREF="ServerRequestHandler.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/obex/ServerRequestHandler.html" title="class in javax.obex">ServerRequestHandler</A>&nbsp;handler,
 * <A HREF="Authenticator.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/obex/Authenticator.html" title="interface in javax.obex">Authenticator</A>&nbsp;auth)
 * throws java.io.IOException</PRE>
 * <DD>Waits for a transport layer connection to be established and specifies
 * the handler to handle the requests from the client and the
 * <code>Authenticator</code> to use to respond to authentication challenge
 * and authentication response headers.
 * <H4>Additional Note for OBEX over Bluetooth</H4>
 * If this method is called on a <code>SessionNotifier</code> object that
 * does not have a <code>ServiceRecord</code> in the SDDB, the
 * <code>ServiceRecord</code> for this object will be added to the SDDB.
 * This method requests the BCC to put the
 * local device in connectable mode so that it will respond to
 * connection attempts by clients.
 * The following checks are done to verify that the service record
 * provided is valid. If any of these checks fail, then a
 * <code>ServiceRegistrationException</code> is thrown.
 * <UL>
 * <LI>ServiceClassIDList and ProtocolDescriptorList, the mandatory
 * service attributes for a <code>btgoep</code> service record, must be
 * present in the <code>ServiceRecord</code> associated with this notifier.
 * <LI>L2CAP, RFCOMM and OBEX must all be in the ProtocolDescriptorList
 * <LI>The <code>ServiceRecord</code> associated with this notifier must
 * not have changed the RFCOMM server channel number
 * </UL>
 * This method will not ensure that <code>ServiceRecord</code> associated
 * with this notifier is a completely
 * valid service record. It is the responsibility of the application to
 * ensure that the service record follows all of the applicable
 * syntactic and semantic rules for service record correctness.
 * if <code>null</code> then no <code>Authenticator</code> will be used
 * <DD><CODE>java.io.IOException</CODE> - if an error occurs in the transport layer
 * <DD><CODE>java.lang.NullPointerException</CODE> - if <code>handler</code> is
 * <code>null</code>
 * <DD><CODE><A HREF="../bluetooth/ServiceRegistrationException.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/bluetooth/ServiceRegistrationException.html" title="class in javax.bluetooth">ServiceRegistrationException</A></CODE> - if the structure
 * of the associated service record is invalid or if the service record
 * could not be added successfully to the local SDDB.  The
 * structure of service record is invalid if the service
 * record is missing any mandatory service attributes, or has
 * changed any of the values described above which are fixed and
 * cannot be changed. Failures to add the record to the SDDB could
 * be due to insufficient disk space, database locks, etc.
 * <DD><CODE><A HREF="../bluetooth/BluetoothStateException.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/bluetooth/BluetoothStateException.html" title="class in javax.bluetooth">BluetoothStateException</A></CODE> - if the server device
 * could not be placed in connectable mode because the device user has
 * configured the device to be non-connectable</DL>
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
 * <TD BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    <A HREF="package-summary.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/obex/package-summary.html"><FONT CLASS="NavBarFont1"><B>Package</B></FONT></A>&nbsp;</TD>
 * <TD BGCOLOR="#FFFFFF" CLASS="NavBarCell1Rev"> &nbsp;<FONT CLASS="NavBarFont1Rev"><B>Class</B></FONT>&nbsp;</TD>
 * <TD BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    <A HREF="package-tree.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/obex/package-tree.html"><FONT CLASS="NavBarFont1"><B>Tree</B></FONT></A>&nbsp;</TD>
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
 * &nbsp;<A HREF="ServerRequestHandler.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/obex/ServerRequestHandler.html" title="class in javax.obex"><B>PREV CLASS</B></A>&nbsp;
 * &nbsp;NEXT CLASS</FONT></TD>
 * <TD BGCOLOR="white" CLASS="NavBarCell2"><FONT SIZE="-2">
 * <A HREF="../../index.html-javax-obex-SessionNotifier.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/index.html?javax/obex/SessionNotifier.html" target="_top"><B>FRAMES</B></A>  &nbsp;
 * &nbsp;<A HREF="SessionNotifier.html" tppabs="http://java.sun.com/javame/reference/apis/jsr082/javax/obex/SessionNotifier.html" target="_top"><B>NO FRAMES</B></A>  &nbsp;
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
 * SUMMARY:&nbsp;NESTED&nbsp;|&nbsp;FIELD&nbsp;|&nbsp;CONSTR&nbsp;|&nbsp;<A HREF="#method_summary">METHOD</A></FONT></TD>
 * <TD VALIGN="top" CLASS="NavBarCell3"><FONT SIZE="-2">
 * DETAIL:&nbsp;FIELD&nbsp;|&nbsp;CONSTR&nbsp;|&nbsp;<A HREF="#method_detail">METHOD</A></FONT></TD>
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
public interface SessionNotifier
{
	/**
	 * Waits for a transport layer connection to be established and specifies
	 * the handler to handle the requests from the client.  No authenticator
	 * is associated with this connection, therefore, it is implementation
	 * dependent as to how an authentication challenge and authentication
	 * response header will be received and processed.
	 * <P>
	 * <H4>Additional Note for OBEX over Bluetooth</H4>
	 * If this method is called on a <code>SessionNotifier</code> object that
	 * does not have a <code>ServiceRecord</code> in the SDDB, the
	 * <code>ServiceRecord</code> for this object will be added to the SDDB.
	 * This method requests the BCC to put the
	 * local device in connectable mode so that it will respond to
	 * connection attempts by clients.
	 * <P>
	 * The following checks are done to verify that the service record
	 * provided is valid. If any of these checks fail, then a
	 * <code>ServiceRegistrationException</code> is thrown.
	 * <UL>
	 * <LI>ServiceClassIDList and ProtocolDescriptorList, the mandatory
	 * service attributes for a <code>btgoep</code> service record, must be
	 * present in the <code>ServiceRecord</code> associated with this notifier.
	 * <LI>L2CAP, RFCOMM and OBEX must all be in the ProtocolDescriptorList
	 * <LI>The <code>ServiceRecord</code> associated with this notifier must
	 * not have changed the RFCOMM server channel number
	 * </UL>
	 * <P>
	 * This method will not ensure that <code>ServiceRecord</code> associated
	 * with this notifier is a completely
	 * valid service record. It is the responsibility of the application to
	 * ensure that the service record follows all of the applicable
	 * syntactic and semantic rules for service record correctness.
	 * <P>
	 * 
	 * @param handler - the request handler that will respond to OBEX requests
	 * @return the connection to the client
	 * @throws java.io.IOException - if an error occurs in the transport layer
	 * @throws java.lang.NullPointerException - if handler is null
	 * @throws ServiceRegistrationException - if the structure of the associated service record is invalid or if the service record could not be added successfully to the local SDDB.  The structure of service record is invalid if the service record is missing any mandatory service attributes, or has changed any of the values described above which are fixed and cannot be changed. Failures to add the record to the SDDB could be due to insufficient disk space, database locks, etc.
	 * @throws BluetoothStateException - if the server device could not be placed in connectable mode because the device user has configured the device to be non-connectable
	 */
	Connection acceptAndOpen( ServerRequestHandler handler) throws java.io.IOException;

	/**
	 * Waits for a transport layer connection to be established and specifies
	 * the handler to handle the requests from the client and the
	 * <code>Authenticator</code> to use to respond to authentication challenge
	 * and authentication response headers.
	 * <P>
	 * <H4>Additional Note for OBEX over Bluetooth</H4>
	 * If this method is called on a <code>SessionNotifier</code> object that
	 * does not have a <code>ServiceRecord</code> in the SDDB, the
	 * <code>ServiceRecord</code> for this object will be added to the SDDB.
	 * This method requests the BCC to put the
	 * local device in connectable mode so that it will respond to
	 * connection attempts by clients.
	 * <P>
	 * The following checks are done to verify that the service record
	 * provided is valid. If any of these checks fail, then a
	 * <code>ServiceRegistrationException</code> is thrown.
	 * <UL>
	 * <LI>ServiceClassIDList and ProtocolDescriptorList, the mandatory
	 * service attributes for a <code>btgoep</code> service record, must be
	 * present in the <code>ServiceRecord</code> associated with this notifier.
	 * <LI>L2CAP, RFCOMM and OBEX must all be in the ProtocolDescriptorList
	 * <LI>The <code>ServiceRecord</code> associated with this notifier must
	 * not have changed the RFCOMM server channel number
	 * </UL>
	 * <P>
	 * This method will not ensure that <code>ServiceRecord</code> associated
	 * with this notifier is a completely
	 * valid service record. It is the responsibility of the application to
	 * ensure that the service record follows all of the applicable
	 * syntactic and semantic rules for service record correctness.
	 * <P>
	 * 
	 * @param handler - the request handler that will respond to OBEX requests
	 * @param auth - the Authenticator to use with this connection; if null then no Authenticator will be used
	 * @return the connection to the client
	 * @throws java.io.IOException - if an error occurs in the transport layer
	 * @throws java.lang.NullPointerException - if handler is null
	 * @throws ServiceRegistrationException - if the structure of the associated service record is invalid or if the service record could not be added successfully to the local SDDB.  The structure of service record is invalid if the service record is missing any mandatory service attributes, or has changed any of the values described above which are fixed and cannot be changed. Failures to add the record to the SDDB could be due to insufficient disk space, database locks, etc.
	 * @throws BluetoothStateException - if the server device could not be placed in connectable mode because the device user has configured the device to be non-connectable
	 */
	Connection acceptAndOpen( ServerRequestHandler handler, Authenticator auth) throws java.io.IOException;

}
