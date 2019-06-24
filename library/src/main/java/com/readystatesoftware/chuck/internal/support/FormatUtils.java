/*
 * Copyright (C) 2017 Jeff Gilfelt.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.readystatesoftware.chuck.internal.support;

import android.content.Context;
import android.text.TextUtils;

import com.readystatesoftware.chuck.R;
import com.readystatesoftware.chuck.internal.data.HttpHeader;
import com.readystatesoftware.chuck.internal.data.HttpTransaction;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Locale;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;

public class FormatUtils {

    public static String formatHeaders(List<HttpHeader> httpHeaders, boolean withMarkup) {
        String out = "";
        if (httpHeaders != null) {
            for (HttpHeader header : httpHeaders) {
                out += ((withMarkup) ? "<b>" : "") + header.getName() + ": " + ((withMarkup) ? "</b>" : "") +
                        header.getValue() + ((withMarkup) ? "<br />" : "\n");
            }
        }
        return out;
    }

    public static String formatByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format(Locale.US, "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String formatJson(String json) {
        try {
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(json);
            return JsonConvertor.getInstance().toJson(je);
        } catch (Exception e) {
            return json;
        }
    }

    public static String formatXml(String xml) {
        try {
            SAXTransformerFactory transformerFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            transformerFactory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalDTD", "");
            transformerFactory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalStylesheet", "");
            Transformer serializer = transformerFactory.newTransformer();
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            Source xmlSource = new SAXSource(new InputSource(new ByteArrayInputStream(xml.getBytes())));
            StreamResult res = new StreamResult(new ByteArrayOutputStream());
            serializer.transform(xmlSource, res);
            return new String(((ByteArrayOutputStream) res.getOutputStream()).toByteArray());
        } catch (Exception e) {
            return xml;
        }
    }

    public static String getShareText(Context context, HttpTransaction transaction) {
        String text = "";
        text += context.getString(R.string.chuck_url) + ": " + v(transaction.getUrl()) + "\n";
        text += context.getString(R.string.chuck_method) + ": " + v(transaction.getMethod()) + "\n";
        text += context.getString(R.string.chuck_protocol) + ": " + v(transaction.getProtocol()) + "\n";
        text += context.getString(R.string.chuck_status) + ": " + v(transaction.getStatus().toString()) + "\n";
        text += context.getString(R.string.chuck_response) + ": " + v(transaction.getResponseSummaryText()) + "\n";
        text += context.getString(R.string.chuck_ssl) + ": " + v(context.getString(transaction.isSsl() ? R.string.chuck_yes : R.string.chuck_no)) + "\n";
        text += "\n";
        text += context.getString(R.string.chuck_request_time) + ": " + v(transaction.getRequestDateString()) + "\n";
        text += context.getString(R.string.chuck_response_time) + ": " + v(transaction.getResponseDateString()) + "\n";
        text += context.getString(R.string.chuck_duration) + ": " + v(transaction.getDurationString()) + "\n";
        text += "\n";
        text += context.getString(R.string.chuck_request_size) + ": " + v(transaction.getRequestSizeString()) + "\n";
        text += context.getString(R.string.chuck_response_size) + ": " + v(transaction.getResponseSizeString()) + "\n";
        text += context.getString(R.string.chuck_total_size) + ": " + v(transaction.getTotalSizeString()) + "\n";
        text += "\n";
        text += "---------- " + context.getString(R.string.chuck_request) + " ----------\n\n";
        String headers = formatHeaders(transaction.getRequestHeaders(), false);
        if (!TextUtils.isEmpty(headers)) {
            text += headers + "\n";
        }
        text += (transaction.requestBodyIsPlainText()) ? v(transaction.getFormattedRequestBody()) :
                context.getString(R.string.chuck_body_omitted);
        text += "\n\n";
        text += "---------- " + context.getString(R.string.chuck_response) + " ----------\n\n";
        headers = formatHeaders(transaction.getResponseHeaders(), false);
        if (!TextUtils.isEmpty(headers)) {
            text += headers + "\n";
        }
        text += (transaction.responseBodyIsPlainText()) ? v(transaction.getFormattedResponseBody()) :
                context.getString(R.string.chuck_body_omitted);
        return text;
    }

    public static String getShareCurlCommand(HttpTransaction transaction) {
        boolean compressed = false;
        String curlCmd = "curl";
        curlCmd += " -X " + transaction.getMethod();
        List<HttpHeader> headers = transaction.getRequestHeaders();
        for (int i = 0, count = headers.size(); i < count; i++) {
            String name = headers.get(i).getName();
            String value = headers.get(i).getValue();
            if ("Accept-Encoding".equalsIgnoreCase(name) && "gzip".equalsIgnoreCase(value)) {
                compressed = true;
            }
            curlCmd += " -H " + "\"" + name + ": " + value + "\"";
        }
        String requestBody = transaction.getRequestBody();
        if (requestBody != null && requestBody.length() > 0) {
            // try to keep to a single line and use a subshell to preserve any line breaks
            curlCmd += " --data $'" + requestBody.replace("\n", "\\n") + "'";
        }
        curlCmd += ((compressed) ? " --compressed " : " ") + transaction.getUrl();
        return curlCmd;
    }

    private static String v(String string) {
        return (string != null) ? string : "";
    }
}