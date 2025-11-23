package com.example.soap_project2

import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.soap_project2.adapter.CompteAdapter
import com.example.soap_project2.beans.TypeCompte
import com.example.soap_project2.beans.Compte
import com.example.soap_project2.ws.Service

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: ExtendedFloatingActionButton
    private val adapter = CompteAdapter()
    private val service = Service()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialisation des vues
        recyclerView = findViewById(R.id.recyclerView)
        fabAdd = findViewById(R.id.fabAdd)

        setupRecyclerView()

        fabAdd.setOnClickListener { showAddDialog() }

        // Chargement initial
        loadData()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Listener sur le bouton supprimer
        adapter.onDeleteClick = { compte ->
            confirmDelete(compte)
        }
    }

    private fun loadData() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val comptes = service.getComptes()
                withContext(Dispatchers.Main) {
                    if (comptes.isEmpty()) {
                        Toast.makeText(this@MainActivity, "Aucun compte trouvé ou erreur serveur", Toast.LENGTH_LONG).show()
                    }
                    adapter.updateComptes(comptes)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Erreur connexion: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showAddDialog() {
        val dialogView = layoutInflater.inflate(R.layout.popup, null)

        MaterialAlertDialogBuilder(this)
            .setTitle("Nouveau Compte")
            .setView(dialogView)
            .setPositiveButton("Ajouter") { _, _ ->
                val etSolde = dialogView.findViewById<TextInputEditText>(R.id.etSolde)
                val radioCourant = dialogView.findViewById<RadioButton>(R.id.radioCourant)

                val soldeStr = etSolde.text.toString()
                if (soldeStr.isNotEmpty()) {
                    val solde = soldeStr.toDoubleOrNull() ?: 0.0
                    val type = if (radioCourant.isChecked) TypeCompte.COURANT else TypeCompte.EPARGNE

                    createCompte(solde, type)
                } else {
                    Toast.makeText(this, "Solde invalide", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun createCompte(solde: Double, type: TypeCompte) {
        lifecycleScope.launch(Dispatchers.IO) {
            val success = service.createCompte(solde, type)
            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(this@MainActivity, "Compte créé avec succès", Toast.LENGTH_SHORT).show()
                    loadData()
                } else {
                    Toast.makeText(this@MainActivity, "Échec de la création", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun confirmDelete(compte: Compte) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirmation")
            .setMessage("Voulez-vous vraiment supprimer le compte N°${compte.id} ?")
            .setPositiveButton("Supprimer") { _, _ ->
                deleteCompte(compte)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun deleteCompte(compte: Compte) {
        lifecycleScope.launch(Dispatchers.IO) {
            if (compte.id != null) {
                val success = service.deleteCompte(compte.id)
                withContext(Dispatchers.Main) {
                    if (success) {
                        adapter.removeCompte(compte)
                        Toast.makeText(this@MainActivity, "Compte supprimé", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}