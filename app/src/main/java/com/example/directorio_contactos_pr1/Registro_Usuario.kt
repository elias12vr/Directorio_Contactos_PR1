package com.example.directorio_contactos_pr1

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Registro_Usuario : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etUsuario: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnRegistrar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_usuario)

        etNombre = findViewById(R.id.etNombre)
        etUsuario = findViewById(R.id.etUsuario)
        etContrasena = findViewById(R.id.etContrasena)
        btnRegistrar = findViewById(R.id.btnRegistrar)

        btnRegistrar.setOnClickListener {
            val nombre = etNombre.text.toString()
            val usuario = etUsuario.text.toString()
            val contrasena = etContrasena.text.toString()

            if (nombre.isNotEmpty() && usuario.isNotEmpty() && contrasena.isNotEmpty()) {
                registrarUsuario(nombre, usuario, contrasena)
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registrarUsuario(nombre: String, usuario: String, contrasena: String) {
        val admin = SQLITE_MANAGER(this, "usuarios", 1)
        val db = admin.writableDatabase

        val values = ContentValues().apply {
            put("nombre", nombre)
            put("usuario", usuario)
            put("contrasena", contrasena)
        }

        val resultado = db.insert("usuarios", null, values)
        db.close()

        if (resultado != -1L) {
            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()

            // Redirigir a la pantalla principal después de un registro exitoso
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("USER_ID", resultado.toInt()) // Pasar el ID del usuario registrado
            startActivity(intent)
            finish() // Cierra la actividad de registro
        } else {
            Toast.makeText(this, "Error al registrar usuario", Toast.LENGTH_SHORT).show()
        }
    }
}
