/*
 * The MIT License
 * Copyright (c) 2019 CERN/Switzerland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.cern.eos.cdmi.protobuf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to handle Protobuf command building and serialization.
 */
public class ProtobufUtils {

  private static final Logger LOG = LoggerFactory.getLogger(ProtobufUtils.class);

  /**
   * Returns the base64 encoded string of the Protobuf "qos ls" command.
   */
  public static String QoSList() {
    return "CAGiAQIKAPgBAQ==";
  }

  public static String QoSListClass(String qosClassName) {
    if (qosClassName.equals("disk_plain")) {
      return "CAGiAQ4KDAoKZGlza19wbGFpbg==";
    } else if (qosClassName.equals("disk_replica")) {
      return "CAGiARAKDgoMZGlza19yZXBsaWNh";
    }

    return null;
  }
}