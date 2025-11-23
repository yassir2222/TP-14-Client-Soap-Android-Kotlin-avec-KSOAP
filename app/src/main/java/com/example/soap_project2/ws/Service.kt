package com.example.soap_project2.ws



import android.util.Log
import com.example.soap_project2.beans.Compte
import com.example.soap_project2.beans.TypeCompte
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE
import java.text.SimpleDateFormat
import java.util.*

class Service {
    // URL avec PORT 8080
    // "10.0.2.2" = localhost de ton PC vu par l'émulateur
    private val NAMESPACE = "http://ws.demo.example.org/"
    private val URL = "http://192.168.1.48:8080/services/ws"

    private val METHOD_GET_COMPTES = "getComptes"
    private val METHOD_CREATE_COMPTE = "createCompte"
    private val METHOD_DELETE_COMPTE = "deleteCompte"

    fun getComptes(): List<Compte> {
        val comptes = mutableListOf<Compte>()
        val request = SoapObject(NAMESPACE, METHOD_GET_COMPTES)
        val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
        envelope.dotNet = false
        envelope.setOutputSoapObject(request)

        try {
            val transport = HttpTransportSE(URL)
            // Timeout de 5 secondes pour éviter de bloquer si le serveur est éteint
            transport.debug = true
            transport.call("", envelope)
            if (envelope.bodyIn is org.ksoap2.SoapFault) {
                val str = (envelope.bodyIn as org.ksoap2.SoapFault).faultstring
                Log.e("SOAP_ERROR", "Erreur serveur : $str")
                return emptyList()
            }
            val response = envelope.bodyIn as SoapObject

            for (i in 0 until response.propertyCount) {
                val property = response.getProperty(i)
                if (property is SoapObject) {
                    val compte = parseSoapToCompte(property)
                    comptes.add(compte)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return comptes
    }

    fun createCompte(solde: Double, type: TypeCompte): Boolean {
        val request = SoapObject(NAMESPACE, METHOD_CREATE_COMPTE)
        request.addProperty("solde", solde.toString())
        request.addProperty("type", type.name)

        val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
        envelope.dotNet = false
        envelope.setOutputSoapObject(request)

        return try {
            val transport = HttpTransportSE(URL)
            transport.call("", envelope)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteCompte(id: Long): Boolean {
        val request = SoapObject(NAMESPACE, METHOD_DELETE_COMPTE)
        request.addProperty("id", id)

        val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
        envelope.dotNet = false
        envelope.setOutputSoapObject(request)

        return try {
            val transport = HttpTransportSE(URL)
            transport.call("", envelope)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun parseSoapToCompte(soapObject: SoapObject): Compte {
        val id = soapObject.getPropertySafely("id").toString().toLongOrNull()
        val solde = soapObject.getPropertySafely("solde").toString().toDoubleOrNull() ?: 0.0
        val dateStr = soapObject.getPropertySafely("dateCreation")

        // Gestion robuste de la date (prend les 10 premiers caractères yyyy-MM-dd)
        val date = try {
            // Convert the result of take(10) to a String before parsing
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr.toString().take(10))
        } catch (e: Exception) { Date() }

        val typeStr = soapObject.getPropertySafely("type")
        // Note: The 'as String' cast here is redundant since getPropertySafely already returns a String.
        val type = try { TypeCompte.valueOf(typeStr as String) } catch(e:Exception) { TypeCompte.COURANT }

        return Compte(id, solde, date ?: Date(), type)
    }

    private fun SoapObject.getPropertySafely(name: String): String {
        return try {
            if (this.hasProperty(name)) {
                this.getProperty(name).toString()
            } else { "" }
        } catch (e: Exception) { "" }
    }
}