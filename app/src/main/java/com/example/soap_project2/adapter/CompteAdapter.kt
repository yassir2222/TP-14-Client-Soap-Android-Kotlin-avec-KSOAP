package com.example.soap_project2.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.soap_project2.R
import com.example.soap_project2.beans.Compte
import com.google.android.material.chip.Chip

import java.text.SimpleDateFormat
import java.util.*

class CompteAdapter : RecyclerView.Adapter<CompteAdapter.CompteViewHolder>() {
    private var comptes = mutableListOf<Compte>()
    var onDeleteClick: ((Compte) -> Unit)? = null

    fun updateComptes(newComptes: List<Compte>) {
        comptes.clear()
        comptes.addAll(newComptes)
        notifyDataSetChanged()
    }

    fun removeCompte(compte: Compte) {
        val index = comptes.indexOfFirst { it.id == compte.id }
        if (index != -1) {
            comptes.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return CompteViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompteViewHolder, position: Int) {
        holder.bind(comptes[position])
    }

    override fun getItemCount() = comptes.size

    inner class CompteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val idTxt: TextView = view.findViewById(R.id.textId)
        private val soldeTxt: TextView = view.findViewById(R.id.textSolde)
        private val typeChip: Chip = view.findViewById(R.id.textType)
        private val dateTxt: TextView = view.findViewById(R.id.textDate)
        private val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)

        fun bind(compte: Compte) {
            idTxt.text = "Compte N° ${compte.id}"
            soldeTxt.text = "${compte.solde} DH"
            typeChip.text = compte.type.name

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dateTxt.text = "Créé le : ${sdf.format(compte.dateCreation)}"

            btnDelete.setOnClickListener {
                onDeleteClick?.invoke(compte)
            }
        }
    }
}