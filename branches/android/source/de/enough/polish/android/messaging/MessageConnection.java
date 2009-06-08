//#condition polish.android
// generated by de.enough.doc2java.Doc2Java (www.enough.de) on Wed Mar 25 13:57:35 CET 2009
package de.enough.polish.android.messaging;

import de.enough.polish.android.io.Connection;

/**
 * The <code>MessageConnection</code> interface defines the basic functionality
 * for sending and receiving messages. It contains methods for sending and
 * receiving messages, factory methods to create a new <code>Message</code>
 * object, and a method
 * that calculates the number of segments of the underlying protocol that are
 * needed to send a specified <code>Message</code> object.
 * <p>
 * This class is instantiated by a call to <code>Connector.open()</code>.
 * An application SHOULD call <code>close()</code> when it
 * is finished with the connection. An <code>IOException</code> is thrown
 * when any methods are called on the <code>MessageConnection</code>
 * after the connection has been closed.
 * <p> Messages are sent on a connection.
 * A connection can be defined as <em>server</em> mode or <em>client</em> mode.
 * </p>
 * <p>In a <em>client</em> mode connection, messages can only be sent.
 * A client mode connection is created by passing a string
 * identifying a destination
 * address to the <code>Connector.open()</code> method.
 * This method returns a <code>MessageConnection</code> object.
 * </p>
 * <p>In a <em>server</em> mode connection, messages can be sent or received.
 * A server mode connection
 * is created by passing a string that identifies an end point (protocol
 * dependent identifier, for example, a port number)
 * on the local host to the <code>Connector.open()</code> method.
 * If the requested
 * end point identifier is already reserved, either by some system
 * application or by another Java application,
 * <code>Connector.open()</code> throws an
 * <code>IOException</code>. Java applications can open
 * <code>MessageConnection</code>s
 * for any unreserved end point identifier, although security permissions might
 * not allow it to send or receive messages using that end point identifier.
 * </p>
 * <p>The <em>scheme</em> that identifies which protocol is used is specific
 * to the given protocol. This interface does not assume any
 * specific protocol and is intended for all wireless messaging
 * protocols.
 * </p>
 * <p>An application can have several <code>MessageConnection</code>
 * instances open simultaneously; these connections can be
 * both client and server mode.
 * </p>
 * <p>The application can create a class that implements the
 * <code>MessageListener</code> interface and register an instance
 * of that class with the <code>MessageConnection</code>(s)
 * to be notified of incoming messages. With this technique,
 * a thread does not have to be blocked, waiting to receive messages.
 * </p>
 * 
 * 
 */
public interface MessageConnection extends Connection
{
	/**
	 * Constant for a message type for <strong>text</strong>
	 * messages (value = "text").
	 * If this constant is used for the <tt>type</tt> parameter in the
	 * <code>newMessage()</code>
	 * methods, then the newly created <code>Message</code>
	 * will be an instance
	 * implementing the <code>TextMessage</code> interface.</DL>
	 * 
	 */
	public static final java.lang.String TEXT_MESSAGE = "text";

	/**
	 * Constant for a message type for <strong>binary</strong>
	 * messages (value = "binary").
	 * If this constant is used for the <tt>type</tt> parameter in the
	 * <code>newMessage()</code>
	 * methods, then the newly created <code>Message</code>
	 * will be an instance
	 * implementing the <code>BinaryMessage</code> interface.</DL>
	 * 
	 * 
	 */
	public static final java.lang.String BINARY_MESSAGE = "binary";

	/**
	 * Constructs a new message object of a given type. When the
	 * string <code>text</code> is passed in, the created
	 * object implements the <code>TextMessage</code> interface.
	 * When the <code>binary</code> constant is passed in, the
	 * created object implements the <code>BinaryMessage</code>
	 * interface. Adapter definitions for messaging protocols can define
	 * new constants and new subinterfaces for the <code>Message</code>s.
	 * The type strings are case-sensitive.
	 * 
	 * <p>For adapter definitions that are not defined within the JCP
	 * process, the strings used MUST begin with an inverted domain
	 * name controlled by the defining organization, as is
	 * used for Java package names. Strings that do not contain a
	 * full stop character "." are reserved for specifications done
	 * within the JCP process and MUST NOT be used by other organizations
	 * defining adapter specification.
	 * </p>
	 * <p>When this method is called from a <em>client</em> mode connection,
	 * the newly created <code>Message</code> has the destination address
	 * set to the address identified when this <code>Connection</code>
	 * was created.
	 * </p>
	 * <p>When this method is called from a <em>server</em> mode connection,
	 * the newly created <code>Message</code> does not have the destination
	 * address set. It must be set by the application before
	 * trying to send the message.
	 * </p>
	 * 
	 * @param type - the type of message to be created. There are constants for basic types defined in this interface.
	 * @return Message object for a given type of message
	 * @throws java.lang.IllegalArgumentException - if the message type is not TEXT_MESSAGE or BINARY_MESSAGE
	 */
	public Message newMessage(java.lang.String type);

	/**
	 * Constructs a new <code>Message</code> object of a given type and
	 * initializes it with the given destination address.
	 * The semantics related to the parameter <code>type</code>
	 * are the same as for the method signature with just the
	 * <code>type</code> parameter.
	 * 
	 * @param type - the type of message to be created. There are constants for basic types defined in this interface.
	 * @param address - destination address for the new message
	 * @return Message object for a given type of message
	 * @throws java.lang.IllegalArgumentException - if the message type is not TEXT_MESSAGE or BINARY_MESSAGE
	 * @see #newMessage(String type)
	 */
	public Message newMessage(java.lang.String type, java.lang.String address);

	/**
	 * Sends a message.
	 * 
	 * @param msg - the message to be sent
	 * @throws java.io.IOException - if the message could not be sent or because of network failure
	 * @throws java.lang.IllegalArgumentException - if the message is incomplete or contains invalid information. This exception is also thrown if the payload of the message exceeds the maximum length for the given messaging protocol.
	 * @throws java.io.InterruptedIOException - if a timeout occurs while either trying to send the message or if this Connection object is closed during this send operation
	 * @throws java.lang.NullPointerException - if the parameter is null
	 * @throws java.lang.SecurityException - if the application does not have permission to send the message
	 * @see #receive()
	 */
	public void send( Message msg) throws java.io.IOException, java.io.InterruptedIOException;

	/**
	 * Receives a message.
	 * 
	 * <p>If there are no <code>Message</code>s for this
	 * <code>MessageConnection</code> waiting,
	 * this method will block until either a message for this <code>Connection</code>
	 * is received or the <code>MessageConnection</code> is closed.
	 * </p>
	 * 
	 * @return a Message object representing the information in the received message
	 * @throws java.io.IOException - if an error occurs while receiving a message
	 * @throws java.io.InterruptedIOException - if this MessageConnection object is closed during this receive method call
	 * @throws java.lang.SecurityException - if the application does not have permission to receive messages using the given port number
	 * @see #send(Message)
	 */
	public Message receive() throws java.io.IOException, java.io.InterruptedIOException;

	/**
	 * Registers a <code>MessageListener</code> object that the platform
	 * can notify when a message has been received on this
	 * <code>MessageConnection</code>.
	 * <p></p>
	 * <p>If there are incoming messages in the queue of this
	 * <code>MessageConnection</code> that have not been retrieved by
	 * the application prior to calling this method, the newly
	 * registered listener object will be notified immediately once
	 * for each such incoming message in the queue.
	 * </p>
	 * <p>There can be at most one listener object registered for
	 * a <code>MessageConnection</code> object at any given point in time.
	 * Setting a new listener will de-register any
	 * previously set listener.
	 * </p>
	 * <p>Passing <code>null</code> as the parameter will de-register
	 * any currenly
	 * registered listener.
	 * </p>
	 * 
	 * @param l - MessageListener object to be registered. If null, any currently registered listener will be de-registered and will not receive notifications.
	 * @throws java.lang.SecurityException - if the application does not have permission to receive messages using the given port number
	 * @throws java.io.IOException - if the connection has been closed, or if an attempt is made to register a listener on a client connection
	 */
	public void setMessageListener( MessageListener l) throws java.io.IOException;

	/**
	 * Returns the number of segments in the underlying protocol that would
	 * be needed for sending the specified <code>Message</code>.
	 * 
	 * <p>Note that this method does not actually send the message.
	 * It will only calculate the number of protocol segments
	 * needed for sending the message.
	 * </p>
	 * <p>This method will calculate the number of segments
	 * needed when this message is split into the protocol
	 * segments using the appropriate features of the underlying protocol.
	 * This method does not take into account possible limitations
	 * of the implementation that may limit the number of
	 * segments that can be sent using this feature. These
	 * limitations are protocol-specific and are documented
	 * with the adapter definition for that protocol.
	 * </p>
	 * 
	 * @param msg - the message to be used for the calculation
	 * @return number of protocol segments needed for sending the message. Returns 0 if the Message object cannot be sent using the underlying protocol.
	 */
	public int numberOfSegments( Message msg);

}
