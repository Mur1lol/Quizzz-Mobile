package com.example.quizzz.app

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizzz.R
import com.example.quizzz.entidades.Categoria
import com.example.quizzz.entidades.CategoriaResponse
import com.example.quizzz.servicos.ListaCategoriaService
import com.example.quizzz.ui.CategoriaListListener
import com.example.quizzz.ui.CategoriasAdapter
import kotlinx.android.synthetic.main.activity_config.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ConfigActivity : AppCompatActivity(), CategoriaListListener {

    lateinit var retrofit: Retrofit
    lateinit var service: ListaCategoriaService
    lateinit var adapter: CategoriasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        var prefsDificuldade = getSharedPreferences("dificuldade", Context.MODE_PRIVATE)
        var prefsCategoria = getSharedPreferences("categoria", Context.MODE_PRIVATE)

        configuraRetrofit()
        carregarLista()

        btJogar.setOnClickListener {
            val intent = Intent(this, ListaActivity::class.java)
            startActivity(intent)
        }

        btAleatorio.setOnClickListener {
            var edDif = prefsDificuldade.edit()
            var edCat = prefsCategoria.edit()

            edDif.putString("dificuldade", null)
            edCat.putInt("categoria", 0)

            edDif.apply()
            edCat.apply()

            Toast.makeText(this@ConfigActivity, getString(R.string.aleatorio), Toast.LENGTH_SHORT).show()
        }

        var dificuldade = prefsDificuldade.getString("dificuldade", null)
        var edDif = prefsDificuldade.edit()

        if (dificuldade == "easy") {
            btFacil.isChecked = true
        }
        else if (dificuldade == "medium") {
            btMedio.isChecked = true
        }
        else if (dificuldade == "hard") {
            btDificil.isChecked = true
        }

        btFacil.setOnClickListener {
            edDif.putString("dificuldade", "easy")
            edDif.apply()
        }

        btMedio.setOnClickListener {
            edDif.putString("dificuldade", "medium")
            edDif.apply()
        }

        btDificil.setOnClickListener {
            edDif.putString("dificuldade", "hard")
            edDif.apply()
        }

        var categoria = prefsCategoria.getInt("categoria", 0)

        Log.e("Dificulade", ""+dificuldade)
        Log.e("Categoria", ""+categoria)
    }

    fun configuraRetrofit() {
        //https://opentdb.com/api_category.php
        retrofit = Retrofit.Builder()
            .baseUrl("https://opentdb.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        service = retrofit.create(ListaCategoriaService::class.java)
    }

    fun carregarLista() {
        service.getListaCategoria().enqueue(object: Callback<CategoriaResponse>{
            override fun onFailure(call: Call<CategoriaResponse>, t: Throwable) {}

            override fun onResponse(call: Call<CategoriaResponse>, response: Response<CategoriaResponse>) {
                val categorias = response.body()?.trivia_categories
                if (categorias != null) {
                    configuraRecyclerView(categorias)
                }
            }
        })
    }

    fun configuraRecyclerView(categorias: List<Categoria>) {
        adapter = CategoriasAdapter(categorias, this)
        listCategorias.adapter = adapter
        listCategorias.layoutManager = LinearLayoutManager(this@ConfigActivity, RecyclerView.VERTICAL, false)
    }

    override fun preferences(categoria: Categoria) {
        var prefsCategoria = getSharedPreferences("categoria", Context.MODE_PRIVATE)
        var edCat = prefsCategoria.edit()

        edCat.putInt("categoria", categoria.id.toInt())
        edCat.apply()
    }

}
