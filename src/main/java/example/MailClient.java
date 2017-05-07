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

import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.util.Utf8;

import example.proto.Mail;
import example.proto.Message;
import example.utils.CommonUtils;
import example.utils.StatUtils;

/**
 * Mail client.
 */
public class MailClient {

  public static void main(String[] args) throws Exception {

    if (args.length != 5) {
      System.out
          .println("Usage: <server_ip> <count> <to> <from> <size_of_body>");
      System.exit(1);
    }

    String sever = args[0];
    Integer count = Integer.parseInt(args[1]);

    NettyTransceiver client = new NettyTransceiver(
        new InetSocketAddress(sever, 65111));

    // client code - attach to the server and send a message
    Mail proxy = (Mail) SpecificRequestor.getClient(Mail.class, client);

    System.out.println("Client built, got proxy");

    // fill in the Message record and send it
    Message message = new Message();
    message.setTo(new Utf8(args[2]));
    message.setFrom(new Utf8(args[3]));

    int sizeOfBody = Integer.parseInt(args[4]);
    message.setBody(new Utf8(new byte[sizeOfBody]));

    long elapsed = System.nanoTime();

    long entryElapsed[] = new long[count];
    for (int i = 0; i < count; i++) {
      long timeUsed = System.nanoTime();
      proxy.send(message);
      timeUsed = System.nanoTime() - timeUsed;

      entryElapsed[i] = timeUsed;
    }
    elapsed = System.nanoTime() - elapsed;

    report(elapsed, entryElapsed, sizeOfBody);

    // cleanup
    client.close();
  }

  private static void report(long elapsed, long[] entryElapsed,
      long sizeOfBody) {

    double elapsedMs = CommonUtils.nsToMs(elapsed);
    long count = entryElapsed.length;
    double throughput = Math.round(1000d * (count / elapsedMs) * 1000d) / 1000d;
    double average = elapsedMs / count;

    System.out.println("Total " + count + " times, elapsed time = " + elapsedMs
        + "ms" + ",throughput=" + throughput + " entries per second");

    double min = StatUtils.min(entryElapsed);
    double max = StatUtils.max(entryElapsed);
    double stddev = StatUtils.stddev(entryElapsed);
    double[] percentiles = StatUtils.percentile(entryElapsed, 6);

    System.out.println("Single avg=" + CommonUtils.nsToMs(average) + "ms, min="
        + CommonUtils.nsToMs(min) + "ms,max=" + CommonUtils.nsToMs(max) + "ms");

    System.out.println("Stddev=" + CommonUtils.nsToMs(stddev) + "ms");

    for (int i = 0; i < percentiles.length; i++) {
      System.out.println((i + 1) + " stddev - " + percentiles[i] + "%");
    }

    long totalBytes = count * sizeOfBody;
    System.out.println("Single entry content size=" + (sizeOfBody / 1024d)
        + "KB, total bandwidth=" + Math.round(totalBytes / 1024d) / 1024d + "MB"
        + ", bandwidth/second="
        + Math.round(totalBytes / elapsedMs * 1000d / 1024d) / 1024d + "MB");
  }

}
