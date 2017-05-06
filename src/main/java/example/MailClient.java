/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.ipc.specific.SpecificResponder;
import org.apache.avro.util.Utf8;

import example.proto.Mail;
import example.proto.Message;

/**
 * Start a server, attach a client, and send a message.
 */
public class MailClient {

  public static void main(String[] args) throws IOException {

    if (args.length != 5) {
      System.out.println("Usage: <server_ip> <count> <to> <from> <body>");
      System.exit(1);
    }

    String sever = args[0];
    Integer count = Integer.parseInt(args[1]);

    NettyTransceiver client = new NettyTransceiver(
        new InetSocketAddress(65111));

    // client code - attach to the server and send a message
    Mail proxy = (Mail) SpecificRequestor.getClient(Mail.class, client);

    System.out.println("Client built, got proxy");

    // fill in the Message record and send it
    Message message = new Message();
    message.setTo(new Utf8(args[2]));
    message.setFrom(new Utf8(args[3]));
    message.setBody(new Utf8(args[4]));

    long mark = System.nanoTime();
    for (int i = 0; i < count; i++) {
      proxy.send(message);
    }
    mark -= System.nanoTime();
    System.out.println("Elapsed time = " + (mark / 1000000.0d) + "ms" + ",avg="
        + (mark / count) + "ns");

    // cleanup
    client.close();
  }
}
