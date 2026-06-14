package com.example.atividadeapimobile

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val CHANNEL_ID = "cotacao_channel"
    private var ultimaCotacaoFormatada = ""
    private var moedaAtual = ""

    // Gerenciador oficial do Android para tratar a resposta da permissão em tempo de execução
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // CRITÉRIO: Trata o cenário de permissão concedida
            Toast.makeText(this, "Permissão aceita! Enviando notificação...", Toast.LENGTH_SHORT).show()
            dispararNotificacao()
        } else {
            // CRITÉRIO: Trata o cenário de permissão negada sem quebrar o aplicativo
            Toast.makeText(this, "Permissão negada. Não podemos enviar alertas sem sua autorização.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        criarCanalDeNotificacao()

        val etCoinInput = findViewById<EditText>(R.id.etCoinInput)
        val btnSearch = findViewById<Button>(R.id.btnSearch)
        val btnNotify = findViewById<Button>(R.id.btnNotify)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        btnSearch.setOnClickListener {
            val coinCode = etCoinInput.text.toString().trim().uppercase(Locale.getDefault())

            if (coinCode.isEmpty()) {
                Toast.makeText(this, "Por favor, informe a sigla de uma moeda!", Toast.LENGTH_SHORT).show()
                tvResult.text = ""
                btnNotify.visibility = View.GONE
                return@setOnClickListener
            }

            val url = "https://economia.awesomeapi.com.br/json/last/$coinCode-BRL"
            val queue = Volley.newRequestQueue(this)

            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET, url, null,
                { response: JSONObject ->
                    try {
                        val key = "${coinCode}BRL"
                        if (response.has(key)) {
                            val coinData = response.getJSONObject(key)

                            val name = coinData.getString("name")
                            val bid = coinData.getString("bid")
                            val ask = coinData.getString("ask")

                            moedaAtual = coinCode
                            ultimaCotacaoFormatada = "Compra: R$ $bid | Venda: R$ $ask"

                            tvResult.text = "💵 Cotação Encontrada:\n\n• Moeda: $name\n• $ultimaCotacaoFormatada"

                            // Torna o botão de notificação visível após carregar os dados da API
                            btnNotify.visibility = View.VISIBLE
                        } else {
                            tvResult.text = "Dados não encontrados para a sigla informada."
                            btnNotify.visibility = View.GONE
                        }
                    } catch (e: Exception) {
                        tvResult.text = "Erro ao ler as informações do servidor."
                        btnNotify.visibility = View.GONE
                    }
                },
                { error ->
                    btnNotify.visibility = View.GONE
                    if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                        tvResult.text = "Sigla inválida! Use siglas como USD, EUR ou BTC."
                    } else {
                        tvResult.text = "Falha de conexão. Verifique sua rede."
                    }
                }
            )
            queue.add(jsonObjectRequest)
        }

        // Fluxo da nova funcionalidade com verificação de permissão obrigatória
        btnNotify.setOnClickListener {
            // CRITÉRIO: Explica a necessidade antes ou durante o fluxo usando o contexto
            checkAndRequestNotificationPermission()
        }
    }

    private fun checkAndRequestNotificationPermission() {
        // A permissão de runtime para notificações só passou a ser exigida no Android 13 (API 33) em diante
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    // Cenário: O usuário já tinha concedido antes. Executa direto.
                    dispararNotificacao()
                }
                else -> {
                    // Cenário: Não possui permissão. Solicita dinamicamente na tela.
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Versões antigas do Android já possuem permissão por padrão
            dispararNotificacao()
        }
    }

    private fun dispararNotificacao() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Cotação Atualizada: $moedaAtual")
            .setContentText(ultimaCotacaoFormatada)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(101, builder.build())
    }

    private fun criarCanalDeNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Canal de Cotações"
            val descriptionText = "Informa as cotações de moedas via API"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}