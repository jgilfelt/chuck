package com.readystatesoftware.chuck.internal.support;

import android.content.Context;

import com.readystatesoftware.chuck.R;

import java.io.EOFException;
import java.nio.charset.Charset;

import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;
import okio.Okio;

/**
 * @author Olivier Perez
 */
public class IOUtils {

    private final Context context;

    public IOUtils(Context context) {
        this.context = context;
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    public boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }

    public String readFromBuffer(Buffer buffer, Charset charset, long maxContentLength) {
        long bufferSize = buffer.size();
        long maxBytes = Math.min(bufferSize, maxContentLength);
        String body = "";
        try {
            body = buffer.readString(maxBytes, charset);
        } catch (EOFException e) {
            body += context.getString(R.string.chuck_body_unexpected_eof);
        }
        if (bufferSize > maxContentLength) {
            body += context.getString(R.string.chuck_body_content_truncated);
        }
        return body;
    }

    public BufferedSource getNativeSource(BufferedSource input, boolean isGzipped) {
        if (isGzipped) {
            GzipSource source = new GzipSource(input);
            return Okio.buffer(source);
        } else {
            return input;
        }
    }

    public boolean bodyHasSupportedEncoding(String contentEncoding) {
        return contentEncoding != null &&
                (contentEncoding.equalsIgnoreCase("identity") ||
                        contentEncoding.equalsIgnoreCase("gzip"));
    }

    public boolean bodyIsGzipped(String contentEncoding) {
        return "gzip".equalsIgnoreCase(contentEncoding);
    }
}
