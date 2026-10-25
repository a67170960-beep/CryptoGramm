package org.telegram.ui;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.text.InputType;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.CryptogramCaseStore;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/** Private, offline-first case workspace with a movable entity graph. */
public class CryptogramCaseToolActivity extends BaseFragment {
    private static final int REQUEST_UNLOCK = 9311;
    private static final int REQUEST_IMPORT = 9312;
    private static final int REQUEST_EXPORT_JSON = 9313;
    private static final int REQUEST_EXPORT_HTML = 9314;
    private static final int MAX_IMPORT_BYTES = 4 * 1024 * 1024;

    private Context context;
    private LinearLayout content;
    private JSONObject currentCase;
    private boolean unlocked;
    private Runnable pendingAuthentication;
    private String pendingExport;
    private String pendingMime;
    private int pendingRequest;
    private TextView selectionLabel;
    private CaseGraphView graphView;

    public CryptogramCaseToolActivity(int account) {
        setCurrentAccount(account);
    }

    @Override
    public View createView(Context ctx) {
        context = ctx;
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString(R.string.CryptogramCases));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
        ScrollView scroll = new ScrollView(ctx);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(20, 27, 34));
        content = new LinearLayout(ctx);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(24));
        scroll.addView(content, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
        fragmentView = scroll;
        render();
        return fragmentView;
    }

    @Override
    public void onFragmentDestroy() {
        unlocked = false;
        currentCase = null;
        super.onFragmentDestroy();
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        super.onActivityResultFragment(requestCode, resultCode, data);
        if (requestCode == REQUEST_UNLOCK) {
            Runnable action = pendingAuthentication;
            pendingAuthentication = null;
            if (resultCode == Activity.RESULT_OK && action != null) {
                action.run();
            } else {
                toast(R.string.CaseAuthenticationCanceled);
            }
        } else if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == REQUEST_IMPORT) {
                importCase(data.getData());
            } else if (requestCode == REQUEST_EXPORT_JSON || requestCode == REQUEST_EXPORT_HTML) {
                writeExport(data.getData());
            }
        }
    }

    private void render() {
        if (content == null) {
            return;
        }
        content.removeAllViews();
        if (!unlocked) {
            addHeading(content, LocaleController.getString(R.string.CryptogramCases));
            addParagraph(content, LocaleController.getString(R.string.CaseUnlockInfo));
            addAction(content, R.string.CaseUnlock, () -> authenticate(() -> {
                unlocked = true;
                render();
            }));
            return;
        }
        if (currentCase == null) {
            renderCaseList();
        } else {
            renderBoard();
        }
    }

    private void renderCaseList() {
        addHeading(content, LocaleController.getString(R.string.CryptogramCases));
        addAction(content, R.string.CaseCreate, this::showCreateCaseDialog);
        addAction(content, R.string.CaseImport, this::chooseImport);
        addAction(content, R.string.CaseLock, () -> {
            unlocked = false;
            render();
        });
        try {
            ArrayList<JSONObject> cases = CryptogramCaseStore.list(context, currentAccount);
            if (cases.isEmpty()) {
                addParagraph(content, LocaleController.getString(R.string.CaseNoCases));
            }
            for (JSONObject item : cases) {
                String title = item.optString("title", "");
                String status = statusText(item.optString("status", "open"));
                int nodes = item.optJSONArray("nodes") == null ? 0 : item.optJSONArray("nodes").length();
                addCaseRow(title + "\n" + status + "  ·  " + nodes + " entities", () -> {
                    currentCase = item;
                    render();
                }, () -> confirmDelete(item));
            }
        } catch (Exception e) {
            addParagraph(content, LocaleController.getString(R.string.CaseWriteError));
        }
    }

    private void renderBoard() {
        String title = currentCase.optString("title", "");
        addHeading(content, title);
        addAction(content, statusActionLabel(), this::cycleStatus);
        addProgress();
        graphView = new CaseGraphView(context, currentCase);
        LinearLayout.LayoutParams graphParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(390));
        graphParams.topMargin = AndroidUtilities.dp(8);
        graphParams.bottomMargin = AndroidUtilities.dp(10);
        content.addView(graphView, graphParams);
        selectionLabel = addParagraph(content, LocaleController.getString(R.string.CaseAddEntity));
        graphView.setSelectionListener(index -> updateSelectedLabel(index));
        addAction(content, R.string.CaseAddEntity, this::showAddEntityType);
        addAction(content, R.string.CaseAddRelation, this::showAddRelation);
        addAction(content, R.string.CaseExtractText, this::showExtractTextDialog);
        addAction(content, R.string.CaseSensitiveReveal, this::revealSensitive);
        addAction(content, R.string.CaseTasks, this::showTasks);
        addAction(content, R.string.CaseAddTask, this::showAddTask);
        addAction(content, R.string.CaseHypotheses, this::showHypotheses);
        addAction(content, R.string.CaseAddHypothesis, this::showAddHypothesis);
        addAction(content, R.string.CaseEvidence, this::showEvidence);
        addAction(content, R.string.CaseAddEvidence, this::showAddEvidence);
        addAction(content, R.string.CaseTimeline, this::showTimeline);
        addAction(content, R.string.CaseMap, this::showCoordinates);
        addAction(content, R.string.CaseExportJson, () -> authenticate(() -> chooseExportJson()));
        addAction(content, R.string.CaseExportHtml, this::chooseExportHtml);
        addAction(content, R.string.CasePrintReport, this::printReport);
        addAction(content, R.string.CaseBack, () -> {
            currentCase = null;
            graphView = null;
            render();
        });
        addAction(content, R.string.CaseLock, () -> {
            currentCase = null;
            unlocked = false;
            render();
        });
    }

    private void updateSelectedLabel(int index) {
        if (selectionLabel == null || currentCase == null) {
            return;
        }
        JSONArray nodes = currentCase.optJSONArray("nodes");
        if (index < 0 || nodes == null || index >= nodes.length()) {
            selectionLabel.setText(LocaleController.getString(R.string.CaseAddEntity));
            return;
        }
        JSONObject node = nodes.optJSONObject(index);
        selectionLabel.setText(node == null ? "" : node.optString("label", ""));
    }

    private void showCreateCaseDialog() {
        EditText title = input(LocaleController.getString(R.string.CaseEntityName), false);
        new android.app.AlertDialog.Builder(context)
                .setTitle(LocaleController.getString(R.string.CaseCreate))
                .setView(wrapInput(title))
                .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                .setPositiveButton(LocaleController.getString(R.string.CaseCreate), (dialog, which) -> {
                    try {
                        JSONObject item = CryptogramCaseStore.newCase(title.getText().toString());
                        CryptogramCaseStore.save(context, currentAccount, item);
                        recordEvent(item, "Case created", null);
                        CryptogramCaseStore.save(context, currentAccount, item);
                        currentCase = item;
                        render();
                    } catch (Exception e) {
                        toast(R.string.CaseInvalidTitle);
                    }
                }).show();
    }

    private void confirmDelete(JSONObject item) {
        new android.app.AlertDialog.Builder(context)
                .setMessage(LocaleController.getString(R.string.CaseConfirmDelete))
                .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                .setPositiveButton(LocaleController.getString(R.string.CaseDelete), (dialog, which) -> {
                    try {
                        CryptogramCaseStore.delete(context, currentAccount, item.optString("id"));
                        render();
                    } catch (Exception e) {
                        toast(R.string.CaseWriteError);
                    }
                }).show();
    }

    private void showAddEntityType() {
        String[] labels = new String[] {
                LocaleController.getString(R.string.CaseTypePerson),
                LocaleController.getString(R.string.CaseTypeChat),
                LocaleController.getString(R.string.CaseTypeChannel),
                LocaleController.getString(R.string.CaseTypeDomain),
                LocaleController.getString(R.string.CaseTypeFile),
                LocaleController.getString(R.string.CaseTypePlace),
                LocaleController.getString(R.string.CaseTypeOrganization),
                LocaleController.getString(R.string.CaseTypeOther)
        };
        String[] types = new String[] { "person", "chat", "channel", "domain", "file", "place", "organization", "other" };
        new android.app.AlertDialog.Builder(context)
                .setTitle(LocaleController.getString(R.string.CaseAddEntity))
                .setItems(labels, (dialog, which) -> showEntityForm(types[which]))
                .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                .show();
    }

    private void showEntityForm(String type) {
        LinearLayout form = formLayout();
        EditText name = input(LocaleController.getString(R.string.CaseEntityName), true);
        EditText notes = input(LocaleController.getString(R.string.CaseNotes), false);
        EditText privateData = input(LocaleController.getString(R.string.CaseSensitiveData), false);
        EditText latitude = input(LocaleController.getString(R.string.CaseLatitude), true);
        EditText longitude = input(LocaleController.getString(R.string.CaseLongitude), true);
        appendInput(form, name);
        appendInput(form, notes);
        appendInput(form, privateData);
        appendInput(form, latitude);
        appendInput(form, longitude);
        new android.app.AlertDialog.Builder(context)
                .setTitle(LocaleController.getString(R.string.CaseAddNodePrompt))
                .setView(form)
                .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                .setPositiveButton(LocaleController.getString(R.string.CaseDone), (dialog, which) -> {
                    String label = name.getText().toString().trim();
                    if (label.isEmpty()) {
                        toast(R.string.CaseInvalidTitle);
                        return;
                    }
                    try {
                        JSONArray nodes = currentCase.getJSONArray("nodes");
                        JSONObject node = new JSONObject();
                        node.put("id", UUID.randomUUID().toString());
                        node.put("type", type);
                        node.put("label", label);
                        node.put("notes", notes.getText().toString());
                        node.put("privateData", privateData.getText().toString());
                        node.put("x", 0.15 + (nodes.length() % 4) * 0.21);
                        node.put("y", 0.18 + ((nodes.length() / 4) % 4) * 0.19);
                        double lat = parseCoordinate(latitude.getText().toString(), -90, 90);
                        double lon = parseCoordinate(longitude.getText().toString(), -180, 180);
                        if (!Double.isNaN(lat) && !Double.isNaN(lon)) {
                            node.put("latitude", lat);
                            node.put("longitude", lon);
                        }
                        nodes.put(node);
                        recordEvent(currentCase, "Entity added: " + label, node.optString("id"));
                        saveCurrent();
                    } catch (Exception e) {
                        toast(R.string.CaseWriteError);
                    }
                }).show();
    }

    private void showAddRelation() {
        if (graphView == null || graphView.getSelectedIndex() < 0) {
            toast(R.string.CaseNeedNode);
            return;
        }
        JSONArray nodes = currentCase.optJSONArray("nodes");
        if (nodes == null || nodes.length() < 2) {
            toast(R.string.CaseNeedNode);
            return;
        }
        int sourceIndex = graphView.getSelectedIndex();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Integer> targetIndices = new ArrayList<>();
        for (int i = 0; i < nodes.length(); i++) {
            if (i != sourceIndex) {
                JSONObject node = nodes.optJSONObject(i);
                if (node != null) {
                    labels.add(node.optString("label", ""));
                    targetIndices.add(i);
                }
            }
        }
        new android.app.AlertDialog.Builder(context)
                .setTitle(LocaleController.getString(R.string.CaseSelectTarget))
                .setItems(labels.toArray(new String[0]), (dialog, which) -> showRelationForm(sourceIndex, targetIndices.get(which)))
                .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                .show();
    }

    private void showRelationForm(int sourceIndex, int targetIndex) {
        EditText relation = input(LocaleController.getString(R.string.CaseRelationType), true);
        EditText confidence = input(LocaleController.getString(R.string.CaseConfidence), true);
        confidence.setText("70");
        LinearLayout form = formLayout();
        appendInput(form, relation);
        appendInput(form, confidence);
        new android.app.AlertDialog.Builder(context)
                .setTitle(LocaleController.getString(R.string.CaseAddRelation))
                .setView(form)
                .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                .setPositiveButton(LocaleController.getString(R.string.CaseDone), (dialog, which) -> {
                    try {
                        JSONArray nodes = currentCase.getJSONArray("nodes");
                        JSONObject edge = new JSONObject();
                        edge.put("source", nodes.getJSONObject(sourceIndex).getString("id"));
                        edge.put("target", nodes.getJSONObject(targetIndex).getString("id"));
                        String type = relation.getText().toString().trim();
                        edge.put("type", type.isEmpty() ? "related" : type);
                        int value = Integer.parseInt(confidence.getText().toString().trim());
                        edge.put("confidence", Math.max(0, Math.min(100, value)));
                        currentCase.getJSONArray("edges").put(edge);
                        recordEvent(currentCase, "Relationship added", null);
                        saveCurrent();
                    } catch (Exception e) {
                        toast(R.string.CaseWriteError);
                    }
                }).show();
    }

    private void showExtractTextDialog() {
        EditText text = input(LocaleController.getString(R.string.CaseExtractDisclaimer), false);
        text.setMinLines(5);
        text.setMaxLines(10);
        new android.app.AlertDialog.Builder(context)
                .setTitle(LocaleController.getString(R.string.CaseExtractText))
                .setView(wrapInput(text))
                .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                .setPositiveButton(LocaleController.getString(R.string.CaseDone), (dialog, which) -> {
                    try {
                        JSONArray candidates = CryptogramCaseStore.extractSelectedText(text.getText().toString());
                        if (candidates.length() == 0) {
                            toast(R.string.CaseNoCandidates);
                            return;
                        }
                        String[] labels = new String[candidates.length()];
                        for (int i = 0; i < candidates.length(); i++) {
                            JSONObject candidate = candidates.getJSONObject(i);
                            labels[i] = candidate.optString("type") + "  ·  " + candidate.optString("value");
                        }
                        new android.app.AlertDialog.Builder(context)
                                .setTitle(LocaleController.getString(R.string.CaseExtractCandidates))
                                .setItems(labels, (d, index) -> addExtractedCandidate(candidates.optJSONObject(index)))
                                .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                                .show();
                    } catch (Exception e) {
                        toast(R.string.CaseNoCandidates);
                    }
                }).show();
    }

    private void addExtractedCandidate(JSONObject candidate) {
        if (candidate == null) {
            return;
        }
        try {
            JSONArray nodes = currentCase.getJSONArray("nodes");
            JSONObject node = new JSONObject();
            node.put("id", UUID.randomUUID().toString());
            node.put("type", candidate.optString("type", "other"));
            node.put("label", candidate.optString("value", ""));
            node.put("notes", "Added from user-selected text");
            node.put("privateData", "");
            node.put("source", candidate.optString("source", "selected_text"));
            node.put("x", 0.16 + (nodes.length() % 4) * 0.21);
            node.put("y", 0.18 + ((nodes.length() / 4) % 4) * 0.19);
            nodes.put(node);
            recordEvent(currentCase, "Candidate added from selected text", node.optString("id"));
            saveCurrent();
        } catch (Exception e) {
            toast(R.string.CaseWriteError);
        }
    }

    private void showAddTask() {
        EditText title = input(LocaleController.getString(R.string.CaseTaskTitle), true);
        new android.app.AlertDialog.Builder(context)
                .setTitle(LocaleController.getString(R.string.CaseAddTask))
                .setView(wrapInput(title))
                .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                .setPositiveButton(LocaleController.getString(R.string.CaseDone), (dialog, which) -> {
                    String value = title.getText().toString().trim();
                    if (!value.isEmpty()) {
                        try {
                            JSONObject task = new JSONObject();
                            task.put("id", UUID.randomUUID().toString());
                            task.put("title", value);
                            task.put("done", false);
                            currentCase.getJSONArray("tasks").put(task);
                            recordEvent(currentCase, "Task added: " + value, null);
                            saveCurrent();
                        } catch (Exception e) {
                            toast(R.string.CaseWriteError);
                        }
                    }
                }).show();
    }

    private void showTasks() {
        JSONArray tasks = currentCase.optJSONArray("tasks");
        if (tasks == null || tasks.length() == 0) {
            toast(R.string.CaseNoCases);
            return;
        }
        String[] labels = new String[tasks.length()];
        for (int i = 0; i < tasks.length(); i++) {
            JSONObject task = tasks.optJSONObject(i);
            labels[i] = (task != null && task.optBoolean("done") ? "[x] " : "[ ] ") + (task == null ? "" : task.optString("title"));
        }
        new android.app.AlertDialog.Builder(context)
                .setTitle(LocaleController.getString(R.string.CaseTasks))
                .setItems(labels, (dialog, which) -> {
                    JSONObject task = tasks.optJSONObject(which);
                    if (task != null) {
                        task.put("done", !task.optBoolean("done"));
                        recordEvent(currentCase, "Task updated: " + task.optString("title"), null);
                        saveCurrent();
                    }
                })
                .setPositiveButton(LocaleController.getString(R.string.CaseDone), null)
                .show();
    }

    private void showAddHypothesis() {
        EditText title = input(LocaleController.getString(R.string.CaseHypothesisTitle), false);
        new android.app.AlertDialog.Builder(context)
                .setTitle(LocaleController.getString(R.string.CaseAddHypothesis))
                .setView(wrapInput(title))
                .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                .setPositiveButton(LocaleController.getString(R.string.CaseDone), (dialog, which) -> {
                    String value = title.getText().toString().trim();
                    if (!value.isEmpty()) {
                        try {
                            JSONObject hypothesis = new JSONObject();
                            hypothesis.put("id", UUID.randomUUID().toString());
                            hypothesis.put("title", value);
                            hypothesis.put("status", "unverified");
                            currentCase.getJSONArray("hypotheses").put(hypothesis);
                            recordEvent(currentCase, "Hypothesis added", null);
                            saveCurrent();
                        } catch (Exception e) {
                            toast(R.string.CaseWriteError);
                        }
                    }
                }).show();
    }

    private void showHypotheses() {
        JSONArray items = currentCase.optJSONArray("hypotheses");
        if (items == null || items.length() == 0) {
            toast(R.string.CaseNoCases);
            return;
        }
        String[] labels = new String[items.length()];
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            labels[i] = (item == null ? "" : item.optString("status")) + "  ·  " + (item == null ? "" : item.optString("title"));
        }
        new android.app.AlertDialog.Builder(context)
                .setTitle(LocaleController.getString(R.string.CaseHypotheses))
                .setItems(labels, (dialog, which) -> {
                    JSONObject item = items.optJSONObject(which);
                    if (item != null) {
                        String status = item.optString("status", "unverified");
                        item.put("status", "unverified".equals(status) ? "confirmed" : ("confirmed".equals(status) ? "refuted" : "unverified"));
                        recordEvent(currentCase, "Hypothesis status changed", null);
                        saveCurrent();
                    }
                })
                .setPositiveButton(LocaleController.getString(R.string.CaseDone), null)
                .show();
    }

    private void showAddEvidence() {
        LinearLayout form = formLayout();
        EditText title = input(LocaleController.getString(R.string.CaseEvidenceTitle), true);
        EditText source = input(LocaleController.getString(R.string.CaseSource), true);
        EditText notes = input(LocaleController.getString(R.string.CaseNotes), false);
        appendInput(form, title);
        appendInput(form, source);
        appendInput(form, notes);
        new android.app.AlertDialog.Builder(context)
                .setTitle(LocaleController.getString(R.string.CaseAddEvidence))
                .setView(form)
                .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                .setPositiveButton(LocaleController.getString(R.string.CaseDone), (dialog, which) -> {
                    try {
                        JSONObject evidence = new JSONObject();
                        evidence.put("id", UUID.randomUUID().toString());
                        evidence.put("title", title.getText().toString().trim());
                        evidence.put("source", source.getText().toString().trim());
                        evidence.put("notes", notes.getText().toString());
                        evidence.put("capturedAt", System.currentTimeMillis());
                        currentCase.getJSONArray("evidence").put(evidence);
                        recordEvent(currentCase, "Evidence recorded: " + evidence.optString("title"), null);
                        saveCurrent();
                    } catch (Exception e) {
                        toast(R.string.CaseWriteError);
                    }
                }).show();
    }

    private void showEvidence() {
        JSONArray items = currentCase.optJSONArray("evidence");
        if (items == null || items.length() == 0) {
            toast(R.string.CaseNoCases);
            return;
        }
        String[] labels = new String[items.length()];
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            labels[i] = (item == null ? "" : item.optString("title")) + "  ·  " + (item == null ? "" : item.optString("source"));
        }
        new android.app.AlertDialog.Builder(context)
                .setTitle(LocaleController.getString(R.string.CaseEvidence))
                .setItems(labels, (dialog, which) -> {
                    JSONObject item = items.optJSONObject(which);
                    if (item != null) {
                        new android.app.AlertDialog.Builder(context)
                                .setTitle(item.optString("title"))
                                .setMessage(item.optString("notes") + "\n\n" + item.optString("source") + "\n" + formatTime(item.optLong("capturedAt")))
                                .setPositiveButton(LocaleController.getString(R.string.CaseDone), null)
                                .show();
                    }
                })
                .setPositiveButton(LocaleController.getString(R.string.CaseDone), null)
                .show();
    }

    private void showTimeline() {
        JSONArray events = currentCase.optJSONArray("events");
        JSONArray evidence = currentCase.optJSONArray("evidence");
        StringBuilder text = new StringBuilder();
        if (events != null) {
            for (int i = 0; i < events.length(); i++) {
                JSONObject event = events.optJSONObject(i);
                if (event != null) {
                    text.append(formatTime(event.optLong("at"))).append("  ·  ").append(event.optString("text")).append("\n");
                }
            }
        }
        if (evidence != null) {
            for (int i = 0; i < evidence.length(); i++) {
                JSONObject item = evidence.optJSONObject(i);
                if (item != null) {
                    text.append(formatTime(item.optLong("capturedAt"))).append("  ·  ").append(item.optString("title")).append(" [").append(item.optString("source")).append("]\n");
                }
            }
        }
        new android.app.AlertDialog.Builder(context)
                .setTitle(LocaleController.getString(R.string.CaseTimeline))
                .setMessage(text.length() == 0 ? LocaleController.getString(R.string.CaseNoCases) : text.toString())
                .setPositiveButton(LocaleController.getString(R.string.CaseDone), null)
                .show();
    }

    private void revealSensitive() {
        if (graphView == null || graphView.getSelectedIndex() < 0) {
            toast(R.string.CaseNeedNode);
            return;
        }
        int index = graphView.getSelectedIndex();
        JSONArray nodes = currentCase.optJSONArray("nodes");
        JSONObject node = nodes == null ? null : nodes.optJSONObject(index);
        if (node == null || node.optString("privateData").isEmpty()) {
            toast(R.string.CaseSensitiveLocked);
            return;
        }
        authenticate(() -> new android.app.AlertDialog.Builder(context)
                .setTitle(node.optString("label"))
                .setMessage(node.optString("privateData"))
                .setPositiveButton(LocaleController.getString(R.string.CaseDone), null)
                .show());
    }

    private void showCoordinates() {
        if (graphView == null || graphView.getSelectedIndex() < 0) {
            toast(R.string.CaseLocationMissing);
            return;
        }
        JSONArray nodes = currentCase.optJSONArray("nodes");
        JSONObject node = nodes == null ? null : nodes.optJSONObject(graphView.getSelectedIndex());
        if (node == null || !node.has("latitude") || !node.has("longitude")) {
            toast(R.string.CaseLocationMissing);
            return;
        }
        String label = node.optString("label", "");
        String uri = "geo:" + node.optDouble("latitude") + "," + node.optDouble("longitude") + "?q=" + node.optDouble("latitude") + "," + node.optDouble("longitude");
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
        } catch (Exception e) {
            toast(R.string.CaseLocationMissing);
        }
    }

    private void cycleStatus() {
        String status = currentCase.optString("status", "open");
        String next = "open".equals(status) ? "in_progress" : ("in_progress".equals(status) ? "resolved" : ("resolved".equals(status) ? "archived" : "open"));
        currentCase.put("status", next);
        recordEvent(currentCase, "Case status: " + next, null);
        saveCurrent();
    }

    private int statusActionLabel() {
        String status = currentCase.optString("status", "open");
        if ("in_progress".equals(status)) return R.string.CaseStatusWorking;
        if ("resolved".equals(status)) return R.string.CaseStatusResolved;
        if ("archived".equals(status)) return R.string.CaseStatusArchived;
        return R.string.CaseStatusOpen;
    }

    private String statusText(String status) {
        if ("in_progress".equals(status)) return LocaleController.getString(R.string.CaseStatusWorking);
        if ("resolved".equals(status)) return LocaleController.getString(R.string.CaseStatusResolved);
        if ("archived".equals(status)) return LocaleController.getString(R.string.CaseStatusArchived);
        return LocaleController.getString(R.string.CaseStatusOpen);
    }

    private void addProgress() {
        JSONArray tasks = currentCase.optJSONArray("tasks");
        int total = tasks == null ? 0 : tasks.length();
        int done = 0;
        if (tasks != null) {
            for (int i = 0; i < tasks.length(); i++) {
                JSONObject task = tasks.optJSONObject(i);
                if (task != null && task.optBoolean("done")) done++;
            }
        }
        addParagraph(content, LocaleController.getString(R.string.CaseProgress) + ": " + done + "/" + total);
    }

    private void chooseImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        startActivityForResult(intent, REQUEST_IMPORT);
    }

    private void chooseExportJson() {
        pendingExport = CryptogramCaseStore.exportJson(currentCase);
        pendingMime = "application/json";
        pendingRequest = REQUEST_EXPORT_JSON;
        createExportDocument("case-" + currentCase.optString("id") + ".json", pendingMime, pendingRequest);
    }

    private void chooseExportHtml() {
        pendingExport = makeHtmlReport(false);
        pendingMime = "text/html";
        pendingRequest = REQUEST_EXPORT_HTML;
        createExportDocument("case-report.html", pendingMime, pendingRequest);
    }

    private void createExportDocument(String name, String mime, int request) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mime);
        intent.putExtra(Intent.EXTRA_TITLE, name);
        startActivityForResult(intent, request);
    }

    private void importCase(Uri uri) {
        try {
            InputStream input = context.getContentResolver().openInputStream(uri);
            if (input == null) throw new IllegalStateException("Missing input stream");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                if (bytes.size() + read > MAX_IMPORT_BYTES) throw new IllegalArgumentException("File too large");
                bytes.write(buffer, 0, read);
            }
            input.close();
            JSONObject imported = CryptogramCaseStore.parseImport(new String(bytes.toByteArray(), "UTF-8"));
            CryptogramCaseStore.save(context, currentAccount, imported);
            currentCase = imported;
            render();
        } catch (Exception e) {
            toast(R.string.CaseImportError);
        }
    }

    private void writeExport(Uri uri) {
        try {
            OutputStream output = context.getContentResolver().openOutputStream(uri, "w");
            if (output == null) throw new IllegalStateException("Missing output stream");
            output.write(pendingExport.getBytes("UTF-8"));
            output.flush();
            output.close();
            pendingExport = null;
            toast(R.string.CaseDone);
        } catch (Exception e) {
            toast(R.string.CaseWriteError);
        }
    }

    private void printReport() {
        final WebView webView = new WebView(context);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
                if (printManager != null) {
                    printManager.print(currentCase.optString("title", "case-report"), view.createPrintDocumentAdapter(currentCase.optString("title", "case-report")), new PrintAttributes.Builder().build());
                }
            }
        });
        webView.loadDataWithBaseURL(null, makeHtmlReport(true), "text/html", "UTF-8", null);
    }

    private String makeHtmlReport(boolean print) {
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html><html><head><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"><style>");
        html.append("body{font:14px sans-serif;background:#17212b;color:#d8e0e8;margin:24px}h1,h2{color:#fff}section{border:1px solid #40505e;padding:12px;margin:12px 0;border-radius:6px}small{color:#9eacb8}.muted{color:#9eacb8}");
        html.append("</style></head><body><h1>").append(escapeHtml(currentCase.optString("title"))).append("</h1><p class=\"muted\">").append(escapeHtml(statusText(currentCase.optString("status")))).append("</p>");
        JSONArray nodes = currentCase.optJSONArray("nodes");
        html.append("<h2>Entities</h2>");
        if (nodes != null) {
            for (int i = 0; i < nodes.length(); i++) {
                JSONObject node = nodes.optJSONObject(i);
                if (node != null) html.append("<section><b>").append(escapeHtml(node.optString("label"))).append("</b><br><small>").append(escapeHtml(node.optString("type"))).append("</small><p>").append(escapeHtml(node.optString("notes"))).append("</p><p class=\"muted\">Sensitive values omitted</p></section>");
            }
        }
        JSONArray edges = currentCase.optJSONArray("edges");
        html.append("<h2>Relationships</h2>");
        if (edges != null) {
            for (int i = 0; i < edges.length(); i++) {
                JSONObject edge = edges.optJSONObject(i);
                if (edge != null) html.append("<section>").append(escapeHtml(edge.optString("type"))).append(" · confidence ").append(edge.optInt("confidence")).append("%</section>");
            }
        }
        appendReportArray(html, "Hypotheses", currentCase.optJSONArray("hypotheses"), "title", "status");
        appendReportArray(html, "Tasks", currentCase.optJSONArray("tasks"), "title", "done");
        appendReportArray(html, "Evidence", currentCase.optJSONArray("evidence"), "title", "source");
        html.append("</body></html>");
        return html.toString();
    }

    private void appendReportArray(StringBuilder html, String title, JSONArray items, String labelKey, String valueKey) {
        html.append("<h2>").append(title).append("</h2>");
        if (items == null) return;
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            if (item != null) html.append("<section>").append(escapeHtml(item.optString(labelKey))).append(" <small>").append(escapeHtml(String.valueOf(item.opt(valueKey)))).append("</small></section>");
        }
    }

    private String escapeHtml(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private void authenticate(Runnable success) {
        Activity activity = getParentActivity();
        if (!(activity instanceof FragmentActivity)) {
            requestDeviceCredential(success);
            return;
        }
        FragmentActivity fragmentActivity = (FragmentActivity) activity;
        BiometricManager manager = BiometricManager.from(fragmentActivity);
        int availability;
        if (Build.VERSION.SDK_INT >= 30) {
            availability = manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
        } else {
            availability = manager.canAuthenticate();
        }
        if (availability != BiometricManager.BIOMETRIC_SUCCESS) {
            requestDeviceCredential(success);
            return;
        }
        BiometricPrompt prompt = new BiometricPrompt(fragmentActivity, ContextCompat.getMainExecutor(fragmentActivity), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                pendingAuthentication = null;
                success.run();
            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                pendingAuthentication = null;
                if (errorCode == BiometricPrompt.ERROR_HW_NOT_PRESENT || errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS || errorCode == BiometricPrompt.ERROR_HW_UNAVAILABLE) {
                    requestDeviceCredential(success);
                } else {
                    toast(R.string.CaseAuthenticationCanceled);
                }
            }
        });
        BiometricPrompt.PromptInfo.Builder builder = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(LocaleController.getString(R.string.CaseUnlock))
                .setSubtitle(LocaleController.getString(R.string.CryptogramCases));
        if (Build.VERSION.SDK_INT >= 30) {
            builder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
        } else {
            builder.setNegativeButtonText(LocaleController.getString(R.string.Cancel));
        }
        prompt.authenticate(builder.build());
    }

    private void requestDeviceCredential(Runnable success) {
        Activity activity = getParentActivity();
        KeyguardManager keyguard = (KeyguardManager) activity.getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguard == null || !keyguard.isKeyguardSecure()) {
            toast(R.string.CaseSetScreenLock);
            return;
        }
        Intent intent = keyguard.createConfirmDeviceCredentialIntent(LocaleController.getString(R.string.CaseUnlock), LocaleController.getString(R.string.CryptogramCases));
        if (intent == null) {
            toast(R.string.CaseSetScreenLock);
            return;
        }
        pendingAuthentication = success;
        startActivityForResult(intent, REQUEST_UNLOCK);
    }

    private void recordEvent(JSONObject item, String text, String entityId) throws JSONException {
        JSONObject event = new JSONObject();
        event.put("at", System.currentTimeMillis());
        event.put("text", text);
        if (entityId != null) event.put("entityId", entityId);
        item.getJSONArray("events").put(event);
    }

    private void saveCurrent() {
        try {
            if (currentCase != null) {
                CryptogramCaseStore.save(context, currentAccount, currentCase);
                render();
            }
        } catch (Exception e) {
            toast(R.string.CaseWriteError);
        }
    }

    private String formatTime(long time) {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault()).format(new Date(time));
    }

    private double parseCoordinate(String text, double min, double max) {
        if (text == null || text.trim().isEmpty()) return Double.NaN;
        try {
            double value = Double.parseDouble(text.trim());
            return value >= min && value <= max ? value : Double.NaN;
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    private EditText input(String hint, boolean singleLine) {
        EditText edit = new EditText(context);
        edit.setHint(hint);
        edit.setTextColor(Color.WHITE);
        edit.setHintTextColor(Color.rgb(147, 160, 172));
        edit.setTextSize(16);
        edit.setPadding(AndroidUtilities.dp(12), AndroidUtilities.dp(8), AndroidUtilities.dp(12), AndroidUtilities.dp(8));
        if (singleLine) {
            edit.setSingleLine(true);
        } else {
            edit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            edit.setMinLines(2);
        }
        return edit;
    }

    private LinearLayout formLayout() {
        LinearLayout form = new LinearLayout(context);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(6), AndroidUtilities.dp(20), AndroidUtilities.dp(4));
        return form;
    }

    private void appendInput(LinearLayout form, EditText edit) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = AndroidUtilities.dp(6);
        form.addView(edit, params);
    }

    private View wrapInput(EditText input) {
        LinearLayout wrapper = formLayout();
        wrapper.addView(input, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        return wrapper;
    }

    private TextView addHeading(LinearLayout parent, String value) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextColor(Color.rgb(232, 238, 243));
        view.setTextSize(20);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setPadding(0, AndroidUtilities.dp(8), 0, AndroidUtilities.dp(8));
        parent.addView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        return view;
    }

    private TextView addParagraph(LinearLayout parent, String value) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextColor(Color.rgb(164, 178, 190));
        view.setTextSize(14);
        view.setPadding(0, AndroidUtilities.dp(8), 0, AndroidUtilities.dp(8));
        parent.addView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        return view;
    }

    private void addAction(LinearLayout parent, int label, Runnable action) {
        TextView button = new TextView(context);
        button.setText(LocaleController.getString(label));
        button.setTextColor(Color.rgb(223, 231, 237));
        button.setTextSize(15);
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setPadding(AndroidUtilities.dp(14), AndroidUtilities.dp(12), AndroidUtilities.dp(14), AndroidUtilities.dp(12));
        android.graphics.drawable.GradientDrawable background = new android.graphics.drawable.GradientDrawable();
        background.setColor(Color.rgb(39, 51, 63));
        background.setCornerRadius(AndroidUtilities.dp(5));
        button.setBackground(background);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = AndroidUtilities.dp(6);
        parent.addView(button, params);
        button.setOnClickListener(view -> action.run());
    }

    private void addCaseRow(String label, Runnable click, Runnable longClick) {
        TextView row = new TextView(context);
        row.setText(label);
        row.setTextColor(Color.rgb(222, 230, 237));
        row.setTextSize(15);
        row.setPadding(AndroidUtilities.dp(14), AndroidUtilities.dp(14), AndroidUtilities.dp(14), AndroidUtilities.dp(14));
        row.setBackgroundColor(Color.rgb(31, 41, 51));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = AndroidUtilities.dp(8);
        content.addView(row, params);
        row.setOnClickListener(view -> click.run());
        row.setOnLongClickListener(view -> {
            longClick.run();
            return true;
        });
    }

    private void toast(int text) {
        if (context != null) Toast.makeText(context, LocaleController.getString(text), Toast.LENGTH_SHORT).show();
    }

    private final class CaseGraphView extends View {
        private final JSONObject data;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private int selectedIndex = -1;
        private int movingIndex = -1;
        private float moveOffsetX;
        private float moveOffsetY;
        private SelectionListener selectionListener;
        private final float nodeWidth = AndroidUtilities.dp(142);
        private final float nodeHeight = AndroidUtilities.dp(58);

        CaseGraphView(Context ctx, JSONObject item) {
            super(ctx);
            data = item;
            setBackgroundColor(Color.rgb(17, 24, 31));
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        void setSelectionListener(SelectionListener listener) {
            selectionListener = listener;
        }

        int getSelectedIndex() {
            return selectedIndex;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(Color.rgb(17, 24, 31));
            paint.setColor(Color.rgb(33, 45, 56));
            paint.setStrokeWidth(AndroidUtilities.dp(1));
            float step = AndroidUtilities.dp(28);
            for (float x = 0; x < getWidth(); x += step) canvas.drawLine(x, 0, x, getHeight(), paint);
            for (float y = 0; y < getHeight(); y += step) canvas.drawLine(0, y, getWidth(), y, paint);
            JSONArray nodes = data.optJSONArray("nodes");
            JSONArray edges = data.optJSONArray("edges");
            if (nodes == null) return;
            if (edges != null) {
                paint.setStyle(Paint.Style.STROKE);
                for (int i = 0; i < edges.length(); i++) {
                    JSONObject edge = edges.optJSONObject(i);
                    if (edge == null) continue;
                    int a = indexOf(nodes, edge.optString("source"));
                    int b = indexOf(nodes, edge.optString("target"));
                    if (a < 0 || b < 0) continue;
                    float[] p1 = center(nodes.optJSONObject(a));
                    float[] p2 = center(nodes.optJSONObject(b));
                    int confidence = Math.max(0, Math.min(100, edge.optInt("confidence", 50)));
                    paint.setColor(Color.argb(90 + confidence * 150 / 100, 133, 161, 179));
                    paint.setStrokeWidth(AndroidUtilities.dp(1 + confidence / 45f));
                    canvas.drawLine(p1[0], p1[1], p2[0], p2[1], paint);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setTextSize(AndroidUtilities.dp(10));
                    paint.setColor(Color.rgb(170, 184, 195));
                    canvas.drawText(edge.optString("type", "related"), (p1[0] + p2[0]) / 2, (p1[1] + p2[1]) / 2 - AndroidUtilities.dp(4), paint);
                    paint.setStyle(Paint.Style.STROKE);
                }
            }
            paint.setStyle(Paint.Style.FILL);
            for (int i = 0; i < nodes.length(); i++) {
                JSONObject node = nodes.optJSONObject(i);
                if (node == null) continue;
                RectF rect = rect(node);
                paint.setColor(typeColor(node.optString("type")));
                canvas.drawRoundRect(rect, AndroidUtilities.dp(5), AndroidUtilities.dp(5), paint);
                if (i == selectedIndex) {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(AndroidUtilities.dp(2));
                    paint.setColor(Color.rgb(207, 220, 230));
                    canvas.drawRoundRect(rect, AndroidUtilities.dp(5), AndroidUtilities.dp(5), paint);
                    paint.setStyle(Paint.Style.FILL);
                }
                paint.setColor(Color.rgb(245, 247, 249));
                paint.setTextSize(AndroidUtilities.dp(13));
                canvas.drawText(shortText(node.optString("label"), 19), rect.left + AndroidUtilities.dp(8), rect.top + AndroidUtilities.dp(24), paint);
                paint.setColor(Color.rgb(190, 204, 215));
                paint.setTextSize(AndroidUtilities.dp(10));
                canvas.drawText(shortText(node.optString("type"), 20), rect.left + AndroidUtilities.dp(8), rect.top + AndroidUtilities.dp(43), paint);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                JSONArray nodes = data.optJSONArray("nodes");
                selectedIndex = -1;
                movingIndex = -1;
                if (nodes != null) {
                    for (int i = nodes.length() - 1; i >= 0; i--) {
                        JSONObject node = nodes.optJSONObject(i);
                        if (node != null && rect(node).contains(x, y)) {
                            selectedIndex = i;
                            movingIndex = i;
                            moveOffsetX = x - rect(node).centerX();
                            moveOffsetY = y - rect(node).centerY();
                            break;
                        }
                    }
                }
                if (selectionListener != null) selectionListener.onSelected(selectedIndex);
                invalidate();
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE && movingIndex >= 0) {
                JSONArray nodes = data.optJSONArray("nodes");
                JSONObject node = nodes == null ? null : nodes.optJSONObject(movingIndex);
                if (node != null && getWidth() > nodeWidth && getHeight() > nodeHeight) {
                    node.put("x", clamp((x - moveOffsetX) / getWidth(), nodeWidth / (2 * getWidth()), 1 - nodeWidth / (2 * getWidth())));
                    node.put("y", clamp((y - moveOffsetY) / getHeight(), nodeHeight / (2 * getHeight()), 1 - nodeHeight / (2 * getHeight())));
                    invalidate();
                }
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                movingIndex = -1;
                if (selectedIndex >= 0) saveCurrent();
                performClick();
                return true;
            }
            return true;
        }

        @Override
        public boolean performClick() {
            super.performClick();
            return true;
        }

        private RectF rect(JSONObject node) {
            float left = (float) node.optDouble("x", 0.5) * getWidth() - nodeWidth / 2;
            float top = (float) node.optDouble("y", 0.5) * getHeight() - nodeHeight / 2;
            return new RectF(left, top, left + nodeWidth, top + nodeHeight);
        }

        private float[] center(JSONObject node) {
            RectF rect = rect(node);
            return new float[] { rect.centerX(), rect.centerY() };
        }

        private int indexOf(JSONArray nodes, String id) {
            for (int i = 0; i < nodes.length(); i++) {
                JSONObject node = nodes.optJSONObject(i);
                if (node != null && id.equals(node.optString("id"))) return i;
            }
            return -1;
        }

        private int typeColor(String type) {
            if ("person".equals(type)) return Color.rgb(61, 83, 99);
            if ("chat".equals(type) || "channel".equals(type)) return Color.rgb(55, 79, 78);
            if ("domain".equals(type)) return Color.rgb(77, 74, 57);
            if ("file".equals(type)) return Color.rgb(76, 66, 81);
            if ("place".equals(type)) return Color.rgb(73, 70, 60);
            if ("organization".equals(type)) return Color.rgb(65, 78, 92);
            return Color.rgb(57, 68, 79);
        }

        private float clamp(float value, float min, float max) {
            return Math.max(min, Math.min(max, value));
        }

        private String shortText(String value, int max) {
            if (value == null) return "";
            return value.length() > max ? value.substring(0, max - 1) + "…" : value;
        }
    }

    private interface SelectionListener {
        void onSelected(int index);
    }
}