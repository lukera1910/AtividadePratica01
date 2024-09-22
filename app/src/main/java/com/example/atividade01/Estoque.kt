package com.example.atividade01

class Estoque {
    companion object {
        private val listaProdutos = mutableListOf<Produto>()

        fun adicionarProduto(produto: Produto) {
            listaProdutos.add(produto)
        }

        fun calcularValorTotalEstoque(): Double {
            return listaProdutos.sumOf { it.preco * it.quantidade }
        }

        fun calcularQuantidadeTotalProdutos(): Int {
            return listaProdutos.sumOf { it.quantidade }
        }

        fun getProdutos(): List<Produto> {
            return listaProdutos
        }
    }
}