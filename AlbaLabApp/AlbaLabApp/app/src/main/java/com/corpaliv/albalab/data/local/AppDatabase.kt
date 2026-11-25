package com.corpaliv.albalab.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [UsuarioEntity::class, RegistroInteresEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun registroInteresDao(): RegistroInteresDao

    companion object {
        /** v1 -> v2: guarda el código y el estado que entrega ms-vinculacion. */
        val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE registros_interes ADD COLUMN remoteId TEXT")
                db.execSQL("ALTER TABLE registros_interes ADD COLUMN estadoRemoto TEXT")
            }
        }

        /** v2 -> v3: referencia al programa/producto de origen del registro. */
        val MIGRACION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE registros_interes ADD COLUMN programaId TEXT")
                db.execSQL("ALTER TABLE registros_interes ADD COLUMN productoId TEXT")
            }
        }

        val MIGRACION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE registros_interes ADD COLUMN claveIdempotencia TEXT")
                db.execSQL("UPDATE registros_interes SET claveIdempotencia = lower(hex(randomblob(16)))")
            }
        }

        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "albalab.db"
                ).addMigrations(MIGRACION_1_2, MIGRACION_2_3, MIGRACION_3_4).build().also { instancia = it }
            }
    }
}
