package com.readystatesoftware.chuck.internal.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.readystatesoftware.chuck.R;
import com.readystatesoftware.chuck.internal.data.HttpTransaction;

public class TransactionPayloadFragment extends Fragment implements TransactionFragment {

    public static final int TYPE_REQUEST = 0;
    public static final int TYPE_RESPONSE = 1;

    private static final String ARG_TYPE = "type";

    TextView headers;
    TextView body;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getArguments().getInt(ARG_TYPE);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chuck_fragment_transaction_payload, container, false);
        headers = (TextView) view.findViewById(R.id.headers);
        body = (TextView) view.findViewById(R.id.body);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        populateUI();
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
        if (!isPlainText) {
            body.setText(getString(R.string.chuck_body_omitted));
        } else {
            body.setText(bodyString);
        }
    }
}