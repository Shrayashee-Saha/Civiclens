package com.example.civiclens.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.civiclens.data.CivicDatabase;

/**
 * Read-only provider for saved observations.
 *   content://com.example.civiclens.provider/observations            all observations
 *   content://com.example.civiclens.provider/observations/{projectId} one project
 */
public class CivicContentProvider extends ContentProvider {

    public static final String AUTHORITY = "com.example.civiclens.provider";
    public static final Uri OBSERVATIONS = Uri.parse("content://" + AUTHORITY + "/observations");

    private static final int ALL_OBSERVATIONS = 1;
    private static final int OBSERVATIONS_FOR_PROJECT = 2;

    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        MATCHER.addURI(AUTHORITY, "observations", ALL_OBSERVATIONS);
        MATCHER.addURI(AUTHORITY, "observations/*", OBSERVATIONS_FOR_PROJECT);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    /** The projection argument is ignored: the columns are fixed. */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        int code = MATCHER.match(uri);
        String sql = "SELECT id, projectId, text, createdAt, status FROM project_observations";
        Object[] args = new Object[0];

        if (code == OBSERVATIONS_FOR_PROJECT) {
            sql += " WHERE projectId = ?";
            args = new Object[]{uri.getLastPathSegment()};
        } else if (code != ALL_OBSERVATIONS) {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        sql += " ORDER BY createdAt DESC";

        SupportSQLiteDatabase db = CivicDatabase.get(getContext()).getOpenHelper().getReadableDatabase();
        Cursor cursor = db.query(new SimpleSQLiteQuery(sql, args));
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return "vnd.android.cursor.dir/vnd.com.example.civiclens.observation";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Read-only provider");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Read-only provider");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Read-only provider");
    }
}
