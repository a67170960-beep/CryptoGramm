package org.telegram.messenger;

import android.content.Context;
import android.security.KeyPairGeneratorSpec;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

/** Local per-account investigation case store. All case JSON is AES-GCM encrypted before disk I/O. */
public final class CryptogramCaseStore {
    private static final String FORMAT = "cryptogram-case";
    private static final int VERSION = 1;
    private static final int MAX_CASE_BYTES = 4 * 1024 * 1024;
    private static final int MAX_WRAPPED_KEY_BYTES = 4096;
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final byte[] MAGIC = new byte[] { 'C', 'G', 'C', 'A', 'S', 'E', 1 };

    private static final Pattern EMAIL = Pattern.compile("(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b");
    private static final Pattern URL = Pattern.compile("(?i)\\b(?:https?://|www\\.)[^\\s<>\\\"']{3,2048}");
    private static final Pattern HANDLE = Pattern.compile("(?<![A-Za-z0-9._%+-])@[A-Za-z0-9_]{4,32}\\b");
    private static final Pattern PHONE = Pattern.compile("(?<![A-Za-z0-9])\\+?[0-9][0-9() .-]{6,18}[0-9](?![A-Za-z0-9])");
    private static final Pattern ETHEREUM = Pattern.compile("(?i)\\b0x[a-f0-9]{40}\\b");
    private static final Pattern BITCOIN = Pattern.compile("\\b(?:bc1[ac-hj-np-z02-9]{11,71}|[13][a-km-zA-HJ-NP-Z1-9]{25,34})\\b");

    private CryptogramCaseStore() {
    }

    public static JSONObject newCase(String title) throws JSONException {
        String cleanTitle = title == null ? "" : title.trim();
        if (cleanTitle.isEmpty() || cleanTitle.length() > 120) {
            throw new IllegalArgumentException("Case title must contain 1 to 120 characters.");
        }
        long now = System.currentTimeMillis();
        JSONObject result = new JSONObject();
        result.put("format", FORMAT);
        result.put("version", VERSION);
        result.put("id", UUID.randomUUID().toString());
        result.put("title", cleanTitle);
        result.put("status", "open");
        result.put("createdAt", now);
        result.put("updatedAt", now);
        result.put("nodes", new JSONArray());
        result.put("edges", new JSONArray());
        result.put("hypotheses", new JSONArray());
        result.put("tasks", new JSONArray());
        result.put("evidence", new JSONArray());
        result.put("events", new JSONArray());
        return result;
    }

    public static synchronized ArrayList<JSONObject> list(Context context, int account) throws Exception {
        File dir = accountDirectory(context, account);
        ArrayList<JSONObject> cases = new ArrayList<>();
        File[] files = dir.listFiles((file, name) -> name.endsWith(".case"));
        if (files == null) {
            return cases;
        }
        for (File file : files) {
            JSONObject item = readFile(context, account, file);
            if (item != null) {
                cases.add(item);
            }
        }
        Collections.sort(cases, (left, right) -> Long.compare(right.optLong("updatedAt"), left.optLong("updatedAt")));
        return cases;
    }

    public static synchronized void save(Context context, int account, JSONObject item) throws Exception {
        if (context == null || item == null || account < 0) {
            throw new IllegalArgumentException("Invalid case context.");
        }
        validateCase(item);
        String id = safeId(item.getString("id"));
        item.put("updatedAt", System.currentTimeMillis());
        byte[] clear = item.toString().getBytes(UTF8);
        if (clear.length > MAX_CASE_BYTES) {
            throw new IllegalArgumentException("Case is too large to save.");
        }
        byte[] encrypted = encrypt(context, account, clear);
        File dir = accountDirectory(context, account);
        File target = new File(dir, id + ".case");
        File temp = new File(dir, id + ".tmp");
        FileOutputStream stream = new FileOutputStream(temp);
        try {
            stream.write(encrypted);
            stream.getFD().sync();
        } finally {
            stream.close();
        }
        if (target.exists() && !target.delete()) {
            temp.delete();
            throw new IllegalStateException("Could not replace case file.");
        }
        if (!temp.renameTo(target)) {
            temp.delete();
            throw new IllegalStateException("Could not commit case file.");
        }
    }

    public static synchronized void delete(Context context, int account, String id) throws Exception {
        File target = new File(accountDirectory(context, account), safeId(id) + ".case");
        if (target.exists() && !target.delete()) {
            throw new IllegalStateException("Could not delete case file.");
        }
    }

    public static JSONObject parseImport(String json) throws JSONException {
        if (json == null || json.length() > MAX_CASE_BYTES) {
            throw new IllegalArgumentException("Import file is empty or too large.");
        }
        JSONObject item = new JSONObject(json);
        validateCase(item);
        item.put("id", UUID.randomUUID().toString());
        item.put("createdAt", System.currentTimeMillis());
        item.put("updatedAt", System.currentTimeMillis());
        return item;
    }

    public static String exportJson(JSONObject item) throws JSONException {
        validateCase(item);
        JSONObject exported = new JSONObject(item.toString());
        exported.put("format", FORMAT);
        exported.put("version", VERSION);
        return exported.toString(2);
    }

    /** Extracts candidates from text explicitly selected by the user. This method never performs network requests. */
    public static JSONArray extractSelectedText(String text) throws JSONException {
        JSONArray found = new JSONArray();
        if (text == null || text.length() > 200000) {
            return found;
        }
        Set<String> seen = new HashSet<>();
        addMatches(found, seen, EMAIL, text, "email");
        addMatches(found, seen, URL, text, "url");
        addMatches(found, seen, HANDLE, text, "username");
        addMatches(found, seen, PHONE, text, "phone");
        addMatches(found, seen, ETHEREUM, text, "crypto_address");
        addMatches(found, seen, BITCOIN, text, "crypto_address");
        return found;
    }

    private static void addMatches(JSONArray target, Set<String> seen, Pattern pattern, String text, String type) throws JSONException {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find() && target.length() < 500) {
            String value = matcher.group().trim();
            String key = type + ":" + value.toLowerCase(java.util.Locale.US);
            if (seen.add(key)) {
                JSONObject item = new JSONObject();
                item.put("type", type);
                item.put("value", value);
                item.put("source", "selected_text");
                target.put(item);
            }
        }
    }

    private static JSONObject readFile(Context context, int account, File file) throws Exception {
        if (file.length() < MAGIC.length + 24 || file.length() > MAX_CASE_BYTES + 8192) {
            throw new IllegalStateException("Invalid encrypted case file size.");
        }
        byte[] data = new byte[(int) file.length()];
        FileInputStream input = new FileInputStream(file);
        try {
            int offset = 0;
            while (offset < data.length) {
                int read = input.read(data, offset, data.length - offset);
                if (read < 0) {
                    throw new IllegalStateException("Truncated encrypted case file.");
                }
                offset += read;
            }
        } finally {
            input.close();
        }
        byte[] clear = decrypt(context, account, data);
        JSONObject item = new JSONObject(new String(clear, UTF8));
        validateCase(item);
        return item;
    }

    private static void validateCase(JSONObject item) throws JSONException {
        if (!FORMAT.equals(item.optString("format")) || item.optInt("version", -1) != VERSION) {
            throw new IllegalArgumentException("Unsupported case format or version.");
        }
        safeId(item.getString("id"));
        String title = item.optString("title", "").trim();
        if (title.isEmpty() || title.length() > 120) {
            throw new IllegalArgumentException("Invalid case title.");
        }
        if (!(item.opt("nodes") instanceof JSONArray) || !(item.opt("edges") instanceof JSONArray)
                || !(item.opt("tasks") instanceof JSONArray) || !(item.opt("hypotheses") instanceof JSONArray)
                || !(item.opt("evidence") instanceof JSONArray) || !(item.opt("events") instanceof JSONArray)) {
            throw new IllegalArgumentException("Case data is incomplete.");
        }
        if (item.getJSONArray("nodes").length() > 5000 || item.getJSONArray("edges").length() > 20000) {
            throw new IllegalArgumentException("Case contains too many graph items.");
        }
    }

    private static File accountDirectory(Context context, int account) {
        if (account < 0) {
            throw new IllegalArgumentException("Invalid account.");
        }
        File dir = new File(new File(context.getFilesDir(), "cryptogram_cases"), "account_" + account);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Could not create private case storage.");
        }
        return dir;
    }

    private static String safeId(String id) {
        if (id == null || !id.matches("[A-Za-z0-9_-]{1,64}")) {
            throw new IllegalArgumentException("Invalid case identifier.");
        }
        return id;
    }

    private static byte[] encrypt(Context context, int account, byte[] clear) throws Exception {
        KeyPair pair = getOrCreateKeyPair(context, account);
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(256);
        SecretKey key = generator.generateKey();
        Cipher wrap = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        wrap.init(Cipher.ENCRYPT_MODE, pair.getPublic());
        byte[] wrappedKey = wrap.doFinal(key.getEncoded());
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] iv = cipher.getIV();
        byte[] encrypted = cipher.doFinal(clear);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(bytes);
        output.write(MAGIC);
        output.writeInt(wrappedKey.length);
        output.write(wrappedKey);
        output.writeInt(iv.length);
        output.write(iv);
        output.writeInt(encrypted.length);
        output.write(encrypted);
        output.flush();
        return bytes.toByteArray();
    }

    private static byte[] decrypt(Context context, int account, byte[] payload) throws Exception {
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(payload));
        byte[] magic = new byte[MAGIC.length];
        input.readFully(magic);
        for (int i = 0; i < MAGIC.length; i++) {
            if (magic[i] != MAGIC[i]) {
                throw new IllegalStateException("Unknown case storage format.");
            }
        }
        int keyLength = input.readInt();
        if (keyLength < 1 || keyLength > MAX_WRAPPED_KEY_BYTES) {
            throw new IllegalStateException("Invalid case encryption header.");
        }
        byte[] wrappedKey = new byte[keyLength];
        input.readFully(wrappedKey);
        int ivLength = input.readInt();
        if (ivLength != 12) {
            throw new IllegalStateException("Invalid case encryption nonce.");
        }
        byte[] iv = new byte[ivLength];
        input.readFully(iv);
        int cipherLength = input.readInt();
        if (cipherLength < 16 || cipherLength > MAX_CASE_BYTES + 32 || cipherLength != input.available()) {
            throw new IllegalStateException("Invalid encrypted case payload.");
        }
        byte[] encrypted = new byte[cipherLength];
        input.readFully(encrypted);
        KeyPair pair = getOrCreateKeyPair(context, account);
        Cipher unwrap = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        unwrap.init(Cipher.DECRYPT_MODE, pair.getPrivate());
        byte[] keyBytes = unwrap.doFinal(wrappedKey);
        SecretKey key = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
        return cipher.doFinal(encrypted);
    }

    private static KeyPair getOrCreateKeyPair(Context context, int account) throws Exception {
        String alias = "cryptogram_case_storage_a" + account;
        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        store.load(null);
        if (!store.containsAlias(alias)) {
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 30);
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
            KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context.getApplicationContext())
                    .setAlias(alias)
                    .setSubject(new X500Principal("CN=Cryptogram Case Storage"))
                    .setSerialNumber(BigInteger.valueOf(account + 1L))
                    .setStartDate(start.getTime())
                    .setEndDate(end.getTime())
                    .build();
            generator.initialize(spec);
            generator.generateKeyPair();
            store.load(null);
        }
        Certificate certificate = store.getCertificate(alias);
        PrivateKey privateKey = (PrivateKey) store.getKey(alias, null);
        if (certificate == null || privateKey == null) {
            throw new IllegalStateException("Case encryption key is unavailable.");
        }
        return new KeyPair(certificate.getPublicKey(), privateKey);
    }
}