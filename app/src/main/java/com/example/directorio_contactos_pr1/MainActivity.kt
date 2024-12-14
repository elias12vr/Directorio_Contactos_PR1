package com.example.directorio_contactos_pr1
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
class MainActivity : AppCompatActivity() {

    private lateinit var etUsuario: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnIngresar: Button
    private lateinit var btnRegistrar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etUsuario = findViewById(R.id.etUsuario)
        etContrasena = findViewById(R.id.etContrasena)
        btnIngresar = findViewById(R.id.btnIngresar)
        btnRegistrar = findViewById(R.id.btnRegistrar)

        // Botón para iniciar sesión
        btnIngresar.setOnClickListener {
            val usuario = etUsuario.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()

            if (usuario.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val admin = SQLITE_MANAGER(this, "usuarios", 1) // Asegúrate de que la versión es correcta

            try {
                if (validarUsuario(admin, usuario, contrasena)) {
                    val userId = obtenerIdUsuario(admin, usuario)
                    if (userId != -1) {
                        // Guardar el ID del usuario en SharedPreferences
                        val sharedPref = getSharedPreferences("USER_PREFS", MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putInt("usuarioActivoId", userId)
                            putString("usuarioActivo", usuario) // También podemos guardar el nombre del usuario si lo necesitas
                            apply()
                        }

                        val intent = Intent(this, Principal::class.java)
                        intent.putExtra("USER_ID", userId)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Error al obtener el ID del usuario", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error al iniciar sesión: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }


        // Botón para registrar usuario
        btnRegistrar.setOnClickListener {
            val intent = Intent(this, Registro_Usuario::class.java)
            startActivity(intent)
        }
    }

    private fun validarUsuario(admin: SQLITE_MANAGER, usuario: String, contrasena: String): Boolean {
        val db = admin.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM usuarios WHERE usuario = ? AND contrasena = ?", arrayOf(usuario, contrasena))
        val existe = cursor.count > 0
        cursor.close()
        db.close()
        return existe
    }

    private fun obtenerIdUsuario(admin: SQLITE_MANAGER, usuario: String): Int {
        val db = admin.readableDatabase
        val cursor = db.rawQuery("SELECT id FROM usuarios WHERE usuario = ?", arrayOf(usuario))
        var userId = -1
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return userId
    }
}
