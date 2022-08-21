package com.osfans.trime.data.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import com.osfans.trime.ime.core.Trime;
import com.osfans.trime.ime.symbol.SimpleKeyBean;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

public class DbDao {

  private static DbHelper helper;
  private static String DB_NAME;
  public static String CLIPBOARD = "clipboard.db";
  public static String COLLECTION = "collection.db";
  public static String DRAFT = "draft.db";

  public DbDao(String dbName) {
    DB_NAME = dbName;
    helper = new DbHelper(Trime.getService(), DB_NAME);
  }

  /** 插入新记录 * */
  public void insert(@NonNull DbBean bean) {
    SQLiteDatabase db = helper.getWritableDatabase();
    db.execSQL(
        "insert into t_data(text,html,type,time) values(?,?,?,?)",
        new Object[] {bean.getText(), bean.getHtml(), bean.getType(), bean.getTime()});
    db.close();
  }

  /** 删除文字相同的记录，插入新记录 * */
  public void add(@NonNull DbBean bean) {
    SQLiteDatabase db = helper.getWritableDatabase();
    db.delete("t_data", "text=?", new String[] {bean.getText()});
    db.execSQL(
        "insert into t_data(text,html,type,time) values(?,?,?,?)",
        new Object[] {bean.getText(), bean.getHtml(), bean.getType(), bean.getTime()});
    db.close();
  }

  /** 删除记录 * */
  public void delete(@NonNull String str) {
    SQLiteDatabase db = helper.getWritableDatabase();
    db.delete("t_data", "text=?", new String[] {str});
    db.close();
  }

  public void delete(@NonNull List<SimpleKeyBean> list) {
    SQLiteDatabase db = helper.getWritableDatabase();
    for (SimpleKeyBean bean : list) db.delete("t_data", "text=?", new String[] {bean.getText()});
    db.close();
  }

  // 取回指定数量的剪贴板； 收藏夹不限定数量，size = -1
  public List<SimpleKeyBean> getAllSimpleBean(int size) {

    List<SimpleKeyBean> list = new ArrayList<>();
    if (size == 0) return list;

    String sql = "select text , html , type , time from t_data ORDER BY time DESC";
    if (size > 0) sql = sql + " limit 0," + size;

    SQLiteDatabase db = helper.getWritableDatabase();
    Cursor cursor = db.rawQuery(sql, null);
    if (cursor != null) {
      while (cursor.moveToNext()) {
        DbBean v = new DbBean(cursor.getString(0));
        list.add(v);
      }
      cursor.close();
    }
    db.close();
    Timber.d("getAllSimpleBean() size=%s limit=%s", list.size(), size);
    return list;
  }

  /**
   * 取回数据（草稿箱）
   *
   * @param info 在顶部显示包含info的数据
   * @param pinned 在顶部显示多少条包含info的数据
   * @param size 取回多少条数据
   * @return 全部数据
   */
  public List<SimpleKeyBean> getAllSimpleBean(String info, int pinned, int size) {

    List<SimpleKeyBean> list = new ArrayList<>();
    List<SimpleKeyBean> tmp = new ArrayList<>();
    if (pinned < 0) pinned = Integer.MAX_VALUE;

    String sql = "select text , html , type , time from t_data ORDER BY time DESC";
    if (size > 0) sql = sql + " limit 0," + size;

    SQLiteDatabase db = helper.getWritableDatabase();

    Cursor cursor = db.rawQuery(sql, null);
    if (cursor == null) {
    } else if (info == null || info.isEmpty()) {

      while (cursor.moveToNext()) {
        DbBean v = new DbBean(cursor.getString(0));
        list.add(v);
      }
    } else {
      while (cursor.moveToNext()) {
        DbBean v = new DbBean(cursor.getString(0));
        if (list.size() < pinned) {
          String packageName = cursor.getString(1);
          if (packageName != null && packageName.equals(info)) list.add(v);
          else tmp.add(v);
        } else if (list.size() == pinned) {
          list.addAll(tmp);
          tmp.clear();
        } else {
          list.add(v);
        }
      }
      cursor.close();

      if (!tmp.isEmpty()) {
        if (list.size() < pinned) {
          cursor =
              db.rawQuery(
                  "select text , html , type , time from t_data WHERE html=? "
                      + "ORDER BY time DESC  limit "
                      + list.size()
                      + ","
                      + size,
                  new String[] {info});
          if (cursor != null) {
            while (cursor.moveToNext()) {
              DbBean v = new DbBean(cursor.getString(0));
              list.add(v);
            }
            cursor.close();
          }
        }
        list.addAll(tmp);
      }
    }
    db.close();
    Timber.d("getAllSimpleBean() size=%s", list.size());
    return list;
  }
}
