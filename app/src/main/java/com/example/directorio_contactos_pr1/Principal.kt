package com.example.directorio_contactos_pr1

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation

class Principal : AppCompatActivity() {

    private lateinit var ivUserImage: ImageButton
    private lateinit var btnRegistro: ImageButton
    private lateinit var btnEditar: ImageButton
    private lateinit var btnEliminar: ImageButton
    private lateinit var btnGuardarContacto: ImageButton
    private lateinit var etNombre: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etEmail: EditText

    private var imageUri: Uri? = null // URI para la imagen seleccionada
    private var selectedContactId: Int? = null // ID del contacto seleccionado

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)

        ivUserImage = findViewById(R.id.ivUserImage)
        btnRegistro = findViewById(R.id.btnRegistro)
        btnEditar = findViewById(R.id.btnEditar)
        btnEliminar = findViewById(R.id.btnEliminar)
        btnGuardarContacto = findViewById(R.id.btnGuardarContacto)
        etNombre = findViewById(R.id.etNombre)
        etTelefono = findViewById(R.id.etTelefono)
        etEmail = findViewById(R.id.etEmail)

        btnEditar.visibility = Button.GONE // Ocultar botones Editar y Eliminar al inicio
        btnEliminar.visibility = Button.GONE

        ivUserImage.setOnClickListener {
            openGallery() // Llamar a la función para abrir la galería
        }

        btnRegistro.setOnClickListener {
            val sharedPref = getSharedPreferences("USER_PREFS", MODE_PRIVATE)
            val usuarioActivoId = sharedPref.getInt("usuarioActivoId", -1) // Recuperar el ID del usuario activo

            if (usuarioActivoId == -1) {
                Toast.makeText(this, "No se encontró el usuario activo", Toast.LENGTH_SHORT).show()
            } else {
                // Mostrar los contactos solo del usuario activo
                mostrarContactosRegistrados(usuarioActivoId)
            }
        }

        btnGuardarContacto.setOnClickListener {
            guardarContacto()
        }

        btnEditar.setOnClickListener {
            editarContacto()
        }

        btnEliminar.setOnClickListener {
            eliminarContacto()
        }
    }

    // Función para abrir la galería
    private fun openGallery() {
        // Verificar si el permiso de imágenes ha sido concedido
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
            // Si el permiso fue concedido, abrir la galería
            pickImageFromGallery()
        } else {
            // Si no se tiene el permiso, solicitarlo
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), PERMISSION_CODE)
        }
    }

    // Manejador de la selección de imagen
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.
    GetContent())
    { uri: Uri? ->
        uri?.let {
            imageUri = it
            // Cargar la imagen con bordes redondeados utilizando Picasso y una transformación
            Picasso.get()
                .load(it)
                .transform(CircleTransform()) // Redondear la imagen
                .fit().centerCrop()
                .into(ivUserImage) // Usar Picasso para cargar la imagen redondeada
        }
    }

    // Función para lanzar la galería
    private fun pickImageFromGallery() {
        pickImageLauncher.launch("image/*")
    }

    // Manejador de permisos en tiempo de ejecución
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si el permiso fue concedido, abrir la galería
                pickImageFromGallery()
            } else {
                // Si el permiso fue denegado, mostrar un mensaje
                Toast.makeText(this, "Permiso denegado para acceder a la galería", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Clase de datos para manejar contactos
    data class Contacto(
        val id: Int,
        val nombre: String,
        val telefono: String,
        val email: String,
        val imagenUri: String? // Agregar campo para la imagen URI
    )

    private fun mostrarContactosRegistrados(usuarioActivoId: Int) {
        val admin = SQLITE_MANAGER(this, "informacion_contactos", 1)
        val db = admin.readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, nombre, telefono, email, imagen_uri FROM informacion_contactos WHERE usuario_id = ?",
            arrayOf(usuarioActivoId.toString()) // Filtrar solo los contactos del usuario activo
        )
        val contactos = mutableListOf<Contacto>() // Lista de objetos de tipo Contacto
        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val nombre = cursor.getString(1)
            val telefono = cursor.getString(2)
            val email = cursor.getString(3)
            val imagenUri = cursor.getString(4) // Obtener la imagen URI
            contactos.add(Contacto(id, nombre, telefono, email, imagenUri)) // Agregar contacto a la lista
        }
        cursor.close()
        db.close()
        if (contactos.isEmpty()) {
            Toast.makeText(this, "No hay contactos registrados", Toast.LENGTH_SHORT).show()
        } else {
            val nombres = contactos.map { it.nombre }.toTypedArray() // Obtener solo los nombres de los contactos
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Contactos Registrados")
            builder.setItems(nombres) { _, which ->
                val contactoSeleccionado = contactos[which]
                selectedContactId = contactoSeleccionado.id // Guardar el ID del contacto seleccionado
                etNombre.setText(contactoSeleccionado.nombre) // Nombre
                etTelefono.setText(contactoSeleccionado.telefono) // Teléfono
                etEmail.setText(contactoSeleccionado.email) // Email
                // Cargar la imagen del contacto si está disponible
                if (contactoSeleccionado.imagenUri.isNullOrEmpty()) {
                    ivUserImage.setImageResource(R.mipmap.nouser) // Mostrar imagen predeterminada si no hay imagen
                } else {
                    Picasso.get()
                        .load(contactoSeleccionado.imagenUri)
                        .transform(CircleTransform()) // Aplicar transformación para redondear la imagen
                        .fit().centerCrop()
                        .into(ivUserImage)
                }
                btnEditar.visibility = Button.VISIBLE // Hacer visible los botones Editar y Eliminar
                btnEliminar.visibility = Button.VISIBLE
            }
            builder.setPositiveButton("Cerrar", null)
            builder.show()
        }
    }

    private fun guardarContacto() {
        val nombre = etNombre.text.toString()
        val telefono = etTelefono.text.toString()
        val email = etEmail.text.toString()
        if (nombre.isNotEmpty() && telefono.isNotEmpty() && email.isNotEmpty()) {
            val admin = SQLITE_MANAGER(this, "informacion_contactos", 1)
            val db = admin.writableDatabase
            val values = ContentValues().apply {
                put("nombre", nombre)
                put("telefono", telefono)
                put("email", email)
                val sharedPref = getSharedPreferences("USER_PREFS", MODE_PRIVATE)
                val usuarioActivoId = sharedPref.getInt("usuarioActivoId", -1)
                put("usuario_id", usuarioActivoId)
                imageUri?.let {
                    put("imagen_uri", it.toString()) // Guardar la URI de la imagen
                } ?: putNull("imagen_uri") // Si no hay imagen, no guardar nada
            }
            val resultado = db.insert("informacion_contactos", null, values)
            db.close()
            if (resultado != -1L) {
                Toast.makeText(this, "Contacto guardado exitosamente", Toast.LENGTH_SHORT).show()
                etNombre.text.clear()
                etTelefono.text.clear()
                etEmail.text.clear()
                ivUserImage.setImageResource(R.mipmap.nouser) // Imagen predeterminada
            } else {
                Toast.makeText(this, "Error al guardar el contacto", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun editarContacto() {
        val nombre = etNombre.text.toString()
        val telefono = etTelefono.text.toString()
        val email = etEmail.text.toString()

        if (selectedContactId != null && nombre.isNotEmpty() && telefono.isNotEmpty() && email.isNotEmpty()) {
            val admin = SQLITE_MANAGER(this, "informacion_contactos", 1)
            val db = admin.writableDatabase

            val values = ContentValues().apply {
                put("nombre", nombre)
                put("telefono", telefono)
                put("email", email)
                // Actualizar la URI de la imagen solo si se ha cambiado
                imageUri?.let {
                    put("imagen_uri", it.toString())
                }
            }

            val resultado = db.update(
                "informacion_contactos",
                values,
                "id = ?",
                arrayOf(selectedContactId.toString())
            )
            db.close()

            if (resultado > 0) {
                etNombre.text.clear()
                etTelefono.text.clear()
                etEmail.text.clear()
                ivUserImage.setImageResource(R.mipmap.nouser) // Imagen predeterminada
                Toast.makeText(this, "Contacto actualizado exitosamente", Toast.LENGTH_SHORT).show()
                btnEditar.visibility = Button.GONE
                btnEliminar.visibility = Button.GONE
            } else {
                Toast.makeText(this, "Error al actualizar el contacto", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Por favor selecciona un contacto y completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun eliminarContacto() {
        if (selectedContactId != null) {
            val admin = SQLITE_MANAGER(this, "informacion_contactos", 1)
            val db = admin.writableDatabase

            val resultado = db.delete("informacion_contactos", "id = ?", arrayOf(selectedContactId.toString()))
            db.close()

            if (resultado > 0) {
                etNombre.text.clear()
                etTelefono.text.clear()
                etEmail.text.clear()
                ivUserImage.setImageResource(R.mipmap.nouser) // Imagen predeterminada
                Toast.makeText(this, "Contacto eliminado exitosamente", Toast.LENGTH_SHORT).show()
                btnEditar.visibility = Button.GONE
                btnEliminar.visibility = Button.GONE
            } else {
                Toast.makeText(this, "Error al eliminar el contacto", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Por favor selecciona un contacto para eliminar", Toast.LENGTH_SHORT).show()
        }
    }

    // Transformación para redondear la imagen
    class CircleTransform : Transformation {
        override fun key(): String {
            return "circle"
        }

        override fun transform(source: Bitmap?): Bitmap? {
            // Verificar que la imagen no sea nula
            if (source == null) return null

            val size = Math.min(source.width, source.height)
            val x = (source.width - size) / 2
            val y = (source.height - size) / 2

            // Crear un recorte cuadrado de la imagen original
            val squared = Bitmap.createBitmap(source, x, y, size, size)

            // Usar un valor predeterminado para el config en caso de que sea null
            val resultConfig = source.config ?: Bitmap.Config.ARGB_8888
            val result = Bitmap.createBitmap(size, size, resultConfig) // Usar el config predeterminado
            val canvas = Canvas(result)
            val paint = Paint()

            // Usar un shader para aplicar el recorte cuadrado a la nueva imagen
            val shader = BitmapShader(squared, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            paint.isAntiAlias = true
            paint.shader = shader
            val radius = size / 2f

            // Dibujar el círculo sobre la nueva imagen
            canvas.drawCircle(radius, radius, radius, paint)

            // Liberar la memoria de la imagen original y del recorte cuadrado
            source.recycle()
            squared.recycle()

            return result
        }
    }



    companion object {
        private const val PERMISSION_CODE = 1001
    }
}
