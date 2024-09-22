package com.example.atividade01

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.gson.Gson

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavegacao()
        }
    }
}

@Composable
fun AppNavegacao() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "cadastro") {
        composable("cadastro") { TelaCadastroProduto(navController) }
        composable("lista") { TelaListaProduto(navController) }
        composable("detalhes/{produtoJson}") { backStackEntry ->
            val produtoJson = backStackEntry.arguments?.getString("produtoJson")
            produtoJson?.let {
                TelaDetalhesProduto(it, navController)
            }
            composable("estatisticas/{valorTotal}/{quantidadeTotal}",
            arguments = listOf(
                navArgument("valorTotal") { type = NavType.FloatType },
                navArgument("quantidadeTotal") { type = NavType.IntType }
            )
            ) { TelaEstatisticas(navController) }
        }
    }
}

@Composable
fun TelaCadastroProduto(navController: NavController) {
    val context = LocalContext.current

    var nome by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var preco by remember { mutableStateOf("") }
    var quantidade by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        TextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome do Produto") })
        TextField(value = categoria, onValueChange = { categoria = it }, label = { Text("Categoria") })
        TextField(
            value = preco,
            onValueChange = { preco = it },
            label = { Text("Preço") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
        TextField(
            value = quantidade,
            onValueChange = { quantidade = it },
            label = { Text("Quantidade em Estoque") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val precoDouble = preco.toDoubleOrNull() ?: 0.0
            val quantidadeInt = quantidade.toIntOrNull() ?: 0

            if (nome.isBlank() || categoria.isBlank() || preco.isBlank() || quantidade.isBlank()) {
                Toast.makeText(context, "Todos os campos são obrigatórios", Toast.LENGTH_SHORT).show()
            } else if (quantidadeInt < 1) {
                Toast.makeText(context, "Quantidade deve ser maior que 0", Toast.LENGTH_SHORT).show()
            } else if (precoDouble < 0) {
                Toast.makeText(context, "Preço não pode ser negativo", Toast.LENGTH_SHORT).show()
            } else {
                val produto = Produto(nome, categoria, precoDouble, quantidadeInt)
                Estoque.adicionarProduto(produto)
                navController.navigate("lista")
            }
        }) {
            Text("Cadastrar")
        }
    }
}

@Composable
fun TelaListaProduto(navController: NavController) {
    val produtos = Estoque.getProdutos()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Lista de Produtos", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))

        if (produtos.isEmpty()) {
            Text("Nenuhum produto cadastrado.")
        } else {
            LazyColumn {
                items(produtos) { produto ->
                    ProdutoItem(produto, navController)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                val valorTotal = Estoque.calcularValorTotalEstoque()
                val quantidadeTotal = Estoque.calcularQuantidadeTotalProdutos()
                // Navegue para a tela de estatísticas, passando esses valores
                navController.navigate("estatisticas/${valorTotal.toFloat()}/${quantidadeTotal}")
            }) {
                Text("Ver Estatísticas")
            }
        }
    }
}

@Composable
fun TelaEstatisticas(navController: NavController) {
    val valorTotal = try {
        Estoque.calcularValorTotalEstoque()
    } catch (e: Exception) {
        0.0 // Valor padrão em caso de erro
    }

    val quantidadeTotal = try {
        Estoque.calcularQuantidadeTotalProdutos()
    } catch (e: Exception) {
        0 // Valor padrão em caso de erro
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)){
        Text("Estatísticas do Estoque", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))

        Text(text = "Valor Total do Estoque: $valorTotal")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Quantidade Total de Produtos: $quantidadeTotal")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate("lista") }) {
            Text("Lista de Produtos")
        }
    }
}

@Composable
fun ProdutoItem(produto: Produto, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("${produto.nome} (${produto.quantidade} unidades)", style = MaterialTheme.typography.bodySmall)
        Button(onClick = {
            // Serializa o objeto Produto usando Gson
            val gson = Gson()
            val produtoJson = gson.toJson(produto)

            // Navega para a tela detalhes, passando o produto serializado
            navController.navigate("detalhes/$produtoJson")
        }) {
            Text("Detalhes")
        }
    }
}

@Composable
fun TelaDetalhesProduto(produtoJson: String, navController: NavController) {
    // Deserializa o produto passado como JSON
    val gson = Gson()
    val produto = gson.fromJson(produtoJson, Produto::class.java)

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Detalhes do produto", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))

        Text("Nome: ${produto.nome}")
        Text("Categoria: ${produto.categoria}")
        Text("Preço: R$${produto.preco}")
        Text("Quantidade em Estoque: ${produto.quantidade}")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            navController.popBackStack()
        }) {
            Text("Lista de Produtos")
        }
    }
}