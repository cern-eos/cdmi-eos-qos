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

package org.cern.eos.cdmi.util;

import org.cern.eos.cdmi.EosStorageBackend;
import org.indigo.cdmi.BackEndException;
import org.indigo.cdmi.BackendCapability;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;

import static org.indigo.cdmi.BackendCapability.CapabilityType.CONTAINER;
import static org.indigo.cdmi.BackendCapability.CapabilityType.DATAOBJECT;

/**
 * Utility class for EOS or CDMI specific parsing tasks.
 */
public class EOSParseUtils {

  private static final Logger LOG = LoggerFactory.getLogger(EOSParseUtils.class);

  /**
   * Given the full response of an EOS command, attempt to extract the output.
   * If an error message is encountered, execution is aborted.
   *
   * An EOS command response follows the following format:
   *   mgm.proc.stdout=#output#
   *   &mgm.proc.stderr=#errors#
   *   &mgm.proc.retc=#retc#
   */
  public static String extractCmdOutput(String cmdResponse) throws BackEndException {
    LOG.debug("Extracting output from command response: {}", cmdResponse);
    int pos;

    // Search if there are error messages
    if ((pos = cmdResponse.indexOf("mgm.proc.stderr=")) != -1) {
      int startPos = pos + 16;

      // Find the end of the error substring
      if ((pos = cmdResponse.indexOf("&", startPos)) == -1) {
        pos = cmdResponse.length();
      }

      String errorMessage = cmdResponse.substring(startPos, pos);

      if (!errorMessage.isEmpty()) {
        throw new BackEndException(
            String.format("Server responded with error message -- %s", errorMessage));
      }
    }

    // Find the beginning of the output substring
    if ((pos = cmdResponse.indexOf("mgm.proc.stdout=")) != -1) {
      int startPos = pos + 16;

      // Find the end of the output substring
      if ((pos = cmdResponse.indexOf("&", startPos)) == -1) {
        pos = cmdResponse.length();
      }

      return cmdResponse.substring(startPos, pos);
    }

    return cmdResponse;
  }

  /**
   * Extract information from EOS QoS class description
   * and create a CDMI BackendCapability object.
   */
  public static BackendCapability backendCapabilityFromJson(JSONObject response,
                                                            BackendCapability.CapabilityType type) {
    Map<String, Object> metadata = metadataFromQoSJson(response, "");

    try {
      List<String> transition = JsonUtils.jsonArrayToStringList(response.getJSONArray("transition"));
      metadata.put("cdmi_capabilities_allowed", capabilitiesAllowed(transition, type));
    } catch (JSONException ignore) {
      // Ignore exception
    }

    BackendCapability backendCapability = new BackendCapability(response.getString("name"), type);
    backendCapability.setMetadata(metadata);
    backendCapability.setCapabilities(EosStorageBackend.capabilities);

    return backendCapability;
  }

  /**
   * Extract metadata information from a QoS description.
   * The QoS description may be either for a class or for an entry.
   *
   * @param response the JSON response representing a QoS description
   * @param suffix the suffix to append to the metadata key
   * @return metadata the metadata JSON object
   */
  public static Map<String, Object> metadataFromQoSJson(JSONObject response,
                                                        String suffix) {
    Map<String, Object> metadata = new HashMap<>();

    try {
      JSONObject jsonMetadata = response.getJSONObject("metadata");

      Integer cdmiRedundancy = jsonMetadata.getInt("cdmi_data_redundancy_provided");
      Integer cdmiLatency = jsonMetadata.getInt("cdmi_latency_provided");
      JSONArray cdmiGeoPlacement = jsonMetadata.getJSONArray("cdmi_geographic_placement_provided");

      metadata.put("cdmi_data_redundancy" + suffix, cdmiRedundancy);
      metadata.put("cdmi_latency" + suffix, cdmiLatency);
      metadata.put("cdmi_geographic_placement" + suffix, cdmiGeoPlacement);
    } catch (JSONException ignore) {
      LOG.debug("Failed to retrieve metadata. Returning empty map.");
      metadata = Collections.emptyMap();
    }

    return metadata;
  }

  /**
   * Extract the list of children from a JSON Fileinfo response.
   *
   * @param fileinfo the JSON fileinfo response
   * @return list of children
   */
  public static List<String> childrenFromFileinfoJSON(JSONObject fileinfo) {
    List<String> children = new LinkedList<>();

    if (!fileinfoIsDirectory(fileinfo)) {
      return children;
    }

    try {
       JSONArray childrenJSON = fileinfo.getJSONArray("children");

       for (int i = 0; i < childrenJSON.length(); i++) {
         children.add(childrenJSON.getJSONObject(i).getString("name"));
       }
    } catch (JSONException ignore) {
      LOG.debug("Failed to retrieve children information. Return empty list.");
      children = Collections.emptyList();
    }

    return children;
  }

  /**
   * Returns true if the given fileinfo JSON object describes a directory,
   * false otherwise.
   */
  public static boolean fileinfoIsDirectory(JSONObject fileinfo) {
    return fileinfo.has("treesize");
  }

  /**
   * Returns the QoS class extracted from the given capability URI.
   */
  public static String qosClassFromCapUri(String capUri) {
    String[] parts = capUri.trim().split("/");
    return parts[parts.length - 1];
  }

  /**
   * Returns a string representation of a capability type.
   */
  public static String capabilityTypeToString(BackendCapability.CapabilityType type) {
    if (type == CONTAINER) {
      return "container";
    } else if (type == DATAOBJECT) {
      return "dataobject";
    }

    return null;
  }

  /**
   * Transforms a list of allowed transitions into a JSON array of CDMI transition URIs.
   */
  private static JSONArray capabilitiesAllowed(List<String> allowedList,
                                               BackendCapability.CapabilityType type) {
    JSONArray capAllowed = new JSONArray();

    for (String allowed : allowedList) {
      capAllowed.put("/cdmi_capabilities/" + capabilityTypeToString(type) + "/" + allowed + "/");
    }

    return capAllowed;
  }
}
