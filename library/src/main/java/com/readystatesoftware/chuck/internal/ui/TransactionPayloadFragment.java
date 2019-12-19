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
package com.readystatesoftware.chuck.internal.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.dandan.jsonhandleview.library.JsonViewLayout;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.readystatesoftware.chuck.GsonInstance;
import com.readystatesoftware.chuck.R;
import com.readystatesoftware.chuck.internal.data.HttpTransaction;

public class TransactionPayloadFragment extends Fragment implements TransactionFragment {

    public static final int TYPE_REQUEST = 0;
    public static final int TYPE_RESPONSE = 1;

    private static final String ARG_TYPE = "type";

    boolean plain = false;
    String response = "";

    TextView headers;
    JsonViewLayout body;
    TextView plainBody;
    Button expandBtn;
    Button collapseBtn;
    Button plainHighlightedToggle;

    private int type;
    private HttpTransaction transaction;

    public TransactionPayloadFragment() {
    }

    public static TransactionPayloadFragment newInstance(int type) {
        TransactionPayloadFragment fragment = new TransactionPayloadFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_TYPE, type);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getArguments().getInt(ARG_TYPE);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chuck_fragment_transaction_payload, container, false);
        headers = view.findViewById(R.id.headers);
        body = view.findViewById(R.id.body);
        plainBody = view.findViewById(R.id.plain_body);
        expandBtn = view.findViewById(R.id.expandBtn);
        collapseBtn = view.findViewById(R.id.collapseBtn);
        plainHighlightedToggle = view.findViewById(R.id.plainHighlightedToggle);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        populateUI();
        expandBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expand();
            }
        });
        collapseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collapse();
            }
        });
        plainHighlightedToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlainHighlighted();
            }
        });
    }

    private void expand() {
        expandBtn.setEnabled(false);
        body.expandAll();
        expandBtn.setEnabled(true);
    }

    private void collapse() {
        collapseBtn.setEnabled(false);
        body.collapseAll();
        collapseBtn.setEnabled(true);
    }

    private void togglePlainHighlighted() {
        if (plain) {
            bindJson(response);
        } else {
            bindText(response);
        }
    }

    @Override
    public void transactionUpdated(HttpTransaction transaction) {
        this.transaction = transaction;
        populateUI();
    }

    private void populateUI() {
        if (isAdded() && transaction != null) {
            switch (type) {
                case TYPE_REQUEST:
                    setText(transaction.getRequestHeadersString(true),
                            transaction.getFormattedRequestBody(), transaction.requestBodyIsPlainText());
                    break;
                case TYPE_RESPONSE:
                    setText(transaction.getResponseHeadersString(true),
                            transaction.getFormattedResponseBody(), transaction.responseBodyIsPlainText());
                    break;
            }
        }
    }

    private void setText(String headersString, String bodyString, boolean isPlainText) {
        headers.setVisibility((TextUtils.isEmpty(headersString) ? View.GONE : View.VISIBLE));
        headers.setText(Html.fromHtml(headersString));
        response = bodyString;
        if (!isPlainText) {
            bindText(getString(R.string.chuck_body_omitted));
        } else if (bodyString != null && isJson(bodyString)) {
            bindJson(bodyString);
            body.bindJson(bodyString);
            body.setTextSize(13);
        } else {
            bindText(bodyString);
        }
    }

    private void bindJson(String bodyString) {
        if (bodyString == null) return;
        plainBody.setVisibility(View.GONE);
        body.setVisibility(View.VISIBLE);
        plain = false;
        plainHighlightedToggle.setText(R.string.plain);
        expandBtn.setVisibility(View.VISIBLE);
        collapseBtn.setVisibility(View.VISIBLE);
    }

    private void bindText(String bodyString) {
        if (bodyString == null) return;
        if (isJson(bodyString)) {
            plainBody.setText(prettyPrint(bodyString));
        } else {
            plainBody.setText(bodyString);
        }

        plainBody.setVisibility(View.VISIBLE);
        body.setVisibility(View.GONE);
        plain = true;
        plainHighlightedToggle.setText(R.string.highlighted);

        expandBtn.setVisibility(View.GONE);
        collapseBtn.setVisibility(View.GONE);
    }

    private boolean isJson(String body) {
        try {
            JsonElement element = new JsonParser().parse(body);
            return element.isJsonObject() || element.isJsonArray();
        } catch (JsonSyntaxException e) {
            return false;
        }
    }

    private String prettyPrint(String str) {
        JsonElement parser = new JsonParser().parse(str);
        if (parser.isJsonObject()) {
            return GsonInstance.get().toJson(parser.getAsJsonObject());
        } else if (parser.isJsonArray()) {
            return GsonInstance.get().toJson(parser.getAsJsonArray());
        } else {
            return str;
        }

    }
}
