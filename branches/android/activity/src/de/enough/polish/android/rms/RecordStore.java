//#condition polish.android
// generated by de.enough.doc2java.Doc2Java (www.enough.de) on Tue Mar 25 12:43:45 CET 2008
package de.enough.polish.android.rms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.enough.polish.util.HashMap;

/**
 * A class representing a record store. A class representing a record store. A
 * record store consists of a collection of records which will remain persistent
 * across multiple invocations of the MIDlet. The platform is responsible for
 * making its best effort to maintain the integrity of the MIDlet's record
 * stores throughout the normal use of the platform, including reboots, battery
 * changes, etc.
 * <p>
 * Record stores are created in platform-dependent locations, which are not
 * exposed to the MIDlets. The naming space for record stores is controlled at
 * the MIDlet suite granularity. MIDlets within a MIDlet suite are allowed to
 * create multiple record stores, as long as they are each given different
 * names. When a MIDlet suite is removed from a platform all the record stores
 * associated with its MIDlets will also be removed. MIDlets within a MIDlet
 * suite can access each other's record stores directly. New APIs in MIDP 2.0
 * allow for the explicit sharing of record stores if the MIDlet creating the
 * RecordStore chooses to give such permission.
 * </p>
 * <p>
 * Sharing is accomplished through the ability to name a RecordStore created by
 * another MIDlet suite.
 * </p>
 * <P>
 * RecordStores are uniquely named using the unique name of the MIDlet suite
 * plus the name of the RecordStore. MIDlet suites are identified by the
 * MIDlet-Vendor and MIDlet-Name attributes from the application descriptor.
 * </p>
 * <p>
 * Access controls are defined when RecordStores to be shared are created.
 * Access controls are enforced when RecordStores are opened. The access modes
 * allow private use or shareable with any other MIDlet suite.
 * </p>
 * <p>
 * Record store names are case sensitive and may consist of any combination of
 * between one and 32 Unicode characters inclusive. Record store names must be
 * unique within the scope of a given MIDlet suite. In other words, MIDlets
 * within a MIDlet suite are not allowed to create more than one record store
 * with the same name, however a MIDlet in one MIDlet suite is allowed to have a
 * record store with the same name as a MIDlet in another MIDlet suite. In that
 * case, the record stores are still distinct and separate.
 * </p>
 * <p>
 * No locking operations are provided in this API. Record store implementations
 * ensure that all individual record store operations are atomic, synchronous,
 * and serialized, so no corruption will occur with multiple accesses. However,
 * if a MIDlet uses multiple threads to access a record store, it is the
 * MIDlet's responsibility to coordinate this access or unintended consequences
 * may result. Similarly, if a platform performs transparent synchronization of
 * a record store, it is the platform's responsibility to enforce exclusive
 * access to the record store between the MIDlet and synchronization engine.
 * </p>
 * <p>
 * Records are uniquely identified within a given record store by their
 * recordId, which is an integer value. This recordId is used as the primary key
 * for the records. The first record created in a record store will have
 * recordId equal to one (1). Each subsequent record added to a RecordStore will
 * be assigned a recordId one greater than the record added before it. That is,
 * if two records are added to a record store, and the first has a recordId of
 * 'n', the next will have a recordId of 'n + 1'. MIDlets can create other
 * sequences of the records in the RecordStore by using the
 * <code>RecordEnumeration</code> class.
 * </p>
 * <p>
 * This record store uses long integers for time/date stamps, in the format used
 * by System.currentTimeMillis(). The record store is time stamped with the last
 * time it was modified. The record store also maintains a <em>version</em>
 * number, which is an integer that is incremented for each operation that
 * modifies the contents of the RecordStore. These are useful for
 * synchronization engines as well as other things.
 * </p>
 */
public class RecordStore extends Object {
	/**
	 * Authorization to allow access only to the current MIDlet suite.
	 * AUTHMODE_PRIVATE has a value of 0.
	 * <P>
	 * <DT><B>See Also:</B>
	 */
	public static final int AUTHMODE_PRIVATE = 0;

	/**
	 * Authorization to allow access to any MIDlet suites. AUTHMODE_ANY has a
	 * value of 1.
	 * <P>
	 * <DT><B>See Also:</B>
	 */
	public static final int AUTHMODE_ANY = 1;

	private static SqlDao sqlDao;
	
	/**
	 * This mapping contains all open record stores by name. If a record store is not in this mapping, it is not open.
	 */
	private static HashMap<String, RecordStore> openedRecordStores = new HashMap<String, RecordStore>();

	/**
	 * The unique name of this record store.
	 */
	private String name;

	/**
	 * The version of this object. It changes whenever it is modified.
	 */
	private int version;
	/**
	 * The id of this record store in the database.
	 */
	long recordStorePk;
	// TODO: Use the next to field.
//	private String vendorName;
//	private String suiteName;
	private int numRecords;
	private int size;
	private long sizeAvailable = 4 * 1024 * 1024;

	// TODO: Manage lastModified everywhere.
	private long lastModified;
	private int nextRecordID = 1;
	
	/**
	 * The number of times this record store was opened. If this number is 0 then this record store is closed.
	 */
	private int openCount = 0;
	// TODO: Manage authMode everwhere.
	private int authMode;
	// TODO: Manage writable everywhere.
//	private boolean writable = false;

	private List<RecordListener> listeners = new ArrayList<RecordListener>();

	/**
	 * @param name the unique name of this record store within a midlet suite
	 * @param pk the database primary key of this store
	 */
	public RecordStore(String name, long pk) {
		this.name = name;
		this.recordStorePk = pk;
	}

	/**
	 * Deletes the named record store. MIDlet suites are only allowed to delete
	 * their own record stores. If the named record store is open (by a MIDlet
	 * in this suite or a MIDlet in a different MIDlet suite) when this method
	 * is called, a RecordStoreException will be thrown. If the named record
	 * store does not exist a RecordStoreNotFoundException will be thrown.
	 * Calling this method does NOT result in recordDeleted calls to any
	 * registered listeners of this RecordStore.
	 * <P>
	 * @param recordStoreName - the MIDlet suite unique record store to delete
	 * @throws RecordStoreException - if a record store-related exception
	 * occurred
	 * @throws RecordStoreNotFoundException - if the record store could not be
	 * found
	 */
	public static void deleteRecordStore(String recordStoreName) throws RecordStoreException, RecordStoreNotFoundException {
		RecordStore recordStore = getOpenedRecordStoreFromCache(recordStoreName);
		if (recordStore != null) {
			throw new RecordStoreException("The record store '" + recordStoreName + "' is not closed.");
		}
		sqlDao.deleteRecordStore(recordStoreName);

	}

	/**
	 * Open (and possibly create) a record store associated with the given
	 * MIDlet suite. If this method is called by a MIDlet when the record store
	 * is already open by a MIDlet in the MIDlet suite, this method returns a
	 * reference to the same RecordStore object.
	 * <P>
	 * @param recordStoreName - the MIDlet suite unique name for the record
	 * store, consisting of between one and 32 Unicode characters inclusive.
	 * @param createIfNecessary - if true, the record store will be created if
	 * necessary
	 * @return RecordStore object for the record store
	 * @throws RecordStoreException - if a record store-related exception
	 * occurred
	 * @throws RecordStoreNotFoundException - if the record store could not be
	 * found
	 * @throws RecordStoreFullException - if the operation cannot be completed
	 * because the record store is full
	 * @throws IllegalArgumentException - if recordStoreName is invalid
	 */
	public static RecordStore openRecordStore(String recordStoreName, boolean createIfNecessary) throws RecordStoreException, RecordStoreFullException, RecordStoreNotFoundException {
		init();
		if (recordStoreName == null) {
			throw new IllegalArgumentException("Parameter 'recordStoreName' must not be null or empty.");
		}

		if (recordStoreName.length() < 1 || recordStoreName.length() > 32) {
			throw new IllegalArgumentException("Parameter 'recordStoreName' must have a length between 1 and 32.");
		}
		
		// Try the cache.
		RecordStore recordStore = openRecordStoreFromCache(recordStoreName);
		if (recordStore != null) {
			return recordStore;
		}
		
		// Try the datadabase.
		recordStore = sqlDao.getRecordStore(recordStoreName);
		if (recordStore != null) {
			cacheRecordStore(recordStoreName,recordStore);
			return recordStore;
		}
		if (!createIfNecessary) {
			throw new RecordStoreNotFoundException("No record store with name '" + recordStoreName + "' found.");
		}
		
		// Create the database.
		recordStore = sqlDao.createRecordStore(recordStoreName);
		if (recordStore == null) {
			throw new RecordStoreException("Could not create record store with name '" + recordStoreName + "'. Reason: The method 'SqlDao.createRecordStore' returned null although it is not allowed to do so.");
		}
		cacheRecordStore(recordStoreName, recordStore);
		return recordStore;
	}

	/**
	 * Open (and possibly create) a record store that can be shared with other
	 * MIDlet suites. The RecordStore is owned by the current MIDlet suite. The
	 * authorization mode is set when the record store is created, as follows:
	 * <ul>
	 * <li><code>AUTHMODE_PRIVATE</code> - Only allows the MIDlet suite that
	 * created the RecordStore to access it. This case behaves identically to
	 * <code>openRecordStore(recordStoreName, createIfNecessary)</code>.</li>
	 * <li><code>AUTHMODE_ANY</code> - Allows any MIDlet to access the
	 * RecordStore. Note that this makes your recordStore accessible by any
	 * other MIDlet on the device. This could have privacy and security issues
	 * depending on the data being shared. Please use carefully.</li>
	 * </ul>
	 * <p>
	 * The owning MIDlet suite may always access the RecordStore and always has
	 * access to write and update the store.
	 * </p>
	 * <p>
	 * If this method is called by a MIDlet when the record store is already
	 * open by a MIDlet in the MIDlet suite, this method returns a reference to
	 * the same RecordStore object.
	 * </p>
	 * <P>
	 * @param recordStoreName - the MIDlet suite unique name for the record
	 * store, consisting of between one and 32 Unicode characters inclusive.
	 * @param createIfNecessary - if true, the record store will be created if
	 * necessary
	 * @param authmode - the mode under which to check or create access. Must be
	 * one of AUTHMODE_PRIVATE or AUTHMODE_ANY. This argument is ignored if the
	 * RecordStore exists.
	 * @param writable - true if the RecordStore is to be writable by other
	 * MIDlet suites that are granted access. This argument is ignored if the
	 * RecordStore exists.
	 * @return RecordStore object for the record store
	 * @throws RecordStoreException - if a record store-related exception
	 * occurred
	 * @throws RecordStoreNotFoundException - if the record store could not be
	 * found
	 * @throws RecordStoreFullException - if the operation cannot be completed
	 * because the record store is full
	 * @throws IllegalArgumentException - if authmode or recordStoreName is
	 * invalid
	 * @since MIDP 2.0
	 */
	public static RecordStore openRecordStore(String recordStoreName, boolean createIfNecessary, int authmode, boolean writable) throws RecordStoreException, RecordStoreFullException, RecordStoreNotFoundException {
		return openRecordStore(recordStoreName, createIfNecessary);
	}

	/**
	 * Open a record store associated with the named MIDlet suite. The MIDlet
	 * suite is identified by MIDlet vendor and MIDlet name. Access is granted
	 * only if the authorization mode of the RecordStore allows access by the
	 * current MIDlet suite. Access is limited by the authorization mode set
	 * when the record store was created:
	 * <ul>
	 * <li><code>AUTHMODE_PRIVATE</code> - Succeeds only if vendorName and
	 * suiteName identify the current MIDlet suite; this case behaves
	 * identically to <code>openRecordStore(recordStoreName, createIfNecessary)
	 * </code>.</li> <li><code>AUTHMODE_ANY</code> - Always succeeds. Note that
	 * this makes your recordStore accessible by any other MIDlet on the device.
	 * This could have privacy and security issues depending on the data being
	 * shared. Please use carefully. Untrusted MIDlet suites are allowed to
	 * share data but this is not recommended. The authenticity of the origin of
	 * untrusted MIDlet suites cannot be verified so shared data may be used
	 * unscrupulously.</li>
	 * </ul>
	 * <p>
	 * If this method is called by a MIDlet when the record store is already
	 * open by a MIDlet in the MIDlet suite, this method returns a reference to
	 * the same RecordStore object.
	 * </p>
	 * <p>
	 * If a MIDlet calls this method to open a record store from its own suite,
	 * the behavior is identical to calling: <code><A
	 * HREF="RecordStore.html#openRecordStore(java.lang.String, boolean)"
	 * tppabs="http://java.sun.com/javame/reference/apis/jsr118/javax/microedition/rms/RecordStore.html#openRecordStore(java.lang.String, boolean)"
	 * ><CODE>openRecordStore(recordStoreName, false)</CODE></A></code>
	 * </p>
	 * <P>
	 * @param recordStoreName - the MIDlet suite unique name for the record
	 * store, consisting of between one and 32 Unicode characters inclusive.
	 * @param vendorName - the vendor of the owning MIDlet suite
	 * @param suiteName - the name of the MIDlet suite
	 * @return RecordStore object for the record store
	 * @throws RecordStoreException - if a record store-related exception
	 * occurred
	 * @throws RecordStoreNotFoundException - if the record store could not be
	 * found
	 * @throws SecurityException - if this MIDlet Suite is not allowed to open
	 * the specified RecordStore.
	 * @throws IllegalArgumentException - if recordStoreName is invalid
	 * @since MIDP 2.0
	 */
	public static RecordStore openRecordStore(String recordStoreName, String vendorName, String suiteName) throws RecordStoreException, RecordStoreNotFoundException {
		return openRecordStore(recordStoreName, true);
	}

	/**
	 * Changes the access mode for this RecordStore. The authorization mode
	 * choices are:
	 * <ul>
	 * <li><code>AUTHMODE_PRIVATE</code> - Only allows the MIDlet suite that
	 * created the RecordStore to access it. This case behaves identically to
	 * <code>openRecordStore(recordStoreName, createIfNecessary)</code>.</li>
	 * <li><code>AUTHMODE_ANY</code> - Allows any MIDlet to access the
	 * RecordStore. Note that this makes your recordStore accessible by any
	 * other MIDlet on the device. This could have privacy and security issues
	 * depending on the data being shared. Please use carefully.</li>
	 * </ul>
	 * <p>
	 * The owning MIDlet suite may always access the RecordStore and always has
	 * access to write and update the store. Only the owning MIDlet suite can
	 * change the mode of a RecordStore.
	 * </p>
	 * <P>
	 * @param authmode - the mode under which to check or create access. Must be
	 * one of AUTHMODE_PRIVATE or AUTHMODE_ANY.
	 * @param writable - true if the RecordStore is to be writable by other
	 * MIDlet suites that are granted access
	 * @throws RecordStoreException - if a record store-related exception
	 * occurred
	 * @throws SecurityException - if this MIDlet Suite is not allowed to change
	 * the mode of the RecordStore
	 * @throws IllegalArgumentException - if authmode is invalid
	 * @since MIDP 2.0
	 */
	public void setMode(int authmode, boolean writable) throws RecordStoreException {
		// TODO implement setMode
	}

	/**
	 * This method is called when the MIDlet requests to have the record store
	 * closed. Note that the record store will not actually be closed until
	 * closeRecordStore() is called as many times as openRecordStore() was
	 * called. In other words, the MIDlet needs to make a balanced number of
	 * close calls as open calls before the record store is closed.
	 * <p>
	 * When the record store is closed, all listeners are removed and all
	 * RecordEnumerations associated with it become invalid. If the MIDlet
	 * attempts to perform operations on the RecordStore object after it has
	 * been closed, the methods will throw a RecordStoreNotOpenException.
	 * <P>
	 * @throws RecordStoreNotOpenException - if the record store is not open
	 * @throws RecordStoreException - if a different record store-related
	 * exception occurred
	 */
	public void closeRecordStore() throws RecordStoreNotOpenException, RecordStoreException {
		if(isClosed()) {
			return;
		}
		boolean closed = closeChachedRecordStore();
		
		// When there are no more open instances of this record store around, inform the listeners.
		if(closed) {
			synchronized (this.listeners) {
				this.listeners.clear();
			}
		}
	}

	/**
	 * Returns an array of the names of record stores owned by the MIDlet suite.
	 * Note that if the MIDlet suite does not have any record stores, this
	 * function will return null. The order of RecordStore names returned is
	 * implementation dependent.
	 * <P>
	 * @return array of the names of record stores owned by the MIDlet suite.
	 * Note that if the MIDlet suite does not have any record stores, this
	 * function will return null.
	 */
	public static String[] listRecordStores() {
		init();
		String[] listRecordStores = sqlDao.listRecordStores();
		if (listRecordStores.length == 0) {
			return null;
		}
		return listRecordStores;
	}

	/**
	 * Returns the name of this RecordStore.
	 * <P>
	 * @return the name of this RecordStore
	 * @throws RecordStoreNotOpenException - if the record store is not open
	 */
	public String getName() throws RecordStoreNotOpenException {
		return this.name;
	}

	/**
	 * Each time a record store is modified (by <code>addRecord</code>, <code>
	 * setRecord</code>, or <code>deleteRecord</code> methods) its <em>version
	 * </em> is incremented. This can be used by MIDlets to quickly tell if
	 * anything has been modified. The initial version number is implementation
	 * dependent. The increment is a positive integer greater than 0. The
	 * version number increases only when the RecordStore is updated. The
	 * increment value need not be constant and may vary with each update.
	 * <P>
	 * @return the current record store version
	 * @throws RecordStoreNotOpenException - if the record store is not open
	 */
	public int getVersion() throws RecordStoreNotOpenException {
		return this.version;
	}

	/**
	 * Returns the number of records currently in the record store.
	 * <P>
	 * @return the number of records currently in the record store
	 * @throws RecordStoreNotOpenException - if the record store is not open
	 */
	public int getNumRecords() throws RecordStoreNotOpenException {
		return this.numRecords;
	}

	/**
	 * Returns the amount of space, in bytes, that the record store occupies.
	 * The size returned includes any overhead associated with the
	 * implementation, such as the data structures used to hold the state of the
	 * record store, etc.
	 * <P>
	 * @return the size of the record store in bytes
	 * @throws RecordStoreNotOpenException - if the record store is not open
	 */
	public int getSize() throws RecordStoreNotOpenException {
		return this.size;
	}

	/**
	 * Returns the amount of additional room (in bytes) available for this
	 * record store to grow. Note that this is not necessarily the amount of
	 * extra MIDlet-level data which can be stored, as implementations may store
	 * additional data structures with each record to support integration with
	 * native applications, synchronization, etc.
	 * <P>
	 * @return the amount of additional room (in bytes) available for this record
	 * store to grow
	 * @throws RecordStoreNotOpenException - if the record store is not open
	 */
	public int getSizeAvailable() throws RecordStoreNotOpenException {

		if (this.sizeAvailable > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		return (int) this.sizeAvailable;
	}

	/**
	 * Returns the last time the record store was modified, in the format used
	 * by System.currentTimeMillis().
	 * <P>
	 * @return the last time the record store was modified, in the format used by
	 * System.currentTimeMillis()
	 * @throws RecordStoreNotOpenException - if the record store is not open
	 */
	public long getLastModified() throws RecordStoreNotOpenException {
		return this.lastModified;
	}

	/**
	 * Adds the specified RecordListener. If the specified listener is already
	 * registered, it will not be added a second time. When a record store is
	 * closed, all listeners are removed.
	 * <P>
	 * @param listener - the RecordChangedListener
	 * @see #removeRecordListener(de.enough.polish.android.rms.RecordListener)
	 */
	public void addRecordListener(RecordListener listener) {
		synchronized (this.listeners) {
			if (!this.listeners.contains(listener)) {
				this.listeners.add(listener);
			}
		}
	}

	/**
	 * Removes the specified RecordListener. If the specified listener is not
	 * registered, this method does nothing.
	 * <P>
	 * @param listener - the RecordChangedListener
	 * @see #addRecordListener(de.enough.polish.android.rms.RecordListener)
	 */
	public void removeRecordListener(RecordListener listener) {
		synchronized (this.listeners) {
			if (!this.listeners.contains(listener)) {
				this.listeners.remove(listener);
			}
		}
	}

	/**
	 * Returns the recordId of the next record to be added to the record store.
	 * This can be useful for setting up pseudo-relational relationships. That
	 * is, if you have two or more record stores whose records need to refer to
	 * one another, you can predetermine the recordIds of the records that will
	 * be created in one record store, before populating the fields and
	 * allocating the record in another record store. Note that the recordId
	 * returned is only valid while the record store remains open and until a
	 * call to <code>addRecord()</code>.
	 * <P>
	 * @return the recordId of the next record to be added to the record store
	 * @throws RecordStoreNotOpenException - if the record store is not open
	 * @throws RecordStoreException - if a different record store-related
	 * exception occurred
	 */
	public int getNextRecordID() throws RecordStoreNotOpenException, RecordStoreException {
		if (isClosed()) {
			throw new RecordStoreNotOpenException("");
		}
		return this.nextRecordID;
	}

	/**
	 * Adds a new record to the record store. The recordId for this new record
	 * is returned. This is a blocking atomic operation. The record is written
	 * to persistent storage before the method returns.
	 * <P>
	 * @param data - the data to be stored in this record. If the record is to
	 * have zero-length data (no data), this parameter may be null.
	 * @param offset - the index into the data buffer of the first relevant byte
	 * for this record
	 * @param numBytes - the number of bytes of the data buffer to use for this
	 * record (may be zero)
	 * @return the recordId for the new record
	 * @throws RecordStoreNotOpenException - if the record store is not open
	 * @throws RecordStoreException - if a different record store-related
	 * exception occurred
	 * @throws RecordStoreFullException - if the operation cannot be completed
	 * because the record store has no more room
	 * @throws SecurityException - if the MIDlet has read-only access to the
	 * RecordStore
	 */
	public int addRecord(byte[] data, int offset, int numBytes) throws RecordStoreNotOpenException, RecordStoreException, RecordStoreFullException {

		if (isClosed()) {
			throw new RecordStoreNotOpenException("The record store is not open because it was closed. This RecordStore object is invalid and will stay so.");
		}
		if (data == null) {
			data = new byte[0];
		}
		if (data.length != 0 && offset >= data.length) {
			throw new RecordStoreException("The offset '" + offset + "' is beyond the size of the data array of '" + data.length + "'");
		}
		if (numBytes < 0) {
			throw new RecordStoreException("The number of bytes '" + numBytes + "' must not be negative.");
		}
		if (offset < 0) {
			throw new RecordStoreException("The offset '" + offset + "' must not be negative.");
		}
		if (offset + numBytes > data.length) {
			throw new RecordStoreException("The Parameter numBytes with value '" + numBytes + "' exceeds the number of available bytes if counted from offset '" + offset + "'");
		}
		byte[] actualData = new byte[numBytes];
		System.arraycopy(data, offset, actualData, 0, numBytes);
		int recordId = sqlDao.addRecord(getPk(), actualData);
		// The addRecord method will increment the nextRecordId in the database.
		// So we update the cache accordingly.
		RecordStore recordStore = sqlDao.getRecordStore(getPk());
		updateRecordStoreInstance(recordStore);
		fireRecordAddedEvent(recordId);
		return recordId;
	}

	// TODO: What about concurrent updates?
	private void updateRecordStoreInstance(RecordStore recordStore) throws RecordStoreException {
		this.name = recordStore.name;
		this.nextRecordID = recordStore.nextRecordID;
		this.numRecords = recordStore.numRecords;
		this.size = recordStore.size;
		this.version = recordStore.version;
		this.recordStorePk = recordStore.recordStorePk;
		this.authMode = recordStore.authMode;
	}

	protected long getPk() {
		return this.recordStorePk;
	}

	/**
	 * The record is deleted from the record store. The recordId for this record
	 * is NOT reused.
	 * <P>
	 * @param recordId - the ID of the record to delete
	 * @throws RecordStoreNotOpenException - if the record store is not open
	 * @throws InvalidRecordIDException - if the recordId is invalid
	 * @throws RecordStoreException - if a general record store exception occurs
	 * @throws SecurityException - if the MIDlet has read-only access to the
	 * RecordStore
	 */
	public void deleteRecord(int recordId) throws RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreException {
		if (isClosed()) {
			throw new RecordStoreNotOpenException();
		}
		if (recordId < 0) {
			throw new InvalidRecordIDException();
		}
		sqlDao.removeRecord(getPk(), recordId);
		RecordStore recordStore = sqlDao.getRecordStore(getPk());
		updateRecordStoreInstance(recordStore);
		fireRecordDeletedEvent(recordId);
	}

	/**
	 * Returns the size (in bytes) of the MIDlet data available in the given
	 * record.
	 * <P>
	 * @param recordId - the ID of the record to use in this operation
	 * @return the size (in bytes) of the MIDlet data available in the given
	 * record
	 * @throws RecordStoreNotOpenException - if the record store is not open
	 * @throws InvalidRecordIDException - if the recordId is invalid
	 * @throws RecordStoreException - if a general record store exception occurs
	 */
	public int getRecordSize(int recordId) throws RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreException {
		return 0;
	}

	/**
	 * Returns the data stored in the given record.
	 * <P>
	 * @param recordId - the ID of the record to use in this operation
	 * @param buffer - the byte array in which to copy the data
	 * @param offset - the index into the buffer in which to start copying
	 * @return the number of bytes copied into the buffer, starting at index
	 * offset
	 * @throws RecordStoreNotOpenException - if the record store is not open
	 * @throws InvalidRecordIDException - if the recordId is invalid
	 * @throws RecordStoreException - if a general record store exception occurs
	 * @throws ArrayIndexOutOfBoundsException - if the record is larger than the
	 * buffer supplied
	 * @see #setRecord(int, byte[], int, int)
	 */
	public int getRecord(int recordId, byte[] buffer, int offset) throws RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreException {
		byte[] data = getRecord(recordId);

		System.arraycopy(data, 0, buffer, offset, data.length);

		return data.length - offset;
	}

	/**
	 * Returns a copy of the data stored in the given record.
	 * <P>
	 * @param recordId - the ID of the record to use in this operation
	 * @return the data stored in the given record. Note that if the record has
	 * no data, this method will return null.
	 * @throws RecordStoreNotOpenException - if the record store is not open
	 * @throws InvalidRecordIDException - if the recordId is invalid
	 * @throws RecordStoreException - if a general record store exception occurs
	 * @see #setRecord(int, byte[], int, int)
	 */
	public byte[] getRecord(int recordId) throws RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreException {
		if (isClosed()) {
			throw new RecordStoreNotOpenException();
		}
		if (recordId < 0) {
			throw new InvalidRecordIDException();
		}
		byte[] record = sqlDao.getRecord(getPk(), recordId);
		return record;
	}

	/**
	 * Sets the data in the given record to that passed in. After this method
	 * returns, a call to <code>getRecord(int recordId)</code> will return an
	 * array of numBytes size containing the data supplied here.
	 * <P>
	 * @param recordId - the ID of the record to use in this operation
	 * @param newData - the new data to store in the record
	 * @param offset - the index into the data buffer of the first relevant byte
	 * for this record
	 * @param numBytes - the number of bytes of the data buffer to use for this
	 * record
	 * @throws RecordStoreNotOpenException - if the record store is not open
	 * @throws InvalidRecordIDException - if the recordId is invalid
	 * @throws RecordStoreException - if a general record store exception occurs
	 * @throws RecordStoreFullException - if the operation cannot be completed
	 * because the record store has no more room
	 * @throws SecurityException - if the MIDlet has read-only access to the
	 * RecordStore
	 * @see #getRecord(int, byte[], int)
	 */
	public void setRecord(int recordId, byte[] newData, int offset, int numBytes) throws RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreException, RecordStoreFullException {
		if (isClosed()) {
			throw new RecordStoreNotOpenException();
		}
		if (recordId < 0) {
			throw new InvalidRecordIDException("The parameter 'recordId' must not be negative.");
		}
		if (newData == null) {
			newData = new byte[0];
		}

		byte[] data = new byte[numBytes];
		System.arraycopy(newData, offset, data, 0, numBytes);

		sqlDao.setRecord(getPk(), recordId, data);
		RecordStore recordStore = sqlDao.getRecordStore(getPk());
		updateRecordStoreInstance(recordStore);
		fireRecordChangedEvent(recordId);
	}

	/**
	 * Returns an enumeration for traversing a set of records in the record
	 * store in an optionally specified order.
	 * <p>
	 * The filter, if non-null, will be used to determine what subset of the
	 * record store records will be used.
	 * <p>
	 * The comparator, if non-null, will be used to determine the order in which
	 * the records are returned.
	 * <p>
	 * If both the filter and comparator is null, the enumeration will traverse
	 * all records in the record store in an undefined order. This is the most
	 * efficient way to traverse all of the records in a record store. If a
	 * filter is used with a null comparator, the enumeration will traverse the
	 * filtered records in an undefined order. The first call to <code>
	 * RecordEnumeration.nextRecord()</code> returns the record data from the
	 * first record in the sequence. Subsequent calls to <code>
	 * RecordEnumeration.nextRecord()</code> return the next consecutive 
	 * record's data. To return the record data from the previous consecutive
	 * from any given point in the enumeration, call <code>previousRecord()
	 * </code>. On the other hand, if after creation the first call is to <code>
	 * previousRecord()</code>, the record data of the last element of the
	 * enumeration will be returned. Each subsequent call to <code>
	 * previousRecord()</code> will step backwards through the sequence.
	 * <P>
	 * @param filter - if non-null, will be used to determine what subset of the
	 * record store records will be used
	 * @param comparator - if non-null, will be used to determine the order in
	 * which the records are returned
	 * @param keepUpdated - if true, the enumerator will keep its enumeration
	 * current with any changes in the records of the record store. Use with
	 * caution as there are possible performance consequences. If false the
	 * enumeration will not be kept current and may return recordIds for records
	 * that have been deleted or miss records that are added later. It may also
	 * return records out of order that have been modified after the enumeration
	 * was built. Note that any changes to records in the record store are
	 * accurately reflected when the record is later retrieved, either directly
	 * or through the enumeration. The thing that is risked by setting this
	 * parameter false is the filtering and sorting order of the enumeration
	 * when records are modified, added, or deleted.
	 * @return an enumeration for traversing a set of records in the record
	 * store in an optionally specified order
	 * @throws RecordStoreNotOpenException - if the record store is not open
	 * @see RecordEnumeration#rebuild()
	 */
	public RecordEnumeration enumerateRecords(RecordFilter filter, RecordComparator comparator, boolean keepUpdated) throws RecordStoreNotOpenException {
		SqlRecordEnumeration sqlRecordEnumeration = new SqlRecordEnumeration(this, filter, comparator, keepUpdated);
		return sqlRecordEnumeration;
	}

	private static void init() {
		if (sqlDao == null) {
			sqlDao = SqlDao.getInstance();
		}
	}

	protected synchronized boolean isClosed() {
		return this.openCount <= 0;
	}

	private void fireRecordAddedEvent(int recordId) {
		synchronized (this.listeners) {
			for (Iterator<RecordListener> iterator = this.listeners.iterator(); iterator.hasNext();) {
				RecordListener recordListener = iterator.next();
				recordListener.recordAdded(this, recordId);
			}
		}
	}
	
	private void fireRecordChangedEvent(int recordId) {
		synchronized (this.listeners) {
			for (Iterator<RecordListener> iterator = this.listeners.iterator(); iterator.hasNext();) {
				RecordListener recordListener = iterator.next();
				recordListener.recordChanged(this, recordId);
			}
		}
	}
	private void fireRecordDeletedEvent(int recordId) {
		synchronized (this.listeners) {
			for (Iterator<RecordListener> iterator = this.listeners.iterator(); iterator.hasNext();) {
				RecordListener recordListener = iterator.next();
				recordListener.recordDeleted(this, recordId);
			}
		}
	}

	
	void setVersion(int version) {
		this.version = version;
	}

	void setNextId(int nextRecordId) {
		this.nextRecordID = nextRecordId;
	}

	void setNumberOfRecords(int numberOfRecords) {
		this.numRecords = numberOfRecords;
	}

	void setSize(int size) {
		this.size = size;
	}
	
	private static RecordStore getOpenedRecordStoreFromCache(String recordStoreName) {
		return openedRecordStores.get(recordStoreName);
	}
	
//	private void removeCachedRecordStore(String recordStoreName) throws RecordStoreException {
//		RecordStore recordStore = openedRecordStores.get(recordStoreName);
//		if (recordStore != null) {
//			throw new RecordStoreException("The record store '" + recordStoreName + "' is not closed.");
//		}
//	}
	
	private static void cacheRecordStore(String recordStoreName,RecordStore recordStore) {
		openedRecordStores.put(recordStoreName, recordStore);
		recordStore.openCount++;
	}
	
	/**
	 * Returns a cached record store and increases the open count.
	 * @param recordStoreName
	 * @return
	 */
	private static RecordStore openRecordStoreFromCache(String recordStoreName) {
		RecordStore recordStore = openedRecordStores.get(recordStoreName);
		if(recordStore != null) {
			recordStore.openCount++;
		}
		return recordStore;
	}
	
	private boolean closeChachedRecordStore() {
		this.openCount--;
		if (this.openCount > 0) {
			return false;
		}
		openedRecordStores.remove(this.name);
		return true;
	}

}
