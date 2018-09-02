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
package com.readystatesoftware.chuck.internal.data;

import android.net.Uri;

import com.google.gson.reflect.TypeToken;
import com.readystatesoftware.chuck.internal.support.FormatUtils;
import com.readystatesoftware.chuck.internal.support.JsonConvertor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.qbusict.cupboard.annotation.Index;
import okhttp3.Headers;

public class HttpTransaction {

    public enum Status {
        Requested,
        Complete,
        Failed
    }

    public static final String[] PARTIAL_PROJECTION = new String[] {
            "_id",
            "requestDate",
            "tookMs",
            "method",
            "host",
            "path",
            "scheme",
            "requestContentLength",
            "responseCode",
            "error",
            "responseContentLength"
    };

    private Long _id;
    @Index private Date requestDate;
    private Date responseDate;
    private Long tookMs;

    private String protocol;
    private String method;
    private String url;
    private String host;
    private String path;
    private String scheme;

    private Long requestContentLength;
    private String requestContentType;
    private String requestHeaders;
    private String requestBody;
    private boolean requestBodyIsPlainText = true;

    private Integer responseCode;
    private String responseMessage;
    private String error;

    private Long responseContentLength;
    private String responseContentType;
    private String responseHeaders;
    private String responseBody;
    private boolean responseBodyIsPlainText = true;

    public Long getId() {
        return _id;
    }

    public HttpTransaction setId(long id) {
        _id = id;
        return this;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public HttpTransaction setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
        return this;
    }

    public Date getResponseDate() {
        return responseDate;
    }

    public HttpTransaction setResponseDate(Date responseDate) {
        this.responseDate = responseDate;
        return this;
    }

    public String getError() {
        return error;
    }

    public HttpTransaction setError(String error) {
        this.error = error;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public HttpTransaction setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getProtocol() {
        return protocol;
    }

    public HttpTransaction setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public String getFormattedRequestBody() {
        return formatBody(requestBody, requestContentType);
    }

    public HttpTransaction setRequestBody(String requestBody) {
        this.requestBody = requestBody;
        return this;
    }

    public boolean requestBodyIsPlainText() {
        return requestBodyIsPlainText;
    }

    public HttpTransaction setRequestBodyIsPlainText(boolean requestBodyIsPlainText) {
        this.requestBodyIsPlainText = requestBodyIsPlainText;
        return this;
    }

    public Long getRequestContentLength() {
        return requestContentLength;
    }

    public HttpTransaction setRequestContentLength(Long requestContentLength) {
        this.requestContentLength = requestContentLength;
        return this;
    }

    public String getRequestContentType() {
        return requestContentType;
    }

    public HttpTransaction setRequestContentType(String requestContentType) {
        this.requestContentType = requestContentType;
        return this;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public String getFormattedResponseBody() {
        return formatBody(responseBody, responseContentType);
    }

    public HttpTransaction setResponseBody(String responseBody) {
        this.responseBody = responseBody;
        return this;
    }

    public boolean responseBodyIsPlainText() {
        return responseBodyIsPlainText;
    }

    public HttpTransaction setResponseBodyIsPlainText(boolean responseBodyIsPlainText) {
        this.responseBodyIsPlainText = responseBodyIsPlainText;
        return this;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public HttpTransaction setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
        return this;
    }

    public Long getResponseContentLength() {
        return responseContentLength;
    }

    public HttpTransaction setResponseContentLength(Long responseContentLength) {
        this.responseContentLength = responseContentLength;
        return this;
    }

    public String getResponseContentType() {
        return responseContentType;
    }

    public HttpTransaction setResponseContentType(String responseContentType) {
        this.responseContentType = responseContentType;
        return this;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public HttpTransaction setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
        return this;
    }

    public Long getTookMs() {
        return tookMs;
    }

    public HttpTransaction setTookMs(Long tookMs) {
        this.tookMs = tookMs;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public HttpTransaction setUrl(String url) {
        this.url = url;
        Uri uri = Uri.parse(url);
        host = uri.getHost();
        path = uri.getPath() + ((uri.getQuery() != null) ? "?" + uri.getQuery() : "");
        scheme = uri.getScheme();
        return this;
    }

    public String getHost() {
        return host;
    }

    public String getPath() {
        return path;
    }

    public String getScheme() {
        return scheme;
    }

    public HttpTransaction setRequestHeaders(Headers headers) {
        setRequestHeaders(toHttpHeaderList(headers));
        return this;
    }

    public HttpTransaction setRequestHeaders(List<HttpHeader> headers) {
        requestHeaders = JsonConvertor.getInstance().toJson(headers);
        return this;
    }

    public List<HttpHeader> getRequestHeaders() {
        return JsonConvertor.getInstance().fromJson(requestHeaders,
                new TypeToken<List<HttpHeader>>(){}.getType());
    }

    public String getRequestHeadersString(boolean withMarkup) {
        return FormatUtils.formatHeaders(getRequestHeaders(), withMarkup);
    }

    public HttpTransaction setResponseHeaders(Headers headers) {
        setResponseHeaders(toHttpHeaderList(headers));
        return this;
    }

    public HttpTransaction setResponseHeaders(List<HttpHeader> headers) {
        responseHeaders = JsonConvertor.getInstance().toJson(headers);
        return this;
    }

    public List<HttpHeader> getResponseHeaders() {
        return JsonConvertor.getInstance().fromJson(responseHeaders,
                new TypeToken<List<HttpHeader>>(){}.getType());
    }

    public String getResponseHeadersString(boolean withMarkup) {
        return FormatUtils.formatHeaders(getResponseHeaders(), withMarkup);
    }

    public Status getStatus() {
        if (error != null) {
            return Status.Failed;
        } else if (responseCode == null) {
            return Status.Requested;
        } else {
            return Status.Complete;
        }
    }

    public String getRequestDateString() {
        return (requestDate != null) ? requestDate.toString() : null;
    }

    public String getResponseDateString() {
        return (responseDate != null) ? responseDate.toString() : null;
    }

    public String getDurationString() {
        return (tookMs != null) ? + tookMs + " ms" : null;
    }

    public String getRequestSizeString() {
        return formatBytes((requestContentLength != null) ? requestContentLength : 0);
    }
    public String getResponseSizeString() {
        return (responseContentLength != null) ? formatBytes(responseContentLength) : null;
    }

    public String getTotalSizeString() {
        long reqBytes = (requestContentLength != null) ? requestContentLength : 0;
        long resBytes = (responseContentLength != null) ? responseContentLength : 0;
        return formatBytes(reqBytes + resBytes);
    }

    public String getResponseSummaryText() {
        switch (getStatus()) {
            case Failed:
                return error;
            case Requested:
                return null;
            default:
                return String.valueOf(responseCode) + " " + responseMessage;
        }
    }

    public String getNotificationText() {
        switch (getStatus()) {
            case Failed:
                return " ! ! !  " + path;
            case Requested:
                return " . . .  " + path;
            default:
                return String.valueOf(responseCode) + " " + path;
        }
    }

    public boolean isSsl() {
        return scheme.toLowerCase().equals("https");
    }

    private List<HttpHeader> toHttpHeaderList(Headers headers) {
        List<HttpHeader> httpHeaders = new ArrayList<>();
        for (int i = 0, count = headers.size(); i < count; i++) {
            httpHeaders.add(new HttpHeader(headers.name(i), headers.value(i)));
        }
        return httpHeaders;
    }

    private String formatBody(String body, String contentType) {
        if (contentType != null && contentType.toLowerCase().contains("json")) {
            return FormatUtils.formatJson(body);
        } else if (contentType != null && contentType.toLowerCase().contains("xml")) {
            return FormatUtils.formatXml(body);
        } else {
            return body;
        }
    }

    private String formatBytes(long bytes) {
        return FormatUtils.formatByteCount(bytes, true);
    }
}
