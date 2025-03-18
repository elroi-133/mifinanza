package com.example.mifinanza

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment


class AcercaDeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View =
            inflater.inflate(R.layout.fragment_acerca_de, container, false) // Reemplaza tu_layout_xml

        // Encuentra las ImageView y TextView
        val facebookIcon = view.findViewById<ImageView>(R.id.facebook_icon)
        val facebookLink = view.findViewById<TextView>(R.id.facebook_link)

        val instagramIcon = view.findViewById<ImageView>(R.id.instagram_icon)
        val instagramLink = view.findViewById<TextView>(R.id.instagram_link)

        val whatsappIcon = view.findViewById<ImageView>(R.id.whatsapp_icon)
        val whatsappLink = view.findViewById<TextView>(R.id.whatsapp_link)

        val telegramIcon = view.findViewById<ImageView>(R.id.telegram_icon)
        val telegramLink = view.findViewById<TextView>(R.id.telegram_link)

        val linkedinIcon = view.findViewById<ImageView>(R.id.linkedin_icon)
        val linkedinLink = view.findViewById<TextView>(R.id.linkedin_link)

        val githubIcon = view.findViewById<ImageView>(R.id.github_icon)
        val githubLink = view.findViewById<TextView>(R.id.github_link)

        // Configura los OnClickListeners
        facebookIcon.setOnClickListener { v: View? -> openLink(facebookLink.text.toString()) }
        instagramIcon.setOnClickListener { v: View? -> openLink(instagramLink.text.toString()) }
        whatsappIcon.setOnClickListener { v: View? -> openLink(whatsappLink.text.toString()) }
        telegramIcon.setOnClickListener { v: View? -> openLink(telegramLink.text.toString()) }
        linkedinIcon.setOnClickListener { v: View? -> openLink(linkedinLink.text.toString()) }
        githubIcon.setOnClickListener { v: View? -> openLink(githubLink.text.toString()) }

        return view
    }

    private fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}