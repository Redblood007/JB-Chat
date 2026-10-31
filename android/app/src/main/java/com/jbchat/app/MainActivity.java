package com.jbchat.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends Activity {
    private static final String API_URL = "https://jb-chat-omega.vercel.app/api/chat";
    private static final int BG = Color.rgb(19, 19, 20), PANEL = Color.rgb(33, 33, 35);
    private static final int FG = Color.rgb(236, 236, 236), MUTED = Color.rgb(160, 160, 160);
    private final ExecutorService network = Executors.newSingleThreadExecutor();
    private LinearLayout messages;
    private ScrollView scroll;
    private EditText composer;
    private TextView welcome;
    private String responseId = "";
    private JSONArray history = new JSONArray();
    private boolean busy;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG); getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);
        loadHistory(); buildScreen();
        if (history.length() == 0) showWelcome(); else restoreHistory();
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(BG);
        LinearLayout top = new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL); top.setPadding(dp(14), dp(8), dp(14), dp(8));
        TextView menu = text("☰", 23, FG); menu.setGravity(Gravity.CENTER); top.addView(menu, new LinearLayout.LayoutParams(dp(44), dp(46)));
        TextView title = text("JB Chat", 18, FG); title.setTypeface(null, Typeface.BOLD); top.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        TextView fresh = text("＋", 26, FG); fresh.setGravity(Gravity.CENTER); fresh.setOnClickListener(v -> newChat()); top.addView(fresh, new LinearLayout.LayoutParams(dp(44), dp(46)));
        root.addView(top, new LinearLayout.LayoutParams(-1, dp(58)));
        scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setClipToPadding(false);
        messages = new LinearLayout(this); messages.setOrientation(LinearLayout.VERTICAL); messages.setPadding(dp(16), dp(24), dp(16), dp(18));
        scroll.addView(messages, new ScrollView.LayoutParams(-1, -2)); root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout bottom = new LinearLayout(this); bottom.setOrientation(LinearLayout.VERTICAL); bottom.setPadding(dp(12), dp(8), dp(12), dp(12));
        LinearLayout box = new LinearLayout(this); box.setGravity(Gravity.CENTER_VERTICAL); box.setPadding(dp(12), dp(5), dp(6), dp(5)); box.setBackground(round(PANEL, 28));
        composer = new EditText(this); composer.setTextColor(FG); composer.setHintTextColor(MUTED); composer.setHint("Message JB Chat"); composer.setTextSize(16); composer.setMinHeight(dp(48)); composer.setMaxLines(5); composer.setPadding(dp(4), dp(7), dp(4), dp(7)); composer.setBackgroundColor(Color.TRANSPARENT); composer.setSingleLine(false); composer.setImeOptions(EditorInfo.IME_ACTION_SEND); composer.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        composer.setOnEditorActionListener((v, action, event) -> { if (action == EditorInfo.IME_ACTION_SEND) { send(); return true; } return false; });
        box.addView(composer, new LinearLayout.LayoutParams(0, -2, 1));
        TextView send = text("↑", 23, Color.BLACK); send.setGravity(Gravity.CENTER); send.setTypeface(null, Typeface.BOLD); send.setBackground(round(Color.WHITE, 24)); box.addView(send, new LinearLayout.LayoutParams(dp(42), dp(42))); send.setOnClickListener(v -> send());
        bottom.addView(box, new LinearLayout.LayoutParams(-1, -2)); TextView note = text("JB Chat can make mistakes. Check important information.", 11, MUTED); note.setGravity(Gravity.CENTER); note.setPadding(0, dp(7), 0, 0); bottom.addView(note);
        root.addView(bottom); setContentView(root);
    }

    private void showWelcome() {
        messages.removeAllViews();
        LinearLayout center = new LinearLayout(this); center.setOrientation(LinearLayout.VERTICAL); center.setGravity(Gravity.CENTER); center.setPadding(dp(8), dp(48), dp(8), dp(24));
        TextView mark = text("✳", 38, Color.WHITE); mark.setGravity(Gravity.CENTER); mark.setBackground(round(Color.rgb(40, 40, 42), 50)); LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(dp(64), dp(64)); mp.gravity = Gravity.CENTER; center.addView(mark, mp);
        welcome = text("What can I help with?", 25, FG); welcome.setTypeface(null, Typeface.BOLD); welcome.setGravity(Gravity.CENTER); welcome.setPadding(0, dp(20), 0, dp(22)); center.addView(welcome);
        String[] ideas = {"Explain something simply", "Help me write a message", "Plan a trip", "Give me ideas"};
        for (String idea : ideas) { TextView item = text("✦   " + idea, 15, FG); item.setPadding(dp(15), dp(15), dp(15), dp(15)); item.setBackground(round(Color.rgb(29, 29, 31), 16)); LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(-1, -2); ip.setMargins(0, dp(5), 0, dp(5)); center.addView(item, ip); item.setOnClickListener(v -> { composer.setText(idea); send(); }); }
        messages.addView(center, new LinearLayout.LayoutParams(-1, -1));
    }

    private void send() {
        if (busy) return; String prompt = composer.getText().toString().trim(); if (prompt.isEmpty()) return;
        composer.setText(""); ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(composer.getWindowToken(), 0);
        if (history.length() == 0) messages.removeAllViews();
        appendBubble(prompt, true); addHistory("user", prompt); persist();
        busy = true; TextView pending = appendBubble("Thinking…", false); scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
        network.execute(() -> {
            String answer; String nextId = "";
            try {
                JSONObject body = new JSONObject().put("message", prompt).put("task", "chat"); if (!responseId.isEmpty()) body.put("previousResponseId", responseId);
                HttpURLConnection c = (HttpURLConnection)new URL(API_URL).openConnection(); c.setRequestMethod("POST"); c.setConnectTimeout(20000); c.setReadTimeout(60000); c.setDoOutput(true); c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                try (OutputStream out = c.getOutputStream()) { out.write(body.toString().getBytes(StandardCharsets.UTF_8)); }
                int code = c.getResponseCode(); BufferedReader reader = new BufferedReader(new InputStreamReader(code < 400 ? c.getInputStream() : c.getErrorStream(), StandardCharsets.UTF_8)); StringBuilder raw = new StringBuilder(); String line; while ((line = reader.readLine()) != null) raw.append(line); reader.close(); c.disconnect();
                JSONObject result = new JSONObject(raw.toString()); if (code >= 400) throw new Exception(result.optString("error", "Request failed (" + code + ")")); answer = result.optString("content", "No response returned."); nextId = result.optString("responseId", "");
            } catch (Exception e) { answer = "I couldn't connect to JB Chat. Please try again in a moment. (" + e.getMessage() + ")"; }
            final String reply = answer, rid = nextId; runOnUiThread(() -> { pending.setText(reply); addHistory("assistant", reply); if (!rid.isEmpty()) responseId = rid; persist(); busy = false; scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN)); });
        });
    }

    private TextView appendBubble(String value, boolean user) {
        TextView bubble = text(value, 16, FG); bubble.setTextIsSelectable(true); bubble.setPadding(dp(15), dp(12), dp(15), dp(12)); bubble.setBackground(round(user ? Color.rgb(48, 48, 50) : BG, user ? 20 : 8));
        LinearLayout row = new LinearLayout(this); row.setGravity(user ? Gravity.END : Gravity.START); row.addView(bubble, new LinearLayout.LayoutParams(user ? -2 : -1, -2)); LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, -2); rp.bottomMargin = dp(12); messages.addView(row, rp); return bubble;
    }

    private void newChat() { history = new JSONArray(); responseId = ""; persist(); showWelcome(); }
    private void addHistory(String role, String value) { try { history.put(new JSONObject().put("role", role).put("text", value)); } catch (Exception ignored) {} }
    private void loadHistory() { try { String saved = getPreferences(0).getString("history", "[]"); history = new JSONArray(saved); responseId = getPreferences(0).getString("responseId", ""); } catch (Exception ignored) { history = new JSONArray(); } }
    private void persist() { getPreferences(0).edit().putString("history", history.toString()).putString("responseId", responseId).apply(); }
    private void restoreHistory() { messages.removeAllViews(); for (int i = 0; i < history.length(); i++) { JSONObject item = history.optJSONObject(i); if (item != null) appendBubble(item.optString("text"), "user".equals(item.optString("role"))); } }
    private TextView text(String value, int size, int color) { TextView v = new TextView(this); v.setText(value); v.setTextSize(size); v.setTextColor(color); return v; }
    private GradientDrawable round(int color, int radius) { GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d; }
    private int dp(float value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }
    @Override protected void onDestroy() { network.shutdownNow(); super.onDestroy(); }
}
