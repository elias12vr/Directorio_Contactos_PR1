package com.example.directorio_contactos_pr1
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
class SQLITE_MANAGER(context: Context, bdName: String, version: Int = 1) : SQLiteOpenHelper(context, bdName, null, version) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "CREATE TABLE usuarios (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nombre TEXT, " +
                    "usuario TEXT UNIQUE, " +
                    "contrasena TEXT)"
        )
        db?.execSQL(
            "CREATE TABLE informacion_contactos (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "usuario_id INTEGER, " +
                    "nombre TEXT, " +
                    "telefono TEXT, " +
                    "email TEXT, " +
                    "imagen_uri BLOB, " +
                    "FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE)"
        )
    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS informacion_contactos")
        db?.execSQL("DROP TABLE IF EXISTS usuarios")
        onCreate(db)
    }
}
