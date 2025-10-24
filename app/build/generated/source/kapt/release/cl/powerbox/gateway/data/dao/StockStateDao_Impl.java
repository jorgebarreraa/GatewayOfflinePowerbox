package cl.powerbox.gateway.data.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import cl.powerbox.gateway.data.entity.StockState;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class StockStateDao_Impl implements StockStateDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<StockState> __insertionAdapterOfStockState;

  private final SharedSQLiteStatement __preparedStmtOfUpdateServerQty;

  private final SharedSQLiteStatement __preparedStmtOfApplyDelta;

  public StockStateDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfStockState = new EntityInsertionAdapter<StockState>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `stock_state` (`productId`,`serverQty`,`localDelta`,`updatedAt`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StockState entity) {
        if (entity.getProductId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getProductId());
        }
        statement.bindLong(2, entity.getServerQty());
        statement.bindLong(3, entity.getLocalDelta());
        statement.bindLong(4, entity.getUpdatedAt());
      }
    };
    this.__preparedStmtOfUpdateServerQty = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE stock_state SET serverQty = ?, updatedAt = ? WHERE productId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfApplyDelta = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE stock_state SET localDelta = localDelta + ?, updatedAt = ? WHERE productId = ?";
        return _query;
      }
    };
  }

  @Override
  public void upsert(final StockState state) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfStockState.insert(state);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void ensureAndDelta(final String pid, final int delta, final long now) {
    __db.beginTransaction();
    try {
      StockStateDao.DefaultImpls.ensureAndDelta(StockStateDao_Impl.this, pid, delta, now);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void ensureAndSetServer(final String pid, final int serverQty, final long now) {
    __db.beginTransaction();
    try {
      StockStateDao.DefaultImpls.ensureAndSetServer(StockStateDao_Impl.this, pid, serverQty, now);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void updateServerQty(final String pid, final int serverQty, final long ts) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateServerQty.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, serverQty);
    _argIndex = 2;
    _stmt.bindLong(_argIndex, ts);
    _argIndex = 3;
    if (pid == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, pid);
    }
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfUpdateServerQty.release(_stmt);
    }
  }

  @Override
  public void applyDelta(final String pid, final int delta, final long ts) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfApplyDelta.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, delta);
    _argIndex = 2;
    _stmt.bindLong(_argIndex, ts);
    _argIndex = 3;
    if (pid == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, pid);
    }
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfApplyDelta.release(_stmt);
    }
  }

  @Override
  public StockState byId(final String pid) {
    final String _sql = "SELECT * FROM stock_state WHERE productId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (pid == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, pid);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
      final int _cursorIndexOfServerQty = CursorUtil.getColumnIndexOrThrow(_cursor, "serverQty");
      final int _cursorIndexOfLocalDelta = CursorUtil.getColumnIndexOrThrow(_cursor, "localDelta");
      final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
      final StockState _result;
      if (_cursor.moveToFirst()) {
        final String _tmpProductId;
        if (_cursor.isNull(_cursorIndexOfProductId)) {
          _tmpProductId = null;
        } else {
          _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
        }
        final int _tmpServerQty;
        _tmpServerQty = _cursor.getInt(_cursorIndexOfServerQty);
        final int _tmpLocalDelta;
        _tmpLocalDelta = _cursor.getInt(_cursorIndexOfLocalDelta);
        final long _tmpUpdatedAt;
        _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
        _result = new StockState(_tmpProductId,_tmpServerQty,_tmpLocalDelta,_tmpUpdatedAt);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<StockState> all() {
    final String _sql = "SELECT * FROM stock_state";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
      final int _cursorIndexOfServerQty = CursorUtil.getColumnIndexOrThrow(_cursor, "serverQty");
      final int _cursorIndexOfLocalDelta = CursorUtil.getColumnIndexOrThrow(_cursor, "localDelta");
      final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
      final List<StockState> _result = new ArrayList<StockState>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final StockState _item;
        final String _tmpProductId;
        if (_cursor.isNull(_cursorIndexOfProductId)) {
          _tmpProductId = null;
        } else {
          _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
        }
        final int _tmpServerQty;
        _tmpServerQty = _cursor.getInt(_cursorIndexOfServerQty);
        final int _tmpLocalDelta;
        _tmpLocalDelta = _cursor.getInt(_cursorIndexOfLocalDelta);
        final long _tmpUpdatedAt;
        _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
        _item = new StockState(_tmpProductId,_tmpServerQty,_tmpLocalDelta,_tmpUpdatedAt);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
