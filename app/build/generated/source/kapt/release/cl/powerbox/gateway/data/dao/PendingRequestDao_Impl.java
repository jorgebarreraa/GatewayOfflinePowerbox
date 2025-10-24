package cl.powerbox.gateway.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import cl.powerbox.gateway.data.entity.PendingRequest;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PendingRequestDao_Impl implements PendingRequestDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PendingRequest> __insertionAdapterOfPendingRequest;

  private final SharedSQLiteStatement __preparedStmtOfMarkDone;

  private final SharedSQLiteStatement __preparedStmtOfMarkAttempt;

  public PendingRequestDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPendingRequest = new EntityInsertionAdapter<PendingRequest>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `pending_request` (`id`,`path`,`method`,`headersJson`,`body`,`clientOrderId`,`createdAt`,`done`,`attempts`,`lastError`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PendingRequest entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getPath() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getPath());
        }
        if (entity.getMethod() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getMethod());
        }
        if (entity.getHeadersJson() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getHeadersJson());
        }
        if (entity.getBody() == null) {
          statement.bindNull(5);
        } else {
          statement.bindBlob(5, entity.getBody());
        }
        if (entity.getClientOrderId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getClientOrderId());
        }
        statement.bindLong(7, entity.getCreatedAt());
        final int _tmp = entity.getDone() ? 1 : 0;
        statement.bindLong(8, _tmp);
        statement.bindLong(9, entity.getAttempts());
        if (entity.getLastError() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getLastError());
        }
      }
    };
    this.__preparedStmtOfMarkDone = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE pending_request SET done = 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkAttempt = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE pending_request SET attempts = attempts + 1, lastError = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final PendingRequest p, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPendingRequest.insert(p);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object markDone(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkDone.acquire();
        int _argIndex = 1;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, id);
        }
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarkDone.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markAttempt(final String id, final String err,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkAttempt.acquire();
        int _argIndex = 1;
        if (err == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, err);
        }
        _argIndex = 2;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, id);
        }
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarkAttempt.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object nextBatch(final int limit,
      final Continuation<? super List<PendingRequest>> $completion) {
    final String _sql = "SELECT * FROM pending_request WHERE done = 0 ORDER BY createdAt ASC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PendingRequest>>() {
      @Override
      @NonNull
      public List<PendingRequest> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPath = CursorUtil.getColumnIndexOrThrow(_cursor, "path");
          final int _cursorIndexOfMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "method");
          final int _cursorIndexOfHeadersJson = CursorUtil.getColumnIndexOrThrow(_cursor, "headersJson");
          final int _cursorIndexOfBody = CursorUtil.getColumnIndexOrThrow(_cursor, "body");
          final int _cursorIndexOfClientOrderId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientOrderId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfDone = CursorUtil.getColumnIndexOrThrow(_cursor, "done");
          final int _cursorIndexOfAttempts = CursorUtil.getColumnIndexOrThrow(_cursor, "attempts");
          final int _cursorIndexOfLastError = CursorUtil.getColumnIndexOrThrow(_cursor, "lastError");
          final List<PendingRequest> _result = new ArrayList<PendingRequest>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PendingRequest _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpPath;
            if (_cursor.isNull(_cursorIndexOfPath)) {
              _tmpPath = null;
            } else {
              _tmpPath = _cursor.getString(_cursorIndexOfPath);
            }
            final String _tmpMethod;
            if (_cursor.isNull(_cursorIndexOfMethod)) {
              _tmpMethod = null;
            } else {
              _tmpMethod = _cursor.getString(_cursorIndexOfMethod);
            }
            final String _tmpHeadersJson;
            if (_cursor.isNull(_cursorIndexOfHeadersJson)) {
              _tmpHeadersJson = null;
            } else {
              _tmpHeadersJson = _cursor.getString(_cursorIndexOfHeadersJson);
            }
            final byte[] _tmpBody;
            if (_cursor.isNull(_cursorIndexOfBody)) {
              _tmpBody = null;
            } else {
              _tmpBody = _cursor.getBlob(_cursorIndexOfBody);
            }
            final String _tmpClientOrderId;
            if (_cursor.isNull(_cursorIndexOfClientOrderId)) {
              _tmpClientOrderId = null;
            } else {
              _tmpClientOrderId = _cursor.getString(_cursorIndexOfClientOrderId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final boolean _tmpDone;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfDone);
            _tmpDone = _tmp != 0;
            final int _tmpAttempts;
            _tmpAttempts = _cursor.getInt(_cursorIndexOfAttempts);
            final String _tmpLastError;
            if (_cursor.isNull(_cursorIndexOfLastError)) {
              _tmpLastError = null;
            } else {
              _tmpLastError = _cursor.getString(_cursorIndexOfLastError);
            }
            _item = new PendingRequest(_tmpId,_tmpPath,_tmpMethod,_tmpHeadersJson,_tmpBody,_tmpClientOrderId,_tmpCreatedAt,_tmpDone,_tmpAttempts,_tmpLastError);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
