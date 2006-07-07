/*
 * Copyright 2005-2006 Noelios Consulting.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.example;

import org.restlet.component.RestletContainer;
import org.restlet.data.Protocols;

import com.noelios.restlet.DirectoryHandler;
import com.noelios.restlet.HostRouter;
import com.noelios.restlet.LogFilter;

/**
 * Logging calls.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Tutorial07
{
   public static void main(String[] args)
   {
      try
      {
         // Create a new Restlet container
      	RestletContainer myContainer = new RestletContainer();

         // Add an HTTP server connector to the Restlet container. 
         // Note that the container is the call restlet.
         myContainer.addServer("HTTP Server", Protocols.HTTP, 8182);

         // Attach a log Filter to the container
         LogFilter log = new LogFilter(myContainer, "com.noelios.restlet.example");
         myContainer.setRoot(log);

         // Create a host router matching calls to the server
         HostRouter host = new HostRouter(myContainer, 8182);
         log.setTarget(host);

         // Create a directory Restlet able to return a deep hierarchy of Web files
         DirectoryHandler directory = new DirectoryHandler(myContainer, "file:///D:/Restlet/www/docs/api/", true, "index");

         // Then attach the Restlet to the log Filter.
         host.addOption("/", directory);

         // Now, let's start the container!
         myContainer.start();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

}
