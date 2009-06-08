//#condition polish.android
// generated by de.enough.doc2java.Doc2Java (www.enough.de) on Wed Jan 21 22:12:19 CET 2009

package de.enough.polish.android.io.file;

import java.io.File;
import java.util.Vector;

/**
 * The FileSystemRegistry is a central registry for file system listeners
 * interested in the adding and removing (or mounting and unmounting) of file
 * systems on a device.
 * <DD>
 * FileConnection 1.0 <DT><B>See Also:</B><DD><A HREF="../../../../de/enough/polish/android/io/file/FileConnection.html"><CODE>FileConnection</CODE></A>,
 * <A HREF="../../../../de/enough/polish/android/io/file/FileSystemListener.html"><CODE>FileSystemListener</CODE></A></DD></DL>
 */
public class FileSystemRegistry extends java.lang.Object
{
	/**
	 * 
	 * This method is used to register a FileSystemListener that is notified in case
	 * of adding and removing a new file system root. Multiple file system listeners
	 * can be added. If file systems are not supported on a device, false is returned
	 * from the method (this check is performed prior to security checks).
	 * <P></P>
	 * 
	 * 
	 * @param listener - The new FileSystemListener to be registered in order to handle adding/removing file system roots.
	 * @return boolean indicating if file system listener was successfully added or not
	 * @throws java.lang.SecurityException - if application is not given permission to read files.
	 * @throws java.lang.NullPointerException - if listener is null.
	 * @see FileSystemListener
	 * @see FileConnection
	 */
	public static boolean addFileSystemListener( FileSystemListener listener)
	{
		//TODO implement addFileSystemListener
		return false;
	}

	/**
	 * 
	 * This method is used to remove a registered FileSystemListener. If file systems
	 * are not supported on a device, false is returned from the method.
	 * <P></P>
	 * 
	 * 
	 * @param listener - The FileSystemListener to be removed.
	 * @return boolean indicating if file system listener was successfully removed or not
	 * @throws java.lang.NullPointerException - if listener is null.
	 * @see FileSystemListener
	 * @see FileConnection
	 */
	public static boolean removeFileSystemListener( FileSystemListener listener)
	{
		//TODO implement removeFileSystemListener
		return false;
	}

	/**
	 * 
	 * This method returns the currently mounted root file systems on a device as
	 * String objects in an Enumeration. If there are no roots available on the
	 * device, a zero length Enumeration is returned. If file systems are not
	 * supported on a device, a zero length Enumeration is also returned (this check
	 * is performed prior to security checks).
	 * <P>
	 * The first directory in the file URI is referred to as the <I>root</I>, which
	 * corresponds to a logical mount point for a particular storage unit or memory.
	 * Root strings are defined by the platform or implementation and can be a string
	 * of zero or more characters ("" can be a valid root string on some systems)
	 * followed by a trailing "/" to denote that the root is a directory. Each root
	 * string is guaranteed to uniquely refer to a root. Root names are device
	 * specific and are not required to adhere to any standard. Examples of possible
	 * root strings and how to open them include:
	 * <table border="1">
	 * <tr>
	 * <th>
	 * Possible Root Value</th><th>Opening the Root</th></tr>
	 * <tr>
	 * <td>CFCard/</td>
	 * <td>Connector.open("file:///CFCard/");</td>
	 * </tr>
	 * <tr>
	 * <td>SDCard/</td>
	 * <td>Connector.open("file:///SDCard/");</td>
	 * </tr>
	 * <tr>
	 * <td>MemoryStick/</td>
	 * <td>Connector.open("file:///MemoryStick/");</td>
	 * </tr>
	 * <tr>
	 * <td>C:/</td>
	 * <td>Connector.open("file:///C:/");</td>
	 * </tr>
	 * <tr>
	 * <td>/</td>
	 * <td>Connector.open("file:////");</td>
	 * </tr>
	 * </table>
	 * <P>
	 * The following is a sample showing the use of listRoots to retrieve the
	 * available size of all roots on a device:
	 * <pre>   Enumeration rootEnum = FileSystemRegistry.listRoots();
	 * while (e.hasMoreElements()) {
	 * String root = (String) e.nextElement();
	 * FileConnection fc = Connector.open("file:///" + root);
	 * System.out.println(fc.availableSize());
	 * }
	 * </pre>
	 * <P></P>
	 * 
	 * 
	 * @return an Eumeration of mounted file systems as String objects.
	 * @throws java.lang.SecurityException - if application is not given permission to read files.
	 * @see FileConnection
	 */
    public static java.util.Enumeration listRoots()
    {
        File files[];
        try
        {
            files=File.listRoots();
        }
        catch(SecurityException e)
        {
            throw e;                
        }
        if(files==null)
            return null;
        
        Vector v=new Vector();
        for(int i=0;i<files.length;i++)
        {
            String name=files[i].getPath();
            if(name!=null){                
                v.addElement(name.trim());
            }   
        }                 
        return v.elements();
    }


}
