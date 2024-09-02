/**
* Web worker: an object of this class executes in its own new thread
* to receive and respond to a single HTTP request. After the constructor
* the object executes on its "run" method, and leaves when it is done.
*
* One WebWorker object is only responsible for one client connection. 
* This code uses Java threads to parallelize the handling of clients:
* each WebWorker runs in its own thread. This means that you can essentially
* just think about what is happening on one client at a time, ignoring 
* the fact that the entirety of the webserver execution might be handling
* other clients, too. 
*
* This WebWorker class (i.e., an object of this class) is where all the
* client interaction is done. The "run()" method is the beginning -- think
* of it as the "main()" for a client interaction. It does three things in
* a row, invoking three methods in this class: it reads the incoming HTTP
* request; it writes out an HTTP header to begin its response, and then it
* writes out some HTML content for the response content. HTTP requests and
* responses are just lines of text (in a very particular format). 
*
**/

import java.net.Socket;
import java.lang.Runnable;
import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.util.TimeZone;

public class WebWorker implements Runnable
{

private Socket socket;

private String firstLine = null;
private String filePath = null;
private boolean errorReadingFile = false;


/**
* Constructor: must have a valid open socket
**/
public WebWorker(Socket s)
{
   socket = s;
}

/**
* Worker thread starting point. Each worker handles just one HTTP 
* request and then returns, which destroys the thread. This method
* assumes that whoever created the worker created it with a valid
* open socket object.
**/
public void run()
{
   System.err.println("Handling connection...");
   try {
      InputStream  is = socket.getInputStream();
      OutputStream os = socket.getOutputStream();
      readHTTPRequest(is);
      
      /*
       * Changees: Added checkFile function that will return true
       * if there is an error reading the file
       */
      checkFile();
      writeHTTPHeader(os,"text/html");
      writeContent(os);
      os.flush();
      socket.close();
   } catch (Exception e) {
      System.err.println("Output error: "+e);
   }
   System.err.println("Done handling connection.");
   return;
}

/*
 * Changes: This function will try to read the file
 * frm the file path and return true if there is an error
 */
private void checkFile()
{
   File file = new File("." + filePath);

   try {
      BufferedReader br = new BufferedReader(new FileReader(file));
      br.close(); 
   } catch (IOException e) {
      errorReadingFile = true;
      System.err.println("Error opening file.");
   }

}


/**
* Read the HTTP request header.
**/
private void readHTTPRequest(InputStream is)
{
   String line;
   BufferedReader r = new BufferedReader(new InputStreamReader(is));
   while (true) 
   {
      try {
         while (!r.ready()) Thread.sleep(1);
         line = r.readLine();

         /*
          * Changes: Store the first request line in firstLine
          * file path will be here
          */
         if (firstLine == null)
         {
            firstLine = line;
         }

         System.err.println("Request line: ("+line+")");
         if (line.length()==0) break;
      } catch (Exception e) {
         System.err.println("Request error: "+e);
         break;
      }
   }

   /*
    * Changes: Extract the file path from the GET request
    */
   if (firstLine != null) {
      String[] parts = firstLine.split(" ");
      if (parts.length >= 2) {
         filePath = parts[1]; // Extract the path part of the request
      }
   }

   System.err.println("Requested file path: " + filePath);

   return;
}

/**
* Write the HTTP header lines to the client network connection.
* @param os is the OutputStream object to write to
* @param contentType is the string MIME content type (e.g. "text/html")
**/
private void writeHTTPHeader(OutputStream os, String contentType) throws Exception
{
   Date d = new Date();
   DateFormat df = DateFormat.getDateTimeInstance();
   df.setTimeZone(TimeZone.getTimeZone("GMT"));

   /*
    * Changes: Check if there was an error reading the file
    * Serve a 404 NOT FOUND error if so
    */
    if (errorReadingFile == false)
    {
      os.write("HTTP/1.1 200 OK\n".getBytes());
    }
    else
    {
      os.write("HTTP/1.1 404 NOT FOUND\n".getBytes());
    }
   os.write("Date: ".getBytes());
   os.write((df.format(d)).getBytes());
   os.write("\n".getBytes());
   os.write("Server: Jon's very own server\n".getBytes());
   //os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
   //os.write("Content-Length: 438\n".getBytes()); 
   os.write("Connection: close\n".getBytes());
   os.write("Content-Type: ".getBytes());
   os.write(contentType.getBytes());
   os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
   return;
}

/**
* Write the data content to the client network connection. This MUST
* be done after the HTTP header has been written out.
* @param os is the OutputStream object to write to
**/
private void writeContent(OutputStream os) throws Exception
{
    /*
     * Changelog: Serve HTML file to localhost
     */
    File file = new File("." + filePath);
    
    System.err.println("Checking file at: " + file.getAbsolutePath());
    
    if (errorReadingFile == false)
    {
      BufferedReader br = new BufferedReader(new FileReader(file));
      String currLine;

      while ((currLine = br.readLine()) != null)
      {
         if (currLine.contains("{{cs371date}}"))
         {
            Date d = new Date();
            DateFormat df = DateFormat.getDateTimeInstance();
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            currLine = currLine.replace("{{cs371date}}", "Date: " + df.format(d));
         }

         if (currLine.contains("{{cs371server}}"))
         {
            currLine = currLine.replace("{{cs371server}}", "CS371 SERVER");
         }

         os.write(currLine.getBytes());
         os.write("\n".getBytes());

      }

      br.close();
    }

    else
    {
      /*
       * error stuff here
       */
      os.write("<html><head></head><body>\n".getBytes());
      os.write("<h3>404 NOT FOUND</h3>\n".getBytes());
      os.write("</body></html>\n".getBytes());
    }

}


} // end class